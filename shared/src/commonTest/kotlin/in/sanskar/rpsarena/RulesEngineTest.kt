package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.engine.RulesEngine
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.RoundOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

class RulesEngineTest {
    @Test fun classicRulesAreCorrect() {
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, RulesEngine.resolve(Gesture.ROCK, Gesture.SCISSORS))
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, RulesEngine.resolve(Gesture.PAPER, Gesture.ROCK))
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, RulesEngine.resolve(Gesture.SCISSORS, Gesture.PAPER))
    }

    @Test fun extendedRulesAreCorrect() {
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, RulesEngine.resolve(Gesture.LIZARD, Gesture.SPOCK))
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, RulesEngine.resolve(Gesture.SPOCK, Gesture.ROCK))
        assertEquals(RoundOutcome.PLAYER_TWO_WIN, RulesEngine.resolve(Gesture.ROCK, Gesture.SPOCK))
    }

    @Test fun sameGestureDraws() {
        Gesture.entries.forEach { assertEquals(RoundOutcome.DRAW, RulesEngine.resolve(it, it)) }
    }
}
