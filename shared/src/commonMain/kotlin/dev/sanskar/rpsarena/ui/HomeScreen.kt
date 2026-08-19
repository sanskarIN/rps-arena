package dev.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sanskar.rpsarena.state.AppController

@Composable
fun HomeScreen(
    controller: AppController,
    onPlay: () -> Unit,
    onAbout: () -> Unit,
) {
    val stats = controller.stats
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(RpsSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(RpsSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
            Text(Strings.appName, style = MaterialTheme.typography.displaySmall)
            Text(Strings.tagline, style = MaterialTheme.typography.titleMedium)
            Text(
                "Offline-first • ${controller.settings.variant.label} • ${controller.settings.gameMode.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm),
        ) {
            Button(onClick = onPlay, modifier = Modifier.weight(1f)) { Text("Enter arena") }
            OutlinedButton(onClick = onAbout, modifier = Modifier.weight(1f)) { Text("Project info") }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Your local record", style = MaterialTheme.typography.titleLarge)
                Text("${stats.wins} wins • ${stats.losses} losses • ${stats.draws} draws")
                Text("Best streak: ${stats.bestStreak} • Win rate: ${stats.winRate}%")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Fair CPU", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Easy is random. Normal adapts occasionally. Expert uses recent local history while keeping randomness. " +
                        "Seed ${controller.settings.seed} makes CPU challenges replayable.",
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Privacy by default", style = MaterialTheme.typography.titleLarge)
                Text("No account, ads, analytics, or mandatory network connection. Settings and game history stay on this device.")
            }
        }

        Text(Strings.madeBy, style = MaterialTheme.typography.labelLarge)
    }
}
