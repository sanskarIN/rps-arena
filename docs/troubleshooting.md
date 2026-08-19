# Troubleshooting

## Gradle cannot find a compatible Java runtime

RPS Arena targets JVM 17. Confirm:

```bash
java -version
gradle --version
```

Point `JAVA_HOME` to a JDK 17+ installation and reopen the terminal/IDE if needed.

## Android SDK not found

Install Android SDK 37 through Android Studio and create a local `local.properties` file if the IDE does not do so automatically:

```properties
sdk.dir=/your/local/android/sdk
```

Never commit `local.properties`.

## Dependency resolution fails

Confirm access to Google Maven, Maven Central, and Gradle Plugin Portal. Retry after ruling out a proxy or transient repository problem. Do not “fix” resolution by downloading unknown JAR files into the repository.

## Android lint or build fails after an SDK update

Run the failing command directly for focused output:

```bash
gradle --stacktrace :androidApp:assembleDebug
gradle --stacktrace :androidApp:lintDebug
```

Check that the installed Android SDK matches `compileSdk` and that the pinned AGP/Gradle/JDK versions remain compatible.

## Desktop app will not launch

First confirm compilation:

```bash
gradle :shared:desktopTest :desktopApp:compileKotlin
```

Then run:

```bash
gradle :desktopApp:run
```

If opening an external URL fails, the game should continue working; URL launch is intentionally best-effort.

## Local data looks corrupt

The repository parser falls back to safe defaults for malformed settings and ignores malformed history entries. If local data is still unusable, use Settings → Reset local data.

Create a backup before reset only if you need the current data for a bug report. Remove personal or machine-specific information before sharing any report.

## A seeded CPU run is different than expected

A seed reproduces the CPU sequence only when the same mode, variant, difficulty, and player-history inputs are replayed in the same order. Changing difficulty or player moves changes how the random sequence is consumed.

## CI differs from local output

Use the same baseline as CI:

- JDK 17
- Gradle 9.5.0
- dependency versions from `gradle/libs.versions.toml`
- Android SDK 37 for Android jobs

Inspect `.github/workflows/ci.yml` before changing local tool versions.

## Reporting unresolved problems

Use the GitHub bug-report template and include the exact failing command and relevant error output. Redact credentials, private paths, and personal data. Security-sensitive problems belong in the private process described in `SECURITY.md`.
