package `in`.sanskar.rpsarena.model

enum class TrendResult(val shortLabel: String) {
    WIN("W"),
    LOSS("L"),
    DRAW("D"),
}

data class RecentTrend(
    val results: List<TrendResult>,
) {
    val wins: Int get() = results.count { it == TrendResult.WIN }
    val losses: Int get() = results.count { it == TrendResult.LOSS }
    val draws: Int get() = results.count { it == TrendResult.DRAW }
    val decisiveGames: Int get() = wins + losses
    val recentWinRate: Int get() = if (decisiveGames == 0) 0 else (wins * 100) / decisiveGames
}

object HistoryTrendParser {
    fun parse(history: List<String>, limit: Int = DEFAULT_LIMIT): RecentTrend {
        val safeLimit = limit.coerceIn(1, MAX_LIMIT)
        val results = history.asSequence()
            .mapNotNull(::parseLine)
            .take(safeLimit)
            .toList()
        return RecentTrend(results)
    }

    private fun parseLine(line: String): TrendResult? {
        val outcome = line.substringAfterLast(" — ", missingDelimiterValue = "").trim()
        if (outcome.isEmpty()) return null
        return when {
            outcome == "Draw" -> TrendResult.DRAW
            outcome == "CPU won" || outcome == "Player 2 won" -> TrendResult.LOSS
            outcome.endsWith(" won") -> TrendResult.WIN
            else -> null
        }
    }

    private const val DEFAULT_LIMIT = 10
    private const val MAX_LIMIT = 30
}
