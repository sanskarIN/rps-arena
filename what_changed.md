# What Changed

## 2026-08-19 — Complete repository baseline

The repository was initialized as a public MIT-licensed RPS Arena project and completed through a dedicated build-validation audit.

### Product implementation

- Added Kotlin Multiplatform + Compose Multiplatform architecture using separate `shared`, `androidApp`, and `desktopApp` modules.
- Added Android and desktop runnable entry points.
- Added classic Rock–Paper–Scissors and optional Lizard–Spock rules.
- Added deterministic seeded CPU logic with Easy, Normal, and Expert presets.
- Added same-device two-player pass-and-play.
- Added Best-of-3, Best-of-5, Endless, Streak, and Tournament configurations.
- Added offline settings, aggregate stats, recent history, achievements, and onboarding.
- Added light/dark/system theme controls and a reduced-motion preference.
- Added Android launcher artwork and repository logo/splash artwork.
- Added optional standalone Rust rules engine with unit tests.

### Engineering and quality

- Added shared rules, CPU, and persistence-codec unit tests.
- Added Android API 26+ support with compile/target SDK 36, using the stable Android SDK available to reproducible CI runners.
- Added Kotlin 2.4.10, Compose Multiplatform 1.11.0, Android Gradle Plugin 9.3.0, and CI-pinned Gradle 9.5.1.
- Added repository hygiene, CI/security automation, dependency updates, verification scripts, issue forms, and pull-request templates.
- Updated the Android Kotlin Multiplatform DSL for the current Android Gradle Plugin.
- Removed obsolete Compose tooling accessors that blocked the Android build.
- Escaped the Kotlin keyword package segment as `` `in`.sanskar... `` in source, tests, and app entry points while preserving the canonical Android/JVM package name `in.sanskar...`.
- Added the required Material 3 experimental opt-in for the top app bar.
- Modernized CI to current checkout/setup actions, Android SDK setup, CodeQL v4, and concurrency cancellation for stale branch runs.

### Documentation and governance

- Added README, MIT license, privacy policy, security policy, support guide, contributing guide, code of conduct, changelog, roadmap, architecture, validation, testing, and release docs.
- Added Buy Me a Coffee funding metadata and highlighted `https://buymeacoffee.com/sanskarIN` in project documentation.
- Added business/support contacts and the required “Made by the Sanskar” credit.
- Aligned README and contributor setup documentation with Android SDK 36.

### Commit email note

The repository documents `sanskarin@outlook.in` as the owner commit email in `.mailmap` and `CONTRIBUTING.md`. GitHub connector/API commits use the identity attached to the authenticated GitHub integration and do not expose an author-email override field; local Git commits should use the documented email.

### Validation audit

Validation was performed on pull request #9 (`validation/build-audit`) and merged into `main` with a merge commit so the focused commits remain preserved.

The final audited head passed all required checks before merge:

- shared Kotlin tests: **passed**;
- Android debug assembly: **passed**;
- desktop JVM classes: **passed**;
- optional Rust engine tests: **passed**;
- CodeQL Kotlin/Java build and analysis: **passed**.

The build audit also fixed the Android SDK setup, stable SDK target, AGP 9 KMP DSL, obsolete Compose accessors, Kotlin package-keyword syntax, and Material 3 opt-in issues discovered by CI. The resulting production baseline was merged only after CI and CodeQL completed successfully.

### Repository checkpoint

- Validation PR: `#9` — merged.
- Validated PR head: `4c2e93330055986d6b87ab002a97b7929c5a2275`.
- Validation merge commit: `4b19247605ce7a94a8e6c819a63f6cd300d00d94`.
- Default branch: `main`.
- License: MIT.
- Privacy default: offline-first, no account, no analytics SDK, no ads SDK, and no Android internet permission.
