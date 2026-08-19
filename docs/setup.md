# Setup Guide

RPS Arena targets Android API 26+ and desktop JVM platforms from a shared Kotlin Multiplatform codebase.

## Required tools

- JDK 17
- Gradle 9.5.1 or a compatible local Gradle installation
- Android SDK 36 with Build Tools 36.0.0 for Android builds
- Android Studio or IntelliJ IDEA for Kotlin/Compose development
- Rust stable only when working on the optional `rust-engine/`

## Clone and verify

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
gradle :shared:allTests
gradle :desktopApp:classes
gradle :androidApp:assembleDebug
```

## Run desktop

```bash
gradle :desktopApp:run
```

## Run Android

Open the repository in Android Studio, select the `androidApp` run configuration, connect an API 26+ emulator/device, and run the application. A command-line debug APK can be built with:

```bash
gradle :androidApp:assembleDebug
```

## Optional Rust engine

```bash
cd rust-engine
cargo test --all-targets
```

The Rust engine is a standalone rules mirror and is not required to run the Kotlin application.

## Local data

RPS Arena is offline-first. Android uses private application preferences and desktop uses its platform-local store. The app does not require a cloud account or network permission for its primary experience.

## Commit identity for the project owner

```bash
git config user.email "sanskarin@outlook.in"
git config user.name "Sanskar"
```

Do not commit signing keys, tokens, store credentials, API keys, or private user data.
