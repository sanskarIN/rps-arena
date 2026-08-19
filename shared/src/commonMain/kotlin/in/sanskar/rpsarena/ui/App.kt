package `in`.sanskar.rpsarena.ui

import androidx.compose.animation.Crossfade
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
import kotlinx.coroutines.delay

@Composable
fun RpsArenaApp(repository: ArenaRepository = ArenaRepository()) {
    val state = remember { ArenaState(repository) }
    val strings = stringsFor(state.settings.language)
    ArenaTheme(state.settings) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!state.settings.onboardingComplete) {
                OnboardingScreen(strings = strings, onDone = state::completeOnboarding)
            } else {
                ArenaScaffold(state, strings)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArenaScaffold(state: ArenaState, strings: ArenaStrings) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appName) },
                actions = {
                    TextButton(onClick = { state.navigate(ArenaScreen.SETTINGS) }) {
                        Text(strings.settings)
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (state.screen) {
                ArenaScreen.HOME -> HomeScreen(state, strings)
                ArenaScreen.PLAY -> PlayScreen(state, strings)
                ArenaScreen.HISTORY -> HistoryScreen(state, strings)
                ArenaScreen.STATS -> StatsScreen(state, strings)
                ArenaScreen.ACHIEVEMENTS -> AchievementsScreen(state, strings)
                ArenaScreen.SETTINGS -> SettingsScreen(state, strings)
                ArenaScreen.ABOUT -> AboutScreen(state, strings)
            }
        }
    }
}

@Composable
private fun OnboardingScreen(strings: ArenaStrings, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🪨 📄 ✂️", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(20.dp))
        Text(strings.welcomeTitle, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(strings.welcomeBody)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text(strings.enterArena) }
    }
}

@Composable
private fun HomeScreen(state: ArenaState, strings: ArenaStrings) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(strings.chooseArena, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${state.settings.playerName} · ${strings.offlineTagline}")
        Button(onClick = { state.navigate(ArenaScreen.PLAY) }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.play)
        }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.STATS) }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.stats)
        }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.HISTORY) }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.history)
        }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.ACHIEVEMENTS) }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.achievements)
        }
        OutlinedButton(onClick = { state.navigate(ArenaScreen.ABOUT) }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.about)
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(strings.madeBy, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PlayScreen(state: ArenaState, strings: ArenaStrings) {
    val config = state.config
    val gestures = Gesture.availableFor(config.variant)
    var seedText by remember(config.seed) { mutableStateOf(config.seed.toString()) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(strings) { state.navigate(ArenaScreen.HOME) }
        Text(strings.play, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        ConfigRow(strings.opponent) {
            ChoiceChip(strings.cpu, config.opponentMode == OpponentMode.CPU) {
                state.updateConfig(config.copy(opponentMode = OpponentMode.CPU))
            }
            ChoiceChip(strings.twoPlayer, config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
                state.updateConfig(config.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER))
            }
        }

        ConfigRow(strings.rules) {
            ChoiceChip(strings.classic, config.variant == GameVariant.CLASSIC) {
                state.updateConfig(config.copy(variant = GameVariant.CLASSIC))
            }
            ChoiceChip(strings.lizardSpock, config.variant == GameVariant.LIZARD_SPOCK) {
                state.updateConfig(config.copy(variant = GameVariant.LIZARD_SPOCK))
            }
        }

        if (config.opponentMode == OpponentMode.CPU) {
            ConfigRow(strings.difficulty) {
                Difficulty.entries.forEach { difficulty ->
                    ChoiceChip(
                        strings.difficultyLabel(difficulty),
                        config.difficulty == difficulty,
                    ) {
                        state.updateConfig(config.copy(difficulty = difficulty))
                    }
                }
            }
        }

        ConfigRow(strings.mode) {
            MatchMode.entries.forEach { mode ->
                ChoiceChip(strings.modeLabel(mode), config.matchMode == mode) {
                    state.updateConfig(config.copy(matchMode = mode))
                }
            }
        }

        ConfigRow(strings.timer) {
            MatchConfig.ALLOWED_TIMER_SECONDS.sorted().forEach { seconds ->
                ChoiceChip(
                    if (seconds == 0) strings.timerOff else "${seconds}s",
                    config.roundTimerSeconds == seconds,
                ) {
                    state.updateConfig(config.copy(roundTimerSeconds = seconds))
                }
            }
        }

        if (config.opponentMode == OpponentMode.CPU) {
            OutlinedTextField(
                value = seedText,
                onValueChange = { value -> seedText = value.filter { it == '-' || it.isDigit() }.take(11) },
                label = { Text(strings.seed) },
                supportingText = { Text(strings.seedReplayHint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            val parsedSeed = seedText.toIntOrNull()
            OutlinedButton(
                onClick = { parsedSeed?.let { state.updateConfig(config.copy(seed = it)) } },
                enabled = parsedSeed != null && parsedSeed != config.seed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.applySeed)
            }
        }

        ScoreCard(state, strings)
        RoundTimer(state, strings)

        if (config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
            Text(
                if (state.pendingPlayerOne == null) strings.localTurnOne else strings.localTurnTwo,
                fontWeight = FontWeight.SemiBold,
            )
        }

        state.lastAnnouncement?.let { announcement ->
            Text(localizeRoundAnnouncement(announcement, strings), style = MaterialTheme.typography.bodyMedium)
        }

        Text(strings.chooseGesture, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            gestures.take(3).forEach { gesture ->
                GestureButton(gesture, strings, Modifier.weight(1f)) { state.play(gesture) }
            }
        }
        if (gestures.size > 3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gestures.drop(3).forEach { gesture ->
                    GestureButton(gesture, strings, Modifier.weight(1f)) { state.play(gesture) }
                }
            }
        }

        val lastRound = state.match.rounds.lastOrNull()
        if (state.settings.reducedMotion) {
            lastRound?.let { RoundResultCard(it, strings) }
        } else {
            Crossfade(targetState = lastRound, label = "round-result") { round ->
                round?.let { RoundResultCard(it, strings) }
            }
        }

        if (state.match.finished) {
            Button(onClick = state::resetMatch, modifier = Modifier.fillMaxWidth()) {
                Text(strings.newMatch)
            }
        }
    }
}

@Composable
private fun RoundTimer(state: ArenaState, strings: ArenaStrings) {
    val seconds = state.config.roundTimerSeconds
    if (seconds <= 0) return

    var remaining by remember(
        seconds,
        state.match.rounds.size,
        state.pendingPlayerOne,
        state.match.finished,
    ) { mutableStateOf(seconds) }

    LaunchedEffect(seconds, state.match.rounds.size, state.pendingPlayerOne, state.match.finished) {
        remaining = seconds
        if (state.match.finished) return@LaunchedEffect
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
        state.expireCurrentTurn()
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${strings.timerRemaining}: ${remaining}s", fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(
            progress = { remaining.toFloat() / seconds.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RoundResultCard(round: RoundRecord, strings: ArenaStrings) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.lastRound, fontWeight = FontWeight.Bold)
            when (round.endReason) {
                RoundEndReason.PLAYED -> {
                    val first = requireNotNull(round.playerOne)
                    val second = requireNotNull(round.playerTwo)
                    Text("${first.emoji} ${strings.gestureLabel(first)} vs ${second.emoji} ${strings.gestureLabel(second)}")
                }
                RoundEndReason.PLAYER_ONE_TIMEOUT -> Text(strings.playerOneTimeout)
                RoundEndReason.PLAYER_TWO_TIMEOUT -> Text(strings.playerTwoTimeout)
            }
            Text(
                when (round.outcome) {
                    RoundOutcome.PLAYER_ONE_WIN -> strings.playerOneWinsRound
                    RoundOutcome.PLAYER_TWO_WIN -> strings.playerTwoWinsRound
                    RoundOutcome.DRAW -> strings.roundDraw
                },
            )
        }
    }
}

@Composable
private fun ScoreCard(state: ArenaState, strings: ArenaStrings) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Score(state.settings.playerName, state.match.playerOneScore)
            Score(strings.draws, state.match.draws)
            Score(if (state.config.opponentMode == OpponentMode.CPU) strings.cpu else "P2", state.match.playerTwoScore)
        }
    }
}

@Composable
private fun Score(label: String, value: Int) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(label)
}

@Composable
private fun GestureButton(
    gesture: Gesture,
    strings: ArenaStrings,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    FilledTonalButton(onClick = onClick, modifier = modifier.heightIn(min = 88.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(gesture.emoji, style = MaterialTheme.typography.headlineMedium)
            Text(strings.gestureLabel(gesture))
        }
    }
}

@Composable
private fun HistoryScreen(state: ArenaState, strings: ArenaStrings) {
    Column(Modifier.fillMaxSize()) {
        BackButton(strings) { state.navigate(ArenaScreen.HOME) }
        Text(strings.history, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (state.history.isEmpty()) {
            Text(strings.noHistory)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.history) { line ->
                    Card(Modifier.fillMaxWidth()) {
                        Text(localizeRoundAnnouncement(line, strings), Modifier.padding(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsScreen(state: ArenaState, strings: ArenaStrings) {
    val trend = state.recentTrend
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BackButton(strings) { state.navigate(ArenaScreen.HOME) }
        Text(strings.stats, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        StatLine(strings.rounds, state.stats.roundsPlayed.toString())
        StatLine(strings.wins, state.stats.wins.toString())
        StatLine(strings.losses, state.stats.losses.toString())
        StatLine(strings.draws, state.stats.draws.toString())
        StatLine(strings.winRate, "${state.stats.winRate}%")
        StatLine(strings.bestStreak, state.stats.bestStreak.toString())
        StatLine(
            strings.recentTrend,
            "${trend.wins} ${strings.wins} · ${trend.losses} ${strings.losses} · ${trend.draws} ${strings.draws}",
        )
    }
}

@Composable
private fun AchievementsScreen(state: ArenaState, strings: ArenaStrings) {
    Column(Modifier.fillMaxSize()) {
        BackButton(strings) { state.navigate(ArenaScreen.HOME) }
        Text(strings.achievements, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
private fun SettingsScreen(state: ArenaState, strings: ArenaStrings) {
    val settings = state.settings
    var playerName by remember(settings.playerName) { mutableStateOf(settings.playerName) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BackButton(strings) { state.navigate(ArenaScreen.HOME) }
        Text(strings.settings, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        SectionTitle(strings.appearance)
        SwitchRow(strings.followSystemTheme, settings.followSystemTheme) {
            state.updateSettings(settings.copy(followSystemTheme = it))
        }
        SwitchRow(strings.darkTheme, settings.darkTheme, enabled = !settings.followSystemTheme) {
            state.updateSettings(settings.copy(darkTheme = it))
        }

        SectionTitle(strings.accessibility)
        SwitchRow(strings.reducedMotion, settings.reducedMotion) {
            state.updateSettings(settings.copy(reducedMotion = it))
        }
        SwitchRow(strings.sound, settings.soundEnabled) {
            state.updateSettings(settings.copy(soundEnabled = it))
        }
        SwitchRow(strings.haptics, settings.hapticsEnabled) {
            state.updateSettings(settings.copy(hapticsEnabled = it))
        }
        Text(strings.accessibilityNote)

        SectionTitle(strings.playerName)
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it.take(ArenaRepository.MAX_PLAYER_NAME_LENGTH) },
            label = { Text(strings.playerName) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = { state.updateSettings(settings.copy(playerName = playerName)) },
            enabled = playerName.trim().isNotEmpty() && playerName.trim() != settings.playerName,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(strings.savePlayerName)
        }

        SectionTitle(strings.language)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(strings.english, settings.language == AppLanguage.ENGLISH) {
                state.updateSettings(settings.copy(language = AppLanguage.ENGLISH))
            }
            ChoiceChip(strings.hindi, settings.language == AppLanguage.HINDI) {
                state.updateSettings(settings.copy(language = AppLanguage.HINDI))
            }
        }

        SectionTitle(strings.dataPrivacy)
        Text(strings.backupHint)
        OutlinedTextField(
            value = state.backupText,
            onValueChange = state::updateBackupText,
            label = { Text("RPS_ARENA_BACKUP|1") },
            minLines = 4,
            maxLines = 10,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = state::prepareBackup, modifier = Modifier.weight(1f)) {
                Text(strings.exportBackup)
            }
            Button(
                onClick = state::importBackup,
                enabled = state.backupText.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) {
                Text(strings.importBackup)
            }
        }
        state.dataMessage?.let { Text(localizeDataMessage(it, strings), style = MaterialTheme.typography.bodyMedium) }

        if (!confirmReset) {
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.resetLocalData)
            }
        } else {
            Text(strings.resetWarning)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { confirmReset = false }, modifier = Modifier.weight(1f)) {
                    Text(strings.cancel)
                }
                Button(
                    onClick = {
                        state.clearUserData()
                        confirmReset = false
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(strings.confirmReset)
                }
            }
        }
    }
}

@Composable
private fun AboutScreen(state: ArenaState, strings: ArenaStrings) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackButton(strings) { state.navigate(ArenaScreen.HOME) }
        Text("${strings.about} ${strings.appName}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(strings.aboutBody)
        Text(strings.extendedRules)
        Text(strings.cpuTransparency)
        Text("${strings.version}: $APP_VERSION")
        Text("${strings.license}: $APP_LICENSE")
        Text(strings.madeBy, fontWeight = FontWeight.Bold)
        Text("Business: sanskarin@outlook.in · sanskarin.business@gmail.com")
        Text("Support: supportramsandesh@gmail.com")
        Text("GitHub: github.com/sanskarIN/rps-arena")
        Text("Support development: buymeacoffee.com/sanskarIN")
    }
}

private fun localizeRoundAnnouncement(raw: String, strings: ArenaStrings): String {
    if (raw == "Player 1 timed out — Player 2 won") {
        return "${strings.playerOneTimeout} — ${strings.playerTwoWinsRound}"
    }
    if (raw == "Player 2 timed out — Player 1 won") {
        return "${strings.playerTwoTimeout} — ${strings.playerOneWinsRound}"
    }

    val delimiter = " — "
    val parts = raw.split(delimiter, limit = 2)
    if (parts.size != 2) return raw
    val gestureParts = parts[0].split(" vs ", limit = 2)
    if (gestureParts.size != 2) return raw

    val first = Gesture.entries.firstOrNull { it.label == gestureParts[0] }
    val second = Gesture.entries.firstOrNull { it.label == gestureParts[1] }
    if (first == null || second == null) return raw

    val outcome = when (parts[1]) {
        "Player 1 won" -> strings.playerOneWinsRound
        "Player 2 won" -> strings.playerTwoWinsRound
        "Draw" -> strings.roundDraw
        else -> parts[1]
    }
    return "${strings.gestureLabel(first)} vs ${strings.gestureLabel(second)}$delimiter$outcome"
}

private fun localizeDataMessage(raw: String, strings: ArenaStrings): String = when {
    raw == "Backup text is ready to copy and save securely." -> strings.backupReady
    raw == "Local statistics, history, and preferences were reset." -> strings.localDataReset
    raw == "Unsupported or missing backup header" -> strings.invalidBackupHeader
    raw == "Backup is too large" -> strings.backupTooLarge
    raw == "Backup contains too many records" -> strings.backupTooManyRecords
    raw == "Malformed backup record" -> strings.malformedBackupRecord
    raw == "Invalid settings record" -> strings.invalidSettingsRecord
    raw == "Invalid stats record" -> strings.invalidStatsRecord
    raw == "Backup has no settings record" -> strings.backupMissingSettings
    raw == "Backup has no stats record" -> strings.backupMissingStats
    raw == "Duplicate settings record" -> strings.duplicateSettingsRecord
    raw == "Duplicate stats record" -> strings.duplicateStatsRecord
    raw.startsWith("Unknown backup record:") -> "${strings.unknownBackupRecord}:${raw.substringAfter(':')}"
    raw.startsWith("Imported settings, statistics, and ") -> {
        val count = raw.removePrefix("Imported settings, statistics, and ").substringBefore(' ')
        "${strings.backupImportedPrefix} $count ${strings.historyRecords}"
    }
    else -> raw
}

@Composable
private fun BackButton(strings: ArenaStrings, onClick: () -> Unit) =
    TextButton(onClick = onClick) { Text(strings.back) }

@Composable
private fun ConfigRow(label: String, content: @Composable RowScope.() -> Unit) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    Row(
        Modifier.fillMaxWidth(),
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
private fun SectionTitle(label: String) {
    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
