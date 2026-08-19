# Android Platform Reference

This guide documents every tracked file under `androidApp/` plus the Android-specific shared storage adapter. It explains application identity, SDK levels, manifest behavior, launcher activity, adaptive icon resources, platform theme shell, offline permission posture, automatic-backup exclusions, local storage initialization, build tasks, packaging, and signing boundaries.

## Android file inventory

Tracked Android application files:

```text
androidApp/build.gradle.kts
androidApp/src/main/AndroidManifest.xml
androidApp/src/main/kotlin/in/sanskar/rpsarena/MainActivity.kt
androidApp/src/main/res/drawable/ic_launcher_foreground.xml
androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
androidApp/src/main/res/values/colors.xml
androidApp/src/main/res/values/themes.xml
androidApp/src/main/res/xml/backup_rules.xml
androidApp/src/main/res/xml/data_extraction_rules.xml
```

Related shared Android adapter:

```text
shared/src/androidMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.android.kt
```

No Android application/source/resource file outside this list is currently tracked in the repository.

## `androidApp/build.gradle.kts`

### Applied plugins

```kotlin
alias(libs.plugins.androidApplication)
alias(libs.plugins.composeMultiplatform)
alias(libs.plugins.composeCompiler)
```

Responsibilities:

- Android application plugin creates Android variants/APK packaging;
- Compose Multiplatform provides Compose dependencies/tooling integration;
- Compose compiler plugin transforms `@Composable` Kotlin code.

The app module itself uses the shared Compose UI rather than duplicating screens.

## Namespace and application ID

```kotlin
namespace = "in.sanskar.rpsarena"
applicationId = "in.sanskar.rpsarena"
```

`namespace` is the Android source/resource namespace.

`applicationId` is the installed application identity used by Android and distribution systems.

Changing `applicationId` creates a different Android application identity and can break upgrade continuity. Do not change it casually for a normal version update.

## SDK configuration

Values come from `gradle/libs.versions.toml`:

```text
compileSdk = 36
targetSdk  = 36
minSdk     = 26
```

### Compile SDK

Controls which Android API symbols can be compiled against.

It does not itself force users to run Android 36.

### Target SDK

Declares the Android platform behavior level the app is designed/tested for. Raising it can enable new platform restrictions/behavior changes and must be tested.

### Minimum SDK

API 26 is the current minimum supported Android version. Raising it intentionally drops older devices; lowering it requires validating that all dependencies/APIs support the lower level.

## Android version

```kotlin
versionCode = 20508
versionName = "2.5.8"
```

`versionCode` is the Android distribution ordering value. This repository maps semantic versions deterministically as:

```text
major * 10000 + minor * 100 + patch
```

with `minor` and `patch` limited to values no greater than 99. `scripts/check_version.py` enforces this mapping and also synchronizes `versionName` with desktop/shared metadata.

A future v2.5.9 would therefore update at least:

```text
versionCode -> 20509
versionName -> 2.5.9
```

plus desktop/shared version constants and changelog/release docs.

## Build features

```kotlin
buildFeatures {
    compose = true
    buildConfig = true
}
```

Compose is required for `setContent`/shared UI integration.

`BuildConfig` generation is enabled, although current cross-platform public version metadata lives in shared `AppMetadata.kt` rather than reading Android-only `BuildConfig`.

## Packaging resource exclusion

```kotlin
resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
```

Some dependencies may contribute duplicate metadata resources under these names. The exclusion avoids APK resource merge conflicts.

This does not change RPS Arena's MIT project license and does not grant permission to ignore dependency license obligations. Dependency licensing must still be respected.

## Android dependencies

```kotlin
implementation(project(":shared"))
implementation(libs.androidx.activity.compose)
```

The Android app receives:

- all shared product logic/UI from `:shared`;
- `androidx.activity:activity-compose` for Compose integration in `ComponentActivity`.

## `AndroidManifest.xml`

Current manifest declares the application and launcher activity only.

### No permissions

There are no `<uses-permission>` elements.

In particular, the current primary app does **not** request:

```text
android.permission.INTERNET
```

This supports the offline-first claim.

If a future LAN feature requires permissions, manifest changes must be explicit and privacy/security docs must be updated before release.

## Application privacy/backup attributes

### `android:allowBackup="false"`

Android automatic application backup is deliberately disabled. RPS Arena does not rely on platform cloud backup/device-transfer as a hidden transport for local SharedPreferences.

This is separate from the app's explicit, user-controlled `RPS_ARENA_BACKUP|1` text export/import.

### `android:fullBackupContent="@xml/backup_rules"`

Points legacy Android backup behavior at `androidApp/src/main/res/xml/backup_rules.xml`.

That rule excludes the entire `sharedpref` domain:

```xml
<exclude domain="sharedpref" path="." />
```

### `android:dataExtractionRules="@xml/data_extraction_rules"`

Points Android 12+ backup/data-transfer policy at `androidApp/src/main/res/xml/data_extraction_rules.xml`.

The file excludes `sharedpref` from both:

- cloud backup;
- device-to-device transfer.

The explicit rules provide defense in depth with `allowBackup=false` and make the intended local-data boundary reviewable in source.

### Enforced privacy contract

Run:

```bash
python3 scripts/check_android_privacy.py
```

The checker fails when:

- `allowBackup` is not `false`;
- manifest references to the two backup-rule files change incorrectly;
- either XML file is missing or malformed;
- the legacy/cloud/device-transfer SharedPreferences exclusions disappear;
- `android.permission.INTERNET` appears in the primary manifest.

This source gate runs in normal CI, the focused security workflow, tagged/manual release preflight, and both local verification scripts.

### `android:icon` / `android:roundIcon`

Reference adaptive launcher resources:

```text
@mipmap/ic_launcher
@mipmap/ic_launcher_round
```

### `android:label="RPS Arena"`

Application label visible to Android launcher/system UI.

### `android:supportsRtl="true"`

Allows Android layout direction support for RTL locales. This is infrastructure support, not proof that every future RTL translation has been manually validated.

### `android:theme="@style/Theme.RpsArena"`

Applies the Android window/activity shell theme before Compose draws the shared UI.

## Launcher activity declaration

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
```

`android:exported="true"` is necessary because the activity has an intent filter that external Android launcher/system components use.

Intent filter:

```text
android.intent.action.MAIN
android.intent.category.LAUNCHER
```

Together these mark `MainActivity` as the app launcher entry point.

There are currently no deep-link or exported service/receiver/provider declarations.

## `MainActivity.kt`

Class:

```kotlin
class MainActivity : ComponentActivity()
```

Startup flow:

```kotlin
super.onCreate(savedInstanceState)
PlatformStore.initialize(this)
setContent { RpsArenaApp() }
```

### Why storage initializes before UI

Shared `ArenaRepository()` defaults to `PlatformStore`. The app may read settings immediately during `RpsArenaApp`/`ArenaState` creation.

Therefore Android `SharedPreferences` must be initialized with a valid `Context` before shared UI/state is constructed.

## Android `PlatformStore`

File:

```text
shared/src/androidMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.android.kt
```

It implements the common `expect PlatformStore` using `SharedPreferences`.

### Initialization

The supplied `Any?` context must cast to Android `Context` or initialization throws:

```text
Android PlatformStore requires a Context
```

Then it uses application context, avoiding retention of an Activity instance.

### Preferences name

```text
rps_arena
```

Mode:

```text
Context.MODE_PRIVATE
```

The Android backup policy excludes the complete SharedPreferences domain, so this store is not automatically copied through Android cloud backup or device transfer.

### Read

`getString` returns stored value or requested default.

If storage somehow has not been initialized, it returns the default instead of dereferencing null.

### Write

Uses:

```kotlin
edit().putString(key, value).apply()
```

This is appropriate for the tiny local preference/stat/history strings currently stored.

## Backup policy resources

### Legacy full-backup rules

File:

```text
androidApp/src/main/res/xml/backup_rules.xml
```

Structure:

```xml
<full-backup-content>
    <exclude domain="sharedpref" path="." />
</full-backup-content>
```

This covers legacy Android full-backup APIs.

### Android 12+ data-extraction rules

File:

```text
androidApp/src/main/res/xml/data_extraction_rules.xml
```

Both `cloud-backup` and `device-transfer` contain the same root SharedPreferences exclusion. If a future storage backend is introduced, reassess these rules rather than assuming SharedPreferences coverage protects unrelated files/databases.

## Launcher color resource

File:

```text
androidApp/src/main/res/values/colors.xml
```

Defines:

```text
ic_launcher_background = #6750A4
```

This color is used by adaptive icon XML.

It is not the entire in-app Material theme; shared `ArenaTheme.kt` owns Compose light/dark color schemes.

## Platform window theme

File:

```text
androidApp/src/main/res/values/themes.xml
```

Style:

```text
Theme.RpsArena
```

Parent:

```text
android:style/Theme.Material.Light.NoActionBar
```

Meaning:

- uses a platform Material-light style for the activity window shell;
- disables the traditional ActionBar because Compose provides the app UI/top bar.

Configured items:

### `android:windowActionModeOverlay = true`

Allows action-mode behavior to overlay rather than force traditional action-bar layout space.

### `android:fontFamily = sans`

Sets a basic platform sans-serif default for the Android window context.

Compose Material typography can independently control rendered Compose text.

### `android:colorAccent = #6750A4`

Provides a platform accent for non-Compose/system-controlled UI that may consult the activity theme.

### Navigation/status bars

Both are set to black, and `windowLightStatusBar=false` indicates light status-bar icons/text appropriate for a dark status bar.

If future edge-to-edge/system-bar design changes, test contrast/navigation behavior across supported Android versions.

## Adaptive icon foreground

File:

```text
androidApp/src/main/res/drawable/ic_launcher_foreground.xml
```

It is an Android vector drawable with:

- 108dp x 108dp canvas;
- 108 x 108 viewport;
- purple field;
- white circular arena disc;
- purple horizontal gesture/bar motif.

Because it is vector XML, no binary density-specific foreground PNGs are required for the current adaptive icon.

## Adaptive launcher icon

File:

```text
androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
```

Combines:

```text
background -> @color/ic_launcher_background
foreground -> @drawable/ic_launcher_foreground
```

`mipmap-anydpi-v26` targets Android adaptive-icon support introduced at API 26, which matches the app's current minimum SDK.

## Round adaptive icon

File:

```text
androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```

It currently has the same adaptive background/foreground definition as `ic_launcher.xml`.

Android launchers that prefer a round icon can apply their own mask to the adaptive icon layers.

## Repository SVG artwork vs Android icon resources

`assets/logo.svg` and `assets/splash.svg` are repository/documentation artwork.

Android's launcher icon uses Android vector/adaptive resources under `androidApp/src/main/res`.

Changing one does not automatically regenerate the other.

When rebranding, review both asset families so README artwork and installed launcher icon do not drift.

## Splash behavior

The repository includes `assets/splash.svg` for branding/documentation, but the current Android manifest/resources do not define a separate Android 12 SplashScreen theme/resource pipeline.

Do not claim that the repository SVG is automatically used by Android startup.

A future native splash implementation should use supported Android splash APIs/themes and preserve fast startup/accessibility.

## Android build tasks

Lint debug:

```bash
gradle :androidApp:lintDebug --stacktrace
```

Build debug APK:

```bash
gradle :androidApp:assembleDebug --stacktrace
```

Release lint:

```bash
gradle :androidApp:lintRelease --stacktrace
```

Build release APK:

```bash
gradle :androidApp:assembleRelease --stacktrace
```

See `docs/command-reference.md` for command semantics.

## Typical debug APK location

Gradle generally writes debug APK output under:

```text
androidApp/build/outputs/apk/debug/
```

Build output is ignored by Git.

Release automation expects release APKs under:

```text
androidApp/build/outputs/apk/release/*.apk
```

If Android Gradle Plugin changes output layout in a future toolchain, update release workflow artifact paths and docs together.

## Android signing

The public repository intentionally ignores:

```text
*.jks
*.keystore
```

and does not contain signing passwords/configuration.

Do not commit a real keystore to make CI "easier."

For store release:

- store key/certificate securely outside Git;
- inject credentials through an authorized secret-bearing build environment;
- limit secret access to trusted release jobs;
- never expose secrets to untrusted fork pull requests;
- document signing process without publishing the secret itself.

## `local.properties`

Ignored because Android tooling often stores a machine-specific SDK path there.

Do not commit:

```text
sdk.dir=...
```

because another developer/CI machine uses a different path.

## Device/emulator workflow

After building/running from Android Studio, verify connected targets with:

```bash
adb devices
```

Manual release checks should include:

- minimum supported API where practical;
- current target/modern Android behavior;
- light/dark/system theme;
- English/Hindi;
- text scaling;
- TalkBack;
- timers on/off;
- local two-player pass-and-play;
- backup/reset flow;
- configuration-chip wrapping on narrow width;
- app restart/persistence.

## Offline-first/privacy verification

Before release, confirm:

- no unexpected `<uses-permission android:name="android.permission.INTERNET">`;
- `android:allowBackup="false"` remains present;
- both backup rule resources still exclude the root SharedPreferences domain;
- `python3 scripts/check_android_privacy.py` passes;
- no analytics/ads/network SDK introduced through dependencies;
- CPU/local gameplay works with airplane mode/no network;
- explicit backup remains local text only;
- optional room architecture remains no-network unless a separately approved LAN adapter ships.

Dependency changes deserve review because a manifest can be merged from libraries; Android Lint/build output and merged-manifest inspection may be used when relevant.

## Adding Android-specific functionality

Use Android platform code only when a capability cannot remain common.

Examples:

- haptic feedback engine;
- audio effect integration;
- sharing/export intents;
- Android-specific file picker;
- future LAN permissions/discovery.

Keep game rules/state ownership in shared code when possible.

## Android change checklist

When editing Android build/manifest/resources/platform storage:

1. run formatting/docs/privacy/version source checks as relevant;
2. run shared tests if shared behavior changed;
3. run `lintDebug`;
4. run `assembleDebug`;
5. inspect manifest/merged manifest for permission/privacy impact;
6. verify backup/data-transfer exclusions if persistence behavior changed;
7. run desktop build too if shared code changed;
8. update screenshots/docs if visual branding changed;
9. update privacy/security docs when permissions/data behavior changes;
10. update version/release docs when distribution behavior changes.
