# ADR 0001: Offline-first shared Kotlin architecture

- Status: Accepted
- Date: 2026-08-19

## Context

RPS Arena targets Android and desktop while requiring a trustworthy offline experience, deterministic rules, local persistence, accessibility, and maintainable testing. A cloud backend is not required for core gameplay.

Duplicating game logic per platform would create rule drift and increase testing cost. Introducing a server or microservices for a local game would add privacy, reliability, deployment, and security complexity without solving a current product need.

## Decision

Use Kotlin Multiplatform with Compose Multiplatform for shared domain logic and UI.

Keep one shared domain rules engine and one application-state controller. Use a small `KeyValueStore` interface for platform persistence adapters. Android uses private application preferences; desktop uses Java Preferences.

Core gameplay requires no account and no network service. Optional future LAN/private-room functionality must be isolated behind explicit opt-in and must not become a prerequisite for offline play.

## Consequences

### Positive

- One canonical rules implementation across supported platforms.
- Deterministic CPU behavior can be tested once in shared code.
- Offline gameplay survives network outages by design.
- Smaller privacy and attack surface than a mandatory backend.
- Shared UI reduces platform drift while still allowing platform entry points.

### Tradeoffs

- Platform-specific capabilities still require adapters and testing.
- Native installer generation remains OS-specific.
- Local preference storage is suitable for small game state but not for large relational datasets.
- A future LAN protocol will require a new security review rather than being hidden inside the current persistence layer.

## Alternatives considered

### Separate native Android and desktop applications

Rejected because it duplicates rules, state logic, and UI behavior without a current platform-specific benefit large enough to justify the maintenance cost.

### Mandatory cloud backend

Rejected because it conflicts with the offline-first mission and adds unnecessary operational and privacy complexity.

### Rust as the primary shared rules engine

Deferred. Rust remains a possible educational/verification experiment, but adding FFI before a demonstrated need would increase build complexity and duplicate a very small deterministic domain model.
