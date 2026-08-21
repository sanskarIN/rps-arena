package `in`.sanskar.rpsarena.data

import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats

data class ArenaBackup(
    val schemaVersion: Int = ArenaBackupCodec.CURRENT_SCHEMA_VERSION,
    val settings: ArenaSettings = ArenaSettings(),
    val stats: ArenaStats = ArenaStats(),
    val history: List<String> = emptyList(),
)

enum class ArenaBackupError {
    EMPTY,
    INVALID_HEADER,
    UNSUPPORTED_SCHEMA,
    MALFORMED_SETTINGS,
    MALFORMED_STATS,
    MALFORMED_HISTORY,
}

sealed interface ArenaBackupDecodeResult {
    data class Success(val backup: ArenaBackup) : ArenaBackupDecodeResult
    data class Failure(val error: ArenaBackupError) : ArenaBackupDecodeResult
}

sealed interface ArenaBackupImportResult {
    data class Success(val importedHistoryCount: Int) : ArenaBackupImportResult
    data class Failure(val error: ArenaBackupError) : ArenaBackupImportResult
}

object ArenaBackupCodec {
    const val CURRENT_SCHEMA_VERSION = 1
    const val MAX_HISTORY_ITEMS = 30

    private const val MAGIC = "RPSARENA_BACKUP"
    private const val SETTINGS = "settings"
    private const val STATS = "stats"
    private const val HISTORY = "history"
    private const val ITEM = "item"

    fun encode(backup: ArenaBackup): String {
        require(backup.schemaVersion == CURRENT_SCHEMA_VERSION) {
            "Only schema version $CURRENT_SCHEMA_VERSION can be encoded"
        }

        val history = backup.history
            .asSequence()
            .map(::sanitizeHistoryItem)
            .filter { it.isNotEmpty() }
            .take(MAX_HISTORY_ITEMS)
            .toList()

        return buildString {
            append(MAGIC).append('|').append(CURRENT_SCHEMA_VERSION).append('\n')
            append(SETTINGS).append('|').append(encodeSettings(backup.settings)).append('\n')
            append(STATS).append('|').append(encodeStats(backup.stats)).append('\n')
            append(HISTORY).append('|').append(history.size)
            history.forEach { item ->
                append('\n').append(ITEM).append('|').append(item)
            }
        }
    }

    fun decode(raw: String): ArenaBackupDecodeResult {
        val normalized = raw.trimEnd('\r', '\n')
        if (normalized.isBlank()) {
            return ArenaBackupDecodeResult.Failure(ArenaBackupError.EMPTY)
        }

        val lines = normalized.lines().map { it.removeSuffix("\r") }
        if (lines.size < 4) {
            return ArenaBackupDecodeResult.Failure(ArenaBackupError.INVALID_HEADER)
        }

        val header = lines[0].split('|')
        if (header.size != 2 || header[0] != MAGIC) {
            return ArenaBackupDecodeResult.Failure(ArenaBackupError.INVALID_HEADER)
        }
        val version = header[1].toIntOrNull()
            ?: return ArenaBackupDecodeResult.Failure(ArenaBackupError.INVALID_HEADER)
        if (version != CURRENT_SCHEMA_VERSION) {
            return ArenaBackupDecodeResult.Failure(ArenaBackupError.UNSUPPORTED_SCHEMA)
        }

        val settings = decodeSettingsLine(lines[1])
            ?: return ArenaBackupDecodeResult.Failure(ArenaBackupError.MALFORMED_SETTINGS)
        val stats = decodeStatsLine(lines[2])
            ?: return ArenaBackupDecodeResult.Failure(ArenaBackupError.MALFORMED_STATS)
        val history = decodeHistory(lines)
            ?: return ArenaBackupDecodeResult.Failure(ArenaBackupError.MALFORMED_HISTORY)

        return ArenaBackupDecodeResult.Success(
            ArenaBackup(
                schemaVersion = version,
                settings = settings,
                stats = stats,
                history = history,
            ),
        )
    }

    private fun encodeSettings(value: ArenaSettings): String = listOf(
        value.darkTheme,
        value.followSystemTheme,
        value.reducedMotion,
        value.soundEnabled,
        value.hapticsEnabled,
        value.extendedVariant,
        value.onboardingComplete,
    ).joinToString("|")

    private fun decodeSettingsLine(line: String): ArenaSettings? {
        val parts = line.split('|')
        if (parts.size != 8 || parts[0] != SETTINGS) return null
        return ArenaSettings(
            darkTheme = strictBoolean(parts[1]) ?: return null,
            followSystemTheme = strictBoolean(parts[2]) ?: return null,
            reducedMotion = strictBoolean(parts[3]) ?: return null,
            soundEnabled = strictBoolean(parts[4]) ?: return null,
            hapticsEnabled = strictBoolean(parts[5]) ?: return null,
            extendedVariant = strictBoolean(parts[6]) ?: return null,
            onboardingComplete = strictBoolean(parts[7]) ?: return null,
        )
    }

    private fun encodeStats(value: ArenaStats): String = listOf(
        value.roundsPlayed,
        value.wins,
        value.losses,
        value.draws,
        value.bestStreak,
        value.currentStreak,
    ).joinToString("|")

    private fun decodeStatsLine(line: String): ArenaStats? {
        val parts = line.split('|')
        if (parts.size != 7 || parts[0] != STATS) return null
        val values = parts.drop(1).mapNotNull { it.toIntOrNull() }
        if (values.size != 6 || values.any { it < 0 }) return null

        val stats = ArenaStats(
            roundsPlayed = values[0],
            wins = values[1],
            losses = values[2],
            draws = values[3],
            bestStreak = values[4],
            currentStreak = values[5],
        )
        if (stats.roundsPlayed != stats.wins + stats.losses + stats.draws) return null
        if (stats.currentStreak > stats.bestStreak || stats.bestStreak > stats.wins) return null
        return stats
    }

    private fun decodeHistory(lines: List<String>): List<String>? {
        val header = lines[3].split('|')
        if (header.size != 2 || header[0] != HISTORY) return null
        val count = header[1].toIntOrNull() ?: return null
        if (count !in 0..MAX_HISTORY_ITEMS || lines.size != 4 + count) return null

        return lines.drop(4).map { line ->
            if (!line.startsWith("$ITEM|")) return null
            val value = line.removePrefix("$ITEM|")
            if (value.isBlank() || value.contains('\n') || value.contains('\r')) return null
            value
        }
    }

    private fun strictBoolean(value: String): Boolean? = when (value) {
        "true" -> true
        "false" -> false
        else -> null
    }

    private fun sanitizeHistoryItem(value: String): String = value
        .replace('\r', ' ')
        .replace('\n', ' ')
        .trim()
}
