# Changelog

All notable changes to RPS Arena are documented here.

## [Unreleased]

### Added

- Kotlin Multiplatform architecture with separate Android and desktop entry-point modules.
- Shared Compose UI, game state, rule engine, and deterministic CPU strategies.
- Classic and Lizard–Spock rules.
- CPU and same-device private two-player modes.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament match configurations.
- Persisted replayable CPU seed and configurable turn timers.
- Deterministic timeout moves for CPU and same-device two-player turns.
- Local stats, recent history, achievements, settings, and first-run onboarding.
- Versioned `RPS_ARENA_BACKUP_V1` export/import for settings, stats, match setup, and history.
- Clear-history and confirmed full local-data reset actions.
- Android adaptive icon assets and project logo/splash artwork.
- Optional standalone Rust rules engine with formatting, Clippy, tests, and benchmark support.
- CI, CodeQL, Dependabot, documentation-link validation, and tagged/manual release artifact workflows.
- Canonical setup, development, architecture, testing, release, troubleshooting, accessibility, performance, roadmap, and ADR documentation.

### Changed

- Production Android compile/target baseline moved from preview API 37 to stable API 36 for reproducible hosted CI.
- Kotlin/AGP/Gradle compatibility aligned to Kotlin 2.4.10, AGP 9.1.0, and Gradle 9.5.0.
- Match setup now persists across application launches.
- Recent history is reactive after play, clear, import, and reset operations.
- Repository persistence accepts injected in-memory storage for common regression tests.
- Unused sound, haptics, and duplicate extended-variant preference flags were removed from the active model.
- The settings codec retains compatibility with the previous seven-field local representation.
- README and release documentation now describe only features/toolchain versions that exist in the audited branch.

### Fixed

- Rust formatting failure detected by CI.
- CodeQL no longer depends on Android SDK package availability.
- CI no longer attempts to install unavailable preview Android platform 37.
- Malformed backup/stat data is rejected or safely defaulted instead of silently replacing valid state.
- Duplicate uppercase documentation guides were removed after canonical lowercase replacements were added.

### Security and privacy

- Core gameplay requires no account, cloud service, analytics SDK, advertising SDK, or Android internet permission.
- Backup import validates all required sections before mutating local data.
- Distribution signing material remains outside the public repository.

## [1.0.0] - 2026-08-19

Initial release-candidate baseline. Tag only after the final clean CI, security, documentation, and manual release audit succeeds.
