# Desktop Platform Reference

RPS Arena's desktop application is a thin JVM/Compose launcher around the shared application. This guide documents every tracked `desktopApp` file plus the desktop-specific shared storage adapter, native packaging configuration, runtime behavior, and platform-specific limitations.

## Desktop file inventory

Tracked desktop application files:

```text
desktopApp/build.gradle.kts
desktopApp/src/main/kotlin/in/sanskar/rpsarena/desktop/Main.kt
```

Related shared desktop adapter:

```text
shared/src/desktopMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.desktop.kt
```

Desktop UI smoke tests live in:

```text
shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt
```

## `desktopApp/build.gradle.kts`

### Plugins

```kotlin
alias(libs.plugins.kotlinJvm)
alias(libs.plugins.composeMultiplatform)
alias(libs.plugins.composeCompiler)
```

- Kotlin JVM compiles the launcher for the JVM.
- Compose Multiplatform provides desktop Compose runtime/windowing/native packaging support.
- Compose compiler handles `@Composable` Kotlin transformations.

The desktop module does not use Kotlin Multiplatform directly because the multiplatform code is already provided by `:shared`.

## Java toolchain

```kotlin
kotlin {
    jvmToolchain(17)
}
```

The desktop launcher is compiled with a Java 17 toolchain.

This aligns with the shared desktop JVM target (`JvmTarget.JVM_17`).

A machine can have other Java versions installed, but the project baseline remains JDK 17.

## Dependencies

```kotlin
implementation(project(":shared"))
implementation(compose.desktop.currentOs)
```

### `project(":shared")`

Provides:

- model/rules/CPU;
- state;
- persistence repository;
- desktop `PlatformStore` actual through the shared desktop target;
- shared Compose UI;
- localization/theme/design metadata.

### `compose.desktop.currentOs`

Chooses the Compose Desktop runtime artifact appropriate to the current host operating system/architecture.

This is why the same Gradle source can be used on Windows/Linux/macOS while native package creation remains host-dependent.

## Main class

Configured:

```kotlin
mainClass = "in.sanskar.rpsarena.desktop.MainKt"
```

Kotlin compiles a top-level function from `Main.kt` into a synthetic JVM class named `MainKt`.

If the entry source/package/function changes, update this `mainClass` value or desktop run/package tasks will fail.

## Native distribution metadata

Package name:

```text
RPS Arena
```

Package version:

```text
1.1.0
```

Description:

```text
Offline-first Rock Paper Scissors arena
```

Vendor:

```text
Sanskar
```

`packageVersion` is checked by `scripts/check_version.py` against Android/shared metadata.

## Declared native formats

```kotlin
targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
```

Meaning:

- `Dmg` — macOS distribution image;
- `Msi` — Windows Installer package;
- `Deb` — Debian Linux package.

Declaring all three does not mean every format can be built on every OS.

Native desktop packaging uses host-specific tooling. Release CI currently builds only the Debian package on Ubuntu.

## `Main.kt`

Current launcher:

```kotlin
fun main() = application {
    PlatformStore.initialize()
    Window(onCloseRequest = ::exitApplication, title = "RPS Arena") {
        RpsArenaApp()
    }
}
```

Startup sequence:

1. enter Compose Desktop `application` scope;
2. initialize desktop platform storage;
3. create a single top-level `Window`;
4. set close request to exit the application;
5. set window title to `RPS Arena`;
6. render shared `RpsArenaApp()`.

The desktop launcher deliberately contains no game rules or duplicate screen hierarchy.

## Desktop `PlatformStore`

File:

```text
shared/src/desktopMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.desktop.kt
```

Uses:

```java
java.util.prefs.Preferences
```

Node:

```text
in/sanskar/rpsarena
```

### Initialization

```kotlin
actual fun initialize(platformContext: Any?) = Unit
```

No platform context is required.

### Lazy preferences node

```kotlin
private val preferences: Preferences by lazy {
    Preferences.userRoot().node("in/sanskar/rpsarena")
}
```

The storage node is opened only when first used.

### Reads/writes

```kotlin
preferences.get(key, defaultValue)
preferences.put(key, value)
```

The product-level format/validation remains in common `ArenaRepository`; the desktop adapter does not understand settings/stats/history schemas.

## Physical desktop persistence location

`java.util.prefs.Preferences` chooses its physical backend according to the JDK and operating system.

Do not hard-code documentation claiming all platforms store data in one universal folder.

When debugging storage, identify OS/JDK behavior rather than deleting random application folders.

The app-level logical node/key contracts are documented in `docs/storage-and-backup.md`.

## Running desktop from source

```bash
gradle :desktopApp:run
```

This launches the window and keeps the Gradle process active until the app closes.

## Compile-only verification

```bash
gradle :desktopApp:classes --stacktrace
```

CI uses this as a portable source/build compatibility gate.

It does not prove native installers can be built/signed on every platform.

## Desktop UI tests

```bash
gradle :shared:desktopTest --stacktrace
```

Tests render the shared UI using Compose's desktop UI test runtime.

Current journeys include:

- onboarding;
- Home -> Play;
- classic gesture controls;
- English -> Hindi switch;
- Hindi gameplay copy;
- Hindi achievement copy;
- backup/import controls;
- reset confirmation.

These tests validate shared UI behavior in a desktop test environment. They are not a replacement for Android TalkBack/device tests.

## Package current host OS

```bash
gradle :desktopApp:packageDistributionForCurrentOS --stacktrace
```

Compose Desktop chooses a supported native format for the host.

Use this for manual packaging validation on Windows/macOS/Linux as appropriate.

## Linux `.deb`

Release CI runs:

```bash
gradle :desktopApp:packageDeb --stacktrace
```

Expected artifact path in release workflow:

```text
desktopApp/build/compose/binaries/main/deb/*.deb
```

The workflow fails artifact upload if no `.deb` is found, preventing a silently empty release job.

## Windows MSI

`TargetFormat.Msi` is configured, but the current public release workflow does not run a Windows job.

To add one safely:

- use `windows-latest` or an approved Windows runner;
- compile/test the same commit;
- run Compose MSI packaging task appropriate to current Compose version;
- upload the MSI;
- decide signing policy;
- keep certificate/private key outside Git;
- add checksum/release notes;
- document SmartScreen/signing expectations accurately.

Do not copy Linux package commands into Windows jobs.

## macOS DMG

`TargetFormat.Dmg` is configured, but the current public release workflow does not create a macOS artifact.

A production macOS distribution can involve:

- macOS runner;
- DMG/application bundle packaging;
- Apple code-signing identity;
- notarization credentials/workflow;
- hardened runtime/entitlements as required by distribution approach.

Do not commit Apple private keys/certificates/passwords.

An unsigned DMG is not equivalent to a notarized production distribution.

## Native package version rules

Compose Desktop package tools can impose platform-specific version syntax/range rules.

Keep semantic versions simple (`major.minor.patch`) and validate packaging tasks before tagging.

`scripts/check_version.py` ensures desktop `packageVersion` matches Android/shared semantic version, but it cannot validate store/distribution policy for every OS.

## Window behavior

Current window configuration sets only:

- title;
- close request.

There is no explicit persisted window size/position, tray integration, menu bar, multiple windows, or custom icon in `Main.kt`.

Do not document those features as implemented.

Compose/the OS determines the default initial window dimensions unless later configured.

## Desktop responsiveness

Shared app content is capped with `ArenaLayoutTokens.ContentMaxWidth` while filling narrower windows.

Configuration chips use wrapping layout, helping when desktop windows are resized smaller.

Manual checks should resize the window significantly and verify:

- no essential option is clipped;
- history/settings remain scrollable;
- gesture controls remain usable;
- long Hindi copy remains readable;
- minimum window constraints, if later added, do not hide content.

## Keyboard accessibility

Compose desktop controls participate in focus/keyboard behavior by default, but release validation should manually traverse:

- onboarding;
- Home navigation;
- Play configuration chips;
- gesture buttons;
- Settings switches/text field/backup/reset;
- History/Achievements.

Check focus visibility and activation, not only whether tabbing technically moves.

## Desktop offline behavior

The desktop launcher contains no network initialization.

Current private-room reference transport is in-memory shared code only.

Normal CPU/local gameplay and persistence work without a network connection.

A future desktop LAN adapter should remain opt-in and close its sockets/listeners when leaving the room.

## Desktop logs/errors

For source build failures:

```bash
gradle :desktopApp:classes --stacktrace
```

For runtime failures:

```bash
gradle :desktopApp:run --stacktrace
```

Avoid sharing logs without checking for local usernames/paths or other environment data.

## Build output

Generated desktop output lives under:

```text
desktopApp/build/
```

Native installer extensions are ignored by `.gitignore`:

```text
*.dmg
*.msi
*.deb
*.rpm
```

Do not commit generated installers into source history when GitHub Releases/artifacts are the intended distribution channel.

## Adding a desktop-specific capability

If a capability is inherently desktop/JVM-specific, place it in an appropriate desktop source set/module and expose a narrow shared abstraction when shared UI/state needs it.

Examples:

- desktop file picker;
- clipboard integration beyond shared APIs;
- OS notifications;
- tray icon;
- desktop LAN socket adapter;
- platform share/open-file behavior.

Do not move common game logic out of `:shared` merely because the feature is first developed on desktop.

## Desktop change checklist

After changing desktop launcher/build/storage/shared UI:

1. run shared tests;
2. run `:shared:desktopTest` when UI changes;
3. run `:desktopApp:classes`;
4. run `:desktopApp:run` manually for interactive changes;
5. run current-OS native packaging for packaging changes;
6. verify version consistency after version edits;
7. test local data persistence/restart when storage changes;
8. verify keyboard/responsive behavior for UI changes;
9. update release/security docs if signing/networking changes.
