package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.*
import `in`.sanskar.rpsarena.state.ArenaScreen
import `in`.sanskar.rpsarena.state.ArenaState
import kotlinx.coroutines.delay

@Composable
fun RpsArenaApp(repository: ArenaRepository = ArenaRepository()) {
    val state = remember { ArenaState(repository) }
    ArenaTheme(state.settings) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!state.settings.onboardingComplete) {
                OnboardingScreen(onDone = state::completeOnboarding)
            } else {
                ArenaScaffold(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArenaScaffold(state: ArenaState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RPS Arena") },
                actions = {
                    TextButton(onClick = { state.navigate(ArenaScreen.SETTINGS) }) { Text("Settings") }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (state.screen) {
                ArenaScreen.HOME -> HomeScreen(state)
                ArenaScreen.PLAY -> PlayScreen(state)
                ArenaScreen.HISTORY -> HistoryScreen(state)
                ArenaScreen.STATS -> StatsScreen(state)
                ArenaScreen.ACHIEVEMENTS -> AchievementsScreen(state)
                ArenaScreen.SETTINGS -> SettingsScreen(state)
                ArenaScreen.ABOUT -> AboutScreen(state)
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🪨 📄 ✂️", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(20.dp))
        Text("Welcome to RPS Arena", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Fast, offline-first matches with transparent CPU difficulty and an optional Lizard–Spock ruleset.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Enter the Arena") }
    }
}

@Composable
private fun HomeScreen(state: ArenaState) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Choose your arena", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Classic strategy. No account. No network required.")
        Button(onClick = { state.navigate(ArenaScreen.PLAY) }, modifier = Modifier.fillMaxWidth()) { Text("Play") }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.STATS) }, modifier = Modifier.fillMaxWidth()) { Text("Stats") }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.HISTORY) }, modifier = Modifier.fillMaxWidth()) { Text("History") }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.ACHIEVEMENTS) }, modifier = Modifier.fillMaxWidth()) { Text("Achievements") }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.ABOUT) }, modifier = Modifier.fillMaxWidth()) { Text("About") }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Made by the Sanskar", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PlayScreen(state: ArenaState) {
    val config = state.config
    val gestures = Gesture.availableFor(config.variant)
    var seedText by remember(config.seed) { mutableStateOf(config.seed.toString()) }
    var seedMessage by remember { mutableStateOf<String?>(null) }
    var secondsLeft by remember(
        config.roundTimerSeconds,
        state.match.rounds.size,
        state.pendingPlayerOne,
        state.match.finished,
    ) { mutableIntStateOf(config.roundTimerSeconds) }

    LaunchedEffect(
        config.roundTimerSeconds,
        state.match.rounds.size,
        state.pendingPlayerOne,
        state.match.finished,
    ) {
        secondsLeft = config.roundTimerSeconds
        if (config.roundTimerSeconds <= 0 || state.match.finished) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft -= 1
        }
        state.playTimeoutMove()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("Play", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ConfigRow("Opponent") {
            ChoiceChip("CPU", config.opponentMode == OpponentMode.CPU) {
                state.updateConfig(config.copy(opponentMode = OpponentMode.CPU))
            }
            ChoiceChip("2 Player", config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
                state.updateConfig(config.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER))
            }
        }
        ConfigRow("Rules") {
            ChoiceChip("Classic", config.variant == GameVariant.CLASSIC) {
                state.updateConfig(config.copy(variant = GameVariant.CLASSIC))
            }
            ChoiceChip("Lizard–Spock", config.variant == GameVariant.LIZARD_SPOCK) {
                state.updateConfig(config.copy(variant = GameVariant.LIZARD_SPOCK))
            }
        }
        if (config.opponentMode == OpponentMode.CPU) {
            ConfigRow("Difficulty") {
                Difficulty.entries.forEach { difficulty ->
                    ChoiceChip(
                        difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        config.difficulty == difficulty,
                    ) { state.updateConfig(config.copy(difficulty = difficulty)) }
                }
            }
        }
        ConfigRow("Mode") {
            MatchMode.entries.forEach { mode ->
                ChoiceChip(
                    mode.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                    config.matchMode == mode,
                ) { state.updateConfig(config.copy(matchMode = mode)) }
            }
        }
        ConfigRow("Round timer") {
            listOf(0, 5, 10, 15, 30, 60).forEach { seconds ->
                ChoiceChip(
                    if (seconds == 0) "Off" else "${seconds}s",
                    config.roundTimerSeconds == seconds,
                ) { state.updateConfig(config.copy(roundTimerSeconds = seconds)) }
            }
        }

        Text("Replayable CPU seed", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = seedText,
                onValueChange = { seedText = it.take(11) },
                label = { Text("Seed") },
                supportingText = { Text("Whole number; changing it restarts the current match.") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    val seed = seedText.toIntOrNull()
                    if (seed == null) {
                        seedMessage = "Enter a valid whole-number seed."
                    } else {
                        state.updateConfig(config.copy(seed = seed))
                        seedMessage = "Seed applied."
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically),
            ) { Text("Apply") }
        }
        seedMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        ScoreCard(state)
        if (config.roundTimerSeconds > 0 && !state.match.finished) {
            Text(
                "Turn timer: ${secondsLeft}s. At zero, a deterministic valid gesture is selected from the active seed.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
            Text(state.localTurnMessage, fontWeight = FontWeight.SemiBold)
        }
        Text("Choose a gesture", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            gestures.take(3).forEach { gesture ->
                GestureButton(gesture, Modifier.weight(1f)) { state.play(gesture) }
            }
        }
        if (gestures.size > 3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gestures.drop(3).forEach { gesture ->
                    GestureButton(gesture, Modifier.weight(1f)) { state.play(gesture) }
                }
            }
        }
        state.match.rounds.lastOrNull()?.let { round ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Last round", fontWeight = FontWeight.Bold)
                    Text("${round.playerOne.emoji} ${round.playerOne.label} vs ${round.playerTwo.emoji} ${round.playerTwo.label}")
                    Text(
                        when (round.outcome) {
                            RoundOutcome.PLAYER_ONE_WIN -> "Player 1 wins the round"
                            RoundOutcome.PLAYER_TWO_WIN -> if (config.opponentMode == OpponentMode.CPU) "CPU wins the round" else "Player 2 wins the round"
                            RoundOutcome.DRAW -> "Draw"
                        },
                    )
                }
            }
        }
        if (state.match.finished) {
            Button(onClick = state::resetMatch, modifier = Modifier.fillMaxWidth()) { Text("New match") }
        } else if (state.match.rounds.isNotEmpty()) {
            OutlinedButton(onClick = state::resetMatch, modifier = Modifier.fillMaxWidth()) { Text("Restart match") }
        }
    }
}

@Composable
private fun ScoreCard(state: ArenaState) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Score("P1", state.match.playerOneScore)
            Score("Draws", state.match.draws)
            Score(if (state.config.opponentMode == OpponentMode.CPU) "CPU" else "P2", state.match.playerTwoScore)
        }
    }
}

@Composable
private fun Score(label: String, value: Int) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(label)
}

@Composable
private fun GestureButton(gesture: Gesture, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(88.dp)
            .semantics { contentDescription = "Choose ${gesture.label}" },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(gesture.emoji, style = MaterialTheme.typography.headlineMedium)
            Text(gesture.label)
        }
    }
}

@Composable
private fun HistoryScreen(state: ArenaState) {
    Column(Modifier.fillMaxSize()) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = state::clearHistory, enabled = state.history.isNotEmpty()) { Text("Clear") }
        }
        Spacer(Modifier.height(8.dp))
        if (state.history.isEmpty()) {
            Text("No rounds yet. Your latest 30 rounds stay on this device.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.history) { line ->
                    Card(Modifier.fillMaxWidth()) { Text(line, Modifier.padding(14.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatsScreen(state: ArenaState) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("Stats", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        StatLine("Rounds", state.stats.roundsPlayed.toString())
        StatLine("Wins", state.stats.wins.toString())
        StatLine("Losses", state.stats.losses.toString())
        StatLine("Draws", state.stats.draws.toString())
        StatLine("Win rate", "${state.stats.winRate}%")
        StatLine("Current streak", state.stats.currentStreak.toString())
        StatLine("Best streak", state.stats.bestStreak.toString())
    }
}

@Composable
private fun AchievementsScreen(state: ArenaState) {
    Column(Modifier.fillMaxSize()) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("Achievements", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.achievements) { achievement ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (achievement.unlocked) "🏆" else "🔒", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(achievement.title, fontWeight = FontWeight.Bold)
                            Text(achievement.description)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: ArenaState) {
    val settings = state.settings
    var backupText by remember { mutableStateOf("") }
    var dataMessage by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        SettingsCard("Appearance & accessibility") {
            SwitchRow("Follow system theme", settings.followSystemTheme) {
                state.updateSettings(settings.copy(followSystemTheme = it))
            }
            SwitchRow("Dark theme", settings.darkTheme, enabled = !settings.followSystemTheme) {
                state.updateSettings(settings.copy(darkTheme = it))
            }
            SwitchRow("Reduced motion", settings.reducedMotion) {
                state.updateSettings(settings.copy(reducedMotion = it))
            }
            Text(
                "Core game actions use text labels, large touch targets, keyboard-compatible controls, and non-color-only results.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        SettingsCard("Privacy & local data") {
            Text("No account or cloud sync is required. Settings, statistics, and recent history remain local to this device.")
            OutlinedButton(
                onClick = {
                    backupText = state.exportBackup()
                    dataMessage = "Backup generated below."
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Generate backup") }
            OutlinedTextField(
                value = backupText,
                onValueChange = { backupText = it },
                label = { Text("RPS Arena backup text") },
                supportingText = { Text("Versioned plain text; do not use this field for secrets.") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    dataMessage = if (state.importBackup(backupText)) {
                        "Backup imported successfully."
                    } else {
                        "Backup rejected: unsupported or malformed data."
                    }
                },
                enabled = backupText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Import backup") }
            OutlinedButton(
                onClick = {
                    state.clearHistory()
                    dataMessage = "Recent history cleared. Lifetime statistics were kept."
                },
                enabled = state.history.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Clear recent history") }

            if (!confirmReset) {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Reset all local data") }
            } else {
                Text("Reset settings, statistics, match setup, and history on this device?")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            state.resetAllData()
                            backupText = ""
                            dataMessage = "All local RPS Arena data reset."
                            confirmReset = false
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Confirm reset") }
                    OutlinedButton(
                        onClick = { confirmReset = false },
                        modifier = Modifier.weight(1f),
                    ) { Text("Cancel") }
                }
            }
            dataMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        SettingsCard("Updates & project") {
            Text("Updates are distributed through project releases. RPS Arena does not silently install updates or require background network access.")
            TextButton(onClick = { state.navigate(ArenaScreen.ABOUT) }) { Text("Open About & support") }
        }
    }
}

@Composable
private fun AboutScreen(state: ArenaState) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("About RPS Arena", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Version 1.0.0 · MIT License")
        Text("An open-source, offline-first Rock Paper Scissors game for Android and desktop.")
        Text("Optional extended rules: Rock–Paper–Scissors–Lizard–Spock.")
        Text("CPU modes are local and deterministic from a seed; no hidden online model is used.")
        Text("Made by the Sanskar", fontWeight = FontWeight.Bold)
        ExternalLink("Repository", "https://github.com/sanskarIN/rps-arena") { uriHandler.openUri(it) }
        ExternalLink("GitHub profile", "https://github.com/sanskarIN") { uriHandler.openUri(it) }
        ExternalLink("Buy Me a Coffee", "https://buymeacoffee.com/sanskarIN") { uriHandler.openUri(it) }
        ExternalLink("Business · sanskarin@outlook.in", "mailto:sanskarin@outlook.in") { uriHandler.openUri(it) }
        ExternalLink("Business · sanskarin.business@gmail.com", "mailto:sanskarin.business@gmail.com") { uriHandler.openUri(it) }
        ExternalLink("Support · supportramsandesh@gmail.com", "mailto:supportramsandesh@gmail.com") { uriHandler.openUri(it) }
        Text("Funding is optional; every game feature remains usable without donating.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ExternalLink(label: String, url: String, open: (String) -> Unit) {
    TextButton(onClick = { runCatching { open(url) } }) { Text(label) }
}

@Composable
private fun BackButton(onClick: () -> Unit) = TextButton(onClick = onClick) { Text("← Back") }

@Composable
private fun ConfigRow(label: String, content: @Composable RowScope.() -> Unit) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content,
    )
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun StatLine(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
