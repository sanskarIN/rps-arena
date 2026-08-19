package dev.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val REPOSITORY_URL = "https://github.com/sanskarIN/rps-arena"
private const val GITHUB_URL = "https://github.com/sanskarIN"
private const val BMC_URL = "https://buymeacoffee.com/sanskarIN"

@Composable
fun AboutScreen(openUrl: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(RpsSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(RpsSpacing.md),
    ) {
        Text(Strings.about, style = MaterialTheme.typography.displaySmall)
        Text("RPS Arena", style = MaterialTheme.typography.headlineMedium)
        Text("Version 1.0.0 • MIT License")
        Text("A privacy-first, offline Rock–Paper–Scissors arena for Android and desktop.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Open source", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { openUrl(REPOSITORY_URL) }) { Text("Open repository") }
                OutlinedButton(onClick = { openUrl(GITHUB_URL) }) { Text("Open GitHub profile") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Support", style = MaterialTheme.typography.titleLarge)
                Text("Business: sanskarin@outlook.in")
                Text("Business: sanskarin.business@gmail.com")
                Text("Support: supportramsandesh@gmail.com")
                OutlinedButton(onClick = { openUrl("mailto:supportramsandesh@gmail.com") }) { Text("Email support") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(RpsSpacing.md), verticalArrangement = Arrangement.spacedBy(RpsSpacing.sm)) {
                Text("Buy Me a Coffee", style = MaterialTheme.typography.titleLarge)
                Text("RPS Arena remains fully usable without donating.")
                OutlinedButton(onClick = { openUrl(BMC_URL) }) { Text("Support the project") }
            }
        }

        Text(Strings.madeBy, style = MaterialTheme.typography.titleMedium)
    }
}
