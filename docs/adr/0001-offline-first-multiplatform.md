# ADR 0001: Offline-first Kotlin/Compose Multiplatform architecture

- Status: Accepted
- Date: 2026-08-19

## Context

RPS Arena targets Android and desktop and must remain fully useful without an account or network service. Duplicating game rules and state per platform would increase drift and testing cost. A mandatory backend would add deployment, privacy, security, and availability complexity that the core game does not need.

## Decision

Use Kotlin Multiplatform and Compose Multiplatform for the shared rules engine, CPU strategy, state model, persistence codecs, UI, and common tests.

Keep Android and desktop as small platform entry/packaging modules. Core gameplay requires no server, account, analytics service, or Android internet permission.

## Consequences

### Benefits

- One canonical game implementation across primary platforms.
- Shared deterministic tests cover both Android and desktop behavior.
- Offline play is the normal architecture rather than a fallback.
- Smaller privacy and operational attack surface.
- Platform modules remain focused on integration and packaging.

### Tradeoffs

- Platform-specific behaviors still need real platform verification.
- Native desktop installers are built per operating system.
- Optional iOS support is deferred until it can be tested and packaged properly.
- Future networking must be added as an explicit optional boundary instead of hidden inside core state.

## Alternatives considered

### Separate native applications

Rejected because the small domain would be duplicated without a strong platform-specific benefit.

### Mandatory cloud backend

Rejected because it conflicts with the offline-first mission and adds unnecessary service/privacy dependencies.
