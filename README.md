# RPS Arena

<p align="center">
  <img src="assets/logo.svg" alt="RPS Arena logo" width="160" />
</p>

<p align="center"><strong>A polished, privacy-first Rock Paper Scissors arena for Android and desktop.</strong></p>

<p align="center">
  <a href="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <a href="https://buymeacoffee.com/sanskarIN"><img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-sanskarIN-FFDD00?logo=buy-me-a-coffee&logoColor=000000"></a>
</p>

RPS Arena turns the classic game into a complete offline-first portfolio application: one canonical rules engine, transparent CPU strategies, same-device private play, local profiles, persisted match setup, seeded challenges, timers, history, statistics, recent trends, achievements, accessibility preferences, validated local backup/restore, and professional release/security automation.

## Screenshots

Real Android and desktop screenshots will be added only from a verified release-candidate build. The repository intentionally does not present fabricated mockups as product captures. Editable branding is available in `assets/`.

## Features

- Classic Rock–Paper–Scissors and Rock–Paper–Scissors–Lizard–Spock.
- Player vs CPU and same-device two-player pass-and-play.
- Easy, Normal, and Expert CPU presets with documented local behavior.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament modes.
- Deterministic seeded CPU matches for reproducible challenges and debugging.
- Optional 5/10/15/30/60-second turn timers with deterministic timeout moves.
- Persisted match configuration across launches.
- Up to six local-only player profiles with validated display names and active-profile selection.
- Local lifetime wins, losses, draws, streaks, win rate, and achievements.
- Up to 30 recent round summaries stored locally.
- Recent W/L/D trend with non-color-only labels and decisive win rate.
- Versioned plain-text V2 backup/restore for settings, profiles, stats, match setup, and history.
- Backward-compatible V1 backup migration plus a non-mutating preview before import.
- Clear-history with one-step undo and confirmed full local-data reset controls.
- Polished onboarding with no forced sign-in.
- Light, dark, and system themes plus reduced-motion result behavior.
- Semantic gesture/trend labels, large touch targets, keyboard-compatible Compose controls, and non-color-only results.
- Clickable repository, funding, business, and support links in About.
- Android and desktop from a Kotlin/Compose Multiplatform codebase.
- Optional standalone Rust rules mirror with formatting, lint, test, and benchmark support.
- Tested private-room/LAN protocol boundary for future opt-in transport work, with no production network dependency in v1.
- Structured redacting local logging.
- CI, CodeQL, dependency review, committed-secret scanning, Dependabot, docs validation, and release artifact automation.
- No account, analytics SDK, ads SDK, cloud dependency, or Android internet permission.

## Supported platforms

| Platform | Status | Notes |
|---|---|---|
| Android | Primary | API 26+, compile/target API 36 |
| Windows | Primary desktop | Compose Desktop JVM app; MSI packaging configured |
| macOS | Primary desktop | Compose Desktop JVM app; DMG packaging configured |
| Linux | Primary desktop | Compose Desktop JVM app; DEB packaging configured |
| iOS | Future | Deferred until native packaging and accessibility can be verified properly |

## Tech stack

- Kotlin **2.4.10**
- Compose Multiplatform **1.11.0**
- Android Gradle Plugin **9.1.0**
- Gradle **9.5.0** verification baseline
- AndroidX Activity Compose **1.13.0**
- kotlinx.coroutines **1.11.0**
- JDK **17+**
- Android API **26+**, compile/target API **36**
- Optional Rust **2024 edition** rules mirror

Dependency versions are centralized in `gradle/libs.versions.toml`.

## Project structure

```text
androidApp/   Android entry point, manifest, resources, packaging
desktopApp/   Desktop entry point and native packaging
shared/       Shared engine, models, state, persistence, UI, tests
rust-engine/  Optional standalone Rust rules mirror and benchmarks
assets/       Editable logo and splash artwork
docs/         Architecture, setup, testing, accessibility, release, ADRs
scripts/      Documentation, security, and local verification helpers
.github/      CI, CodeQL, security/release automation, Dependabot, templates, funding
```

## Quick start

Requirements: Git, JDK 17+, Gradle 9.5.0, plus Android SDK 36 for Android builds.

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
gradle --no-daemon :shared:allTests
gradle :desktopApp:run
```

Android verification:

```bash
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
```

See [Setup](docs/setup.md) and [Development](docs/development.md) for the full environment guide.

## Testing

Shared automated coverage includes:

- classic and Lizard–Spock rule relationships;
- deterministic CPU behavior;
- persistence codecs and legacy-settings migration;
- local profile validation and lifecycle;
- match-config persistence;
- V2 backup/restore, V1 migration, preview, size bounds, and atomic malformed-backup rejection;
- timed auto-moves and local two-player timer handoff;
- history clear/undo and data reset behavior;
- recent-history W/L/D trend derivation;
- private-room protocol validation;
- Rust/Kotlin rule-contract checks.

Primary CI commands:

```bash
gradle --no-daemon :shared:compileKotlinDesktop
gradle --no-daemon :shared:allTests
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
gradle --no-daemon :desktopApp:classes
python scripts/check_docs_links.py
python scripts/check_for_secrets.py
```

The Rust job runs `cargo fmt --check`, Clippy with warnings denied, and the full Rust test suite. CodeQL analyzes Java/Kotlin source independently from Android SDK installation. Pull requests also run dependency review.

See [Testing](docs/testing.md).

## CPU difficulty transparency

- **Easy:** chooses a random valid gesture.
- **Normal:** mostly random and occasionally counters recent player behavior.
- **Expert:** uses local move history more aggressively while retaining randomness.

The CPU runs entirely on-device. No hidden remote model, telemetry service, or network lookup decides moves. Supplying the same seed and the same sequence of player inputs makes challenges reproducible.

## Architecture

```text
Android / Desktop entry point
            ↓
      Shared Compose UI
            ↓
         ArenaState
       ↙            ↘
Rules + CPU      ArenaRepository
                     ↓
                KeyValueStore
              ↙              ↘
   SharedPreferences      Java Preferences
```

Game rules remain independent from UI and platform APIs. Persistence is injected through a small store interface so common tests can use deterministic in-memory storage. Recent trends are derived from persisted history instead of being stored twice. The private-room protocol is a separate opt-in boundary and is not a dependency of v1 gameplay.

See [Architecture](docs/architecture.md) and the records in [docs/adr/](docs/adr/).

## Privacy and security

RPS Arena stores local profile display names, game configuration, settings, aggregate statistics, and recent history on the device. Android requests no internet permission. Backup text is intentionally readable, includes the local data the user chooses to export, and is not secret storage.

- [Privacy](PRIVACY.md)
- [Security and responsible disclosure](SECURITY.md)
- [Accessibility](docs/accessibility.md)
- [Performance](docs/performance.md)
- [Repository settings](docs/repository-settings.md)

Never commit API keys, signing material, passwords, tokens, personal user data, or private production endpoints. RPS Arena v1 requires no runtime secrets. Repository CI includes a high-confidence committed-secret scan, CodeQL, and pull-request dependency review; Dependabot covers Gradle, Cargo, and GitHub Actions updates.

## Build and release

Desktop packaging is configured for MSI, DMG, and DEB on the corresponding operating systems. Android CI verifies a debug APK; release automation builds an unsigned Android release artifact. Distribution signing credentials remain outside Git.

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
- [Repository settings](docs/repository-settings.md)
- [Roadmap](ROADMAP.md)
- [Changelog](CHANGELOG.md)
- [Work handoff](what_changed.md)

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and [SECURITY.md](SECURITY.md). Use small, meaningful commits and add regression coverage for bug fixes.

When committing locally as the project owner, configure Git with `sanskarin@outlook.in`.

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
