# Toolchain Installation, Version Checks, and Upgrade Guide

RPS Arena uses a pinned/validated project toolchain instead of assuming that the newest version of every development tool will work together. The source of truth for project dependency versions is `gradle/libs.versions.toml`; GitHub Actions additionally pins the Gradle/JDK/Android SDK environment used for CI.

## Validated baseline

| Tool / platform | Project baseline | Where it is defined |
|---|---:|---|
| Java / JDK | 17 | CI, desktop/shared JVM target |
| Gradle | 9.5.1 | CI and release workflows |
| Kotlin | 2.4.10 | version catalog |
| Compose Multiplatform | 1.11.0 | version catalog |
| Android Gradle Plugin | 9.3.0 | version catalog |
| Android compile SDK | 36 | version catalog |
| Android target SDK | 36 | version catalog |
| Android minimum SDK | 26 | version catalog |
| Android Build Tools | 36.0.0 in CI | CI / CodeQL / release workflows |
| AndroidX Activity Compose | 1.13.0 | version catalog |
| Kotlin Coroutines | 1.10.2 | version catalog |
| Python | Python 3 compatible | repository verification scripts |
| Rust | stable, optional | CI uses stable toolchain |
| Git | modern Git with `switch` support recommended | local source-control workflow |

The table is a compatibility baseline for this repository. It is not a claim that each number is the newest version available globally.

## Why JDK 17 is required

The shared Android and desktop Kotlin targets compile to JVM target 17, and `desktopApp` explicitly requests a Java 17 toolchain. Using an older JDK can prevent Gradle or Kotlin compilation from running. A newer JDK may work for launching Gradle, but the repository's validated runtime/build target remains Java 17.

Check Java:

```bash
java -version
javac -version
```

- `java` runs the JVM launcher.
- `javac` is the Java compiler supplied by a full JDK.
- Both should resolve to the intended JDK installation.

### Installing JDK 17

Use a reputable OpenJDK distribution such as Eclipse Temurin, the JDK bundled/supported by Android Studio, or another standards-compatible JDK 17 distribution.

After installation, ensure `JAVA_HOME` points to the JDK directory and that its `bin` directory is available to the shell when necessary.

Windows PowerShell inspection:

```powershell
$env:JAVA_HOME
Get-Command java
java -version
```

macOS/Linux inspection:

```bash
echo "$JAVA_HOME"
command -v java
java -version
```

Do not point `JAVA_HOME` at the `bin` directory itself; point it at the JDK root.

## Gradle: local installation is currently required

This repository does not track a Gradle Wrapper. There is no `gradlew`, `gradlew.bat`, or `gradle/wrapper/gradle-wrapper.properties` in the current repository.

Therefore local commands use:

```bash
gradle ...
```

rather than:

```text
./gradlew ...
```

Check Gradle:

```bash
gradle --version
```

The project CI uses Gradle 9.5.1. For the closest local/CI parity, use the same version.

### If your Gradle is older

If an older Gradle fails before project configuration, install the validated Gradle version and make sure your shell resolves the intended executable.

Windows:

```powershell
Get-Command gradle
where.exe gradle
gradle --version
```

macOS/Linux:

```bash
command -v gradle
type -a gradle
gradle --version
```

Multiple paths in the output usually mean more than one installation is available. Fix `PATH` ordering rather than deleting unrelated software blindly.

### If your Gradle is newer

Do not assume a newer major/minor version is automatically safe. First run the repository verification suite. If the build is clean but CI intentionally remains pinned, keep the documented CI version until a dedicated toolchain-upgrade pull request updates and validates it.

## Android Studio and Android SDK

Android Studio is the recommended IDE for Android development. The Android application requires:

- Android platform SDK 36;
- Build Tools 36.0.0 in the validated CI environment;
- platform-tools for `adb` and device communication;
- a JDK 17-compatible Gradle configuration.

### Check installed SDK packages

Using the Android SDK command-line tools:

```bash
sdkmanager --list
```

Common required package identifiers include:

```text
platform-tools
platforms;android-36
build-tools;36.0.0
```

If `sdkmanager` is not on `PATH`, use Android Studio's SDK Manager instead of moving/deleting SDK directories manually.

### Install missing packages with `sdkmanager`

When command-line tools are configured:

```bash
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

Meaning:

- `platform-tools` provides tools such as `adb`.
- `platforms;android-36` provides Android API 36 compile definitions.
- `build-tools;36.0.0` provides packaging/build utilities used by the Android toolchain.

Accept Android SDK licenses through the official SDK tooling when required:

```bash
sdkmanager --licenses
```

Read the licenses before accepting them.

## Android device/emulator tools

Verify Android Debug Bridge:

```bash
adb version
adb devices
```

- `adb version` confirms which platform-tools installation is being used.
- `adb devices` lists authorized connected devices/emulators.

A device shown as `unauthorized` requires user approval on the physical device; do not attempt to bypass Android's authorization prompt.

## IDE options

### Android Studio

Best choice when working on:

- Android manifest/resources;
- Android SDK/emulators;
- Compose Android preview/debugging;
- Gradle Android variants;
- device logs.

### IntelliJ IDEA

Suitable for Kotlin Multiplatform/shared/desktop work when the installed Kotlin/Gradle support matches the project.

### VS Code

Useful for Markdown, Rust, Python scripts, Git, and lightweight source editing. Full Android/Compose project support is generally stronger in Android Studio/IntelliJ.

### Suggested extensions/plugins by task

Use only extensions you actually need. Examples:

- Kotlin support for Kotlin editing;
- Rust Analyzer for `rust-engine/`;
- Python support for `scripts/*.py`;
- Markdown preview/linting for `docs/`;
- EditorConfig support if your editor does not honor `.editorconfig` natively;
- Git/GitHub integration for repository operations.

Never install an extension solely because a random setup guide says it is required. Review publisher, permissions, update history, and necessity first.

## Python 3

Python is used only for repository validation scripts; it is not an application runtime dependency.

Check:

```bash
python3 --version
```

or on Windows:

```powershell
python --version
py --version
```

The scripts use standard-library functionality only, so no Python package installation is required for them.

## Git

Check:

```bash
git --version
```

Important project commands are explained in `docs/command-reference.md`.

Configure repository-local owner identity when appropriate:

```bash
git config user.name "Sanskar"
git config user.email "sanskarin@outlook.in"
```

## Optional Rust toolchain

The main Kotlin application does not require Rust. Install Rust only if you intend to work on `rust-engine/` or run its optional verification locally.

Check:

```bash
rustc --version
cargo --version
```

CI intentionally uses the stable Rust channel rather than a repository-pinned nightly compiler.

After installation/update:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

## Environment variables

### `JAVA_HOME`

Points to the selected JDK root.

### Android SDK environment

Depending on your Android tooling/setup, `ANDROID_HOME` can point to the Android SDK root. Android Studio can often manage the SDK path without requiring you to set it globally.

Do not commit a machine-specific Android SDK path. `local.properties` is ignored for this reason.

### `PATH`

Controls which `java`, `gradle`, `git`, `python`, `cargo`, `adb`, and related executable is found first.

When a command reports an unexpected version, inspect `PATH` resolution before reinstalling tools.

## What to do when a tool is out of support

Treat upgrades as compatibility changes, not routine text edits.

### 1. Identify the affected layer

Examples:

- JDK support change -> Gradle/Kotlin/desktop/Android compatibility;
- Gradle change -> AGP/Kotlin/Compose plugin compatibility;
- AGP change -> Gradle + Android SDK + Kotlin Multiplatform DSL compatibility;
- Kotlin change -> Compose compiler/KMP/source compatibility;
- Compose change -> UI APIs/testing runtime compatibility;
- Android target SDK change -> manifest/platform behavior and store requirements;
- Rust edition/toolchain change -> optional crate only.

### 2. Upgrade one compatibility group deliberately

Do not independently bump every version to its newest number in one unreviewable commit. Prefer a focused toolchain/dependency upgrade pull request.

### 3. Update the source of truth

Dependency/plugin/SDK versions belong in `gradle/libs.versions.toml` unless the project intentionally pins an environment version in workflow YAML.

If Gradle itself changes, update all workflow occurrences consistently:

- `.github/workflows/ci.yml`;
- `.github/workflows/codeql.yml`;
- `.github/workflows/release.yml`;
- documentation that names the validated baseline.

### 4. Run the full gate

```bash
python3 scripts/check_format.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Then verify GitHub CI and CodeQL on the exact candidate commit.

### 5. Update documentation

At minimum review:

- `README.md`;
- `docs/setup.md`;
- this file;
- `docs/build-system.md`;
- `docs/ci-cd.md`;
- `docs/troubleshooting.md`;
- `CHANGELOG.md` when the change is release-relevant.

## Dependency update policy

Dependabot checks Gradle, Cargo, and GitHub Actions weekly. A Dependabot pull request is a proposal, not proof of compatibility.

For each dependency update:

1. read the dependency/tool release notes;
2. identify breaking/deprecated APIs;
3. run relevant focused tests;
4. run the full repository gate;
5. review generated build/manifest/packaging behavior when applicable;
6. merge only after required hosted checks are green.

## Avoid these upgrade mistakes

- Do not delete Android Studio/SDK directories because a single Gradle task failed.
- Do not install multiple JDKs without knowing which one Gradle resolves.
- Do not replace JDK 17 with a different major version merely because it is newer.
- Do not raise `minSdk` unless intentionally dropping older Android support.
- Do not raise `targetSdk` without reviewing platform behavior changes.
- Do not commit `local.properties` or machine-specific SDK paths.
- Do not commit keystores, passwords, signing certificates, access tokens, or store credentials.
- Do not bypass failing CI by weakening the check unless the check itself is proven incorrect and the change is documented.

## After changing any toolchain component

Record:

- old version;
- new version;
- reason for update;
- compatibility changes required;
- exact validation commands/results;
- CI/CodeQL result;
- any user/platform support impact.

That information belongs in the pull request and, for release-impacting changes, `CHANGELOG.md` / `what_changed.md`.
