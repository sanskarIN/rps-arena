package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.model.HistoryTrendParser
import `in`.sanskar.rpsarena.model.TrendResult

@Composable
internal fun RecentTrendCard(history: List<String>) {
    val trend = HistoryTrendParser.parse(history)
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(Strings.recentTrend, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (trend.results.isEmpty()) {
                Text(Strings.recentTrendEmpty)
            } else {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    trend.results.forEachIndexed { index, result ->
                        val fullLabel = when (result) {
                            TrendResult.WIN -> "Win"
                            TrendResult.LOSS -> "Loss"
                            TrendResult.DRAW -> "Draw"
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(result.shortLabel) },
                            modifier = Modifier.semantics {
                                contentDescription = "Recent result ${index + 1}: $fullLabel"
                            },
                        )
                    }
                }
                Text(Strings.recentTrendLegend, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${Strings.recentDecisiveWinRate}: ${trend.recentWinRate}%",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
