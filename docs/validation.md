# Validation

RPS Arena treats executable CI evidence as the release gate. A document saying that a check should pass is not a substitute for the check actually passing on the candidate commit.

## Required automated checks

The `CI` workflow requires:

- repository text formatting check;
- relative Markdown documentation-link check;
- exhaustive tracked-file documentation-reference coverage check;
- high-confidence committed-secret pattern check;
- Android offline/automatic-backup privacy-contract check;
- Android/desktop/iOS/shared/About semantic-version consistency plus deterministic Android/iOS numeric build-code mapping;
- shared Kotlin test suite, including logger/privacy-adjacent business tests and desktop UI tests;
- Android lint;
- Android debug assembly;
- desktop JVM compilation;
- JS+Wasm Web compatibility distribution build;
- iOS simulator Kotlin framework build on macOS;
- iOS SwiftUI/Xcode simulator host build without signing;
- optional Rust engine tests.

The separate `CodeQL` workflow analyzes Kotlin/Java code.

The focused `Security checks` workflow independently re-runs the secret/privacy source checks and, for pull requests, performs dependency review with high-severity findings configured to fail.

## Source-quality gates

Run all fast source checks before compilation:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

### Documentation completeness

`scripts/check_docs_coverage.py` obtains every Git-tracked path with `git ls-files` and requires that exact path to appear in backticks inside `docs/repository-file-reference.md`.

This prevents source/config/workflow/resource/test/documentation/platform-host files from being added silently without being represented in the exhaustive file reference.

The check proves path coverage, not that every explanation is perfect. Human review still evaluates documentation correctness/depth.

### Documentation links

`scripts/check_docs_links.py` validates repository-relative Markdown link targets and rejects links that escape the repository root or resolve to missing files. External URLs are intentionally outside this offline source check.

### Committed-secret patterns

`scripts/check_for_secrets.py` looks for several high-confidence credential/private-key forms while skipping generated/IDE output, large files, binaries, and its own detector source. It is defense in depth, not a claim that every possible secret format can be recognized.

### Android privacy contract

`scripts/check_android_privacy.py` parses the primary Android manifest plus legacy/Android 12+ backup rules. It fails if automatic backup is re-enabled, SharedPreferences backup/device-transfer exclusions disappear, XML becomes invalid, or the primary manifest gains `android.permission.INTERNET`.

### Version consistency

`scripts/check_version.py` requires Android `versionName`, desktop `packageVersion`, iOS `CFBundleShortVersionString`/Xcode `MARKETING_VERSION`, and shared `APP_VERSION` to match. It verifies About renders the shared constant and requires Android `versionCode`, iOS `CFBundleVersion`, and Xcode `CURRENT_PROJECT_VERSION` to equal:

```text
major * 10000 + minor * 100 + patch
```

For v2.5.8, the required mobile build code is `20508`.

## Cross-platform build evidence

### Android

```bash
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

### Windows/Linux/macOS desktop

```bash
gradle :desktopApp:classes --stacktrace
```

Native installer/package tasks remain host-dependent and are validated in release/manual platform workflows as appropriate.

### Web

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

This is the CI build gate for the combined Kotlin/Wasm + Kotlin/JS browser distribution.

### iOS/iPadOS

Requires macOS:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

This validates the Kotlin/Native framework and SwiftUI host without requiring private Apple signing credentials.

## Release validation

The tag/manual workflow repeats repository formatting, documentation links, exhaustive documentation coverage, committed-secret patterns, Android privacy contract, version verification, shared tests, Android release lint/build, desktop Linux packaging, JS+Wasm Web packaging, iOS device/simulator framework builds plus simulator-host validation, and Rust package tests before a tagged release can publish public artifacts and checksums.

Release tags must still be created from validated `main`: the release workflow is a second release-specific gate, not a replacement for pull-request CI, Security checks, and CodeQL on the exact candidate commit. If release workflow behavior changes, keep `docs/ci-cd.md`, `docs/release.md`, and this file synchronized.

Signing credentials are intentionally outside the public repository and are not required to validate the open-source builds.

## Local parity commands

Portable/common gate:

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

Run the iOS commands above on macOS for iPhone/iPad changes or full release parity.

The shell and PowerShell helpers in [`scripts/`](../scripts/) cover the repository-level gates available on their host. CI adds host-specific iOS validation on macOS.

## Manual evidence

Before a stable tag, complete the journeys in [`testing.md`](testing.md), the accessibility checks in [`accessibility.md`](accessibility.md), and the relevant platform guide. Record any unresolved blocker as an explicit known limitation instead of declaring a clean release.

For cross-platform changes manually verify:

- shared gameplay behavior stays equivalent across targets;
- platform-local persistence survives a normal app/browser restart;
- English/Hindi copy and responsive layouts remain usable;
- Android TalkBack, iOS VoiceOver, desktop keyboard, and browser keyboard/pointer behavior are reviewed where relevant;
- Web Wasm and JS compatibility startup paths are exercised when Web packaging changes;
- signing credentials remain outside Git and public CI;
- README/documentation-index links point to tracked files;
- every new file has a useful explanation, not merely a filename mention.

## Exact-head rule

Required checks must pass on the exact commit intended for merge.

Because CI, Security checks, and CodeQL use `cancel-in-progress: true`, rapid commits can cancel obsolete runs. A green older SHA does not validate a newer documentation/code head.

Do not merge while the current candidate's required jobs are queued, in progress, cancelled without replacement, or failed.

## Validation history

The v1.0.0 build audit established the Kotlin/Android/Desktop/Rust/CodeQL baseline. Version 2.5.8 extends that gate with formatting, Android/Desktop/iOS/shared synchronized versions, deterministic mobile build-code validation, iOS/iPadOS Kotlin/Native + SwiftUI host compilation, Web JS+Wasm compatibility builds, Android lint, persistence migration/backup, timeout, trend, localization/UI, private-room protocol regression coverage, exhaustive tracked-file documentation coverage, relative-link validation, committed-secret detection, fail-closed Android backup/privacy checks, dependency review, and redacting logger regression coverage.

`what_changed.md` is the handoff source for the exact current validation result and most recent meaningful commits.
