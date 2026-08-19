package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.state.ArenaState

@Composable
internal fun LocalProfilesSettings(state: ArenaState) {
    var nameInput by remember(state.activeProfile.id) { mutableStateOf(state.activeProfile.displayName) }
    var message by remember { mutableStateOf<String?>(null) }

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(Strings.localProfiles, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(Strings.localProfilesSummary, style = MaterialTheme.typography.bodySmall)
            Text("${Strings.activeProfile}: ${state.activeProfile.displayName}", fontWeight = FontWeight.SemiBold)

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.profilesState.profiles.forEach { profile ->
                    val active = profile.id == state.activeProfile.id
                    FilterChip(
                        selected = active,
                        onClick = {
                            if (state.activateProfile(profile.id)) {
                                nameInput = state.activeProfile.displayName
                                message = null
                            }
                        },
                        label = { Text(Strings.profileChip(profile.displayName, active)) },
                    )
                }
            }

            OutlinedTextField(
                value = nameInput,
                onValueChange = { input ->
                    if (input.length <= ArenaRepository.MAX_PROFILE_NAME_LENGTH) nameInput = input
                },
                label = { Text(Strings.profileName) },
                supportingText = { Text(Strings.profileNameHelp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        message = if (state.renameActiveProfile(nameInput)) {
                            nameInput = state.activeProfile.displayName
                            Strings.profileRenamed
                        } else {
                            Strings.profileRejected
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(Strings.renameProfile) }
                OutlinedButton(
                    onClick = {
                        message = if (state.createProfile(nameInput)) {
                            nameInput = state.activeProfile.displayName
                            Strings.profileCreated
                        } else {
                            Strings.profileRejected
                        }
                    },
                    enabled = state.profilesState.profiles.size < ArenaRepository.MAX_PROFILES,
                    modifier = Modifier.weight(1f),
                ) { Text(Strings.createProfile) }
            }

            OutlinedButton(
                onClick = {
                    val deleted = state.deleteProfile(state.activeProfile.id)
                    if (deleted) nameInput = state.activeProfile.displayName
                    message = if (deleted) Strings.profileDeleted else Strings.profileDeleteRejected
                },
                enabled = state.profilesState.profiles.size > 1,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(Strings.deleteProfile) }

            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
