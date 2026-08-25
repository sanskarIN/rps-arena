# ADR 0002: Keep private-room networking behind an optional transport boundary

- Status: Accepted
- Date: 2026-08-19

## Context

The product roadmap calls for private-room/LAN multiplayer architecture without making cloud connectivity mandatory. Adding networking directly to game state would couple primary offline gameplay to permissions, transport failures, discovery behavior, and platform-specific sockets.

## Decision

Define private-room behavior through `PrivateRoomGateway` and `PrivateRoomSession` contracts in shared code. Ship an in-memory reference adapter for deterministic tests and same-process development.

A real LAN adapter may be added later as an explicit opt-in platform module. It must validate room codes and participant identity, support only the required two-player scope, avoid mandatory external infrastructure, and stop all network activity when the room closes.

## Security and privacy rules

- Do not enable background discovery by default.
- Do not transmit analytics or unrelated device data.
- Do not treat a short room code as authentication for sensitive data; rooms carry game moves only.
- Reject forged participant IDs at the session boundary.
- Keep primary gameplay functional when networking is absent or denied.

## Consequences

The current application has a testable multiplayer contract without requesting Android network permission. A future LAN implementation can be reviewed and shipped independently instead of changing the core game engine.
