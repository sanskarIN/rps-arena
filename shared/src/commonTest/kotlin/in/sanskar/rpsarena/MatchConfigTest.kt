package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MatchConfigTest {
    @Test
    fun allowedRoundTimersAreAccepted() {
        MatchConfig.ALLOWED_TIMER_SECONDS.forEach { seconds ->
            assertEquals(seconds, MatchConfig(roundTimerSeconds = seconds).roundTimerSeconds)
        }
    }

    @Test
    fun unsupportedRoundTimerIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            MatchConfig(roundTimerSeconds = 15)
        }
    }

    @Test
    fun finiteModesExposeExpectedWinTargets() {
        assertEquals(2, MatchConfig(matchMode = MatchMode.BEST_OF_3).roundsToWin)
        assertEquals(3, MatchConfig(matchMode = MatchMode.BEST_OF_5).roundsToWin)
        assertEquals(5, MatchConfig(matchMode = MatchMode.TOURNAMENT).roundsToWin)
        assertNull(MatchConfig(matchMode = MatchMode.ENDLESS).roundsToWin)
        assertNull(MatchConfig(matchMode = MatchMode.STREAK).roundsToWin)
    }
}
