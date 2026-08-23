package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaHistoryCodec
import `in`.sanskar.rpsarena.model.ArenaHistoryEntry
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.RoundOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArenaHistoryCodecTest {
    @Test
    fun roundTripPreservesStructuredAndLegacyEntries() {
        val history = listOf(
            ArenaHistoryEntry.Round(Gesture.ROCK, Gesture.SCISSORS, RoundOutcome.PLAYER_ONE_WIN),
            ArenaHistoryEntry.Legacy("Paper | Spock — 50% draw"),
        )

        assertEquals(history, ArenaHistoryCodec.decode(ArenaHistoryCodec.encode(history)))
    }

    @Test
    fun encoderSanitizesAndLimitsLegacyEntries() {
        val input = (1..35).map { ArenaHistoryEntry.Legacy(" Round $it\nresult ") }
        val decoded = ArenaHistoryCodec.decode(ArenaHistoryCodec.encode(input))

        assertEquals(
            (1..ArenaHistoryCodec.MAX_ITEMS).map { ArenaHistoryEntry.Legacy("Round $it result") },
            decoded,
        )
    }

    @Test
    fun rejectsUnknownGesture() {
        val raw = """
            RPSARENA_HISTORY|2
            count|1
            round|WATER|ROCK|PLAYER_ONE_WIN
        """.trimIndent()

        assertNull(ArenaHistoryCodec.decode(raw))
    }

    @Test
    fun rejectsCountMismatch() {
        val raw = """
            RPSARENA_HISTORY|2
            count|2
            round|ROCK|SCISSORS|PLAYER_ONE_WIN
        """.trimIndent()

        assertNull(ArenaHistoryCodec.decode(raw))
    }

    @Test
    fun rejectsUnsupportedHistorySchema() {
        val raw = """
            RPSARENA_HISTORY|3
            count|0
        """.trimIndent()

        assertNull(ArenaHistoryCodec.decode(raw))
    }
}
