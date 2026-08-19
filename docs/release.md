# Release Guide

## Release gate

A release candidate is eligible only when:

- `main` CI is green;
- CodeQL is green;
- shared tests pass;
- Android debug/release compilation passes as configured;
- desktop classes/package tasks pass on the relevant host;
- Rust tests pass;
- changelog, roadmap, privacy notes, and `what_changed.md` match the shipped behavior;
- no signing key, store credential, token, certificate, or private user data is committed.

## Version locations

For v1.1.0 the public version is declared in:

- `androidApp/build.gradle.kts` (`versionCode = 2`, `versionName = "1.1.0"`);
- `desktopApp/build.gradle.kts` (`packageVersion = "1.1.0"`);
- About UI;
- `CHANGELOG.md`.

Keep these values synchronized when preparing later versions.

## Local verification

```bash
gradle :shared:allTests --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Run the manual checks in `docs/testing.md` and `docs/accessibility.md` before tagging a stable release.

## GitHub release artifacts

The tag workflow is designed to build reproducible unsigned/public artifacts without repository secrets. Store signing remains a separate controlled step.

Recommended release tag format:

```text
v1.1.0
```

Tag only the audited `main` commit.

## Android signing

Keep keystores, passwords, Play credentials, and signing configuration outside Git. Signed store artifacts should be produced in an authorized environment using private credentials injected at release time.

The public repository must remain buildable without those credentials.

## Desktop signing/notarization

Windows signing and Apple signing/notarization require external certificates/accounts. Keep these credentials outside the repository. Unsigned packages can still be generated for CI validation where platform policy permits.

## Release notes

Include:

- user-visible features and fixes;
- supported platforms;
- privacy/networking behavior;
- migration/backup compatibility notes;
- known limitations;
- verification summary;
- checksum information for published artifacts;
- contact/support and MIT license links.

## Rollback

If a release artifact or tag points to an incorrect commit, stop distribution, document the problem, fix forward on a new patch version, and avoid rewriting published release history unless there is a compelling security reason.
