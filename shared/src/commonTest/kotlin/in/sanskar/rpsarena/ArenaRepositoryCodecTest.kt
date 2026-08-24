package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.ArenaStore
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArenaRepositoryCodecTest {
    private val repository = ArenaRepository()

    @Test fun settingsRoundTrip() {
        val value = ArenaSettings(true, false, true, false, true, true, true)
        assertEquals(value, repository.decodeSettings(repository.encodeSettings(value)))
    }

    @Test fun matchConfigRoundTrip() {
        val value = MatchConfig(
            variant = GameVariant.LIZARD_SPOCK,
            opponentMode = OpponentMode.LOCAL_TWO_PLAYER,
            difficulty = Difficulty.EXPERT,
            matchMode = MatchMode.TOURNAMENT,
            seed = 8421,
        )
        assertEquals(value, repository.decodeMatchConfig(repository.encodeMatchConfig(value)))
    }

    @Test fun malformedMatchConfigFallsBackToDefaults() {
        assertEquals(MatchConfig(), repository.decodeMatchConfig("invalid|config"))
    }

    @Test fun statsRoundTrip() {
        val value = ArenaStats(12, 7, 3, 2, 4, 2)
        assertEquals(value, repository.decodeStats(repository.encodeStats(value)))
    }

    @Test fun invalidStatsFallBackToDefaults() {
        assertEquals(ArenaStats(), repository.decodeStats("4|2|1|0|2|1"))
        assertEquals(ArenaStats(), repository.decodeStats("4|2|1|1|3|1"))
        assertEquals(ArenaStats(), repository.decodeStats("4|2|-1|3|1|0"))
        assertEquals(ArenaStats(), repository.decodeStats("not|numbers|at|all|0|0"))
    }

    @Test fun historyWritesAreSanitizedAndBounded() {
        val store = MemoryStore()
        val storedRepository = ArenaRepository(store)
        val oversized = "  first\r\nsecond " + "x".repeat(300)

        storedRepository.addHistory(oversized)
        storedRepository.addHistory("  ")

        val history = storedRepository.loadHistory()
        assertEquals(1, history.size)
        assertFalse(history.single().contains('\r'))
        assertFalse(history.single().contains('\n'))
        assertTrue(history.single().length <= ArenaRepository.MAX_HISTORY_LINE_LENGTH)
    }

    private class MemoryStore : ArenaStore {
        private val values = mutableMapOf<String, String>()

        override fun getString(key: String, defaultValue: String): String =
            values[key] ?: defaultValue

        override fun putString(key: String, value: String) {
            values[key] = value
        }
    }
}
