# Changelog

All notable changes to RPS Arena are documented here.

## [Unreleased]

### Added
- Kotlin Multiplatform architecture with separate Android and desktop entry-point modules.
- Shared Compose UI and game state.
- Classic and Lizard–Spock rules.
- CPU and same-device two-player modes.
- Multiple match formats, local stats, recent history, settings, achievements, and onboarding.
- Versioned schema-v1 offline backup and restore for settings, aggregate stats, and recent history.
- Backup validation that rejects malformed or unsupported data before repository writes begin.
- Compose Multiplatform string-resource localization with English fallback and Hindi translations.
- Localization catalog parity and placeholder validation in local verification scripts and CI.
- Shared Compose UI automation covering onboarding, navigation, gameplay, settings persistence, and backup dialogs.
- Stable semantic UI tags that keep tests independent from translated labels.
- Android device-test configuration and a dedicated test-host activity.
- Android adaptive icon assets and project splash artwork.
- Optional Rust rules engine.
- CI, CodeQL, dependency updates, contribution, privacy, security, validation, release, testing, backup, localization, and UI-testing documentation.

### Changed
- Moved shared UI copy, dynamic game labels, achievement text, and backup validation messages into localized resources.
- Made achievement and local-turn state presentation-neutral so the UI owns localized copy.
- Added an `ArenaStore` persistence boundary so tests can use isolated in-memory data while production keeps the existing platform store.
- Expanded local and CI verification with desktop UI-test execution and Android device-test APK assembly.

### Fixed
- Aligned Android builds and documentation with stable compile/target SDK 36.
- Updated the Android Kotlin Multiplatform DSL for Android Gradle Plugin 9.
- Removed obsolete Compose tooling accessors from the Android application module.
- Corrected Kotlin source, test, and entry-point packages whose leading `in` segment must be escaped in Kotlin syntax.
- Added the Material 3 opt-in required by the top app bar.
- Modernized Android SDK setup and current GitHub Actions/CodeQL workflow versions.

### Verified
- Shared Kotlin tests pass.
- Android debug assembly passes.
- Desktop JVM compilation passes.
- Rust engine tests pass.
- CodeQL Kotlin/Java analysis passes.
- Localization catalogs expose matching keys and formatting placeholders.
- Desktop shared UI tests are included in CI.
- Android shared UI instrumentation tests compile into a device-test APK in CI.

## [1.0.0] - 2026-08-19

Initial production-ready repository baseline.
