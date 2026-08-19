# Validation

RPS Arena treats executable CI evidence as the release gate. A document saying that a check should pass is not a substitute for the check actually passing on the candidate commit.

## Required automated checks

The `CI` workflow requires:

- repository text formatting check;
- relative Markdown documentation-link check;
- exhaustive tracked-file documentation-reference coverage check;
- high-confidence committed-secret pattern check;
- Android offline/automatic-backup privacy-contract check;
- Android/desktop/shared/About semantic-version consistency plus Android numeric `versionCode` mapping;
- shared Kotlin test suite, including logger/privacy-adjacent business tests and desktop UI tests;
- Android lint;
- Android debug assembly;
- desktop JVM compilation;
- optional Rust engine tests.

The separate `CodeQL` workflow analyzes Kotlin/Java code.

The focused `Security checks` workflow independently re-runs the secret/privacy source checks and, for pull requests, performs dependency review with high-severity findings configured to fail.

## Source-quality gates

Run all fast source checks before compilation:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

### Documentation completeness

`scripts/check_docs_coverage.py` obtains every Git-tracked path with `git ls-files` and requires that exact path to appear in backticks inside `docs/repository-file-reference.md`.

This prevents source/config/workflow/resource/test/documentation files from being added silently without being represented in the exhaustive file reference.

The check proves path coverage, not that every explanation is perfect. Human review still evaluates documentation correctness/depth.

### Documentation links

`scripts/check_docs_links.py` validates repository-relative Markdown link targets and rejects links that escape the repository root or resolve to missing files. External URLs are intentionally outside this offline source check.

### Committed-secret patterns

`scripts/check_for_secrets.py` looks for several high-confidence credential/private-key forms while skipping generated/IDE output, large files, binaries, and its own detector source. It is defense in depth, not a claim that every possible secret format can be recognized.

### Android privacy contract

`scripts/check_android_privacy.py` parses the primary Android manifest plus legacy/Android 12+ backup rules. It fails if automatic backup is re-enabled, SharedPreferences backup/device-transfer exclusions disappear, XML becomes invalid, or the primary manifest gains `android.permission.INTERNET`.

### Version consistency

`scripts/check_version.py` requires Android `versionName`, desktop `packageVersion`, and shared `APP_VERSION` to match; verifies About renders the shared constant; and checks Android `versionCode` against `major * 10000 + minor * 100 + patch`. For v2.5.8, the required Android code is `20508`.

## Release validation

The tag/manual workflow repeats repository formatting, documentation links, exhaustive documentation coverage, committed-secret patterns, Android privacy contract, version verification, shared tests, Android release lint/build, desktop Linux packaging, and Rust package tests before a tagged release can publish public unsigned artifacts and checksums.

Release tags must still be created from validated `main`: the release workflow is a second release-specific gate, not a replacement for pull-request CI, Security checks, and CodeQL on the exact candidate commit. If release workflow behavior changes, keep `docs/ci-cd.md`, `docs/release.md`, and this file synchronized.

Signing credentials are intentionally outside the public repository and are not required to validate the open-source build.

## Local parity commands

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

The shell and PowerShell helpers in [`scripts/`](../scripts/) run the same repository-level gates (with optional local Rust execution when Cargo is installed).

## Manual evidence

Before a stable tag, complete the journeys in [`testing.md`](testing.md) and the accessibility checks in [`accessibility.md`](accessibility.md). Record any unresolved blocker as an explicit known limitation instead of declaring a clean release.

For documentation-only milestones, also manually verify:

- README/documentation-index links point to tracked files;
- commands match the current build architecture (including the absence of a Gradle Wrapper);
- platform/privacy/security claims match source/manifests and Android backup-policy XML;
- version/tool numbers are described as project baselines unless independently intended as current-global claims;
- every new file has a useful explanation, not merely a filename mention.

## Exact-head rule

Required checks must pass on the exact commit intended for merge.

Because CI, Security checks, and CodeQL use `cancel-in-progress: true`, rapid commits can cancel obsolete runs. A green older SHA does not validate a newer documentation/code head.

Do not merge while the current candidate's required jobs are queued, in progress, cancelled without replacement, or failed.

## Validation history

The v1.0.0 build audit established the Kotlin/Android/Desktop/Rust/CodeQL baseline. Version 2.5.8 extends that gate with formatting, synchronized semantic versions and Android numeric version-code validation, Android lint, persistence migration/backup, timeout, trend, localization/UI, private-room protocol regression coverage, exhaustive tracked-file documentation coverage, relative-link validation, committed-secret detection, fail-closed Android backup/privacy checks, dependency review, and redacting logger regression coverage. The release workflow independently repeats all fast source gates before release packaging.

`what_changed.md` is the handoff source for the exact current validation result and most recent meaningful commits.
