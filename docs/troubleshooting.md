# Troubleshooting

## Android SDK not found or API 36 missing

Confirm `ANDROID_HOME`/Android Studio points to an SDK containing platform 36 and Build Tools 36.0.0. In Android Studio, install them from SDK Manager, then retry:

```bash
gradle :androidApp:assembleDebug --stacktrace
```

## Wrong JDK

The project targets JVM 17. Verify:

```bash
java -version
```

Configure Gradle/Android Studio to use JDK 17 if a different runtime is selected.

## Gradle dependency or cache failure

Retry with the stack trace first rather than deleting caches immediately:

```bash
gradle :shared:allTests --stacktrace
```

If the failure is a corrupted local cache, stop Gradle daemons and refresh dependencies only after reading the failure output.

## Desktop application does not start

Compile first to separate source errors from runtime packaging issues:

```bash
gradle :desktopApp:classes --stacktrace
```

Then run:

```bash
gradle :desktopApp:run --stacktrace
```

## Backup import is rejected

A valid backup starts with:

```text
RPS_ARENA_BACKUP|1
```

The importer rejects malformed, oversized, duplicate, or unknown records. Generate a fresh backup from Settings when possible rather than editing the text manually.

## Settings appear reset after upgrading

The application migrates legacy `settings_v1` data to `settings_v2`. If a legacy record is malformed, safe defaults are used rather than accepting corrupted state. Statistics/history use their existing compatible records.

## Timer expires unexpectedly

Changing match mode, opponent, rules, difficulty, seed, or timer resets the current match. In local two-player mode each player's turn receives a fresh countdown. Set the round timer to **Off** to disable timeout scoring.

## Private-room code is rejected

Room codes are six characters and intentionally avoid ambiguous characters such as `I`, `O`, `0`, and `1`. The current repository includes the transport-neutral contract and in-memory reference adapter; primary gameplay remains offline and does not request Android network permission.

## CI differs from local behavior

CI uses JDK 17, Gradle 9.5.1, Android SDK 36/Build Tools 36.0.0, and current repository lock/configuration. Compare local versions with `.github/workflows/ci.yml` before changing source code to work around an environment mismatch.

## Reporting a reproducible defect

Use the GitHub bug-report form and include platform, version, exact steps, expected result, actual result, and sanitized logs. Never attach credentials, signing material, or private user data.
