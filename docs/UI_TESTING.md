# UI testing

RPS Arena keeps its primary Compose UI automation in `shared/src/commonTest` so the same user-flow tests can target desktop JVM and Android device tests.

## What the suite covers

`RpsArenaUiTest` currently verifies these high-value flows:

- onboarding completion persists and opens the home screen;
- home → play → home navigation works;
- choosing a gesture produces a visible round result;
- the reduced-motion setting persists through the repository;
- backup export opens the expected dialog.

Tests identify controls through `ArenaUiTags`, not translated labels. This keeps automation stable when the runtime language changes.

## Deterministic persistence

Production uses `PlatformArenaStore`, which delegates to the existing Android `SharedPreferences` or desktop `Preferences` implementation. UI tests inject an in-memory `ArenaStore` into `ArenaRepository` so test data is isolated, repeatable, and never writes to a developer's real RPS Arena preferences.

## Desktop

Run the shared UI suite on the desktop JVM target:

```bash
gradle :shared:desktopTest
```

The desktop test source set includes the current Compose desktop runtime while the test source itself remains in `commonTest`.

## Android

The Android KMP target opts into a device-test compilation with `sourceSetTreeName = "test"`, so common UI tests are included in `androidDeviceTest`.

Compile the Android instrumentation test APK without an emulator:

```bash
gradle :shared:assembleAndroidDeviceTest
```

Run the tests on a connected device or emulator:

```bash
gradle :shared:connectedAndroidDeviceTest
```

The Android device-test manifest uses `androidx.activity.ComponentActivity` as the test host and the suite runs with `androidx.test.runner.AndroidJUnitRunner`.

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
2. shared tests;
3. desktop UI tests;
4. Android device-test APK assembly;
5. Android debug assembly;
6. desktop JVM compilation;
7. Rust tests in the separate Rust job.

CI intentionally compiles the Android device-test APK without launching an emulator. Actual Android instrumentation execution remains available through `connectedAndroidDeviceTest` on a developer machine or an Android-capable test runner. This keeps normal pull-request CI deterministic and avoids hiding device-test compilation regressions.

## Adding a UI test

1. Prefer a stable constant in `ArenaUiTags` instead of matching visible text.
2. Put platform-neutral flows in `shared/src/commonTest`.
3. Inject an isolated `ArenaStore` when persistence is involved.
4. Use the Compose Multiplatform v2 `runComposeUiTest` API.
5. Assert a user-observable state change, not an internal implementation detail when possible.
6. Run `desktopTest` and `assembleAndroidDeviceTest` before submitting the change.
