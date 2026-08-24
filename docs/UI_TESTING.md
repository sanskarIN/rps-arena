# UI testing

RPS Arena uses platform-appropriate Compose UI test harnesses while keeping selectors and test data deterministic across targets.

## What the suites cover

The desktop `RpsArenaUiTest` verifies these high-value flows:

- onboarding completion persists and opens the home screen;
- home → play → home navigation works;
- choosing a gesture produces a visible round result;
- the reduced-motion setting persists through the repository;
- backup export opens the expected dialog.

The Android `RpsArenaAndroidUiTest` provides instrumentation smoke coverage for:

- onboarding → home;
- home → play → gesture → visible round result.

Tests identify controls through `ArenaUiTags`, not translated labels. This keeps automation stable when the runtime language changes.

## Deterministic persistence

Production uses `PlatformArenaStore`, which delegates to the existing Android `SharedPreferences` or desktop `Preferences` implementation. UI tests inject an in-memory `ArenaStore` into `ArenaRepository` so test data is isolated, repeatable, and never writes to a developer's real RPS Arena preferences.

## Shared logic tests

Platform-neutral rules, repository, codec, state, localization, and other non-rendering tests remain in `shared/src/commonTest`.

The Android KMP target enables `withHostTest {}` so these common JVM-compatible tests are also exercised by the Android host-test task. Compose UI rendering tests are deliberately not placed in `commonTest`, because the desktop Compose test harness is not valid inside the Android JVM host-test environment.

## Desktop UI tests

Desktop Compose UI tests live in:

```text
shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt
```

Run them with:

```bash
gradle :shared:desktopTest
```

`desktopTest` owns the Compose Multiplatform UI-test dependency and the current desktop runtime required by `runComposeUiTest`.

## Android instrumentation tests

Android instrumentation UI tests live in:

```text
shared/src/androidDeviceTest/kotlin/in/sanskar/rpsarena/RpsArenaAndroidUiTest.kt
```

The Android KMP target opts into a device-test compilation with `sourceSetTreeName = "test"` and uses `androidx.test.runner.AndroidJUnitRunner`.

Compile the Android instrumentation test APK without an emulator:

```bash
gradle :shared:assembleAndroidDeviceTest
```

Run the tests on a connected device or emulator:

```bash
gradle :shared:connectedAndroidDeviceTest
```

The device-test manifest uses `androidx.activity.ComponentActivity` as the test host. Android tests use `createAndroidComposeRule<ComponentActivity>()`, while desktop tests use the Compose Multiplatform v2 `runComposeUiTest` API.

## Why the suites are separated

The UI selectors, repository injection, and user-flow intent are shared, but the rendering harness is platform-specific:

```text
commonTest       -> non-rendering shared tests

desktopTest      -> runComposeUiTest + desktop Compose runtime

androidDeviceTest -> AndroidJUnitRunner + createAndroidComposeRule
```

This prevents desktop UI infrastructure from being executed as Android host-JVM tests while still validating the same important product flows on both rendering environments.

## Full verification

Unix-like systems:

```bash
./scripts/verify.sh
```

Windows PowerShell:

```powershell
./scripts/verify.ps1
```

The verification scripts run localization catalog validation, shared tests, desktop UI tests, Android device-test APK assembly, Android debug assembly, desktop compilation, and optional Rust tests.

## CI behavior

Pull requests and pushes to `main` run:

1. localization catalog validation;
2. shared tests, including Android host tests;
3. desktop UI tests;
4. Android device-test APK assembly;
5. Android debug assembly;
6. desktop JVM compilation;
7. Rust tests in the separate Rust job.

CI intentionally compiles the Android device-test APK without launching an emulator. Actual Android instrumentation execution remains available through `connectedAndroidDeviceTest` on a developer machine or an Android-capable test runner. This keeps normal pull-request CI deterministic while still catching instrumentation source/dependency/manifest compilation regressions.

## Adding a UI test

1. Prefer a stable constant in `ArenaUiTags` instead of matching visible text.
2. Keep non-rendering platform-neutral tests in `shared/src/commonTest`.
3. Put desktop Compose rendering tests in `shared/src/desktopTest`.
4. Put Android instrumentation rendering tests in `shared/src/androidDeviceTest`.
5. Inject an isolated `ArenaStore` when persistence is involved.
6. Assert a user-observable state change, not an internal implementation detail when possible.
7. Run `allTests`, `desktopTest`, and `assembleAndroidDeviceTest` before submitting the change.
