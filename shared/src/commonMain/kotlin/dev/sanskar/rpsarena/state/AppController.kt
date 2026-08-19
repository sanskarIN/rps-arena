package dev.sanskar.rpsarena.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.sanskar.rpsarena.data.AppRepository
import dev.sanskar.rpsarena.domain.Achievement
import dev.sanskar.rpsarena.domain.AchievementCatalog
import dev.sanskar.rpsarena.domain.AppSettings
import dev.sanskar.rpsarena.domain.CpuStrategy
import dev.sanskar.rpsarena.domain.GameRules
import dev.sanskar.rpsarena.domain.GameStats
import dev.sanskar.rpsarena.domain.Gesture
import dev.sanskar.rpsarena.domain.MatchState
import dev.sanskar.rpsarena.domain.OpponentMode
import dev.sanskar.rpsarena.domain.RoundOutcome
import dev.sanskar.rpsarena.domain.RoundRecord

class AppController(private val repository: AppRepository) {
    var settings by mutableStateOf(repository.loadSettings())
        private set

    var stats by mutableStateOf(repository.loadStats())
        private set

    var match by mutableStateOf(MatchState())
        private set

    private val mutableHistory = mutableStateListOf<RoundRecord>()
    val history: List<RoundRecord> get() = mutableHistory
    val achievements: List<Achievement> get() = AchievementCatalog.evaluate(stats)

    private val playerHistory = mutableListOf<Gesture>()
    private var cpuStrategy = CpuStrategy(settings.seed)

    init {
        mutableHistory.addAll(repository.loadHistory())
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val previous = settings
        val next = transform(settings)
        settings = next.copy(roundTimerSeconds = next.roundTimerSeconds.coerceIn(0, 60))
        repository.saveSettings(settings)
        if (settings.seed != previous.seed) cpuStrategy = CpuStrategy(settings.seed)
        if (
            settings.variant != previous.variant ||
            settings.gameMode != previous.gameMode ||
            settings.opponentMode != previous.opponentMode
        ) {
            restartMatch()
        }
    }

    fun submitGesture(gesture: Gesture) {
        if (match.finished || gesture !in settings.variant.gestures) return

        if (settings.opponentMode == OpponentMode.LOCAL_TWO_PLAYER && match.pendingPlayerOne == null) {
            match = match.copy(
                pendingPlayerOne = gesture,
                message = "Player 1 locked in. Pass the device to Player 2.",
            )
            return
        }

        val playerOne = when (settings.opponentMode) {
            OpponentMode.CPU -> gesture
            OpponentMode.LOCAL_TWO_PLAYER -> match.pendingPlayerOne ?: return
        }
        val playerTwo = when (settings.opponentMode) {
            OpponentMode.CPU -> cpuStrategy.choose(settings.difficulty, settings.variant, playerHistory)
            OpponentMode.LOCAL_TWO_PLAYER -> gesture
        }

        resolveRound(playerOne, playerTwo)
    }

    fun onTimerExpired() {
        if (settings.roundTimerSeconds <= 0 || match.finished) return
        submitGesture(settings.variant.gestures.first())
    }

    fun restartMatch() {
        match = MatchState()
        playerHistory.clear()
        cpuStrategy = CpuStrategy(settings.seed)
    }

    fun clearHistory() {
        mutableHistory.clear()
        repository.saveHistory(emptyList())
    }

    fun resetAllData() {
        repository.resetAll()
        settings = AppSettings()
        stats = GameStats()
        mutableHistory.clear()
        playerHistory.clear()
        cpuStrategy = CpuStrategy(settings.seed)
        match = MatchState()
        repository.saveSettings(settings)
        repository.saveStats(stats)
        repository.saveHistory(emptyList())
    }

    fun exportBackup(): String = repository.exportBackup()

    fun importBackup(raw: String): Boolean {
        if (!repository.importBackup(raw)) return false
        settings = repository.loadSettings()
        stats = repository.loadStats()
        mutableHistory.clear()
        mutableHistory.addAll(repository.loadHistory())
        restartMatch()
        return true
    }

    private fun resolveRound(playerOne: Gesture, playerTwo: Gesture) {
        val outcome = GameRules.outcome(playerOne, playerTwo, settings.variant)
        val record = RoundRecord(match.roundNumber, playerOne, playerTwo, outcome)

        val playerOneScore = match.playerOneScore + if (outcome == RoundOutcome.PLAYER_ONE_WIN) 1 else 0
        val playerTwoScore = match.playerTwoScore + if (outcome == RoundOutcome.PLAYER_TWO_WIN) 1 else 0
        val draws = match.draws + if (outcome == RoundOutcome.DRAW) 1 else 0
        val finished = GameRules.isMatchFinished(settings.gameMode, playerOneScore, playerTwoScore)

        stats = when (outcome) {
            RoundOutcome.PLAYER_ONE_WIN -> stats.copy(
                wins = stats.wins + 1,
                currentStreak = stats.currentStreak + 1,
                bestStreak = maxOf(stats.bestStreak, stats.currentStreak + 1),
            )
            RoundOutcome.PLAYER_TWO_WIN -> stats.copy(
                losses = stats.losses + 1,
                currentStreak = 0,
            )
            RoundOutcome.DRAW -> stats.copy(draws = stats.draws + 1)
        }

        mutableHistory += record
        while (mutableHistory.size > 30) mutableHistory.removeAt(0)
        if (settings.opponentMode == OpponentMode.CPU) playerHistory += playerOne

        repository.saveStats(stats)
        repository.saveHistory(mutableHistory)

        val message = when {
            finished && playerOneScore > playerTwoScore -> "Match complete — Player 1 wins."
            finished -> "Match complete — Player 2 wins."
            outcome == RoundOutcome.PLAYER_ONE_WIN -> "Player 1 wins the round."
            outcome == RoundOutcome.PLAYER_TWO_WIN -> if (settings.opponentMode == OpponentMode.CPU) "CPU wins the round." else "Player 2 wins the round."
            else -> "Draw round."
        }

        match = MatchState(
            playerOneScore = playerOneScore,
            playerTwoScore = playerTwoScore,
            draws = draws,
            roundNumber = match.roundNumber + 1,
            pendingPlayerOne = null,
            lastRound = record,
            finished = finished,
            message = message,
        )
    }
}
