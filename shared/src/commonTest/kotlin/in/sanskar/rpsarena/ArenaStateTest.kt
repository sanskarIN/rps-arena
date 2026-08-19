package in.sanskar.rpsarena

import in.sanskar.rpsarena.data.ArenaRepository
import in.sanskar.rpsarena.data.KeyValueStore
import in.sanskar.rpsarena.model.ArenaSettings
import in.sanskar.rpsarena.model.ArenaStats
import in.sanskar.rpsarena.model.GameVariant
import in.sanskar.rpsarena.model.MatchConfig
import in.sanskar.rpsarena.model.MatchMode
import in.sanskar.rpsarena.model.OpponentMode
import in.sanskar.rpsarena.state.ArenaScreen
import in.sanskar.rpsarena.state.ArenaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

        state.playTimeoutMove()
        assertEquals(null, state.pendingPlayerOne)
        assertEquals(1, state.match.rounds.size)
    }

    @Test
    fun disabledTimerDoesNotCreateRound() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.playTimeoutMove()
        assertTrue(state.match.rounds.isEmpty())
    }

    @Test
    fun resetAllDataReturnsToSafeDefaults() {
        val state = ArenaState(ArenaRepository(StateMemoryStore()))
        state.completeOnboarding()
        state.updateConfig(MatchConfig(matchMode = MatchMode.ENDLESS, seed = 123, roundTimerSeconds = 10))
        state.playTimeoutMove()
        state.navigate(ArenaScreen.SETTINGS)

        state.resetAllData()

        assertEquals(ArenaSettings(), state.settings)
        assertEquals(ArenaStats(), state.stats)
        assertEquals(MatchConfig(), state.config)
        assertTrue(state.history.isEmpty())
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
}
