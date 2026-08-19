package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.*
import `in`.sanskar.rpsarena.state.ArenaScreen
import `in`.sanskar.rpsarena.state.ArenaState

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
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("Play", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ConfigRow("Opponent") {
            ChoiceChip("CPU", config.opponentMode == OpponentMode.CPU) { state.updateConfig(config.copy(opponentMode = OpponentMode.CPU)) }
            ChoiceChip("2 Player", config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) { state.updateConfig(config.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER)) }
        }
        ConfigRow("Rules") {
            ChoiceChip("Classic", config.variant == GameVariant.CLASSIC) { state.updateConfig(config.copy(variant = GameVariant.CLASSIC)) }
            ChoiceChip("Lizard–Spock", config.variant == GameVariant.LIZARD_SPOCK) { state.updateConfig(config.copy(variant = GameVariant.LIZARD_SPOCK)) }
        }
        if (config.opponentMode == OpponentMode.CPU) {
            ConfigRow("Difficulty") {
                Difficulty.entries.forEach { d -> ChoiceChip(d.name.lowercase().replaceFirstChar { it.uppercase() }, config.difficulty == d) { state.updateConfig(config.copy(difficulty = d)) } }
            }
        }
        ConfigRow("Mode") {
            MatchMode.entries.forEach { m ->
                ChoiceChip(m.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, config.matchMode == m) { state.updateConfig(config.copy(matchMode = m)) }
            }
        }
        ScoreCard(state)
        if (config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) Text(state.localTurnMessage, fontWeight = FontWeight.SemiBold)
        Text("Choose a gesture", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            gestures.take(3).forEach { gesture -> GestureButton(gesture, Modifier.weight(1f)) { state.play(gesture) } }
        }
        if (gestures.size > 3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gestures.drop(3).forEach { gesture -> GestureButton(gesture, Modifier.weight(1f)) { state.play(gesture) } }
            }
        }
        state.match.rounds.lastOrNull()?.let { round ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Last round", fontWeight = FontWeight.Bold)
                    Text("${round.playerOne.emoji} ${round.playerOne.label} vs ${round.playerTwo.emoji} ${round.playerTwo.label}")
                    Text(when (round.outcome) {
                        RoundOutcome.PLAYER_ONE_WIN -> "Player 1 wins the round"
                        RoundOutcome.PLAYER_TWO_WIN -> "Player 2 wins the round"
                        RoundOutcome.DRAW -> "Draw"
                    })
                }
            }
        }
        if (state.match.finished) {
            Button(onClick = state::resetMatch, modifier = Modifier.fillMaxWidth()) { Text("New match") }
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

@Composable private fun Score(label: String, value: Int) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(label)
}

@Composable
private fun GestureButton(gesture: Gesture, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier.height(88.dp)) {
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
        Text("History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (state.history.isEmpty()) Text("No rounds yet. Your latest 30 rounds stay on this device.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.history) { line -> Card(Modifier.fillMaxWidth()) { Text(line, Modifier.padding(14.dp)) } }
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
            items(state.achievements) { a ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (a.unlocked) "🏆" else "🔒", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(12.dp))
                        Column { Text(a.title, fontWeight = FontWeight.Bold); Text(a.description) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: ArenaState) {
    val s = state.settings
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        SwitchRow("Follow system theme", s.followSystemTheme) { state.updateSettings(s.copy(followSystemTheme = it)) }
        SwitchRow("Dark theme", s.darkTheme, enabled = !s.followSystemTheme) { state.updateSettings(s.copy(darkTheme = it)) }
        SwitchRow("Reduced motion", s.reducedMotion) { state.updateSettings(s.copy(reducedMotion = it)) }
        SwitchRow("Sound", s.soundEnabled) { state.updateSettings(s.copy(soundEnabled = it)) }
        SwitchRow("Haptics", s.hapticsEnabled) { state.updateSettings(s.copy(hapticsEnabled = it)) }
        Text("Accessibility: all core actions have text labels and support keyboard/touch navigation.")
    }
}

@Composable
private fun AboutScreen(state: ArenaState) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text("About RPS Arena", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("An open-source, offline-first Rock Paper Scissors game for Android and desktop.")
        Text("Optional extended rules: Rock–Paper–Scissors–Lizard–Spock.")
        Text("CPU modes are local and deterministic from a seed; no hidden online model is used.")
        Text("Made by the Sanskar", fontWeight = FontWeight.Bold)
        Text("Business: sanskarin@outlook.in · sanskarin.business@gmail.com")
        Text("Support: supportramsandesh@gmail.com")
        Text("GitHub: github.com/sanskarIN/rps-arena")
        Text("Support development: buymeacoffee.com/sanskarIN")
    }
}

@Composable private fun BackButton(onClick: () -> Unit) = TextButton(onClick = onClick) { Text("← Back") }

@Composable
private fun ConfigRow(label: String, content: @Composable RowScope.() -> Unit) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), content = content)
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun StatLine(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(value, fontWeight = FontWeight.Bold) } }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, enabled: Boolean = true, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
    }
}
