# iOS and iPadOS Platform Reference

RPS Arena now exposes the same shared Compose Multiplatform application on iPhone and iPad through a small SwiftUI host. Game rules, state, localization, persistence codecs, backup/import, achievements, and most UI remain in the `:shared` module.

## Platform status

- iPhone and iPad are supported through the Kotlin/Native `iosArm64` device target and `iosSimulatorArm64` Apple-silicon simulator target.
- The Xcode host explicitly excludes the `x86_64` simulator architecture because the repository does not configure a Kotlin/Native `iosX64` framework target.
- Building/running the iOS application requires macOS and Xcode.
- Public CI validates an Apple-silicon simulator build without private signing credentials.
- App Store/TestFlight signing remains an external credential-bearing release step.

## Files

```text
shared/src/iosMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.ios.kt
shared/src/iosMain/kotlin/in/sanskar/rpsarena/ui/MainViewController.kt
iosApp/iosApp/iOSApp.swift
iosApp/iosApp/ContentView.swift
iosApp/iosApp/Info.plist
iosApp/iosApp.xcodeproj/project.pbxproj
iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/RPS Arena.xcscheme
```

## Shared Gradle targets

`shared/build.gradle.kts` declares:

```kotlin
iosArm64()
iosSimulatorArm64()
```

Each target creates a static framework named:

```text
RpsArenaShared
```

The framework contains the shared Compose UI and shared application logic.

The Xcode project sets `EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64` for the application target. This keeps Xcode's requested simulator architecture aligned with the configured `iosSimulatorArm64` Kotlin target instead of asking Gradle to build an unavailable `iosX64` framework.

## Kotlin entry point

`MainViewController.kt` exposes:

```kotlin
fun MainViewController(): UIViewController
```

It initializes the iOS `PlatformStore` and returns a `ComposeUIViewController` that renders `RpsArenaApp()`.

The Swift host therefore does not duplicate gameplay screens or business rules.

## SwiftUI host

`iOSApp.swift` is the native `@main` application entry point.

`ContentView.swift` wraps the Kotlin `UIViewController` with `UIViewControllerRepresentable` and displays it as the SwiftUI root content.

This keeps the native shell intentionally thin:

```text
SwiftUI App -> Compose UIViewController -> RpsArenaApp -> shared state/engine/repository
```

## Local persistence

The iOS `PlatformStore` uses `NSUserDefaults.standardUserDefaults`.

The same logical keys and codecs used on Android/Desktop/Web remain authoritative in `ArenaRepository`, including settings, statistics, history, and match configuration.

Platform storage is only a string key/value transport. It must not reimplement codec or validation rules.

## App metadata

`iosApp/iosApp/Info.plist` currently declares:

```text
CFBundleShortVersionString = 2.5.8
CFBundleVersion = 20508
```

The Xcode target also uses:

```text
MARKETING_VERSION = 2.5.8
CURRENT_PROJECT_VERSION = 20508
```

`scripts/check_version.py` validates these values against Android, desktop, and shared `APP_VERSION` metadata.

The numeric build convention is:

```text
major * 10000 + minor * 100 + patch
```

For `2.5.8`, the expected build number is `20508`.

## Rendering metadata

`Info.plist` includes `CADisableMinimumFrameDurationOnPhone = true` so Compose can use the display refresh behavior expected by the current iOS integration.

The target supports both iPhone and iPad device families.

## Xcode direct integration

The Xcode project contains a `Compile Kotlin Framework` build phase.

It runs:

```bash
gradle :shared:embedAndSignAppleFrameworkForXcode
```

from the repository root.

The repository currently does not track a Gradle Wrapper, so the Xcode phase intentionally uses the locally installed `gradle` command. Keep this synchronized with the repository's documented no-wrapper build architecture.

## Build the Kotlin simulator framework

From the repository root on an Apple-silicon macOS host:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

Release framework:

```bash
gradle :shared:linkReleaseFrameworkIosSimulatorArm64 --stacktrace
```

Device release framework:

```bash
gradle :shared:linkReleaseFrameworkIosArm64 --stacktrace
```

Generated frameworks are under `shared/build/bin/<target>/<buildType>Framework/` and are not tracked by Git.

## Build the iOS app from command line

Unsigned simulator validation:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

The project's simulator architecture exclusion keeps this command on `arm64`. This is the same style of host build exercised by CI.

## Run from Xcode

1. Open `iosApp/iosApp.xcodeproj` on an Apple-silicon Mac.
2. Select the `RPS Arena` scheme.
3. Select an iPhone or iPad simulator/device.
4. Ensure JDK 17 and the validated Gradle baseline are available to the Xcode build phase.
5. Run the application.

A physical device build may require an Apple development team/profile depending on local Xcode signing configuration.

An Intel-only iOS simulator build is not part of the current source-support claim because `iosX64` is not configured.

## CI

The `ios` job in `.github/workflows/ci.yml` runs on an Apple-silicon macOS runner and:

1. installs JDK 17;
2. installs the repository's Gradle baseline;
3. links the iOS simulator Kotlin framework;
4. builds the SwiftUI host for the iOS simulator without code signing.

This catches Kotlin/Native target failures, missing Swift/Kotlin bridge symbols, Xcode project errors, and common framework-integration regressions.

## Release workflow

The release workflow builds both:

```text
iosArm64 release framework
iosSimulatorArm64 release framework
```

and validates the Release iOS simulator host.

It packages the frameworks into ZIP artifacts for reproducible public validation.

These framework ZIPs are not App Store IPA files and are not a substitute for signed App Store/TestFlight distribution.

## Signing boundary

Never commit:

- Apple signing certificates/private keys;
- provisioning profiles containing private account material;
- App Store Connect API keys;
- Apple account passwords/session credentials.

Signed distribution must inject authorized credentials outside the public repository.

## Privacy/offline behavior

The iOS host adds no account, analytics, ads, cloud service, or mandatory networking dependency. The shared game remains offline-first.

Local settings/statistics/history are stored through `NSUserDefaults` and the explicit `RPS_ARENA_BACKUP|1` format remains the user-controlled portability mechanism.

## Adding iOS-specific APIs

Prefer shared APIs first. Add iOS-specific code under `iosMain` only when native APIs are actually required, for example:

- haptics;
- native share sheet;
- platform file picker;
- future local-network transport;
- platform-specific accessibility behavior.

Keep rules, CPU strategy, scoring, persistence codecs, and normal product state in common code.

## iOS change checklist

When iOS-related code changes:

1. run `python3 scripts/check_version.py` when version metadata changes;
2. link an iOS simulator framework;
3. build the Xcode simulator host;
4. verify persistence across relaunch;
5. verify English/Hindi rendering;
6. test portrait and landscape behavior;
7. test iPhone and iPad layouts where practical;
8. test VoiceOver/text scaling for UI changes;
9. update this guide, release notes, and the exhaustive file reference when platform files change;
10. never weaken signing/secret handling merely to make public CI produce a store artifact.
