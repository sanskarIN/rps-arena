## What changed

Describe the focused change and the user-visible or engineering reason for it.

## Validation

- [ ] `python3 scripts/check_format.py`
- [ ] `python3 scripts/check_docs_links.py`
- [ ] `python3 scripts/check_docs_coverage.py`
- [ ] `python3 scripts/check_for_secrets.py`
- [ ] `python3 scripts/check_android_privacy.py` when Android/privacy/storage policy is relevant (recommended for broad changes)
- [ ] `python3 scripts/check_version.py` when release/versioned UI files are affected
- [ ] `gradle :shared:allTests --stacktrace`
- [ ] `gradle :shared:desktopTest --stacktrace` when shared Compose UI is affected
- [ ] `gradle :androidApp:lintDebug --stacktrace` when Android/shared UI is affected
- [ ] `gradle :androidApp:assembleDebug --stacktrace` when Android/shared code is affected
- [ ] `gradle :desktopApp:classes --stacktrace` when desktop/shared code is affected
- [ ] `cargo test --manifest-path rust-engine/Cargo.toml --all-targets` when Rust is affected
- [ ] Manual accessibility checks completed when controls, animation, timers, focus, or copy changed
- [ ] Every new/renamed tracked file is documented in `docs/repository-file-reference.md`
- [ ] Documentation, `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md` updated when appropriate

## Data compatibility

- [ ] Existing settings/history/statistics continue to load, or a migration is included.
- [ ] Explicit backup format compatibility is preserved, or the schema version and migration guidance are updated.
- [ ] New persisted/imported values are bounded and validated.
- [ ] Android automatic backup remains disabled and SharedPreferences remain excluded from legacy/cloud/device-transfer policy unless a reviewed privacy-policy change intentionally replaces that contract.

## Safety / privacy / security

- [ ] No secrets, signing keys, tokens, certificates, or private credentials were committed.
- [ ] No analytics, ads, telemetry, mandatory cloud dependency, or Android network permission was introduced without explicit design/privacy review.
- [ ] Any logging/diagnostic change preserves sensitive-data redaction and does not silently introduce a remote sink.
- [ ] Optional networking remains behind the documented private-room transport boundary.
- [ ] Logs, examples, screenshots, and fixtures contain no private user data.
- [ ] Dependency changes have been reviewed for security/licensing/compatibility impact.

## Release impact

- [ ] Version numbers remain synchronized when this change is release-visible.
- [ ] Release notes/changelog describe any user-visible behavior, privacy/security change, migration, or known limitation.
- [ ] Required CI, Security checks, and CodeQL are green on the exact current PR head before merge.
