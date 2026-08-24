# RPS Arena Roadmap

## v1.0.0 — Validated baseline

- [x] Classic and Lizard–Spock rules.
- [x] CPU and same-device two-player gameplay.
- [x] Best-of-3, Best-of-5, Endless, Streak, and Tournament formats.
- [x] Offline settings, statistics, history, achievements, and onboarding.
- [x] Android and desktop entry points.
- [x] Optional Rust rules mirror.
- [x] CI, CodeQL, repository governance, privacy, security, and release documentation.

## v2.5.8 — Reconciled cross-platform release candidate

### Completed on the current reconciled branch

- [x] Preserve the granular phase-7 history while reconciling current validated `main` through a two-parent merge.
- [x] Keep a safety branch at `archive/phase-7-pre-main-sync-20260824` for the untouched pre-reconciliation implementation.
- [x] Versioned `RPSARENA_BACKUP|1` export/import with strict schema validation and bounded history.
- [x] Injectable `ArenaStore` persistence boundary with production `PlatformArenaStore` delegation.
- [x] Persist `match_config_v1` ruleset, opponent, difficulty, match mode, and CPU seed with corruption fallback.
- [x] Harden persisted statistics invariants and history sanitization/length limits.
- [x] Deterministic seeded CPU behavior and state replay regression tests.
- [x] Reject gestures unavailable in the active ruleset.
- [x] Compose Multiplatform English/Hindi resource catalogs with CI key/placeholder parity validation.
- [x] Stable localization-independent `ArenaUiTags`.
- [x] Desktop Compose UI tests for onboarding/navigation/gameplay/settings/backup behavior.
- [x] Android KMP instrumentation smoke tests and device-test APK assembly.
- [x] Reduced-motion preference and documented accessibility review expectations.
- [x] No-op-by-default privacy-safe structured logger with sensitive-field redaction.
- [x] Transport-neutral private-room contracts with a deterministic two-player no-network reference adapter.
- [x] Private-room sender/lifecycle/round/participant/room-code validation.
- [x] Android API 26+ source/build target.
- [x] JVM desktop support for Windows, Linux, and macOS.
- [x] iPhone/iPad Kotlin/Native device + Apple-silicon simulator frameworks and SwiftUI/Xcode host.
- [x] Web application with Kotlin/Wasm + Kotlin/JS compatibility distribution and browser localStorage.
- [x] CI gates for source formatting, docs links/coverage, secrets, Android privacy, versions, localization, shared tests, desktop UI, Android instrumentation packaging/lint/build, desktop, Web, iOS, and Rust.
- [x] Separate Security checks/dependency review and CodeQL workflows.
- [x] Tagged/manual release automation for public Android, Linux desktop, Web, iOS framework, and Rust artifacts with SHA-256 checksums.
- [x] Synchronize v2.5.8 metadata across Android, desktop, iOS/Xcode, and shared metadata at `2.5.8` / `20508`.
- [x] Correct the iOS simulator source-support boundary by excluding unsupported `x86_64` rather than claiming an unconfigured `iosX64` target.
- [x] Fix documentation-link validation so fenced/inline code examples are not misclassified as Markdown links.
- [x] Prepare a gated v2.5.9 plan without prematurely changing v2.5.8 runtime/package metadata.

### Remaining before v2.5.8 merge/tag

- [ ] Obtain green CI on the exact final PR head.
- [ ] Obtain green Security checks on the exact final PR head.
- [ ] Obtain green CodeQL on the exact final PR head.
- [ ] Fix any exact-head cross-platform failure without bypassing release gates.
- [ ] Consolidate the temporary reconciliation file reference into the canonical repository file reference when the file set is final.
- [ ] Audit README/changelog/platform docs for any remaining pre-reconciliation timer/profile/reset claims.
- [ ] Decide whether visible seed editing is required for v2.5.8 or belongs in v2.5.9; persisted deterministic seed support already exists.
- [ ] Keep round timers/timeouts out of v2.5.8 claims unless they are ported onto the current model/state/resource architecture with migration and regression tests.
- [ ] Keep player profiles/trends/reset flows out of v2.5.8 claims unless their newer-compatible implementations are ported and tested.
- [ ] Verify final package outputs, v2.5.8 metadata, and SHA-256 release checksums.
- [ ] Merge PR #11 with meaningful commit history preserved.
- [ ] Tag `v2.5.8`, verify the GitHub release workflow/artifacts, and confirm post-merge `main` remains green.

## v2.5.9 — Planned next patch

Detailed scope and entry criteria are maintained in [`docs/NEXT_VERSION.md`](docs/NEXT_VERSION.md). The planned eventual semantic/mobile values are `2.5.9` and `20509`, but they must not be applied until v2.5.8 is actually released.

Candidate work includes:

- [ ] Backup preview-before-import.
- [ ] Reversible history clearing.
- [ ] Explicit reset-data confirmation and state/persistence synchronization.
- [ ] Carefully ported local multi-profile support from superseded work without replacing newer storage/localization/backup architecture.
- [ ] User-visible localized CPU seed controls.
- [ ] Round timers/timeouts with persisted-config migration and deterministic state tests.
- [ ] Broader Android instrumentation and desktop UI automation.
- [ ] Keyboard/focus/reduced-motion/accessibility regression coverage where supported.
- [ ] Permission-minimal local backup file import/export if it remains explicit and offline-first.
- [ ] iOS/Web robustness improvements based on v2.5.8 release evidence.

## Optional/platform-dependent work

- [ ] Add a real LAN transport behind `PrivateRoomGateway` only through an explicit networking release/privacy/security review. Primary gameplay must remain fully offline.
- [ ] Add signed Android store, signed/notarized desktop, and signed App Store/TestFlight jobs only after authorized credentials exist outside Git.
- [ ] Evaluate additional platforms only when they have a meaningful product use case and can maintain the same privacy, accessibility, testing, and documentation bar.

## Roadmap rules

Roadmap items are not promises of a date. Security, privacy, accessibility, deterministic gameplay, data compatibility, documentation accuracy, and green exact-head validation take priority over feature count.

**Made by the Sanskar.**
