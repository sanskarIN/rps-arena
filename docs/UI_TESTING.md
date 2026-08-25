# UI testing

RPS Arena uses platform-appropriate Compose UI test harnesses while keeping selectors and test data deterministic across targets.

## What the suites cover

The desktop `RpsArenaUiTest` verifies high-value flows including onboarding persistence, home/play navigation, visible round results, reduced-motion persistence, and backup export.

The Android `RpsArenaAndroidUiTest` provides instrumentation smoke coverage for onboarding → home and home → play → gesture → visible round result.

Tests identify controls through `ArenaUiTags`, not translated labels. This keeps automation stable when the runtime language changes.

## Deterministic persistence

Production uses `PlatformArenaStore`, which delegates to platform storage. UI tests inject an in-memory `ArenaStore` into `ArenaRepository` so test data is isolated and never writes developer/user preferences.

## Shared logic tests

Non-rendering rules, repository, codec, state, localization, room-contract, logging, and other platform-neutral tests live in `shared/src/commonTest`.

The Android KMP target enables `withHostTest {}`. Compose rendering tests are deliberately not kept in `commonTest`, because desktop rendering infrastructure is not valid inside the Android host JVM environment.

## Desktop UI tests

```text
shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt
```

Run:

```bash
gradle :shared:desktopTest
```

`desktopTest` owns the Compose UI-test dependency and desktop runtime required by `runComposeUiTest`.

## Android instrumentation tests

```text
shared/src/androidDeviceTest/kotlin/in/sanskar/rpsarena/RpsArenaAndroidUiTest.kt
```

The Android KMP target uses a device-test source tree and `androidx.test.runner.AndroidJUnitRunner`.

Compile the instrumentation APK without an emulator:

```bash
gradle :shared:assembleAndroidDeviceTest
```

On a configured connected device/emulator, run:

```bash
gradle :shared:connectedAndroidDeviceTest
```

The instrumentation-only manifest supplies a `ComponentActivity` host. It does not alter the production Android manifest or add production permissions.

## Test split

```text
commonTest        -> non-rendering shared tests
desktopTest       -> runComposeUiTest + desktop Compose runtime
androidDeviceTest -> AndroidJUnitRunner + createAndroidComposeRule
```

This prevents a desktop rendering harness from being executed as Android host-JVM tests while retaining common selectors and deterministic persistence patterns.

## Full verification

Unix-like systems:

```bash
./scripts/verify.sh
```

Windows PowerShell:

```powershell
./scripts/verify.ps1
```

The verification scripts include source gates, localization validation, shared tests, desktop UI tests, Android instrumentation packaging, Android lint/build, desktop compilation, Web compatibility distribution, optional macOS iOS framework validation, and optional Rust tests.

## CI behavior

Pull requests run:

1. source/documentation/privacy/version/localization gates;
2. shared/common tests, including Android host tests;
3. desktop UI tests;
4. Android device-test APK assembly;
5. Android lint/debug build;
6. desktop compilation;
7. Web compatibility distribution;
8. iOS simulator framework/Xcode host validation on macOS;
9. Rust tests in a separate job.

Hosted CI compiles the Android instrumentation APK but does not launch an emulator. Actual instrumentation execution remains available on a connected Android-capable runner.

## Adding a UI test

1. Prefer a stable `ArenaUiTags` constant rather than visible text matching.
2. Keep non-rendering platform-neutral tests in `commonTest`.
3. Put desktop rendering tests in `desktopTest`.
4. Put Android instrumentation rendering tests in `androidDeviceTest`.
5. Inject isolated `ArenaStore` persistence when state is involved.
6. Assert user-observable state changes where possible.
7. Run `allTests`, `desktopTest`, and `assembleAndroidDeviceTest` before merge.
