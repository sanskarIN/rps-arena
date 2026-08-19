package dev.sanskar.rpsarena.data

import dev.sanskar.rpsarena.domain.AppSettings
import dev.sanskar.rpsarena.domain.Difficulty
import dev.sanskar.rpsarena.domain.GameMode
import dev.sanskar.rpsarena.domain.GameStats
import dev.sanskar.rpsarena.domain.GameVariant
import dev.sanskar.rpsarena.domain.Gesture
import dev.sanskar.rpsarena.domain.RoundOutcome
import dev.sanskar.rpsarena.domain.RoundRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistenceTest {
    @Test
    fun settingsStatsAndHistoryRoundTrip() {
        val store = MemoryStore()
        val repository = AppRepository(store)
        val settings = AppSettings(
            variant = GameVariant.LIZARD_SPOCK,
            difficulty = Difficulty.EXPERT,
            gameMode = GameMode.ENDLESS,
            seed = 1234L,
        )
        val stats = GameStats(wins = 5, losses = 2, draws = 1, currentStreak = 2, bestStreak = 4)
        val history = listOf(RoundRecord(1, Gesture.SPOCK, Gesture.ROCK, RoundOutcome.PLAYER_ONE_WIN))

        repository.saveSettings(settings)
        repository.saveStats(stats)
        repository.saveHistory(history)

        assertEquals(settings, repository.loadSettings())
        assertEquals(stats, repository.loadStats())
        assertEquals(history, repository.loadHistory())
    }

    @Test
    fun backupCanMoveDataBetweenStores() {
        val source = AppRepository(MemoryStore())
        source.saveStats(GameStats(wins = 8, losses = 1))
        val backup = source.exportBackup()

        val target = AppRepository(MemoryStore())
        assertTrue(target.importBackup(backup))
        assertEquals(8, target.loadStats().wins)
        assertFalse(target.importBackup("not-a-backup"))
    }

    @Test
    fun malformedValuesFallBackSafely() {
        val store = MemoryStore().apply { putString("settings.v1", "broken|data") }
        val repository = AppRepository(store)
        assertEquals(AppSettings(), repository.loadSettings())
    }
}

private class MemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
    override fun clear() { values.clear() }
}
