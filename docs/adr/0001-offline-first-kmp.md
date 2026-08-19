# ADR 0001: Offline-first Kotlin Multiplatform core

- Status: Accepted
- Date: 2026-08-19

## Context

RPS Arena must support Android and desktop, remain useful without an account or network connection, and keep game rules testable outside UI code.

## Decision

Use Kotlin Multiplatform for shared model, rules, CPU strategy, state, persistence contracts, and Compose Multiplatform UI. Keep Android and desktop modules thin and platform-specific only where necessary.

Primary gameplay will not depend on a backend, account service, analytics service, advertising SDK, or Android network permission.

## Consequences

### Positive

- One rules implementation serves both primary platforms.
- Deterministic CPU and persistence codecs can be tested in shared code.
- Offline behavior is the default rather than a fallback.
- Platform packaging can evolve independently of game rules.

### Trade-offs

- Platform APIs require expect/actual adapters where shared abstractions are insufficient.
- UI choices must remain compatible with both Android and desktop Compose targets.
- Cloud-only features are intentionally out of scope for the primary product.
