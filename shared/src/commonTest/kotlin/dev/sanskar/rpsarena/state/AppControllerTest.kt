package dev.sanskar.rpsarena.state

import dev.sanskar.rpsarena.data.AppRepository
import dev.sanskar.rpsarena.data.KeyValueStore
import dev.sanskar.rpsarena.domain.GameMode
import dev.sanskar.rpsarena.domain.Gesture
import dev.sanskar.rpsarena.domain.OpponentMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppControllerTest {
    @Test
    fun localTwoPlayerHidesFirstChoiceUntilSecondChoice() {
        val controller = AppController(AppRepository(TestStore()))
        controller.updateSettings { it.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER) }

        controller.submitGesture(Gesture.ROCK)
        assertEquals(Gesture.ROCK, controller.match.pendingPlayerOne)
        assertNull(controller.match.lastRound)

        controller.submitGesture(Gesture.SCISSORS)
        assertNull(controller.match.pendingPlayerOne)
        assertNotNull(controller.match.lastRound)
        assertEquals(1, controller.match.playerOneScore)
    }

    @Test
    fun bestOfThreeEndsAtTwoWinsAndRestartClearsMatchOnly() {
        val controller = AppController(AppRepository(TestStore()))
        controller.updateSettings {
            it.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER, gameMode = GameMode.BEST_OF_3)
        }
        repeat(2) {
            controller.submitGesture(Gesture.ROCK)
            controller.submitGesture(Gesture.SCISSORS)
        }

        assertTrue(controller.match.finished)
        assertEquals(2, controller.stats.wins)

        controller.restartMatch()
        assertFalse(controller.match.finished)
        assertEquals(0, controller.match.playerOneScore)
        assertEquals(2, controller.stats.wins)
    }
}

private class TestStore : KeyValueStore {
    private val map = mutableMapOf<String, String>()
    override fun getString(key: String): String? = map[key]
    override fun putString(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
    override fun clear() { map.clear() }
}
