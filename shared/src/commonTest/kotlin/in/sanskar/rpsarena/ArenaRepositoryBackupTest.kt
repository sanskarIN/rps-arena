package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.AppLanguage
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArenaRepositoryBackupTest {
    @Test
    fun backupRoundTripPreservesSettingsStatsAndHistory() {
        val sourceValues = mutableMapOf<String, String>()
        val source = repository(sourceValues)
        val settings = ArenaSettings(
            darkTheme = true,
            followSystemTheme = false,
            reducedMotion = true,
            soundEnabled = false,
            hapticsEnabled = false,
            extendedVariant = true,
            onboardingComplete = true,
            playerName = "Sanskar | Player",
            language = AppLanguage.HINDI,
        )
        val stats = ArenaStats(8, 5, 2, 1, 3, 2)
        source.saveSettings(settings)
        source.saveStats(stats)
        source.addHistory("Rock vs Scissors — Player 1 won")
        source.addHistory("Paper vs Paper — Draw")

        val targetValues = mutableMapOf<String, String>()
        val target = repository(targetValues)
        val result = target.importBackup(source.exportBackup())

        assertTrue(result.imported)
        assertEquals(settings, target.loadSettings())
        assertEquals(stats, target.loadStats())
        assertEquals(source.loadHistory(), target.loadHistory())
    }

    @Test
    fun malformedBackupDoesNotOverwriteExistingData() {
        val values = mutableMapOf<String, String>()
        val repository = repository(values)
        val originalStats = ArenaStats(2, 1, 1, 0, 1, 0)
        repository.saveStats(originalStats)

        val result = repository.importBackup("RPS_ARENA_BACKUP|1\nstats|broken")

        assertFalse(result.imported)
        assertEquals(originalStats, repository.loadStats())
    }

    @Test
    fun legacySettingsMigrateIntoVersionTwoStorage() {
        val values = mutableMapOf(
            "settings_v1" to "true|false|true|false|true|true|true",
        )
        val repository = repository(values)

        val migrated = repository.loadSettings()

        assertTrue(migrated.darkTheme)
        assertTrue(migrated.onboardingComplete)
        assertEquals("Player 1", migrated.playerName)
        assertTrue(values.containsKey("settings_v2"))
    }

    @Test
    fun recentTrendUsesNewestHistoryOnly() {
        val repository = repository(mutableMapOf())
        repository.addHistory("Rock vs Scissors — Player 1 won")
        repository.addHistory("Rock vs Paper — Player 2 won")
        repository.addHistory("Rock vs Rock — Draw")
        repository.addHistory("Paper vs Rock — Player 1 won")

        val trend = repository.loadRecentTrend(limit = 3)

        assertEquals(1, trend.wins)
        assertEquals(1, trend.losses)
        assertEquals(1, trend.draws)
    }

    private fun repository(values: MutableMap<String, String>) = ArenaRepository(
        readString = { key, defaultValue -> values[key] ?: defaultValue },
        writeString = { key, value -> values[key] = value },
    )
}
