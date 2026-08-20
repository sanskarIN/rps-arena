# Testing Guide

## Repository source/documentation/security gates

Before compilation, the primary CI job verifies repository source/documentation/privacy integrity:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

These respectively verify text formatting, repository-relative Markdown links, every tracked file's presence in the exhaustive file reference, high-confidence committed-secret patterns, Android offline/automatic-backup privacy invariants, and synchronized application version metadata.

The focused `Security checks` workflow independently repeats the secret/privacy checks and reviews pull-request dependency changes for high-severity findings.

## Required shared suite

```bash
gradle :shared:allTests --stacktrace
```

The Android KMP library target explicitly enables host-side tests with `withHostTest {}`. This prevents common tests from silently skipping Android-host compilation under the Android-KMP plugin, where host and device tests are disabled by default.

Coverage includes:

- every classic rule direction and draw behavior;
- extended Lizard–Spock rules;
- seeded CPU determinism and allowed-gesture constraints;
- replayable seeded CPU behavior through `ArenaState`;
- match timer and win-target invariants;
- settings/stat codec round trips;
- legacy settings migration;
- invalid-statistics rejection;
- local player-name sanitization and bounds;
- recent-history bounds and newline sanitization;
- versioned backup export/import, unknown-record rejection, and non-destructive malformed-import behavior;
- recent win/loss/draw trend aggregation;
- CPU and local-two-player timeout scoring;
- backup restore refreshing in-memory state;
- English/Hindi gesture, difficulty, match-mode, version metadata, and achievement-copy catalogs;
- private-room code validation, normalized value semantics, independently parsed room-key equality, two-participant limits, sender validation, positive-round validation, lifecycle-event authority, event exchange, and idempotent close behavior;
- no-op structured logger sensitive-field redaction, output bounds, and event-name validation.

See `docs/test-catalog.md` for a file-by-file description of every automated test.

## Android KMP host tests

Host-side tests are enabled in `shared/build.gradle.kts`:

```kotlin
android {
    // ...
    withHostTest {}
}
```

Run the Android host-test target directly with:

```bash
gradle :shared:testAndroidHostTest --stacktrace
```

These tests execute on the host JVM; they do not replace emulator/device instrumentation or accessibility review. Their purpose is to ensure shared/common test code continues compiling and behaving correctly through the Android KMP target as well as the desktop/native/web targets.

## Compose desktop UI smoke tests

The `desktopTest` source set uses Compose Multiplatform's v2 UI-test runner (`androidx.compose.ui.test.v2.runComposeUiTest`). The current smoke suite verifies:

- onboarding reaches the home screen and primary Play journey;
- Rock/Paper/Scissors controls are rendered on the primary gameplay screen;
- Settings can switch core navigation copy from English to Hindi;
- Hindi gameplay renders localized Rock/Paper/Scissors labels;
- Hindi achievements render localized title and description copy;
- backup/import controls are exposed;
- local-data reset requires explicit confirmation.

Run them directly with:

```bash
gradle :shared:desktopTest --stacktrace
```

They are also included in the shared test gate for the supported desktop target.

## Android privacy verification

Run:

```bash
python3 scripts/check_android_privacy.py
```

The source-level checker parses the Android manifest and both backup-policy XML files. It fails if the primary manifest gains internet permission, automatic backup becomes enabled, backup resource references drift, or SharedPreferences cease to be excluded from legacy/cloud/device-transfer backup.

This test is intentionally separate from Android Lint because the repository treats those privacy choices as product invariants, not merely platform syntax.

## Android quality/build verification

```bash
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

This verifies Android lint, packaging, shared Android compilation, resources, launcher assets, and the primary entry point.

Android device/emulator instrumentation remains a platform-dependent follow-up; the repository does not pretend that host-side or desktop UI tests are equivalent to TalkBack or physical-device behavior.

## Desktop build verification

```bash
gradle :desktopApp:classes --stacktrace
```

For native packaging on a supported host OS:

```bash
gradle :desktopApp:packageDistributionForCurrentOS --stacktrace
```

## Web build verification

The CI/release Web gate builds the combined Kotlin/Wasm + Kotlin/JS compatibility distribution:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

This exercises the shared Web source set, Kotlin web toolchain setup, both browser backends, and compatibility packaging. Browser interaction, persistence, keyboard/touch, and accessibility behavior still require the manual checks documented in `docs/web-platform.md`.

## iOS/iPadOS verification

On macOS, build the simulator framework:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

Then validate the unsigned simulator host:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

This verifies Kotlin/Native compilation, the exported framework, SwiftUI host integration, and simulator linkage without requiring private Apple signing credentials.

## Rust engine

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

The Rust engine is optional and independent of the Kotlin app, but its tests remain part of CI.

## Manual product checks

Before release, verify:

1. onboarding completion persists;
2. CPU match works in Classic and Lizard–Spock variants;
3. local two-player hides Player 1's gesture before Player 2 chooses;
4. every match format resets and finishes correctly;
5. replaying the same seed and player move sequence produces the same CPU choices;
6. timers restart per turn and score the correct timeout winner;
7. recent trend numbers match recent history;
8. player name and language persist across restart;
9. English/Hindi changes update gameplay choices, round results, history rendering, settings feedback, and achievements without changing stored game rules;
10. match-mode and timer chips wrap instead of clipping on a narrow phone-width viewport;
11. backup export can restore settings, stats, and history after a reset;
12. malformed backup text is rejected without overwriting valid local data;
13. reduced-motion mode removes result transition animation;
14. Android automatic backup remains disabled and explicit text export remains functional;
15. keyboard and TalkBack/VoiceOver/browser accessibility checks from `docs/accessibility.md` pass;
16. the Web compatibility build starts in both Wasm-capable and JS-fallback paths;
17. the iOS simulator host renders the shared application and retains local settings through NSUserDefaults.

## CI gate

`.github/workflows/ci.yml` runs formatting, relative docs links, tracked-file documentation coverage, secret patterns, Android privacy, version consistency, shared tests, Android lint/debug assembly, desktop classes, the Web compatibility distribution, the iOS simulator framework/SwiftUI host build, and Rust tests.

`.github/workflows/security.yml` repeats focused security/privacy checks and dependency review. `.github/workflows/codeql.yml` performs Kotlin/Java static security analysis.

A release candidate should not be merged while any required check is failing or still pending on the exact candidate SHA.
