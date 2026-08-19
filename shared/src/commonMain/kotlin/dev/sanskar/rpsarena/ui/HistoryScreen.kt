package dev.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sanskar.rpsarena.domain.RoundOutcome
import dev.sanskar.rpsarena.state.AppController

@Composable
fun HistoryScreen(controller: AppController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(RpsSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(RpsSpacing.md),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(Strings.history, style = MaterialTheme.typography.displaySmall)
                Text("Latest 30 rounds, stored only on this device.")
            }
            OutlinedButton(onClick = controller::clearHistory, enabled = controller.history.isNotEmpty()) {
                Text(Strings.clearHistory)
            }
        }

        if (controller.history.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(RpsSpacing.lg)) {
                    Text("No rounds yet", style = MaterialTheme.typography.titleLarge)
                    Text("Play a round and it will appear here.")
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                items(controller.history.asReversed(), key = { it.roundNumber to it.hashCode() }) { round ->
                    val result = when (round.outcome) {
                        RoundOutcome.PLAYER_ONE_WIN -> "Player 1 win"
                        RoundOutcome.PLAYER_TWO_WIN -> "Player 2 / CPU win"
                        RoundOutcome.DRAW -> "Draw"
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(RpsSpacing.md)) {
                            Text("Round ${round.roundNumber} • $result", style = MaterialTheme.typography.titleMedium)
                            Text("${round.playerOne.symbol} ${round.playerOne.label} vs ${round.playerTwo.symbol} ${round.playerTwo.label}")
                        }
                    }
                }
            }
        }
    }
}
