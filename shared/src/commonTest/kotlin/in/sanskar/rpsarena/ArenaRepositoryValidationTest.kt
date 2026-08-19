package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ArenaRepositoryValidationTest {
    @Test
    fun invalidStatsInvariantFallsBackToDefaults() {
        val repository = repository(mutableMapOf())

        assertEquals(ArenaStats(), repository.decodeStats("2|2|2|0|1|1"))
        assertEquals(ArenaStats(), repository.decodeStats("2|1|0|1|2|0"))
        assertEquals(ArenaStats(), repository.decodeStats("2|1|0|1|1|2"))
        assertEquals(ArenaStats(), repository.decodeStats("-1|0|0|0|0|0"))
    }

    @Test
    fun playerNameIsSanitizedAndBoundedBeforeStorage() {
        val values = mutableMapOf<String, String>()
        val repository = repository(values)
        val longName = "  Sanskar\n" + "x".repeat(100)

        repository.saveSettings(ArenaSettings(playerName = longName))

        val stored = repository.loadSettings().playerName
        assertFalse(stored.contains('\n'))
        assertEquals(ArenaRepository.MAX_PLAYER_NAME_LENGTH, stored.length)
    }

    @Test
    fun blankPlayerNameFallsBackToDefault() {
        val repository = repository(mutableMapOf())

        repository.saveSettings(ArenaSettings(playerName = " \n \r "))

        assertEquals("Player 1", repository.loadSettings().playerName)
    }

    @Test
    fun unknownBackupRecordIsRejectedBeforeWrites() {
        val values = mutableMapOf<String, String>()
        val repository = repository(values)
        val original = ArenaStats(1, 1, 0, 0, 1, 1)
        repository.saveStats(original)
        val settings = repository.encodeSettings(ArenaSettings())
        val stats = repository.encodeStats(ArenaStats())

        val result = repository.importBackup(
            "RPS_ARENA_BACKUP|1\nsettings|$settings\nstats|$stats\nunknown|value",
        )

        assertFalse(result.imported)
        assertEquals(original, repository.loadStats())
    }

    @Test
    fun historyStripsLineBreaksAndKeepsLatestThirtyRecords() {
        val repository = repository(mutableMapOf())
        repeat(35) { index -> repository.addHistory("round-$index\ncontinued") }

        val history = repository.loadHistory()

        assertEquals(ArenaRepository.MAX_HISTORY, history.size)
        assertEquals("round-34 continued", history.first())
        assertEquals("round-5 continued", history.last())
    }

    private fun repository(values: MutableMap<String, String>) = ArenaRepository(
        readString = { key, defaultValue -> values[key] ?: defaultValue },
        writeString = { key, value -> values[key] = value },
    )
}
