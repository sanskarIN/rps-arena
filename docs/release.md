# Release

RPS Arena should not be tagged as complete until a clean checkout passes the required verification and the resulting UI is manually reviewed.

## Versioning

Use semantic versioning. Keep these aligned for a release:

- Android `versionName`/`versionCode`;
- desktop `packageVersion`;
- `CHANGELOG.md`;
- release notes;
- Git tag.

## Release-candidate verification

From a clean checkout:

```bash
gradle --no-daemon :shared:compileKotlinDesktop
gradle --no-daemon :shared:desktopTest
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
gradle --no-daemon :desktopApp:classes
```

Then run the optional Rust checks:

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

Also require:

- successful CodeQL analysis when repository security scanning is available;
- dependency-update/security review;
- manual primary-journey checklist from `docs/testing.md`;
- accessibility checklist from `docs/accessibility.md`;
- documentation-link verification;
- review for secrets/private data/generated signing files.

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

## Release notes

Include:

- version and exact tag;
- supported platforms;
- user-visible changes;
- fixed defects;
- privacy/security/accessibility changes;
- storage or backup migration notes;
- known limitations;
- exact artifacts that were actually produced.

## Rollback

Prefer a tested patch release when a defect is found after publishing. If distribution must pause, clearly mark the affected release and preserve the history needed to understand security or migration issues.
