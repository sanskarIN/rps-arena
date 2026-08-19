package dev.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sanskar.rpsarena.state.AppController

@Composable
fun StatsScreen(controller: AppController) {
    val stats = controller.stats
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(RpsSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(RpsSpacing.md),
    ) {
        Text(Strings.stats, style = MaterialTheme.typography.displaySmall)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Lifetime local stats", style = MaterialTheme.typography.titleLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatValue("Wins", stats.wins)
                    StatValue("Losses", stats.losses)
                    StatValue("Draws", stats.draws)
                    StatValue("Rounds", stats.totalRounds)
                }
                Text("Win rate: ${stats.winRate}%")
                Text("Current streak: ${stats.currentStreak} • Best streak: ${stats.bestStreak}")
            }
        }

        Text(Strings.achievements, style = MaterialTheme.typography.headlineSmall)
        controller.achievements.forEach { achievement ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(RpsSpacing.md)) {
                    Text(
                        if (achievement.unlocked) "✓ ${achievement.title}" else "○ ${achievement.title}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(achievement.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatValue(label: String, value: Int) {
    Column {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
