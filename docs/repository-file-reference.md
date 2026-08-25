# Repository File Reference — Every Tracked File

This is the exhaustive file-by-file reference for RPS Arena. Every Git-tracked file is named here with its purpose, ownership, and change implications. `scripts/check_docs_coverage.py` enforces this list with `git ls-files`: if a future tracked file is not mentioned in backticks here, the documentation-coverage check fails.

Generated directories such as `.gradle/`, `.kotlin/`, module `build/`, `node_modules/`, Xcode `DerivedData/`, and `rust-engine/target/` are intentionally not listed because they are not tracked source files.

## Root repository policy and metadata

### `.editorconfig`

Editor-neutral text formatting policy. It sets UTF-8, LF endings, final newline, spaces/indentation, and trailing-whitespace behavior. YAML/JSON/TOML use two-space indentation. Markdown deliberately disables editor trimming because two trailing spaces can represent a Markdown hard break; the repository format checker handles the more precise policy.

Edit when repository-wide text style changes. Keep aligned with `scripts/check_format.py`.

### `.gitattributes`

Git-level line-ending/binary classification. Normal text is normalized to LF; `.bat` is CRLF; PNG/ICO/JAR are binary. This reduces OS-specific diff noise.

Edit when introducing file types needing special Git treatment.

### `.gitignore`

Defines untracked local/generated/sensitive patterns: Gradle/Kotlin caches, Java classes/logs, IDE files, Android `local.properties`, captures, keystores, Kotlin Web `node_modules`/package-store output, Xcode user/DerivedData state, desktop installers, Rust target output, and current `Cargo.lock` policy.

Never rely on this file to protect a secret that was already committed.

### `.mailmap`

Canonical Git contributor identity mapping. Current canonical owner entry is `Sanskar <sanskarin@outlook.in>`.

### `README.md`

Public repository landing page: product overview, artwork, features, cross-platform status, stack, quick start, CPU/timer/backup/private-room explanation, quality gates, privacy, release, support, funding, license.

Keep high-level; link deep material under `docs/` rather than duplicating every implementation detail.

### `CHANGELOG.md`

Version-oriented notable changes. Current v2.5.8 section records timers, seed controls, persisted match setup, profile/trends, backup, localization/responsiveness, private-room architecture/security hardening, cross-platform Android/iOS/Desktop/Web support, tests, CI/release/docs, and synchronized release metadata.

Update for release-visible behavior/security/compatibility changes.

### `ROADMAP.md`

Canonical project roadmap/checklist for the v1.0 baseline, v2.5.8 completion/cross-platform milestone, and future optional/platform-dependent work. This is the source of truth; `docs/ROADMAP.md` only points here.

### `CONTRIBUTING.md`

Contributor setup, owner commit identity, commit style, required verification, PR compatibility/security/network/localization/accessibility expectations.

### `CODE_OF_CONDUCT.md`

Community participation/conduct expectations for repository interactions. Keep behavior/community policy independent from implementation docs.

### `SECURITY.md`

Supported-version security policy, private vulnerability reporting, security boundaries, backup/network constraints, dependency/CI posture, secret policy, coordinated disclosure expectations.

### `PRIVACY.md`

Explains local data, explicit backup text, network/tracking posture, retention/reset, Android automatic-backup exclusion, third-party links, and contact. Must stay synchronized with manifest/data behavior.

### `SUPPORT.md`

Non-security support path, prerequisite docs, contact addresses, bug-report information, safe-reporting guidance, optional funding link.

### `LICENSE`

MIT license text for the project. Legal license file; do not modify casually as ordinary prose.

### `what_changed.md`

Detailed project handoff log requested for this repository workflow. Records implementation, migrations, validation status, limitations, key files, representative commits, v2.5.8 release/cross-platform metadata, and release-notes draft.

### `build.gradle.kts`

Root Gradle plugin declarations using version-catalog aliases with `apply false`. Makes plugin versions available without applying platform plugins to the root project.

### `settings.gradle.kts`

Gradle build composition: plugin repositories, centralized dependency repositories with `FAIL_ON_PROJECT_REPOS`, root project name, included modules `:shared`, `:androidApp`, `:desktopApp`, and `:webApp`.

### `gradle.properties`

Gradle/Kotlin runtime build settings: 3 GiB max JVM heap, UTF-8, build cache, configuration cache, parallel build, official Kotlin style, incremental Kotlin compilation.

## GitHub configuration and automation

### `.github/CODEOWNERS`

Default maintainer ownership for the repository, with explicit coverage for automation/security files, shared gameplay/persistence, Rust, and platform packaging. This routes review attention to `@sanskarIN` when repository branch/ruleset settings use code-owner review requirements.

### `.github/FUNDING.yml`

GitHub funding metadata. Exposes the custom Buy Me a Coffee URL. Funding is optional and does not change app features/license/access.

### `.github/dependabot.yml`

Weekly Dependabot configuration for Gradle at repository root, Cargo under `/rust-engine`, and GitHub Actions. Limits each ecosystem to five open update PRs.

### `.github/pull_request_template.md`

Default PR checklist covering validation, data compatibility/migrations, backup compatibility, security/privacy, optional networking, accessibility, documentation, and release impact.

### `.github/release.yml`

GitHub generated-release-note categorization. Excludes `skip-changelog`/`dependencies`; groups feature/fix/security-reliability/documentation/other labels.

### `.github/ISSUE_TEMPLATE/bug_report.yml`

Structured public bug form. Applies `bug` label; requests platform, problem/expected behavior, reproduction steps, version, context.

### `.github/ISSUE_TEMPLATE/feature_request.yml`

Structured enhancement request. Applies `enhancement` label; requires problem/opportunity and proposed solution and surfaces offline-first/no-tracking project principles.

### `.github/ISSUE_TEMPLATE/config.yml`

Disables blank issues and redirects sensitive security reporting to `SECURITY.md` and support questions to `SUPPORT.md`.

### `.github/workflows/ci.yml`

Primary CI: repository formatting, relative documentation links, exhaustive file-reference coverage, high-confidence committed-secret patterns, Android privacy contract, cross-platform semantic/build-code consistency, JDK/Android/Gradle setup, shared tests, Android lint/debug APK, desktop classes, JS+Wasm Web compatibility distribution, macOS-hosted iOS simulator framework/application build, and independent Rust tests. Pull requests/pushes to `main`; obsolete runs cancelled via concurrency.

### `.github/workflows/codeql.yml`

CodeQL Java/Kotlin security analysis on push/PR to `main` and weekly schedule. Sets JDK/Android/Gradle build environment, builds Android/Desktop code, uploads analysis results.

### `.github/workflows/security.yml`

Focused security workflow for pushes/PRs to `main`. Rechecks committed-secret and Android privacy contracts and, on pull requests, runs GitHub dependency review with high-severity findings configured to fail the job.

### `.github/workflows/release.yml`

Manual/tag release validation and packaging. Repeats formatting, docs-link, exhaustive documentation, secret-pattern, Android privacy, and semantic/build-code source gates before Android release tests/build; also builds Linux DEB, JS+Wasm Web compatibility ZIP, iOS device/simulator framework ZIPs with simulator host validation, and Rust crate artifacts. Tag runs merge artifacts, create SHA-256 sums, and publish GitHub Release.

## Android application module

### `androidApp/build.gradle.kts`

Android app plugin/configuration: namespace/application ID, SDK levels, Android version code/name, Compose/BuildConfig, packaging exclusion, dependencies on shared UI/logic and Activity Compose. Version 2.5.8 uses Android `versionCode = 20508`.

### `androidApp/src/main/AndroidManifest.xml`

Android application/component declaration. No permissions are declared. Explicitly disables Android automatic app backup, points platform backup/data-transfer APIs at exclusion rules, defines icons/label/RTL/theme, and exports only launcher `MainActivity` with MAIN/LAUNCHER intent filter.

### `androidApp/src/main/kotlin/in/sanskar/rpsarena/MainActivity.kt`

Android entry point. Initializes Android `PlatformStore` with Context, then renders shared `RpsArenaApp()` through Activity Compose.

### `androidApp/src/main/res/drawable/ic_launcher_foreground.xml`

Android vector adaptive-icon foreground artwork: purple field, white disc, purple bar motif.

### `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

Adaptive launcher icon definition using background color + vector foreground.

### `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

Round-icon adaptive definition; currently same layers as standard adaptive launcher icon.

### `androidApp/src/main/res/values/colors.xml`

Defines `ic_launcher_background` purple used by adaptive icons.

### `androidApp/src/main/res/values/themes.xml`

Android window shell theme `Theme.RpsArena`: no ActionBar, sans font, purple accent, dark system bars. Shared Compose theme owns most in-app colors.

### `androidApp/src/main/res/xml/backup_rules.xml`

Legacy Android full-backup policy. Excludes the entire `sharedpref` domain so the app-private `rps_arena` preferences are not copied by legacy automatic backup mechanisms.

### `androidApp/src/main/res/xml/data_extraction_rules.xml`

Android 12+ data-extraction policy. Excludes the entire `sharedpref` domain from both cloud backup and device-to-device transfer, keeping settings/statistics/history/profile data under explicit in-app backup control.

## iOS and iPadOS application

### `iosApp/iosApp.xcodeproj/project.pbxproj`

Native Xcode project definition for the `RPS Arena` iOS target. Defines Swift sources, iPhone/iPad device families, iOS deployment baseline, version/build metadata, framework search/link settings, and the `Compile Kotlin Framework` direct-integration build phase that invokes `gradle :shared:embedAndSignAppleFrameworkForXcode`.

### `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/RPS Arena.xcscheme`

Shared Xcode scheme used by developers and CI. Makes the iOS application target discoverable to command-line `xcodebuild` and keeps Debug/Release build actions reproducible across checkouts.

### `iosApp/iosApp/iOSApp.swift`

SwiftUI `@main` application entry point. Opens `ContentView` and intentionally keeps native application-shell logic thin.

### `iosApp/iosApp/ContentView.swift`

SwiftUI bridge around the Kotlin `MainViewController()` using `UIViewControllerRepresentable`. Imports the generated `RpsArenaShared` framework and hosts the same shared Compose UI used by other platforms.

### `iosApp/iosApp/Info.plist`

Native app metadata: display/bundle identity, semantic version `2.5.8`, build `20508`, iPhone/iPad orientation support, launch-screen metadata, and `CADisableMinimumFrameDurationOnPhone` for Compose rendering behavior.

## Root visual assets

### `assets/logo.svg`

Editable repository logo source used by README/project presentation. 1024-square purple rounded logo, white arena disc/bar motif, accessible title/description.

### `assets/splash.svg`

Editable 1600x900 branding artwork with gestures, title, and credit. Documentation/marketing artwork; not automatically wired as Android/iOS native startup splash.

## Desktop application module

### `desktopApp/build.gradle.kts`

Desktop Kotlin JVM/Compose build: Java 17 toolchain, dependency on `:shared` and current-OS Compose runtime, main class, DMG/MSI/DEB native distribution metadata/version/vendor. Current package version is 2.5.8.

### `desktopApp/src/main/kotlin/in/sanskar/rpsarena/desktop/Main.kt`

Desktop entry point: initializes desktop store, opens one Compose Window titled RPS Arena, exits on close, renders shared app. The same JVM desktop application runs on supported Windows, Linux, and macOS hosts.

## Web application module

### `webApp/build.gradle.kts`

Dedicated browser application module. Declares executable Kotlin/JS and Kotlin/Wasm browser targets, applies the default Web hierarchy, depends on `:shared`, and supports Compose compatibility distribution packaging.

### `webApp/src/webMain/kotlin/in/sanskar/rpsarena/web/Main.kt`

Shared JS/Wasm browser entry point. Initializes Web storage, attaches `ComposeViewport` to the `webApp` HTML container, and renders `RpsArenaApp()`.

### `webApp/src/webMain/resources/index.html`

Browser host page with UTF-8/responsive metadata, full-window CSS, theme/description metadata, `#webApp` viewport container, and a no-JavaScript fallback message.

## Gradle version catalog

### `gradle/libs.versions.toml`

Central versions/aliases for Kotlin, Compose, AGP, AndroidX Activity Compose, coroutines, `kotlinx-browser`, Android compile/target/min SDK, libraries, and Gradle plugins.

## Optional Rust engine

### `rust-engine/Cargo.toml`

Rust crate metadata: `rps-arena-engine`, version 0.1.0, edition 2024, MIT, repository metadata, `rlib` crate type.

### `rust-engine/README.md`

Short crate-local README for the optional Rust rules engine. Keep its concise commands/scope aligned with deeper `docs/rust-engine.md`.

### `rust-engine/src/lib.rs`

Five-gesture Rust rule mirror, `Outcome`, `resolve()` win-pair logic, and representative classic/extended unit tests. Not linked into Kotlin runtime.

## Repository scripts

### `scripts/check_format.py`

Read-only text policy checker. Recursively validates relevant tracked/worktree text for UTF-8, final newline, accidental trailing whitespace; permits standard Markdown two-space hard breaks; skips generated/cache directories.

### `scripts/check_docs_links.py`

Read-only Markdown relative-link validator. Scans repository Markdown, skips external/mail/data/anchor-only targets, prevents paths escaping repository root, and fails when an internal relative target does not exist.

### `scripts/check_docs_coverage.py`

Uses `git ls-files -z` and this file to ensure every tracked path is explicitly named in backticks. Fails when documentation file-reference coverage is incomplete.

### `scripts/check_for_secrets.py`

Read-only high-confidence committed-secret scanner for private-key blocks and recognizable GitHub/AWS/Google/generic secret-token formats. It skips generated/IDE directories, large/binary files, and itself to avoid self-matching the detector patterns.

### `scripts/check_android_privacy.py`

Parses the Android manifest and both backup-policy XML files. Fails if automatic backup becomes enabled, backup/data-transfer rule references or SharedPreferences exclusions disappear, XML is invalid, or `android.permission.INTERNET` is introduced into the primary offline-first Android manifest.

### `scripts/check_version.py`

Checks Android `versionName`/`versionCode`, desktop `packageVersion`, iOS `CFBundleShortVersionString`/`CFBundleVersion`, Xcode `MARKETING_VERSION`/`CURRENT_PROJECT_VERSION`, and shared `APP_VERSION`; verifies About renders the shared version constant; and requires mobile build codes to match `major * 10000 + minor * 100 + patch`.

### `scripts/verify.sh`

Bash full verification entry point: formatting, docs links/coverage, secret patterns, Android privacy, version, shared tests, Android lint/build, desktop classes, optional Cargo tests. Platform-specific iOS/Web build commands are additionally documented and exercised by CI.

### `scripts/verify.ps1`

PowerShell equivalent of repository verification for Windows environments with the same source/build/test gates.

## Shared module build

### `shared/build.gradle.kts`

Kotlin Multiplatform shared library build. Configures Android/JVM 17, desktop JVM 17, iOS device/simulator frameworks, Kotlin/JS browser, Kotlin/Wasm browser, default hierarchy with shared `webMain`, Compose/coroutines common dependencies, Kotlin tests, and Compose desktop UI-test dependencies.

## Shared platform storage adapters

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.kt`

Common `expect object` storage contract: initialize/getString/putString. Keeps platform APIs out of common repository logic.

### `shared/src/androidMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.android.kt`

Android `actual PlatformStore` backed by app-private `SharedPreferences` named `rps_arena`. Requires Context initialization before shared app state reads persistence. Android platform backup rules explicitly exclude this preferences store from automatic cloud/device-transfer backup.

### `shared/src/desktopMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.desktop.kt`

Desktop `actual PlatformStore` backed by `java.util.prefs.Preferences` node `in/sanskar/rpsarena`; no context required.

### `shared/src/iosMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.ios.kt`

iOS/iPadOS `actual PlatformStore` backed by `NSUserDefaults.standardUserDefaults`; no platform context is required. Common `ArenaRepository` codecs/validation remain authoritative.

### `shared/src/webMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.web.kt`

Shared Kotlin/JS + Kotlin/Wasm `actual PlatformStore` backed by browser `window.localStorage`; no platform context is required. Browser origin/profile controls physical persistence lifetime.

## Shared iOS bridge

### `shared/src/iosMain/kotlin/in/sanskar/rpsarena/ui/MainViewController.kt`

Kotlin/Native iOS entry bridge. Initializes iOS storage and exposes a `UIViewController` created by `ComposeUIViewController { RpsArenaApp() }` for the SwiftUI host.

## Shared data layer

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaRepository.kt`

Persistence/codec/migration/history/trend/backup/reset authority. Contains injected storage functions, settings v1/v2 decoding, stats invariants, history bounds, backup schema/escaping/strict import validation, keys/limits.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/BackupModels.kt`

Small `BackupImportResult(imported, message)` data model with success/failure factory helpers.

## Shared engine

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/engine/RulesEngine.kt`

Authoritative five-gesture defeat matrix, winner resolver, and counter lookup used by CPU strategy.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/engine/CpuStrategy.kt`

Seeded local CPU: Easy random; Normal mostly random with later last-move counter behavior; Expert mostly frequency-based prediction after enough history; always variant-filtered.

## Shared logging

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/logging/SafeLogger.kt`

Transport/sink-neutral structured logger utility. The default sink is no-op, so it creates no telemetry. Optional sinks receive bounded string fields after key-based redaction of password/token/authorization/cookie/email/backup/content/payload-like values; event names must use bounded lowercase snake_case.

## Shared model

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/model/GameModels.kt`

All primary enums/data: gestures/variants/opponents/difficulty/modes/outcomes/end reasons/languages, `MatchConfig` timer/target validation, round/match snapshots, stats, settings, trend, achievement model.

## Shared private-room architecture

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/network/PrivateRoom.kt`

Room-code grammar, roles/participants/events, gateway/session interfaces, deterministic two-participant in-memory implementation, sender/lifecycle/round validation and idempotent close. No real network I/O.

## Shared state

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/state/ArenaState.kt`

Runtime Compose-observable orchestration: screens/settings/stats/config/match, CPU/local play, timers, score/finish/streak/history updates, achievements, backup/import/reset, local pending gesture.

## Shared UI/localization/design

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt`

Shared Compose application/screens: onboarding/scaffold/home/play/history/stats/achievements/settings/about, config controls/timer/result/cards, responsive FlowRows, backup/reset UI, history/data localization adapters.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaStrings.kt`

Typed English/Hindi core UI catalogs plus enum-keyed gesture/difficulty/match-mode labels and backup/result/settings copy.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AchievementStrings.kt`

English/Hindi achievement title/description mapping by stable achievement ID with safe unknown-ID fallback.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AppMetadata.kt`

Language-independent shared constants `APP_VERSION` and `APP_LICENSE` used by About/version validation. Current application version is 2.5.8.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaDesign.kt`

Shared layout dimensions: screen padding, max content width, compact/section spacing, gesture minimum height.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaTheme.kt`

Material 3 branded light/dark color schemes, component shapes, system/dark-theme selection, shared theme wrapper.

## Shared common tests

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/RulesEngineTest.kt`

Classic/extended winner examples and draw for every gesture.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/CpuStrategyTest.kt`

Same-seed sequence determinism and Classic variant safety against extended gestures.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/MatchConfigTest.kt`

Allowed/invalid timers and expected finite/endless win targets.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryCodecTest.kt`

Settings/stat encode-decode round trips.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryBackupTest.kt`

Backup round trip, non-destructive malformed import, legacy settings migration, newest-history trend behavior.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryValidationTest.kt`

Stats invariant rejection, name sanitation/bounds/default, unknown backup record rejection before destructive writes, history bound/newline sanitation.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStateTest.kt`

CPU/local timeout behavior, disabled-timer no-op, seeded CPU replay through full state, backup restore live-state refresh.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/PrivateRoomTest.kt`

Room-code normalization/rejection, peer exchange, sender/round/lifecycle validation, close idempotency, two-participant limit.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStringsTest.kt`

English/Hindi enum label coverage, English canonical gesture-label agreement, shared semantic version shape.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/AchievementStringsTest.kt`

Known achievement English/Hindi distinction/nonblank copy and safe unknown-ID fallback.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/SafeLoggerTest.kt`

Verifies sensitive-field redaction before a sink can observe data, non-sensitive field length bounding, and rejection of invalid event names.

## Shared desktop UI test

### `shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt`

Compose desktop smoke journeys for onboarding -> play, language switch, Hindi gameplay/achievement copy, backup/import controls, and reset confirmation.

## Documentation files

### `docs/documentation-index.md`

Audience/goal-based map of the documentation set and recommended reading paths.

### `docs/setup.md`

Concise prerequisites, clone/verify/run guidance, optional Rust note, local data and Git identity.

### `docs/toolchain.md`

Deep JDK/Gradle/Android SDK/IDE/Python/Git/Rust installation/version/upgrade guidance and compatibility-upgrade procedure.

### `docs/command-reference.md`

Detailed meaning/side effects of repository verification, Gradle, Android, desktop, iOS, Web, Rust, Git, tag, and diagnostic commands.

### `docs/build-system.md`

Root Gradle/settings/properties/version catalog, module graph, shared source sets/targets, platform app packaging, generated output and no-wrapper architecture.

### `docs/development.md`

Concise architecture-aware developer workflow, persistence/timer/network/localization boundaries, commit style.

### `docs/maintenance.md`

Long-term maintenance playbook: style/Git, versions, dependencies, gameplay/data/language/network/platform changes, docs, release, incidents.

### `docs/architecture.md`

High-level KMP modules/layers, persistence/determinism/timers/private-room/privacy boundaries.

### `docs/domain-and-gameplay.md`

Detailed gestures/rules/CPU thresholds/match modes/state transitions/timers/scoring/streaks/achievements/history/invariants.

### `docs/storage-and-backup.md`

Exact PlatformStore behavior, keys/codecs, settings migration, stats invariants, history format, backup grammar/escaping/limits/transaction-like validation/reset.

### `docs/localization.md`

English/Hindi architecture, canonical vs display values/history, achievement copy, persisted language, adding languages/RTL/testing, typed-message future path.

### `docs/private-room-protocol.md`

Current room contracts/reference lifecycle plus future LAN authority/fairness/versioning/input-bound/privacy/failure/security requirements.

### `docs/android-platform.md`

Every Android app/resource/storage file, SDK/manifest/launcher/theme/icon/backup-policy/APK/signing/offline behavior and validation.

### `docs/ios-platform.md`

Complete iPhone/iPad platform guide: Kotlin/Native targets, NSUserDefaults, Compose UIViewController bridge, SwiftUI/Xcode host, versioning, CI, framework packaging, signing/privacy boundaries.

### `docs/desktop-platform.md`

Every desktop app/storage file, JVM/window/native package formats, UI tests, host-dependent signing/package behavior for Windows/Linux/macOS.

### `docs/web-platform.md`

Complete browser platform guide: Kotlin/JS + Kotlin/Wasm compatibility mode, ComposeViewport entry point, localStorage persistence, development/distribution commands, CI/release artifacts, limitations and testing.

### `docs/rust-engine.md`

Every Rust crate file, rule parity, Cargo tests/package, dependency/lock/FFI/WASM/security boundaries.

### `docs/branding-assets.md`

Root SVGs, Android launcher assets, shared theme ownership, rebranding/accessibility/export checks.

### `docs/testing.md`

Commands and broad automated/manual release test strategy.

### `docs/test-catalog.md`

Every tracked automated Kotlin/Compose test with exact regression responsibility/assertions.

### `docs/validation.md`

Executable validation contract: CI/CodeQL/security/release gate and local parity commands.

### `docs/accessibility.md`

Current accessibility baseline and manual keyboard/TalkBack/text-scale/contrast/motion/timer/destructive-action review policy; iOS VoiceOver/browser keyboard review belongs in platform validation when those surfaces change.

### `docs/performance.md`

Bounded data/performance targets, safeguards, measurement workflow, networking budget.

### `docs/troubleshooting.md`

JDK/SDK/Gradle/desktop/backup/settings/timer/private-room/CI troubleshooting.

### `docs/ci-cd.md`

Every `.github` automation/config: triggers, permissions, jobs, Android/Desktop/iOS/Web/Rust artifacts, checksums, Dependabot, issue/PR/funding/release/security behavior.

### `docs/release.md`

Release eligibility, Android/Desktop/iOS/shared version locations, Web/iOS artifacts, local verification, signing/notarization boundaries, notes/rollback.

### `docs/github-settings.md`

Recommended GitHub-hosted branch rules, merge policy, Actions/security features, Discussions, labels, milestones, metadata/release settings.

### `docs/glossary.md`

Technical/project terminology from KMP/AGP/Compose/CI/CodeQL through repository-specific schemas/identifiers and non-implemented feature terms.

### `docs/ROADMAP.md`

Deliberately tiny pointer to canonical root `ROADMAP.md`; prevents duplicate roadmap state.

### `docs/adr/0001-offline-first-kmp.md`

Architecture Decision Record: use offline-first Kotlin Multiplatform shared model/rules/state/persistence/UI with thin platform modules.

### `docs/adr/0002-private-room-boundary.md`

Architecture Decision Record: keep private-room networking optional behind transport contracts; primary gameplay remains network-independent.

### `docs/repository-file-reference.md`

This exhaustive manifest. It documents itself because the coverage checker requires every tracked path, including this reference, to appear here.

## Coverage rule

Run:

```bash
python3 scripts/check_docs_coverage.py
```

The script obtains the authoritative list from Git itself:

```text
git ls-files -z
```

For each tracked path it requires the exact backtick-wrapped path to occur in this file. Therefore:

- new source file -> add it here;
- new documentation file -> add it here;
- new workflow/resource/test -> add it here;
- rename/delete -> update this file in the same change.

This keeps the repository's "document every file" requirement continuously enforceable.
