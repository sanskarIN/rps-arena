# Build System Reference

RPS Arena is a four-module Gradle build with an optional Rust crate and a native Xcode host beside it. The shared Kotlin Multiplatform module targets Android, JVM desktop, iOS/iPadOS, Kotlin/JS browser, and Kotlin/Wasm browser; thin platform applications host that shared product code.

## Build graph

```text
root project: rps-arena
├── :shared
│   ├── Android KMP target
│   ├── JVM target named desktop
│   ├── iOS arm64 target
│   ├── iOS simulator arm64 target
│   ├── Kotlin/JS browser target
│   └── Kotlin/Wasm browser target
├── :androidApp -> depends on :shared
├── :desktopApp -> depends on :shared
└── :webApp -> depends on :shared

iosApp/       native SwiftUI/Xcode host -> generated RpsArenaShared.framework
rust-engine/   separate Cargo package; not a Gradle module
```

The main game model, rules, CPU logic, repository, state, localization, Compose UI, logging, backup/import behavior, and private-room contracts live in `:shared`.

## `settings.gradle.kts`

This file defines repository sources and Gradle modules.

### Plugin repositories

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

### Dependency repositories

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

`FAIL_ON_PROJECT_REPOS` prevents subprojects from quietly adding unreviewed repositories.

### Modules

```kotlin
rootProject.name = "rps-arena"
include(":shared", ":androidApp", ":desktopApp", ":webApp")
```

`iosApp` is an Xcode project rather than a Gradle subproject. Its Kotlin framework comes from `:shared`.

`rust-engine` uses Cargo independently.

## Root `build.gradle.kts`

The root project declares plugin aliases with `apply false`. This centralizes plugin/version availability without applying Android/Kotlin/Compose behavior to the root project itself.

## Version catalog

File:

```text
gradle/libs.versions.toml
```

Current major project baseline entries include:

```text
Kotlin 2.4.10
Compose Multiplatform 1.11.0
Android Gradle Plugin 9.3.0
AndroidX Activity Compose 1.13.0
Kotlin Coroutines 1.10.2
kotlinx-browser 0.3
Android compile SDK 36
Android target SDK 36
Android min SDK 26
```

The catalog supplies aliases for Android, Kotlin Multiplatform/JVM, Compose, browser APIs, and related plugins/dependencies.

These are repository baselines, not claims that each number is globally the newest release.

## `gradle.properties`

Current properties control:

- Gradle max JVM heap;
- UTF-8 file encoding;
- build cache;
- configuration cache;
- parallel execution;
- official Kotlin code style;
- incremental Kotlin compilation.

Cross-platform target additions should be tested with configuration caching enabled because new plugins/tasks can expose incompatibilities.

## `:shared` module

File:

```text
shared/build.gradle.kts
```

Plugins:

- Kotlin Multiplatform;
- Android Kotlin Multiplatform library;
- Compose Multiplatform;
- Kotlin Compose compiler.

### Android target

The shared Android target uses:

```text
namespace: in.sanskar.rpsarena.shared
compile SDK: 36
minimum SDK: 26
JVM target: 17
Android resources: enabled
```

Its platform persistence implementation lives under `androidMain`.

### Desktop JVM target

Configured as:

```kotlin
jvm("desktop")
```

with JVM target 17.

This target supplies shared code to the Windows/Linux/macOS JVM desktop application.

### iOS/iPadOS targets

Configured:

```kotlin
listOf(
    iosArm64(),
    iosSimulatorArm64(),
).forEach { iosTarget ->
    iosTarget.binaries.framework {
        baseName = "RpsArenaShared"
        isStatic = true
    }
}
```

`iosArm64` produces a framework for physical modern iPhone/iPad arm64 devices.

`iosSimulatorArm64` produces a framework for Apple-silicon iOS simulators.

Both frameworks use the same base name:

```text
RpsArenaShared
```

The native SwiftUI host imports that framework.

### Kotlin/JS browser target

Configured:

```kotlin
js {
    browser()
}
```

In `:shared` this is a library target. The executable browser app lives in `:webApp`.

### Kotlin/Wasm browser target

Configured:

```kotlin
wasmJs {
    browser()
}
```

This is also a shared library target.

### Default hierarchy

`applyDefaultHierarchyTemplate()` is used after defining the targets.

This allows common intermediate source sets such as `webMain` to share browser-specific implementations between Kotlin/JS and Kotlin/Wasm while retaining `commonMain` for code shared by every platform.

## Shared source-set structure

### `commonMain`

Contains product code that must compile for all targets.

Dependencies include:

- Compose Runtime;
- Compose Foundation;
- Compose Animation;
- Material 3;
- Compose UI;
- Kotlin Coroutines Core.

Examples of common responsibilities:

- models;
- game rules;
- CPU strategy;
- state machine;
- repository/backup codecs;
- localization;
- shared UI;
- logging abstraction;
- private-room contracts.

Do not put Android `Context`, Foundation/UIKit classes, Java-only APIs, or browser DOM APIs directly in `commonMain`.

### `commonTest`

Uses `kotlin("test")` and contains the platform-independent regression suite.

### `androidMain`

Contains the Android `actual PlatformStore` backed by SharedPreferences.

### `desktopMain`

Contains the JVM desktop `actual PlatformStore` backed by Java Preferences.

### `iosMain`

Shared by configured iOS targets through the hierarchy.

Contains:

```text
PlatformStore.ios.kt
MainViewController.kt
```

The first adapts NSUserDefaults. The second exposes a `ComposeUIViewController` to Swift.

### `webMain`

Shared by JS and Wasm browser targets.

Contains browser-local persistence through `window.localStorage` and depends on `kotlinx-browser`.

### `desktopTest`

Adds Compose desktop UI-test dependencies and contains shared UI smoke tests.

## `expect` / `actual` storage architecture

Common declaration:

```text
shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.kt
```

Platform implementations:

```text
shared/src/androidMain/.../PlatformStore.android.kt
shared/src/desktopMain/.../PlatformStore.desktop.kt
shared/src/iosMain/.../PlatformStore.ios.kt
shared/src/webMain/.../PlatformStore.web.kt
```

This is the intended multiplatform boundary: only the physical string-storage mechanism varies. `ArenaRepository` remains the schema, migration, validation, and backup authority.

## `:androidApp`

File:

```text
androidApp/build.gradle.kts
```

The Android application uses:

- Android application plugin;
- Compose Multiplatform;
- Compose compiler;
- dependency on `:shared`;
- AndroidX Activity Compose.

Current identity:

```text
namespace/applicationId: in.sanskar.rpsarena
versionName: 2.5.8
versionCode: 20508
```

Android semantic version code convention:

```text
major * 10000 + minor * 100 + patch
```

`scripts/check_version.py` enforces this.

## `:desktopApp`

File:

```text
desktopApp/build.gradle.kts
```

The JVM desktop app depends on `:shared` and `compose.desktop.currentOs`.

Main class:

```text
in.sanskar.rpsarena.desktop.MainKt
```

Package version:

```text
2.5.8
```

Declared native package formats:

```text
DMG
MSI
DEB
```

The JVM source is cross-platform across Windows/Linux/macOS, while native installer generation/signing remains host-dependent.

## `:webApp`

File:

```text
webApp/build.gradle.kts
```

Plugins:

- Kotlin Multiplatform;
- Compose Multiplatform;
- Compose compiler.

### Executable JS target

```kotlin
js {
    browser()
    binaries.executable()
}
```

### Executable Wasm target

```kotlin
wasmJs {
    browser()
    binaries.executable()
}
```

The app applies the default hierarchy and places shared browser startup code under `webMain`.

`commonMain` depends on:

```text
:shared
Compose Runtime
Compose UI
```

The app entry point attaches `ComposeViewport` to the HTML host and renders `RpsArenaApp()`.

## Web compatibility distribution

Preferred production Web build:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

Expected generated output:

```text
webApp/build/dist/composeWebCompatibility/productionExecutable/
```

This combines Wasm and JS browser outputs through Compose's compatibility packaging path.

Development commands include:

```bash
gradle :webApp:wasmJsBrowserDevelopmentRun --stacktrace
gradle :webApp:jsBrowserDevelopmentRun --stacktrace
```

## Native iOS host

Directory:

```text
iosApp/
```

This is not included with `include(...)` because it is an Xcode application project.

The native host consists of:

```text
iosApp.xcodeproj
iosApp.swift
ContentView.swift
Info.plist
```

The SwiftUI layer imports `RpsArenaShared` and embeds `MainViewController()` exported from Kotlin.

## Direct Kotlin/Xcode integration

The Xcode project has a shell build phase that runs:

```bash
gradle :shared:embedAndSignAppleFrameworkForXcode
```

from repository root.

Because this repository currently does not track a Gradle Wrapper, the build phase intentionally invokes the installed `gradle` executable. If the repository later adopts the official wrapper, update this build phase and every documented command consistently.

## iOS framework tasks

Simulator Debug:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

Simulator Release:

```bash
gradle :shared:linkReleaseFrameworkIosSimulatorArm64 --stacktrace
```

Device Release:

```bash
gradle :shared:linkReleaseFrameworkIosArm64 --stacktrace
```

Generated frameworks live under module build output and are not tracked.

## iOS version metadata

`Info.plist`:

```text
CFBundleShortVersionString = 2.5.8
CFBundleVersion = 20508
```

Xcode target:

```text
MARKETING_VERSION = 2.5.8
CURRENT_PROJECT_VERSION = 20508
```

The same numeric build-code convention used by Android is enforced for iOS by `scripts/check_version.py`.

## Why platform apps remain thin

Intended dependency direction:

```text
Android Activity ------┐
iOS SwiftUI host ------┼-> shared Compose UI/state -> shared engine/data
Desktop Window --------┤
Web ComposeViewport ---┘
```

Avoid duplicating:

- `RulesEngine`;
- CPU algorithms;
- persistence codecs;
- backup schema;
- match state machine;
- localization catalogs;
- achievement logic

inside platform hosts.

## Build task dependency behavior

Android:

```bash
gradle :androidApp:assembleDebug
```

automatically builds the required shared Android variant.

Desktop:

```bash
gradle :desktopApp:classes
```

builds the shared desktop JVM dependency.

Web:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution
```

builds the required shared JS/Wasm outputs.

iOS:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64
```

builds the shared native framework. Xcode then embeds/links the framework through the configured direct integration.

## Generated output

Typical ignored generated paths include:

```text
androidApp/build/
desktopApp/build/
shared/build/
webApp/build/
.gradle/
.kotlin/
node_modules/
kotlin-js-store/
DerivedData/
rust-engine/target/
```

Do not treat generated installers/frameworks/npm-style package caches as source files.

## `local.properties`

Android tools commonly use `local.properties` for a machine-specific SDK path. It remains ignored because committing one developer's SDK path would reduce portability.

## No Gradle Wrapper currently tracked

The repository currently relies on:

- an installed Gradle for local development/Xcode direct integration;
- `gradle/actions/setup-gradle` in GitHub Actions.

All repository commands therefore use:

```text
gradle
```

rather than:

```text
./gradlew
```

A future wrapper change should be generated through trusted Gradle tooling, integrity-reviewed, and applied consistently across local docs, Xcode scripts, and CI.

## Cross-platform CI mapping

Ubuntu job validates:

```text
source/security/privacy/version gates
shared tests
Android lint/build
desktop JVM compilation
Web compatibility distribution
```

macOS job validates:

```text
iOS simulator framework
iOS SwiftUI/Xcode simulator host
```

Rust job validates:

```text
optional Rust crate tests
```

## Configuration changes requiring broad validation

Run the full relevant gate after changing:

- `settings.gradle.kts`;
- root `build.gradle.kts`;
- `gradle.properties`;
- version catalog;
- any module `build.gradle.kts`;
- KMP target declarations/hierarchy;
- Android SDK levels;
- Kotlin/Compose/AGP versions;
- JVM target/toolchain;
- browser target configuration;
- iOS framework configuration;
- Xcode framework integration;
- source-set dependencies;
- native packaging formats.

Portable gate:

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

On macOS also validate iOS framework/Xcode host builds.

## Build-system ownership principles

Use one obvious source of truth where practical:

- dependency/plugin/SDK versions -> version catalog;
- semantic release version -> synchronized Android/Desktop/iOS/shared declarations checked by script;
- mobile numeric build code -> deterministic semantic mapping checked by script;
- Gradle module list -> `settings.gradle.kts`;
- generic Gradle runtime behavior -> `gradle.properties`;
- Android packaging -> Android module;
- desktop packaging -> desktop module;
- browser executables/distribution -> Web module;
- iOS framework -> shared KMP module;
- iOS native shell/signing settings -> Xcode project;
- optional Rust metadata -> `rust-engine/Cargo.toml`.

Clear ownership prevents platform-specific drift while keeping the shared product behavior genuinely multiplatform.
