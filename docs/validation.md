# Validation

RPS Arena treats executable CI evidence as the release gate. A document saying that a check should pass is not a substitute for the check actually passing on the candidate commit.

## Required automated checks

The `CI` workflow requires:

- repository text formatting check;
- exhaustive tracked-file documentation-reference coverage check;
- Android/desktop/shared/About version consistency check;
- shared Kotlin test suite;
- Android lint;
- Android debug assembly;
- desktop JVM compilation;
- optional Rust engine tests.

The separate `CodeQL` workflow analyzes Kotlin/Java code.

## Documentation completeness gate

```bash
python3 scripts/check_docs_coverage.py
```

The checker obtains every Git-tracked path with `git ls-files` and requires that exact path to appear in backticks inside `docs/repository-file-reference.md`.

This prevents source/config/workflow/resource/test/documentation files from being added silently without being represented in the exhaustive file reference.

The check proves path coverage, not that every explanation is perfect. Human review still evaluates documentation correctness/depth.

## Release validation

The tag workflow repeats repository formatting, exhaustive documentation coverage, version verification, shared tests, Android release lint/build, desktop Linux packaging, and Rust package tests before a tagged release can publish public unsigned artifacts and checksums.

Release tags must still be created from validated `main`: the release workflow is a second release-specific gate, not a replacement for pull-request CI and CodeQL on the exact candidate commit. If release workflow behavior changes, keep `docs/ci-cd.md` and this file synchronized.

Signing credentials are intentionally outside the public repository and are not required to validate the open-source build.

## Local parity commands

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_coverage.py
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
- platform/privacy/security claims match source/manifests;
- version/tool numbers are described as project baselines unless independently intended as current-global claims;
- every new file has a useful explanation, not merely a filename mention.

## Exact-head rule

Required checks must pass on the exact commit intended for merge.

Because CI and CodeQL use `cancel-in-progress: true`, rapid commits can cancel obsolete runs. A green older SHA does not validate a newer documentation/code head.

Do not merge while the current candidate's required jobs are queued, in progress, cancelled without replacement, or failed.

## Validation history

The v1.0.0 build audit established the Kotlin/Android/Desktop/Rust/CodeQL baseline. Version 1.1.0 extends that gate with formatting, synchronized-version, Android lint, persistence migration/backup, timeout, trend, localization/UI, private-room protocol regression coverage, and exhaustive tracked-file documentation coverage. The release workflow now independently repeats the formatting, documentation-coverage, and version source gates before release packaging.

`what_changed.md` is the handoff source for the exact current validation result and most recent meaningful commits.
