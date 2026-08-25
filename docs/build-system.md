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

The game model, rules, CPU logic, repository, state, localization, Compose UI, logging, backup/import behavior, and private-room contracts live in `:shared`.

## `settings.gradle.kts`

This file defines plugin sources, dependency sources, Kotlin web-tool distribution sources, and Gradle modules.

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

### Centralized dependency repositories

The build uses `RepositoriesMode.PREFER_SETTINGS` rather than `FAIL_ON_PROJECT_REPOS` because Kotlin/JS and Kotlin/Wasm setup tasks still attempt to register Ivy repositories for Node.js, Yarn, and Binaryen. Strict failure mode rejects those toolchain repositories before the Web build can start.

RPS Arena keeps repository control centralized by declaring the required tool-distribution repositories explicitly in settings and restricting each one to its expected dependency group:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node.js distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("org.nodejs") }
        }

        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact](-v[revision]).[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }

        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen distributions"
                    patternLayout {
                        artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }
    }
}
```

`PREFER_SETTINGS` means repositories added by subprojects/plugins do not replace the reviewed settings-level source list. The three `exclusiveContent` blocks prevent those tool-distribution hosts from becoming general-purpose Maven repositories.

Do not add arbitrary repositories to work around dependency-resolution failures. Add a source only when its ownership, artifact group, URL pattern, and necessity are understood and documented.

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

Current validated baseline entries include:

```text
Kotlin 2.4.10
Compose Multiplatform 1.11.0
Compose Material 3 1.11.0-alpha07
Android Gradle Plugin 9.3.0
AndroidX Activity Compose 1.13.0
Kotlin Coroutines 1.10.2
kotlinx-browser 0.3
Android compile SDK 36
Android target SDK 36
Android min SDK 26
```

Compose Runtime, Foundation, Animation, UI, UI Test, and Material 3 use explicit Maven coordinates in the version catalog. This avoids Compose 1.11 Gradle-plugin dependency aliases that are deprecated at error level.

The desktop runtime is the exception on this pinned Compose 1.11 baseline: `compose.desktop.currentOs` is used by the desktop application and desktop test source set because the later all-platform desktop aggregate artifact is not published for Compose 1.11.0. Revisit that dependency only as part of a tested Compose upgrade.

These are repository compatibility baselines, not claims that every number is globally newest.

## `gradle.properties`

Current properties control Gradle heap, UTF-8 file encoding, build/configuration caches, parallel execution, Kotlin code style, and incremental compilation.

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
host-side tests: enabled
```

Host tests are explicitly opted in with:

```kotlin
withHostTest {}
```

The Android-KMP plugin disables host/device test components by default, so this opt-in ensures common regression tests are also compiled/executed through the Android host target.

Its platform persistence implementation lives under `androidMain`.

### Desktop JVM target

Configured as:

```kotlin
jvm("desktop")
```

with JVM target 17. It supplies shared code to the Windows/Linux/macOS JVM desktop application.

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

`iosArm64` targets modern physical iPhone/iPad arm64 devices. `iosSimulatorArm64` targets Apple-silicon iOS simulators. Both frameworks use the base name `RpsArenaShared` and are consumed by the native SwiftUI host.

### Kotlin/JS browser target

```kotlin
js {
    browser()
}
```

In `:shared` this is a library target. The executable browser app lives in `:webApp`.

### Kotlin/Wasm browser target

```kotlin
wasmJs {
    browser()
}
```

This is also a shared library target.

### Default hierarchy

`applyDefaultHierarchyTemplate()` is used after defining the targets. It enables common intermediate source sets such as `webMain` to share browser-specific implementations between Kotlin/JS and Kotlin/Wasm while retaining `commonMain` for code shared by every platform.

## Shared source-set structure

### `commonMain`

Contains platform-independent product code. Dependencies include Compose Runtime, Foundation, Animation, Material 3, UI, and Kotlin Coroutines Core.

Do not put Android `Context`, Foundation/UIKit classes, Java-only APIs, or browser DOM APIs directly in `commonMain`.

The private-room `RoomCode` type is intentionally a normal immutable data class rather than a JVM-only inline wrapper, so the same validated code type compiles on JVM, Native, JS, and Wasm.

### `commonTest`

Uses `kotlin("test")` and contains the platform-independent regression suite.

### `androidMain`

Contains the Android `actual PlatformStore` backed by SharedPreferences.

### `desktopMain`

Contains the JVM desktop `actual PlatformStore` backed by Java Preferences.

### `iosMain`

Contains `PlatformStore.ios.kt` and `MainViewController.kt`; it is shared by the configured iOS targets through the default hierarchy.

### `webMain`

Shared by JS and Wasm browser targets. It contains browser-local persistence through `window.localStorage` and depends on `kotlinx-browser`.

### `desktopTest`

Adds Compose desktop UI-test dependencies and contains UI smoke tests. Tests use the Compose Multiplatform v2 runner import:

```kotlin
androidx.compose.ui.test.v2.runComposeUiTest
```

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

Only the physical string-storage mechanism varies. `ArenaRepository` remains the schema, migration, validation, and backup authority.

## `:androidApp`

File: `androidApp/build.gradle.kts`.

The Android application uses the Android application plugin, Compose Multiplatform/compiler, `:shared`, and AndroidX Activity Compose.

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

File: `desktopApp/build.gradle.kts`.

The JVM desktop app depends on `:shared` and `compose.desktop.currentOs` for the pinned Compose 1.11 runtime.

```text
main class: in.sanskar.rpsarena.desktop.MainKt
package version: 2.5.8
native formats: DMG, MSI, DEB
```

The JVM source is cross-platform across Windows/Linux/macOS, while native installer generation/signing remains host-dependent.

## `:webApp`

File: `webApp/build.gradle.kts`.

It declares executable Kotlin/JS and Kotlin/Wasm browser targets and depends on `:shared`, Compose Runtime, and Compose UI.

The app entry point attaches `ComposeViewport` to the HTML host and renders `RpsArenaApp()`.

Preferred production build:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

Expected output:

```text
webApp/build/dist/composeWebCompatibility/productionExecutable/
```

Development commands:

```bash
gradle :webApp:wasmJsBrowserDevelopmentRun --stacktrace
gradle :webApp:jsBrowserDevelopmentRun --stacktrace
```

## Native iOS host

Directory: `iosApp/`.

The native SwiftUI layer imports `RpsArenaShared` and embeds `MainViewController()` exported from Kotlin. The Xcode project has a shell build phase that runs:

```bash
gradle :shared:embedAndSignAppleFrameworkForXcode
```

Because the repository currently does not track a Gradle Wrapper, the build phase invokes the installed `gradle` executable.

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

Version metadata:

```text
CFBundleShortVersionString = 2.5.8
CFBundleVersion = 20508
MARKETING_VERSION = 2.5.8
CURRENT_PROJECT_VERSION = 20508
```

## Why platform apps remain thin

Intended dependency direction:

```text
Android Activity ------┐
iOS SwiftUI host ------┼-> shared Compose UI/state -> shared engine/data
Desktop Window --------┤
Web ComposeViewport ---┘
```

Avoid duplicating rules, CPU algorithms, persistence codecs, backup schema, match state, localization, or achievement logic inside platform hosts.

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

`local.properties` remains ignored because Android SDK paths are machine-specific.

## No Gradle Wrapper currently tracked

Local development/Xcode direct integration uses an installed Gradle executable; GitHub Actions provisions Gradle 9.5.1 through `gradle/actions/setup-gradle`.

Commands therefore use:

```text
gradle
```

rather than `./gradlew`. A future wrapper adoption must update local docs, Xcode scripts, and CI consistently and review wrapper integrity before commit.

## Cross-platform CI mapping

Ubuntu Kotlin job validates:

```text
source/security/privacy/version gates
shared tests including Android host tests
Android lint/build
desktop JVM compilation
Web compatibility distribution
```

macOS job validates:

```text
iOS simulator framework
iOS SwiftUI/Xcode simulator host
```

Rust job validates the optional crate tests. Focused Security and CodeQL workflows run independently.

## Configuration changes requiring broad validation

Run the full relevant gate after changing settings, root/version-catalog/module Gradle files, target declarations/hierarchy, SDK levels, Kotlin/Compose/AGP versions, JVM targets, browser configuration, iOS framework integration, source-set dependencies, or native packaging formats.

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

On macOS also validate the iOS framework and Xcode simulator host.

## Build-system ownership principles

Use one obvious source of truth where practical:

- dependency/plugin/SDK versions -> version catalog;
- semantic release version -> synchronized Android/Desktop/iOS/shared declarations checked by script;
- mobile numeric build code -> deterministic semantic mapping checked by script;
- Gradle module/repository policy -> `settings.gradle.kts`;
- generic Gradle runtime behavior -> `gradle.properties`;
- Android packaging -> Android module;
- desktop packaging -> desktop module;
- browser executables/distribution -> Web module;
- iOS framework -> shared KMP module;
- iOS shell/signing settings -> Xcode project;
- optional Rust metadata -> `rust-engine/Cargo.toml`.

Clear ownership prevents platform-specific drift while keeping shared product behavior genuinely multiplatform.
