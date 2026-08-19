# CI, CodeQL, Dependabot, and Release Automation

RPS Arena uses GitHub Actions for validation/security/release packaging and GitHub configuration files for dependency updates, issue intake, release-note categorization, and funding links. This guide documents every tracked file under `.github/`, including triggers, permissions, jobs, failure meaning, artifact paths, documentation coverage, and maintenance rules.

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

Places repository source and Git metadata into the runner workspace. Git metadata is required by the documentation coverage script because it executes `git ls-files`.

### 2. Repository formatting

```bash
python3 scripts/check_format.py
```

Checks UTF-8, final newlines, and trailing whitespace policy.

### 3. Documentation file coverage

```bash
python3 scripts/check_docs_coverage.py
```

This is the repository's enforceable "do not skip files in documentation" gate.

The script:

1. runs `git ls-files -z`;
2. reads `docs/repository-file-reference.md`;
3. checks that every tracked path appears exactly in backticks;
4. prints every missing path and exits non-zero when coverage is incomplete.

This catches newly added source/config/resource/workflow/test/doc files that were not added to the exhaustive reference. It does not judge prose quality; human review still checks whether each explanation is correct and deep enough.

### 4. Version consistency

```bash
python3 scripts/check_version.py
```

Checks Android/Desktop/shared app version agreement and verifies About renders the shared version constant.

### 5. Java setup

```yaml
uses: actions/setup-java@v5
```

With:

```text
distribution: temurin
java-version: 17
cache: gradle
```

Provides JDK 17 and supported Gradle dependency caching integration.

### 6. Android SDK setup

```yaml
uses: android-actions/setup-android@v4
```

Packages:

```text
platform-tools
platforms;android-36
build-tools;36.0.0
```

This makes the hosted Android environment match the project baseline.

### 7. Gradle setup

```yaml
uses: gradle/actions/setup-gradle@v4
```

Pinned Gradle:

```text
9.5.1
```

This is especially important because the repository currently tracks no Gradle Wrapper.

### 8. Shared tests

```bash
gradle :shared:allTests --stacktrace
```

Validates common business/data/protocol/localization tests and the configured shared/desktop test target.

### 9. Android lint

```bash
gradle :androidApp:lintDebug --stacktrace
```

Checks Android resources, manifest/configuration/API usage, and other Android correctness rules.

### 10. Android debug assembly

```bash
gradle :androidApp:assembleDebug --stacktrace
```

Proves Android app/shared Android compilation and APK packaging succeed.

### 11. Desktop classes

```bash
gradle :desktopApp:classes --stacktrace
```

Proves desktop/shared JVM compilation succeeds.

## CI Rust job

Also uses Ubuntu.

Default working directory:

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

This validates the optional Rust rule mirror independently from Kotlin.

## Interpreting CI result

A PR is not fully green unless all required jobs/checks for the exact candidate succeed.

Important statuses:

- `queued` — no validation conclusion yet;
- `in_progress` — still running;
- `success` — passed;
- `failure` — at least one required step failed;
- `cancelled` — run did not complete, often because a newer commit superseded it.

A cancelled run is not equivalent to success. A successful older SHA does not validate a newer PR head.

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

Under GitHub Actions cron scheduling this means 03:17 UTC every Monday.

The scheduled run can detect newly recognized static-analysis findings even when source has not just changed.

## CodeQL permissions

```yaml
permissions:
  security-events: write
  packages: read
  contents: read
```

- `security-events: write` uploads code-scanning results;
- `contents: read` reads source;
- `packages: read` supports package access where needed by analysis/build tooling.

## CodeQL environment/build

```yaml
env:
  GRADLE_OPTS: -Dorg.gradle.daemon=false
```

Disables the Gradle daemon for the ephemeral workflow process.

The analyze job sets up:

- checkout v6;
- Temurin JDK 17;
- Android SDK 36 / Build Tools 36.0.0;
- CodeQL init v4 for `java-kotlin`;
- Gradle 9.5.1.

Build observed by CodeQL:

```bash
gradle :androidApp:assembleDebug :desktopApp:classes
```

Final analysis/upload:

```yaml
uses: github/codeql-action/analyze@v4
```

A green CodeQL job is evidence for the configured analyzer/rules, not proof that no vulnerability exists.

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

Examples:

```text
v1.1.0
v2.0.0
```

The `publish` job is further restricted to a `refs/tags/v...` ref, so a manual branch run can exercise build jobs without creating a tag release.

## Release permissions

```yaml
permissions:
  contents: write
```

Write access is needed for GitHub Release creation. Changes to this workflow therefore deserve extra review.

## Documentation coverage and release workflow

The main CI workflow now enforces `scripts/check_docs_coverage.py`. The release workflow currently repeats formatting and version checks but does **not** independently repeat the documentation coverage script.

Release policy therefore requires creating version tags from validated `main`, whose required CI has already passed documentation coverage.

A direct future change to make the release workflow repeat documentation coverage is reasonable, but the connector safety layer blocked that workflow-write attempt during this documentation phase; no bypass was used. The checked-in workflow remains the source of truth.

## Release Android job

Runs on Ubuntu and performs:

1. checkout;
2. format check;
3. version consistency check;
4. JDK 17 setup;
5. Android SDK setup;
6. Gradle 9.5.1 setup;
7. shared tests;
8. `lintRelease`;
9. `assembleRelease`;
10. upload APK artifacts.

Artifact name:

```text
rps-arena-android
```

Path:

```text
androidApp/build/outputs/apk/release/*.apk
```

`if-no-files-found: error` prevents a silent empty artifact upload.

The public repository does not provide private signing credentials, so this is a public/unsigned build validation artifact unless a controlled signing system is later added.

## Release Linux desktop job

Runs Ubuntu.

Build commands:

```bash
gradle :desktopApp:classes --stacktrace
gradle :desktopApp:packageDeb --stacktrace
```

Artifact:

```text
name: rps-arena-linux
path: desktopApp/build/compose/binaries/main/deb/*.deb
```

Current release automation does not create Windows MSI or macOS DMG jobs.

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

Uploads:

```text
name: rps-arena-rust-engine
path: rust-engine/target/package/*.crate
```

`cargo package` creates the crate archive; it does not publish to crates.io.

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

The job downloads all successful artifacts into `dist/` with `merge-multiple: true`.

Checksums:

```bash
sha256sum dist/* > dist/SHA256SUMS.txt
```

A checksum detects artifact-byte changes/corruption; it is not a substitute for platform code signing.

Release command:

```bash
gh release create "${GITHUB_REF_NAME}" dist/* \
  --verify-tag \
  --generate-notes \
  --title "RPS Arena ${GITHUB_REF_NAME}"
```

`GH_TOKEN` is supplied from `${{ github.token }}`, not a committed personal token.

`--verify-tag` ensures the tag already exists; `--generate-notes` uses GitHub's release-note generation.

## Release-note categorization

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

This affects generated GitHub release notes and does not replace `CHANGELOG.md`.

## Dependabot

File:

```text
.github/dependabot.yml
```

Schema version 2.

Weekly ecosystems:

### Gradle

```text
directory: /
open PR limit: 5
```

### Cargo

```text
directory: /rust-engine
open PR limit: 5
```

### GitHub Actions

```text
directory: /
open PR limit: 5
```

Dependabot PRs are proposals; compatibility still requires review and CI.

## Pull-request template

File:

```text
.github/pull_request_template.md
```

The template now explicitly asks authors to run:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_coverage.py
```

and to ensure every new/renamed tracked file is documented in `docs/repository-file-reference.md`.

It also covers version consistency, shared/UI/platform/Rust checks, accessibility, migrations, backup compatibility, security/privacy/networking, and release impact.

## Bug report form

File:

```text
.github/ISSUE_TEMPLATE/bug_report.yml
```

Applies `bug` label and requests platform, observed/expected behavior, reproduction steps, version, and context.

Security-sensitive reports must use `SECURITY.md`, not this public form.

## Feature request form

File:

```text
.github/ISSUE_TEMPLATE/feature_request.yml
```

Applies `enhancement`, requires problem/opportunity and proposed solution, and surfaces offline-first/no-tracking project principles.

## Issue template configuration

File:

```text
.github/ISSUE_TEMPLATE/config.yml
```

Disables blank issues and routes:

- security reports -> `SECURITY.md`;
- support -> `SUPPORT.md`.

## Funding configuration

File:

```text
.github/FUNDING.yml
```

Custom funding URL:

```text
https://buymeacoffee.com/sanskarIN
```

Funding remains optional and separate from product access/license.

## Branch/ruleset policy

Some repository settings live on GitHub rather than in files. See `docs/github-settings.md`.

Operational merge rule:

> Require CI + CodeQL (and other configured required checks) on the exact current PR head.

Do not merge because a previous commit in the same PR was green.

## Troubleshooting queued runs

While queued:

- no job-step validation has happened yet;
- no step log may exist;
- the run must not be reported as passed.

Repeated pushes can cancel prior runs through concurrency. Freeze the intended candidate before waiting for final validation.

## Troubleshooting failed jobs

Identify the first meaningful failing step:

- Formatting -> text policy;
- Documentation file coverage -> new/renamed path missing from file reference;
- Version consistency -> Android/Desktop/shared metadata drift;
- Shared tests -> business/data/protocol/localization/UI regression;
- Android lint -> Android quality/config/API issue;
- Android build -> compile/resource/package issue;
- Desktop classes -> JVM/shared desktop issue;
- Rust -> crate/test issue;
- CodeQL -> analysis/build/security finding;
- release upload -> expected artifact path produced no file.

Fix the cause in a focused commit; do not simply remove a gate to obtain green status.

## Secrets policy

Normal CI requires no private repository secrets.

The release workflow uses GitHub's scoped token for release creation.

Future signing credentials must live outside Git in an authorized secret-management environment. Never echo secrets into logs or expose them to untrusted fork PRs.

## Workflow change checklist

When editing workflows:

1. minimize permissions;
2. review action major versions;
3. keep intended JDK/Gradle/Android environment pins synchronized;
4. verify artifact/task paths;
5. protect fork PRs from secrets;
6. keep required artifact uploads fail-closed;
7. preserve diagnostic stack traces;
8. update `docs/validation.md`, `docs/release.md`, this file, and file reference;
9. run the workflow through a PR before trusting a release tag;
10. never commit raw credentials.

## Automation ownership summary

| File | Primary purpose |
|---|---|
| `.github/workflows/ci.yml` | format/docs/version/build/test/lint validation |
| `.github/workflows/codeql.yml` | scheduled/PR/push Kotlin/Java static security analysis |
| `.github/workflows/release.yml` | validate/package/upload/tag release artifacts |
| `.github/dependabot.yml` | weekly dependency/action update proposals |
| `.github/release.yml` | generated release-note categories |
| `.github/pull_request_template.md` | review/compatibility/documentation checklist |
| `.github/ISSUE_TEMPLATE/bug_report.yml` | structured public defects |
| `.github/ISSUE_TEMPLATE/feature_request.yml` | structured improvements |
| `.github/ISSUE_TEMPLATE/config.yml` | disable blank issues + security/support routing |
| `.github/FUNDING.yml` | optional project funding link |
