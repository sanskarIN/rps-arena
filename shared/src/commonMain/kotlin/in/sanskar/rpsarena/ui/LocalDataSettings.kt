package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.state.ArenaState

@Composable
internal fun LocalDataSettings(state: ArenaState) {
    var backupText by remember { mutableStateOf("") }
    var dataMessage by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    val preview = remember(backupText) {
        backupText.takeIf { it.isNotBlank() }?.let(state::previewBackup)
    }

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                Strings.privacyLocalData,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(Strings.privacySummary)

            OutlinedButton(
                onClick = {
                    backupText = state.exportBackup()
                    dataMessage = Strings.backupGenerated
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(Strings.generateBackup) }

            OutlinedTextField(
                value = backupText,
                onValueChange = {
                    backupText = it.take(MAX_BACKUP_INPUT_CHARS)
                    dataMessage = null
                },
                label = { Text(Strings.backupField) },
                supportingText = { Text(Strings.backupHelp) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )

            if (backupText.isNotBlank()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(Strings.backupPreview, fontWeight = FontWeight.SemiBold)
                        if (preview == null) {
                            Text(Strings.backupNoPreview, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text(
                                Strings.backupPreviewSummary(
                                    version = preview.formatVersion,
                                    activeProfile = preview.activeProfileName,
                                    profileCount = preview.profileNames.size,
                                    rounds = preview.stats.roundsPlayed,
                                    historyEntries = preview.historyEntries,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(Strings.backupPreviewHelp, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    dataMessage = if (state.importBackup(backupText)) {
                        backupText = ""
                        Strings.backupImported
                    } else {
                        Strings.backupRejected
                    }
                },
                enabled = preview != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(Strings.importBackup) }

            OutlinedButton(
                onClick = {
                    state.clearHistory()
                    dataMessage = Strings.historyCleared
                },
                enabled = state.history.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(Strings.clearRecentHistory) }

            if (state.canUndoHistoryClear) {
                Button(
                    onClick = {
                        dataMessage = if (state.undoHistoryClear()) Strings.historyRestored else Strings.backupRejected
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(Strings.undoHistoryClear) }
            }

            if (!confirmReset) {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(Strings.resetAllData) }
            } else {
                Text(Strings.resetConfirmation)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            state.resetAllData()
                            backupText = ""
                            dataMessage = Strings.resetComplete
                            confirmReset = false
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(Strings.confirmReset) }
                    OutlinedButton(
                        onClick = { confirmReset = false },
                        modifier = Modifier.weight(1f),
                    ) { Text(Strings.cancel) }
                }
            }
            dataMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

private const val MAX_BACKUP_INPUT_CHARS = 32_768
