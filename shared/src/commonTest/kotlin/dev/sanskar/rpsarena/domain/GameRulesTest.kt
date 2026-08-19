package dev.sanskar.rpsarena.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameRulesTest {
    @Test
    fun classicRulesResolveAllCoreOutcomes() {
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, GameRules.outcome(Gesture.ROCK, Gesture.SCISSORS, GameVariant.CLASSIC))
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, GameRules.outcome(Gesture.PAPER, Gesture.ROCK, GameVariant.CLASSIC))
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, GameRules.outcome(Gesture.SCISSORS, Gesture.PAPER, GameVariant.CLASSIC))
        assertEquals(RoundOutcome.PLAYER_TWO_WIN, GameRules.outcome(Gesture.ROCK, Gesture.PAPER, GameVariant.CLASSIC))
        assertEquals(RoundOutcome.DRAW, GameRules.outcome(Gesture.ROCK, Gesture.ROCK, GameVariant.CLASSIC))
    }

    @Test
    fun lizardSpockAddsExpectedRelationships() {
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, GameRules.outcome(Gesture.LIZARD, Gesture.SPOCK, GameVariant.LIZARD_SPOCK))
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, GameRules.outcome(Gesture.SPOCK, Gesture.SCISSORS, GameVariant.LIZARD_SPOCK))
        assertEquals(RoundOutcome.PLAYER_TWO_WIN, GameRules.outcome(Gesture.SPOCK, Gesture.LIZARD, GameVariant.LIZARD_SPOCK))
    }

    @Test
    fun matchFinishThresholdsAreCorrect() {
        assertFalse(GameRules.isMatchFinished(GameMode.BEST_OF_3, 1, 1))
        assertTrue(GameRules.isMatchFinished(GameMode.BEST_OF_3, 2, 0))
        assertTrue(GameRules.isMatchFinished(GameMode.BEST_OF_5, 1, 3))
        assertFalse(GameRules.isMatchFinished(GameMode.ENDLESS, 100, 0))
    }
}
