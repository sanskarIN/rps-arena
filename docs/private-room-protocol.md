# Private-Room Protocol and Future LAN Transport Guide

RPS Arena contains a transport-neutral private-room multiplayer boundary plus an in-memory reference implementation. It does **not** currently ship a real network/LAN socket implementation in the primary application.

This document defines the current protocol model, room-code grammar, participant lifecycle, sender authority, event validation, in-memory behavior, security/privacy boundaries, and requirements for any future real LAN adapter.

## Purpose of the boundary

Primary RPS Arena gameplay is offline-first. A multiplayer room feature should not force the entire game to depend on:

- internet permission;
- cloud accounts;
- remote databases;
- background discovery;
- third-party realtime services.

The shared interfaces allow a future transport to be added behind a stable contract without moving networking concerns into `ArenaState`, `RulesEngine`, or core UI logic.

## Source file

`shared/src/commonMain/kotlin/in/sanskar/rpsarena/network/PrivateRoom.kt`

It currently defines:

- `RoomCode`;
- `RoomRole`;
- `RoomParticipant`;
- `RoomEvent`;
- `PrivateRoomSession`;
- `PrivateRoomGateway`;
- `InMemoryPrivateRoomGateway`.

## `RoomCode`

`RoomCode` is a Kotlin inline value class with a private constructor.

A code can be created only through:

```kotlin
RoomCode.parse(raw)
```

or:

```kotlin
RoomCode.require(raw)
```

### Normalization

Input is:

1. trimmed;
2. converted to uppercase.

So an input containing a leading and trailing space, represented here as:

```text
␠abc234␠
```

normalizes to:

```text
ABC234
```

### Length

Exactly six characters are required.

### Allowed alphabet

```text
ABCDEFGHJKLMNPQRSTUVWXYZ23456789
```

Notably omitted:

```text
I
O
0
1
```

These are intentionally excluded because they are visually ambiguous in many fonts/displays.

### `parse()` vs `require()`

`parse()` returns `null` for invalid input.

`require()` throws `IllegalArgumentException` when invalid.

Use `parse()` for user-entered room-code validation. Use `require()` for trusted fixtures/constants where invalid source data is a programmer error.

## Room-code security meaning

A six-character room code is a rendezvous identifier, not high-assurance authentication.

Do not use it to protect:

- passwords;
- payment data;
- private messages;
- account tokens;
- sensitive files.

The intended protocol carries game-room events only.

A future LAN implementation should not claim that the short code is encryption or identity proof.

## Participant roles

`RoomRole`:

- `HOST`;
- `GUEST`.

`RoomParticipant` contains:

- `id` — session/transport identity;
- `displayName` — sanitized user-facing name;
- `role` — host/guest role.

The in-memory gateway assigns generated participant IDs such as:

```text
local-1
local-2
```

A real transport will need its own collision-safe session identity design.

## Display-name normalization

The in-memory reference adapter:

- replaces CR/LF with spaces;
- trims outer whitespace;
- limits length to 32 characters;
- falls back to `Player` if blank.

A future transport must perform equivalent validation on received names instead of trusting remote input.

## Room events

`RoomEvent` is a sealed interface. Current event types are:

### `ParticipantJoined`

```kotlin
data class ParticipantJoined(
    val participant: RoomParticipant,
)
```

Lifecycle event indicating a real participant joined.

### `ParticipantLeft`

```kotlin
data class ParticipantLeft(
    val participantId: String,
)
```

Lifecycle event indicating a real session closed/left.

### `GestureSelected`

```kotlin
data class GestureSelected(
    val participantId: String,
    val round: Int,
    val gesture: Gesture,
)
```

Gameplay event containing sender, positive round number, and selected gesture.

### `RestartRequested`

```kotlin
data class RestartRequested(
    val participantId: String,
)
```

Gameplay/control request for a restart handshake.

The current reference transport only validates/transfers events; it does not itself implement a complete online match state machine or restart consensus UI.

## `PrivateRoomSession`

A connected participant sees:

```kotlin
interface PrivateRoomSession {
    val code: RoomCode
    val participant: RoomParticipant

    fun send(event: RoomEvent): Boolean
    fun drainEvents(): List<RoomEvent>
    fun close()
}
```

### `code`

Stable room identifier for the session.

### `participant`

The local participant identity attached to this session.

### `send(event)`

Attempts to send a client-owned event.

Returns `true` only when the reference adapter accepts it.

A `false` result can indicate:

- session is closed/detached;
- participant ID was forged;
- gesture round was non-positive;
- client tried to send a gateway-owned lifecycle event.

### `drainEvents()`

Returns all currently queued incoming events in order and clears the queue.

This polling-style method is intentionally simple for deterministic tests/reference behavior. A real LAN adapter may internally use flows/channels/sockets, but it must still satisfy the semantic contract expected by clients or evolve the interface deliberately.

### `close()`

Ends the session.

The reference implementation is idempotent: calling `close()` more than once does not broadcast repeated leave events.

## `PrivateRoomGateway`

```kotlin
interface PrivateRoomGateway {
    fun host(code: RoomCode, displayName: String): PrivateRoomSession
    fun join(code: RoomCode, displayName: String): PrivateRoomSession?
}
```

### `host()`

Creates a new room and HOST session.

The in-memory implementation rejects hosting a code already present in the gateway instance.

### `join()`

Returns a GUEST session when:

- the room exists;
- it has room for a second participant.

Returns `null` when no join is possible.

## Two-player limit

The reference room accepts at most two sessions.

This matches the current Rock Paper Scissors opponent model and deliberately prevents an unbounded room collection from becoming an accidental chat/lobby service.

A future spectator/multi-player design would need an explicit protocol/product revision.

## In-memory room lifecycle

### Host creation

1. verify code is not already hosted;
2. create empty room object;
3. create HOST participant/session;
4. store session under participant ID;
5. return host session.

The host does not receive its own `ParticipantJoined` event.

### Guest join

1. look up room;
2. reject if missing;
3. reject if already two sessions;
4. create GUEST participant/session;
5. insert guest;
6. broadcast `ParticipantJoined(guest)` to other sessions (host);
7. return guest session.

### Guest/host close

1. if already closed, return;
2. mark closed;
3. remove own participant ID from room;
4. broadcast `ParticipantLeft(id)` to remaining session;
5. if room is empty, remove room object from gateway.

## Broadcast behavior

Reference helper:

```text
broadcast(room, event, exceptParticipantId)
```

iterates current sessions and queues the event for everyone except the excluded participant.

Normal client sends are therefore peer-directed and do not echo back to sender in the in-memory reference implementation.

Lifecycle join/leave events are generated by gateway behavior, not accepted from client `send()`.

## Sender authority validation

For `GestureSelected`:

```text
event.participantId must equal session.participant.id
round must be > 0
```

For `RestartRequested`:

```text
event.participantId must equal session.participant.id
```

For:

```text
ParticipantJoined
ParticipantLeft
```

client `send()` always rejects them.

This distinction prevents a participant from telling the peer that another fake participant joined/left.

## Closed-session behavior

A session rejects sends when:

- its `closed` flag is true; or
- the room's current session map no longer points to that session object under its participant ID.

This second identity check prevents detached/stale session objects from sending after their registration is gone.

## Test coverage

`PrivateRoomTest.kt` covers:

- code normalization;
- ambiguous/invalid code rejection;
- host/guest event exchange;
- forged participant ID rejection;
- two-participant limit;
- invalid zero/non-positive round rejection;
- lifecycle-event send rejection;
- close lifecycle event;
- repeated close idempotency.

If protocol behavior changes, tests should change in the same pull request.

## What the current implementation is **not**

It is not:

- Bluetooth networking;
- Wi-Fi Direct;
- TCP/UDP LAN transport;
- mDNS/NSD discovery;
- WebSocket service;
- internet matchmaking;
- NAT traversal;
- encrypted peer transport;
- account authentication;
- persistent room server;
- background service.

It makes no network call at all.

## Android permission boundary

The primary `AndroidManifest.xml` currently declares no internet/network permissions.

Adding a real LAN adapter may require permission/manifest changes depending on transport/API/platform version. Those changes must be explicit and reviewed; do not quietly add network permissions to support code that is not user-facing or enabled.

## Requirements for a future LAN adapter

A production LAN implementation should satisfy at least these areas.

### Explicit activation

Network behavior starts only after the user enters/hosts/joins a private-room flow.

Do not run permanent discovery loops on app launch.

### Offline independence

Classic CPU/local-two-player gameplay must still work when:

- Wi-Fi is off;
- permission is denied;
- no peer exists;
- network transport crashes/fails.

### Input validation

Treat all received peer data as untrusted.

Validate:

- protocol version;
- message type;
- room code;
- participant/session ID;
- display-name length/content;
- round number/range;
- gesture enum/value;
- message size;
- sequence/state validity;
- restart/lifecycle authority.

### Bounded messages

Define maximum serialized message size and reject oversized frames before allocating/processing unbounded data.

### State ordering

A real network can reorder, duplicate, delay, or drop messages depending on transport.

Define:

- round sequence number;
- duplicate handling;
- stale message rejection;
- restart generation/match ID;
- disconnect/reconnect behavior;
- conflict resolution when both peers act concurrently.

The current in-memory queue has deterministic local ordering and does not model all network failure modes.

### Host authority vs peer symmetry

Choose and document whether:

- host is authoritative match coordinator; or
- peers run symmetric deterministic state and validate each other.

Do not leave authority ambiguous once real messages can arrive asynchronously.

### Gesture privacy/fairness

Naively sending a gesture immediately can let one peer learn the other move before selecting.

A fair network RPS implementation should consider a commit/reveal protocol or another design that prevents early disclosure.

A safe conceptual sequence is:

1. each peer chooses gesture;
2. each sends a commitment derived from gesture + random nonce;
3. after both commitments arrive, each reveals gesture + nonce;
4. peers verify reveals match commitments;
5. resolve round.

If implementing cryptographic commitments, use established cryptographic hash APIs correctly; do not invent custom cryptography. Define nonce size/source and protocol encoding precisely, add replay protection, and get security review.

The current in-memory `GestureSelected` event is an architecture reference and should not automatically be treated as a fairness-secure wire protocol.

### Privacy

A future LAN adapter should transmit only information necessary for the game, for example:

- room/session protocol metadata;
- chosen display name;
- game configuration;
- gameplay events.

Do not transmit unrelated device identifiers, contacts, location, analytics IDs, filesystem paths, account data, or full local history.

### Network shutdown

Closing/leaving a room must stop:

- sockets;
- discovery registrations;
- listener coroutines;
- retry loops;
- keepalives;
- queued outgoing tasks.

No background room network activity should continue after session close unless a separately documented platform requirement exists.

## Suggested future message envelope

If a real serialized protocol is added, use an explicit versioned envelope rather than directly serializing arbitrary Kotlin objects.

Conceptually:

```text
protocolVersion
roomCode or session token
matchId
eventId
senderId
eventType
payload
```

Add strict length/type/range validation.

Do not use language-native arbitrary object deserialization on untrusted network input.

## Protocol versioning

The current Kotlin interfaces do not expose a wire protocol version because no wire protocol exists yet.

Before networking, define one.

Rules:

- incompatible message changes require version negotiation/rejection;
- unknown event types should not be blindly executed;
- old clients need explicit compatibility policy;
- security-critical fields should never have ambiguous defaults.

## Discovery choices

A future LAN implementation may evaluate platform-supported local discovery APIs. Whatever technology is chosen, document:

- Android permission implications;
- desktop firewall behavior;
- supported subnets/network types;
- discovery timeout;
- manual-code fallback;
- privacy of advertised service metadata;
- cleanup/unregister behavior.

Do not promise cross-network/internet connectivity if only same-LAN discovery is implemented.

## UI/state integration requirements

A real room feature should not push socket details into Composables.

Prefer layers such as:

```text
UI
 -> room/match state coordinator
 -> PrivateRoomGateway / session abstraction
 -> platform transport
```

Core `RulesEngine` should continue resolving gestures independently of transport.

Persisted lifetime stats policy for network rounds must be decided explicitly rather than accidentally inheriting local behavior.

## Failure states to design

At minimum provide user-visible behavior for:

- invalid room code;
- room not found;
- room full;
- peer disconnected;
- host closed room;
- incompatible protocol version;
- malformed peer message;
- timeout waiting for peer;
- restart disagreement;
- permission denied;
- local network unavailable;
- discovery failure;
- duplicate/stale event.

Do not expose raw exceptions as the only UI.

## Security review checklist for real transport

Before release:

- no mandatory internet/cloud path;
- least required permissions;
- no secrets committed;
- no sensitive data in room messages;
- message-size bounds;
- parser fuzz/edge tests where practical;
- sender/session validation;
- replay/stale-event handling;
- fair gesture selection protocol;
- disconnect cleanup;
- lifecycle tests;
- threat model documented;
- privacy policy updated;
- Android/Desktop/manual network testing performed on every platform that ships the transport.

## Files likely affected by real LAN work

- `PrivateRoom.kt` or a successor protocol package;
- platform-specific source sets/adapters;
- Android manifest if permissions are needed;
- iOS entitlement/info configuration if local-network features require it;
- Web transport/security policy when browser networking is involved;
- room state coordinator;
- UI screens/localization;
- protocol tests;
- platform integration tests;
- `PRIVACY.md`;
- `SECURITY.md`;
- `docs/architecture.md`;
- `docs/testing.md`;
- this document;
- `README.md`/`CHANGELOG.md` when shipped.

## Current release statement

As of the v2.5.8 candidate documented here, private-room support means **testable transport-neutral architecture and an in-memory two-player reference adapter**, not production LAN multiplayer. Primary gameplay remains offline-first and the Android application has not introduced an internet permission.
