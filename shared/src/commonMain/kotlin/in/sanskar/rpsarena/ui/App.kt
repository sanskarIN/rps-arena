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
                title = { Text(Strings.appName) },
                actions = {
                    TextButton(onClick = { state.navigate(ArenaScreen.SETTINGS) }) { Text(Strings.settings) }
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
        Text(Strings.welcomeTitle, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(Strings.welcomeBody)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text(Strings.enterArena) }
    }
}

@Composable
private fun HomeScreen(state: ArenaState) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(Strings.chooseArena, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(Strings.homeSubtitle)
        Button(onClick = { state.navigate(ArenaScreen.PLAY) }, modifier = Modifier.fillMaxWidth()) { Text(Strings.play) }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.STATS) }, modifier = Modifier.fillMaxWidth()) { Text(Strings.stats) }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.HISTORY) }, modifier = Modifier.fillMaxWidth()) { Text(Strings.history) }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.ACHIEVEMENTS) }, modifier = Modifier.fillMaxWidth()) { Text(Strings.achievements) }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.ABOUT) }, modifier = Modifier.fillMaxWidth()) { Text(Strings.about) }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(Strings.madeBy, style = MaterialTheme.typography.labelLarge)
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
        Text(Strings.play, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ConfigRow(Strings.opponent) {
            ChoiceChip(Strings.cpu, config.opponentMode == OpponentMode.CPU) {
                state.updateConfig(config.copy(opponentMode = OpponentMode.CPU))
            }
            ChoiceChip(Strings.twoPlayer, config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
                state.updateConfig(config.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER))
            }
        }
        ConfigRow(Strings.rules) {
            ChoiceChip(Strings.classic, config.variant == GameVariant.CLASSIC) {
                state.updateConfig(config.copy(variant = GameVariant.CLASSIC))
            }
            ChoiceChip(Strings.lizardSpock, config.variant == GameVariant.LIZARD_SPOCK) {
                state.updateConfig(config.copy(variant = GameVariant.LIZARD_SPOCK))
            }
        }
        if (config.opponentMode == OpponentMode.CPU) {
            ConfigRow(Strings.difficulty) {
                Difficulty.entries.forEach { difficulty ->
                    ChoiceChip(
                        Strings.difficultyLabel(difficulty),
                        config.difficulty == difficulty,
                    ) { state.updateConfig(config.copy(difficulty = difficulty)) }
                }
            }
        }
        ConfigRow(Strings.mode) {
            MatchMode.entries.forEach { mode ->
                ChoiceChip(
                    Strings.matchModeLabel(mode),
                    config.matchMode == mode,
                ) { state.updateConfig(config.copy(matchMode = mode)) }
            }
        }
        ConfigRow(Strings.roundTimer) {
            listOf(0, 5, 10, 15, 30, 60).forEach { seconds ->
                ChoiceChip(
                    if (seconds == 0) Strings.timerOff else Strings.timerSeconds(seconds),
                    config.roundTimerSeconds == seconds,
                ) { state.updateConfig(config.copy(roundTimerSeconds = seconds)) }
            }
        }

        Text(Strings.seedTitle, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = seedText,
                onValueChange = { seedText = it.take(11) },
                label = { Text(Strings.seedLabel) },
                supportingText = { Text(Strings.seedHelp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    val seed = seedText.toIntOrNull()
                    if (seed == null) {
                        seedMessage = Strings.seedInvalid
                    } else {
                        state.updateConfig(config.copy(seed = seed))
                        seedMessage = Strings.seedApplied
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically),
            ) { Text(Strings.seedLabel) }
        }
        seedMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        ScoreCard(state)
        if (config.roundTimerSeconds > 0 && !state.match.finished) {
            Text(Strings.turnTimer(secondsLeft), style = MaterialTheme.typography.bodySmall)
        }
        if (config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
            Text(state.localTurnMessage, fontWeight = FontWeight.SemiBold)
        }
        Text(Strings.chooseGesture, style = MaterialTheme.typography.titleLarge)
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
                    Text(Strings.lastRound, fontWeight = FontWeight.Bold)
                    Text("${round.playerOne.emoji} ${round.playerOne.label} vs ${round.playerTwo.emoji} ${round.playerTwo.label}")
                    Text(Strings.outcomeLabel(round.outcome, config.opponentMode == OpponentMode.CPU))
                }
            }
        }
        if (state.match.finished) {
            Button(onClick = state::resetMatch, modifier = Modifier.fillMaxWidth()) { Text(Strings.newMatch) }
        } else if (state.match.rounds.isNotEmpty()) {
            OutlinedButton(onClick = state::resetMatch, modifier = Modifier.fillMaxWidth()) { Text(Strings.restartMatch) }
        }
    }
}

@Composable
private fun ScoreCard(state: ArenaState) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Score(Strings.playerOneShort, state.match.playerOneScore)
            Score(Strings.draws, state.match.draws)
            Score(if (state.config.opponentMode == OpponentMode.CPU) Strings.cpu else Strings.playerTwoShort, state.match.playerTwoScore)
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
            .semantics { contentDescription = Strings.chooseGestureAccessibility(gesture.label) },
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
            Text(Strings.history, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = state::clearHistory, enabled = state.history.isNotEmpty()) { Text(Strings.clear) }
        }
        Spacer(Modifier.height(8.dp))
        if (state.history.isEmpty()) {
            Text(Strings.noHistory)
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
        Text(Strings.stats, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        StatLine(Strings.rounds, state.stats.roundsPlayed.toString())
        StatLine(Strings.wins, state.stats.wins.toString())
        StatLine(Strings.losses, state.stats.losses.toString())
        StatLine(Strings.draws, state.stats.draws.toString())
        StatLine(Strings.winRate, "${state.stats.winRate}%")
        StatLine(Strings.currentStreak, state.stats.currentStreak.toString())
        StatLine(Strings.bestStreak, state.stats.bestStreak.toString())
    }
}

@Composable
private fun AchievementsScreen(state: ArenaState) {
    Column(Modifier.fillMaxSize()) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text(Strings.achievements, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
        Text(Strings.settings, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        SettingsCard(Strings.appearanceAccessibility) {
            SwitchRow(Strings.followSystemTheme, settings.followSystemTheme) {
                state.updateSettings(settings.copy(followSystemTheme = it))
            }
            SwitchRow(Strings.darkTheme, settings.darkTheme, enabled = !settings.followSystemTheme) {
                state.updateSettings(settings.copy(darkTheme = it))
            }
            SwitchRow(Strings.reducedMotion, settings.reducedMotion) {
                state.updateSettings(settings.copy(reducedMotion = it))
            }
            Text(Strings.accessibilitySummary, style = MaterialTheme.typography.bodySmall)
        }

        SettingsCard(Strings.privacyLocalData) {
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
                onValueChange = { backupText = it },
                label = { Text(Strings.backupField) },
                supportingText = { Text(Strings.backupHelp) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    dataMessage = if (state.importBackup(backupText)) Strings.backupImported else Strings.backupRejected
                },
                enabled = backupText.isNotBlank(),
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

        SettingsCard(Strings.updatesProject) {
            Text(Strings.updatesSummary)
            TextButton(onClick = { state.navigate(ArenaScreen.ABOUT) }) { Text(Strings.openAboutSupport) }
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
        Text(Strings.aboutTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(Strings.versionLicense)
        Text(Strings.aboutDescription)
        Text(Strings.aboutExtended)
        Text(Strings.aboutCpu)
        Text(Strings.madeBy, fontWeight = FontWeight.Bold)
        ExternalLink(Strings.repository, "https://github.com/sanskarIN/rps-arena") { uriHandler.openUri(it) }
        ExternalLink(Strings.githubProfile, "https://github.com/sanskarIN") { uriHandler.openUri(it) }
        ExternalLink(Strings.buyMeCoffee, "https://buymeacoffee.com/sanskarIN") { uriHandler.openUri(it) }
        ExternalLink(Strings.businessOutlook, "mailto:sanskarin@outlook.in") { uriHandler.openUri(it) }
        ExternalLink(Strings.businessGmail, "mailto:sanskarin.business@gmail.com") { uriHandler.openUri(it) }
        ExternalLink(Strings.supportEmail, "mailto:supportramsandesh@gmail.com") { uriHandler.openUri(it) }
        Text(Strings.fundingOptional, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ExternalLink(label: String, url: String, open: (String) -> Unit) {
    TextButton(onClick = { runCatching { open(url) } }) { Text(label) }
}

@Composable
private fun BackButton(onClick: () -> Unit) = TextButton(onClick = onClick) { Text(Strings.back) }

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
