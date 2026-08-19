# Validation

RPS Arena treats executable CI evidence as the release gate. A document saying that a check should pass is not a substitute for the check actually passing on the candidate commit.

## Required automated checks

The `CI` workflow requires:

- repository text formatting check;
- Android/desktop/About version consistency check;
- shared Kotlin test suite;
- Android lint;
- Android debug assembly;
- desktop JVM compilation;
- optional Rust engine tests.

The separate `CodeQL` workflow analyzes Kotlin/Java code.

## Release validation

The tag workflow repeats version/format verification, shared tests, Android release lint/build, desktop Linux packaging, and Rust package tests before a tagged release can publish public unsigned artifacts and checksums.

Signing credentials are intentionally outside the public repository and are not required to validate the open-source build.

## Local parity commands

```bash
python3 scripts/check_format.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

The shell and PowerShell helpers in [`scripts/`](../scripts/) mirror the same repository-level intent.

## Manual evidence

Before a stable tag, complete the journeys in [`testing.md`](testing.md) and the accessibility checks in [`accessibility.md`](accessibility.md). Record any unresolved blocker as an explicit known limitation instead of declaring a clean release.

## Validation history

The v1.0.0 build audit established the Kotlin/Android/Desktop/Rust/CodeQL baseline. Version 1.1.0 extends that gate with formatting, synchronized-version, Android lint, persistence migration/backup, timeout, trend, and private-room protocol regression coverage.

`what_changed.md` is the handoff source for the exact current validation result and most recent meaningful commits.
