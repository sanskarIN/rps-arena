# What Changed

## 2026-08-24 — v2.5.8 reconciliation and release hardening

Repository: `sanskarIN/rps-arena`  
Working pull request: `#11` (`feature/phase-7-completion` -> `main`)  
Current release line: **v2.5.8**  
Planned next version: **v2.5.9**  
License: MIT  
Product posture: offline-first; no mandatory account, analytics SDK, ads SDK, cloud model, or gameplay backend.

This file is the current repository handoff. It replaces the pre-reconciliation description that mixed an older parallel UI/localization implementation with the newer milestones already merged to `main`.

## Reconciliation checkpoint

The cross-platform v2.5.8 branch had diverged from `main` while backup/restore, Compose-resource localization, and platform-specific UI automation were completed independently. The branch was reconciled without squashing its history.

### Safety and ancestry

- Original pre-sync v2.5.8 head: `f6531aa322abb74b1436eb7a6c6fcfd08e528be1`.
- Safety branch: `archive/phase-7-pre-main-sync-20260824`.
- Current `main` parent used for reconciliation: `73591feddbda870dd0bcb82015b740397fb5e81a`.
- Two-parent reconciliation commit: `70d8b6c1c01cda5d81ebf7ab4c5bade9accc79cc`.
- The reconciliation preserved the complete granular phase-7 history and made PR #11 mergeable against current `main`.

The validated `main` implementations are authoritative for:

- `ArenaStore` injection and production `PlatformArenaStore` delegation;
- versioned `RPSARENA_BACKUP|1` backup/restore;
- Compose Multiplatform English/Hindi resources;
- stable localization-independent `ArenaUiTags`;
- desktop Compose UI tests;
- Android KMP instrumentation smoke tests;
- current shared state/repository/UI architecture.

The v2.5.8 branch continues to provide compatible cross-platform/release infrastructure for:

- iOS/iPadOS Kotlin/Native + SwiftUI/Xcode hosting;
- Kotlin/JS + Kotlin/Wasm Web hosting;
- release automation and artifact packaging;
- security/repository source gates;
- transport-neutral private-room contracts;
- no-op-by-default structured safe logging;
- synchronized v2.5.8 package metadata.

Older parallel `ArenaStrings`/`AppLanguage` localization and old backup implementations are intentionally not restored over the newer `main` architecture.

## Current supported source targets

The reconciled v2.5.8 branch has source/build targets for:

- Android API 26+;
- iPhone/iPad through `iosArm64` and `iosSimulatorArm64`;
- Windows/Linux/macOS through Compose Desktop/JVM;
- Web through Kotlin/Wasm plus Kotlin/JS compatibility output;
- optional independently tested Rust rules-engine crate.

### Android

- Application module: `androidApp`.
- Compile/target SDK: 36.
- Minimum SDK: 26.
- Production persistence: app-private `SharedPreferences`.
- Automatic Android app backup remains disabled.
- SharedPreferences remain excluded from platform backup/device-transfer rules.
- Primary Android manifest intentionally has no `android.permission.INTERNET`.

### iOS/iPadOS

The shared module provides:

```text
iosArm64
iosSimulatorArm64
```

Both produce the static `RpsArenaShared` framework. The native host includes:

```text
iosApp/iosApp/iOSApp.swift
iosApp/iosApp/ContentView.swift
iosApp/iosApp/Info.plist
iosApp/iosApp.xcodeproj/project.pbxproj
iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/RPS Arena.xcscheme
```

`PlatformStore.ios.kt` uses `NSUserDefaults`. `MainViewController.kt` exposes the shared Compose UI to SwiftUI. The Xcode simulator host excludes unsupported `x86_64`, matching the configured Apple-silicon `iosSimulatorArm64` target. Public source/CI does not contain Apple signing credentials.

### Desktop

`desktopApp` remains the JVM/Compose Desktop host for Windows, Linux, and macOS. Persistence uses Java Preferences. Native DMG/MSI/DEB configuration exists; public release automation directly packages Linux `.deb`, while production Windows/macOS signing/notarization remains credential/host dependent.

### Web

`webApp` provides executable Kotlin/JS and Kotlin/Wasm browser targets sharing `webMain`. Browser persistence uses `window.localStorage`; the host attaches `ComposeViewport` to the `webApp` container and renders the shared application. CI builds the Compose compatibility browser distribution.

## Current gameplay baseline

The current reconciled shared product retains:

- Classic Rock–Paper–Scissors;
- optional Rock–Paper–Scissors–Lizard–Spock;
- CPU opponent;
- same-device two-player pass-and-play;
- Easy, Normal, and Expert CPU strategies;
- Best of 3, Best of 5, Endless, Streak, and Tournament match modes;
- deterministic CPU seed behavior;
- aggregate statistics, recent history, achievements, onboarding, settings, and explicit offline backup/restore.

### Restored after reconciliation

The following overlapping v2.5.8 work has already been reintroduced in focused commits instead of overwriting current `main` code:

- persisted `MatchConfig` under `match_config_v1`;
- persisted ruleset, opponent mode, difficulty, match mode, and CPU seed;
- safe fallback to default match configuration for malformed stored values;
- stronger persisted-stat invariant checks;
- bounded/sanitized history writes with a 160-character per-entry limit;
- no-op-by-default structured `SafeLogger` integration in `ArenaState`;
- privacy-safe logging for onboarding, settings changes, backup result, match configuration, match reset, rounds, and rejected gestures;
- rejection of gestures unavailable in the currently selected ruleset;
- deterministic replay and persisted-match-state regression tests.

Focused continuation commits include:

- `2ab240a9deeb9c0da96b0b971df486971e7099d8` — `feat: persist match configuration`;
- `fceeca8158a7524a66a5517624fedaff528203fb` — `feat: restore persisted match setup in state`;
- `727eb91d0b760572d148f7a0bc634997a6207050` — `test: cover persisted match configuration`;
- `6ea3d615e1cf8acd00bd77be5fe59a7268bd643e` — `fix: harden persisted stats and history validation`;
- `d9f686c111faeb5c234b9f8604d157471a40cf18` — `test: cover persistence validation hardening`;
- `72d441003306c58dbfcc62639de7e1a4e1fd098c` — `feat: integrate privacy-safe state logging`;
- `d21b19da9a52651f4d7741dc8c3ef08d372eb21b` — `test: restore state replay and persistence coverage`.

## Data and backup compatibility

### Production persistence keys

Current active keys include:

```text
settings_v1
match_config_v1
stats_v1
history_v1
```

Platform storage remains a simple string transport behind `PlatformStore` / `ArenaStore`; common repository code owns encoding, validation, limits, and migration decisions.

### Backup schema

The authoritative explicit backup header is:

```text
RPSARENA_BACKUP|1
```

Backup schema 1 includes:

- settings;
- aggregate statistics;
- up to 30 recent history entries.

It validates the complete payload before applying imported data, rejects unsupported schema versions, validates statistics invariants, and sanitizes bounded history records. `match_config_v1` is deliberately not silently injected into schema 1 during reconciliation; changing backup contents requires an explicit compatibility/versioning decision.

## Localization architecture

The authoritative shared localization system is Compose Multiplatform resources:

```text
shared/src/commonMain/composeResources/values/strings.xml
shared/src/commonMain/composeResources/values-hi/strings.xml
```

English and Hindi catalogs are checked by `scripts/verify_localizations.py` for key parity and formatting-placeholder compatibility. Stable `ArenaUiTags` are independent of visible strings so UI automation remains valid across locales.

The older manual `ArenaStrings` / `AppLanguage` implementation from the pre-sync branch is not part of the reconciled runtime.

## UI automation and testing

The current architecture deliberately separates test responsibilities:

- `commonTest`: shared non-rendering logic and persistence tests;
- `desktopTest`: Compose rendering/UI automation on desktop JVM;
- `androidDeviceTest`: Android instrumentation/UI smoke tests;
- Rust: independent rules-engine tests.

Current Android instrumentation covers onboarding/gameplay and uses an instrumentation-only host activity. It adds no production permission or network dependency.

The combined CI gate runs:

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
```

CI additionally builds the iOS simulator framework and unsigned SwiftUI/Xcode host on macOS and runs `cargo test --all-targets` for the Rust engine.

Focused Security checks and CodeQL remain separate exact-head gates.

## Repository quality gates

The reconciled source checks enforce:

- text formatting and final-newline policy;
- valid internal Markdown links while ignoring fenced/inline-code examples;
- exhaustive documentation of every Git-tracked file;
- high-confidence committed-secret patterns;
- Android offline/privacy manifest and backup-rule invariants;
- cross-platform semantic version/build-code agreement;
- English/Hindi resource parity.

During reconciliation, `docs/reconciliation-file-reference.md` temporarily complements the canonical exhaustive repository reference. It exists to keep the coverage rule strict while newer main-milestone files are folded into the larger v2.5.8 documentation set.

## Private-room boundary

The branch contains transport-neutral two-player contracts and a deterministic `InMemoryPrivateRoomGateway`. It performs no network I/O and requires no Android Internet permission.

Current protections include:

- six-character unambiguous room codes;
- two-participant maximum;
- bounded/sanitized display names;
- sender identity checks;
- positive round-number validation;
- lifecycle-event restrictions;
- idempotent close behavior.

A real LAN/network adapter remains explicitly optional and is not part of the v2.5.8 shipping runtime.

## Safe logging boundary

`SafeLogger` is structured but has a no-op default sink, so using it does not create telemetry. Sensitive field names such as password, secret, token, authorization, cookie, email, backup/content/payload are redacted before any custom sink receives them, and non-sensitive values are length bounded.

`ArenaState` logs only coarse operational metadata and never logs raw backup text.

## v2.5.8 version synchronization

Release metadata remains intentionally fixed at:

```text
2.5.8
20508
```

The release checker verifies:

- Android `versionName = "2.5.8"`;
- Android `versionCode = 20508`;
- desktop `packageVersion = "2.5.8"`;
- iOS `CFBundleShortVersionString = 2.5.8`;
- iOS `CFBundleVersion = 20508`;
- Xcode `MARKETING_VERSION = 2.5.8`;
- Xcode `CURRENT_PROJECT_VERSION = 20508`;
- shared `APP_VERSION = "2.5.8"`.

Mobile numeric codes follow:

```text
major * 10000 + minor * 100 + patch
```

The checker intentionally validates metadata rather than depending on a particular UI-localization implementation.

## Release automation

The v2.5.8 tagged/manual release workflow is intended to produce/validate:

- Android unsigned/public release artifact;
- Linux `.deb` desktop artifact;
- Web compatibility ZIP;
- iOS device/simulator framework ZIPs and simulator host validation;
- Rust crate package;
- SHA-256 checksums before GitHub Release publication.

Public automation intentionally does not contain Android keystores, Apple signing identities, provisioning secrets, App Store Connect credentials, Windows certificates, or macOS notarization credentials.

## Remaining v2.5.8 work

Before PR #11 can merge/tag, the exact final branch head still needs:

1. CI, Security checks, and CodeQL green on that exact revision;
2. any reconciliation failures fixed without bypassing quality gates;
3. PR #11 description synchronized with the actual reconciled feature set;
4. final documentation/reference consolidation;
5. decision on whether user-visible seed controls and round timers are release-blocking or should move to v2.5.9;
6. final release artifact/checksum/version audit;
7. merge to `main` with granular history preserved;
8. `v2.5.8` tag and release workflow verification;
9. post-merge `main` green check.

The older branch had implementations for timer/timeouts, player-name/trend UI, reset flows, and other enhancements, but those are not considered present merely because they exist in history. They must be ported onto the reconciled architecture with tests before being claimed as current v2.5.8 behavior.

## Next version preparation — v2.5.9

`docs/NEXT_VERSION.md` now defines the post-v2.5.8 plan. Runtime/package metadata has **not** been bumped early.

The planned eventual transition is:

```text
v2.5.9
Android/iOS build code 20509
```

Entry requires a completed v2.5.8 merge/tag/release. Candidate v2.5.9 work includes:

- backup preview-before-import;
- reversible history clearing;
- explicit reset-data confirmation;
- carefully ported multi-profile support from the superseded final-audit branch;
- localized visible seed controls;
- round timers/timeouts after model/persistence migration tests;
- broader Android/desktop UI and accessibility automation;
- iOS/Web release robustness;
- optional permission-minimal local file import/export UX.

Real LAN networking, cloud accounts/sync, analytics/ads, mandatory Android Internet permission, and embedded production signing credentials are explicitly not automatic v2.5.9 scope.

## Superseded-work cleanup after release

After the canonical v2.5.8 branch is merged, remaining repository cleanup includes:

- inspect PR #10 only for genuinely unique work (not duplicate infrastructure);
- port useful multi-profile, backup-preview, or undo-history behavior as focused changes if still desired;
- close/retire PR #10 once unique work is accounted for;
- close stale dependency PRs already superseded by the canonical dependency baseline;
- remove obsolete branches when safe;
- verify `main` remains green after cleanup.

## Validation rule

No older successful workflow run is enough after a new commit. PR #11 is merge-ready only when CI, Security checks, and CodeQL are green on the **exact final head** containing all release code and documentation.

**Made by the Sanskar.**
