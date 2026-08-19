package `in`.sanskar.rpsarena.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.engine.CpuStrategy
import `in`.sanskar.rpsarena.engine.RulesEngine
import `in`.sanskar.rpsarena.logging.SafeLogger
import `in`.sanskar.rpsarena.model.*

enum class ArenaScreen { HOME, PLAY, HISTORY, STATS, ACHIEVEMENTS, SETTINGS, ABOUT }

class ArenaState(
    private val repository: ArenaRepository = ArenaRepository(),
    private val logger: SafeLogger = SafeLogger(),
) {
    var screen by mutableStateOf(ArenaScreen.HOME)
        private set
    var settings by mutableStateOf(repository.loadSettings())
        private set
    var stats by mutableStateOf(repository.loadStats())
        private set
    var config by mutableStateOf(repository.loadConfig())
        private set
    var profilesState by mutableStateOf(repository.loadProfilesState())
        private set
    var match by mutableStateOf(MatchSnapshot(config))
        private set
    var history by mutableStateOf(repository.loadHistory())
        private set
    var pendingPlayerOne by mutableStateOf<Gesture?>(null)
        private set
    var localTurnPhase by mutableStateOf(LocalTurnPhase.PLAYER_ONE_CHOOSE)
        private set
    var canUndoHistoryClear by mutableStateOf(false)
        private set

    private var clearedHistorySnapshot: List<String>? = null
    private var cpu = CpuStrategy(config.seed)

    val activeProfile: LocalProfile get() = profilesState.activeProfile

    val achievements: List<Achievement> get() = listOf(
        Achievement("first_win", stats.wins >= 1),
        Achievement("ten_rounds", stats.roundsPlayed >= 10),
        Achievement("streak_3", stats.bestStreak >= 3),
        Achievement("streak_7", stats.bestStreak >= 7),
        Achievement("century", stats.roundsPlayed >= 100),
    )

    fun navigate(to: ArenaScreen) {
        screen = to
    }

    fun completeOnboarding() {
        updateSettings(settings.copy(onboardingComplete = true))
        logger.info("onboarding_completed")
    }

    fun updateSettings(value: ArenaSettings) {
        settings = value
        repository.saveSettings(value)
        logger.debug(
            "settings_updated",
            mapOf(
                "follow_system_theme" to value.followSystemTheme,
                "dark_theme" to value.darkTheme,
                "reduced_motion" to value.reducedMotion,
            ),
        )
    }

    fun createProfile(displayName: String): Boolean {
        val updated = repository.createProfile(displayName) ?: return false
        profilesState = updated
        resetMatch()
        logger.info("local_profile_created", mapOf("profile_count" to updated.profiles.size))
        return true
    }

    fun renameActiveProfile(displayName: String): Boolean {
        val updated = repository.renameProfile(activeProfile.id, displayName) ?: return false
        profilesState = updated
        logger.info("local_profile_renamed")
        return true
    }

    fun activateProfile(profileId: String): Boolean {
        val updated = repository.activateProfile(profileId) ?: return false
        profilesState = updated
        resetMatch()
        logger.info("local_profile_activated")
        return true
    }

    fun deleteProfile(profileId: String): Boolean {
        val updated = repository.deleteProfile(profileId) ?: return false
        profilesState = updated
        resetMatch()
        logger.info("local_profile_deleted", mapOf("profile_count" to updated.profiles.size))
        return true
    }

    fun updateConfig(value: MatchConfig) {
        val sanitized = value.copy(roundTimerSeconds = value.roundTimerSeconds.coerceIn(0, 60))
        config = sanitized
        repository.saveConfig(sanitized)
        logger.info(
            "match_config_updated",
            mapOf(
                "variant" to sanitized.variant.name,
                "opponent" to sanitized.opponentMode.name,
                "difficulty" to sanitized.difficulty.name,
                "mode" to sanitized.matchMode.name,
                "round_timer_seconds" to sanitized.roundTimerSeconds,
            ),
        )
        resetMatch()
    }

    fun resetMatch() {
        cpu = CpuStrategy(config.seed)
        match = MatchSnapshot(config)
        pendingPlayerOne = null
        localTurnPhase = LocalTurnPhase.PLAYER_ONE_CHOOSE
        logger.debug("match_reset")
    }

    fun clearHistory() {
        if (history.isEmpty()) return
        clearedHistorySnapshot = history
        repository.clearHistory()
        history = emptyList()
        canUndoHistoryClear = true
        logger.info("history_cleared")
    }

    fun undoHistoryClear(): Boolean {
        val snapshot = clearedHistorySnapshot ?: return false
        if (!repository.replaceHistory(snapshot)) return false
        history = repository.loadHistory()
        clearedHistorySnapshot = null
        canUndoHistoryClear = false
        logger.info("history_clear_undone", mapOf("history_entries" to history.size))
        return true
    }

    fun previewBackup(raw: String): BackupPreview? = repository.previewBackup(raw)

    fun exportBackup(): String {
        logger.info("backup_exported", mapOf("history_entries" to history.size))
        return repository.exportBackup()
    }

    fun importBackup(raw: String): Boolean {
        if (!repository.importBackup(raw)) {
            logger.warn("backup_import_rejected")
            return false
        }
        settings = repository.loadSettings()
        stats = repository.loadStats()
        config = repository.loadConfig()
        profilesState = repository.loadProfilesState()
        history = repository.loadHistory()
        invalidateHistoryUndo()
        resetMatch()
        screen = ArenaScreen.HOME
        logger.info(
            "backup_import_accepted",
            mapOf(
                "history_entries" to history.size,
                "profile_count" to profilesState.profiles.size,
            ),
        )
        return true
    }

    fun resetAllData() {
        repository.resetAll()
        settings = repository.loadSettings()
        stats = repository.loadStats()
        config = repository.loadConfig()
        profilesState = repository.loadProfilesState()
        history = emptyList()
        invalidateHistoryUndo()
        resetMatch()
        screen = ArenaScreen.HOME
        logger.info("local_data_reset")
    }

    fun playTimeoutMove() {
        if (config.roundTimerSeconds <= 0) return
        if (match.finished && config.matchMode !in setOf(MatchMode.ENDLESS, MatchMode.STREAK)) return
        val gestures = Gesture.availableFor(config.variant)
        val turnOffset = if (pendingPlayerOne == null) 0 else 1
        val deterministicIndex = ((config.seed.toLong() + match.rounds.size + turnOffset) and 0x7fff_ffffL) % gestures.size
        logger.debug("round_timeout_move", mapOf("round" to match.rounds.size + 1))
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
            localTurnPhase = LocalTurnPhase.PLAYER_TWO_CHOOSE
            return
        }
        pendingPlayerOne = null
        localTurnPhase = LocalTurnPhase.PLAYER_ONE_CHOOSE
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
        invalidateHistoryUndo()
        logger.debug(
            "round_completed",
            mapOf(
                "round" to match.rounds.size,
                "outcome" to outcome.name,
                "match_finished" to finished,
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

    private fun invalidateHistoryUndo() {
        clearedHistorySnapshot = null
        canUndoHistoryClear = false
    }

    private fun historyLine(round: RoundRecord): String =
        "${round.playerOne.label} vs ${round.playerTwo.label} — ${when (round.outcome) {
            RoundOutcome.PLAYER_ONE_WIN -> "${activeProfile.displayName} won"
            RoundOutcome.PLAYER_TWO_WIN -> if (config.opponentMode == OpponentMode.CPU) "CPU won" else "Player 2 won"
            RoundOutcome.DRAW -> "Draw"
        }}"
}
