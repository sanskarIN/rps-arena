package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.network.InMemoryPrivateRoomGateway
import `in`.sanskar.rpsarena.network.RoomCode
import `in`.sanskar.rpsarena.network.RoomEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrivateRoomTest {
    @Test
    fun roomCodeRejectsAmbiguousAndMalformedValues() {
        assertNotNull(RoomCode.parse("ABC234"))
        assertEquals("ABC234", RoomCode.parse(" abc234 ")?.value)
        assertNull(RoomCode.parse("ABCI23"))
        assertNull(RoomCode.parse("SHORT"))
    }

    @Test
    fun hostAndGuestExchangeValidatedEvents() {
        val gateway = InMemoryPrivateRoomGateway()
        val code = RoomCode.require("RPS234")
        val host = gateway.host(code, "Host")
        val guest = gateway.join(code, "Guest")
        assertNotNull(guest)

        val joinEvent = host.drainEvents().single() as RoomEvent.ParticipantJoined
        assertEquals(guest.participant.id, joinEvent.participant.id)

        val gesture = RoomEvent.GestureSelected(
            participantId = host.participant.id,
            round = 1,
            gesture = Gesture.ROCK,
        )
        assertTrue(host.send(gesture))
        assertEquals(listOf(gesture), guest.drainEvents())

        val forged = RoomEvent.RestartRequested(participantId = "someone-else")
        assertFalse(host.send(forged))
    }

    @Test
    fun privateRoomAllowsOnlyTwoParticipants() {
        val gateway = InMemoryPrivateRoomGateway()
        val code = RoomCode.require("RM2345")
        gateway.host(code, "Host")
        assertNotNull(gateway.join(code, "Guest"))
        assertNull(gateway.join(code, "Third"))
    }
}
