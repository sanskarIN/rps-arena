# Next Version Plan — v2.5.9

RPS Arena remains on **v2.5.8** while the validated cross-platform release is finalized. This document prepares the next patch release without changing package/runtime metadata early.

## Current release boundary

The exact merged v2.5.8 source revision is:

```text
4136aff448e9489a3e8252ceea7c1e9e79d17c19
```

CI, focused Security checks, CodeQL, and cross-platform version/build-code validation are green on that revision. The remaining v2.5.8 release work is tag creation plus tagged artifact/checksum verification. Do not begin the v2.5.9 version bump until those release steps are complete.

## Version transition

The planned post-release metadata is:

- semantic version: `2.5.9`;
- Android `versionName`: `2.5.9`;
- Android `versionCode`: `20509`;
- desktop `packageVersion`: `2.5.9`;
- iOS `CFBundleShortVersionString`: `2.5.9`;
- iOS `CFBundleVersion`: `20509`;
- Xcode `MARKETING_VERSION`: `2.5.9`;
- Xcode `CURRENT_PROJECT_VERSION`: `20509`;
- shared `APP_VERSION`: `2.5.9`.

Do not apply these values until v2.5.8 is merged to `main`, all release gates are green on the merged revision, the `v2.5.8` tag is created, and release artifacts/checksums are verified.

## Entry criteria

Development for v2.5.9 should begin from the released v2.5.8 `main` state only after:

1. CI passes formatting, documentation, localization, secret, privacy, version, shared tests, desktop UI tests, Android instrumentation packaging, Android lint/build, desktop, Web, iOS, and Rust gates;
2. Security checks and CodeQL pass on the exact release revision;
3. v2.5.8 package metadata is consistent across Android, desktop, iOS/Xcode, and shared metadata;
4. tagged release packaging completes and SHA-256 checksums are available;
5. superseded branches/PRs are classified so no old implementation is accidentally reintroduced.

The first four source-validation conditions are already satisfied for the merged v2.5.8 revision; the tagged packaging/checksum condition remains the release boundary.

## Planned v2.5.9 scope

### Data-safety and recovery UX

- Add backup preview-before-import using the validated backup decoder so users can inspect schema/history count before committing an import.
- Add reversible history clearing with an explicit undo path and tests for persistence/state synchronization.
- Add a clear/reset-data confirmation flow with offline-safe semantics and explicit documentation.
- Keep the existing `RPSARENA_BACKUP|1` schema backward compatible unless a deliberately versioned schema migration is required.

### Local player experience

- Evaluate and port the useful multi-profile implementation from the superseded final-audit branch without replacing the newer `ArenaStore`, Compose resources, or backup codec architecture.
- Keep profile names bounded/sanitized and local-only.
- Add profile-specific tests before exposing profile switching in UI.

### Match controls

- Add a localized user-visible CPU seed control while preserving deterministic replay behavior.
- Restore configurable round timers/timeouts only after model/state/repository migration tests are in place.
- Ensure persisted match setup remains compatible when new configuration fields are added.

### Quality and accessibility

- Expand Android instrumentation coverage beyond onboarding/gameplay to settings, backup validation, and accessibility-relevant navigation.
- Expand desktop UI coverage for persistence/reset/history flows.
- Add focused tests for keyboard navigation, reduced-motion behavior, and semantic labels where supported by the test harness.
- Continue localization-key and placeholder parity checks for every supported catalog.

### Platform robustness

- Improve iOS and Web validation based on any v2.5.8 release findings without changing the offline-first product contract.
- Consider permission-minimal platform file import/export UX for backups only if it can remain explicit and local-first.
- Keep platform signing credentials out of the public repository and CI source configuration.

## Explicitly deferred

The following are not automatically part of v2.5.9:

- real LAN/private-room network transport;
- cloud accounts or cloud synchronization;
- analytics or advertising SDKs;
- Android Internet permission for primary gameplay;
- production Apple/Windows/macOS signing credentials;
- a large localization expansion without a reviewed translation/testing plan.

The transport-neutral private-room contract and deterministic in-memory adapter may continue to evolve, but a real network transport should remain an explicit opt-in feature with separate privacy/security review rather than being silently added to a patch release.

## Release discipline

Every v2.5.9 feature should land as a focused change with tests and documentation. The release branch should preserve meaningful commit history, avoid replacing validated current architecture with older parallel implementations, and require CI, Security checks, and CodeQL to be green on the exact final head before merge/tagging.
