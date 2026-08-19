package dev.sanskar.rpsarena.data

import dev.sanskar.rpsarena.domain.AppSettings
import dev.sanskar.rpsarena.domain.Difficulty
import dev.sanskar.rpsarena.domain.GameMode
import dev.sanskar.rpsarena.domain.GameStats
import dev.sanskar.rpsarena.domain.GameVariant
import dev.sanskar.rpsarena.domain.Gesture
import dev.sanskar.rpsarena.domain.OpponentMode
import dev.sanskar.rpsarena.domain.RoundOutcome
import dev.sanskar.rpsarena.domain.RoundRecord
import dev.sanskar.rpsarena.domain.ThemePreference

interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun clear()
}

class AppRepository(private val store: KeyValueStore) {
    fun loadSettings(): AppSettings = runCatching {
        val parts = store.getString(KEY_SETTINGS)?.split('|') ?: return@runCatching AppSettings()
        AppSettings(
            theme = enumValueOf(parts.getOrNull(0) ?: ThemePreference.SYSTEM.name),
            reducedMotion = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: false,
            roundTimerSeconds = parts.getOrNull(2)?.toIntOrNull()?.coerceIn(0, 60) ?: 0,
            variant = enumValueOf(parts.getOrNull(3) ?: GameVariant.CLASSIC.name),
            difficulty = enumValueOf(parts.getOrNull(4) ?: Difficulty.NORMAL.name),
            gameMode = enumValueOf(parts.getOrNull(5) ?: GameMode.BEST_OF_3.name),
            opponentMode = enumValueOf(parts.getOrNull(6) ?: OpponentMode.CPU.name),
            seed = parts.getOrNull(7)?.toLongOrNull() ?: 20260819L,
        )
    }.getOrDefault(AppSettings())

    fun saveSettings(settings: AppSettings) {
        store.putString(
            KEY_SETTINGS,
            listOf(
                settings.theme.name,
                settings.reducedMotion,
                settings.roundTimerSeconds,
                settings.variant.name,
                settings.difficulty.name,
                settings.gameMode.name,
                settings.opponentMode.name,
                settings.seed,
            ).joinToString("|"),
        )
    }

    fun loadStats(): GameStats = runCatching {
        val parts = store.getString(KEY_STATS)?.split('|') ?: return@runCatching GameStats()
        GameStats(
            wins = parts.getOrNull(0)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            losses = parts.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            draws = parts.getOrNull(2)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            currentStreak = parts.getOrNull(3)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            bestStreak = parts.getOrNull(4)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
        )
    }.getOrDefault(GameStats())

    fun saveStats(stats: GameStats) {
        store.putString(
            KEY_STATS,
            listOf(stats.wins, stats.losses, stats.draws, stats.currentStreak, stats.bestStreak)
                .joinToString("|"),
        )
    }

    fun loadHistory(): List<RoundRecord> = runCatching {
        val raw = store.getString(KEY_HISTORY).orEmpty()
        if (raw.isBlank()) return@runCatching emptyList()
        raw.split(';').mapNotNull(::decodeRound).takeLast(MAX_HISTORY)
    }.getOrDefault(emptyList())

    fun saveHistory(history: List<RoundRecord>) {
        store.putString(KEY_HISTORY, history.takeLast(MAX_HISTORY).joinToString(";") { encodeRound(it) })
    }

    fun exportBackup(): String = buildString {
        appendLine("RPS_ARENA_BACKUP_V1")
        appendLine("settings=${store.getString(KEY_SETTINGS).orEmpty()}")
        appendLine("stats=${store.getString(KEY_STATS).orEmpty()}")
        append("history=${store.getString(KEY_HISTORY).orEmpty()}")
    }

    fun importBackup(raw: String): Boolean {
        val lines = raw.lineSequence().toList()
        if (lines.firstOrNull() != "RPS_ARENA_BACKUP_V1") return false
        val values = lines.drop(1).mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        val settings = values["settings"] ?: return false
        val stats = values["stats"] ?: return false
        val history = values["history"] ?: return false

        store.putString(KEY_SETTINGS, settings)
        store.putString(KEY_STATS, stats)
        store.putString(KEY_HISTORY, history)
        return true
    }

    fun resetAll() = store.clear()

    private fun encodeRound(round: RoundRecord): String = listOf(
        round.roundNumber,
        round.playerOne.name,
        round.playerTwo.name,
        round.outcome.name,
    ).joinToString(",")

    private fun decodeRound(raw: String): RoundRecord? = runCatching {
        val parts = raw.split(',')
        RoundRecord(
            roundNumber = parts[0].toInt().coerceAtLeast(1),
            playerOne = Gesture.valueOf(parts[1]),
            playerTwo = Gesture.valueOf(parts[2]),
            outcome = RoundOutcome.valueOf(parts[3]),
        )
    }.getOrNull()

    private companion object {
        const val KEY_SETTINGS = "settings.v1"
        const val KEY_STATS = "stats.v1"
        const val KEY_HISTORY = "history.v1"
        const val MAX_HISTORY = 30
    }
}
