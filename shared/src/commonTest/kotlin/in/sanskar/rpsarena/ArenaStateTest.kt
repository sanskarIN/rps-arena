package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.OpponentMode
import `in`.sanskar.rpsarena.model.RoundEndReason
import `in`.sanskar.rpsarena.model.RoundOutcome
import `in`.sanskar.rpsarena.state.ArenaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArenaStateTest {
    @Test
    fun cpuTimeoutAwardsRoundToOpponent() {
        val state = state()
        state.updateConfig(MatchConfig(roundTimerSeconds = 5))

        state.expireCurrentTurn()

        val round = state.match.rounds.single()
        assertEquals(RoundEndReason.PLAYER_ONE_TIMEOUT, round.endReason)
        assertEquals(RoundOutcome.PLAYER_TWO_WIN, round.outcome)
        assertEquals(1, state.stats.losses)
    }

    @Test
    fun localSecondPlayerTimeoutAwardsRoundToPlayerOne() {
        val state = state()
        state.updateConfig(
            MatchConfig(
                opponentMode = OpponentMode.LOCAL_TWO_PLAYER,
                roundTimerSeconds = 5,
            ),
        )
        state.play(Gesture.PAPER)
        assertEquals(Gesture.PAPER, state.pendingPlayerOne)

        state.expireCurrentTurn()

        val round = state.match.rounds.single()
        assertEquals(Gesture.PAPER, round.playerOne)
        assertNull(round.playerTwo)
        assertEquals(RoundEndReason.PLAYER_TWO_TIMEOUT, round.endReason)
        assertEquals(RoundOutcome.PLAYER_ONE_WIN, round.outcome)
        assertEquals(1, state.stats.wins)
        assertNull(state.pendingPlayerOne)
    }

    @Test
    fun timerDisabledDoesNotChangeMatch() {
        val state = state()

        state.expireCurrentTurn()

        assertTrue(state.match.rounds.isEmpty())
        assertEquals(0, state.stats.roundsPlayed)
    }

    @Test
    fun backupImportRefreshesInMemoryState() {
        val values = mutableMapOf<String, String>()
        val repository = repository(values)
        val state = ArenaState(repository)
        state.updateConfig(MatchConfig(roundTimerSeconds = 5))
        state.expireCurrentTurn()
        state.prepareBackup()
        val exported = state.backupText

        state.clearUserData()
        assertEquals(0, state.stats.roundsPlayed)
        state.updateBackupText(exported)
        state.importBackup()

        assertEquals(1, state.stats.roundsPlayed)
        assertFalse(state.dataMessage.isNullOrBlank())
    }

    private fun state(): ArenaState = ArenaState(repository(mutableMapOf()))

    private fun repository(values: MutableMap<String, String>) = ArenaRepository(
        readString = { key, defaultValue -> values[key] ?: defaultValue },
        writeString = { key, value -> values[key] = value },
    )
}
