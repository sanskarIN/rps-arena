# Setup

## Requirements

- Git
- JDK 17 or newer
- Gradle 9.5.0 for the documented verification baseline
- Android SDK 36 and Build Tools 36.0.0 for Android development
- Android Studio or IntelliJ IDEA for IDE-based development
- A supported desktop operating system for Compose Desktop
- Rust stable only when working on the optional `rust-engine/`

## Clone

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
```

For the current final-audit branch before merge:

```bash
git checkout chatgpt/final-audit-20260819
```

## Verify Java and Gradle

```bash
java -version
gradle --version
```

RPS Arena compiles Kotlin/JVM targets for Java 17.

## Android SDK

Install Android platform 36 and Build Tools 36.0.0 with Android Studio's SDK Manager or `sdkmanager`.

If Android Studio does not create it automatically, add a local `local.properties` file pointing to your own SDK:

```properties
sdk.dir=/your/local/android/sdk
```

`local.properties` is machine-specific and must not be committed.

## Verify shared and desktop code

```bash
gradle --no-daemon :shared:compileKotlinDesktop
gradle --no-daemon :shared:desktopTest
gradle --no-daemon :desktopApp:classes
```

Run the desktop application:

```bash
gradle :desktopApp:run
```

## Verify Android

```bash
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
```

The debug APK is produced under `androidApp/build/outputs/apk/debug/`.

## Optional Rust engine

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

## Runtime configuration

RPS Arena v1 requires no API keys, cloud account, server endpoint, analytics token, or environment secret. `.env.example` intentionally documents that absence.

## First launch defaults

- Classic rules
- CPU opponent
- Normal difficulty
- Best of 3
- deterministic seed `20260819`
- timer off
- system theme
- reduced motion off

The first-run onboarding is local and does not require sign-in.
