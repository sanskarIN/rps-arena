# RPS Arena Roadmap

## v1.0.0 — Validated baseline

- [x] Classic and Lizard–Spock rules.
- [x] CPU and same-device two-player gameplay.
- [x] Best-of-3, Best-of-5, Endless, Streak, and Tournament formats.
- [x] Offline settings, statistics, history, achievements, and onboarding.
- [x] Android and desktop entry points.
- [x] Optional Rust rules mirror.
- [x] CI, CodeQL, repository governance, privacy, security, and release documentation.

## v2.5.8 — Product completion, cross-platform expansion, and release hardening

- [x] Configurable 5/10/20/30/60-second round timers with an Off option.
- [x] Explicit timeout outcomes that update score, statistics, trends, and history consistently.
- [x] Replayable challenge-seed controls in the gameplay UI.
- [x] Local player-name profile preference.
- [x] Recent 10-round win/loss/draw trend summary.
- [x] Versioned `RPS_ARENA_BACKUP|1` export/import with strict validation and bounded input.
- [x] Safe settings migration from `settings_v1` to `settings_v2`.
- [x] Local data reset with explicit confirmation.
- [x] English and Hindi gameplay/settings/result/history/achievement UI catalogs and rendering.
- [x] Responsive max-width content framing plus wrapping configuration chips for narrow screens.
- [x] Reduced-motion result behavior and documented accessibility review procedure.
- [x] Transport-neutral private-room multiplayer contracts with a deterministic two-player in-memory adapter.
- [x] Private-room lifecycle authority, sender identity, positive-round, room-code, participant-limit, and close-event validation.
- [x] Shared business/persistence/localization/protocol regression coverage.
- [x] Compose desktop UI smoke tests for onboarding, primary play navigation, Hindi gameplay/achievements, backup controls, and destructive reset confirmation.
- [x] Android application target for API 26+.
- [x] JVM desktop support for Windows, Linux, and macOS.
- [x] iPhone/iPad support through Kotlin/Native device/simulator frameworks plus a SwiftUI/Xcode host.
- [x] Web application with Kotlin/Wasm plus Kotlin/JS compatibility distribution and browser localStorage persistence.
- [x] CI validation for Android, desktop, Web compatibility output, iOS simulator framework/host, and Rust.
- [x] Localization-safe version consistency verification across Android, desktop, iOS, shared metadata, About UI, and deterministic mobile build codes.
- [x] Reproducible tag workflow for unsigned Android/Linux/Web/iOS-framework/Rust release artifacts and checksums.
- [x] Repository formatting, documentation completeness, secret-pattern, Android privacy, and Android lint CI gates.
- [x] Complete setup, development, testing, accessibility, performance, troubleshooting, architecture, platform, ADR, and release guides.

## Next optional/platform-dependent work

- [ ] Add a real LAN transport adapter behind `PrivateRoomGateway` only if an explicit networking release is approved. Primary gameplay must remain fully offline and must not acquire mandatory networking.
- [ ] Add device/emulator-driven Android Compose instrumentation tests and broaden platform UI journeys when runner cost/stability is approved for the project.
- [ ] Add signed Android store, signed/notarized desktop, and signed App Store/TestFlight release jobs only after authorized signing credentials are configured outside Git. Never commit release credentials.
- [ ] Evaluate additional platform families only when they have a meaningful product use case and can maintain the same privacy, accessibility, testing, and documentation quality bar.

## Roadmap rules

Roadmap items are not promises of a date. Security, privacy, accessibility, deterministic gameplay, data compatibility, and a green validation gate take priority over feature count.

**Made by the Sanskar.**
