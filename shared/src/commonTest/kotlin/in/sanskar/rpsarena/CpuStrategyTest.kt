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

    @Test fun sameSeedRemainsDeterministicAcrossDifficultiesAndVariants() {
        Difficulty.entries.forEach { difficulty ->
            GameVariant.entries.forEach { variant ->
                val a = CpuStrategy(2026)
                val b = CpuStrategy(2026)
                val history = List(8) { Gesture.ROCK }
                val seqA = List(40) { a.choose(difficulty, variant, history) }
                val seqB = List(40) { b.choose(difficulty, variant, history) }
                assertEquals(seqA, seqB, "$difficulty/$variant")
            }
        }
    }

    @Test fun classicNeverReturnsExtendedGestures() {
        val cpu = CpuStrategy(9)
        repeat(100) {
            assertTrue(
                cpu.choose(Difficulty.EXPERT, GameVariant.CLASSIC, List(8) { Gesture.ROCK }) in
                    Gesture.availableFor(GameVariant.CLASSIC),
            )
        }
    }

    @Test fun lizardSpockVariantOnlyReturnsSupportedGestures() {
        val cpu = CpuStrategy(17)
        repeat(100) {
            assertTrue(
                cpu.choose(Difficulty.NORMAL, GameVariant.LIZARD_SPOCK, emptyList()) in
                    Gesture.availableFor(GameVariant.LIZARD_SPOCK),
            )
        }
    }
}
