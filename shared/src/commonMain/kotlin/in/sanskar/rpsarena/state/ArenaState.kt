package `in`.sanskar.rpsarena.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.engine.CpuStrategy
import `in`.sanskar.rpsarena.engine.RulesEngine
import `in`.sanskar.rpsarena.model.*

enum class ArenaScreen { HOME, PLAY, HISTORY, STATS, ACHIEVEMENTS, SETTINGS, ABOUT }

class ArenaState(private val repository: ArenaRepository = ArenaRepository()) {
    var screen by mutableStateOf(ArenaScreen.HOME)
        private set
    var settings by mutableStateOf(repository.loadSettings())
        private set
    var stats by mutableStateOf(repository.loadStats())
        private set
    var config by mutableStateOf(repository.loadConfig())
        private set
    var match by mutableStateOf(MatchSnapshot(config))
        private set
    var history by mutableStateOf(repository.loadHistory())
        private set
    var pendingPlayerOne by mutableStateOf<Gesture?>(null)
        private set
    var localTurnMessage by mutableStateOf("Player 1: choose secretly")
        private set

    private var cpu = CpuStrategy(config.seed)

    val achievements: List<Achievement> get() = listOf(
        Achievement("first_win", "First Victory", "Win your first round", stats.wins >= 1),
        Achievement("ten_rounds", "Arena Regular", "Play 10 rounds", stats.roundsPlayed >= 10),
        Achievement("streak_3", "On Fire", "Reach a 3-round win streak", stats.bestStreak >= 3),
        Achievement("streak_7", "Unstoppable", "Reach a 7-round win streak", stats.bestStreak >= 7),
        Achievement("century", "Century", "Play 100 rounds", stats.roundsPlayed >= 100),
    )

    fun navigate(to: ArenaScreen) {
        screen = to
    }

    fun completeOnboarding() {
        updateSettings(settings.copy(onboardingComplete = true))
    }

    fun updateSettings(value: ArenaSettings) {
        settings = value
        repository.saveSettings(value)
    }

    fun updateConfig(value: MatchConfig) {
        val sanitized = value.copy(roundTimerSeconds = value.roundTimerSeconds.coerceIn(0, 60))
        config = sanitized
        repository.saveConfig(sanitized)
        resetMatch()
    }

    fun resetMatch() {
        cpu = CpuStrategy(config.seed)
        match = MatchSnapshot(config)
        pendingPlayerOne = null
        localTurnMessage = "Player 1: choose secretly"
    }

    fun clearHistory() {
        repository.clearHistory()
        history = emptyList()
    }

    fun exportBackup(): String = repository.exportBackup()

    fun importBackup(raw: String): Boolean {
        if (!repository.importBackup(raw)) return false
        settings = repository.loadSettings()
        stats = repository.loadStats()
        config = repository.loadConfig()
        history = repository.loadHistory()
        resetMatch()
        screen = ArenaScreen.HOME
        return true
    }

    fun resetAllData() {
        repository.resetAll()
        settings = repository.loadSettings()
        stats = repository.loadStats()
        config = repository.loadConfig()
        history = emptyList()
        resetMatch()
        screen = ArenaScreen.HOME
    }

    fun playTimeoutMove() {
        if (config.roundTimerSeconds <= 0) return
        if (match.finished && config.matchMode !in setOf(MatchMode.ENDLESS, MatchMode.STREAK)) return
        val gestures = Gesture.availableFor(config.variant)
        val turnOffset = if (pendingPlayerOne == null) 0 else 1
        val deterministicIndex = ((config.seed.toLong() + match.rounds.size + turnOffset) and 0x7fff_ffffL) % gestures.size
        play(gestures[deterministicIndex.toInt()])
    }

    fun play(gesture: Gesture) {
        if (match.finished && config.matchMode !in setOf(MatchMode.ENDLESS, MatchMode.STREAK)) return
        when (config.opponentMode) {
            OpponentMode.CPU -> playAgainstCpu(gesture)
            OpponentMode.LOCAL_TWO_PLAYER -> playLocalTwoPlayer(gesture)
        }
    }

    private fun playAgainstCpu(player: Gesture) {
        val priorPlayerMoves = match.rounds.map { it.playerOne }
        val opponent = cpu.choose(config.difficulty, config.variant, priorPlayerMoves)
        recordRound(player, opponent)
    }

    private fun playLocalTwoPlayer(gesture: Gesture) {
        val first = pendingPlayerOne
        if (first == null) {
            pendingPlayerOne = gesture
            localTurnMessage = "Player 2: choose now — Player 1 move is hidden"
            return
        }
        pendingPlayerOne = null
        localTurnMessage = "Player 1: choose secretly"
        recordRound(first, gesture)
    }

    private fun recordRound(playerOne: Gesture, playerTwo: Gesture) {
        val outcome = RulesEngine.resolve(playerOne, playerTwo)
        val p1 = match.playerOneScore + if (outcome == RoundOutcome.PLAYER_ONE_WIN) 1 else 0
        val p2 = match.playerTwoScore + if (outcome == RoundOutcome.PLAYER_TWO_WIN) 1 else 0
        val draws = match.draws + if (outcome == RoundOutcome.DRAW) 1 else 0
        val target = config.roundsToWin
        val finished = target != null && (p1 >= target || p2 >= target)
        val round = RoundRecord(playerOne, playerTwo, outcome)
        match = match.copy(
            rounds = match.rounds + round,
            playerOneScore = p1,
            playerTwoScore = p2,
            draws = draws,
            finished = finished,
        )
        updateStats(outcome)
        repository.addHistory(historyLine(round))
        history = repository.loadHistory()
    }

    private fun updateStats(outcome: RoundOutcome) {
        val won = outcome == RoundOutcome.PLAYER_ONE_WIN
        val lost = outcome == RoundOutcome.PLAYER_TWO_WIN
        val newStreak = if (won) stats.currentStreak + 1 else 0
        stats = stats.copy(
            roundsPlayed = stats.roundsPlayed + 1,
            wins = stats.wins + if (won) 1 else 0,
            losses = stats.losses + if (lost) 1 else 0,
            draws = stats.draws + if (outcome == RoundOutcome.DRAW) 1 else 0,
            currentStreak = newStreak,
            bestStreak = maxOf(stats.bestStreak, newStreak),
        )
        repository.saveStats(stats)
    }

    private fun historyLine(round: RoundRecord): String =
        "${round.playerOne.label} vs ${round.playerTwo.label} — ${when (round.outcome) {
            RoundOutcome.PLAYER_ONE_WIN -> "Player 1 won"
            RoundOutcome.PLAYER_TWO_WIN -> "Player 2 won"
            RoundOutcome.DRAW -> "Draw"
        }}"
}
