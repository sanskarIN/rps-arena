package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import kotlin.test.Test
import kotlin.test.assertEquals

class ArenaRepositoryCodecTest {
    private val repository = ArenaRepository()

    @Test fun settingsRoundTrip() {
        val value = ArenaSettings(true, false, true, false, true, true, true)
        assertEquals(value, repository.decodeSettings(repository.encodeSettings(value)))
    }

    @Test fun statsRoundTrip() {
        val value = ArenaStats(12, 7, 3, 2, 4, 2)
        assertEquals(value, repository.decodeStats(repository.encodeStats(value)))
    }
}
