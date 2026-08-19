# Setup

## Requirements

- Git
- JDK 17 or newer
- Gradle 9.5.0 when using the repository without a generated wrapper
- Android Studio with Android SDK 37 for Android development
- A supported desktop OS for Compose Desktop

The version catalog pins Kotlin, Compose Multiplatform, Android Gradle Plugin, AndroidX, and coroutine dependencies.

## Clone

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
```

If testing the active development branch before it is merged:

```bash
git checkout chatgpt/full-build-20260819
```

## Verify Java and Gradle

```bash
java -version
gradle --version
```

The build targets JVM 17.

## Desktop

Compile and test:

```bash
gradle :shared:desktopTest
gradle :desktopApp:compileKotlin
```

Run:

```bash
gradle :desktopApp:run
```

## Android

Set the Android SDK location in your local `local.properties` if Android Studio has not generated it automatically:

```properties
sdk.dir=/your/local/android/sdk
```

Do not commit `local.properties`.

Build and lint:

```bash
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

The debug APK is generated under `androidApp/build/outputs/apk/debug/`.

## IDE import

Open the repository root in Android Studio or IntelliJ IDEA. Allow Gradle sync to finish before editing generated run configurations.

## No environment secrets required

RPS Arena v1 does not require API keys, server URLs, accounts, analytics credentials, or cloud configuration. `.env.example` documents that intentional absence of runtime secrets.

## First launch

RPS Arena starts with:

- Classic rules
- CPU opponent
- Normal difficulty
- Best of 3
- System theme
- no round timer
- deterministic default seed `20260819`

All of these settings can be changed locally from the Settings screen.
