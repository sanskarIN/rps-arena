package dev.sanskar.rpsarena.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.sanskar.rpsarena.domain.OpponentMode
import dev.sanskar.rpsarena.domain.RoundOutcome
import dev.sanskar.rpsarena.domain.RoundRecord
import dev.sanskar.rpsarena.state.AppController
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(controller: AppController) {
    val settings = controller.settings
    val match = controller.match
    val timerSeconds = settings.roundTimerSeconds
    var secondsLeft by remember(match.roundNumber, match.pendingPlayerOne, timerSeconds) {
        mutableIntStateOf(timerSeconds)
    }

    LaunchedEffect(match.roundNumber, match.pendingPlayerOne, timerSeconds, match.finished) {
        if (timerSeconds <= 0 || match.finished) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft -= 1
        }
        controller.onTimerExpired()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(RpsSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(RpsSpacing.md),
    ) {
        Text("Arena", style = MaterialTheme.typography.displaySmall)
        Text(
            "${settings.variant.label} • ${settings.gameMode.label} • ${settings.opponentMode.label}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(RpsSpacing.md),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                ScoreValue("Player 1", match.playerOneScore)
                ScoreValue("Draws", match.draws)
                ScoreValue(if (settings.opponentMode == OpponentMode.CPU) "CPU" else "Player 2", match.playerTwoScore)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(RpsSpacing.xs)) {
            Text(match.message, style = MaterialTheme.typography.titleMedium)
            val turnLabel = when {
                match.finished -> "Match finished"
                settings.opponentMode == OpponentMode.CPU -> "Your move"
                match.pendingPlayerOne == null -> "Player 1 — choose privately"
                else -> "Player 2 — choose privately"
            }
            Text(turnLabel, style = MaterialTheme.typography.bodyLarge)
            if (timerSeconds > 0 && !match.finished) {
                Text("Timer: ${secondsLeft}s • At zero, the first valid gesture is auto-selected.")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm),
        ) {
            settings.variant.gestures.forEach { gesture ->
                Button(
                    onClick = { controller.submitGesture(gesture) },
                    enabled = !match.finished,
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .semantics { contentDescription = "Choose ${gesture.label}" },
                ) {
                    Text("${gesture.symbol} ${gesture.label}")
                }
            }
        }

        val last = match.lastRound
        if (settings.reducedMotion) {
            if (last != null) RoundResultCard(last, settings.opponentMode)
        } else {
            AnimatedContent(targetState = last, label = "round-result") { record ->
                if (record != null) RoundResultCard(record, settings.opponentMode)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
            OutlinedButton(onClick = controller::restartMatch) {
                Text(Strings.restart)
            }
        }

        if (settings.opponentMode == OpponentMode.CPU) {
            Text(
                "CPU: ${settings.difficulty.label} — ${settings.difficulty.description}. Seed: ${settings.seed}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "Pass-and-play keeps Player 1's pending gesture hidden until Player 2 chooses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScoreValue(label: String, value: Int) {
    Column {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RoundResultCard(record: RoundRecord, opponentMode: OpponentMode) {
    val outcomeText = when (record.outcome) {
        RoundOutcome.PLAYER_ONE_WIN -> "Player 1 won"
        RoundOutcome.PLAYER_TWO_WIN -> if (opponentMode == OpponentMode.CPU) "CPU won" else "Player 2 won"
        RoundOutcome.DRAW -> "Draw"
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
            Text("Round ${record.roundNumber}: $outcomeText", style = MaterialTheme.typography.titleLarge)
            Text("${record.playerOne.symbol} ${record.playerOne.label}  vs  ${record.playerTwo.symbol} ${record.playerTwo.label}")
        }
    }
}
