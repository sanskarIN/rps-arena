# Performance

RPS Arena is intentionally lightweight. It has no network-backed feed, image pipeline, database, or background synchronization in v1, so the primary performance risks are unnecessary recomposition, unbounded local data, and expensive UI work.

## Budgets

Release-candidate targets:

- game-rule resolution remains constant-time;
- CPU gesture selection remains effectively instant for the capped in-memory history used by a match;
- persisted recent history is capped at 30 records;
- no artificial loading delays;
- no blocking network work on the UI thread because core play has no network dependency;
- no unbounded list growth in persisted history.

These are engineering targets, not benchmark claims. Device-specific startup and memory numbers must be measured before publishing numeric performance claims.

## Current safeguards

- Rule lookup uses a fixed gesture relation map.
- Persistence reads compact preference strings rather than scanning files.
- History is capped before persistence and display.
- The CPU considers only current-session player history and five or fewer possible gestures.
- Shared UI avoids large assets and expensive rendering effects.

## Profiling

For Android, use Android Studio CPU, memory, and Compose tooling on a release-like build.

For desktop, use JVM tooling such as Java Flight Recorder when investigating measured regressions.

Profile before adding caches. Any cache must document ownership, invalidation, and memory bounds.

## Regression checks

Performance-sensitive changes should record:

1. scenario and device/runtime;
2. before measurement;
3. after measurement;
4. measurement method;
5. tradeoffs.

Do not optimize by weakening correctness, accessibility, privacy, or deterministic test behavior.

## Future LAN mode

If private-room/LAN multiplayer is implemented, add explicit budgets for discovery frequency, message size, queue bounds, reconnect behavior, and battery/network use before shipping it.
