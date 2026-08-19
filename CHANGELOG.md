# Changelog

All notable changes to RPS Arena are documented here.

## [Unreleased]

No unreleased user-visible changes are currently queued.

## [1.1.0] - 2026-08-19

### Added
- Configurable round timers with 5, 10, 20, 30, and 60 second options plus an Off setting.
- Explicit timeout outcomes for CPU and same-device two-player matches.
- Replayable challenge-seed controls in the gameplay UI.
- Local player-name profile preference.
- Recent 10-round win/loss/draw trend summary.
- Versioned `RPS_ARENA_BACKUP|1` backup export/import for settings, statistics, and recent history.
- Confirmed local-data reset flow.
- English and Hindi core UI catalogs.
- Reduced-motion-aware round-result transition behavior.
- Transport-neutral private-room multiplayer contracts with a deterministic in-memory two-player reference adapter.
- Regression tests for timeout behavior, backup validation/restore, settings migration, trends, and private-room protocol constraints.
- Repository formatting and cross-platform version consistency verification scripts.
- Android lint as a required CI gate.
- Tagged release workflow for unsigned Android, Linux desktop, and Rust package artifacts with SHA-256 checksums.
- Setup, development, troubleshooting, accessibility, performance, architecture, testing, release, and ADR documentation.

### Changed
- Settings persistence now uses `settings_v2` and migrates compatible `settings_v1` data automatically.
- Android package version is now `1.1.0` (`versionCode = 2`).
- Desktop package version is now `1.1.0`.
- The root `ROADMAP.md` is now the canonical roadmap source.
- History and local profile input are length-bounded and sanitized before persistence.

### Security and reliability
- Backup import rejects oversized input, excessive record counts, duplicate settings/stat records, unknown record types, invalid settings, invalid statistics invariants, and malformed records before replacing local state.
- Private-room reference sessions reject events that claim a different participant identity and enforce a two-participant maximum.
- Primary gameplay remains offline-first and does not add an Android internet permission.

## [1.0.0] - 2026-08-19

### Added
- Kotlin Multiplatform architecture with separate Android and desktop entry-point modules.
- Shared Compose UI and game state.
- Classic and Lizard–Spock rules.
- CPU and same-device two-player modes.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament formats.
- Deterministic seeded CPU logic with Easy, Normal, and Expert presets.
- Offline statistics, recent history, settings, achievements, and onboarding.
- Android adaptive icon assets and project splash artwork.
- Optional Rust rules engine.
- CI, CodeQL, dependency updates, contribution, privacy, security, validation, release, and testing documentation.

### Fixed during validation
- Aligned Android builds and documentation with stable compile/target SDK 36.
- Updated the Android Kotlin Multiplatform DSL for Android Gradle Plugin 9.
- Removed obsolete Compose tooling accessors from the Android application module.
- Corrected Kotlin source, test, and entry-point packages whose leading `in` segment must be escaped in Kotlin syntax.
- Added the Material 3 opt-in required by the top app bar.
- Modernized Android SDK setup and current GitHub Actions/CodeQL workflow versions.

### Verified
- Shared Kotlin tests passed.
- Android debug assembly passed.
- Desktop JVM compilation passed.
- Rust engine tests passed.
- CodeQL Kotlin/Java analysis passed.
