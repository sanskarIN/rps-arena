package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.network.InMemoryPrivateRoomGateway
import `in`.sanskar.rpsarena.network.RoomCode
import `in`.sanskar.rpsarena.network.RoomEvent
import `in`.sanskar.rpsarena.network.RoomParticipant
import `in`.sanskar.rpsarena.network.RoomRole
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
    fun independentlyParsedRoomCodesIdentifyTheSameRoom() {
        val gateway = InMemoryPrivateRoomGateway()
        gateway.host(RoomCode.require("RPS234"), "Host")

        val guest = gateway.join(RoomCode.require(" rps234 "), "Guest")

        assertNotNull(guest)
        assertEquals("RPS234", guest.code.value)
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
    fun sessionRejectsInvalidRoundAndLifecycleEvents() {
        val gateway = InMemoryPrivateRoomGateway()
        val code = RoomCode.require("SAFE23")
        val host = gateway.host(code, "Host")
        val guest = gateway.join(code, "Guest")
        assertNotNull(guest)
        host.drainEvents()

        assertFalse(
            host.send(
                RoomEvent.GestureSelected(
                    participantId = host.participant.id,
                    round = 0,
                    gesture = Gesture.PAPER,
                ),
            ),
        )
        assertFalse(
            host.send(
                RoomEvent.ParticipantJoined(
                    RoomParticipant(host.participant.id, "Fake", RoomRole.GUEST),
                ),
            ),
        )
        assertFalse(host.send(RoomEvent.ParticipantLeft(host.participant.id)))
        assertTrue(guest.drainEvents().isEmpty())
    }

    @Test
    fun closingSessionBroadcastsLifecycleEventOnce() {
        val gateway = InMemoryPrivateRoomGateway()
        val code = RoomCode.require("CLSE23")
        val host = gateway.host(code, "Host")
        val guest = gateway.join(code, "Guest")
        assertNotNull(guest)
        host.drainEvents()

        guest.close()
        guest.close()

        assertEquals(
            listOf(RoomEvent.ParticipantLeft(guest.participant.id)),
            host.drainEvents(),
        )
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
