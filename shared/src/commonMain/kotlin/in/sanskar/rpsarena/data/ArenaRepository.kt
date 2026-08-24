package `in`.sanskar.rpsarena.data

import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode

class ArenaRepository(private val store: ArenaStore = PlatformArenaStore) {
    fun loadSettings(): ArenaSettings = decodeSettings(store.getString(KEY_SETTINGS))
    fun saveSettings(value: ArenaSettings) = store.putString(KEY_SETTINGS, encodeSettings(value))

    fun loadMatchConfig(): MatchConfig = decodeMatchConfig(store.getString(KEY_MATCH_CONFIG))
    fun saveMatchConfig(value: MatchConfig) = store.putString(KEY_MATCH_CONFIG, encodeMatchConfig(value))

    fun loadStats(): ArenaStats = decodeStats(store.getString(KEY_STATS))
    fun saveStats(value: ArenaStats) = store.putString(KEY_STATS, encodeStats(value))

    fun loadHistory(): List<String> = store.getString(KEY_HISTORY)
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(MAX_HISTORY)
        .toList()

    fun addHistory(line: String) {
        val updated = (listOf(line.replace('\n', ' ')) + loadHistory()).take(MAX_HISTORY)
        saveHistory(updated)
    }

    fun exportBackup(): String = ArenaBackupCodec.encode(
        ArenaBackup(
            settings = loadSettings(),
            stats = loadStats(),
            history = loadHistory(),
        ),
    )

    fun importBackup(raw: String): ArenaBackupImportResult = when (val result = ArenaBackupCodec.decode(raw)) {
        is ArenaBackupDecodeResult.Failure -> ArenaBackupImportResult.Failure(result.error)
        is ArenaBackupDecodeResult.Success -> {
            val backup = result.backup
            saveSettings(backup.settings)
            saveStats(backup.stats)
            saveHistory(backup.history)
            ArenaBackupImportResult.Success(backup.history.size)
        }
    }

    internal fun encodeSettings(value: ArenaSettings): String = listOf(
        value.darkTheme,
        value.followSystemTheme,
        value.reducedMotion,
        value.soundEnabled,
        value.hapticsEnabled,
        value.extendedVariant,
        value.onboardingComplete,
    ).joinToString("|")

    internal fun decodeSettings(raw: String): ArenaSettings {
        val p = raw.split('|')
        if (p.size != 7) return ArenaSettings()
        return ArenaSettings(
            darkTheme = p[0].toBooleanStrictOrNull() ?: false,
            followSystemTheme = p[1].toBooleanStrictOrNull() ?: true,
            reducedMotion = p[2].toBooleanStrictOrNull() ?: false,
            soundEnabled = p[3].toBooleanStrictOrNull() ?: true,
            hapticsEnabled = p[4].toBooleanStrictOrNull() ?: true,
            extendedVariant = p[5].toBooleanStrictOrNull() ?: false,
            onboardingComplete = p[6].toBooleanStrictOrNull() ?: false,
        )
    }

    internal fun encodeMatchConfig(value: MatchConfig): String = listOf(
        value.variant.name,
        value.opponentMode.name,
        value.difficulty.name,
        value.matchMode.name,
        value.seed,
    ).joinToString("|")

    internal fun decodeMatchConfig(raw: String): MatchConfig {
        val p = raw.split('|')
        if (p.size != 5) return MatchConfig()
        val variant = GameVariant.entries.firstOrNull { it.name == p[0] } ?: return MatchConfig()
        val opponentMode = OpponentMode.entries.firstOrNull { it.name == p[1] } ?: return MatchConfig()
        val difficulty = Difficulty.entries.firstOrNull { it.name == p[2] } ?: return MatchConfig()
        val matchMode = MatchMode.entries.firstOrNull { it.name == p[3] } ?: return MatchConfig()
        val seed = p[4].toIntOrNull() ?: return MatchConfig()
        return MatchConfig(
            variant = variant,
            opponentMode = opponentMode,
            difficulty = difficulty,
            matchMode = matchMode,
            seed = seed,
        )
    }

    internal fun encodeStats(value: ArenaStats): String = listOf(
        value.roundsPlayed, value.wins, value.losses, value.draws,
        value.bestStreak, value.currentStreak,
    ).joinToString("|")

    internal fun decodeStats(raw: String): ArenaStats {
        val p = raw.split('|').mapNotNull { it.toIntOrNull() }
        if (p.size != 6) return ArenaStats()
        return ArenaStats(p[0], p[1], p[2], p[3], p[4], p[5])
    }

    private fun saveHistory(lines: List<String>) {
        val sanitized = lines
            .asSequence()
            .map { it.replace('\r', ' ').replace('\n', ' ').trim() }
            .filter { it.isNotEmpty() }
            .take(MAX_HISTORY)
            .toList()
        store.putString(KEY_HISTORY, sanitized.joinToString("\n"))
    }

    companion object {
        const val MAX_HISTORY = ArenaBackupCodec.MAX_HISTORY_ITEMS
        private const val KEY_SETTINGS = "settings_v1"
        private const val KEY_MATCH_CONFIG = "match_config_v1"
        private const val KEY_STATS = "stats_v1"
        private const val KEY_HISTORY = "history_v1"
    }
}
