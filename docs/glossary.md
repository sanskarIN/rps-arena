# RPS Arena Technical Glossary

This glossary explains project terms, build words, Kotlin/Compose concepts, Android/Desktop terminology, testing/security/release vocabulary, and repository-specific names used throughout the documentation.

## A

### AAB
Android App Bundle. A Play-distribution package format. RPS Arena's current public workflow builds APK artifacts, not an AAB. Do not claim AAB publishing is configured unless a bundle task/release path is added.

### ABI
Application Binary Interface. Relevant if native/Rust libraries are ever linked across Android/desktop architectures. The current Kotlin app does not use Rust FFI.

### `actual`
Kotlin Multiplatform implementation of a common `expect` declaration for one target/source set. RPS Arena has Android and desktop `actual PlatformStore` objects.

### adaptive icon
Android launcher icon made from separate foreground/background layers that a launcher can mask into supported shapes.

### AGP
Android Gradle Plugin. RPS Arena's version catalog currently pins AGP 9.3.0.

### APK
Android application package used for installable/debug/release Android builds. Current release workflow uploads an unsigned/public release APK artifact.

### app-private storage
Storage protected by the operating system's application/user isolation model. Android `SharedPreferences` uses `MODE_PRIVATE`; this is not encryption by itself.

## B

### backup schema
Versioned text format used by RPS Arena's explicit local export/import. Current header: `RPS_ARENA_BACKUP|1`.

### branch
A movable Git reference used to develop changes independently from `main`.

### build cache
Gradle mechanism for reusing compatible task outputs rather than rebuilding them.

### Build Tools
Android SDK packaging/build utilities. CI currently installs Build Tools 36.0.0.

## C

### canonical data
Stable/internal representation used regardless of UI language. Example: stored history uses canonical gesture/result text while UI can render localized copy.

### Cargo
Rust package/build/test tool used for `rust-engine/`.

### CI
Continuous Integration. RPS Arena's `CI` GitHub Actions workflow runs formatting, version checks, tests, Android lint/build, desktop compilation, and Rust tests.

### checksum
Digest used to detect whether artifact bytes changed/corrupted. Release workflow creates SHA-256 checksums. A checksum is not equivalent to code signing.

### CodeQL
GitHub static code analysis system. RPS Arena analyzes Java/Kotlin on pushes/PRs to main and a weekly schedule.

### commit
Immutable Git object recording a project snapshot plus metadata/message/parents.

### Compose Multiplatform
JetBrains Compose UI technology used by RPS Arena to share UI between Android and JVM desktop.

### Composable
Function annotated with `@Composable` that participates in Compose UI composition.

### configuration cache
Gradle feature that reuses configured build state across compatible runs. Enabled in `gradle.properties`.

### coroutine
Kotlin concurrency abstraction. RPS Arena uses coroutine `delay()` for UI timer countdown work through `LaunchedEffect`.

## D

### debug variant
Android development build variant. CI runs `lintDebug` and `assembleDebug`.

### dependency
External library/module required by another component. The project centralizes Gradle versions in `libs.versions.toml`.

### Dependabot
GitHub service configured here for weekly Gradle, Cargo, and GitHub Actions update pull requests.

### deterministic
Given the same inputs/state/seed/call order, behavior repeats. RPS Arena's seeded CPU random sequence is deterministic.

### DMG
macOS disk image package format configured in Compose Desktop target formats. Current Linux release workflow does not build it.

## E

### `expect`
Kotlin Multiplatform common declaration requiring platform-specific `actual` implementations. `PlatformStore` is the project's current example.

### exported Activity
Android component allowed to be launched by external/system actors according to manifest intent filters. `MainActivity` is exported because it is the launcher Activity.

## F

### FFI
Foreign Function Interface: calling code across language/native ABI boundaries. RPS Arena currently has no Kotlin/Rust FFI.

### FlowRow
Compose layout that wraps children to additional rows when horizontal space is insufficient. RPS Arena uses it for configuration chips on narrow screens.

### foreground/background adaptive icon layers
Separate Android icon layers that allow launcher masking/parallax effects.

## G

### Git
Distributed version-control system used by the repository.

### GitHub Actions
GitHub-hosted workflow automation system used for CI, CodeQL build support, and releases.

### Gradle
Build automation system for Kotlin/Android/Desktop modules. This repository currently relies on installed/setup Gradle rather than a tracked Gradle Wrapper.

### Gradle Wrapper
Standard `gradlew`/`gradlew.bat` + wrapper configuration/JAR that pins/downloads a Gradle distribution. It is **not currently tracked** in this repository.

## H

### handoff log
`what_changed.md`, the detailed repository checkpoint describing completed work, validation, migrations, limitations, and next external tasks.

### history
Up to 30 recent canonical round summary strings stored locally under `history_v1`.

## I

### immutable data class
Kotlin data object typically replaced with `.copy(...)` instead of mutating fields. Match snapshots/settings/stats use this style.

### in-memory private-room gateway
No-network reference implementation of `PrivateRoomGateway` used for deterministic architecture/testing.

### instrumentation test
Test running with Android framework/device/emulator instrumentation. RPS Arena does not currently track an Android instrumentation suite.

## J

### JDK
Java Development Kit. Project baseline is JDK 17.

### JVM
Java Virtual Machine. Desktop app and Kotlin Android JVM bytecode use Java/JVM tooling; shared desktop target compiles to JVM target 17.

### JVM target
Bytecode compatibility level emitted by Kotlin compiler. Shared Android/desktop targets use JVM 17.

## K

### Kotlin Multiplatform / KMP
Kotlin build model that shares common source while allowing target-specific implementations. `:shared` targets Android and desktop JVM.

### Kotlin DSL
Gradle build scripts written as `.gradle.kts` using Kotlin syntax.

## L

### LAN
Local Area Network. A future private-room transport may use LAN networking, but the current release candidate contains no real LAN adapter.

### `LaunchedEffect`
Compose API that launches a coroutine tied to composition keys/lifecycle. The round countdown uses it.

### lint
Static correctness/style/API analysis. Android Lint is run by CI for the Android app.

### localization
Presenting UI copy in selected language while retaining language-independent domain identities. RPS Arena ships English and Hindi core catalogs.

### `local.properties`
Common Android local machine file for SDK path. Ignored by Git and should not be committed.

## M

### main
Default/stable repository branch used as CI/release integration target.

### manifest
`AndroidManifest.xml`, Android application component/permission/metadata declaration.

### Material 3
Google's current Material Design component system implemented via Compose Material 3 library used in shared UI.

### migration
Conversion/compatibility path from an older persisted representation to a newer one. RPS Arena migrates compatible `settings_v1` to `settings_v2`.

### MSI
Windows Installer package format configured for desktop native distributions but not currently produced by public release CI.

## N

### namespace
Android/Kotlin resource/source namespace. Android app uses `in.sanskar.rpsarena`; shared Android library uses `in.sanskar.rpsarena.shared`.

### native distribution
OS-specific packaged desktop application such as DEB/MSI/DMG.

## O

### offline-first
Product design where primary functionality works without account/network/cloud. RPS Arena CPU/local play/storage follow this model.

### onboarding
First-run welcome state controlled by persisted `onboardingComplete` setting.

### outcome
`RoundOutcome`: Player 1 win, Player 2 win, or draw.

## P

### package / application ID
Android installed app identity `in.sanskar.rpsarena`. Changing it can create a separate application rather than a normal upgrade.

### packageVersion
Compose Desktop native package version. Kept synchronized with Android/shared app version.

### PlatformStore
Small KMP `expect/actual` string storage abstraction used by `ArenaRepository`.

### Preferences
Java desktop user preference API used by desktop `PlatformStore`.

### PR / pull request
GitHub review/integration proposal from one branch to another.

### private room
Two-participant room architecture represented by shared gateway/session/event contracts. Current concrete adapter is in-memory only.

## R

### release variant
Android optimized/distribution-oriented build variant. Public release workflow lints/builds it without committing signing credentials.

### repository
`ArenaRepository` in product architecture means the local data/codec layer; "repository" in Git/GitHub context means the whole source repository. Use context to distinguish them.

### responsive layout
UI that remains usable as available size changes. RPS Arena caps desktop content width and wraps config chips on narrow widths.

### RTL
Right-to-left layout/language direction. Android declares `supportsRtl=true`; actual RTL-language shipping still requires testing.

### Rust `rlib`
Standard Rust library artifact configured by the optional crate. It is not a JVM-callable shared library automatically.

## S

### schema
Defined structure/meaning of persisted or transported data. Settings/history/stats/backup formats all have compatibility expectations.

### SDK
Software Development Kit. Android compile/target/min SDK values have distinct roles.

### seed
Integer used to initialize deterministic pseudo-random CPU strategy. Same seed plus same strategy inputs/call order reproduces sequence.

### semantic version / SemVer
Common `major.minor.patch` version shape such as `1.1.0`. RPS Arena checks three numeric components for shared app version.

### SHA-256
Cryptographic hash function used by release workflow for artifact checksums.

### SharedPreferences
Android key/value preference API used by Android `PlatformStore`.

### signing
Cryptographic publisher identity applied to app packages. Signing keys are intentionally not committed to this public repo.

### source set
Kotlin Multiplatform grouping such as `commonMain`, `androidMain`, `desktopMain`, `commonTest`, `desktopTest`.

### stack trace
Detailed Java/Kotlin call chain printed on failure with Gradle `--stacktrace`.

### state machine
Here, `ArenaState` orchestration that moves match/settings/UI state through valid transitions.

### streak
Consecutive Player 1 wins; draw/loss resets current streak under current implementation.

## T

### target
A platform compilation target in KMP/Gradle, e.g. shared Android target or `desktop` JVM target.

### target SDK
Android behavior compatibility level the app declares. Current target SDK is 36.

### timeout
Timed-turn expiration represented by typed `RoundEndReason`, not a fabricated gesture.

### TOML
Configuration format used by Gradle version catalog and Cargo manifest.

### transport
Mechanism carrying room messages (in-memory reference now; possible LAN implementation later).

## U

### unsigned artifact
Package built without project owner/store signing key. Public release automation intentionally produces public/unsigned artifacts unless secure signing is added later.

### UTF-8
Text encoding required by repository formatting policy.

## V

### variant
Can mean Android build variant (`debug`, `release`) or product `GameVariant` (`CLASSIC`, `LIZARD_SPOCK`). Use surrounding context.

### version catalog
`gradle/libs.versions.toml` central names/versions for Gradle plugins/libraries/SDK values.

### versionCode
Android integer release ordering value. Current value is 2.

### versionName
Android user-visible semantic version string. Current candidate is `1.1.0`.

## W

### workflow
GitHub Actions YAML automation. Current workflows: CI, CodeQL, Release.

### workflow concurrency
GitHub Actions grouping policy that cancels obsolete runs for the same ref/workflow when newer commits arrive.

## Y

### YAML
Configuration language used by GitHub Actions/Dependabot/issue forms/funding. Repository EditorConfig uses two-space indentation for YAML.

## Repository-specific identifiers

### `RPS_ARENA_BACKUP|1`
Exact header for current app-controlled text backup schema.

### `settings_v1`
Legacy seven-boolean settings key/format.

### `settings_v2`
Current settings key including name/language fields.

### `stats_v1`
Current aggregate stats key.

### `history_v1`
Current recent history key.

### `rps_arena`
Android SharedPreferences file name.

### `in/sanskar/rpsarena`
Java Preferences node used by desktop storage.

### `ArenaState`
Shared runtime orchestration/state owner.

### `RulesEngine`
Shared authoritative gesture winner/counter table.

### `CpuStrategy`
Seeded local CPU selection strategy.

### `ArenaRepository`
Shared local serialization/migration/history/backup repository.

### `PrivateRoomGateway`
Transport-neutral host/join contract.

### `InMemoryPrivateRoomGateway`
No-network two-player reference implementation.

### `ArenaStrings`
Typed core UI localization catalog.

### `AchievementCopy`
Localized title/description representation keyed from stable achievement IDs.

### `ArenaLayoutTokens`
Shared dimensions for screen/content spacing/max width/gesture target height.

### `ArenaTheme`
Shared Material 3 light/dark theme function.

### `APP_VERSION`
Shared semantic app version constant rendered by About and checked against Android/Desktop versions.

## Terms intentionally not claimed

### AI / machine learning CPU
The CPU is deterministic seeded random + simple last/frequency counter strategy. It is not an online AI/ML model.

### cloud sync
Not implemented.

### production LAN multiplayer
Not implemented; only transport-neutral architecture/in-memory adapter exists.

### end-to-end encryption
Not implemented/needed for current no-network adapter.

### signed store release automation
Not implemented without external authorized credentials.

### iOS application
Not part of current release target.

When project scope changes, update this glossary so terminology remains accurate rather than marketing ahead of implementation.
