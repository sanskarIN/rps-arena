# ADR 0004: Keep private-room networking outside core gameplay

- Status: Accepted
- Date: 2026-08-19

## Context

The product direction allows optional LAN/private-room multiplayer, but core RPS Arena is intentionally offline-first. Adding discovery or peer networking prematurely would require additional permissions, untrusted-input handling, reconnect behavior, protocol versioning, and abuse/security review.

## Decision

Do not add mandatory networking to v1. Any future private-room implementation must live behind an explicit transport/session boundary and user opt-in.

Before UI exposure, the implementation must define and test:

- versioned protocol messages;
- bounded room codes/session identifiers;
- malformed-input rejection;
- replay/duplicate-message handling;
- timeouts and disconnects;
- resource limits and discovery cadence;
- privacy behavior and permissions;
- deterministic rule resolution independent of peer claims.

The local CPU and same-device two-player modes must continue to work without any network permission or service.

## Consequences

- v1 keeps a small attack surface and dependable offline behavior.
- Networking cannot silently leak into the core repository/state layer.
- A later LAN feature receives a deliberate threat model and protocol test suite before release.
- Cloud infrastructure remains optional rather than becoming an architectural dependency.

## Alternatives considered

### Ship an unfinished LAN placeholder

Rejected because exposed but unverified networking would conflict with the project's no-broken-core-feature and security requirements.

### Require a hosted multiplayer backend

Rejected because it would undermine offline-first operation and create account/service dependencies that are unnecessary for local play.
