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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.model.HistoryTrendParser

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
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            tonalElevation = 2.dp,
                            modifier = Modifier.semantics {
                                contentDescription = Strings.recentResultAccessibility(index + 1, result)
                            },
                        ) {
                            Text(
                                result.shortLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
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
