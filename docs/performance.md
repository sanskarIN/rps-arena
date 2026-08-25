# Performance Notes

RPS Arena is deliberately small, offline-first, and dependency-light. Performance work should preserve that character rather than add infrastructure without evidence.

## Budgets

These are engineering targets for ordinary supported devices, not claims that every device will produce identical measurements:

- Core gesture resolution: constant-time rule lookup with no I/O.
- CPU selection: bounded work over at most the current match history.
- Recent history persistence: maximum 30 records.
- Backup input: maximum 128 KiB and bounded record count.
- UI gesture controls: no blocking filesystem or network work in click handlers beyond the existing small local preference writes.
- Primary gameplay: no network request and no cloud dependency.

## Current safeguards

- History is capped at 30 entries.
- Backup import caps both input length and record count before parsing.
- CPU strategy scans only local match history and uses deterministic seeded randomness.
- Private-room reference transport is in-memory and bounded to two participants.
- No analytics, advertising, image-loading, or background-sync SDK is part of the primary app.
- Reduced-motion mode avoids the animated result transition.

## Measurement workflow

When a performance regression is suspected:

1. Reproduce it on a supported Android device/emulator or desktop target.
2. Record build type, hardware, OS, Java runtime, and exact user flow.
3. Measure before changing implementation.
4. Separate startup, UI rendering, persistence, CPU strategy, and packaging concerns.
5. Add a deterministic test/benchmark when the hot path is stable enough to measure reliably.
6. Document the before/after evidence in the pull request.

## Large-data behavior

The product intentionally avoids unbounded local lists. If history, profiles, achievements, or multiplayer event logs grow in a future release, introduce paging/bounded retention before adding large in-memory collections.

## Networking budget

Primary RPS Arena remains fully usable without networking. A future LAN adapter must not introduce discovery loops, background polling, telemetry, or mandatory cloud calls into offline gameplay. Network work should be scoped to an explicit private-room session and stopped when that session closes.
