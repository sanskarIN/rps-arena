# What Changed

## 2026-08-19 — Complete repository baseline

The repository was initialized as a public MIT-licensed RPS Arena project.

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
- Added Android API 26+ / target 37 configuration.
- Added Kotlin 2.4.10, Compose Multiplatform 1.11.0, Android Gradle Plugin 9.3.0, and CI-pinned Gradle 9.5.1.
- Added repository hygiene, CI/security automation, and verification scripts.

### Documentation and governance

- Added README, MIT license, privacy policy, security policy, support guide, contributing guide, code of conduct, changelog, roadmap, architecture, testing, and release docs.
- Added Buy Me a Coffee funding metadata and highlighted `https://buymeacoffee.com/sanskarIN` in project documentation.
- Added business/support contacts and the required “Made by the Sanskar” credit.

### Commit email note

The repository documents `sanskarin@outlook.in` as the owner commit email in `.mailmap` and `CONTRIBUTING.md`. GitHub connector/API commits use the identity attached to the authenticated GitHub integration and do not expose an author-email override field; local Git commits should use the documented email.

### Validation

CI is configured to validate shared Kotlin tests, Android assembly, desktop compilation, and Rust tests. Any discovered CI issue is fixed with a focused commit and recorded in this file.
