# RPS Arena

<p align="center">
  <img src="assets/logo.svg" alt="RPS Arena logo" width="160" />
</p>

<p align="center"><strong>Offline-first Rock Paper Scissors across Android, iPhone/iPad, Windows, Linux, macOS, and the Web — deterministic CPU challenges, local two-player play, timers, backups, and optional Lizard–Spock rules.</strong></p>

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
- Shared Kotlin Multiplatform/Compose Multiplatform UI and business logic across Android, iOS/iPadOS, desktop, and Web.
- JVM desktop application for Windows, Linux, and macOS.
- Browser compatibility build using Kotlin/Wasm plus Kotlin/JS fallback output.
- Platform-local persistence through SharedPreferences, NSUserDefaults, Java Preferences, or browser localStorage.
- Optional standalone Rust rules engine for experimentation.
- No account, analytics SDK, ads SDK, cloud model, or mandatory gameplay backend.
- Android automatic backup disabled; local SharedPreferences are excluded from cloud/device-transfer backup policy.
- No-op-by-default structured logger with sensitive-field redaction for any future opt-in diagnostic sink.
- CI-enforced formatting, docs links/file coverage, committed-secret patterns, Android privacy, cross-platform versions, shared tests, Android/Desktop/Web builds, iOS simulator validation, and Rust tests.
- Separate dependency-review/security workflow plus CodeQL static analysis.
- Exhaustive repository documentation with CI-enforced coverage for every Git-tracked file.

## Supported platforms

| Platform | Status | Minimum / runtime |
|---|---|---|
| Android | Supported / primary mobile | API 26+; compile/target SDK 36 |
| iPhone / iPad | Supported | iOS/iPadOS 15+; Kotlin/Native + SwiftUI host |
| Windows | Supported desktop | JVM 17 / Compose Desktop |
| Linux | Supported desktop | JVM 17 / Compose Desktop |
| macOS | Supported desktop | JVM 17 / Compose Desktop |
| Web | Supported browser target | Kotlin/Wasm + Kotlin/JS compatibility distribution |

The Web target follows Compose Multiplatform's upstream Web stability level. Browser/runtime limitations are documented in [`docs/web-platform.md`](docs/web-platform.md).

## Tech stack

- Kotlin 2.4.10
- Compose Multiplatform 1.11.0
- Kotlin Coroutines 1.10.2
- Android Gradle Plugin 9.3.0
- Gradle 9.5.1
- Android API 26+ / compile + target SDK 36
- Kotlin/Native iOS device + Apple-silicon simulator frameworks
- Kotlin/Wasm + Kotlin/JS browser targets
- SwiftUI host for iPhone/iPad
- Optional Rust 2024 edition rules engine

These are the repository's validated project baselines, not a claim that each number is globally the newest available version.

## Project structure

```text
androidApp/   Android entry point, manifest, adaptive icon, backup/privacy policy, packaging
iosApp/       SwiftUI/Xcode iPhone + iPad host for the shared Compose framework
desktopApp/   Windows/Linux/macOS JVM desktop entry point and native packaging
webApp/       Kotlin/Wasm + Kotlin/JS browser application and compatibility distribution
shared/       Shared model, engine, state, persistence, logging, networking contracts, UI, tests, platform adapters
rust-engine/  Optional standalone Rust rules mirror
assets/       Editable logo and splash artwork
docs/         Deep architecture/platform/build/testing/maintenance/file documentation
scripts/      Source/security/privacy/version/documentation/full verification helpers
.github/      ownership, CI/security/CodeQL/release, Dependabot, issue/PR/funding configuration
gradle/       Version catalog
```

## Quick start

Base requirements: JDK 17, a local Gradle compatible with the validated 9.5.1 baseline, Python 3 for repository checks, and the tooling for the platform you intend to build. Android requires Android SDK 36; iOS/iPadOS requires macOS + Xcode; Web requires a supported modern browser for runtime testing.

The current repository does **not** track a Gradle Wrapper, so commands use `gradle` rather than `./gradlew`.

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
```

Android debug build:

```bash
gradle :androidApp:assembleDebug --stacktrace
```

Desktop run:

```bash
gradle :desktopApp:run
```

Web development run (Wasm):

```bash
gradle :webApp:wasmJsBrowserDevelopmentRun --stacktrace
```

Web production compatibility distribution:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

iOS simulator framework on macOS:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

Then open `iosApp/iosApp.xcodeproj` in Xcode, or use the command-line flow documented in [`docs/ios-platform.md`](docs/ios-platform.md).

Full setup: [`docs/setup.md`](docs/setup.md)

Deep tool installation/upgrade guidance: [`docs/toolchain.md`](docs/toolchain.md)

Command meanings: [`docs/command-reference.md`](docs/command-reference.md)

Development workflow: [`docs/development.md`](docs/development.md)

## Complete documentation

Start with the role-based [`docs/documentation-index.md`](docs/documentation-index.md).

Deep references include:

- [`docs/build-system.md`](docs/build-system.md) — Gradle modules, KMP targets, version catalog, source sets, tasks, no-wrapper setup;
- [`docs/domain-and-gameplay.md`](docs/domain-and-gameplay.md) — models, rules, CPU probabilities, state machine, timers, scoring, invariants;
- [`docs/storage-and-backup.md`](docs/storage-and-backup.md) — exact keys, codecs, migration, history grammar, explicit backup schema/escaping/limits;
- [`docs/localization.md`](docs/localization.md) — English/Hindi catalogs, canonical vs localized data, adding languages;
- [`docs/private-room-protocol.md`](docs/private-room-protocol.md) — current no-network room contracts and future LAN security/fairness requirements;
- [`docs/android-platform.md`](docs/android-platform.md) — every Android app/resource/storage/backup-policy file;
- [`docs/ios-platform.md`](docs/ios-platform.md) — iPhone/iPad Kotlin/Native, SwiftUI/Xcode, persistence, CI, signing boundaries;
- [`docs/desktop-platform.md`](docs/desktop-platform.md) — Windows/Linux/macOS launcher/build/storage/native packaging;
- [`docs/web-platform.md`](docs/web-platform.md) — JS/Wasm compatibility targets, browser storage, run/build/deploy paths;
- [`docs/rust-engine.md`](docs/rust-engine.md) — every optional Rust crate file and parity policy;
- [`docs/test-catalog.md`](docs/test-catalog.md) — every tracked automated Kotlin/Compose test and its regression responsibility;
- [`docs/ci-cd.md`](docs/ci-cd.md) — every `.github` ownership/automation/configuration file;
- [`docs/maintenance.md`](docs/maintenance.md) — long-term maintenance/change/release playbook;
- [`docs/glossary.md`](docs/glossary.md) — project/build/platform/security terminology;
- [`docs/branding-assets.md`](docs/branding-assets.md) — SVG/adaptive-icon/theme ownership;
- [`docs/repository-file-reference.md`](docs/repository-file-reference.md) — exhaustive every-tracked-file reference.

`python3 scripts/check_docs_coverage.py` uses `git ls-files` and fails CI if any tracked path is absent from the exhaustive file reference.

## CPU difficulty transparency

- **Easy:** random valid gesture.
- **Normal:** mostly random; after enough history it can counter the player's latest gesture.
- **Expert:** after enough history it estimates the player's most frequent allowed gesture and usually counters it while retaining randomness.

The CPU does not use internet services or hidden machine-learning models. Given the same seed, difficulty, ruleset, and player-move history, its random sequence is reproducible. Exact thresholds are documented in [`docs/domain-and-gameplay.md`](docs/domain-and-gameplay.md).

## Timed matches

Round timers are optional and can be disabled. In CPU mode, a player timeout awards the round to the CPU. In local two-player mode, the currently choosing player loses the timed turn. Timeout outcomes are typed, displayed, stored in history, and included in aggregate statistics.

## Backup and restore

Settings can prepare a plain-text, versioned local backup beginning with:

```text
RPS_ARENA_BACKUP|1
```

The importer validates size, record count, record types, duplicates, settings values, statistics invariants, and history bounds before replacing local state. Keep exported backup text private if a local player name or gameplay history is sensitive to you. Exact grammar is documented in [`docs/storage-and-backup.md`](docs/storage-and-backup.md).

Platform-local stores differ, but the app-level backup schema is shared. Android automatic backup is disabled and SharedPreferences are excluded from legacy/cloud/device-transfer policy. The in-app backup text is the explicit, user-controlled portability path.

## Private-room architecture

The shared module contains `PrivateRoomGateway`/`PrivateRoomSession` transport contracts plus an in-memory reference implementation for deterministic testing and development. This is deliberately not a hidden network feature: no production LAN adapter is shipped. A future LAN adapter must remain explicit and optional on every platform.

The current room contract is **not** production LAN multiplayer. See [`docs/private-room-protocol.md`](docs/private-room-protocol.md) for that boundary and future wire-protocol/security requirements.

## Testing and quality gates

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

On macOS, iOS validation additionally links the simulator framework and builds the Xcode host without code signing. Primary CI runs these platform gates on the appropriate Ubuntu/macOS runners. The focused Security workflow independently rechecks secret/privacy contracts and performs pull-request dependency review. CodeQL separately performs Kotlin/Java security analysis. Tagged/manual release preflight repeats the fast source gates before packaging. See [`docs/testing.md`](docs/testing.md), [`docs/test-catalog.md`](docs/test-catalog.md), [`docs/validation.md`](docs/validation.md), and [`docs/ci-cd.md`](docs/ci-cd.md).

## Architecture

The primary flow is:

```text
Platform host -> shared Compose UI -> ArenaState -> RulesEngine / CpuStrategy
                                       -> ArenaRepository -> PlatformStore
                                       -> optional PrivateRoomGateway boundary
shared utilities                     -> SafeLogger (no-op sink by default)
```

Platform hosts are intentionally thin: Android Activity, iOS SwiftUI/Xcode bridge, JVM desktop Window, or Web ComposeViewport.

See [`docs/architecture.md`](docs/architecture.md), [`docs/build-system.md`](docs/build-system.md), and [`docs/adr/`](docs/adr/).

## Accessibility

RPS Arena uses visible text labels, large gesture targets, text-based outcome/timer feedback, optional timers, light/dark/system themes, and reduced-motion behavior. Manual keyboard, TalkBack, VoiceOver, browser keyboard/focus, text-scaling, and contrast review expectations are documented in [`docs/accessibility.md`](docs/accessibility.md) and the platform guides.

## Privacy and security

RPS Arena is offline-first. Local storage contains preferences, aggregate statistics, and up to 30 recent round summaries. Android uses app-private SharedPreferences, iOS uses NSUserDefaults, desktop uses Java Preferences, and Web uses origin-local `localStorage`. The primary Android app has no internet permission, disables automatic backup, and excludes SharedPreferences from cloud/device-transfer policy. Repository checks fail if those Android privacy invariants regress or recognizable high-confidence credential material is committed. See [`PRIVACY.md`](PRIVACY.md), [`SECURITY.md`](SECURITY.md), and the platform/storage guides.

## Release

Version 2.5.8 is synchronized across Android, desktop, iOS, and shared About metadata. Android and iOS use numeric build `20508`. Tagged release automation can build unsigned/public Android, Linux desktop, JS+Wasm Web compatibility, iOS framework, and Rust artifacts with SHA-256 checksums. Signed Android stores, signed/notarized desktop installers, and App Store/TestFlight artifacts require private credentials supplied outside the public repository. See [`docs/release.md`](docs/release.md) and [`docs/ci-cd.md`](docs/ci-cd.md).

## Roadmap

See [`ROADMAP.md`](ROADMAP.md). Future networking and signed-store distribution work must remain optional and credential-safe.

## Contributing

Read [`CONTRIBUTING.md`](CONTRIBUTING.md), [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md), [`SECURITY.md`](SECURITY.md), and [`docs/maintenance.md`](docs/maintenance.md). The documented owner commit email is `sanskarin@outlook.in`.

Any new tracked file must also be documented in [`docs/repository-file-reference.md`](docs/repository-file-reference.md). `.github/CODEOWNERS` routes maintainer review ownership; repository rules can make code-owner approval mandatory.

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
