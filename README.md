# RPS Arena

<p align="center">
  <img src="assets/logo.svg" alt="RPS Arena logo" width="160" />
</p>

<p align="center"><strong>Offline-first Rock Paper Scissors for Android and desktop — with optional Lizard–Spock rules.</strong></p>

<p align="center">
  <a href="https://github.com/sanskarIN/rps-arena/actions"><img alt="CI" src="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <a href="https://buymeacoffee.com/sanskarIN"><img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-support-FFDD00?logo=buymeacoffee&logoColor=000000"></a>
</p>

## Highlights

- Classic Rock–Paper–Scissors plus optional Rock–Paper–Scissors–Lizard–Spock.
- Player vs CPU and same-device two-player pass-and-play.
- Easy, Normal, and Expert CPU presets with transparent, local behavior.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament modes.
- Deterministic seeded CPU matches for reproducibility.
- Offline stats, recent history, achievements, settings, and onboarding.
- Versioned offline backup and restore for settings, aggregate stats, and recent history.
- Shared Compose UI localization with English fallback and Hindi translations.
- Shared Compose UI automation for desktop and Android device-test targets.
- Light/dark/system theme options and reduced-motion preference.
- Android + desktop from a Kotlin Multiplatform/Compose Multiplatform codebase.
- Optional standalone Rust rules engine for experimentation.
- No account, analytics SDK, ads SDK, or network permission in the app.

## Tech stack

- Kotlin 2.4.10
- Compose Multiplatform 1.11.0
- Android Gradle Plugin 9.3.0
- Gradle 9.5.1
- Android API 26+ / compile + target SDK 36
- Optional Rust 2024 edition engine

## Project structure

```text
androidApp/   Android entry point and packaging
desktopApp/   Desktop entry point and native packaging
shared/       Shared game engine, state, persistence, UI, resources, tests
rust-engine/  Optional standalone Rust rules mirror
assets/       Logo and splash artwork
docs/         Architecture, backup, localization, UI testing, release and support documentation
```

## Build and verify

Requirements: JDK 17+, Gradle 9.5.1, Android SDK 36 for Android, Python 3 for catalog verification, and a supported desktop OS.

```bash
python3 scripts/verify_localizations.py
gradle :shared:allTests
gradle :shared:desktopTest
gradle :shared:assembleAndroidDeviceTest
gradle :desktopApp:run
gradle :androidApp:assembleDebug
```

For the repository-wide checks, use `./scripts/verify.sh` on Unix-like systems or `./scripts/verify.ps1` on Windows.

## CPU difficulty transparency

- **Easy:** random valid gesture.
- **Normal:** mostly random; sometimes counters the player's most recent gesture.
- **Expert:** after enough history, estimates the player's most frequent gesture and usually counters it, while retaining randomness.

The CPU does not use internet services or hidden machine-learning models.

## Backup and restore

Open **Settings → Backup & restore** to export or import a versioned text backup. Imports are fully validated before saved RPS Arena data is replaced, and the feature does not add networking or new Android permissions.

See [docs/BACKUP.md](docs/BACKUP.md) for the schema, validation rules, compatibility contract, and privacy guidance.

## Localization

The shared Compose UI uses Compose Multiplatform string resources. English is the fallback catalog and Hindi is the first additional locale. Catalog parity and placeholder safety are checked by `scripts/verify_localizations.py` and CI.

See [docs/LOCALIZATION.md](docs/LOCALIZATION.md) for translation rules, supported locale structure, validation, and the current backward-compatibility boundary for persisted history summaries.

## UI automation

Shared UI tests use stable semantic tags rather than visible text, so the same flows stay reliable across localized UI. Desktop tests execute in CI, while the Android device-test APK is assembled in CI and can be run on a connected device or emulator with `gradle :shared:connectedAndroidDeviceTest`.

See [docs/UI_TESTING.md](docs/UI_TESTING.md) for covered flows, test-store isolation, commands, and contribution guidance.

## Privacy

RPS Arena is offline-first. Game settings, aggregate stats, and up to 30 recent round summaries are saved locally. See [PRIVACY.md](PRIVACY.md).

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and [SECURITY.md](SECURITY.md). Please configure Git commits with `sanskarin@outlook.in` when contributing as the project owner.

## Support and contact

- Business: `sanskarin@outlook.in`
- Business: `sanskarin.business@gmail.com`
- Support: `supportramsandesh@gmail.com`
- GitHub: <https://github.com/sanskarIN>
- Repository: <https://github.com/sanskarIN/rps-arena>
- **Buy Me a Coffee:** <https://buymeacoffee.com/sanskarIN>

## License

MIT. See [LICENSE](LICENSE).

**Made by the Sanskar.**
