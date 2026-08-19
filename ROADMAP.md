# RPS Arena Roadmap

## v1.0.0 — Validated baseline

- [x] Classic and Lizard–Spock rules.
- [x] CPU and same-device two-player gameplay.
- [x] Best-of-3, Best-of-5, Endless, Streak, and Tournament formats.
- [x] Offline settings, statistics, history, achievements, and onboarding.
- [x] Android and desktop entry points.
- [x] Optional Rust rules mirror.
- [x] CI, CodeQL, repository governance, privacy, security, and release documentation.

## v1.1.0 — Product completion and release hardening

- [x] Configurable 5/10/20/30/60-second round timers with an Off option.
- [x] Explicit timeout outcomes that update score, statistics, trends, and history consistently.
- [x] Replayable challenge-seed controls in the gameplay UI.
- [x] Local player-name profile preference.
- [x] Recent 10-round win/loss/draw trend summary.
- [x] Versioned `RPS_ARENA_BACKUP|1` export/import with strict validation and bounded input.
- [x] Safe settings migration from `settings_v1` to `settings_v2`.
- [x] Local data reset with explicit confirmation.
- [x] English and Hindi core UI catalogs.
- [x] Reduced-motion result behavior and documented accessibility review procedure.
- [x] Transport-neutral private-room multiplayer contracts with a deterministic two-player in-memory adapter.
- [x] Repository formatting and Android lint CI gates.
- [x] Version consistency verification across Android, desktop, and About UI.
- [x] Reproducible tag workflow for unsigned Android/Linux/Rust release artifacts and checksums.
- [x] Expanded shared regression tests for persistence, timeout, backup, trend, and room protocol behavior.
- [x] Complete setup, development, testing, accessibility, performance, troubleshooting, architecture, ADR, and release guides.

## Next optional/platform-dependent work

- [ ] Add a real LAN transport adapter behind `PrivateRoomGateway` only if an explicit networking release is approved. Primary gameplay must remain fully offline and must not acquire mandatory networking.
- [ ] Add device/emulator-driven Compose UI automation for Android and desktop when the chosen test runner is stable across the pinned Kotlin/Compose toolchain.
- [ ] Add signed Android store and signed/notarized desktop release jobs only after signing credentials are configured in an authorized secret store. Never commit release credentials.
- [ ] Evaluate optional iOS packaging without weakening Android/desktop quality or the offline-first architecture.

## Roadmap rules

Roadmap items are not promises of a date. Security, privacy, accessibility, deterministic gameplay, data compatibility, and a green validation gate take priority over feature count.

**Made by the Sanskar.**
