# CI, CodeQL, Dependabot, and Release Automation

RPS Arena uses GitHub Actions for validation/security/release packaging and GitHub configuration files for dependency updates, issue intake, release-note categorization, and funding links. This guide documents every tracked file under `.github/` and explains triggers, permissions, jobs, failure meaning, artifact paths, and maintenance rules.

## `.github/` file inventory

```text
.github/FUNDING.yml
.github/dependabot.yml
.github/pull_request_template.md
.github/release.yml
.github/ISSUE_TEMPLATE/bug_report.yml
.github/ISSUE_TEMPLATE/config.yml
.github/ISSUE_TEMPLATE/feature_request.yml
.github/workflows/ci.yml
.github/workflows/codeql.yml
.github/workflows/release.yml
```

These files are repository automation/governance source, not generated build output.

## CI workflow

File:

```text
.github/workflows/ci.yml
```

Workflow name:

```text
CI
```

### Triggers

Runs on:

```text
push to main
pull_request targeting main
```

It does not run for every arbitrary branch push unless that branch is part of a qualifying pull request.

### Permissions

```yaml
permissions:
  contents: read
```

The CI workflow needs source-read access only. It should not have release/write permissions because tests/builds do not need them.

## CI concurrency

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

Meaning:

- runs are grouped by workflow + Git ref;
- when a newer run for the same PR/ref starts, an older in-progress/queued run can be cancelled;
- this prevents wasting runner time validating obsolete commits.

Consequently, many rapid documentation/code commits can leave earlier runs cancelled/queued. The merge gate must use the **latest exact PR head**, not an older green run.

## CI Kotlin job

Runner:

```text
ubuntu-latest
```

Steps, in order:

### 1. Checkout

```yaml
uses: actions/checkout@v6
```

Places repository source into the runner workspace.

### 2. Repository formatting

```bash
python3 scripts/check_format.py
```

Checks UTF-8, final newlines, and trailing whitespace policy.

### 3. Version consistency

```bash
python3 scripts/check_version.py
```

Checks Android/Desktop/shared app version agreement and About metadata usage.

### 4. Java setup

```yaml
uses: actions/setup-java@v5
```

With:

```text
distribution: temurin
java-version: 17
cache: gradle
```

This provides JDK 17 and enables supported Gradle dependency caching integration.

### 5. Android SDK setup

```yaml
uses: android-actions/setup-android@v4
```

Packages:

```text
platform-tools
platforms;android-36
build-tools;36.0.0
```

This makes the runner's Android environment match the project SDK baseline.

### 6. Gradle setup

```yaml
uses: gradle/actions/setup-gradle@v4
```

Pinned Gradle:

```text
9.5.1
```

This matters because the repository currently has no Gradle Wrapper.

### 7. Shared tests

```bash
gradle :shared:allTests --stacktrace
```

Validates common unit/business tests and configured shared target tests, including desktop UI smoke coverage under the current KMP setup.

### 8. Android lint

```bash
gradle :androidApp:lintDebug --stacktrace
```

Catches Android-specific correctness/configuration/resource/API issues.

### 9. Android debug assembly

```bash
gradle :androidApp:assembleDebug --stacktrace
```

Proves Android application/shared Android compilation and APK packaging succeed for the debug variant.

### 10. Desktop classes

```bash
gradle :desktopApp:classes --stacktrace
```

Proves desktop/shared desktop JVM compilation succeeds.

## CI Rust job

Also uses Ubuntu.

Workflow-level default working directory:

```text
rust-engine
```

Steps:

1. checkout;
2. install stable Rust through `dtolnay/rust-toolchain@stable`;
3. run:

```bash
cargo test --all-targets
```

This job validates the optional Rust rule mirror independently from Kotlin.

## Interpreting CI result

A pull request is not fully green unless **both** jobs succeed:

- `kotlin`;
- `rust`.

A cancelled run is not equivalent to a passed run.

A queued run has no conclusion and must not be described as successful.

A successful old commit does not validate a newer PR head.

## CodeQL workflow

File:

```text
.github/workflows/codeql.yml
```

Workflow name:

```text
CodeQL
```

### Triggers

Runs on:

- push to `main`;
- pull request targeting `main`;
- weekly schedule.

Cron:

```text
17 3 * * 1
```

Meaning under GitHub Actions cron/UTC scheduling: minute 17, hour 3, every Monday.

The scheduled run helps catch newly recognized security patterns even when source has not just changed.

## CodeQL permissions

```yaml
permissions:
  security-events: write
  packages: read
  contents: read
```

- `security-events: write` is required to upload code-scanning results;
- `contents: read` reads source;
- `packages: read` supports dependency/package access needed by analysis/build contexts.

Do not give CodeQL broader repository write permissions without a concrete need.

## CodeQL Gradle option

```yaml
env:
  GRADLE_OPTS: -Dorg.gradle.daemon=false
```

This disables the Gradle daemon for the workflow process, simplifying lifecycle behavior on ephemeral hosted runners.

## CodeQL analyze job

Job display name:

```text
Analyze Kotlin/Java
```

Runner:

```text
ubuntu-latest
```

Setup includes:

- checkout v6;
- Temurin JDK 17;
- Android SDK 36/Build Tools 36.0.0;
- CodeQL init v4 for `java-kotlin`;
- Gradle 9.5.1.

Build command:

```bash
gradle :androidApp:assembleDebug :desktopApp:classes
```

This builds relevant JVM/Kotlin/Android code while CodeQL observes compilation.

Final step:

```yaml
uses: github/codeql-action/analyze@v4
```

uploads analysis results to GitHub code scanning.

## CodeQL limitations

A green CodeQL job means the configured analyzer found no blocking result under its rules/configuration. It does not prove the absence of every vulnerability.

Manual security design review remains necessary for:

- backup/import changes;
- future networking;
- permissions;
- signing/release automation;
- secrets;
- untrusted parsers;
- FFI/unsafe Rust if introduced.

## Release workflow

File:

```text
.github/workflows/release.yml
```

Workflow name:

```text
Release
```

### Triggers

Manual:

```text
workflow_dispatch
```

Tag push:

```text
v*
```

Examples matching tag trigger:

```text
v1.1.0
v2.0.0
```

A manual run validates/builds artifacts but the `publish` job is gated to actual `refs/tags/v...` refs.

## Release permissions

```yaml
permissions:
  contents: write
```

Write permission is required for the final GitHub Release creation.

Because this workflow can publish release assets, changes to it deserve stricter review than ordinary documentation changes.

## Release Android job

Runs Ubuntu and performs:

1. checkout;
2. format check;
3. version consistency check;
4. JDK 17 setup;
5. Android SDK setup;
6. Gradle 9.5.1 setup;
7. shared tests;
8. `lintRelease`;
9. `assembleRelease`;
10. upload release APK artifacts.

Artifact name:

```text
rps-arena-android
```

Path:

```text
androidApp/build/outputs/apk/release/*.apk
```

`if-no-files-found: error` prevents a successful-looking upload step with missing package output.

The artifact is public-build/unsigned unless external signing configuration is deliberately added.

## Release Linux desktop job

Runs Ubuntu.

Steps:

- checkout;
- JDK 17;
- Gradle 9.5.1;
- desktop classes;
- Debian package task;
- artifact upload.

Command:

```bash
gradle :desktopApp:packageDeb --stacktrace
```

Artifact name:

```text
rps-arena-linux
```

Path:

```text
desktopApp/build/compose/binaries/main/deb/*.deb
```

Current workflow does not publish Windows MSI or macOS DMG because those require host-specific jobs and, for production trust, signing/notarization decisions.

## Release Rust job

Working directory:

```text
rust-engine
```

Runs:

```bash
cargo test --all-targets
cargo package
```

Artifact name:

```text
rps-arena-rust-engine
```

Path:

```text
rust-engine/target/package/*.crate
```

Again, `cargo package` does not publish to crates.io.

## Release publish job

Condition:

```yaml
if: startsWith(github.ref, 'refs/tags/v')
```

Dependencies:

```text
android
desktop-linux
rust
```

The publish job does not run until those jobs succeed.

### Artifact download

Downloads all job artifacts into:

```text
dist
```

with `merge-multiple: true`, producing one release staging directory.

### Checksums

```bash
sha256sum dist/* > dist/SHA256SUMS.txt
```

Creates SHA-256 digest list for staged artifacts.

A checksum helps detect accidental/corrupt downloads. It is not a replacement for signed provenance/code signing.

### GitHub release creation

Uses GitHub CLI:

```bash
gh release create "${GITHUB_REF_NAME}" dist/* \
  --verify-tag \
  --generate-notes \
  --title "RPS Arena ${GITHUB_REF_NAME}"
```

`GH_TOKEN` receives `${{ github.token }}`.

Meaning:

- release is created for current tag name;
- `--verify-tag` requires that tag to exist rather than implicitly creating a release tag;
- `--generate-notes` uses GitHub release-note generation;
- all files in `dist/` including checksum file are attached.

## Release note categorization

File:

```text
.github/release.yml
```

Excluded labels:

- `skip-changelog`;
- `dependencies`.

Categories:

- Features -> `enhancement`, `feature`;
- Fixes -> `bug`, `fix`;
- Security and reliability -> `security`, `reliability`;
- Documentation -> `documentation`;
- Other changes -> wildcard.

This configuration affects GitHub-generated release notes. It does not replace hand-maintained `CHANGELOG.md`.

Maintain label names consistently or categories will not work as intended.

## Dependabot

File:

```text
.github/dependabot.yml
```

Schema version:

```text
2
```

Weekly update ecosystems:

### Gradle

Directory:

```text
/
```

Open PR limit:

```text
5
```

Covers Gradle dependencies/plugins represented in the project.

### Cargo

Directory:

```text
/rust-engine
```

Open PR limit 5.

### GitHub Actions

Directory:

```text
/
```

Open PR limit 5.

This helps keep action references current.

Dependabot proposals still require compatibility review and CI; do not auto-assume an update is safe.

## Pull-request template

File:

```text
.github/pull_request_template.md
```

The current template asks authors to document/verify:

- what changed;
- format check;
- version consistency when relevant;
- shared tests;
- desktop UI tests for shared UI;
- Android lint/build;
- desktop classes;
- Rust tests when relevant;
- manual accessibility review;
- docs/changelog/roadmap/handoff updates;
- settings/history/stat migration compatibility;
- backup format compatibility;
- bounded/validated new persistent data;
- no committed secrets/signing credentials;
- no unreviewed tracking/ads/cloud/network permission;
- optional networking boundary preservation;
- release version/note impact.

The checkboxes are a review aid; GitHub Actions provide executable enforcement for the automated subset.

## Bug report issue form

File:

```text
.github/ISSUE_TEMPLATE/bug_report.yml
```

Automatically applies label:

```text
bug
```

Asks for:

- platform: Android/Windows/macOS/Linux/Shared logic;
- what happened + expected behavior;
- reproduction steps;
- app/version;
- additional context.

Platform and reproduction/problem fields are required.

Security-sensitive problems should not use this public form; `config.yml` directs security reporting to `SECURITY.md`.

## Feature request form

File:

```text
.github/ISSUE_TEMPLATE/feature_request.yml
```

Automatically applies:

```text
enhancement
```

Requires:

- problem/opportunity;
- proposed solution.

Also surfaces project principles:

- preserve offline-first default;
- avoid tracking/ads.

These principle checkboxes help set expectations but do not automatically enforce architecture.

## Issue-template configuration

File:

```text
.github/ISSUE_TEMPLATE/config.yml
```

```yaml
blank_issues_enabled: false
```

This discourages unstructured blank public issues.

Contact links:

- Security reports -> `SECURITY.md`;
- Support guide -> `SUPPORT.md`.

Links target the default `main` branch. Documentation changes on a feature branch become visible there after merge.

## Funding configuration

File:

```text
.github/FUNDING.yml
```

Custom funding link:

```text
https://buymeacoffee.com/sanskarIN
```

GitHub can surface this through repository sponsor/funding UI.

Funding is optional and is not part of gameplay, licensing, or access control.

## Required branch protection/ruleset concept

GitHub settings are not all stored in repository files. `docs/github-settings.md` documents recommended protection for `main`.

The important operational rule is:

> Require the exact current PR head's CI and CodeQL checks before merging.

Do not merge because an earlier commit in the same PR had a green run while newer commits remain queued/failing.

## Workflow changes checklist

When editing any workflow:

1. minimize permissions;
2. pin/update action major versions deliberately;
3. review whether fork PRs can access secrets;
4. keep environment versions synchronized across CI/CodeQL/release where intended;
5. verify task/artifact paths;
6. use `if-no-files-found: error` for required release artifacts;
7. preserve `--stacktrace` on Gradle validation tasks;
8. run/test workflow through a PR before relying on it for a tag;
9. update `docs/validation.md`, `docs/release.md`, this guide, and file reference;
10. never put raw credentials in YAML.

## Troubleshooting queued runs

A run may remain queued because of hosted-runner availability or account/repository scheduling. While queued:

- there are no step logs yet;
- it has not validated source;
- do not mark it passed;
- do not merge if it is required.

Repeated pushes can cause older runs to be cancelled through concurrency.

Freeze the candidate commit before waiting for final validation so runners test the exact intended merge head.

## Troubleshooting failed jobs

Use GitHub Actions job logs and identify the first meaningful failing step.

Examples:

- Formatting -> file policy problem;
- Version consistency -> synchronized metadata drift;
- Shared tests -> business/persistence/UI test regression;
- Android lint -> Android-specific quality issue;
- assembleDebug -> compile/package issue;
- desktop classes -> JVM/shared-desktop compile issue;
- Rust -> crate/test issue;
- CodeQL -> code scanning/build/analyzer issue;
- release artifact upload -> build produced no file at expected path.

Fix the cause in a focused commit; do not simply remove the failing gate.

## Secrets policy

No current CI test job requires private secrets.

Release workflow uses GitHub's scoped repository token for release creation, not a committed personal token.

Future signing secrets must live in GitHub Environments/Secrets or another authorized secret manager with least privilege and protected release access.

Never echo signing passwords/private keys/tokens to logs.

## Automation ownership summary

| File | Primary purpose |
|---|---|
| `.github/workflows/ci.yml` | build/test/lint validation |
| `.github/workflows/codeql.yml` | scheduled/PR/push static security analysis |
| `.github/workflows/release.yml` | validate/package/upload/tag release artifacts |
| `.github/dependabot.yml` | weekly dependency/action update proposals |
| `.github/release.yml` | generated release-note categories |
| `.github/pull_request_template.md` | review checklist |
| `.github/ISSUE_TEMPLATE/bug_report.yml` | structured public defects |
| `.github/ISSUE_TEMPLATE/feature_request.yml` | structured improvements |
| `.github/ISSUE_TEMPLATE/config.yml` | disable blank issues + security/support routing |
| `.github/FUNDING.yml` | optional project funding link |
