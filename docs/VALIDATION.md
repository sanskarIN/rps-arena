# Validation

RPS Arena uses repository CI as the repeatable validation source for every supported implementation layer.

Required checks:

- Shared Kotlin tests: rules, deterministic CPU behavior, persistence, backup/configuration, timers, and state transitions.
- Android debug assembly and lint against stable API 36.
- Desktop JVM compilation.
- Optional Rust formatting, Clippy, and unit tests.
- CodeQL analysis for Kotlin/Java code.
- Relative Markdown link validation.

Local commands are documented in `testing.md` and mirrored by `../scripts/verify.sh` and `../scripts/verify.ps1` where the host toolchain is available.

A release should only be cut from a commit for which required CI jobs pass. Any build-system correction discovered during validation must be committed separately and recorded in `../what_changed.md`.
