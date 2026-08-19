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
- Local lifetime stats, recent history, achievements, settings, and first-run onboarding.
- Up to six local-only player profiles with bounded names, active-profile selection, persistence, reset behavior, and backup support.
- Recent W/L/D trend derivation from stored history with a text legend, semantic result labels, and decisive win rate.
- Local completed-round `Copy result` action with explicit copied state and no network dependency.
- Versioned `RPS_ARENA_BACKUP_V2` export/import for settings, profiles, stats, match setup, and history.
- Non-mutating validated backup preview before import.
- Backward-compatible migration of `RPS_ARENA_BACKUP_V1` backups to a default local profile.
- One-step undo for an accidental recent-history clear until new history is written.
- Clear-history and confirmed full local-data reset actions.
- Reduced-motion-aware round-result rendering.
- Stable semantic UI test tags for the primary onboarding/play journey.
- Shared Compose UI regression test covering first-run onboarding through the first rendered round result.
- Central English UI copy catalog for localization-ready application/settings/achievement/turn/profile/data/trend/copy-result text.
- Structured local logging with sensitive-key redaction and no intentional profile-name/backup/history logging.
- Pure private-room protocol/transport boundary with validation tests and no production network dependency.
- Android adaptive icon assets and project logo/splash artwork.
- Optional standalone Rust rules engine with formatting, Clippy, tests, and benchmark support.
- CI, CodeQL, Dependabot, documentation-link validation, security checks, and tagged/manual release artifact workflows.
- High-confidence committed-secret scanner and pull-request dependency review workflow.
- Canonical setup, development, architecture, testing, release, troubleshooting, accessibility, performance, roadmap, repository-settings, and ADR documentation.

### Changed

- Production Android compile/target baseline moved from preview API 37 to stable API 36 for reproducible hosted CI.
- Kotlin/AGP/Gradle compatibility aligned to Kotlin 2.4.10, AGP 9.1.0, and Gradle 9.5.0.
- Match setup now persists across application launches.
- Recent history is reactive after play, clear, undo, import, and reset operations.
- History entries are bounded and sanitized before persistence.
- Player-one history rows include an explicit `Player 1 (...)` role prefix so profile names such as `CPU` or `Player 2` cannot be confused with opponent outcomes.
- Backup decoding is size-bounded and stages settings, stats, match configuration, profiles, and history validation before mutation.
- Repository persistence accepts injected in-memory storage for common regression and UI tests.
- User-facing achievement and local-turn text moved out of domain/state models into the UI copy catalog.
- Recent trend status values are non-interactive semantic surfaces rather than fake clickable controls.
- Main shared content is bounded and centered on wider desktop windows while remaining adaptive on narrow layouts.
- Unused sound, haptics, and duplicate extended-variant preference flags were removed from the active model rather than exposing controls that did not have real behavior.
- The settings codec retains compatibility with the previous seven-field local representation.
- README and release documentation describe only features/toolchain versions that exist in the audited branch.

### Fixed

- Rust formatting failure detected by CI.
- Kotlin packages using the `in` keyword are escaped correctly in source and tests.
- CodeQL no longer depends on Android SDK package availability.
- CI no longer attempts to install unavailable preview Android platform 37.
- Malformed backup/stat data is rejected or safely defaulted instead of silently replacing valid state.
- Backup history validation now completes before any imported state is written.
- History clear can be recovered once instead of being immediately irreversible.
- Recent trend parsing no longer risks misclassifying player-one wins when a local profile display name resembles an opponent label.
- Trend status tokens no longer present non-actions as clickable controls to keyboard or assistive-technology users.
- Duplicate uppercase documentation guides were removed after canonical lowercase replacements were added.

### Security and privacy

- Core gameplay requires no account, cloud service, analytics SDK, advertising SDK, or Android internet permission.
- Local profile display names are device-local, included only in user-generated backups, and intentionally excluded from structured logs.
- The copy-result action writes only the user-selected round summary to the platform clipboard after an explicit user action.
- Backup preview and import use the same defensive decoder; malformed, oversized, or internally inconsistent data is rejected.
- CI scans committed source for high-confidence credential patterns and reviews dependency changes on pull requests.
- Dependabot tracks Gradle, Cargo, and GitHub Actions dependencies.
- Distribution signing material remains outside the public repository.

## [1.0.0] - 2026-08-19

Initial release-candidate baseline. Tag only after the final clean CI, security, documentation, and manual release audit succeeds.
