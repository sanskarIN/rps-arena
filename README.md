# RPS Arena

<p align="center">
  <img src="assets/logo.svg" alt="RPS Arena logo" width="160" />
</p>

<p align="center"><strong>A polished, privacy-first Rock–Paper–Scissors arena for Android and desktop.</strong></p>

<p align="center">
  <a href="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <a href="https://buymeacoffee.com/sanskarIN"><img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-sanskarIN-FFDD00?logo=buy-me-a-coffee&logoColor=000000"></a>
</p>

RPS Arena takes a tiny classic game seriously: one shared deterministic rules engine, transparent CPU strategies, same-device multiplayer, local stats and achievements, accessibility preferences, backup/restore, and no mandatory account or cloud service.

## Screenshots

Real release screenshots are captured only from verified Android and desktop builds. Until the first release candidate is visually audited, see the UI source in `shared/src/commonMain/kotlin/dev/sanskar/rpsarena/ui/` and the editable brand artwork in `assets/logo.svg` rather than presenting mockups as product captures.

## Features

- Classic Rock–Paper–Scissors and Rock–Paper–Scissors–Lizard–Spock.
- Player vs CPU and private same-device two-player pass-and-play.
- Easy, Normal, and Expert CPU presets with documented, local behavior.
- Replayable seeded CPU sequences for debugging and challenge reproduction.
- Best-of-3, Best-of-5, Tournament, Endless, and Streak modes.
- Configurable 0–60 second round timer.
- Lifetime local wins/losses/draws, win rate, current/best streak, and achievements.
- Latest 30 round summaries stored locally.
- Light, dark, and system themes.
- Reduced-motion preference and semantic gesture labels.
- Plain-text versioned backup/import with safe invalid-data handling.
- Responsive phone/desktop layouts.
- About/support/funding screen with non-intrusive project links.
- No required account, ads SDK, analytics SDK, backend, or Android internet permission.

## Supported platforms

| Platform | Status | Notes |
|---|---|---|
| Android | Primary | API 26+, target/compile API 37 |
| Windows | Primary desktop | Compose Desktop JVM application; MSI packaging configured |
| macOS | Primary desktop | Compose Desktop JVM application; DMG packaging configured |
| Linux | Primary desktop | Compose Desktop JVM application; DEB packaging configured |
| iOS | Future | Optional target only after platform testing/accessibility can be maintained |

## Tech stack

- Kotlin **2.4.10**
- Compose Multiplatform **1.11.1**
- Android Gradle Plugin **9.1.0**
- Gradle **9.5.0** in CI/local documented baseline
- AndroidX Activity Compose **1.13.0**
- kotlinx.coroutines **1.11.0**
- JDK **17+**
- Android API **26+**, compile/target **37**

Dependency versions are centralized in `gradle/libs.versions.toml`.

## Project structure

```text
androidApp/    Android entry point, manifest, storage adapter, packaging
desktopApp/    Desktop entry point, storage adapter, native packaging
shared/        Domain engine, CPU, persistence, state, shared Compose UI, tests
assets/        Editable project logo artwork
docs/          Architecture, setup, testing, release, accessibility, ADRs
.github/       CI, CodeQL, Dependabot, funding, issue/PR templates
```

An optional Rust mirror and optional LAN/private-room implementation are intentionally deferred until they add measurable value; neither is required for v1 core play.

## Quick start

Requirements: Git, JDK 17+, Gradle 9.5.0, plus Android SDK 37 for Android builds.

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
gradle :shared:desktopTest
gradle :desktopApp:run
```

Android:

```bash
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

See [Setup](docs/setup.md) and [Development](docs/development.md) for the full environment guide.

## Testing

The shared automated suites cover:

- classic and Lizard–Spock rule relationships;
- best-of match thresholds;
- deterministic CPU behavior and variant-safe output;
- local persistence, malformed-data fallback, and backup import/export;
- private pass-and-play handoff, scoring, match completion, and restart semantics.

Run:

```bash
gradle :shared:desktopTest
gradle :shared:compileKotlinDesktop
gradle :desktopApp:compileKotlin
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

CI performs compilation, tests, Android build/lint, and CodeQL security analysis. See [Testing](docs/testing.md).

## CPU difficulty transparency

- **Easy:** chooses a random valid gesture.
- **Normal:** usually random; has a 30% chance to counter the player's latest gesture.
- **Expert:** after at least three player moves, has a 70% chance to counter the most frequent observed gesture; otherwise random.

Every strategy retains randomness and runs entirely on-device. There is no hidden machine-learning model or remote decision service.

## Architecture

RPS Arena is a modular offline-first application:

```text
Android/Desktop entry point
          ↓
     Shared Compose UI
          ↓
      AppController
       ↙       ↘
GameRules/CPU   AppRepository
                    ↓
               KeyValueStore
             ↙              ↘
 SharedPreferences       Java Preferences
```

Game rules stay independent from UI and platform APIs. Platform modules provide only entry-point and persistence integration. See [Architecture](docs/architecture.md) and [ADR 0001](docs/adr/0001-offline-first-multiplatform-architecture.md).

## Privacy and security

RPS Arena stores settings, aggregate statistics, and up to 30 recent round summaries locally. Android requests no internet permission. External repository/support/funding links are opened by the operating system only after the user selects them.

- [Privacy policy](PRIVACY.md)
- [Security policy and responsible disclosure](SECURITY.md)
- [Accessibility](docs/accessibility.md)
- [Performance](docs/performance.md)

Never commit API keys, signing secrets, credentials, personal user data, private endpoints, or generated secrets. The v1 application requires no runtime environment secrets.

## Build and release

Desktop native packaging is configured for MSI, DMG, and DEB; native packages are built on their corresponding operating systems. Android CI verifies a debug APK. Distribution signing material must remain outside the repository.

See [Release](docs/release.md) and `.github/workflows/release.yml`.

## Documentation

- [Setup](docs/setup.md)
- [Development](docs/development.md)
- [Testing](docs/testing.md)
- [Architecture](docs/architecture.md)
- [Accessibility](docs/accessibility.md)
- [Performance](docs/performance.md)
- [Release](docs/release.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Roadmap](ROADMAP.md)
- [Changelog](CHANGELOG.md)
- [Work handoff](what_changed.md)

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and [SECURITY.md](SECURITY.md). Use small, meaningful commits and add regression coverage for fixes.

Project-owner Git commits use `sanskarin@outlook.in`.

## Support and contact

- Business: `sanskarin@outlook.in`
- Business: `sanskarin.business@gmail.com`
- Support: `supportramsandesh@gmail.com`
- GitHub: <https://github.com/sanskarIN>
- Repository: <https://github.com/sanskarIN/rps-arena>
- **Buy Me a Coffee:** <https://buymeacoffee.com/sanskarIN>

[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-sanskarIN-FFDD00?logo=buy-me-a-coffee&logoColor=000000)](https://buymeacoffee.com/sanskarIN)

Funding is optional; every game feature remains usable without donating.

## License

RPS Arena is open source under the [MIT License](LICENSE).

**Made by the Sanskar.**
