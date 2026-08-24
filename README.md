# RPS Arena

<p align="center">
  <img src="assets/logo.svg" alt="RPS Arena logo" width="160" />
</p>

<p align="center"><strong>Offline-first Rock Paper Scissors across Android, iPhone/iPad, Windows, Linux, macOS, and the Web — deterministic CPU play, local two-player matches, explicit backups, and optional Lizard–Spock rules.</strong></p>

<p align="center">
  <a href="https://github.com/sanskarIN/rps-arena/actions"><img alt="CI" src="https://github.com/sanskarIN/rps-arena/actions/workflows/ci.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <a href="https://buymeacoffee.com/sanskarIN"><img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-support-FFDD00?logo=buymeacoffee&logoColor=000000"></a>
</p>

## Preview

<p align="center">
  <img src="assets/splash.svg" alt="RPS Arena splash artwork showing the project branding" width="520" />
</p>

Real device screenshots are release artifacts rather than fabricated mockups. Editable repository artwork remains in [`assets/`](assets/).

## Highlights

- Classic Rock–Paper–Scissors plus optional Rock–Paper–Scissors–Lizard–Spock.
- Player vs CPU and same-device two-player pass-and-play.
- Easy, Normal, and Expert CPU presets with transparent local behavior.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament modes.
- Deterministic seeded CPU behavior with the seed persisted as part of match setup.
- Persisted ruleset, opponent mode, difficulty, match mode, and seed with safe corruption fallback.
- Offline aggregate statistics, bounded recent history, achievements, settings, and onboarding.
- Versioned local backup/import for settings, statistics, and up to 30 recent history entries.
- Compose Multiplatform English/Hindi shared resources with CI-enforced catalog parity.
- Light/dark/system theme options and a reduced-motion preference.
- Stable localization-independent semantic tags for UI automation.
- Desktop Compose UI tests plus Android KMP instrumentation smoke tests.
- Transport-neutral private-room multiplayer contracts with a deterministic no-network two-player reference adapter.
- Shared Kotlin Multiplatform/Compose Multiplatform UI and business logic across Android, iOS/iPadOS, desktop, and Web.
- Platform-local persistence through SharedPreferences, NSUserDefaults, Java Preferences, or browser localStorage.
- Optional standalone Rust rules engine for experimentation and parity checks.
- No account, analytics SDK, ads SDK, cloud model, or mandatory gameplay backend.
- Android automatic backup disabled; SharedPreferences are excluded from platform cloud/device-transfer backup.
- No-op-by-default structured logger with sensitive-field redaction for any future explicit diagnostic sink.
- CI-enforced formatting, docs links/file coverage, committed-secret patterns, Android privacy, cross-platform versions, localization parity, tests, platform builds, Security checks, and CodeQL.

## Supported platforms

| Platform | Status | Minimum / runtime |
|---|---|---|
| Android | Supported / primary mobile | API 26+; compile/target SDK 36 |
| iPhone / iPad | Supported source target | Kotlin/Native + SwiftUI/Xcode host; Apple-silicon simulator validation |
| Windows | Supported desktop | JVM 17 / Compose Desktop |
| Linux | Supported desktop | JVM 17 / Compose Desktop |
| macOS | Supported desktop | JVM 17 / Compose Desktop |
| Web | Supported browser target | Kotlin/Wasm + Kotlin/JS compatibility distribution |

The Web target follows Compose Multiplatform's upstream Web stability level. Browser/runtime limitations are documented in [`docs/web-platform.md`](docs/web-platform.md). iOS simulator source validation currently targets Apple silicon because `iosX64` is not configured; see [`docs/ios-platform.md`](docs/ios-platform.md).

## Tech stack

- Kotlin 2.4.10
- Compose Multiplatform 1.11.0
- Android Gradle Plugin 9.3.0
- Gradle 9.5.1 in CI
- Android API 26+ / compile + target SDK 36
- Kotlin/Native iOS device + Apple-silicon simulator frameworks
- Kotlin/Wasm + Kotlin/JS browser targets
- SwiftUI host for iPhone/iPad
- Optional Rust 2024 edition rules engine

These are repository baselines, not a claim that every number is the newest globally available version.

## Project structure

```text
androidApp/   Android entry point, manifest, adaptive icon, privacy/backup policy
iosApp/       SwiftUI/Xcode iPhone + iPad host for the shared Compose framework
desktopApp/   Windows/Linux/macOS JVM desktop host and native packaging
webApp/       Kotlin/Wasm + Kotlin/JS browser application and compatibility distribution
shared/       Shared models, engine, state, persistence, logging, room contracts, UI, tests, platform adapters
rust-engine/  Optional standalone Rust rules mirror
assets/       Editable logo and splash artwork
docs/         Architecture/platform/build/testing/release/maintenance documentation
scripts/      Source/security/privacy/version/localization/full-verification helpers
.github/      ownership, CI/security/CodeQL/release, Dependabot, issue/PR/funding configuration
gradle/       Version catalog
```

## Quick start

Base requirements are JDK 17, an installed Gradle compatible with the validated 9.5.1 baseline, Python 3 for repository checks, and platform tooling for the target you want to build. Android requires SDK 36; iOS/iPadOS requires macOS + Xcode; Web requires a supported modern browser for runtime testing.

The repository currently does **not** track a Gradle Wrapper, so examples use `gradle` rather than `./gradlew`.

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
python3 scripts/verify_localizations.py
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

Desktop UI tests:

```bash
gradle :shared:desktopTest --stacktrace
```

Android instrumentation APK assembly:

```bash
gradle :shared:assembleAndroidDeviceTest --stacktrace
```

Web development run (Wasm):

```bash
gradle :webApp:wasmJsBrowserDevelopmentRun --stacktrace
```

Web production compatibility distribution:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

iOS simulator framework on an Apple-silicon macOS host:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

Then open `iosApp/iosApp.xcodeproj` in Xcode or use the command-line flow in [`docs/ios-platform.md`](docs/ios-platform.md).

Useful guides:

- [`docs/setup.md`](docs/setup.md)
- [`docs/toolchain.md`](docs/toolchain.md)
- [`docs/command-reference.md`](docs/command-reference.md)
- [`docs/development.md`](docs/development.md)

## Documentation

Start with [`docs/documentation-index.md`](docs/documentation-index.md). Deep references include:

- [`docs/build-system.md`](docs/build-system.md) — Gradle modules, targets, catalog, source sets and build tasks;
- [`docs/domain-and-gameplay.md`](docs/domain-and-gameplay.md) — models, rules, CPU behavior and game-state invariants;
- [`docs/storage-and-backup.md`](docs/storage-and-backup.md) — persistence keys, codecs and backup compatibility;
- [`docs/localization.md`](docs/localization.md) and [`docs/LOCALIZATION.md`](docs/LOCALIZATION.md) — localization architecture and current Compose resource catalogs;
- [`docs/UI_TESTING.md`](docs/UI_TESTING.md) — current desktop and Android UI-automation architecture;
- [`docs/private-room-protocol.md`](docs/private-room-protocol.md) — no-network room contracts and future transport boundary;
- [`docs/android-platform.md`](docs/android-platform.md) — Android app/storage/privacy details;
- [`docs/ios-platform.md`](docs/ios-platform.md) — iPhone/iPad Kotlin/Native, SwiftUI/Xcode and signing boundaries;
- [`docs/desktop-platform.md`](docs/desktop-platform.md) — Windows/Linux/macOS host and packaging;
- [`docs/web-platform.md`](docs/web-platform.md) — JS/Wasm compatibility build, storage and deployment boundaries;
- [`docs/rust-engine.md`](docs/rust-engine.md) — optional Rust crate and parity policy;
- [`docs/test-catalog.md`](docs/test-catalog.md) — automated-test responsibilities;
- [`docs/ci-cd.md`](docs/ci-cd.md) — GitHub automation and release gates;
- [`docs/maintenance.md`](docs/maintenance.md) — long-term maintenance/release workflow;
- [`docs/repository-file-reference.md`](docs/repository-file-reference.md) — canonical exhaustive tracked-file reference;
- [`docs/reconciliation-file-reference.md`](docs/reconciliation-file-reference.md) — temporary companion while v2.5.8 documentation is consolidated;
- [`docs/NEXT_VERSION.md`](docs/NEXT_VERSION.md) — gated v2.5.9 plan.

`python3 scripts/check_docs_coverage.py` uses `git ls-files` and fails if any tracked path is absent from the approved exhaustive references.

## CPU difficulty transparency

- **Easy:** random valid gesture.
- **Normal:** mostly random; after enough history it can counter the player's latest gesture.
- **Expert:** after enough history it estimates the player's most frequent allowed gesture and usually counters it while retaining randomness.

The CPU uses no internet service or hidden machine-learning model. Given the same seed, difficulty, ruleset, and move history, its random sequence is reproducible. The seed is persisted with match configuration; a dedicated visible seed editor is not yet part of the reconciled UI.

## Match configuration persistence

The shared repository persists:

```text
variant | opponent | difficulty | match mode | seed
```

under `match_config_v1`. Invalid or malformed stored values fall back to `MatchConfig()` defaults instead of partially applying corrupt state.

Round timers/timeouts existed on the pre-reconciliation branch but have **not** yet been ported onto the current Compose-resource/ArenaStore architecture. They are therefore not claimed as current v2.5.8 runtime behavior.

## Backup and restore

Settings provides an explicit plain-text versioned backup beginning with:

```text
RPSARENA_BACKUP|1
```

Schema 1 contains settings, aggregate statistics, and up to 30 recent history entries. The decoder validates the header/schema, settings, non-negative/statistical invariants, history count and record shape before import. History text is newline-sanitized and bounded.

`match_config_v1` is intentionally outside backup schema 1 during reconciliation, so changing backup contents later requires an explicit compatibility decision rather than silently altering schema 1.

Platform stores differ, but app-level backup semantics are shared. Android automatic backup is disabled and SharedPreferences are excluded from cloud/device-transfer policy; the in-app backup text is the explicit user-controlled portability path.

## Private-room architecture

The shared module contains `PrivateRoomGateway`/`PrivateRoomSession` contracts plus an in-memory deterministic reference implementation. It is deliberately **not** a hidden network feature: no real production LAN transport is shipped and normal gameplay needs no Internet permission.

See [`docs/private-room-protocol.md`](docs/private-room-protocol.md) for the current boundary and future security/fairness requirements.

## Testing and quality gates

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
python3 scripts/verify_localizations.py
gradle :shared:allTests --stacktrace
gradle :shared:desktopTest --stacktrace
gradle :shared:assembleAndroidDeviceTest --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

On macOS, CI additionally links the iOS simulator framework and builds the Xcode host without code signing. The Security workflow rechecks secret/privacy contracts and dependency review. CodeQL separately analyzes Kotlin/Java. Tagged/manual release preflight repeats release-critical gates before packaging.

## Architecture

```text
Platform host -> shared Compose UI -> ArenaState -> RulesEngine / CpuStrategy
                                       -> ArenaRepository -> ArenaStore -> PlatformStore
                                       -> optional PrivateRoomGateway boundary
shared utilities                     -> SafeLogger (no-op sink by default)
```

Platform hosts are intentionally thin: Android Activity, iOS SwiftUI/Xcode bridge, JVM desktop Window, or Web ComposeViewport.

See [`docs/architecture.md`](docs/architecture.md), [`docs/build-system.md`](docs/build-system.md), and [`docs/adr/`](docs/adr/).

## Accessibility

RPS Arena uses visible text labels, large gesture targets, light/dark/system themes, a reduced-motion preference, and localization-independent test semantics. Manual keyboard, TalkBack, VoiceOver, browser focus, text-scaling, and contrast review expectations are documented in [`docs/accessibility.md`](docs/accessibility.md) and platform guides.

## Privacy and security

RPS Arena remains offline-first. Android uses app-private SharedPreferences, iOS uses NSUserDefaults, desktop uses Java Preferences, and Web uses origin-local `localStorage`. The primary Android app has no Internet permission, automatic backup is disabled, and SharedPreferences are excluded from platform cloud/device-transfer backup.

`SafeLogger` has a no-op default sink and redacts sensitive field names before any future custom sink receives them. Current state logging records only coarse operational metadata and never raw backup content.

See [`PRIVACY.md`](PRIVACY.md) and [`SECURITY.md`](SECURITY.md).

## Release

Version **2.5.8** remains synchronized across Android, desktop, iOS/Xcode, and shared metadata. Android/iOS numeric build code is **20508**. Tagged release automation is designed to validate/package public Android, Linux desktop, Web compatibility, iOS framework, and Rust artifacts and generate SHA-256 checksums.

Signed store/notarized artifacts require private credentials outside the public repository. See [`docs/release.md`](docs/release.md) and [`docs/ci-cd.md`](docs/ci-cd.md).

## Next version

v2.5.9 is planned, but package metadata will not change until v2.5.8 is merged, tagged, and its release artifacts/checksums are verified. The planned eventual mobile build code is **20509**.

See [`docs/NEXT_VERSION.md`](docs/NEXT_VERSION.md) for candidate work such as backup preview, reversible history clearing, reset confirmation, carefully ported multi-profile support, visible seed controls, timer restoration with migration tests, broader UI/accessibility coverage, and iOS/Web robustness.

## Contributing

Read [`CONTRIBUTING.md`](CONTRIBUTING.md), [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md), [`SECURITY.md`](SECURITY.md), and [`docs/maintenance.md`](docs/maintenance.md). The canonical owner commit email is `sanskarin@outlook.in`.

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
