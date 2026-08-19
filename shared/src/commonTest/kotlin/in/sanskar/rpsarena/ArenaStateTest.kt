package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.KeyValueStore
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.LocalTurnPhase
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode
import `in`.sanskar.rpsarena.state.ArenaScreen
import `in`.sanskar.rpsarena.state.ArenaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArenaStateTest {
    @Test
    fun matchConfigurationPersistsAcrossStateInstances() {
        val store = StateMemoryStore()
        val expected = MatchConfig(
            variant = GameVariant.LIZARD_SPOCK,
            opponentMode = OpponentMode.LOCAL_TWO_PLAYER,
            matchMode = MatchMode.ENDLESS,
            seed = 77,
            roundTimerSeconds = 20,
        )

        ArenaState(ArenaRepository(store)).updateConfig(expected)

        assertEquals(expected, ArenaState(ArenaRepository(store)).config)
    }

    @Test
    fun localProfileLifecycleUpdatesActivePlayer() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        assertEquals("Player 1", state.activeProfile.displayName)

        assertTrue(state.renameActiveProfile("Arena Ace"))
        assertEquals("Arena Ace", state.activeProfile.displayName)
        assertTrue(state.createProfile("Challenger"))
        assertEquals("Challenger", state.activeProfile.displayName)
        assertEquals(2, state.profilesState.profiles.size)

        val first = state.profilesState.profiles.first()
        assertTrue(state.activateProfile(first.id))
        assertEquals("Arena Ace", state.activeProfile.displayName)
        assertTrue(state.deleteProfile(first.id))
        assertEquals("Challenger", state.activeProfile.displayName)
        assertEquals(1, state.profilesState.profiles.size)
        assertFalse(state.deleteProfile(state.activeProfile.id))
    }

    @Test
    fun classicRulesRejectExtendedGestureAtStateBoundary() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        assertEquals(GameVariant.CLASSIC, state.config.variant)

        state.play(Gesture.LIZARD)

        assertTrue(state.match.rounds.isEmpty())
        assertEquals(0, state.stats.roundsPlayed)
        assertTrue(state.history.isEmpty())
        assertEquals(null, state.pendingPlayerOne)
    }

    @Test
    fun timeoutMoveCompletesCpuRoundDeterministically() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.updateConfig(
            MatchConfig(
                opponentMode = OpponentMode.CPU,
                seed = 12,
                roundTimerSeconds = 5,
            ),
        )

        state.playTimeoutMove()

        assertEquals(1, state.match.rounds.size)
        assertEquals(1, state.stats.roundsPlayed)
    }

    @Test
    fun localTimeoutAdvancesBothPrivateTurns() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.updateConfig(
            MatchConfig(
                opponentMode = OpponentMode.LOCAL_TWO_PLAYER,
                seed = 42,
                roundTimerSeconds = 5,
            ),
        )

        state.playTimeoutMove()
        assertTrue(state.pendingPlayerOne != null)
        assertEquals(LocalTurnPhase.PLAYER_TWO_CHOOSE, state.localTurnPhase)

        state.playTimeoutMove()
        assertEquals(null, state.pendingPlayerOne)
        assertEquals(LocalTurnPhase.PLAYER_ONE_CHOOSE, state.localTurnPhase)
        assertEquals(1, state.match.rounds.size)
    }

    @Test
    fun disabledTimerDoesNotCreateRound() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.playTimeoutMove()
        assertTrue(state.match.rounds.isEmpty())
    }

    @Test
    fun clearedHistoryCanBeUndoneOnce() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.updateConfig(MatchConfig(matchMode = MatchMode.ENDLESS, roundTimerSeconds = 5))
        state.playTimeoutMove()
        val original = state.history
        assertTrue(original.isNotEmpty())

        state.clearHistory()
        assertTrue(state.history.isEmpty())
        assertTrue(state.canUndoHistoryClear)

        assertTrue(state.undoHistoryClear())
        assertEquals(original, state.history)
        assertFalse(state.canUndoHistoryClear)
        assertFalse(state.undoHistoryClear())
    }

    @Test
    fun newRoundInvalidatesHistoryUndoSnapshot() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.updateConfig(MatchConfig(matchMode = MatchMode.ENDLESS, roundTimerSeconds = 5))
        state.playTimeoutMove()
        state.clearHistory()
        assertTrue(state.canUndoHistoryClear)

        state.playTimeoutMove()

        assertFalse(state.canUndoHistoryClear)
        assertFalse(state.undoHistoryClear())
        assertEquals(1, state.history.size)
    }

    @Test
    fun backupPreviewIsNonMutatingAtStateBoundary() {
        val source = ArenaState(ArenaRepository(StateMemoryStore()))
        source.renameActiveProfile("Backup Player")
        source.updateConfig(MatchConfig(matchMode = MatchMode.ENDLESS, roundTimerSeconds = 5))
        source.playTimeoutMove()
        val backup = source.exportBackup()

        val target = ArenaState(ArenaRepository(StateMemoryStore()))
        val preview = assertNotNull(target.previewBackup(backup))

        assertEquals("Backup Player", preview.activeProfileName)
        assertEquals(1, preview.historyEntries)
        assertEquals("Player 1", target.activeProfile.displayName)
        assertTrue(target.history.isEmpty())
    }

    @Test
    fun resetAllDataReturnsToSafeDefaults() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.completeOnboarding()
        state.renameActiveProfile("Temporary")
        state.updateConfig(MatchConfig(matchMode = MatchMode.ENDLESS, seed = 123, roundTimerSeconds = 10))
        state.playTimeoutMove()
        state.navigate(ArenaScreen.SETTINGS)

        state.resetAllData()

        assertEquals(ArenaSettings(), state.settings)
        assertEquals(ArenaStats(), state.stats)
        assertEquals(MatchConfig(), state.config)
        assertEquals("Player 1", state.activeProfile.displayName)
        assertTrue(state.history.isEmpty())
        assertFalse(state.canUndoHistoryClear)
        assertEquals(ArenaScreen.HOME, state.screen)
        assertFalse(state.settings.onboardingComplete)
    }
}

private class StateMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String, defaultValue: String): String = values[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}