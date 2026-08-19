# Release

RPS Arena should not be tagged as complete until a clean checkout passes the required verification and the resulting UI is manually reviewed.

## Versioning

Use semantic versioning. The authoritative application version lives in `gradle/libs.versions.toml`:

- `appVersion` feeds Android `versionName` and desktop `packageVersion`;
- `appVersionCode` feeds Android `versionCode`.

For every release, update those catalog values once and keep the following aligned with them:

- `CHANGELOG.md`;
- release notes;
- Git tag.

Do not reintroduce independent hard-coded Android or desktop release versions.

## Release-candidate verification

From a clean checkout:

```bash
gradle --no-daemon :shared:compileKotlinDesktop
gradle --no-daemon :shared:allTests
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
gradle --no-daemon :desktopApp:classes
python scripts/check_docs_links.py
python scripts/check_android_privacy.py
python scripts/check_for_secrets.py
```

Then run the optional Rust checks:

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

Also require:

- successful latest CI workflow for Kotlin/Android/Desktop and Rust;
- successful latest CodeQL analysis;
- successful latest Documentation workflow;
- successful Android privacy-contract validation;
- successful committed-secret scan;
- successful dependency review for the release pull request when GitHub dependency review is available;
- Dependabot/security-alert review;
- manual primary-journey checklist from `docs/testing.md`;
- accessibility checklist from `docs/accessibility.md`;
- review for secrets/private data/generated signing files;
- confirmation that repository rules/protection use exact observed check names, if enabled.

Do not treat cancelled superseded workflow runs as failures; evaluate the workflows attached to the exact release-candidate commit.

## Android local-data privacy gate

Before release, keep the Android privacy contract intact:

- `android.permission.INTERNET` is absent from the v1 manifest;
- `android:allowBackup` remains `false`;
- `android:fullBackupContent` points to `@xml/backup_rules`;
- `android:dataExtractionRules` points to `@xml/data_extraction_rules`;
- legacy backup rules exclude all application shared preferences;
- current cloud-backup rules exclude all application shared preferences;
- current device-transfer rules exclude all application shared preferences.

`scripts/check_android_privacy.py` enforces these repository invariants. The user-controlled RPS Arena V2 text export/import remains the explicit portability mechanism for application data.

## Backup compatibility gate

For v1.0, manually verify both supported backup paths:

1. Generate a V2 backup containing at least two profiles, non-default match setup, statistics, and history.
2. Confirm the preview summary appears without changing current data.
3. Import the V2 backup and verify all persisted sections are restored.
4. Import a valid V1 backup and verify it migrates to the default local profile.
5. Confirm malformed and oversized backups are rejected without partial mutation.

The current export format is `RPS_ARENA_BACKUP_V2`. V1 is import-only compatibility.

## Automated artifacts

`.github/workflows/release.yml` can be run manually or from a `v*` tag.

It produces:

- an **unsigned** Android release APK artifact;
- a Compose Desktop distributable on Linux;
- a Compose Desktop distributable on Windows;
- a Compose Desktop distributable on macOS.

The unsigned Android artifact is intentionally not a Play Store signing solution.

## Signing

Store-distribution signing material must remain outside Git. Never commit:

- Android keystores;
- key passwords;
- private certificates;
- signing API tokens;
- exported private keys.

A future signed-release job must use encrypted repository/environment secrets with the minimum required permissions and must not print sensitive values.

## Desktop packaging

The desktop module configures DMG, MSI, and DEB targets. Native packaging is OS-specific; release artifacts should be created on the corresponding operating system.

## Screenshots

Only capture and publish screenshots from a verified build. Do not substitute design mockups while labeling them as application screenshots.

Capture at minimum:

- Android home/play screen;
- Android settings with local profiles/data controls;
- desktop play screen;
- desktop statistics/recent-trend screen;
- one light-theme and one dark-theme view if both are release-verified.

## Release notes

Include:

- version and exact tag;
- exact release commit SHA;
- supported platforms;
- user-visible changes;
- fixed defects;
- privacy/security/accessibility changes;
- V1-to-V2 backup migration note;
- known limitations, including device-wide aggregate stats and no production private-room transport;
- exact artifacts that were actually produced;
- workflow results used as release evidence.

## Rollback

Prefer a tested patch release when a defect is found after publishing. If distribution must pause, clearly mark the affected release and preserve the history needed to understand security or migration issues.
