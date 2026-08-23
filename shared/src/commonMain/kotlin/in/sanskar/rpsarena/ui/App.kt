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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.data.ArenaBackupError
import `in`.sanskar.rpsarena.data.ArenaBackupImportResult
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.*
import `in`.sanskar.rpsarena.resources.*
import `in`.sanskar.rpsarena.state.ArenaScreen
import `in`.sanskar.rpsarena.state.ArenaState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
                title = { Text(stringResource(Res.string.app_name)) },
                actions = {
                    TextButton(
                        onClick = { state.navigate(ArenaScreen.SETTINGS) },
                        modifier = Modifier.testTag(ArenaUiTags.TOP_SETTINGS),
                    ) {
                        Text(stringResource(Res.string.settings))
                    }
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
        modifier = Modifier.fillMaxSize().padding(28.dp).testTag(ArenaUiTags.ONBOARDING_SCREEN),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🪨 📄 ✂️", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(Res.string.welcome_title), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(Res.string.welcome_body))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone, modifier = Modifier.testTag(ArenaUiTags.ENTER_ARENA)) {
            Text(stringResource(Res.string.enter_arena))
        }
    }
}

@Composable
private fun HomeScreen(state: ArenaState) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag(ArenaUiTags.HOME_SCREEN),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(Res.string.home_subtitle))
        Button(
            onClick = { state.navigate(ArenaScreen.PLAY) },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.HOME_PLAY),
        ) { Text(stringResource(Res.string.play)) }
        OutlinedButton(
            onClick = { state.navigate(ArenaScreen.STATS) },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.HOME_STATS),
        ) { Text(stringResource(Res.string.stats)) }
        OutlinedButton(
            onClick = { state.navigate(ArenaScreen.HISTORY) },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.HOME_HISTORY),
        ) { Text(stringResource(Res.string.history)) }
        OutlinedButton(
            onClick = { state.navigate(ArenaScreen.ACHIEVEMENTS) },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.HOME_ACHIEVEMENTS),
        ) { Text(stringResource(Res.string.achievements)) }
        OutlinedButton(
            onClick = { state.navigate(ArenaScreen.ABOUT) },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.HOME_ABOUT),
        ) { Text(stringResource(Res.string.about)) }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(stringResource(Res.string.made_by), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PlayScreen(state: ArenaState) {
    val config = state.config
    val gestures = Gesture.availableFor(config.variant)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag(ArenaUiTags.PLAY_SCREEN),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(modifier = Modifier.testTag(ArenaUiTags.PLAY_BACK)) { state.navigate(ArenaScreen.HOME) }
        Text(stringResource(Res.string.play), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ConfigRow(stringResource(Res.string.opponent)) {
            ChoiceChip(stringResource(Res.string.cpu), config.opponentMode == OpponentMode.CPU) { state.updateConfig(config.copy(opponentMode = OpponentMode.CPU)) }
            ChoiceChip(stringResource(Res.string.two_player), config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) { state.updateConfig(config.copy(opponentMode = OpponentMode.LOCAL_TWO_PLAYER)) }
        }
        ConfigRow(stringResource(Res.string.rules)) {
            ChoiceChip(stringResource(Res.string.classic), config.variant == GameVariant.CLASSIC) { state.updateConfig(config.copy(variant = GameVariant.CLASSIC)) }
            ChoiceChip(stringResource(Res.string.lizard_spock), config.variant == GameVariant.LIZARD_SPOCK) { state.updateConfig(config.copy(variant = GameVariant.LIZARD_SPOCK)) }
        }
        if (config.opponentMode == OpponentMode.CPU) {
            ConfigRow(stringResource(Res.string.difficulty)) {
                Difficulty.entries.forEach { difficulty ->
                    ChoiceChip(stringResource(difficulty.resource), config.difficulty == difficulty) {
                        state.updateConfig(config.copy(difficulty = difficulty))
                    }
                }
            }
        }
        ConfigRow(stringResource(Res.string.mode)) {
            MatchMode.entries.forEach { mode ->
                ChoiceChip(stringResource(mode.resource), config.matchMode == mode) {
                    state.updateConfig(config.copy(matchMode = mode))
                }
            }
        }
        ScoreCard(state)
        if (config.opponentMode == OpponentMode.LOCAL_TWO_PLAYER) {
            Text(
                stringResource(
                    if (state.pendingPlayerOne == null) Res.string.local_turn_player_one
                    else Res.string.local_turn_player_two,
                ),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(stringResource(Res.string.choose_gesture), style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            gestures.take(3).forEach { gesture ->
                GestureButton(
                    gesture,
                    Modifier.weight(1f).testTag(ArenaUiTags.gesture(gesture.name)),
                ) { state.play(gesture) }
            }
        }
        if (gestures.size > 3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gestures.drop(3).forEach { gesture ->
                    GestureButton(
                        gesture,
                        Modifier.weight(1f).testTag(ArenaUiTags.gesture(gesture.name)),
                    ) { state.play(gesture) }
                }
            }
        }
        state.match.rounds.lastOrNull()?.let { round ->
            Card(Modifier.fillMaxWidth().testTag(ArenaUiTags.LAST_ROUND)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(Res.string.last_round), fontWeight = FontWeight.Bold)
                    val first = "${round.playerOne.emoji} ${stringResource(round.playerOne.resource)}"
                    val second = "${round.playerTwo.emoji} ${stringResource(round.playerTwo.resource)}"
                    Text(stringResource(Res.string.versus, first, second))
                    Text(stringResource(round.outcome.resource))
                }
            }
        }
        if (state.match.finished) {
            Button(
                onClick = state::resetMatch,
                modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.NEW_MATCH),
            ) { Text(stringResource(Res.string.new_match)) }
        }
    }
}

@Composable
private fun ScoreCard(state: ArenaState) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Score(stringResource(Res.string.player_one_short), state.match.playerOneScore)
            Score(stringResource(Res.string.draws), state.match.draws)
            Score(
                if (state.config.opponentMode == OpponentMode.CPU) stringResource(Res.string.cpu) else stringResource(Res.string.player_two_short),
                state.match.playerTwoScore,
            )
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
    FilledTonalButton(onClick = onClick, modifier = modifier.height(88.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(gesture.emoji, style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(gesture.resource))
        }
    }
}

@Composable
private fun HistoryScreen(state: ArenaState) {
    val history = state.historyEntries
    Column(Modifier.fillMaxSize().testTag(ArenaUiTags.HISTORY_SCREEN)) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text(stringResource(Res.string.history), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (history.isEmpty()) Text(stringResource(Res.string.history_empty))
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history) { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Text(historyEntryLabel(entry), Modifier.padding(14.dp))
                }
            }
        }
    }
}

@Composable
private fun historyEntryLabel(entry: ArenaHistoryEntry): String = when (entry) {
    is ArenaHistoryEntry.Round -> {
        val first = "${entry.playerOne.emoji} ${stringResource(entry.playerOne.resource)}"
        val second = "${entry.playerTwo.emoji} ${stringResource(entry.playerTwo.resource)}"
        "${stringResource(Res.string.versus, first, second)} — ${stringResource(entry.outcome.resource)}"
    }
    is ArenaHistoryEntry.Legacy -> entry.summary
}

@Composable
private fun StatsScreen(state: ArenaState) {
    Column(
        Modifier.fillMaxWidth().testTag(ArenaUiTags.STATS_SCREEN),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text(stringResource(Res.string.stats), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        StatLine(stringResource(Res.string.rounds), state.stats.roundsPlayed.toString())
        StatLine(stringResource(Res.string.wins), state.stats.wins.toString())
        StatLine(stringResource(Res.string.losses), state.stats.losses.toString())
        StatLine(stringResource(Res.string.draws), state.stats.draws.toString())
        StatLine(stringResource(Res.string.win_rate), "${state.stats.winRate}%")
        StatLine(stringResource(Res.string.best_streak), state.stats.bestStreak.toString())
    }
}

@Composable
private fun AchievementsScreen(state: ArenaState) {
    Column(Modifier.fillMaxSize().testTag(ArenaUiTags.ACHIEVEMENTS_SCREEN)) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text(stringResource(Res.string.achievements), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.achievements) { achievement ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (achievement.unlocked) "🏆" else "🔒", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(achievement.titleResource), fontWeight = FontWeight.Bold)
                            Text(stringResource(achievement.descriptionResource))
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
    var showExport by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    var exportText by remember { mutableStateOf("") }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<ArenaBackupError?>(null) }
    var importedHistoryCount by remember { mutableStateOf<Int?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag(ArenaUiTags.SETTINGS_SCREEN),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text(stringResource(Res.string.settings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        SwitchRow(stringResource(Res.string.follow_system_theme), settings.followSystemTheme) { state.updateSettings(settings.copy(followSystemTheme = it)) }
        SwitchRow(stringResource(Res.string.dark_theme), settings.darkTheme, enabled = !settings.followSystemTheme) { state.updateSettings(settings.copy(darkTheme = it)) }
        SwitchRow(
            stringResource(Res.string.reduced_motion),
            settings.reducedMotion,
            modifier = Modifier.testTag(ArenaUiTags.SETTINGS_REDUCED_MOTION),
        ) { state.updateSettings(settings.copy(reducedMotion = it)) }
        SwitchRow(stringResource(Res.string.sound), settings.soundEnabled) { state.updateSettings(settings.copy(soundEnabled = it)) }
        SwitchRow(stringResource(Res.string.haptics), settings.hapticsEnabled) { state.updateSettings(settings.copy(hapticsEnabled = it)) }
        Text(stringResource(Res.string.accessibility_note))

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(stringResource(Res.string.backup_restore), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(Res.string.backup_restore_description))
        OutlinedButton(
            onClick = {
                exportText = state.exportBackup()
                importedHistoryCount = null
                showExport = true
            },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.SETTINGS_EXPORT_BACKUP),
        ) { Text(stringResource(Res.string.export_backup)) }
        OutlinedButton(
            onClick = {
                importText = ""
                importError = null
                importedHistoryCount = null
                showImport = true
            },
            modifier = Modifier.fillMaxWidth().testTag(ArenaUiTags.SETTINGS_IMPORT_BACKUP),
        ) { Text(stringResource(Res.string.import_backup)) }
        importedHistoryCount?.let {
            Text(stringResource(Res.string.backup_imported, it), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }

    if (showExport) {
        AlertDialog(
            onDismissRequest = { showExport = false },
            modifier = Modifier.testTag(ArenaUiTags.EXPORT_DIALOG),
            title = { Text(stringResource(Res.string.export_backup)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.export_backup_help))
                    OutlinedTextField(
                        value = exportText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.arena_backup_label)) },
                        minLines = 8,
                        maxLines = 12,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExport = false }) { Text(stringResource(Res.string.done)) }
            },
        )
    }

    if (showImport) {
        AlertDialog(
            onDismissRequest = { showImport = false },
            modifier = Modifier.testTag(ArenaUiTags.IMPORT_DIALOG),
            title = { Text(stringResource(Res.string.import_backup)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.import_backup_help))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = {
                            importText = it
                            importError = null
                        },
                        label = { Text(stringResource(Res.string.backup_text)) },
                        minLines = 8,
                        maxLines = 12,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    )
                    importError?.let { Text(backupErrorLabel(it), style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = importText.isNotBlank(),
                    onClick = {
                        when (val result = state.importBackup(importText)) {
                            is ArenaBackupImportResult.Success -> {
                                importedHistoryCount = result.importedHistoryCount
                                importError = null
                                showImport = false
                            }
                            is ArenaBackupImportResult.Failure -> {
                                importError = result.error
                            }
                        }
                    },
                ) { Text(stringResource(Res.string.import_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showImport = false }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }
}

@Composable
private fun AboutScreen(state: ArenaState) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag(ArenaUiTags.ABOUT_SCREEN),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackButton { state.navigate(ArenaScreen.HOME) }
        Text(stringResource(Res.string.about_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(Res.string.about_summary))
        Text(stringResource(Res.string.about_extended_rules))
        Text(stringResource(Res.string.about_cpu))
        Text(stringResource(Res.string.made_by), fontWeight = FontWeight.Bold)
        Text(stringResource(Res.string.business_contact))
        Text(stringResource(Res.string.support_contact))
        Text(stringResource(Res.string.github_contact))
        Text(stringResource(Res.string.support_development))
    }
}

@Composable
private fun backupErrorLabel(error: ArenaBackupError): String = stringResource(
    when (error) {
        ArenaBackupError.EMPTY -> Res.string.backup_error_empty
        ArenaBackupError.INVALID_HEADER -> Res.string.backup_error_invalid_header
        ArenaBackupError.UNSUPPORTED_SCHEMA -> Res.string.backup_error_unsupported_schema
        ArenaBackupError.MALFORMED_SETTINGS -> Res.string.backup_error_settings
        ArenaBackupError.MALFORMED_STATS -> Res.string.backup_error_stats
        ArenaBackupError.MALFORMED_HISTORY -> Res.string.backup_error_history
    },
)

private val Gesture.resource: StringResource
    get() = when (this) {
        Gesture.ROCK -> Res.string.gesture_rock
        Gesture.PAPER -> Res.string.gesture_paper
        Gesture.SCISSORS -> Res.string.gesture_scissors
        Gesture.LIZARD -> Res.string.gesture_lizard
        Gesture.SPOCK -> Res.string.gesture_spock
    }

private val Difficulty.resource: StringResource
    get() = when (this) {
        Difficulty.EASY -> Res.string.difficulty_easy
        Difficulty.NORMAL -> Res.string.difficulty_normal
        Difficulty.EXPERT -> Res.string.difficulty_expert
    }

private val MatchMode.resource: StringResource
    get() = when (this) {
        MatchMode.BEST_OF_3 -> Res.string.mode_best_of_3
        MatchMode.BEST_OF_5 -> Res.string.mode_best_of_5
        MatchMode.ENDLESS -> Res.string.mode_endless
        MatchMode.STREAK -> Res.string.mode_streak
        MatchMode.TOURNAMENT -> Res.string.mode_tournament
    }

private val RoundOutcome.resource: StringResource
    get() = when (this) {
        RoundOutcome.PLAYER_ONE_WIN -> Res.string.round_player_one_wins
        RoundOutcome.PLAYER_TWO_WIN -> Res.string.round_player_two_wins
        RoundOutcome.DRAW -> Res.string.round_draw
    }

private val Achievement.titleResource: StringResource
    get() = when (id) {
        "first_win" -> Res.string.achievement_first_victory_title
        "ten_rounds" -> Res.string.achievement_arena_regular_title
        "streak_3" -> Res.string.achievement_on_fire_title
        "streak_7" -> Res.string.achievement_unstoppable_title
        "century" -> Res.string.achievement_century_title
        else -> Res.string.achievements
    }

private val Achievement.descriptionResource: StringResource
    get() = when (id) {
        "first_win" -> Res.string.achievement_first_victory_description
        "ten_rounds" -> Res.string.achievement_arena_regular_description
        "streak_3" -> Res.string.achievement_on_fire_description
        "streak_7" -> Res.string.achievement_unstoppable_description
        "century" -> Res.string.achievement_century_description
        else -> Res.string.achievements
    }

@Composable
private fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) =
    TextButton(onClick = onClick, modifier = modifier) { Text(stringResource(Res.string.back)) }

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
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
    modifier: Modifier = Modifier,
    onChecked: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled, modifier = modifier)
    }
}
