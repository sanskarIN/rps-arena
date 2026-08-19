# What Changed

This file is the authoritative implementation and validation handoff for the current RPS Arena production-quality audit. It intentionally contains the detailed repository progress, code decisions, regression work, validation state, known limitations, and release gates that would otherwise be repeated in chat.

## Current checkpoint

- Date: 2026-08-19
- Repository: `sanskarIN/rps-arena`
- Working branch: `chatgpt/final-audit-20260819`
- Pull request: `#10` — `fix: complete final quality and compatibility audit`
- Pull-request base: `main`
- Main/base SHA at the latest explicit branch check: `6b07e6d2c85b4d7867154509138a6b8e734ac2ad`
- PR state immediately before this ledger refresh: open, draft, mergeable
- Pre-ledger implementation/documentation head: `82f90e7577a4d5ee5f700c013c715e214fde9742`
- PR commit count at that pre-ledger checkpoint: 184
- Release status: release-candidate implementation is substantially complete, but this branch is **not** yet a verified `v1.0.0` release. Do not tag, publish, or move the PR out of draft solely because GitHub reports it as mergeable. The exact latest commit must pass hosted CI, CodeQL, Documentation, and Security checks, and the manual Android/desktop/accessibility release gates must be completed from real builds.

## Project contract preserved

The implementation continues to follow the uploaded RPS Arena master prompt and user-requested repository workflow:

- Kotlin + Compose Multiplatform as the primary application stack.
- Android as the primary mobile target.
- Windows, macOS, and Linux as supported desktop targets.
- iOS deferred rather than falsely advertised before native packaging/accessibility verification.
- Optional Rust used only where it provides clear independent rules/testing value.
- Classic Rock–Paper–Scissors and Rock–Paper–Scissors–Lizard–Spock.
- CPU and same-device private two-player play.
- Easy, Normal, and Expert CPU behavior.
- Best-of-3, Best-of-5, Endless, Streak, and Tournament modes.
- Offline-first persistence.
- Local profiles, settings, history, stats, trends, achievements, timers, and deterministic seeds.
- Accessibility and reduced-motion behavior.
- Localization-ready UI copy boundary.
- Structured privacy-conscious logging.
- Versioned backup/restore, migration, preview, and destructive-action safety.
- Optional future private-room/LAN architecture without making networking a v1 dependency.
- CI, CodeQL, security automation, release automation, community/governance files, documentation, support, and funding.
- Many focused meaningful commits instead of a monolithic squash during development.
- Detailed progress maintained here rather than repeatedly consuming chat context.

Project identity/contact information retained throughout public documentation:

- Branding: `Made by the Sanskar`.
- Business: `sanskarin@outlook.in`.
- Business: `sanskarin.business@gmail.com`.
- Support: `supportramsandesh@gmail.com`.
- GitHub profile: `https://github.com/sanskarIN`.
- Repository: `https://github.com/sanskarIN/rps-arena`.
- Buy Me a Coffee: `https://buymeacoffee.com/sanskarIN`.
- Requested commit email: `sanskarin@outlook.in`.

## Phase 1 — Baseline, compatibility, and branch reconciliation

Completed:

- Audited the existing repository rather than replacing it with a disconnected rewrite.
- Reconciled the final-audit branch with newer validated `main` history using a merge, preserving both histories.
- Preserved granular commits.
- Corrected Kotlin source/import packages whose leading `in` segment is a Kotlin keyword by using escaped `` `in` `` identifiers.
- Replaced unreliable preview Android API 37 with stable API 36 after hosted SDK installation failure.
- Aligned Kotlin/AGP/Gradle versions to an explicitly compatible set.
- Fixed the Rust formatting failure found by hosted validation.
- Moved CodeQL to no-build Java/Kotlin analysis so it does not depend on Android SDK availability.
- Removed obsolete duplicate uppercase documentation guides after canonical lowercase replacements were created.
- Kept screenshot claims honest: no mockup is presented as a verified application screenshot.

### Current source-controlled toolchain

- Kotlin: `2.4.10`
- Compose Multiplatform: `1.11.0`
- Android Gradle Plugin: `9.1.0`
- Gradle validation baseline: `9.5.0`
- AndroidX Activity Compose: `1.13.0`
- kotlinx.coroutines: `1.11.0`
- JDK: `17+`
- Android min SDK: `26`
- Android compile SDK: `36`
- Android target SDK: `36`
- Optional Rust crate: Rust 2024 edition

Any historical references to API 37, AGP 9.3.0, or Gradle 9.5.1 are superseded by the source-controlled values above.

## Phase 2 — Core rules and gameplay

Implemented/retained:

- canonical Kotlin `RulesEngine`;
- complete Classic RPS relationships;
- complete Lizard–Spock relationships;
- `Gesture.availableFor(variant)` as the shared gesture-availability source;
- player-vs-CPU;
- same-device two-player pass-and-play;
- hidden first-player choice for local two-player handoff;
- explicit `LocalTurnPhase` domain state;
- Easy CPU;
- Normal CPU;
- Expert CPU;
- deterministic seeded CPU behavior;
- Best-of-3;
- Best-of-5;
- Tournament first-to-5;
- Endless;
- Streak;
- 5/10/15/30/60-second timers;
- deterministic timeout moves;
- match reset that recreates deterministic CPU state.

### Active-ruleset state-boundary hardening

The UI already hid Lizard/Spock in Classic mode, but a direct caller could previously invoke `ArenaState.play(Gesture.LIZARD)` while Classic rules were active.

Fixed:

- `ArenaState.play` validates the supplied gesture against `Gesture.availableFor(config.variant)` before any mutation.
- Invalid variant/gesture combinations are rejected.
- No round is created.
- No stats are changed.
- No history is written.
- No pending local-player move is created.
- Logging contains only bounded gesture/variant enum metadata.

Regression coverage verifies this boundary.

Relevant commits:

- `2617c26db4b299316aff9d29bc372711cc646599` — `fix: reject gestures outside active ruleset`
- `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` — `test: reject extended gestures in classic state`

## Phase 3 — Match configuration and release-version authority

Persisted match configuration includes game variant, opponent mode, difficulty, match mode, deterministic seed, and timer seconds.

`ArenaState.updateConfig` normalizes the timer to `0..60`, persists the new configuration, logs only technical metadata, and resets the active match so configuration and match state cannot drift.

### Single release-version source

Android and desktop previously hard-coded `1.0.0` independently. This was replaced with one version catalog authority.

`gradle/libs.versions.toml` owns:

- `appVersion = "1.0.0"`;
- `appVersionCode = "1"`.

Android consumes `versionName` from `appVersion` and `versionCode` from `appVersionCode`. Desktop consumes `packageVersion` from `appVersion`.

`docs/release.md` documents the catalog as authoritative and explicitly says not to reintroduce independent hard-coded Android/Desktop versions.

Relevant commits:

- `aa3d954c912e169403ff901636cd947f6de5e802`
- `2bb2fd19fec23bf30d62e385433d9c9c52158b31`
- `dcd827b8569fb7777156a2d40536749ec30e3271`
- `aa76452b289ff242bbc439c968467f13f3c3f058`
- `56281426cdc0b58d3d31b60c22d945b4f4741b94`
- `62f17bc6cb0cf59633c631258b79cb84c4ffda1c`

## Phase 4 — Persistence architecture

Implemented:

- shared `KeyValueStore` abstraction;
- default shared adapter;
- Android `SharedPreferences` implementation;
- Desktop Java `Preferences` implementation;
- injectable in-memory stores for common/state/UI tests;
- persisted settings;
- persisted aggregate stats;
- persisted match config;
- persisted local profiles;
- persisted recent history.

Android storage uses the private `SharedPreferences` file `rps_arena`.

No account service, database server, cloud sync, or backend is required for v1 gameplay/progress.

## Phase 5 — Local player profiles and physical storage cleanup

Implemented:

- up to six local profiles;
- internal bounded profile IDs;
- 1–24-character display-name bound;
- whitespace normalization;
- newline/control-character rejection;
- active-profile selection;
- create;
- rename;
- activate;
- delete;
- cannot delete final profile;
- reset to default `Player 1`;
- persistence;
- V2 backup/restore;
- V1 migration to default profile.

Profiles are local labels only. They are not accounts, authentication identities, telemetry identities, emails, passwords, or cloud records.

Aggregate v1 stats remain device-wide by design. A future per-profile-stat model must use an explicit migration instead of silently changing the current data contract.

### Orphaned profile-name key bug

A persistence/privacy audit found that rewriting the active profile ID list did not physically remove the preference entry containing a discarded profile's display name. Normal decoding no longer showed that profile, but the old alias could remain stored.

This is fixed.

The storage contract now includes true key removal:

```kotlin
interface KeyValueStore {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
    fun remove(key: String)
}
```

Important final refinement: `remove` is a **required contract method**. An earlier intermediate version supplied a default implementation that wrote an empty string. That fallback was removed because a method named `remove` must not silently degrade into “store an empty value.” Every implementation/test double now provides real key deletion semantics.

Production implementations:

- Android uses `SharedPreferences.Editor.remove(key).apply()`.
- Desktop uses `Preferences.remove(key)`.
- `DefaultKeyValueStore` delegates to the platform implementation.

Repository cleanup behavior:

1. Read previously persisted valid profile IDs.
2. Normalize/validate the replacement profile set.
3. Compute IDs no longer present.
4. Remove each discarded `profile_name_v1:<id>` key.
5. Write current profile IDs/names and active profile.

This cleanup applies to:

- explicit profile deletion;
- full local-data reset;
- backup import replacing a larger local profile set with a smaller one.

Regression coverage inspects the underlying memory-store key map, not only decoded profile state, and verifies physical key removal for all three paths.

Relevant commits:

- `f07a14e1eb849f80079aa5659e3a073a4103aabd` — add removable storage contract
- `25629c99db627265712f3434c883cfa679c2515a` — Android key removal
- `514605b8fc322f8c9a901c27325db79ea08228ad` — desktop key removal
- `0e9d6396c71a88e7ea5cb3b493852195e4152ee8` — repository orphan-profile cleanup
- `e06e8a35a407b38347e9a07cbecfa5688a366220` — delete cleanup regression
- `fac04d9a99a70706590a9e6ff8ede8d422920beb` — reset cleanup regression
- `250522e5a25ce8393064e5ae40d1dd527c048859` — import cleanup regression
- `63b05d8a5891dbb32db767d2716ab7564f74465f` — changelog alignment
- `57e30b1310b00dea3b060a2d91d9792188588789` — privacy alignment
- `60413be055ce5af56737f46f1dc839b32d962e59` — testing alignment
- `97064f8045e0baaa0c7cb920299db7e2f16bf7a5` — require true removal contract
- `0852cd9a56dae7f83ef2dca51d20ea37f3653728` — state test-store true deletion
- `82f90e7577a4d5ee5f700c013c715e214fde9742` — UI test-store true deletion

## Phase 6 — History, statistics, trends, and achievements

Implemented:

- maximum 30 recent history rows;
- bounded/sanitized history writes;
- reactive history after play/import/clear/undo/reset;
- rounds played;
- wins;
- losses;
- draws;
- win rate;
- current streak;
- best streak;
- achievement unlock conditions;
- achievement display copy outside domain state;
- recent W/L/D trend derived from history rather than stored twice;
- recent decisive win rate;
- W/L/D legend;
- semantic full-result descriptions;
- non-interactive trend status surfaces.

New player-one win history uses `Player 1 (<profile name>) won`, preventing names such as `CPU` or `Player 2` from being mistaken for opponent outcomes by the trend parser. Regression tests cover reserved-looking names.

## Phase 7 — Backup/restore/migration/preview/atomicity

### Export format

Current exports use `RPS_ARENA_BACKUP_V2` and include settings, aggregate stats, match configuration, local profile IDs/names, active profile, and recent history.

### Compatibility

- V1 remains importable.
- V1 migrates to the default local profile.
- V1 is import-only compatibility.
- Unsupported headers are rejected.

### Defensive limits

Implemented:

- raw backup maximum: 32,768 characters;
- strict settings decoding;
- non-negative/internally consistent stats;
- rounds == wins + losses + draws;
- current streak <= best streak;
- enum validation;
- timer range validation;
- profile count bound;
- profile ID pattern validation;
- profile name validation;
- active-profile membership validation;
- history count bound;
- history line-length bound;
- control-character checks;
- escape decoding validation;
- complete required-section decoding before import mutation.

### Atomic history validation

History decoding/validation occurs before imported values are written. Invalid history cannot fail after other imported state has already been applied.

### Strict backup key parser

The old parser used `mapNotNull(...).toMap()`, allowing malformed rows to be ignored and duplicate keys to overwrite earlier values.

This is fixed. `parseBackupValues` requires valid bounded keys, requires `=`, rejects duplicates, and fails the shared preview/import decoder before state mutation.

Regression tests verify duplicate-key and no-separator rows are rejected without target mutation.

Relevant commits:

- `e79f49e21ca42cc866ed7f0a7587209d6218a008`
- `682d987f836a72d1984003f94603a793842728d3`
- `b9bbea27aaec3115eccbac8f7883cbc3068ddda5`
- `3ba9cfd3e02e189dbbc1816a87b77bb995f4f740`
- `cf40c9d6ae690b6b648012df9a0f5c25b63e2444`

The earlier limitation describing malformed/duplicate backup rows as future work is closed.

### Backup preview

`previewBackup` uses the same decoder as import and is non-mutating. It exposes only a safe summary: format version, profile names, active profile, stats/config summary, and history-entry count. Settings keeps import disabled until the pasted backup validates.

### History-clear undo

Recent-history clear retains one in-memory restoration snapshot. Clear can be undone once; a new round, successful import, or full reset invalidates the snapshot. Full reset remains confirmation-gated.

## Phase 8 — Android OS backup/privacy boundary

The original manifest used `android:allowBackup="true"`, conflicting with the intended explicit local-data portability model.

The Android backup boundary is now source-controlled:

- `android:allowBackup="false"`;
- `android:fullBackupContent="@xml/backup_rules"`;
- `android:dataExtractionRules="@xml/data_extraction_rules"`;
- no `android.permission.INTERNET` in v1.

Legacy `backup_rules.xml` excludes the entire `sharedpref` domain. Android 12+ `data_extraction_rules.xml` excludes shared preferences from both cloud backup and device transfer.

The explicit RPS Arena text export/import remains the user-controlled application portability path.

Relevant commits:

- `9c1b0a21f9009714893d61fab00539a36e54a83a`
- `8aa08fb773df2b3a97ba4abf2d338170453637df`
- `3d1c42286c8acd286b14e417f7677f914fb061d3`
- `c5c58f881323ebe8095d1cc6264947b7d7eed295`
- `d6188151e9f933ac58f67b66c33a403592e117b5`
- `49a24ee6a139151ced3cb36c1cc92db8b551cfc0`
- `b57e030aa1a74a9609f24c9c84afa79fae3e5781`

## Phase 9 — Android privacy regression validator

`scripts/check_android_privacy.py` enforces:

- application element exists;
- `allowBackup=false`;
- required manifest backup/extraction references;
- no INTERNET permission;
- valid legacy/current backup XML;
- shared-preference exclusion from legacy backup;
- shared-preference exclusion from cloud backup;
- shared-preference exclusion from device transfer.

It runs in hosted Security checks and both local verifier scripts.

Relevant commits:

- `95298c6ec7b4d86e3aa1def2dc0000db1114d74b`
- `d811344030e9bea39f4df5a76cb4d0b092c71bb6`
- `a01e7c599a91e31b0241b97df2a216c87ff1324f`
- `0dea92276caacd01795010ba85157ef0ca17c507`
- `b7144c45c7f871427393e4ebd7b110e4dd111194`
- `2b648cd832a68035dc9f74fd30e606e9da53f7fb`
- `c3a5eac292a3c1fb1baa0f0c9507a111f900bc2f`
- `9ffe7814e2ecbd8d2fcb25f76d6fbd97a4b9ec24`

## Phase 10 — UI and UX

Shared Compose screens:

- onboarding;
- Home;
- Play;
- History;
- Stats;
- Achievements;
- Settings;
- About.

Current behavior includes active profile display, profile management, match configuration, seed/timer controls, local two-player handoff, gesture controls, result rendering, match restart, history/undo, stats/trends, achievements, theme/reduced-motion settings, backup preview/import, confirmed full reset, project/update explanation, About links, funding/support/business contacts, and `Made by the Sanskar` branding.

Responsive work includes bounded wide-desktop content and horizontally scrollable dense controls on narrow layouts.

## Phase 11 — Local Copy result

Completed-round cards expose `Copy result`.

- copied text includes app name, gestures, and displayed outcome;
- write occurs only after explicit user action;
- success state is shown;
- app does not read existing clipboard data;
- copied text is not uploaded;
- no network SDK/permission was introduced.

## Phase 12 — Accessibility and reduced motion

Implemented Material focus/touch semantics, large gesture targets, explicit gesture descriptions, textual outcomes/timers/turn state, non-color-only trends, active-profile text, text-labeled copy action, copy-success status, persisted reduced motion, static result behavior under reduced motion, destructive reset confirmation, and history undo.

`docs/accessibility.md` contains manual keyboard/screen-reader/scaling/status/motion checks.

## Phase 13 — Localization-ready copy boundary

`ui/Strings.kt` owns current English UI copy across navigation, onboarding, profiles, match setup, turns, timers, seed, history, stats/trends, achievements, backup/undo/reset, Copy result, About, support, and funding.

Known localization debt: `Gesture.label` remains English domain data. The project therefore says localization-ready, not multilingual.

## Phase 14 — Structured local logging

`SafeLogger` provides structured log levels/events, event-name validation, no-op default sink, sensitive-key redaction, bounded metadata, and tests.

No intentional logging of backup contents, local profile names, history text, credentials, tokens, or secrets.

## Phase 15 — Optional private-room/LAN architecture

Implemented as architecture/testing boundary only:

- `PrivateRoomTransport`;
- Hello/Ready/Move/Leave commands;
- version/room/sender/message/round validation;
- variant-compatible moves;
- in-memory contract support/tests;
- ADR threat model.

v1 has no production transport, automatic discovery, backend dependency, or Android Internet permission.

## Phase 16 — Optional Rust engine

Standalone Rust 2024 rules mirror retained with deterministic rules, unit tests, formatting gate, Clippy warnings denied, full test gate, benchmark support, and Kotlin/Rust contract fixtures. Kotlin remains application runtime authority.

## Phase 17 — Compose UI regression coverage

Implemented Compose UI test dependency/runtime, stable semantic tags, isolated in-memory repository, and `RpsArenaUiTest` primary journey:

1. first-run onboarding rendered;
2. onboarding completed;
3. Home reached;
4. Play opened;
5. Rock selected;
6. completed-round result rendered.

The `runComposeUiTest` v2 API/import was checked against JetBrains Compose Multiplatform source before freezing the test wiring.

## Phase 18 — CI/workflow architecture and de-duplication

Validation workflows:

- CI;
- CodeQL;
- Documentation;
- Security checks;
- Release.

CI covers shared compilation/tests, Android assemble/lint, desktop compile, and Rust format/Clippy/tests. CodeQL uses Java/Kotlin no-build analysis. Documentation checks local Markdown links. Security runs secret scanning, Android privacy validation, and PR dependency review.

### Duplicate Actions fix

The PR branch was previously matched by both feature-branch `push` and `pull_request`, creating duplicate executions with different refs.

Current policy:

- `pull_request` targeting `main` for proposed changes;
- `push` targeting `main` for post-merge validation;
- scheduled CodeQL retained;
- feature branches are not duplicated under `push`.

Relevant commits:

- `afa5b7406cf8988e2eb0200f7ee012156a4fb749`
- `55acfc22eb5b88133b2951f2fcb7a879989211e2`
- `c772f6acbadad98bae9668d70602862d2b884890`
- `62098425881b28a5f6d292a816568c9a0980b69d`
- `e41a58e9512dd631ad70797512d9e177419728fa`
- `7eab5e07e0fc21577eac39a7318cf3dc8c3c8e06`

## Phase 19 — Local verification scripts

`scripts/verify.sh` and `scripts/verify.ps1` run local equivalents of shared compile/tests, Android debug assemble/lint, desktop classes, docs links, Android privacy contract, secret scan, and optional Rust format/Clippy/tests.

Relevant commits include `5a95dad87d9933a31a1a0885db5d97ccbf30c90c`, `17eacfe0dd8d7187201120f2cea213b43f060ab6`, `69701b64f462aaf40b944a2392447b4615857ad2`, `a01e7c599a91e31b0241b97df2a216c87ff1324f`, and `0dea92276caacd01795010ba85157ef0ca17c507`.

## Phase 20 — Security/community/governance

Repository includes MIT License, contributing guide, code of conduct, security policy, support policy, privacy policy, CODEOWNERS, bug/feature issue forms, issue routing config, PR template, Dependabot, FUNDING config, repository-settings guide, and CI/CodeQL/docs/security/release workflows.

Templates request useful platform/reproduction/testing details and prompt contributors to consider privacy/security/accessibility/persistence/docs impact.

## Phase 21 — Documentation audit

Canonical documentation includes README, changelog, roadmap, privacy/security/support/contributing/conduct/license files, setup/development/testing/architecture/accessibility/performance/release/troubleshooting/repository-settings/validation docs, ADRs, and this handoff.

Documentation does not claim unsupported iOS, production LAN rooms, production Rust runtime integration, fabricated screenshots, signed Android store artifacts, enabled branch protection, or workflow success that has not occurred.

## Phase 22 — Privacy model

Current v1 privacy properties:

- no account;
- no cloud sync;
- no analytics SDK;
- no ads SDK;
- no production backend;
- no Android Internet permission;
- Android application backup disabled;
- shared preferences excluded from configured Android backup/transfer paths;
- profile aliases physically removed from production preference stores when their IDs are discarded;
- user-controlled readable backup export;
- shared strict backup preview/import decoder;
- sensitive/free-form user data excluded from structured logs;
- clipboard write only after explicit user action;
- no clipboard read;
- external About links only after user action.

## Automated regression coverage inventory

Shared/common tests cover:

- Classic rules;
- Lizard–Spock rules;
- active-ruleset state guard;
- deterministic CPU behavior;
- settings codec;
- legacy settings migration;
- stats codec/fallback;
- match config codec/persistence;
- profile lifecycle/validation/counts/persistence;
- profile-name key removal on delete;
- profile-name key removal on reset;
- profile-name key removal on profile-replacing import;
- V2 backup round-trip;
- V1 migration;
- backup preview non-mutation;
- oversized backup rejection;
- invalid history rejection;
- atomic invalid-history rejection;
- duplicate backup-key rejection;
- malformed backup-row rejection;
- history replacement;
- CPU timeout;
- local two-player timeout handoff;
- disabled timer;
- history clear/undo/invalidation;
- full reset;
- trend parsing/rate calculation;
- reserved-looking profile-name trend behavior;
- private-room validation/transport contract;
- SafeLogger behavior;
- Kotlin/Rust contract fixtures;
- primary Compose UI onboarding-to-result journey.

Repository validation additionally covers Android privacy XML/manifest invariants, docs links, secret patterns, Android build/lint, desktop compilation, CodeQL, dependency review, and Rust checks.

## Source-controlled verification commands

Kotlin/platform:

```bash
gradle --no-daemon :shared:compileKotlinDesktop --stacktrace
gradle --no-daemon :shared:allTests --stacktrace
gradle --no-daemon :androidApp:assembleDebug --stacktrace
gradle --no-daemon :androidApp:lintDebug --stacktrace
gradle --no-daemon :desktopApp:classes --stacktrace
```

Documentation/privacy/security:

```bash
python scripts/check_docs_links.py
python scripts/check_android_privacy.py
python scripts/check_for_secrets.py
```

Rust:

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

Aggregate local entry points: `scripts/verify.sh` and `scripts/verify.ps1`.

## Hosted validation defect/fix history

Observed failures/defects were not hidden:

1. Preview Android API 37 unavailable — moved to stable API 36.
2. Rust formatting failure — formatted source; gate retained.
3. CodeQL coupled to Android build — switched to no-build analysis.
4. Kotlin `in` package keyword compile failures — escaped identifiers throughout source/tests.
5. Audit branch/main divergence — reconciled by merge.
6. Duplicate PR branch workflow executions — feature-branch push triggers removed.
7. State API accepted extended gesture in Classic mode — state guard plus regression.
8. Android OS backup privacy mismatch — manifest/rules/validator.
9. Backup duplicate/malformed key ambiguity — strict parser plus regressions.
10. Discarded profile aliases could remain as orphan preference keys — native removal API, cleanup logic, and delete/reset/import regressions.
11. Intermediate `KeyValueStore.remove` fallback could write an empty value instead of guaranteeing deletion — removed; true deletion is now mandatory for every store implementation.

Superseded workflow runs can appear cancelled because concurrency cancels earlier commits. Older cancelled runs are not release evidence either way.

## Exact automated status immediately before this ledger refresh

Pre-ledger head:

`82f90e7577a4d5ee5f700c013c715e214fde9742`

GitHub created these PR workflows for that exact head:

- Security checks — run `32220926398` — pending
- CodeQL — run `32220926364` — pending
- CI — run `32220926420` — queued
- Documentation — run `32220926372` — queued

No failure log existed for those exact-head runs at the checkpoint. Queued/pending is not success.

This handoff commit creates a newer head. The workflows attached to that new exact SHA become the authoritative validation set. Always fetch PR #10 again before release/merge decisions.

## Gradle wrapper audit and integration limitation

The repository currently requires installed Gradle 9.5.0 instead of a committed standard Gradle wrapper.

Audit work:

- confirmed no partial wrapper exists;
- verified the official Gradle 9.5.0 wrapper JAR from upstream tagged source;
- attempted safe connector-based transfer.

Limitation:

- foreign repository Git blob SHAs cannot be directly referenced in the target repository tree;
- GitHub rejected the upstream blob SHA for this repository;
- the execution container cannot resolve GitHub externally to download/re-upload the binary;
- committing wrapper scripts/properties without the matching official JAR would create a broken wrapper.

Therefore no fake/partial wrapper was committed. Installed Gradle 9.5.0 remains a documented prerequisite until all official wrapper components can be committed together and tested.

## Repository settings outside source-file control

Latest branch inspection showed `main` is currently unprotected. The connected GitHub action set does not expose branch-protection/ruleset mutation, so the project documents recommended rules in `docs/repository-settings.md` instead of pretending source files enabled them.

Recommended settings include branch/ruleset protection, exact required checks after stable names are observed, GitHub-native secret scanning/push protection when available, dependency alerts, and private vulnerability reporting.

## Known limitations intentionally retained

- No iOS target in v1.
- No production LAN/private-room transport in v1.
- Aggregate statistics are device-wide rather than per profile.
- `Gesture.label` remains English domain data; full resource-backed localization is not complete.
- Compose UI automated coverage covers the primary journey but not every Settings/profile/backup/accessibility interaction.
- Real Android/desktop screenshots are intentionally absent until captured from verified builds.
- Android release automation produces an unsigned artifact; Play Store signing remains outside the repository.
- Complete Gradle wrapper is not committed because the available integration cannot safely transfer the official wrapper JAR; Gradle 9.5.0 is documented as a prerequisite.
- No cloud sync by design.

Closed limitations that must not be reintroduced as open items:

- malformed/duplicate backup key rows are rejected;
- discarded profile-name keys are physically removed by production stores;
- `KeyValueStore.remove` no longer has an empty-string fallback.

## Manual release gates still required

Before `v1.0.0`:

1. Fetch PR #10 exact latest head.
2. Confirm `main` has not moved unexpectedly; reconcile if needed.
3. Confirm PR mergeability.
4. Require exact-head CI Kotlin success.
5. Require exact-head CI Rust success.
6. Require exact-head CodeQL success.
7. Require exact-head Documentation success.
8. Require exact-head committed-secret scan success.
9. Require exact-head Android privacy-contract validation success.
10. Require dependency-review success when available for the PR.
11. Review Dependabot/security alerts where repository settings expose them.
12. Verify first-run Android onboarding.
13. Verify Classic CPU at Easy/Normal/Expert.
14. Verify Lizard–Spock CPU.
15. Verify local two-player hidden first move.
16. Verify Best-of-3.
17. Verify Best-of-5.
18. Verify Tournament first-to-5.
19. Verify Endless continuation.
20. Verify Streak continuation.
21. Verify deterministic seed replay.
22. Verify all timer presets.
23. Verify CPU timeout move.
24. Verify both local-player timeout phases.
25. Verify settings/config persistence after restart.
26. Verify profile create/rename/select/delete.
27. Verify discarded profile aliases are absent after storage reload.
28. Verify full reset removes extra profile aliases and returns defaults.
29. Verify profile-replacing backup import leaves no discarded alias key.
30. Verify V2 backup preview.
31. Verify V2 export/import with multiple profiles/stats/config/history.
32. Verify valid V1 migration.
33. Verify malformed-history backup rejection without mutation.
34. Verify duplicate-key backup rejection without mutation.
35. Verify malformed-row backup rejection without mutation.
36. Verify oversized backup rejection without mutation.
37. Verify history clear/undo.
38. Verify undo invalidation after new history.
39. Verify recent trend display.
40. Verify Copy result clipboard content and success state.
41. Verify confirmed full local reset.
42. Verify light theme.
43. Verify dark theme.
44. Verify system theme.
45. Verify reduced-motion result behavior.
46. Verify desktop keyboard navigation.
47. Verify gesture semantics with accessibility tooling where available.
48. Verify trend semantics do not depend on color.
49. Verify About repository/funding/business/support links.
50. Verify Android v1 manifest has no Internet permission.
51. Verify Android backup/privacy validator passes.
52. Capture real Android release-candidate screenshots.
53. Capture real desktop release-candidate screenshots.
54. Move PR #10 out of draft only after automated/manual evidence is acceptable.
55. Merge without squashing if preserving granular history remains desired.
56. Verify resulting `main` SHA.
57. Tag only the verified main commit.
58. Verify actual release workflow artifacts before publishing downloads.
59. Never represent an unsigned Android artifact as store-signed.

## Commit identity

Requested project-owner identity:

`Sanskar <sanskarin@outlook.in>`

Git commit metadata inspected during this audit for `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` showed both author and committer with that email.

## Major granular commit checkpoints

The branch intentionally contains many focused commits. Significant continuation checkpoints include:

### Profiles/trends/data safety

- `80f4a887e20d699a01f47d820280b9f45f97c7a1`
- `e11045e99872128cf3b7122236b8dbed41ca07af`
- `cc22fed3ce1842dcef450fa4124b7d332a93dad2`
- `adb0572275cad32e8c3bb7a214cd632a7527e9ef`
- `d4ec46b411bca10275edb1d3db756696ba7a9642`
- `e4d2963e047d4615b939abb277e6cfabd10f0a23`
- `0fd9c62ebcd61a1a8a20db69d9384511de47022e`
- `6317825774d7055d6cc77543227f7daa73c506a5`
- `7de3e0973aa9af57e8ca896b25f66ef88d087e96`
- `0dfff6138a1777201962b87a56af3b5357852507`
- `68d5cb02c2ce8ea31b9364f89854ab149ec1f8be`
- `57268aadeefb7a85aaa76928e34acc018874ef47`
- `d051e6f175f77fe69fbc4aa03557a4170d9a23e6`
- `e2d87cec038b60bb0ec64c853ed8f5e0f4320624`
- `a836081ef0c4a55102bc65173a28209837a8ee6e`
- `4dd6c1bee71db8478291505a9573c8d5344d6f0f`
- `20575e761054e813b69163c5737e1467f162b0ae`
- `4b611b34fac9ba693361b23e755e6eaf208817ac`
- `cfe0c6ddffd514770dadf0c4078509cc00138ee5`

### UI/localization/tests/logging/security

- `a228759a7b1af482673835f43470ba1b7502a5c4`
- `2197d59329f3eef8c58324f6ca1e3895e007d124`
- `d2396e458711d769ffc10cc0540b4cd192111dd8`
- `c2f4176ad1e311f1e2e19471251539ae7eee44b1`
- `a3f333d6a1a83317fe8387a9e170620923861448`
- `733c09acf34e8a538caf15a831dbc0162d1f5981`
- `0de9f12b1eb4ce3bc07956499aa4d911d1484df7`
- `02023a407aa981e4e9902624a27bd51902e8c515`
- `0003a1df28932d880f27ae97c2d47e7b85f606c2`
- `732247f4e9929ad1767f0a501c06143ffee819be`
- `ea26d3a72b679b28034638401fe4695512ae6c2f`
- `3fad5498edfde3d8c881e7a3ff0d9d9e68cc6e78`
- `93b279fb81a87062695fc66a1cd331dc92af87ce`

### Governance/verification

- `cdb863543a17ff9eef944c160a8ff78e8283a4b2`
- `b6837d688b2fded5ed253acd3e2174d8e9da3254`
- `bc5e3031e97cb0b6aefd068e425a2d875578d77e`
- `d1a9cf1f13d9b1b955e33ff90ebec4af16e7f11a`
- `b8b1585ccc4803ec4816459ee137ed5742c46021`
- `a3166fb903f4429c007ce486264b9750a6d6c737`
- `7eec5de5f0cbd8b44019ae41ce5ce002a2fd4198`
- `5a95dad87d9933a31a1a0885db5d97ccbf30c90c`
- `17eacfe0dd8d7187201120f2cea213b43f060ab6`
- `69701b64f462aaf40b944a2392447b4615857ad2`
- `a1c07d8f6c49c7f5331b3acf434f466239873ee1`
- `0da1a665e996fa536b8f6c819e2881ff6edf562d`
- `607c224434b8f8ebf7c26ea31d08a81eaa9aeba3`
- `e1d3f5b7766263373273166821648ba25afcbcf0`
- `16e6b15e352f1ffee390137adc4165afcdb899c3`

### Final compatibility/privacy/workflow/parser/profile cleanup

- `2617c26db4b299316aff9d29bc372711cc646599`
- `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355`
- `aa3d954c912e169403ff901636cd947f6de5e802`
- `2bb2fd19fec23bf30d62e385433d9c9c52158b31`
- `dcd827b8569fb7777156a2d40536749ec30e3271`
- `afa5b7406cf8988e2eb0200f7ee012156a4fb749`
- `55acfc22eb5b88133b2951f2fcb7a879989211e2`
- `c772f6acbadad98bae9668d70602862d2b884890`
- `62098425881b28a5f6d292a816568c9a0980b69d`
- `9c1b0a21f9009714893d61fab00539a36e54a83a`
- `8aa08fb773df2b3a97ba4abf2d338170453637df`
- `3d1c42286c8acd286b14e417f7677f914fb061d3`
- `95298c6ec7b4d86e3aa1def2dc0000db1114d74b`
- `d811344030e9bea39f4df5a76cb4d0b092c71bb6`
- `a01e7c599a91e31b0241b97df2a216c87ff1324f`
- `0dea92276caacd01795010ba85157ef0ca17c507`
- `e79f49e21ca42cc866ed7f0a7587209d6218a008`
- `682d987f836a72d1984003f94603a793842728d3`
- `b9bbea27aaec3115eccbac8f7883cbc3068ddda5`
- `f07a14e1eb849f80079aa5659e3a073a4103aabd`
- `25629c99db627265712f3434c883cfa679c2515a`
- `514605b8fc322f8c9a901c27325db79ea08228ad`
- `0e9d6396c71a88e7ea5cb3b493852195e4152ee8`
- `e06e8a35a407b38347e9a07cbecfa5688a366220`
- `fac04d9a99a70706590a9e6ff8ede8d422920beb`
- `250522e5a25ce8393064e5ae40d1dd527c048859`
- `63b05d8a5891dbb32db767d2716ab7564f74465f`
- `57e30b1310b00dea3b060a2d91d9792188588789`
- `60413be055ce5af56737f46f1dc839b32d962e59`
- `97064f8045e0baaa0c7cb920299db7e2f16bf7a5`
- `0852cd9a56dae7f83ef2dca51d20ea37f3653728`
- `82f90e7577a4d5ee5f700c013c715e214fde9742`

## Continuation procedure

Do not begin the next continuation by inventing another v1 feature.

1. Fetch PR #10 metadata and exact latest head.
2. Fetch CI/CodeQL/Documentation/Security workflows for that exact SHA.
3. If a job fails, inspect job steps/logs and fix the observed defect with a focused commit plus regression coverage when practical.
4. If checks remain queued/pending, do not represent them as success.
5. Confirm `main` has not advanced before merging; reconcile if necessary.
6. Refresh this file after any corrective commit.
7. Complete manual Android/desktop product and accessibility gates only with actual build evidence.
8. Capture only real screenshots from verified builds.
9. Move PR #10 from draft only after acceptable automated and manual evidence.
10. Merge without squash if preserving granular history remains desired.
11. Verify resulting `main` and release workflow artifacts before tagging/publishing.
