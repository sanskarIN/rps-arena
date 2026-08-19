package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.engine.CpuStrategy
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.Gesture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CpuStrategyTest {
    @Test fun sameSeedProducesSameSequence() {
        val a = CpuStrategy(42)
        val b = CpuStrategy(42)
        val seqA = List(20) { a.choose(Difficulty.EASY, GameVariant.CLASSIC, emptyList()) }
        val seqB = List(20) { b.choose(Difficulty.EASY, GameVariant.CLASSIC, emptyList()) }
        assertEquals(seqA, seqB)
    }

    @Test fun classicNeverReturnsExtendedGestures() {
        val cpu = CpuStrategy(9)
        repeat(100) {
            assertTrue(cpu.choose(Difficulty.EXPERT, GameVariant.CLASSIC, List(8) { Gesture.ROCK }) in Gesture.availableFor(GameVariant.CLASSIC))
        }
    }
}
