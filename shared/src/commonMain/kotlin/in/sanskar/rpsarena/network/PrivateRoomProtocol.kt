package `in`.sanskar.rpsarena.network

import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.Gesture

/**
 * Pure protocol boundary for a future opt-in LAN/private-room transport.
 *
 * Core gameplay does not depend on this API and the Android app requests no network permission.
 * A future transport must treat every decoded envelope as untrusted input and must not accept a
 * peer-provided match result as authoritative; both peers resolve outcomes with the local rules engine.
 */
object PrivateRoomProtocol {
    const val VERSION = 1
    const val ROOM_CODE_LENGTH = 6
    const val MAX_MESSAGE_ID_LENGTH = 36
    const val MAX_ROUND = 10_000

    private val roomCodeCharacters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toSet()

    fun isValidRoomCode(value: String): Boolean =
        value.length == ROOM_CODE_LENGTH && value.all { it in roomCodeCharacters }

    fun validate(envelope: RoomEnvelope): ProtocolValidation = when {
        envelope.protocolVersion != VERSION -> ProtocolValidation.Rejected("unsupported protocol version")
        !isValidRoomCode(envelope.roomCode) -> ProtocolValidation.Rejected("invalid room code")
        envelope.messageId.isBlank() || envelope.messageId.length > MAX_MESSAGE_ID_LENGTH ->
            ProtocolValidation.Rejected("invalid message id")
        envelope.round !in 0..MAX_ROUND -> ProtocolValidation.Rejected("round out of range")
        envelope.command is RoomCommand.Move && envelope.round == 0 ->
            ProtocolValidation.Rejected("move requires a positive round")
        envelope.command is RoomCommand.Move &&
            envelope.command.gesture !in Gesture.availableFor(envelope.command.variant) ->
            ProtocolValidation.Rejected("gesture is not valid for variant")
        else -> ProtocolValidation.Accepted
    }
}

data class RoomEnvelope(
    val protocolVersion: Int = PrivateRoomProtocol.VERSION,
    val roomCode: String,
    val messageId: String,
    val round: Int,
    val command: RoomCommand,
)

sealed interface RoomCommand {
    data class Hello(val variant: GameVariant) : RoomCommand
    data object Ready : RoomCommand
    data class Move(val variant: GameVariant, val gesture: Gesture) : RoomCommand
    data object Leave : RoomCommand
}

sealed interface ProtocolValidation {
    data object Accepted : ProtocolValidation
    data class Rejected(val reason: String) : ProtocolValidation
}

/**
 * Transport contract only. No production transport is provided in v1.
 *
 * Implementations must be explicitly opt-in, bounded, cancellable, and scoped to the local/private
 * session. They must not introduce a mandatory hosted service for CPU or pass-and-play modes.
 */
interface PrivateRoomTransport {
    suspend fun host(roomCode: String): Result<Unit>
    suspend fun join(roomCode: String): Result<Unit>
    suspend fun send(envelope: RoomEnvelope): Result<Unit>
    suspend fun receive(): Result<RoomEnvelope>
    suspend fun close()
}
