package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode
import kotlin.test.Test
import kotlin.test.assertEquals

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
            seed = -424242,
            roundTimerSeconds = 20,
        )
        assertEquals(value, repository.decodeMatchConfig(repository.encodeMatchConfig(value)))
    }

    @Test fun statsRoundTrip() {
        val value = ArenaStats(12, 7, 3, 2, 4, 2)
        assertEquals(value, repository.decodeStats(repository.encodeStats(value)))
    }
}
