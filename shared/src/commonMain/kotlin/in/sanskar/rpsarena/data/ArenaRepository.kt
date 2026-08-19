package in.sanskar.rpsarena.data

import in.sanskar.rpsarena.model.ArenaSettings
import in.sanskar.rpsarena.model.ArenaStats
import in.sanskar.rpsarena.model.Difficulty
import in.sanskar.rpsarena.model.GameVariant
import in.sanskar.rpsarena.model.MatchConfig
import in.sanskar.rpsarena.model.MatchMode
import in.sanskar.rpsarena.model.OpponentMode

class ArenaRepository(private val store: KeyValueStore = PlatformStore) {
    fun loadSettings(): ArenaSettings = decodeSettings(store.getString(KEY_SETTINGS))
    fun saveSettings(value: ArenaSettings) = store.putString(KEY_SETTINGS, encodeSettings(value))

    fun loadStats(): ArenaStats = decodeStats(store.getString(KEY_STATS))
    fun saveStats(value: ArenaStats) = store.putString(KEY_STATS, encodeStats(value))

    fun loadConfig(): MatchConfig = decodeConfig(store.getString(KEY_CONFIG))
    fun saveConfig(value: MatchConfig) = store.putString(KEY_CONFIG, encodeConfig(value))

    fun loadHistory(): List<String> = store.getString(KEY_HISTORY)
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(MAX_HISTORY)
        .toList()

    fun addHistory(line: String) {
        val updated = (listOf(line.replace('\n', ' ')) + loadHistory()).take(MAX_HISTORY)
        store.putString(KEY_HISTORY, updated.joinToString("\n"))
    }

    fun clearHistory() {
        store.putString(KEY_HISTORY, "")
    }

    fun resetAll() {
        saveSettings(ArenaSettings())
        saveStats(ArenaStats())
        saveConfig(MatchConfig())
        clearHistory()
    }

    fun exportBackup(): String = buildString {
        appendLine(BACKUP_HEADER)
        appendLine("settings=${encodeSettings(loadSettings())}")
        appendLine("stats=${encodeStats(loadStats())}")
        appendLine("config=${encodeConfig(loadConfig())}")
        append("history=${loadHistory().joinToString("\t") { escapeHistory(it) }}")
    }

    fun importBackup(raw: String): Boolean {
        val lines = raw.replace("\r\n", "\n").trim().split('\n')
        if (lines.firstOrNull() != BACKUP_HEADER) return false

        val values = lines.drop(1).mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
        }.toMap()

        val settings = decodeSettingsOrNull(values["settings"].orEmpty()) ?: return false
        val stats = decodeStatsOrNull(values["stats"].orEmpty()) ?: return false
        val config = decodeConfigOrNull(values["config"].orEmpty()) ?: return false
        val historyRaw = values["history"] ?: return false
        val history = if (historyRaw.isBlank()) {
            emptyList()
        } else {
            historyRaw.split('\t').map { unescapeHistory(it) ?: return false }
        }
        if (history.size > MAX_HISTORY || history.any { it.length > MAX_HISTORY_LINE_LENGTH }) return false

        saveSettings(settings)
        saveStats(stats)
        saveConfig(config)
        store.putString(KEY_HISTORY, history.joinToString("\n"))
        return true
    }

    internal fun encodeSettings(value: ArenaSettings): String = listOf(
        value.darkTheme,
        value.followSystemTheme,
        value.reducedMotion,
        value.onboardingComplete,
    ).joinToString("|")

    internal fun decodeSettings(raw: String): ArenaSettings = decodeSettingsOrNull(raw) ?: ArenaSettings()

    private fun decodeSettingsOrNull(raw: String): ArenaSettings? {
        val parts = raw.split('|')
        return when (parts.size) {
            4 -> {
                val values = parts.map { it.toBooleanStrictOrNull() ?: return null }
                ArenaSettings(
                    darkTheme = values[0],
                    followSystemTheme = values[1],
                    reducedMotion = values[2],
                    onboardingComplete = values[3],
                )
            }
            7 -> {
                val legacy = parts.map { it.toBooleanStrictOrNull() ?: return null }
                ArenaSettings(
                    darkTheme = legacy[0],
                    followSystemTheme = legacy[1],
                    reducedMotion = legacy[2],
                    onboardingComplete = legacy[6],
                )
            }
            else -> null
        }
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

    private fun decodeStatsOrNull(raw: String): ArenaStats? {
        val p = raw.split('|').map { it.toIntOrNull() ?: return null }
        if (p.size != 6 || p.any { it < 0 }) return null
        if (p[0] != p[1] + p[2] + p[3]) return null
        if (p[5] > p[4]) return null
        return ArenaStats(p[0], p[1], p[2], p[3], p[4], p[5])
    }

    internal fun encodeConfig(value: MatchConfig): String = listOf(
        value.variant.name,
        value.opponentMode.name,
        value.difficulty.name,
        value.matchMode.name,
        value.seed,
        value.roundTimerSeconds,
    ).joinToString("|")

    internal fun decodeConfig(raw: String): MatchConfig = decodeConfigOrNull(raw) ?: MatchConfig()

    private fun decodeConfigOrNull(raw: String): MatchConfig? {
        val p = raw.split('|')
        if (p.size != 6) return null
        return runCatching {
            val timer = p[5].toInt()
            require(timer in 0..60)
            MatchConfig(
                variant = GameVariant.valueOf(p[0]),
                opponentMode = OpponentMode.valueOf(p[1]),
                difficulty = Difficulty.valueOf(p[2]),
                matchMode = MatchMode.valueOf(p[3]),
                seed = p[4].toInt(),
                roundTimerSeconds = timer,
            )
        }.getOrNull()
    }

    private fun escapeHistory(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\t", "\\t")

    private fun unescapeHistory(value: String): String? {
        val result = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val character = value[index]
            if (character != '\\') {
                result.append(character)
                index += 1
                continue
            }
            if (index + 1 >= value.length) return null
            when (value[index + 1]) {
                '\\' -> result.append('\\')
                't' -> result.append('\t')
                else -> return null
            }
            index += 2
        }
        return result.toString()
    }

    companion object {
        const val MAX_HISTORY = 30
        private const val MAX_HISTORY_LINE_LENGTH = 240
        private const val BACKUP_HEADER = "RPS_ARENA_BACKUP_V1"
        private const val KEY_SETTINGS = "settings_v1"
        private const val KEY_STATS = "stats_v1"
        private const val KEY_CONFIG = "config_v1"
        private const val KEY_HISTORY = "history_v1"
    }
}
