package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.network.PrivateRoomProtocol
import `in`.sanskar.rpsarena.network.ProtocolValidation
import `in`.sanskar.rpsarena.network.RoomCommand
import `in`.sanskar.rpsarena.network.RoomEnvelope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PrivateRoomProtocolTest {
    @Test
    fun roomCodeRejectsAmbiguousAndMalformedInput() {
        assertTrue(PrivateRoomProtocol.isValidRoomCode("ABC234"))
        assertEquals(false, PrivateRoomProtocol.isValidRoomCode("ABC23O"))
        assertEquals(false, PrivateRoomProtocol.isValidRoomCode("abc234"))
        assertEquals(false, PrivateRoomProtocol.isValidRoomCode("ABC23"))
    }

    @Test
    fun validMoveEnvelopeIsAccepted() {
        val envelope = RoomEnvelope(
            roomCode = "ABC234",
            messageId = "message-1",
            round = 1,
            command = RoomCommand.Move(GameVariant.CLASSIC, Gesture.ROCK),
        )

        assertIs<ProtocolValidation.Accepted>(PrivateRoomProtocol.validate(envelope))
    }

    @Test
    fun classicRoomRejectsExtendedGesture() {
        val envelope = RoomEnvelope(
            roomCode = "ABC234",
            messageId = "message-2",
            round = 2,
            command = RoomCommand.Move(GameVariant.CLASSIC, Gesture.SPOCK),
        )

        assertIs<ProtocolValidation.Rejected>(PrivateRoomProtocol.validate(envelope))
    }

    @Test
    fun protocolRejectsUnsupportedVersionAndUnboundedRound() {
        val unsupported = RoomEnvelope(
            protocolVersion = 99,
            roomCode = "ABC234",
            messageId = "message-3",
            round = 1,
            command = RoomCommand.Ready,
        )
        val tooLarge = RoomEnvelope(
            roomCode = "ABC234",
            messageId = "message-4",
            round = PrivateRoomProtocol.MAX_ROUND + 1,
            command = RoomCommand.Ready,
        )

        assertIs<ProtocolValidation.Rejected>(PrivateRoomProtocol.validate(unsupported))
        assertIs<ProtocolValidation.Rejected>(PrivateRoomProtocol.validate(tooLarge))
    }
}
