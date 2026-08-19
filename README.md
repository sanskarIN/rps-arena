# RPS Arena

<p align="center">
  <img src="assets/logo.svg" alt="RPS Arena logo" width="160" />
</p>

<p align="center"><strong>Offline-first Rock Paper Scissors for Android and desktop — deterministic CPU challenges, local two-player play, timers, backups, and optional Lizard–Spock rules.</strong></p>

<p align="center">
  <a href="https://github.com/sanskarIN/rps-arena/actions"><img alt="CI" src="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <a href="https://buymeacoffee.com/sanskarIN"><img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-support-FFDD00?logo=buymeacoffee&logoColor=000000"></a>
</p>

## Preview

<p align="center">
  <img src="assets/splash.svg" alt="RPS Arena splash artwork showing the project branding" width="520" />
</p>

Real device screenshots are release artifacts rather than fabricated mockups. The editable repository artwork remains in [`assets/`](assets/).

## Highlights

- Classic Rock–Paper–Scissors plus optional Rock–Paper–Scissors–Lizard–Spock.
- Player vs CPU and same-device two-player pass-and-play.
- Easy, Normal, and Expert CPU presets with transparent, local behavior.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament modes.
- Replayable deterministic CPU challenges with an editable integer seed.
- Optional 5/10/20/30/60-second round timers with explicit timeout scoring.
- Offline aggregate statistics, recent 10-round W/L/D trends, history, achievements, settings, and onboarding.
- Versioned local backup/import for settings, statistics, and recent history.
- Local player-name preference and English/Hindi core UI catalogs.
- Light/dark/system theme options and reduced-motion result behavior.
- Transport-neutral private-room multiplayer architecture with a deterministic two-player in-memory reference adapter.
- Android + desktop from a Kotlin Multiplatform/Compose Multiplatform codebase.
- Optional standalone Rust rules engine for experimentation.
- No account, analytics SDK, ads SDK, cloud model, or Android internet permission in the primary app.

## Supported platforms

| Platform | Status | Minimum / runtime |
|---|---|---|
| Android | Primary | API 26+; compile/target SDK 36 |
| Windows | Desktop | JVM 17 / Compose Desktop |
| Linux | Desktop | JVM 17 / Compose Desktop |
| macOS | Desktop | JVM 17 / Compose Desktop |
| iOS | Architecture-compatible future evaluation | Not part of the current release gate |

## Tech stack

- Kotlin 2.4.10
- Compose Multiplatform 1.11.0
- Kotlin Coroutines 1.10.2
- Android Gradle Plugin 9.3.0
- Gradle 9.5.1
- Android API 26+ / compile + target SDK 36
- Optional Rust 2024 edition rules engine

## Project structure

```text
androidApp/   Android entry point and packaging
desktopApp/   Desktop entry point and native packaging
shared/       Shared model, engine, state, persistence, networking contracts, UI, tests
rust-engine/  Optional standalone Rust rules mirror
assets/       Logo and splash artwork
docs/         Setup, architecture, testing, accessibility, performance and release docs
scripts/      Repository formatting/version verification helpers
```

## Quick start

Requirements: JDK 17+, Gradle 9.5.1, Android SDK 36 for Android, and a supported desktop OS.

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
gradle :shared:allTests
gradle :desktopApp:run
```

Android debug build:

```bash
gradle :androidApp:assembleDebug
```

Full setup: [`docs/setup.md`](docs/setup.md)

Development workflow: [`docs/development.md`](docs/development.md)

## CPU difficulty transparency

- **Easy:** random valid gesture.
- **Normal:** mostly random; after enough history it can counter the player's latest gesture.
- **Expert:** after enough history it estimates the player's most frequent allowed gesture and usually counters it while retaining randomness.

The CPU does not use internet services or hidden machine-learning models. Given the same seed, difficulty, ruleset, and player-move history, its random sequence is reproducible.

## Timed matches

Round timers are optional and can be disabled. In CPU mode, a player timeout awards the round to the CPU. In local two-player mode, the currently choosing player loses the timed turn. Timeout outcomes are typed, displayed, stored in history, and included in aggregate statistics.

## Backup and restore

Settings can prepare a plain-text, versioned local backup beginning with:

```text
RPS_ARENA_BACKUP|1
```

The importer validates size, record count, record types, duplicates, settings values, statistics invariants, and history bounds before replacing local state. Keep exported backup text private if a local player name or gameplay history is sensitive to you.

## Private-room architecture

The shared module contains `PrivateRoomGateway`/`PrivateRoomSession` transport contracts plus an in-memory reference implementation for deterministic testing and development. This is deliberately not a hidden network feature: the current primary Android app still declares no internet permission. A future LAN adapter must remain explicit and optional.

## Testing and quality gates

```bash
python3 scripts/check_format.py
python3 scripts/check_version.py
gradle :shared:allTests
gradle :androidApp:lintDebug
gradle :androidApp:assembleDebug
gradle :desktopApp:classes
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

CI enforces formatting, synchronized versions, shared tests, Android lint/build, desktop compilation, Rust tests, and CodeQL security analysis. See [`docs/testing.md`](docs/testing.md) and [`docs/validation.md`](docs/validation.md).

## Architecture

The primary flow is:

```text
Compose UI -> ArenaState -> RulesEngine / CpuStrategy
                       -> ArenaRepository -> PlatformStore
                       -> optional PrivateRoomGateway boundary
```

See [`docs/architecture.md`](docs/architecture.md) and [`docs/adr/`](docs/adr/).

## Accessibility

RPS Arena uses visible text labels, large gesture targets, text-based outcome/timer feedback, optional timers, light/dark/system themes, and reduced-motion behavior. Manual keyboard/TalkBack/text-scaling review steps are documented in [`docs/accessibility.md`](docs/accessibility.md).

## Privacy and security

RPS Arena is offline-first. Local storage contains preferences, aggregate statistics, and up to 30 recent round summaries. The primary Android app has no internet permission. See [`PRIVACY.md`](PRIVACY.md) and [`SECURITY.md`](SECURITY.md).

## Release

Version 1.1.0 is configured for Android and desktop. Tagged releases can build unsigned/public Android, Linux desktop, and Rust package artifacts with SHA-256 checksums. Store signing/notarization requires private credentials supplied outside the repository. See [`docs/release.md`](docs/release.md).

## Roadmap

See [`ROADMAP.md`](ROADMAP.md). Future networking and signing work must remain optional and credential-safe.

## Contributing

Read [`CONTRIBUTING.md`](CONTRIBUTING.md), [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md), and [`SECURITY.md`](SECURITY.md). The documented owner commit email is `sanskarin@outlook.in`.

## Support and contact

- Business: `sanskarin@outlook.in`
- Business: `sanskarin.business@gmail.com`
- Support: `supportramsandesh@gmail.com`
- GitHub: <https://github.com/sanskarIN>
- Repository: <https://github.com/sanskarIN/rps-arena>
- **Buy Me a Coffee:** <https://buymeacoffee.com/sanskarIN>

[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-sanskarIN-FFDD00?logo=buy-me-a-coffee&logoColor=000000)](https://buymeacoffee.com/sanskarIN)

## License

MIT. See [`LICENSE`](LICENSE).

**Made by the Sanskar.**
