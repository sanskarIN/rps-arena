# Troubleshooting

## Java or Gradle mismatch

RPS Arena targets JVM 17 and uses Gradle 9.5.0 as the documented CI baseline.

```bash
java -version
gradle --version
```

Point `JAVA_HOME` to a JDK 17+ installation when necessary.

## Android SDK not found

Install Android platform 36 and Build Tools 36.0.0. If Android Studio has not generated `local.properties`, create it locally:

```properties
sdk.dir=/your/local/android/sdk
```

Do not commit `local.properties`.

## Android package installation fails in CI

The project intentionally targets stable Android API 36. Do not switch CI to a preview platform unless the hosted runner's SDK manager can install it and the toolchain is officially compatible.

## Dependency resolution failure

Verify access to Google Maven, Maven Central, and the Gradle Plugin Portal. Do not fix a repository outage by downloading unknown JAR files into source control.

## Shared tests fail

Run the focused task with a stack trace:

```bash
gradle --no-daemon :shared:desktopTest --stacktrace
```

For rule, persistence, timer, and state bugs, add a deterministic regression test before or with the fix.

## Android build or lint fails

```bash
gradle --no-daemon :androidApp:assembleDebug --stacktrace
gradle --no-daemon :androidApp:lintDebug --stacktrace
```

Confirm SDK 36 is installed and the version catalog still matches the documented toolchain.

## Desktop application will not run

First compile it:

```bash
gradle --no-daemon :desktopApp:classes --stacktrace
```

Then:

```bash
gradle :desktopApp:run
```

Failure to open an external About link should not prevent the game from running.

## Rust CI fails

From `rust-engine/`:

```bash
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

Run `cargo fmt --all` locally before committing formatting fixes.

## Local data looks corrupt

RPS Arena falls back to safe defaults for invalid stored codecs. Use Settings to generate a backup if you need the current state for debugging, then use the confirmed full-data reset if recovery is not needed.

Do not post private machine information or secrets in bug reports.

## Backup import is rejected

Only the exact supported `RPS_ARENA_BACKUP_V1` format is accepted. Import validates settings, stats, match configuration, and history before replacing local data. Unknown/corrupt formats are rejected intentionally.

## Seeded challenge differs from another run

A seed is reproducible only when the active variant, difficulty, match setup, and ordered player inputs are also identical. Changing inputs changes how the deterministic CPU sequence is consumed.

## Reporting unresolved problems

Use the GitHub bug template with platform, app version, exact reproduction steps, and sanitized logs. Report security-sensitive issues privately using `SECURITY.md`.
