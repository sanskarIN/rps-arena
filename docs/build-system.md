# Build System Reference

RPS Arena is a three-module Gradle build with an optional Rust crate beside it. This guide explains every Gradle configuration file, how modules depend on one another, how Kotlin Multiplatform source sets are organized, and why common tasks behave the way they do.

## Build graph

```text
root project: rps-arena
├── :shared
│   ├── Android Kotlin Multiplatform target
│   └── JVM target named desktop
├── :androidApp -> depends on :shared
└── :desktopApp -> depends on :shared

rust-engine/   separate Cargo package; not a Gradle module
```

The Android and desktop applications are intentionally thin. The main game model, rules, CPU logic, persistence repository, state, localization, UI, and testable private-room contracts live in `:shared`.

## `settings.gradle.kts`

This file creates the Gradle build and defines where plugins/dependencies may come from.

### `pluginManagement`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

Purpose:

- `google()` provides Android/Google Gradle plugins and artifacts.
- `mavenCentral()` is a primary Maven artifact repository.
- `gradlePluginPortal()` provides Gradle plugin metadata/artifacts.

This block affects plugin resolution, not ordinary module dependencies alone.

### Central dependency repositories

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

`FAIL_ON_PROJECT_REPOS` means module build files should not quietly add their own repositories. If a module tries to declare a repository, Gradle fails. This makes dependency provenance easier to review because repositories are centralized.

### Root name and modules

```kotlin
rootProject.name = "rps-arena"
include(":shared", ":androidApp", ":desktopApp")
```

- `rootProject.name` is the Gradle build's root name.
- `include` registers the three Gradle subprojects.
- `rust-engine` is not included because it uses Cargo, not Gradle.

## Root `build.gradle.kts`

The root file declares plugin versions through version-catalog aliases but uses `apply false`:

```kotlin
alias(libs.plugins.kotlinMultiplatform) apply false
```

Meaning:

- Gradle knows which plugin/version is available to child modules.
- The plugin is **not** applied to the root project itself.
- Each module applies only the plugins it needs.

This keeps the root project lightweight and prevents Android/Kotlin/Compose behavior from being unnecessarily applied everywhere.

## `gradle/libs.versions.toml`

This is the version catalog and primary source of truth for Gradle-managed dependency/plugin versions.

### `[versions]`

Named reusable version values:

```toml
kotlin = "2.4.10"
compose = "1.11.0"
agp = "9.3.0"
activityCompose = "1.13.0"
coroutines = "1.10.2"
androidCompileSdk = "36"
androidTargetSdk = "36"
androidMinSdk = "26"
```

Using names prevents the same version from being copied into several build files.

### `[libraries]`

Maps logical catalog names to Maven coordinates, for example:

```toml
androidx-activity-compose = {
    module = "androidx.activity:activity-compose",
    version.ref = "activityCompose"
}
```

In Kotlin DSL this becomes:

```kotlin
libs.androidx.activity.compose
```

### `[plugins]`

Maps aliases to Gradle plugin IDs and version references. For example:

```toml
kotlinMultiplatform = {
    id = "org.jetbrains.kotlin.multiplatform",
    version.ref = "kotlin"
}
```

The alias is consumed through:

```kotlin
alias(libs.plugins.kotlinMultiplatform)
```

## `gradle.properties`

Current properties:

```properties
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true
kotlin.code.style=official
kotlin.incremental=true
```

### `org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8`

- `-Xmx3g` gives the Gradle JVM a maximum heap of roughly 3 GiB.
- `-Dfile.encoding=UTF-8` makes JVM default file encoding explicit for build consistency.

This is a maximum, not a statement that Gradle always consumes 3 GiB.

### `org.gradle.caching=true`

Enables Gradle's build cache so compatible task outputs can be reused rather than recomputed.

### `org.gradle.configuration-cache=true`

Allows Gradle to reuse the configured task graph when the build and plugins are compatible with configuration caching.

If a future plugin is incompatible, fix/upgrade the plugin or deliberately reassess this setting rather than silently ignoring repeated warnings.

### `org.gradle.parallel=true`

Allows independent projects/tasks to execute concurrently when Gradle determines it is safe.

### `kotlin.code.style=official`

Requests Kotlin's official code-style defaults in supported tooling.

### `kotlin.incremental=true`

Allows Kotlin compilation to rebuild only affected source portions where possible.

## `:shared` module

File: `shared/build.gradle.kts`.

Applied plugins:

- Kotlin Multiplatform;
- Android Kotlin Multiplatform library;
- Compose Multiplatform;
- Kotlin Compose compiler plugin.

### Android target

```kotlin
android {
    namespace = "in.sanskar.rpsarena.shared"
    compileSdk = ...
    minSdk = ...
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    androidResources {
        enable = true
    }
}
```

Important details:

- This is the **shared library's Android target**, not the final Android application.
- The namespace is different from the app module's namespace so generated Android symbols remain unambiguous.
- JVM bytecode target is 17.
- Android resource support is enabled for the shared Android KMP library even though current shared UI primarily uses Compose APIs.

### Desktop target

```kotlin
jvm("desktop") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
```

`jvm("desktop")` creates a JVM target whose Kotlin source-set names include `desktopMain`/`desktopTest`.

### `commonMain`

Dependencies:

- Compose Runtime — state/composition runtime;
- Compose Foundation — foundational UI/layout behaviors;
- Compose Animation — result transition support;
- Material 3 — buttons/cards/scaffold/theme components;
- Compose UI — core UI types;
- Kotlin Coroutines Core — timer delay/coroutine support.

`commonMain` code must compile for every configured shared target. Avoid putting Android `Context`, Java-only APIs, or desktop-only APIs directly here unless hidden behind multiplatform abstractions.

### `androidMain`

Contains Android-specific `actual` implementation of `PlatformStore` using `SharedPreferences`.

### `desktopMain`

Contains desktop-specific `actual` implementation of `PlatformStore` using `java.util.prefs.Preferences`.

### `commonTest`

Uses `kotlin("test")` and contains platform-independent rules/state/repository/protocol/localization tests.

### `desktopTest`

Adds:

- `compose.uiTest`;
- `compose.desktop.currentOs`.

This enables real Compose desktop UI smoke tests around the shared `RpsArenaApp`.

## `:androidApp` module

File: `androidApp/build.gradle.kts`.

Plugins:

- Android application;
- Compose Multiplatform;
- Compose compiler.

### Identity

```kotlin
namespace = "in.sanskar.rpsarena"
applicationId = "in.sanskar.rpsarena"
```

`namespace` controls generated Android/Kotlin resource namespace behavior. `applicationId` identifies the installed Android application package.

### Android SDK levels

The module reads compile/min/target SDK values from the version catalog.

- `compileSdk`: API definitions available at compile time.
- `targetSdk`: platform behavior level the app declares it has been designed/tested for.
- `minSdk`: oldest Android API allowed to install/run the app.

These values are different concepts; do not change one merely because another changes.

### Android versioning

```kotlin
versionCode = 20508
versionName = "2.5.8"
```

- `versionCode` is the monotonically increasing Android integer used for upgrade ordering.
- `versionName` is the user-visible semantic version string.
- The repository maps `major.minor.patch` to `major * 10000 + minor * 100 + patch`, requiring minor/patch values no greater than 99.

The project's version-consistency script checks Android `versionName` against desktop/shared declarations and verifies that Android `versionCode` matches that deterministic semantic mapping.

### Build features

```kotlin
buildFeatures {
    compose = true
    buildConfig = true
}
```

- `compose = true` enables Android Compose build integration.
- `buildConfig = true` requests a generated `BuildConfig` class even though current shared metadata is kept independently.

### Packaging exclusion

```kotlin
resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
```

Prevents duplicate dependency metadata resources with these paths from causing packaging conflicts. It does **not** remove the project's MIT `LICENSE` file from the repository.

### Dependencies

```kotlin
implementation(project(":shared"))
implementation(libs.androidx.activity.compose)
```

The app delegates product behavior/UI to `:shared`; AndroidX Activity Compose provides the Compose-aware Activity integration used by `MainActivity`.

## `:desktopApp` module

File: `desktopApp/build.gradle.kts`.

Plugins:

- Kotlin JVM;
- Compose Multiplatform;
- Compose compiler.

### JVM toolchain

```kotlin
kotlin {
    jvmToolchain(17)
}
```

Requests a Java 17 compiler/toolchain for desktop Kotlin compilation.

### Dependencies

```kotlin
implementation(project(":shared"))
implementation(compose.desktop.currentOs)
```

The current operating-system Compose Desktop artifact supplies the platform runtime needed by the desktop launcher.

### Main class

```kotlin
mainClass = "in.sanskar.rpsarena.desktop.MainKt"
```

Top-level Kotlin function `main()` in `Main.kt` compiles to a JVM class named `MainKt`.

### Native package formats

Configured formats:

- `Dmg` — macOS disk image package;
- `Msi` — Windows Installer package;
- `Deb` — Debian-family Linux package.

Native packaging is host-dependent. An Ubuntu runner can build the Debian package; it cannot create a properly signed/notarized macOS package just because `TargetFormat.Dmg` appears in the build script.

## Why `:androidApp` and `:desktopApp` do not duplicate game logic

Both application modules depend on `:shared`. The intended direction is:

```text
platform launcher -> shared application UI/state -> shared engine/data/model
```

Avoid adding a second `RulesEngine`, CPU strategy, settings codec, or match state machine inside a platform app. Duplicated behavior would drift and undermine multiplatform tests.

## Source-set placement rules

Put code in `commonMain` when it uses APIs available to all shared targets.

Put code in `androidMain` when it requires Android-specific classes such as `Context` or `SharedPreferences`.

Put code in `desktopMain` when it requires desktop/JVM-specific APIs such as `java.util.prefs.Preferences` and there is no common abstraction.

Use an `expect` declaration in common code plus matching `actual` implementations when shared code needs a small platform-specific capability.

Current example:

```text
commonMain: PlatformStore (expect)
androidMain: PlatformStore.android.kt (actual)
desktopMain: PlatformStore.desktop.kt (actual)
```

## Common task dependency behavior

When you run:

```bash
gradle :androidApp:assembleDebug
```

Gradle sees `androidApp -> shared` and compiles the appropriate shared Android variant/target automatically.

When you run:

```bash
gradle :desktopApp:classes
```

Gradle compiles the desktop application's JVM classes and the shared desktop dependency as needed.

You normally do not need to manually compile `:shared` first.

## Build output directories

Generated output appears under module-local `build/` directories, for example:

```text
androidApp/build/
desktopApp/build/
shared/build/
```

These are ignored by Git and should never be treated as source files.

Rust uses:

```text
rust-engine/target/
```

which is also ignored.

## Why `local.properties` is not tracked

Android tools commonly use `local.properties` for a machine-specific SDK path. Since every developer/CI machine can use a different path, committing it would make the repository less portable. `.gitignore` excludes it.

## Why no Gradle Wrapper currently exists

The current repository relies on a locally installed Gradle and GitHub Actions' `gradle/actions/setup-gradle` action. This is why all documented commands use `gradle` instead of `./gradlew`.

A future pull request may add the official Gradle Wrapper for stronger local reproducibility. If that happens, it should commit the standard wrapper properties/scripts/JAR generated by Gradle, review wrapper integrity, update documentation/CI, and switch commands consistently. Do not handcraft or download an unverified wrapper JAR from an arbitrary source.

## Configuration changes that require broad validation

Run the full gate after changing:

- `settings.gradle.kts`;
- root `build.gradle.kts`;
- `gradle.properties`;
- `gradle/libs.versions.toml`;
- any module `build.gradle.kts`;
- Android SDK levels;
- Kotlin/Compose/AGP versions;
- JVM target/toolchain;
- source-set dependencies;
- native packaging formats.

Full gate:

```bash
python3 scripts/check_format.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

## Build-system ownership principle

A value should have one obvious source of truth whenever possible:

- dependency/plugin/SDK versions -> version catalog;
- application semantic version -> synchronized Android/Desktop/shared metadata declarations checked by script;
- repository lists/modules -> `settings.gradle.kts`;
- generic Gradle runtime behavior -> `gradle.properties`;
- platform packaging -> platform module build file;
- optional Rust crate metadata -> `rust-engine/Cargo.toml`.

Keeping those boundaries clear makes upgrades, reviews, and CI failures easier to reason about.
