# Changelog

All notable changes to RPS Arena are documented here.

## [Unreleased]

No unreleased user-visible changes are currently queued.

## [1.1.0] - 2026-08-19

### Added
- Configurable round timers with 5, 10, 20, 30, and 60 second options plus an Off setting.
- Explicit timeout outcomes for CPU and same-device two-player matches.
- Replayable challenge-seed controls in the gameplay UI.
- Persistent `match_config_v1` storage for the last ruleset, opponent, difficulty, match mode, seed, and timer selection across app restarts.
- Local player-name profile preference.
- Recent 10-round win/loss/draw trend summary.
- Versioned `RPS_ARENA_BACKUP|1` backup export/import for settings, statistics, and recent history.
- Confirmed local-data reset flow, including restoration of saved match setup to defaults.
- English and Hindi localization for navigation, gameplay choices, difficulty/match modes, results, timeout feedback, history rendering, backup/reset feedback, settings, and achievements.
- Reduced-motion-aware round-result transition behavior.
- Branded light/dark color schemes, reusable layout tokens, responsive max-width content framing, and rounded Material 3 component shapes.
- Wrapping configuration-chip layouts so match and timer options remain reachable on narrow screens.
- Transport-neutral private-room multiplayer contracts with a deterministic in-memory two-player reference adapter.
- Shared app metadata constants for language-neutral About/version rendering.
- No-op-by-default structured logger with sensitive-key redaction and bounded non-sensitive fields.
- Regression tests for timer invariants, timeout behavior, backup validation/restore, settings migration, persisted match-config codec/corruption/restart/reset behavior, history/profile sanitization, trends, deterministic replay, localization catalogs, achievement copy, private-room protocol constraints, and logger redaction.
- Compose desktop UI smoke tests covering onboarding, primary play navigation, English/Hindi switching, Hindi gameplay/achievement copy, backup controls, and destructive reset confirmation.
- Repository formatting and cross-platform version consistency verification scripts.
- Relative Markdown documentation-link validation.
- High-confidence committed-secret pattern validation.
- Android privacy-contract validation for no Internet permission, disabled automatic backup, and SharedPreferences cloud/device-transfer exclusions.
- `scripts/check_docs_coverage.py`, which compares `git ls-files` with the exhaustive repository file reference so every tracked path must be documented.
- Documentation coverage enforcement in primary CI, tagged/manual release automation, Unix/PowerShell verification scripts, PR template, testing guide, validation contract, and contributor workflow.
- Focused Security workflow that re-runs secret/privacy checks and performs pull-request dependency review for high-severity findings.
- `.github/CODEOWNERS` maintainer ownership routing for the repository, security/automation, shared core, Rust, and platform packaging.
- Android lint as a required CI gate.
- Tagged release workflow for unsigned Android, Linux desktop, and Rust package artifacts with SHA-256 checksums.
- GitHub repository-settings guidance for rulesets, security features, labels, milestones, Discussions, and release settings.
- Deep command and toolchain references covering command meanings, installation, version inspection, safe upgrades, and the repository's current no-Gradle-Wrapper setup.
- Deep build-system reference covering root Gradle files, version catalog, source sets, platform modules, build output, packaging, and ownership rules.
- Deep domain/gameplay reference covering every model, rule relationship, CPU probability threshold, state transition, timeout, score/streak/history rule, and invariant.
- Deep storage/backup reference covering platform stores, exact keys/codecs, settings migration, persisted match setup, stat invariants, history grammar, explicit backup escaping/limits/import/reset compatibility, and Android automatic-backup separation.
- Deep localization reference for English/Hindi catalogs, canonical vs localized values, achievement copy, persisted language, RTL considerations, and adding future languages.
- Deep private-room protocol reference defining current room-code/session/event authority and future LAN fairness, validation, privacy, versioning, and failure requirements.
- File-by-file Android, desktop, optional Rust-engine, automated-test, GitHub automation, branding, maintenance, and glossary references.
- Role-based documentation index and exhaustive `docs/repository-file-reference.md` covering every Git-tracked file.

### Changed
- Settings persistence now uses `settings_v2` and migrates compatible `settings_v1` data automatically.
- Match configuration now loads from validated local persistence instead of reverting to default gameplay controls on every app/state restart.
- `RPS_ARENA_BACKUP|1` remains intentionally unchanged and does not include `match_config_v1`; importing v1 therefore preserves the receiving device's local match setup.
- Android package version is now `1.1.0` (`versionCode = 2`).
- Desktop package version is now `1.1.0`.
- Version validation now checks Android, desktop, shared metadata, and that About renders the shared version constant instead of depending on English UI text.
- The root `ROADMAP.md` is now the canonical roadmap source.
- History and local profile input are length-bounded and sanitized before persistence.
- Contribution, support, privacy, security, setup, testing, validation, CI/CD, Android-platform, storage/backup, and README documentation now match the stronger source/privacy/security gates.
- The formatting gate permits the standard two-space Markdown hard-break syntax while still rejecting accidental trailing whitespace elsewhere.
- README now distinguishes pinned project baselines from claims about globally newest tool versions and explicitly documents that the repository currently does not track a Gradle Wrapper.

### Security and reliability
- Android automatic app backup is disabled with `android:allowBackup="false"`; legacy/cloud/device-transfer policy files exclude the complete SharedPreferences domain.
- CI, focused Security checks, local verification, and release preflight fail when the Android automatic-backup/no-Internet privacy contract regresses.
- High-confidence committed private-key/token patterns are checked without echoing the matched secret value.
- Pull-request dependency review is configured to fail for high-severity findings.
- The structured logger has no production sink by default and redacts password/secret/token/authorization/cookie/email/backup/content/payload-like fields before any explicit sink receives them.
- Backup import rejects oversized input, excessive record counts, duplicate settings/stat records, unknown record types, invalid settings, invalid statistics invariants, and malformed records before replacing local state.
- Persisted match configuration rejects unknown enum values, invalid seeds, unsupported timers, and malformed field counts by falling back to safe defaults.
- Private-room reference sessions reject events that claim a different participant identity and enforce a two-participant maximum.
- Private-room client sessions cannot forge gateway-owned join/leave lifecycle events and reject gesture events with non-positive round numbers.
- Closing a room session broadcasts its leave lifecycle event only once.
- Primary gameplay remains offline-first and does not add an Android internet permission.
- Relative documentation links, exhaustive tracked-file documentation coverage, committed-secret patterns, Android privacy, and version consistency are all rechecked before release packaging.

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
