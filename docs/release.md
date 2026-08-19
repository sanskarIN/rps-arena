# Release Guide

## Release gate

A release candidate is eligible only when:

- `main` CI is green on the exact candidate;
- focused Security checks are green;
- CodeQL is green;
- repository formatting passes;
- relative documentation links pass;
- exhaustive tracked-file documentation coverage passes;
- high-confidence committed-secret scanning passes;
- Android privacy-contract validation passes;
- cross-platform version consistency passes;
- shared tests pass;
- Android debug/release lint and compilation pass as configured;
- desktop classes/package tasks pass on the relevant host;
- Rust tests pass;
- changelog, roadmap, privacy notes, and `what_changed.md` match the shipped behavior;
- no signing key, store credential, token, certificate, or private user data is committed.

## Version locations

For v2.5.8 the public version is declared in:

- `androidApp/build.gradle.kts` (`versionCode = 20508`, `versionName = "2.5.8"`);
- `desktopApp/build.gradle.kts` (`packageVersion = "2.5.8"`);
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AppMetadata.kt` (`APP_VERSION = "2.5.8"`);
- About UI, which renders the shared version constant;
- `CHANGELOG.md`.

Android `versionCode` follows `major * 10000 + minor * 100 + patch`; `scripts/check_version.py` validates this mapping in addition to synchronizing the public semantic version across platforms.

Keep these values synchronized when preparing later versions.

## Local verification

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Run the manual checks in `docs/testing.md` and `docs/accessibility.md` before tagging a stable release.

## Android privacy preflight

Before release, verify that the primary Android manifest still has no internet permission, keeps `android:allowBackup="false"`, and points to both backup-policy XML files. The XML policies exclude the complete SharedPreferences domain from legacy backup, Android cloud backup, and device-to-device transfer.

`scripts/check_android_privacy.py` enforces these source invariants in CI, the focused security workflow, local verification, and release preflight.

## GitHub release artifacts

The tag workflow is designed to build reproducible unsigned/public artifacts without repository secrets. It re-runs formatting, documentation-link validation, exhaustive documentation coverage, committed-secret patterns, Android privacy, and version consistency before the release build/test/package steps. Store signing remains a separate controlled step.

Recommended release tag format:

```text
v2.5.8
```

Tag only the audited `main` commit. The tag workflow is an additional release gate and does not replace green pull-request CI, Security checks, and CodeQL evidence on that exact source.

## Android signing

Keep keystores, passwords, Play credentials, and signing configuration outside Git. Signed store artifacts should be produced in an authorized environment using private credentials injected at release time.

The public repository must remain buildable without those credentials.

## Desktop signing/notarization

Windows signing and Apple signing/notarization require external certificates/accounts. Keep these credentials outside the repository. Unsigned packages can still be generated for CI validation where platform policy permits.

## Release notes

Include:

- user-visible features and fixes;
- supported platforms;
- privacy/networking and automatic-backup behavior;
- migration/explicit-backup compatibility notes;
- known limitations;
- verification summary;
- checksum information for published artifacts;
- contact/support and MIT license links.

## Rollback

If a release artifact or tag points to an incorrect commit, stop distribution, document the problem, fix forward on a new patch version, and avoid rewriting published release history unless there is a compelling security reason.
