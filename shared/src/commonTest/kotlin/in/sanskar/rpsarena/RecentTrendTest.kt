package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.HistoryTrendParser
import `in`.sanskar.rpsarena.model.TrendResult
import kotlin.test.Test
import kotlin.test.assertEquals

class RecentTrendTest {
    @Test
    fun parsesLegacyAndNamedPlayerHistory() {
        val trend = HistoryTrendParser.parse(
            listOf(
                "Rock vs Scissors — Arena Ace won",
                "Paper vs Rock — CPU won",
                "Scissors vs Scissors — Draw",
                "Rock vs Scissors — Player 1 won",
                "Paper vs Scissors — Player 2 won",
            ),
        )

        assertEquals(
            listOf(
                TrendResult.WIN,
                TrendResult.LOSS,
                TrendResult.DRAW,
                TrendResult.WIN,
                TrendResult.LOSS,
            ),
            trend.results,
        )
        assertEquals(2, trend.wins)
        assertEquals(2, trend.losses)
        assertEquals(1, trend.draws)
        assertEquals(50, trend.recentWinRate)
    }

    @Test
    fun ignoresUnknownHistoryAndHonorsLimit() {
        val trend = HistoryTrendParser.parse(
            listOf(
                "unrecognized",
                "Rock vs Paper — CPU won",
                "Paper vs Rock — Player One won",
                "Rock vs Rock — Draw",
            ),
            limit = 2,
        )
        assertEquals(listOf(TrendResult.LOSS, TrendResult.WIN), trend.results)
    }

    @Test
    fun drawOnlyTrendHasZeroDecisiveWinRate() {
        val trend = HistoryTrendParser.parse(listOf("Rock vs Rock — Draw"))
        assertEquals(0, trend.recentWinRate)
        assertEquals(0, trend.decisiveGames)
    }
}
