# CI, Security, CodeQL, Dependabot, and Release Automation

RPS Arena uses GitHub Actions for cross-platform validation, security checks, static analysis, and release packaging. Repository configuration under `.github/` also defines ownership, dependency updates, issue intake, pull-request quality gates, generated release-note categories, and funding metadata.

## `.github/` inventory

```text
.github/CODEOWNERS
.github/FUNDING.yml
.github/dependabot.yml
.github/pull_request_template.md
.github/release.yml
.github/ISSUE_TEMPLATE/bug_report.yml
.github/ISSUE_TEMPLATE/config.yml
.github/ISSUE_TEMPLATE/feature_request.yml
.github/workflows/ci.yml
.github/workflows/codeql.yml
.github/workflows/security.yml
.github/workflows/release.yml
```

These are tracked source/configuration files. Generated artifacts and runner caches are not committed.

## Exact-head rule

CI, Security checks, and CodeQL use pull-request/push events around `main`. CI and Security checks cancel obsolete runs for the same ref when newer commits arrive.

Therefore:

- a green older SHA does not validate a newer PR head;
- `queued` and `in_progress` are not success;
- a cancelled run is not success;
- merge/release decisions must use the exact candidate commit.

## Primary CI workflow

File:

```text
.github/workflows/ci.yml
```

Workflow name:

```text
CI
```

Triggers:

```text
push to main
pull_request targeting main
```

Repository-level permission:

```yaml
permissions:
  contents: read
```

CI does not need release/write access.

### Kotlin/Android/Desktop/Web job

Runner:

```text
ubuntu-latest
```

The job executes, in order:

1. `actions/checkout@v6`;
2. repository formatting;
3. relative Markdown link validation;
4. exhaustive tracked-file documentation coverage;
5. committed-secret pattern validation;
6. Android privacy-contract validation;
7. cross-platform version validation;
8. Temurin JDK 17 setup;
9. Android SDK 36 / Build Tools 36.0.0 setup;
10. Gradle 9.5.1 setup;
11. shared Kotlin tests;
12. Android lint;
13. Android debug APK assembly;
14. desktop JVM compilation;
15. JS+Wasm Web compatibility distribution build.

Fast source commands:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

Build/test commands:

```bash
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

### Why the Web compatibility distribution is a CI gate

The Web module has Kotlin/Wasm and Kotlin/JS browser executables. Building only one target could allow fallback-specific source/packaging drift. `composeCompatibilityBrowserDistribution` validates the combined output expected for deployment.

See `docs/web-platform.md` for runtime/build details.

## iOS/iPadOS CI job

Runner:

```text
macos-latest
```

A macOS runner is required because Kotlin/Native Apple framework linking and Xcode host compilation depend on Apple tooling.

Setup:

- checkout v6;
- Temurin JDK 17;
- Gradle 9.5.1.

Kotlin framework gate:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

SwiftUI/Xcode host gate:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

This validates the shared Kotlin/Native framework plus native SwiftUI bridge without requiring a developer certificate or provisioning secret.

It does **not** produce a signed App Store archive.

## Rust CI job

Runner:

```text
ubuntu-latest
```

Working directory:

```text
rust-engine
```

Commands:

```bash
cargo test --all-targets
```

The Rust crate remains optional to the Kotlin application runtime but is still required to stay healthy in repository CI.

## Focused Security workflow

File:

```text
.github/workflows/security.yml
```

Workflow name:

```text
Security checks
```

Triggers on push/PR around `main` and uses read-only source permissions except for the pull-request read permission required by dependency review.

### Secret/privacy job

Runs:

```bash
python scripts/check_for_secrets.py
python scripts/check_android_privacy.py
```

The first check looks for several high-confidence committed credential/private-key forms without echoing matched secret values. The second enforces the Android offline/automatic-backup contract.

### Dependency review

Pull requests run:

```yaml
uses: actions/dependency-review-action@v4
with:
  fail-on-severity: high
```

High-severity dependency findings therefore fail the focused security workflow.

This supplements Dependabot and human dependency review; it is not proof that every dependency is vulnerability-free.

## CodeQL

File:

```text
.github/workflows/codeql.yml
```

The workflow analyzes Java/Kotlin on:

- pushes to `main`;
- pull requests targeting `main`;
- weekly schedule.

Environment includes JDK 17, Android SDK 36, and Gradle 9.5.1. The observed build covers the Android and desktop Kotlin/JVM paths before CodeQL analysis/upload.

CodeQL is one security signal; it does not replace tests, dependency review, privacy checks, or manual review of native/Web platform code.

## Release workflow

File:

```text
.github/workflows/release.yml
```

Triggers:

```text
workflow_dispatch
push tags matching v*
```

Repository permission:

```yaml
permissions:
  contents: write
```

This write permission exists so a validated tag run can create the GitHub Release. Do not copy it into normal PR build workflows.

## Release source preflight

The Android release job repeats all fast source gates:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

Publication depends on all platform jobs, so a failure in these source checks or any required platform package/validation job prevents the publish stage from running.

Release automation is defense in depth. Tags must still point to a `main` commit whose PR CI, Security checks, and CodeQL passed on that exact source.

## Android release job

Runner:

```text
ubuntu-latest
```

Key commands:

```bash
gradle :shared:allTests --stacktrace
gradle :androidApp:lintRelease --stacktrace
gradle :androidApp:assembleRelease --stacktrace
```

Artifact:

```text
name: rps-arena-android
path: androidApp/build/outputs/apk/release/*.apk
```

The public repository contains no private Android signing credentials, so this is a public/unsigned validation artifact unless a separate authorized signing system is used later.

## Linux desktop release job

Runner:

```text
ubuntu-latest
```

Commands:

```bash
gradle :desktopApp:classes --stacktrace
gradle :desktopApp:packageDeb --stacktrace
```

Artifact:

```text
name: rps-arena-linux
path: desktopApp/build/compose/binaries/main/deb/*.deb
```

Windows MSI and macOS DMG formats are configured in the desktop build but need host-specific packaging/signing workflows for production distribution.

## Web release job

Runner:

```text
ubuntu-latest
```

Build:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

The generated directory:

```text
webApp/build/dist/composeWebCompatibility/productionExecutable/
```

is zipped as:

```text
rps-arena-web.zip
```

Artifact name:

```text
rps-arena-web
```

The ZIP is suitable for deployment to a static host after browser compatibility/manual checks.

## iOS release job

Runner:

```text
macos-latest
```

Builds release frameworks:

```bash
gradle \
  :shared:linkReleaseFrameworkIosArm64 \
  :shared:linkReleaseFrameworkIosSimulatorArm64 \
  --stacktrace
```

Validates the Release SwiftUI host without signing:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Release \
  CODE_SIGNING_ALLOWED=NO \
  build
```

Packages:

```text
rps-arena-ios-device-framework.zip
rps-arena-ios-simulator-framework.zip
```

Artifact name:

```text
rps-arena-ios
```

These are framework/integration artifacts, not signed IPA/TestFlight/App Store packages. Apple signing credentials remain outside Git.

## Rust release job

Runs:

```bash
cargo test --all-targets
cargo package
```

Artifact:

```text
name: rps-arena-rust-engine
path: rust-engine/target/package/*.crate
```

`cargo package` creates an archive; it does not publish to crates.io.

## Publish job

Runs only for tag refs matching `refs/tags/v...`.

Dependencies:

```text
android
desktop-linux
web
ios
rust
```

Artifacts are downloaded into one `dist/` tree and SHA-256 checksums are generated:

```bash
sha256sum dist/* > dist/SHA256SUMS.txt
```

Release creation uses the repository-provided GitHub token rather than a committed personal access token:

```bash
gh release create "${GITHUB_REF_NAME}" dist/* \
  --verify-tag \
  --generate-notes \
  --title "RPS Arena ${GITHUB_REF_NAME}"
```

Checksums detect artifact-byte changes/corruption; they are not equivalent to platform signing/notarization.

## Release-note categories

File:

```text
.github/release.yml
```

Generated-note groups cover features, fixes, security/reliability, documentation, and other changes. `skip-changelog` and `dependencies` labels are excluded from generated categories according to the current configuration.

`CHANGELOG.md` remains the curated project changelog.

## Dependabot

File:

```text
.github/dependabot.yml
```

Weekly update ecosystems:

- Gradle at repository root;
- Cargo under `/rust-engine`;
- GitHub Actions at repository root.

Dependabot PRs still require normal compatibility/security/CI review.

## Pull-request template

File:

```text
.github/pull_request_template.md
```

The checklist covers source gates, data/backup compatibility, security/privacy, optional networking, accessibility, platform builds, version consistency, release impact, and the requirement that every new/renamed tracked file be documented in `docs/repository-file-reference.md`.

## Issue templates

### Bug report

File:

```text
.github/ISSUE_TEMPLATE/bug_report.yml
```

Requests platform, behavior, reproduction steps, version, and context. Sensitive security findings belong in the private path described by `SECURITY.md`.

### Feature request

File:

```text
.github/ISSUE_TEMPLATE/feature_request.yml
```

Surfaces the project's offline-first/no-tracking design constraints when proposing new functionality.

### Configuration

File:

```text
.github/ISSUE_TEMPLATE/config.yml
```

Disables blank issues and routes security/support requests to the appropriate policy documents.

## CODEOWNERS

File:

```text
.github/CODEOWNERS
```

Default ownership is `@sanskarIN`, with explicit path coverage for security/automation, shared core, Rust, and platform packaging areas. Repository rulesets must require code-owner approval if review is intended to be mandatory.

## Funding

File:

```text
.github/FUNDING.yml
```

Contains optional Buy Me a Coffee funding metadata. Funding does not affect MIT licensing, functionality, CI, or access to the project.

## Maintenance rules

When changing CI/release/platform targets:

1. keep workflow permissions minimal;
2. preserve exact-head validation semantics;
3. keep the fast source gates synchronized across CI/release/local docs;
4. add host-specific jobs only where required (for example macOS for iOS);
5. never expose signing secrets to untrusted pull requests;
6. keep artifact paths and `if-no-files-found: error` behavior accurate;
7. update `docs/release.md`, `docs/validation.md`, platform docs, README, changelog, and file reference;
8. test compatibility-distribution/package commands before calling a platform supported;
9. distinguish unsigned validation artifacts from signed production/store releases.
