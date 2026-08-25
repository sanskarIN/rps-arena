package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.ArenaStore
import `in`.sanskar.rpsarena.logging.LogEvent
import `in`.sanskar.rpsarena.logging.SafeLogger
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode
import `in`.sanskar.rpsarena.state.ArenaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArenaStateTest {
    @Test
    fun sameSeedAndMovesReplaySameCpuSequence() {
        val first = state()
        val second = state()
        val config = MatchConfig(seed = 424242)
        first.updateConfig(config)
        second.updateConfig(config)
        val moves = listOf(
            Gesture.ROCK,
            Gesture.PAPER,
            Gesture.SCISSORS,
            Gesture.ROCK,
            Gesture.ROCK,
        )

        moves.forEach { move ->
            first.play(move)
            second.play(move)
        }

        assertEquals(
            first.match.rounds.map { it.playerTwo },
            second.match.rounds.map { it.playerTwo },
        )
        assertEquals(first.match.rounds.map { it.outcome }, second.match.rounds.map { it.outcome })
    }

    @Test
    fun matchConfigurationPersistsAcrossStateInstances() {
        val store = MemoryStore()
        val repository = ArenaRepository(store)
        val expected = MatchConfig(
            variant = GameVariant.LIZARD_SPOCK,
            opponentMode = OpponentMode.LOCAL_TWO_PLAYER,
            difficulty = Difficulty.EXPERT,
            matchMode = MatchMode.TOURNAMENT,
            seed = -424242,
        )

        ArenaState(repository).updateConfig(expected)
        val restored = ArenaState(repository)

        assertEquals(expected, restored.config)
        assertEquals(expected, restored.match.config)
    }

    @Test
    fun gesturesOutsideCurrentRulesetAreRejectedAndLogged() {
        val events = mutableListOf<LogEvent>()
        val state = ArenaState(
            repository = ArenaRepository(MemoryStore()),
            logger = SafeLogger(events::add),
        )

        state.play(Gesture.LIZARD)

        assertTrue(state.match.rounds.isEmpty())
        assertEquals("invalid_gesture_rejected", events.single().event)
    }

    private fun state(): ArenaState = ArenaState(ArenaRepository(MemoryStore()))

    private class MemoryStore(
        private val values: MutableMap<String, String> = mutableMapOf(),
    ) : ArenaStore {
        override fun getString(key: String, defaultValue: String): String =
            values[key] ?: defaultValue

        override fun putString(key: String, value: String) {
            values[key] = value
        }
    }
}
