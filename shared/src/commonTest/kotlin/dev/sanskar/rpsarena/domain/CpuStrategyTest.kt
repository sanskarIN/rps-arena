package dev.sanskar.rpsarena.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CpuStrategyTest {
    @Test
    fun sameSeedProducesSameSequence() {
        val first = CpuStrategy(42)
        val second = CpuStrategy(42)
        val history = listOf(Gesture.ROCK, Gesture.ROCK, Gesture.PAPER, Gesture.ROCK)

        val firstSequence = List(20) { first.choose(Difficulty.EXPERT, GameVariant.CLASSIC, history) }
        val secondSequence = List(20) { second.choose(Difficulty.EXPERT, GameVariant.CLASSIC, history) }

        assertEquals(firstSequence, secondSequence)
    }

    @Test
    fun cpuNeverChoosesGestureOutsideVariant() {
        val cpu = CpuStrategy(99)
        repeat(200) {
            assertTrue(cpu.choose(Difficulty.EASY, GameVariant.CLASSIC, emptyList()) in GameVariant.CLASSIC.gestures)
        }
    }

    @Test
    fun counterAlwaysBeatsTarget() {
        GameVariant.LIZARD_SPOCK.gestures.forEach { target ->
            val counter = GameRules.counterFor(target, GameVariant.LIZARD_SPOCK)
            assertEquals(
                RoundOutcome.PLAYER_ONE_WIN,
                GameRules.outcome(counter, target, GameVariant.LIZARD_SPOCK),
            )
        }
    }
}
