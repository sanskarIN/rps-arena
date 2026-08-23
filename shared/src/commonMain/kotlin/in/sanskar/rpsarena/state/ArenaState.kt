package `in`.sanskar.rpsarena.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import `in`.sanskar.rpsarena.data.ArenaBackupImportResult
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
    var config by mutableStateOf(MatchConfig())
        private set
    var match by mutableStateOf(MatchSnapshot(config))
        private set
    var pendingPlayerOne by mutableStateOf<Gesture?>(null)
        private set

    private var cpu = CpuStrategy(config.seed)

    val historyEntries: List<ArenaHistoryEntry> get() = repository.loadHistoryEntries()
    val achievements: List<Achievement> get() = listOf(
        Achievement("first_win", stats.wins >= 1),
        Achievement("ten_rounds", stats.roundsPlayed >= 10),
        Achievement("streak_3", stats.bestStreak >= 3),
        Achievement("streak_7", stats.bestStreak >= 7),
        Achievement("century", stats.roundsPlayed >= 100),
    )

    fun navigate(to: ArenaScreen) { screen = to }

    fun completeOnboarding() {
        updateSettings(settings.copy(onboardingComplete = true))
    }

    fun updateSettings(value: ArenaSettings) {
        settings = value
        repository.saveSettings(value)
    }

    fun exportBackup(): String = repository.exportBackup()

    fun importBackup(raw: String): ArenaBackupImportResult {
        val result = repository.importBackup(raw)
        if (result is ArenaBackupImportResult.Success) {
            settings = repository.loadSettings()
            stats = repository.loadStats()
        }
        return result
    }

    fun updateConfig(value: MatchConfig) {
        config = value
        resetMatch()
    }

    fun resetMatch() {
        cpu = CpuStrategy(config.seed)
        match = MatchSnapshot(config)
        pendingPlayerOne = null
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
            return
        }
        pendingPlayerOne = null
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
        repository.addHistoryEntry(
            ArenaHistoryEntry.Round(
                playerOne = round.playerOne,
                playerTwo = round.playerTwo,
                outcome = round.outcome,
            ),
        )
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
}
