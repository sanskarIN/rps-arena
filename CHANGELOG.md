# Changelog

All notable changes to RPS Arena are documented here.

## [Unreleased]

### v2.5.8 release candidate

v2.5.8 is reconciled against the current validated `main` architecture. The package/runtime version remains `2.5.8` with mobile build code `20508`; this section becomes a dated release entry only after the exact release revision is tagged and its release artifacts/checksums are verified.

#### Added

- iPhone/iPad Kotlin/Native `iosArm64` and Apple-silicon `iosSimulatorArm64` framework targets.
- SwiftUI/Xcode iOS host embedding the shared Compose UI through `ComposeUIViewController`.
- iOS `NSUserDefaults` persistence adapter behind the shared `PlatformStore` contract.
- First-class Kotlin/Wasm + Kotlin/JS Web application with shared `webMain`, Compose compatibility distribution, and browser `localStorage` persistence.
- Transport-neutral `PrivateRoomGateway` / `PrivateRoomSession` contracts with a deterministic in-memory no-network two-player reference adapter.
- No-op-by-default structured `SafeLogger` with sensitive-field redaction and bounded non-sensitive values.
- Persisted `match_config_v1` for ruleset, opponent, difficulty, match mode, and deterministic CPU seed.
- Safe fallback to default match configuration when stored data is malformed.
- Stricter statistics invariant validation and bounded/newline-safe history persistence.
- Compose Multiplatform English/Hindi resources plus automated key/placeholder parity validation.
- Stable localization-independent `ArenaUiTags`.
- Desktop Compose UI automation and Android KMP instrumentation smoke tests.
- Android device-test APK assembly in local/hosted verification.
- Expanded state tests for deterministic replay, persisted match setup, invalid-gesture rejection, and privacy-safe logging.
- Cross-platform version verification for Android, desktop, iOS plist/Xcode metadata, shared metadata, and deterministic numeric mobile build-code mapping.
- Source gates for formatting, relative Markdown links, exhaustive tracked-file documentation, committed-secret patterns, Android privacy invariants, and localization catalogs.
- Focused Security workflow with dependency review plus CodeQL Kotlin/Java analysis.
- Tagged/manual packaging workflows for public Android, Linux desktop, Web compatibility ZIP, iOS framework ZIPs, Rust crate output, and SHA-256 checksums.
- `docs/NEXT_VERSION.md`, which prepares v2.5.9 without changing live v2.5.8 package metadata.

#### Changed

- Reconciled the long-running phase-7 branch with current `main` through two-parent commit `70d8b6c1c01cda5d81ebf7ab4c5bade9accc79cc` instead of squashing/rebasing away its granular history.
- Preserved the untouched pre-reconciliation state on `archive/phase-7-pre-main-sync-20260824`.
- Adopted current `main` as authoritative for `ArenaStore`, `RPSARENA_BACKUP|1`, Compose-resource localization, `ArenaUiTags`, desktop UI tests, Android instrumentation tests, and current shared UI/state/repository structure.
- Reintroduced overlapping v2.5.8 product behavior as focused commits rather than copying the older parallel UI/localization/backup implementation.
- CI now combines repository source gates, localization validation, shared tests, desktop UI tests, Android instrumentation packaging, Android lint/debug, desktop compilation, Web compatibility distribution, iOS simulator framework/host validation, and Rust tests.
- Gradle Actions setup is aligned on v6 where used by the reconciled workflows.
- Documentation-link validation ignores fenced/inline code before scanning relative Markdown links, preventing Ivy artifact-pattern false positives.
- The Xcode simulator host excludes unsupported `x86_64`, accurately matching the configured Apple-silicon simulator target instead of claiming `iosX64` support.
- README, roadmap, PR description, and `what_changed.md` now distinguish implemented behavior from features that exist only in older branch history.
- The exact merged `main` release revision is `4136aff448e9489a3e8252ceea7c1e9e79d17c19`.

#### Security and reliability

- Android primary gameplay remains offline-first with no `android.permission.INTERNET` in the main manifest.
- Android automatic backup remains disabled and SharedPreferences remain excluded from platform cloud/device-transfer policy.
- Backup schema 1 remains `RPSARENA_BACKUP|1` and validates complete input before applying settings/statistics/history.
- Backup contents remain settings, aggregate statistics, and up to 30 history entries; `match_config_v1` is not silently added to schema 1 during reconciliation.
- Persisted statistics reject negative or inconsistent counters and impossible streak relationships.
- History writes strip CR/LF, trim blanks, ignore empty records, and bound entries to 160 characters.
- Invalid gestures for the active ruleset are rejected rather than recorded.
- Safe logging has no production sink by default and never logs raw backup payloads from `ArenaState`.
- Public repository/CI contains no Android keystores, Apple signing identities, store credentials, Windows certificates, or macOS notarization secrets.
- Real LAN/private-room network transport is still not shipped.

#### Exact-head validation evidence

- CI push run `32853891608` completed successfully on `4136aff448e9489a3e8252ceea7c1e9e79d17c19`.
- Security checks push run `32853891297` completed successfully on the same revision.
- CodeQL push run `32853891464` completed successfully on the same revision.
- Cross-platform version consistency remains `2.5.8` / `20508`.

#### Not currently claimed as v2.5.8 runtime behavior

The pre-reconciliation branch history contains implementations for the following, but they are not treated as current features unless ported onto the reconciled architecture with focused tests:

- configurable round timers and typed timeout outcomes;
- user-visible editable seed controls (the seed itself is currently persisted/deterministic);
- local player-name/profile UI and recent trend summaries;
- destructive reset flow and associated confirmation UX;
- the older manual `ArenaStrings` / `AppLanguage` localization implementation.

These items are candidates for v2.5.9 and are not described as shipped v2.5.8 behavior.

#### Release steps remaining

- Create the `v2.5.8` tag from the exact validated release revision.
- Confirm the tag-triggered Release workflow completes successfully.
- Verify generated Android, desktop, Web, iOS, and Rust artifacts where configured.
- Verify generated SHA-256 checksums against the published artifacts.
- Publish/finalize the GitHub release notes only after artifact and checksum verification.
- Confirm post-release `main` remains green.

### v2.5.9 preparation

The next patch is planned but not yet version-bumped. The eventual semantic/mobile values are `2.5.9` and `20509` only after v2.5.8 is released. Candidate scope is maintained in [`docs/NEXT_VERSION.md`](docs/NEXT_VERSION.md) and includes backup preview, reversible history clearing, reset confirmation, carefully ported profile support, visible seed controls, timer restoration with migration tests, broader UI/accessibility automation, and iOS/Web robustness.

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
