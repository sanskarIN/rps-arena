package `in`.sanskar.rpsarena.network

import `in`.sanskar.rpsarena.model.Gesture

data class RoomCode private constructor(val value: String) {
    companion object {
        private val allowed = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toSet()

        fun parse(raw: String): RoomCode? {
            val normalized = raw.trim().uppercase()
            if (normalized.length != 6 || normalized.any { it !in allowed }) return null
            return RoomCode(normalized)
        }

        fun require(raw: String): RoomCode =
            parse(raw) ?: throw IllegalArgumentException("Room code must be 6 unambiguous uppercase letters or digits")
    }
}

enum class RoomRole { HOST, GUEST }

data class RoomParticipant(
    val id: String,
    val displayName: String,
    val role: RoomRole,
)

sealed interface RoomEvent {
    data class ParticipantJoined(val participant: RoomParticipant) : RoomEvent
    data class ParticipantLeft(val participantId: String) : RoomEvent
    data class GestureSelected(
        val participantId: String,
        val round: Int,
        val gesture: Gesture,
    ) : RoomEvent
    data class RestartRequested(val participantId: String) : RoomEvent
}

interface PrivateRoomSession {
    val code: RoomCode
    val participant: RoomParticipant

    fun send(event: RoomEvent): Boolean
    fun drainEvents(): List<RoomEvent>
    fun close()
}

interface PrivateRoomGateway {
    fun host(code: RoomCode, displayName: String): PrivateRoomSession
    fun join(code: RoomCode, displayName: String): PrivateRoomSession?
}

/**
 * Deterministic transport used for tests, demos, and same-process development.
 *
 * A future LAN adapter can implement [PrivateRoomGateway] without changing domain/UI code.
 * This implementation performs no network I/O and requires no Android network permission.
 */
class InMemoryPrivateRoomGateway : PrivateRoomGateway {
    private data class Room(
        val sessions: MutableMap<String, MemorySession> = linkedMapOf(),
    )

    private val rooms = mutableMapOf<RoomCode, Room>()
    private var nextParticipant = 1

    override fun host(code: RoomCode, displayName: String): PrivateRoomSession {
        require(code !in rooms) { "Room already exists" }
        val room = Room()
        rooms[code] = room
        return createSession(room, code, displayName, RoomRole.HOST)
    }

    override fun join(code: RoomCode, displayName: String): PrivateRoomSession? {
        val room = rooms[code] ?: return null
        if (room.sessions.size >= 2) return null
        val session = createSession(room, code, displayName, RoomRole.GUEST)
        broadcast(room, RoomEvent.ParticipantJoined(session.participant), exceptParticipantId = session.participant.id)
        return session
    }

    private fun createSession(
        room: Room,
        code: RoomCode,
        displayName: String,
        role: RoomRole,
    ): MemorySession {
        val participant = RoomParticipant(
            id = "local-${nextParticipant++}",
            displayName = normalizeDisplayName(displayName),
            role = role,
        )
        return MemorySession(code, participant, room).also { session ->
            room.sessions[participant.id] = session
        }
    }

    private fun normalizeDisplayName(raw: String): String = raw
        .replace('\n', ' ')
        .replace('\r', ' ')
        .trim()
        .take(32)
        .ifBlank { "Player" }

    private fun broadcast(room: Room, event: RoomEvent, exceptParticipantId: String? = null) {
        room.sessions.values
            .filter { it.participant.id != exceptParticipantId }
            .forEach { it.enqueue(event) }
    }

    private inner class MemorySession(
        override val code: RoomCode,
        override val participant: RoomParticipant,
        private val room: Room,
    ) : PrivateRoomSession {
        private val events = mutableListOf<RoomEvent>()
        private var closed = false

        override fun send(event: RoomEvent): Boolean {
            if (closed || room.sessions[participant.id] !== this) return false
            val valid = when (event) {
                is RoomEvent.GestureSelected ->
                    event.participantId == participant.id && event.round > 0
                is RoomEvent.RestartRequested ->
                    event.participantId == participant.id
                is RoomEvent.ParticipantJoined,
                is RoomEvent.ParticipantLeft -> false
            }
            if (!valid) return false
            broadcast(room, event, exceptParticipantId = participant.id)
            return true
        }

        override fun drainEvents(): List<RoomEvent> {
            val snapshot = events.toList()
            events.clear()
            return snapshot
        }

        override fun close() {
            if (closed) return
            closed = true
            room.sessions.remove(participant.id)
            broadcast(room, RoomEvent.ParticipantLeft(participant.id))
            if (room.sessions.isEmpty()) rooms.remove(code)
        }

        fun enqueue(event: RoomEvent) {
            if (!closed) events += event
        }
    }
}
