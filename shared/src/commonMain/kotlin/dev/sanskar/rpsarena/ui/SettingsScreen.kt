package dev.sanskar.rpsarena.ui

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.sanskar.rpsarena.domain.Difficulty
import dev.sanskar.rpsarena.domain.GameMode
import dev.sanskar.rpsarena.domain.GameVariant
import dev.sanskar.rpsarena.domain.OpponentMode
import dev.sanskar.rpsarena.domain.ThemePreference
import dev.sanskar.rpsarena.state.AppController

@Composable
fun SettingsScreen(controller: AppController) {
    val settings = controller.settings
    var seedText by remember(settings.seed) { mutableStateOf(settings.seed.toString()) }
    var seedMessage by remember { mutableStateOf<String?>(null) }
    var backupText by remember { mutableStateOf("") }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(RpsSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(RpsSpacing.md),
    ) {
        Text(Strings.settings, style = MaterialTheme.typography.displaySmall)

        SettingCard("Appearance & accessibility") {
            ChoiceRow("Theme", ThemePreference.entries, settings.theme, { it.label }) { choice ->
                controller.updateSettings { it.copy(theme = choice) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Reduced motion", style = MaterialTheme.typography.titleMedium)
                    Text("Disables animated result transitions.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = settings.reducedMotion,
                    onCheckedChange = { enabled -> controller.updateSettings { it.copy(reducedMotion = enabled) } },
                )
            }
        }

        SettingCard("Game rules") {
            ChoiceRow("Variant", GameVariant.entries, settings.variant, { it.label }) { choice ->
                controller.updateSettings { it.copy(variant = choice) }
            }
            ChoiceRow("Opponent", OpponentMode.entries, settings.opponentMode, { it.label }) { choice ->
                controller.updateSettings { it.copy(opponentMode = choice) }
            }
            ChoiceRow("Mode", GameMode.entries, settings.gameMode, { it.label }) { choice ->
                controller.updateSettings { it.copy(gameMode = choice) }
            }
            if (settings.opponentMode == OpponentMode.CPU) {
                ChoiceRow("CPU difficulty", Difficulty.entries, settings.difficulty, { it.label }) { choice ->
                    controller.updateSettings { it.copy(difficulty = choice) }
                }
                Text(settings.difficulty.description, style = MaterialTheme.typography.bodySmall)
            }

            Text("Round timer", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                OutlinedButton(
                    onClick = { controller.updateSettings { it.copy(roundTimerSeconds = (it.roundTimerSeconds - 5).coerceAtLeast(0)) } },
                    enabled = settings.roundTimerSeconds > 0,
                ) { Text("−5s") }
                Text(if (settings.roundTimerSeconds == 0) "Off" else "${settings.roundTimerSeconds}s", modifier = Modifier.padding(RpsSpacing.sm))
                OutlinedButton(
                    onClick = { controller.updateSettings { it.copy(roundTimerSeconds = (it.roundTimerSeconds + 5).coerceAtMost(60)) } },
                    enabled = settings.roundTimerSeconds < 60,
                ) { Text("+5s") }
            }
            Text("At zero, RPS Arena auto-selects the first valid gesture so a timed round always completes.", style = MaterialTheme.typography.bodySmall)
        }

        SettingCard("Replayable CPU seed") {
            OutlinedTextField(
                value = seedText,
                onValueChange = { seedText = it.take(20) },
                label = { Text("Seed") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                val seed = seedText.toLongOrNull()
                if (seed == null) {
                    seedMessage = "Enter a whole number between ${Long.MIN_VALUE} and ${Long.MAX_VALUE}."
                } else {
                    controller.updateSettings { it.copy(seed = seed) }
                    controller.restartMatch()
                    seedMessage = "Seed applied. Restarted from the deterministic sequence."
                }
            }) { Text("Apply seed") }
            seedMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        SettingCard("Backup & restore") {
            Text("Backups are plain text and contain only local settings, aggregate stats, and the latest round history.")
            Row(horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                OutlinedButton(onClick = {
                    backupText = controller.exportBackup()
                    backupMessage = "Backup generated below."
                }) { Text("Generate backup") }
                Button(onClick = {
                    backupMessage = if (controller.importBackup(backupText.trim())) "Backup imported." else "Backup is invalid or unsupported."
                }) { Text("Import backup") }
            }
            OutlinedTextField(
                value = backupText,
                onValueChange = { backupText = it },
                label = { Text("Backup text") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
            backupMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        SettingCard("Privacy & local data") {
            Text("RPS Arena does not require an account and does not send game data to a server.")
            if (!confirmReset) {
                OutlinedButton(onClick = { confirmReset = true }) { Text(Strings.resetData) }
            } else {
                Text("This removes settings, stats, and saved history from this device.")
                Row(horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                    Button(onClick = {
                        controller.resetAllData()
                        backupText = ""
                        backupMessage = "Local data reset."
                        confirmReset = false
                    }) { Text("Confirm reset") }
                    TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.md)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            content()
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    title: String,
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(RpsSpacing.xs)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(RpsSpacing.sm),
        ) {
            values.forEach { value ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelected(value) },
                    label = { Text(label(value)) },
                )
            }
        }
    }
}
