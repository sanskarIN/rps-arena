# What Changed

## Current milestone — v2.5.8 full cross-platform product and release baseline

Date: 2026-08-20
Repository: `sanskarIN/rps-arena`
Working PR: `#11` (`feature/phase-7-completion` -> `main`)
License: MIT
Primary product posture: offline-first; no account, analytics SDK, ads SDK, cloud model, or mandatory gameplay backend.

This file is the detailed repository handoff. It records the current implementation, cross-platform architecture, data compatibility, build/release gates, documentation, limitations, and representative commit history so those details do not need to be repeated in chat.

## Current supported platform architecture

RPS Arena v2.5.8 now has real project targets/entry points for:

- Android;
- iPhone;
- iPad;
- Windows desktop;
- Linux desktop;
- macOS desktop;
- Web through Kotlin/Wasm plus Kotlin/JS compatibility output.

The optional Rust rules engine remains an independently tested companion crate rather than a runtime requirement.

### Android

- Application module: `androidApp`.
- Minimum API: 26.
- Compile/target SDK: 36.
- Entry point: `MainActivity`.
- Shared persistence adapter: Android `SharedPreferences`.
- Automatic Android backup remains disabled.
- SharedPreferences remain excluded from legacy/cloud/device-transfer backup rules.
- Primary Android manifest still intentionally avoids `android.permission.INTERNET`.

### iPhone and iPad

Added Kotlin/Native targets in `shared/build.gradle.kts`:

```text
iosArm64
iosSimulatorArm64
```

Both export a static framework:

```text
RpsArenaShared.framework
```

Added iOS platform persistence:

```text
shared/src/iosMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.ios.kt
```

It uses `NSUserDefaults.standardUserDefaults` while retaining all serialization, migration, validation, and backup logic in common `ArenaRepository` code.

Added Compose/native bridge:

```text
shared/src/iosMain/kotlin/in/sanskar/rpsarena/ui/MainViewController.kt
```

It initializes platform storage and returns a `ComposeUIViewController` rendering the same `RpsArenaApp()` used by other targets.

Added native SwiftUI/Xcode host:

```text
iosApp/iosApp/iOSApp.swift
iosApp/iosApp/ContentView.swift
iosApp/iosApp/Info.plist
iosApp/iosApp.xcodeproj/project.pbxproj
iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/RPS Arena.xcscheme
```

The SwiftUI host stays intentionally thin. It embeds the Kotlin Compose view controller instead of recreating gameplay, state, or storage logic in Swift.

The Xcode project includes direct Kotlin framework integration through:

```bash
gradle :shared:embedAndSignAppleFrameworkForXcode
```

The public repository contains no Apple private signing credentials.

### Windows, Linux, and macOS desktop

The existing `desktopApp` remains the shared JVM desktop entry point for all three supported desktop operating systems.

It uses:

- JVM 17;
- Compose Desktop;
- shared `RpsArenaApp()`;
- Java Preferences for local persistence;
- DMG/MSI/DEB native-distribution configuration.

The public tagged workflow currently packages Linux `.deb`; Windows/macOS signing/notarization remain host/credential-dependent release tasks rather than source-support gaps.

### Web

Added first-class module:

```text
webApp
```

`settings.gradle.kts` now includes:

```text
:shared
:androidApp
:desktopApp
:webApp
```

The Web application has both:

```text
Kotlin/Wasm browser executable
Kotlin/JS browser executable
```

Both share the same `webMain` entry point and browser storage adapter.

Added:

```text
webApp/build.gradle.kts
webApp/src/webMain/kotlin/in/sanskar/rpsarena/web/Main.kt
webApp/src/webMain/resources/index.html
shared/src/webMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.web.kt
```

Browser startup:

1. initialize `PlatformStore`;
2. attach a Compose viewport to `#webApp`;
3. render `RpsArenaApp()`.

Browser persistence uses:

```text
window.localStorage
```

The deployable release build uses Compose compatibility distribution so modern Wasm output and the JavaScript compatibility path are generated together.

## Shared-module target graph

The shared module now targets:

```text
Android
JVM desktop
iOS arm64
iOS simulator arm64
Kotlin/JS browser
Kotlin/Wasm browser
```

The default Kotlin hierarchy is applied so JS/Wasm can share `webMain` browser-specific code while product behavior remains in `commonMain`.

Cross-platform ownership remains:

```text
Platform host
  -> shared Compose UI
    -> ArenaState
      -> RulesEngine / CpuStrategy
      -> ArenaRepository
        -> expect/actual PlatformStore
      -> optional PrivateRoomGateway
```

No platform host owns an independent game engine.

## Cross-platform persistence

The common abstraction remains:

```text
PlatformStore.initialize()
PlatformStore.getString()
PlatformStore.putString()
```

Current physical adapters:

| Platform | Backend |
|---|---|
| Android | app-private SharedPreferences |
| iOS/iPadOS | NSUserDefaults |
| Windows/Linux/macOS desktop | java.util.prefs.Preferences |
| Web JS/Wasm | browser localStorage |

`ArenaRepository` remains the schema/validation authority. Platform stores are intentionally simple string transports.

## Existing gameplay/product completion retained

The cross-platform expansion preserves the v2.5.8 product-completion work already implemented on the branch.

### Rules and game modes

- Classic Rock–Paper–Scissors.
- Rock–Paper–Scissors–Lizard–Spock.
- CPU opponent.
- Same-device two-player pass-and-play.
- Best of 3.
- Best of 5.
- Endless.
- Streak.
- Tournament.

### CPU behavior

- Easy random strategy.
- Normal adaptive counter behavior after sufficient history.
- Expert frequency-based prediction/counter behavior with retained randomness.
- Deterministic seeded random sequence.
- Editable replay/challenge seed in the gameplay UI.

### Timers

Supported values:

```text
Off
5s
10s
20s
30s
60s
```

Timeouts remain typed outcomes rather than fabricated gestures.

CPU mode:

- player timeout awards the round to CPU.

Local two-player:

- timeout while Player 1 is choosing awards Player 2 the round;
- timeout while Player 2 is choosing awards Player 1 the round.

Timeout rounds update score, lifetime statistics, recent trend, streak state, and history consistently.

## Persistence and compatibility

### Settings

Current key:

```text
settings_v2
```

Legacy key:

```text
settings_v1
```

Valid legacy data migrates automatically when v2 is absent.

### Persisted match configuration

Current key:

```text
match_config_v1
```

It persists validated match setup such as ruleset, opponent, difficulty, mode, seed, and timer selection.

Invalid/corrupt persisted values fall back to safe defaults.

### Statistics and history

Existing keys remain:

```text
stats_v1
history_v1
```

History is bounded and sanitized before persistence.

### Explicit backup/import

Current schema header remains:

```text
RPS_ARENA_BACKUP|1
```

The backup contains settings, statistics, and bounded recent history.

Import validates the entire payload before replacement and rejects, among other cases:

- oversized payloads;
- excessive record counts;
- malformed records;
- unsupported headers;
- duplicate settings/stat records;
- unknown record types;
- invalid settings/language values;
- invalid/non-numeric/negative statistics;
- inconsistent rounds/wins/losses/draws invariants;
- impossible streak relationships.

`match_config_v1` intentionally remains outside backup schema v1, so importing a v1 backup preserves the receiving device's own current match setup.

## Localization

English and Hindi shared catalogs remain implemented in common code.

Localized areas include:

- onboarding;
- navigation;
- Play/Settings/Stats/History/Achievements/About;
- opponent/rules labels;
- CPU difficulty;
- match modes;
- timer labels;
- seed controls;
- gesture labels;
- turn instructions;
- timeout/result copy;
- visible recognized-history rendering;
- statistics labels;
- player profile controls;
- backup/import/reset controls;
- common data-operation feedback;
- About metadata labels;
- achievement titles/descriptions.

The same catalog is consumed by Android, iOS/iPadOS, desktop, and Web.

## UI and accessibility baseline

The shared UI retains:

- Material 3 light/dark themes;
- system theme selection;
- reusable layout tokens;
- constrained desktop max width;
- wrapping configuration chips;
- large gesture targets;
- text representations for timer/result/score state;
- reduced-motion result behavior;
- optional timers.

Platform review expectations now include:

- Android TalkBack;
- iOS VoiceOver;
- desktop keyboard/focus;
- browser keyboard/pointer/touch;
- text scaling/zoom;
- portrait/landscape and responsive layouts.

## Private-room architecture

The current implementation still ships no real network transport.

Shared contracts include:

- `PrivateRoomGateway`;
- `PrivateRoomSession`;
- `RoomCode`;
- `RoomParticipant`;
- `RoomRole`;
- typed `RoomEvent` values.

Current implementation:

```text
InMemoryPrivateRoomGateway
```

It is no-network and used for deterministic protocol/reference testing.

Current integrity controls include:

- six-character validated room codes;
- ambiguous-character rejection;
- two-participant maximum;
- bounded/sanitized display names;
- sender identity validation;
- positive gesture-round validation;
- lifecycle-event authority restrictions;
- idempotent session close;
- rejection of sends from closed/detached sessions.

A future LAN/Web networking adapter must remain optional and must not convert normal gameplay into a cloud/network dependency.

## Version 2.5.8 synchronization

Public semantic version:

```text
2.5.8
```

Deterministic mobile build number:

```text
20508
```

Version declarations currently checked:

- Android `versionName = "2.5.8"`;
- Android `versionCode = 20508`;
- desktop `packageVersion = "2.5.8"`;
- iOS `CFBundleShortVersionString = 2.5.8`;
- iOS `CFBundleVersion = 20508`;
- Xcode `MARKETING_VERSION = 2.5.8`;
- Xcode `CURRENT_PROJECT_VERSION = 20508`;
- shared `APP_VERSION = "2.5.8"`;
- About renders shared `APP_VERSION`.

`scripts/check_version.py` enforces:

```text
major * 10000 + minor * 100 + patch
```

for Android/iOS numeric build codes, with minor/patch limited to two digits by the current mapping.

The Web app renders shared `APP_VERSION`; it does not maintain another independent version constant.

## Repository source-quality gates

Fast source gates remain:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

### Formatting

Checks UTF-8, final newline, and trailing whitespace while permitting intentional Markdown two-space hard breaks.

### Documentation links

Validates repository-relative Markdown links and prevents missing/escaping local targets.

### Documentation coverage

`scripts/check_docs_coverage.py` uses:

```text
git ls-files -z
```

and requires every tracked path to be explicitly documented in:

```text
docs/repository-file-reference.md
```

All newly added iOS, Xcode, Web, and cross-platform documentation files are included in that exhaustive reference.

### Secret patterns

The source scanner checks several high-confidence credential/private-key forms without printing detected secret values.

### Android privacy contract

The checker enforces:

- automatic app backup disabled;
- valid backup-rule references;
- SharedPreferences cloud/device-transfer exclusion;
- absence of Android Internet permission in the primary offline-first manifest.

### Version consistency

Now validates Android, desktop, iOS plist/Xcode, shared metadata, About rendering, and mobile build-code mapping.

## Cross-platform CI

`.github/workflows/ci.yml` now has three primary platform groups.

### Ubuntu Kotlin/Android/Desktop/Web

Runs:

```bash
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

### macOS iOS/iPadOS

Runs:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

and:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

### Rust

Runs:

```bash
cargo test --all-targets
```

Focused Security checks and CodeQL remain separate required signals under the exact-head validation policy.

## Release automation

Tagged/manual release workflow now includes:

### Android

- shared tests;
- release lint;
- unsigned/public APK build.

Artifact:

```text
rps-arena-android
```

### Linux desktop

- desktop compile;
- `.deb` package.

Artifact:

```text
rps-arena-linux
```

### Web

Builds:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

Packages the generated compatibility distribution as:

```text
rps-arena-web.zip
```

### iOS/iPadOS

Builds device and simulator Release frameworks and validates the Release SwiftUI host without signing.

Packages:

```text
rps-arena-ios-device-framework.zip
rps-arena-ios-simulator-framework.zip
```

These are framework/integration artifacts, not signed IPA/App Store files.

### Rust

Runs tests and `cargo package`.

### Publish

The tagged publish job depends on:

```text
android
desktop-linux
web
ios
rust
```

and generates SHA-256 checksums before GitHub Release publication.

## Signing and credential boundaries

Public source/CI intentionally does not include:

- Android keystores/passwords;
- Play credentials;
- Apple private signing certificates;
- provisioning/account secrets;
- App Store Connect API keys;
- Windows private signing certificates;
- macOS notarization credentials;
- private GitHub/store tokens.

Therefore:

- Android public artifacts remain unsigned validation artifacts unless an authorized signing workflow is supplied externally;
- iOS CI produces simulator/framework validation, not signed TestFlight/App Store IPA distribution;
- Windows/macOS desktop production signing/notarization remains external.

This is an intentional security boundary, not missing source-platform support.

## Web stability boundary

The Web application is implemented and built in CI using both Kotlin/Wasm and Kotlin/JS compatibility output. Its platform maturity still follows upstream Compose Multiplatform Web stability. The repository therefore documents browser-specific limitations/tests instead of presenting Web behavior as identical to a mature native target in every browser environment.

## New cross-platform documentation

Added:

```text
docs/ios-platform.md
docs/web-platform.md
```

Updated:

```text
README.md
ROADMAP.md
CHANGELOG.md
docs/documentation-index.md
docs/repository-file-reference.md
docs/release.md
docs/validation.md
docs/ci-cd.md
what_changed.md
```

The iOS guide covers:

- target definitions;
- NSUserDefaults;
- Kotlin -> UIViewController bridge;
- SwiftUI host;
- Xcode direct integration;
- versioning;
- simulator/device framework tasks;
- CI/release;
- signing/privacy boundaries.

The Web guide covers:

- JS/Wasm hierarchy;
- ComposeViewport;
- localStorage;
- development runs;
- compatibility distribution;
- deployment artifacts;
- browser testing/limitations.

## `.gitignore` cross-platform hygiene

New generated-state exclusions include:

```text
.kotlin/
node_modules/
kotlin-js-store/
DerivedData/
xcuserdata/
*.xcuserstate
```

Existing Android keystore, build, desktop installer, IDE, and Rust target exclusions remain.

## Testing already present on the milestone

Shared/common tests continue covering:

- game rules;
- CPU determinism;
- match timer/target invariants;
- settings/stat codecs;
- legacy migration;
- backup validation/round trip;
- persistence validation;
- timeout behavior;
- deterministic replay through state;
- private-room protocol restrictions;
- English/Hindi catalogs;
- achievement copy;
- safe logger redaction.

Desktop Compose UI tests continue covering:

- onboarding;
- primary Play navigation;
- classic gestures;
- English/Hindi switching;
- Hindi gameplay/achievement copy;
- backup/import controls;
- destructive-reset confirmation.

Cross-platform CI now adds compile/package evidence for Web and iOS rather than assuming common-code tests alone imply those targets build.

## Current intentional limitations

- Production LAN/private-room socket transport is not shipped.
- Android device/emulator instrumentation is not yet part of hosted CI.
- Public CI does not contain private signing credentials.
- Tagged workflow packages Linux desktop directly; Windows MSI/macOS DMG production packaging/signing remain host-specific distribution work.
- iOS public release automation validates/packages frameworks rather than a signed IPA.
- Web behavior is subject to browser/runtime compatibility and upstream Web platform maturity.
- Sound/haptics settings exist but platform-specific effect engines are not added solely to increase dependency count.
- The repository still does not track a Gradle Wrapper; documented commands use an installed/setup Gradle.

## Next optional work after a green exact-head cross-platform merge

1. Add a real opt-in LAN transport only after explicit security/privacy/product approval.
2. Add Android device/emulator instrumentation and broaden platform UI automation where runner stability/cost is acceptable.
3. Configure signed Android, Windows/macOS desktop, and App Store/TestFlight release jobs only after authorized secrets exist outside Git.
4. Add hosted Web deployment automation only when a deployment provider/domain is intentionally selected.
5. Add more languages only when translation/testing maintenance quality can be preserved.
6. Consider an integrity-reviewed official Gradle Wrapper change for stronger local Gradle reproducibility.
7. Evaluate additional platform families only when there is a real product use case and an appropriate testing/distribution path.

## Representative cross-platform commits

The cross-platform continuation intentionally used small cohesive commits. Representative messages include:

- `build(web): add browser API dependency`
- `build(shared): add iOS and Wasm targets`
- `feat(ios): add persistent platform store`
- `feat(ios): expose shared Compose view controller`
- `feat(web): add browser application module`
- `feat(web): launch shared UI in Compose viewport`
- `feat(web): add responsive browser shell`
- `build: register web application module`
- `feat(ios): add SwiftUI application entry point`
- `feat(ios): host shared Compose UI in SwiftUI`
- `feat(ios): add application metadata and display settings`
- `feat(ios): add Xcode host project with direct KMP integration`
- `build(web): add JS fallback target for compatibility mode`
- `build(web): enable JS and Wasm compatibility targets`
- `refactor(web): share browser persistence across JS and Wasm`
- `refactor(web): share browser entry point across JS and Wasm`
- `refactor(web): share responsive host page across browser targets`
- `refactor(web): remove redundant Wasm-only platform store`
- `refactor(web): remove redundant Wasm-only entry point`
- `refactor(web): remove redundant Wasm-only host page`
- `build(ios): add shared Xcode scheme`
- `ci: validate web compatibility and iOS simulator builds`
- `ci(version): validate iOS release metadata`
- `ci(release): package web and validate iOS artifacts`
- `chore: ignore web and Xcode generated state`
- `fix(ios): import UIKit explicitly in Swift bridge`
- `docs(ios): add complete iPhone and iPad platform guide`
- `docs(web): add complete browser compatibility platform guide`
- `docs(reference): document all iOS and Web platform files`
- `docs(index): add iOS and Web platform navigation`
- `docs(readme): publish full cross-platform support matrix`
- `docs(roadmap): mark iOS and Web cross-platform targets complete`
- `docs(release): add iOS and Web release gates and artifacts`
- `docs(validation): require iOS and Web cross-platform build evidence`
- `docs(changelog): record complete cross-platform expansion`
- `docs(ci): document Android iOS desktop Web and Rust automation`.

## Commit identity

The repository documents:

```text
Sanskar <sanskarin@outlook.in>
```

as the canonical owner identity through `.mailmap` and contributor/setup documentation. Commits produced through the authenticated GitHub integration use the repository owner's GitHub-authorized identity.

## Final validation rule

No older green workflow run is considered sufficient after these cross-platform commits.

The branch is merge-ready only after CI, focused Security checks, and CodeQL complete successfully on the **exact final PR head** containing this handoff and all cross-platform documentation/code.

Any new fix changes the head and therefore requires validation again.

**Made by the Sanskar.**
