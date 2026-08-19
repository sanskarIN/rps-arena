# Validation

RPS Arena uses repository CI as the repeatable validation source for every supported implementation layer and keeps local verification entry points aligned with those release gates.

Required checks:

- Shared Kotlin tests: rules, deterministic CPU behavior, persistence codecs, local profiles, backup migration/safety, state transitions, recent trends, private-room protocol contracts, and the primary Compose UI journey.
- Android debug assembly against the configured compile/target SDK.
- Android lint for the debug variant.
- Desktop JVM compilation.
- Optional Rust engine formatting, Clippy with warnings denied, and unit tests.
- Repository-local Markdown link validation.
- High-confidence committed-secret scanning.
- CodeQL analysis for Kotlin/Java code.
- Pull-request dependency review when GitHub makes dependency-review data available for the repository.

Local commands are documented in `docs/testing.md` and grouped by `scripts/verify.sh` and `scripts/verify.ps1`. Those scripts run the Kotlin/platform checks, documentation-link validator, committed-secret scanner, and—when Cargo is installed—the complete optional Rust formatting/lint/test suite.

## Workflow trigger policy

CI, Documentation, Security checks, and CodeQL intentionally use:

- `pull_request` targeting `main` for proposed changes;
- `push` targeting `main` for post-merge validation.

Feature/PR branches are not also listed under `push`. This avoids running the same branch commit twice as separate push and pull-request executions with different concurrency keys. Workflow concurrency still cancels superseded runs inside the same PR/ref so the exact latest commit remains authoritative.

A release should only be cut from a commit for which the required hosted CI, documentation, security, and CodeQL jobs pass and the manual product/accessibility gates are complete. A local verifier success is useful development evidence but is not a substitute for hosted release-candidate evidence.

Any build-system, test, security, or migration correction discovered during validation must be committed separately and recorded in `what_changed.md`.
