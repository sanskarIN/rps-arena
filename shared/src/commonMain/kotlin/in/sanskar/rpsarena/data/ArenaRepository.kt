package `in`.sanskar.rpsarena.data

import `in`.sanskar.rpsarena.model.AppLanguage
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.ArenaTrend
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode

class ArenaRepository(
    private val readString: (String, String) -> String = { key, defaultValue ->
        PlatformStore.getString(key, defaultValue)
    },
    private val writeString: (String, String) -> Unit = { key, value ->
        PlatformStore.putString(key, value)
    },
) {
    fun loadSettings(): ArenaSettings {
        val current = readString(KEY_SETTINGS_V2, "")
        if (current.isNotBlank()) return decodeSettings(current)

        val legacy = readString(KEY_SETTINGS_V1, "")
        if (legacy.isBlank()) return ArenaSettings()

        return decodeSettings(legacy).also(::saveSettings)
    }

    fun saveSettings(value: ArenaSettings) =
        writeString(KEY_SETTINGS_V2, encodeSettings(value.copy(playerName = normalizePlayerName(value.playerName))))

    fun loadMatchConfig(): MatchConfig = decodeMatchConfig(readString(KEY_MATCH_CONFIG_V1, ""))
    fun saveMatchConfig(value: MatchConfig) = writeString(KEY_MATCH_CONFIG_V1, encodeMatchConfig(value))

    fun loadStats(): ArenaStats = decodeStats(readString(KEY_STATS, ""))
    fun saveStats(value: ArenaStats) = writeString(KEY_STATS, encodeStats(value))

    fun loadHistory(): List<String> = readString(KEY_HISTORY, "")
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(MAX_HISTORY)
        .toList()

    fun addHistory(line: String) {
        val sanitized = line.replace('\n', ' ').replace('\r', ' ').trim().take(MAX_HISTORY_LINE_LENGTH)
        if (sanitized.isEmpty()) return
        val updated = (listOf(sanitized) + loadHistory()).take(MAX_HISTORY)
        writeString(KEY_HISTORY, updated.joinToString("\n"))
    }

    fun loadRecentTrend(limit: Int = 10): ArenaTrend {
        val safeLimit = limit.coerceIn(1, MAX_HISTORY)
        var wins = 0
        var losses = 0
        var draws = 0
        loadHistory().take(safeLimit).forEach { line ->
            when {
                line.endsWith("Player 1 won") -> wins++
                line.endsWith("Player 2 won") -> losses++
                line.endsWith("Draw") -> draws++
            }
        }
        return ArenaTrend(wins = wins, losses = losses, draws = draws)
    }

    fun exportBackup(): String = buildString {
        appendLine(BACKUP_HEADER)
        appendLine("settings|${escapeBackupValue(encodeSettings(loadSettings()))}")
        appendLine("stats|${escapeBackupValue(encodeStats(loadStats()))}")
        loadHistory().forEach { line ->
            appendLine("history|${escapeBackupValue(line)}")
        }
    }.trimEnd()

    fun importBackup(raw: String): BackupImportResult {
        if (raw.length > MAX_BACKUP_LENGTH) {
            return BackupImportResult.failure("Backup is too large")
        }

        val lines = raw.lineSequence().map { it.trimEnd('\r') }.toList()
        if (lines.isEmpty() || lines.first() != BACKUP_HEADER) {
            return BackupImportResult.failure("Unsupported or missing backup header")
        }
        if (lines.size > MAX_BACKUP_LINES) {
            return BackupImportResult.failure("Backup contains too many records")
        }

        var settings: ArenaSettings? = null
        var stats: ArenaStats? = null
        val history = mutableListOf<String>()

        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val separator = line.indexOf('|')
            if (separator <= 0) return BackupImportResult.failure("Malformed backup record")
            val type = line.substring(0, separator)
            val value = unescapeBackupValue(line.substring(separator + 1))
            when (type) {
                "settings" -> {
                    if (settings != null) return BackupImportResult.failure("Duplicate settings record")
                    settings = decodeSettingsOrNull(value)
                        ?: return BackupImportResult.failure("Invalid settings record")
                }
                "stats" -> {
                    if (stats != null) return BackupImportResult.failure("Duplicate stats record")
                    stats = decodeStatsOrNull(value)
                        ?: return BackupImportResult.failure("Invalid stats record")
                }
                "history" -> {
                    if (history.size >= MAX_HISTORY) continue
                    val sanitized = value.replace('\n', ' ').replace('\r', ' ').trim().take(MAX_HISTORY_LINE_LENGTH)
                    if (sanitized.isNotEmpty()) history += sanitized
                }
                else -> return BackupImportResult.failure("Unknown backup record: $type")
            }
        }

        val validatedSettings = settings ?: return BackupImportResult.failure("Backup has no settings record")
        val validatedStats = stats ?: return BackupImportResult.failure("Backup has no stats record")

        saveSettings(validatedSettings)
        saveStats(validatedStats)
        writeString(KEY_HISTORY, history.joinToString("\n"))
        return BackupImportResult.success("Imported settings, statistics, and ${history.size} history records")
    }

    fun clearUserData(preserveOnboarding: Boolean = true) {
        val onboarding = preserveOnboarding && loadSettings().onboardingComplete
        saveSettings(ArenaSettings(onboardingComplete = onboarding))
        saveMatchConfig(MatchConfig())
        saveStats(ArenaStats())
        writeString(KEY_HISTORY, "")
    }

    internal fun encodeSettings(value: ArenaSettings): String = listOf(
        value.darkTheme,
        value.followSystemTheme,
        value.reducedMotion,
        value.soundEnabled,
        value.hapticsEnabled,
        value.extendedVariant,
        value.onboardingComplete,
        escapeField(normalizePlayerName(value.playerName)),
        value.language.name,
    ).joinToString("|")

    internal fun decodeSettings(raw: String): ArenaSettings = decodeSettingsOrNull(raw) ?: ArenaSettings()

    internal fun decodeSettingsOrNull(raw: String): ArenaSettings? {
        val p = raw.split('|')
        if (p.size == 7) {
            return ArenaSettings(
                darkTheme = p[0].toBooleanStrictOrNull() ?: return null,
                followSystemTheme = p[1].toBooleanStrictOrNull() ?: return null,
                reducedMotion = p[2].toBooleanStrictOrNull() ?: return null,
                soundEnabled = p[3].toBooleanStrictOrNull() ?: return null,
                hapticsEnabled = p[4].toBooleanStrictOrNull() ?: return null,
                extendedVariant = p[5].toBooleanStrictOrNull() ?: return null,
                onboardingComplete = p[6].toBooleanStrictOrNull() ?: return null,
            )
        }
        if (p.size != 9) return null
        val language = AppLanguage.entries.firstOrNull { it.name == p[8] } ?: return null
        return ArenaSettings(
            darkTheme = p[0].toBooleanStrictOrNull() ?: return null,
            followSystemTheme = p[1].toBooleanStrictOrNull() ?: return null,
            reducedMotion = p[2].toBooleanStrictOrNull() ?: return null,
            soundEnabled = p[3].toBooleanStrictOrNull() ?: return null,
            hapticsEnabled = p[4].toBooleanStrictOrNull() ?: return null,
            extendedVariant = p[5].toBooleanStrictOrNull() ?: return null,
            onboardingComplete = p[6].toBooleanStrictOrNull() ?: return null,
            playerName = normalizePlayerName(unescapeField(p[7])),
            language = language,
        )
    }

    internal fun encodeMatchConfig(value: MatchConfig): String = listOf(
        value.variant.name,
        value.opponentMode.name,
        value.difficulty.name,
        value.matchMode.name,
        value.seed,
        value.roundTimerSeconds,
    ).joinToString("|")

    internal fun decodeMatchConfig(raw: String): MatchConfig = decodeMatchConfigOrNull(raw) ?: MatchConfig()

    internal fun decodeMatchConfigOrNull(raw: String): MatchConfig? {
        val p = raw.split('|')
        if (p.size != 6) return null
        val variant = GameVariant.entries.firstOrNull { it.name == p[0] } ?: return null
        val opponentMode = OpponentMode.entries.firstOrNull { it.name == p[1] } ?: return null
        val difficulty = Difficulty.entries.firstOrNull { it.name == p[2] } ?: return null
        val matchMode = MatchMode.entries.firstOrNull { it.name == p[3] } ?: return null
        val seed = p[4].toIntOrNull() ?: return null
        val roundTimerSeconds = p[5].toIntOrNull() ?: return null
        if (roundTimerSeconds !in MatchConfig.ALLOWED_TIMER_SECONDS) return null
        return MatchConfig(
            variant = variant,
            opponentMode = opponentMode,
            difficulty = difficulty,
            matchMode = matchMode,
            seed = seed,
            roundTimerSeconds = roundTimerSeconds,
        )
    }

    internal fun encodeStats(value: ArenaStats): String = listOf(
        value.roundsPlayed,
        value.wins,
        value.losses,
        value.draws,
        value.bestStreak,
        value.currentStreak,
    ).joinToString("|")

    internal fun decodeStats(raw: String): ArenaStats = decodeStatsOrNull(raw) ?: ArenaStats()

    internal fun decodeStatsOrNull(raw: String): ArenaStats? {
        val p = raw.split('|').map { it.toIntOrNull() ?: return null }
        if (p.size != 6 || p.any { it < 0 }) return null
        if (p[0] != p[1] + p[2] + p[3]) return null
        if (p[5] > p[4] || p[4] > p[1]) return null
        return ArenaStats(p[0], p[1], p[2], p[3], p[4], p[5])
    }

    private fun normalizePlayerName(raw: String): String = raw
        .replace('\n', ' ')
        .replace('\r', ' ')
        .trim()
        .take(MAX_PLAYER_NAME_LENGTH)
        .ifBlank { "Player 1" }

    private fun escapeField(value: String): String = value.replace("%", "%25").replace("|", "%7C")
    private fun unescapeField(value: String): String = value.replace("%7C", "|").replace("%25", "%")

    private fun escapeBackupValue(value: String): String = value
        .replace("%", "%25")
        .replace("|", "%7C")
        .replace("\n", "%0A")
        .replace("\r", "%0D")

    private fun unescapeBackupValue(value: String): String = value
        .replace("%0D", "\r")
        .replace("%0A", "\n")
        .replace("%7C", "|")
        .replace("%25", "%")

    companion object {
        const val MAX_HISTORY = 30
        const val MAX_PLAYER_NAME_LENGTH = 32
        private const val MAX_HISTORY_LINE_LENGTH = 160
        private const val MAX_BACKUP_LENGTH = 128 * 1024
        private const val MAX_BACKUP_LINES = 64
        private const val BACKUP_HEADER = "RPS_ARENA_BACKUP|1"
        private const val KEY_SETTINGS_V1 = "settings_v1"
        private const val KEY_SETTINGS_V2 = "settings_v2"
        private const val KEY_MATCH_CONFIG_V1 = "match_config_v1"
        private const val KEY_STATS = "stats_v1"
        private const val KEY_HISTORY = "history_v1"
    }
}
