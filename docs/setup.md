# Setup Guide

RPS Arena targets Android API 26+ and desktop JVM platforms from a shared Kotlin Multiplatform codebase. This page is the short setup path; use `docs/toolchain.md` for deep installation/upgrade guidance and `docs/command-reference.md` for command meanings.

## Required tools

Validated project baseline:

- JDK 17;
- Gradle 9.5.1 locally or another version proven compatible with the project;
- Android SDK 36 + Build Tools 36.0.0 for Android parity with CI;
- Python 3 for repository validation scripts;
- Git;
- Android Studio or IntelliJ IDEA for Kotlin/Compose development;
- Rust stable only when working on the optional `rust-engine/`.

## Important: no tracked Gradle Wrapper

The current repository does not contain `gradlew`, `gradlew.bat`, or `gradle/wrapper/*`. Use a locally installed `gradle` executable.

Verify:

```bash
gradle --version
java -version
python3 --version
git --version
```

On Windows, Python may be exposed as `python` or `py` instead of `python3`.

## Clone

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
```

`git clone` creates the local repository working copy; `cd` enters it.

## Verify repository/documentation integrity

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_version.py
```

- format check validates UTF-8/newlines/whitespace policy;
- documentation coverage verifies every Git-tracked file is present in `docs/repository-file-reference.md`;
- version check verifies Android/Desktop/shared app version synchronization.

## Verify application code

```bash
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
```

Optional Rust:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Or use the repository scripts:

Unix-like:

```bash
bash scripts/verify.sh
```

PowerShell:

```powershell
./scripts/verify.ps1
```

## Run desktop

```bash
gradle :desktopApp:run
```

Desktop data uses Java Preferences through the shared desktop `PlatformStore` adapter.

## Run Android

1. Install Android platform SDK 36/required Build Tools through Android Studio SDK Manager or supported SDK command-line tools.
2. Open the repository in Android Studio.
3. Allow Gradle project synchronization to finish.
4. Select/create the `androidApp` run configuration.
5. Connect an API 26+ Android device/emulator.
6. Run the application.

Command-line debug APK:

```bash
gradle :androidApp:assembleDebug --stacktrace
```

The manifest currently requests no network permission.

## Optional Rust engine

From repository root:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

or:

```bash
cd rust-engine
cargo test --all-targets
```

The Rust engine is a standalone rules mirror and is not required to run the Kotlin application.

## IDE guidance

Use Android Studio when working on Android SDK/resources/emulators/manifests. IntelliJ IDEA is suitable for Kotlin/shared/desktop work when its installed tooling supports the project. VS Code can be useful for Markdown, Git, Python validation scripts, and optional Rust work.

See `docs/toolchain.md` before installing large sets of unnecessary extensions/tools.

## Machine-specific files

Android tooling may create `local.properties` with an SDK path. It is intentionally ignored and must not be committed.

Generated output such as `.gradle/`, module `build/`, and `rust-engine/target/` is also untracked/reproducible.

## Local data

RPS Arena is offline-first.

Android uses private application `SharedPreferences`; desktop uses Java Preferences. Product-level formats and backup/migration behavior are documented in `docs/storage-and-backup.md`.

The primary app requires no cloud account or mandatory network connection.

## Commit identity for the project owner

Repository-local configuration:

```bash
git config user.email "sanskarin@outlook.in"
git config user.name "Sanskar"
```

`.mailmap` also defines the canonical owner identity.

## Next reading

- `docs/documentation-index.md` — full documentation map;
- `docs/toolchain.md` — installation/upgrades;
- `docs/command-reference.md` — command meanings;
- `docs/build-system.md` — Gradle/KMP structure;
- `docs/troubleshooting.md` — common failures;
- `docs/repository-file-reference.md` — every tracked file.

Do not commit signing keys, tokens, store credentials, API keys, certificates, personal backups, or private user data.
