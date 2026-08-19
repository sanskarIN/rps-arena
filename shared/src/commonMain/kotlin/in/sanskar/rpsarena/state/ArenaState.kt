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
    var config by mutableStateOf(MatchConfig())
        private set
    var match by mutableStateOf(MatchSnapshot(config))
        private set
    var pendingPlayerOne by mutableStateOf<Gesture?>(null)
        private set
    var backupText by mutableStateOf("")
        private set
    var dataMessage by mutableStateOf<String?>(null)
        private set
    var lastAnnouncement by mutableStateOf<String?>(null)
        private set

    private var cpu = CpuStrategy(config.seed)

    val history: List<String> get() = repository.loadHistory()
    val recentTrend: ArenaTrend get() = repository.loadRecentTrend()
    val achievements: List<Achievement> get() = listOf(
        Achievement("first_win", "First Victory", "Win your first round", stats.wins >= 1),
        Achievement("ten_rounds", "Arena Regular", "Play 10 rounds", stats.roundsPlayed >= 10),
        Achievement("streak_3", "On Fire", "Reach a 3-round win streak", stats.bestStreak >= 3),
        Achievement("streak_7", "Unstoppable", "Reach a 7-round win streak", stats.bestStreak >= 7),
        Achievement("century", "Century", "Play 100 rounds", stats.roundsPlayed >= 100),
    )

    fun navigate(to: ArenaScreen) {
        screen = to
        dataMessage = null
    }

    fun completeOnboarding() {
        updateSettings(settings.copy(onboardingComplete = true))
    }

    fun updateSettings(value: ArenaSettings) {
        repository.saveSettings(value)
        settings = repository.loadSettings()
    }

    fun updateConfig(value: MatchConfig) {
        config = value
        resetMatch()
    }

    fun resetMatch() {
        cpu = CpuStrategy(config.seed)
        match = MatchSnapshot(config)
        pendingPlayerOne = null
        lastAnnouncement = null
    }

    fun play(gesture: Gesture) {
        if (match.finished && config.matchMode !in setOf(MatchMode.ENDLESS, MatchMode.STREAK)) return
        when (config.opponentMode) {
            OpponentMode.CPU -> playAgainstCpu(gesture)
            OpponentMode.LOCAL_TWO_PLAYER -> playLocalTwoPlayer(gesture)
        }
    }

    fun expireCurrentTurn() {
        if (config.roundTimerSeconds == 0) return
        if (match.finished && config.matchMode !in setOf(MatchMode.ENDLESS, MatchMode.STREAK)) return

        when (config.opponentMode) {
            OpponentMode.CPU -> recordResolvedRound(
                playerOne = null,
                playerTwo = null,
                outcome = RoundOutcome.PLAYER_TWO_WIN,
                reason = RoundEndReason.PLAYER_ONE_TIMEOUT,
            )
            OpponentMode.LOCAL_TWO_PLAYER -> {
                val first = pendingPlayerOne
                if (first == null) {
                    recordResolvedRound(
                        playerOne = null,
                        playerTwo = null,
                        outcome = RoundOutcome.PLAYER_TWO_WIN,
                        reason = RoundEndReason.PLAYER_ONE_TIMEOUT,
                    )
                } else {
                    recordResolvedRound(
                        playerOne = first,
                        playerTwo = null,
                        outcome = RoundOutcome.PLAYER_ONE_WIN,
                        reason = RoundEndReason.PLAYER_TWO_TIMEOUT,
                    )
                }
                pendingPlayerOne = null
            }
        }
    }

    fun updateBackupText(value: String) {
        backupText = value.take(MAX_BACKUP_INPUT_CHARS)
        dataMessage = null
    }

    fun prepareBackup() {
        backupText = repository.exportBackup()
        dataMessage = "Backup text is ready to copy and save securely."
    }

    fun importBackup() {
        val result = repository.importBackup(backupText)
        dataMessage = result.message
        if (result.imported) {
            settings = repository.loadSettings()
            stats = repository.loadStats()
            resetMatch()
        }
    }

    fun clearUserData() {
        repository.clearUserData(preserveOnboarding = true)
        settings = repository.loadSettings()
        stats = repository.loadStats()
        backupText = ""
        resetMatch()
        dataMessage = "Local statistics, history, and preferences were reset."
    }

    private fun playAgainstCpu(player: Gesture) {
        val priorPlayerMoves = match.rounds.mapNotNull { it.playerOne }
        val opponent = cpu.choose(config.difficulty, config.variant, priorPlayerMoves)
        recordPlayedRound(player, opponent)
    }

    private fun playLocalTwoPlayer(gesture: Gesture) {
        val first = pendingPlayerOne
        if (first == null) {
            pendingPlayerOne = gesture
            lastAnnouncement = "Player 1 move locked. Pass the device to Player 2."
            return
        }
        pendingPlayerOne = null
        recordPlayedRound(first, gesture)
    }

    private fun recordPlayedRound(playerOne: Gesture, playerTwo: Gesture) {
        recordResolvedRound(
            playerOne = playerOne,
            playerTwo = playerTwo,
            outcome = RulesEngine.resolve(playerOne, playerTwo),
            reason = RoundEndReason.PLAYED,
        )
    }

    private fun recordResolvedRound(
        playerOne: Gesture?,
        playerTwo: Gesture?,
        outcome: RoundOutcome,
        reason: RoundEndReason,
    ) {
        val p1 = match.playerOneScore + if (outcome == RoundOutcome.PLAYER_ONE_WIN) 1 else 0
        val p2 = match.playerTwoScore + if (outcome == RoundOutcome.PLAYER_TWO_WIN) 1 else 0
        val draws = match.draws + if (outcome == RoundOutcome.DRAW) 1 else 0
        val target = config.roundsToWin
        val finished = target != null && (p1 >= target || p2 >= target)
        val round = RoundRecord(playerOne, playerTwo, outcome, reason)
        match = match.copy(
            rounds = match.rounds + round,
            playerOneScore = p1,
            playerTwoScore = p2,
            draws = draws,
            finished = finished,
        )
        updateStats(outcome)
        val historyLine = historyLine(round)
        repository.addHistory(historyLine)
        lastAnnouncement = historyLine
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

    private fun historyLine(round: RoundRecord): String = when (round.endReason) {
        RoundEndReason.PLAYER_ONE_TIMEOUT -> "Player 1 timed out — Player 2 won"
        RoundEndReason.PLAYER_TWO_TIMEOUT -> "Player 2 timed out — Player 1 won"
        RoundEndReason.PLAYED -> {
            val first = requireNotNull(round.playerOne)
            val second = requireNotNull(round.playerTwo)
            "${first.label} vs ${second.label} — ${when (round.outcome) {
                RoundOutcome.PLAYER_ONE_WIN -> "Player 1 won"
                RoundOutcome.PLAYER_TWO_WIN -> "Player 2 won"
                RoundOutcome.DRAW -> "Draw"
            }}"
        }
    }

    companion object {
        private const val MAX_BACKUP_INPUT_CHARS = 128 * 1024
    }
}
