# Validation

RPS Arena uses repository CI as the repeatable validation source for every supported implementation layer.

Required checks:

- Shared Kotlin tests: rules, deterministic CPU behavior, and persistence codecs.
- Android debug assembly against the configured compile/target SDK.
- Desktop JVM compilation.
- Optional Rust engine unit tests.
- CodeQL analysis for Kotlin/Java code.

Local commands are documented in `docs/TESTING.md` and mirrored by `scripts/verify.sh` and `scripts/verify.ps1`.

A release should only be cut from a commit for which the required CI jobs pass. Any build-system correction discovered during validation must be committed separately and recorded in `what_changed.md`.
