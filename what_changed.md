# What Changed

This file is the authoritative implementation handoff for the current RPS Arena audit. It intentionally contains the detailed repository progress, validation evidence, limitations, and next release gates that would otherwise be repeated in chat.

## Current checkpoint

- Date: 2026-08-19
- Repository: `sanskarIN/rps-arena`
- Working branch: `chatgpt/final-audit-20260819`
- Pull request: `#10` — `fix: complete final quality and compatibility audit`
- Pull-request base: `main`
- Base SHA at this checkpoint: `6b07e6d2c85b4d7867154509138a6b8e734ac2ad`
- PR state immediately before this ledger refresh: open, draft, mergeable
- Feature/documentation head immediately before this ledger refresh: `62f17bc6cb0cf59633c631258b79cb84c4ffda1c`
- PR commit count immediately before this ledger refresh: 142
- Release status: release-candidate implementation is feature-complete enough for automated/manual release gates, but it must not be tagged or represented as a verified `v1.0.0` release until the exact latest commit passes the required workflows and the manual product/accessibility checks are completed on real builds.

## Source prompt scope preserved

The implementation continues to follow the uploaded RPS Arena master prompt and the user-requested repository workflow:

- Kotlin + Compose Multiplatform as the primary application stack;
- Android plus Windows/macOS/Linux desktop support;
- optional Rust only where it provides clear rules-engine/testing value;
- classic Rock–Paper–Scissors and optional Lizard–Spock;
- player-vs-CPU and same-device private two-player play;
- Easy, Normal, and Expert CPU behavior;
- Best-of-3, Best-of-5, Endless, Streak, and Tournament match modes;
- offline-first local persistence;
- local profiles, settings, history, statistics, trends, achievements, timers, and seeded challenges;
- accessibility and reduced-motion behavior;
- localization-ready UI copy boundary;
- structured privacy-conscious logging;
- versioned backup/restore and safer destructive actions;
- future private-room/LAN architecture without making networking a v1 dependency;
- CI, CodeQL, security automation, release automation, documentation, and repository governance;
- granular meaningful commits rather than one monolithic commit;
- detailed handoff information maintained here instead of consuming chat context.

## Phase 1 — Baseline and compatibility audit

Completed:

- Reconciled the final-audit branch with validated `main` work rather than overwriting newer fixes.
- Preserved the main branch corrections for Kotlin packages whose leading `in` segment must be escaped as `` `in` `` in Kotlin source/imports.
- Replaced the unavailable preview Android API 37 baseline with stable Android 16 / API 36 for reproducible hosted CI.
- Aligned Kotlin/AGP/Gradle versions to a compatible set rather than independently choosing the newest number from each ecosystem.
- Fixed the Rust formatting defect discovered by hosted validation.
- Decoupled CodeQL from Android SDK installation by using a no-build analysis path.
- Removed obsolete duplicate uppercase documentation guides after canonical lowercase replacements existed.
- Reconciled the branch with the validated main history through a merge instead of rewriting commit history.

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
- Rust crate: Rust 2024 edition

Historical references to API 37, AGP 9.3.0, or Gradle 9.5.1 are not the current production baseline.

## Phase 2 — Core gameplay and rules

Implemented and retained:

- Classic Rock–Paper–Scissors rules engine.
- Rock–Paper–Scissors–Lizard–Spock rules engine.
- `Gesture.availableFor(variant)` as the shared variant availability source.
- CPU opponent mode.
- Same-device two-player pass-and-play mode.
- Hidden first-player choice during local two-player handoff.
- Explicit `LocalTurnPhase` domain state instead of storing UI sentences in state.
- Easy/Normal/Expert CPU strategies.
- Seeded deterministic CPU behavior for reproducibility.
- Best-of-3 match completion.
- Best-of-5 match completion.
- Tournament first-to-5 behavior.
- Endless continuation.
- Streak continuation.
- Optional 5/10/15/30/60-second timers.
- Deterministic timeout gesture selection derived from the configured seed/current round/turn.
- Match reset behavior that recreates seeded CPU state.

### State-boundary ruleset hardening

A final static audit found that UI controls correctly hid Lizard/Spock in Classic mode, but the public `ArenaState.play(gesture)` method itself still accepted those values if invoked directly from another caller or future UI.

This was fixed at the state boundary:

- Classic mode now rejects gestures not returned by `Gesture.availableFor(GameVariant.CLASSIC)`.
- The rejection happens before CPU/local-two-player state mutation.
- The event is logged only with bounded technical enum metadata.
- A regression test verifies that calling `state.play(Gesture.LIZARD)` in Classic mode does not create a round, alter statistics/history, or create a pending local move.

Relevant commits:

- `2617c26db4b299316aff9d29bc372711cc646599` — `fix: reject gestures outside active ruleset`
- `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` — `test: reject extended gestures in classic state`

## Phase 3 — Match configuration and release metadata

Match configuration is persisted locally and includes:

- rules variant;
- opponent mode;
- CPU difficulty;
- match mode;
- deterministic seed;
- round timer seconds.

`ArenaState.updateConfig` sanitizes timer values to `0..60`, persists the normalized config, logs only non-sensitive enum/numeric metadata, and resets the current match so the new setup takes effect consistently.

### Single release-version source

The final build audit found Android and desktop independently hard-coded `1.0.0`. This was removed as a future release-drift risk.

`gradle/libs.versions.toml` now owns:

- `appVersion = "1.0.0"`
- `appVersionCode = "1"`

Android now reads:

- `versionName` from `appVersion`;
- `versionCode` from `appVersionCode`.

Desktop now reads:

- `packageVersion` from `appVersion`.

`docs/release.md` explicitly defines the version catalog as the authoritative source and tells maintainers not to reintroduce independent hard-coded target versions.

Relevant commits:

- `aa3d954c912e169403ff901636cd947f6de5e802` — `build: centralize application release version`
- `2bb2fd19fec23bf30d62e385433d9c9c52158b31` — `build: source Android release version from catalog`
- `dcd827b8569fb7777156a2d40536749ec30e3271` — `build: source desktop release version from catalog`
- `aa76452b289ff242bbc439c968467f13f3c3f058` — `docs: record ruleset guard and shared release version`
- `56281426cdc0b58d3d31b60c22d945b4f4741b94` — `docs: define single release version source`
- `62f17bc6cb0cf59633c631258b79cb84c4ffda1c` — `docs: record active-ruleset state regression`

## Phase 4 — Offline persistence and local profiles

Implemented:

- `KeyValueStore` abstraction.
- Android `SharedPreferences` adapter.
- Desktop Java `Preferences` adapter.
- Injected in-memory store support for deterministic common/UI tests.
- Persisted application settings.
- Persisted aggregate statistics.
- Persisted match configuration.
- Persisted bounded recent history.
- Persisted local profile list and active profile.
- Up to six local profiles.
- Internal profile IDs.
- Profile display-name normalization.
- 1–24 character display-name bound.
- Create profile.
- Rename profile.
- Activate profile.
- Delete profile.
- Refusal to delete the final remaining profile.
- Reset to the default `Player 1` profile.

Local profiles are device identities only. They are not accounts, authentication records, online handles, telemetry identities, or cloud profiles.

Aggregate v1 statistics intentionally remain device-wide rather than per-profile. A future per-profile-stat design must be an explicit schema/migration change rather than silently changing existing semantics.

## Phase 5 — History, statistics, trends, and achievements

Implemented:

- reactive recent history after play/import/clear/undo/reset;
- maximum 30 history rows;
- bounded/sanitized history writes;
- lifetime round count;
- wins;
- losses;
- draws;
- win rate;
- current streak;
- best streak;
- achievement unlock conditions;
- UI-owned achievement title/description text;
- recent W/L/D trend derivation from stored history instead of duplicate persisted trend state;
- recent decisive win rate;
- text legend for W/L/D;
- semantic trend descriptions such as `Recent result 1: Win`;
- non-interactive trend status surfaces rather than fake buttons.

A history/trend ambiguity was also fixed. New player-one wins use an explicit role prefix:

`Player 1 (<profile name>) won`

This prevents profile names such as `CPU` or `Player 2` from making the trend parser misclassify the result. Regression tests cover reserved-looking local profile names.

## Phase 6 — Data safety, backup, restore, and undo

### Current export format

`RPS_ARENA_BACKUP_V2`

V2 contains:

- settings;
- aggregate statistics;
- match configuration;
- local profile IDs/display names;
- active profile;
- recent history.

### Compatibility

- V1 remains supported for import.
- V1 imports migrate to the default local profile.
- V1 is import compatibility only; new exports use V2.
- Unknown backup headers are rejected.

### Defensive bounds/validation

Implemented:

- maximum backup input size: 32,768 characters;
- strict settings value decoding;
- non-negative/internally consistent statistics validation;
- match enum validation;
- timer range validation;
- profile count bound;
- profile ID format validation;
- profile name validation;
- active-profile membership validation;
- history count bound;
- history line-length bound;
- invalid history escape rejection;
- full decoded-section validation before import mutation.

A reliability issue discovered during the audit was fixed so history validation happens in the shared decode/staging phase before imported settings/stats/config/profile values are written. Malformed history can therefore no longer fail after other imported sections have already mutated local state.

### Backup preview

`previewBackup` uses the same decoder as import and is non-mutating. It exposes a safe summary including:

- backup format version;
- local profile names;
- active profile;
- statistics/config summary data;
- history-entry count.

Settings keeps import unavailable until the pasted backup validates.

### History undo

Recent-history clear retains a single in-memory restoration snapshot:

- clearing history exposes `Undo history clear`;
- undo restores the snapshot once;
- a newly written round invalidates the snapshot;
- successful backup import invalidates it;
- full reset invalidates it.

Full reset remains confirmation-gated because it intentionally removes multiple independent categories of local data.

### Additional strict-parser observation

The current backup decoder is already bounded and validates all required decoded sections, but its key/value collection currently tolerates unparseable extra lines and uses map semantics for duplicate keys. This is not currently known to permit mutation of invalid decoded required fields because the required values are still decoded/validated before import, but rejecting malformed/duplicate key rows would make the format stricter and less ambiguous. This remains a hardening item rather than being falsely described as completed in this checkpoint.

## Phase 7 — UI and UX

Implemented/retained shared Compose screens:

- onboarding;
- Home;
- Play;
- History;
- Stats;
- Achievements;
- Settings;
- About.

Current UI behavior includes:

- active profile on Home;
- active profile on the scoreboard;
- profile-management Settings section;
- match rules/opponent/difficulty/mode/timer controls;
- replayable seed input;
- deterministic timer countdown behavior;
- local two-player handoff text;
- gesture buttons;
- last-round result card;
- match restart/new match action;
- recent history and clear/undo controls;
- statistics and trend card;
- achievements;
- system/light/dark theme behavior;
- reduced-motion preference;
- backup generation/preview/import;
- full local reset confirmation;
- project/update explanation;
- clickable repository/funding/business/support links in About;
- `Made by the Sanskar` branding.

Desktop/narrow-layout work includes:

- primary content bounded to 960 dp on wider windows;
- fill-width behavior on narrower surfaces;
- horizontally scrollable dense chip rows so controls do not clip unnecessarily.

## Phase 8 — Accessibility and motion

Implemented:

- Material controls for standard focus/touch semantics;
- 88 dp gesture buttons;
- explicit gesture content descriptions;
- non-color-only textual round outcomes;
- textual timer state;
- textual local-player turn state;
- W/L/D text legend;
- full Win/Loss/Draw semantic trend descriptions;
- active-profile text labels;
- text-labeled copy-result action;
- copied-result success state;
- persisted reduced-motion setting;
- `AnimatedContent` result transition only when reduced motion is disabled;
- direct static result rendering when reduced motion is enabled;
- confirmation for full destructive reset;
- undo for recent-history clear.

Accessibility documentation contains keyboard, screen-reader, scaling, contrast/status, motion, and manual release checklists.

## Phase 9 — Local completed-round Copy result

Implemented a local copy/share preparation flow without adding a network SDK or Android internet permission.

Behavior:

- each completed-round card exposes `Copy result`;
- copied text includes the RPS Arena name, both gesture labels, and the displayed outcome;
- clipboard write happens only after explicit user action;
- the application shows `Result copied for sharing.` after the write;
- RPS Arena does not read the existing clipboard;
- RPS Arena does not upload copied result text;
- privacy/accessibility/testing documentation records the clipboard boundary.

## Phase 10 — Localization-ready copy boundary

Implemented `ui/Strings.kt` for the current English product copy, including:

- application/navigation text;
- onboarding;
- profile management;
- match controls;
- timer/seed text;
- history;
- statistics/trends;
- achievements;
- local-turn text;
- backup/restore/undo/reset;
- copy-result text;
- About/support/funding.

Known localization debt:

- `Gesture.label` remains an English string in the domain enum.

The project therefore describes itself as localization-ready, not already multilingual.

## Phase 11 — Structured local logging

Implemented `SafeLogger` with:

- structured log level/event model;
- bounded event naming;
- no-op default sink;
- sensitive field-name redaction;
- value-length truncation;
- tests for redaction, truncation, safe fields, and invalid event names.

Sensitive-key patterns include password/passwd/secret/token/authorization/cookie/email/backup/content/payload.

Intentional logging rules:

- no backup contents;
- no local profile display names;
- no history text;
- no credentials/secrets/tokens;
- bounded technical metadata only for normal state events.

The ruleset rejection added in this continuation logs only gesture/variant enum metadata.

## Phase 12 — Optional private-room/LAN architecture

Implemented a pure shared architecture boundary, not a production network feature:

- `PrivateRoomTransport` suspend interface;
- Hello/Ready/Move/Leave commands;
- protocol version validation;
- constrained six-character room codes;
- sender ID bounds;
- message ID bounds;
- round-number bounds;
- variant-compatible gesture validation;
- in-memory transport contract/testing support;
- ADR threat-model guidance.

Important v1 boundary:

- no production network transport;
- no Android internet permission;
- no automatic LAN discovery;
- no mandatory backend;
- CPU play remains local;
- pass-and-play remains local;
- peer-provided results are not authoritative over the local rules engine.

Any production private-room transport remains a later milestone requiring malformed-input, replay, disconnect, concurrency, resource-bound, and transport-security testing.

## Phase 13 — Optional Rust engine

Retained a standalone optional Rust rules mirror with:

- Rust 2024 edition crate;
- deterministic rules implementation;
- unit tests;
- `cargo fmt --check`;
- Clippy with warnings denied;
- test execution across targets/features;
- benchmark support;
- Kotlin/Rust rule-contract fixtures/checks.

Rust is intentionally optional and not the application runtime source of truth until a future integration proves measurable value and equal supported-target coverage.

## Phase 14 — Compose primary-journey regression coverage

Implemented real shared Compose UI regression coverage rather than leaving UI tests as a future-only item.

Changes:

- added `org.jetbrains.compose.ui:ui-test` at the same Compose version through the version catalog;
- configured `commonTest` with Compose UI testing;
- configured `desktopTest` with `compose.desktop.currentOs` so the shared test has a desktop runtime;
- added stable `UiTags.kt` semantic tags;
- tagged onboarding entry, Home Play, gesture controls, and last-round result;
- added `RpsArenaUiTest` with an isolated in-memory repository;
- test journey: first render → onboarding → Home → Play → Rock → first rendered round result.

The `androidx.compose.ui.test.v2.runComposeUiTest` API/import was checked against JetBrains Compose Multiplatform source before freezing this test wiring.

## Phase 15 — CI and automation

### Main CI workflow

`.github/workflows/ci.yml` validates:

- shared desktop Kotlin compilation;
- all shared tests, including the shared Compose UI test through desktop runtime;
- Android debug assembly;
- Android lint;
- desktop application classes;
- Rust formatting;
- Rust Clippy with warnings denied;
- Rust tests.

The workflow uses stable Android SDK 36 and the Gradle 9.5.0 validation baseline.

### CodeQL

`.github/workflows/codeql.yml` performs Java/Kotlin analysis without depending on the Android SDK build path.

### Documentation

`.github/workflows/docs.yml` runs `python scripts/check_docs_links.py`.

### Security

`.github/workflows/security.yml` runs:

- the repository high-confidence committed-secret scanner;
- GitHub dependency review for pull requests.

Dependabot tracks:

- Gradle;
- Cargo;
- GitHub Actions.

### Release

`.github/workflows/release.yml` supports manual/tagged artifact builds for:

- unsigned Android release APK;
- Linux desktop distributable;
- Windows desktop distributable;
- macOS desktop distributable.

Signing material remains outside Git.

## Phase 16 — Local verification scripts

Both local verification entry points were upgraded so they no longer run a reduced subset compared with CI.

Current verification scripts cover the appropriate local equivalents of:

- shared Kotlin compilation/tests;
- Android debug assembly;
- Android lint;
- desktop classes;
- documentation-link validation;
- committed-secret scan;
- Rust format/Clippy/tests when Cargo is installed.

Scripts:

- `scripts/verify.sh`
- `scripts/verify.ps1`

Relevant commits include:

- `5a95dad87d9933a31a1a0885db5d97ccbf30c90c` — Unix verifier expansion
- `17eacfe0dd8d7187201120f2cea213b43f060ab6` — PowerShell verifier expansion
- `69701b64f462aaf40b944a2392447b4615857ad2` — validation-contract documentation alignment

## Phase 17 — Security/repository governance

Repository-side hardening now includes:

- `SECURITY.md` with private reporting expectations and no-secrets guidance;
- `SUPPORT.md` with support/business channels and privacy-safe troubleshooting guidance;
- `CONTRIBUTING.md` with toolchain/test/commit expectations;
- `CODE_OF_CONDUCT.md` community expectations;
- `.github/CODEOWNERS`;
- bug-report issue form;
- feature-request issue form;
- issue-template routing/config;
- pull-request template;
- Dependabot;
- funding configuration;
- repository-settings hardening guide;
- secret scanner;
- security workflow;
- CodeQL workflow;
- CI workflow;
- documentation workflow;
- release workflow.

The templates request platform/reproduction/testing information and call attention to accessibility, privacy/security, persistence/migration, and documentation impact where relevant.

Recent governance/template commits from this audit include:

- `5612dee5be1c36349530e616da4dd4d10f7c1103`
- `662dbf42d4d942ca22ff6eafa6092e5f33a2723c`
- `3ff784e8b10ab6ba61bbe33fb8611a010b422f1e`
- `a17e4a141fe43697ccd21b5f376456bb2893673c`
- `a1c07d8f6c49c7f5331b3acf434f466239873ee1`
- `0da1a665e996fa536b8f6c819e2881ff6edf562d`
- `607c224434b8f8ebf7c26ea31d08a81eaa9aeba3`
- `e1d3f5b7766263373273166821648ba25afcbcf0`
- `16e6b15e352f1ffee390137adc4165afcdb899c3`

## Phase 18 — Documentation audit

Canonical documentation includes:

- `README.md`
- `CHANGELOG.md`
- `ROADMAP.md`
- `PRIVACY.md`
- `SECURITY.md`
- `SUPPORT.md`
- `CONTRIBUTING.md`
- `CODE_OF_CONDUCT.md`
- `LICENSE`
- `docs/setup.md`
- `docs/development.md`
- `docs/testing.md`
- `docs/architecture.md`
- `docs/accessibility.md`
- `docs/performance.md`
- `docs/release.md`
- `docs/troubleshooting.md`
- `docs/repository-settings.md`
- `docs/VALIDATION.md`
- ADRs under `docs/adr/`
- this `what_changed.md` handoff.

Obsolete uppercase duplicates were removed after lowercase canonical replacements were introduced so contributors do not encounter contradictory guides.

README/documentation now describe only behavior that actually exists in the branch. In particular:

- screenshots are not fabricated;
- iOS is not claimed as supported;
- production LAN rooms are not claimed as implemented;
- Rust is not claimed as the production app engine;
- Android release artifacts are not claimed as signed store packages;
- final release readiness is not claimed before exact-head workflow/manual evidence.

## Phase 19 — Privacy boundaries

Current v1 privacy properties:

- no account required;
- no cloud sync required;
- no analytics SDK;
- no advertising SDK;
- no production backend dependency;
- Android requests no internet permission;
- local profile display names remain device-local unless the user explicitly includes them in an exported backup;
- backups are human-readable and explicitly not encryption or secret storage;
- structured logging excludes profile names/backups/history content by design;
- Copy result writes only after explicit user activation;
- RPS Arena does not read the existing clipboard;
- About links open externally only after user activation.

## Automated regression coverage

Common/shared tests now cover, among other existing areas:

- Classic rules;
- Lizard–Spock rules;
- active-ruleset state-boundary rejection;
- deterministic CPU behavior;
- settings codec;
- legacy seven-field settings migration;
- statistics codec;
- corrupted statistics fallback;
- match-config codec/persistence;
- local profile lifecycle;
- profile-name normalization/rejection;
- profile count limits;
- profile persistence;
- V2 backup round trip with profiles;
- V1 backup migration;
- backup preview non-mutation;
- oversized backup rejection;
- malformed backup rejection;
- atomic invalid-history backup rejection;
- validated history replacement;
- CPU timeout round creation;
- local two-player timeout handoff;
- timer-disabled behavior;
- history clear/undo;
- undo invalidation after new history;
- full reset defaults;
- recent trend parsing/rate calculation;
- reserved-looking profile names in history trends;
- private-room validation/transport contract behavior;
- SafeLogger redaction/truncation/event validation;
- primary shared Compose onboarding-to-first-result UI journey;
- Kotlin/Rust rules-contract coverage.

## Source-controlled validation commands

Kotlin/platform:

```bash
gradle --no-daemon :shared:compileKotlinDesktop --stacktrace
gradle --no-daemon :shared:allTests --stacktrace
gradle --no-daemon :androidApp:assembleDebug --stacktrace
gradle --no-daemon :androidApp:lintDebug --stacktrace
gradle --no-daemon :desktopApp:classes --stacktrace
```

Rust:

```bash
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

Documentation/security:

```bash
python scripts/check_docs_links.py
python scripts/check_for_secrets.py
```

The project also exposes the expanded `scripts/verify.sh` and `scripts/verify.ps1` entry points for contributors.

## Hosted workflow failure/fix history

Observed historical failures during this audit were treated as defects rather than ignored:

1. Preview Android API 37 SDK installation was unavailable on the hosted environment.
   - Fixed by moving the production/CI baseline to stable API 36.
2. Rust `cargo fmt --check` failed.
   - Fixed by formatting the Rust rules mirror.
3. CodeQL was coupled to Android build/SDK availability.
   - Fixed by moving CodeQL to no-build Java/Kotlin analysis.
4. Kotlin packages using `in` as an unescaped identifier failed compilation.
   - Fixed throughout source/tests with `` `in` `` package/import escaping.
5. The final-audit branch diverged from newer validated main work.
   - Fixed by reconciling histories rather than discarding either side.

Older workflow runs can also appear as `cancelled` because branch concurrency intentionally cancels superseded commits. A cancelled superseded run is neither release success nor evidence of a defect in the latest commit.

## Exact automated status immediately before this ledger commit

The pre-ledger head was:

`62f17bc6cb0cf59633c631258b79cb84c4ffda1c`

GitHub created the following pull-request runs for that exact head, and they were still queued when this ledger refresh was prepared:

- CI — run `32219204131` — queued
- CodeQL — run `32219204170` — queued
- Documentation — run `32219204128` — queued
- Security checks — run `32219204163` — queued

No failure log existed for those exact-head runs at that checkpoint, and queued status is not treated as success.

This `what_changed.md` update itself creates a newer commit and therefore a new authoritative workflow set. Final validation must use the workflows attached to the ledger commit, not the pre-ledger run IDs above.

## Gradle wrapper audit

A release-engineering audit identified that the repository currently uses a documented globally installed Gradle 9.5.0 baseline instead of a committed standard Gradle wrapper.

Work completed:

- verified the official `gradle-wrapper.jar` from the Gradle `v9.5.0` tag in the upstream `gradle/gradle` repository;
- confirmed the official upstream wrapper binary rather than inventing a replacement.

Integration limitation encountered:

- the GitHub connector can read the upstream binary as base64 and can create blobs in the target repository, but Git object SHAs are repository-local for the relevant create-tree operation;
- directly referencing the upstream Gradle repository blob SHA from `sanskarIN/rps-arena` was rejected by GitHub as `not a valid blob` for the target repository;
- the execution container cannot reach GitHub through external DNS to download/re-upload the binary;
- committing only `gradlew`, `gradlew.bat`, and wrapper properties without the matching JAR would create a broken wrapper and was intentionally rejected.

Therefore the repository continues to document/use Gradle 9.5.0 as an installed prerequisite at this checkpoint. A complete wrapper should be added later only when all official wrapper components can be committed together and validated. Do not represent a partial wrapper as completed.

## Known limitations intentionally retained

- No iOS target in v1.
- No production LAN/private-room transport in v1; only a tested architecture/protocol boundary.
- Aggregate statistics are device-wide, not per profile.
- `Gesture.label` remains English domain data; full resource-backed localization is not complete.
- Compose UI automated coverage exists for the primary journey but is not yet exhaustive for every Settings/profile/backup/accessibility path.
- Real Android/desktop screenshots are not committed until captured from an actually verified build.
- Android release workflow produces an unsigned artifact; Play Store signing remains deliberately outside the public repository.
- A complete Gradle wrapper is not yet committed for the integration reason documented above.
- Backup required fields are validated before mutation, but duplicate/unparseable extra key rows can be rejected more strictly in a future hardening change.
- No cloud sync by design.

## Manual release gates still required

Before tagging `v1.0.0`:

1. Confirm the exact latest PR head is mergeable and based on the intended current `main`.
2. Require successful latest CI Kotlin job.
3. Require successful latest CI Rust job.
4. Require successful latest CodeQL run.
5. Require successful latest Documentation run.
6. Require successful latest committed-secret scan.
7. Require successful dependency review when GitHub supports it for this pull request.
8. Review Dependabot/security alerts where repository settings make them available.
9. Run the manual product checklist in `docs/testing.md` on Android.
10. Run the manual product checklist in `docs/testing.md` on desktop.
11. Run/verify the accessibility checklist in `docs/accessibility.md`.
12. Verify every CPU difficulty on a real build.
13. Verify Classic and Lizard–Spock on a real build.
14. Verify local two-player hidden handoff on a real build.
15. Verify Best-of-3/Best-of-5/Tournament/Endless/Streak behavior.
16. Verify all timer presets and both local-player timeout phases.
17. Verify deterministic seed replay with identical player input.
18. Verify profile create/rename/select/delete and persistence after restart.
19. Verify V2 backup preview/export/import with multiple profiles.
20. Verify valid V1 backup migration.
21. Verify malformed/oversized backup rejection without partial data loss.
22. Verify history clear/undo and undo invalidation.
23. Verify Copy result clipboard content and success status.
24. Verify theme/reduced-motion behavior.
25. Verify external About links.
26. Capture real release-candidate Android screenshots.
27. Capture real release-candidate desktop screenshots.
28. Move PR #10 out of draft only after automated/manual evidence is acceptable.
29. Merge without squashing if preserving the intentionally granular history remains desired.
30. Tag only the verified `main` commit.
31. Run/verify the release workflow artifacts before publishing downloads.
32. Do not publish an unsigned Android artifact as a store-signed production package.

## Commit identity

Requested project-owner commit email:

`Sanskar <sanskarin@outlook.in>`

The GitHub git-commit metadata inspected during this continuation for commit `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` showed both author and committer as:

`Sanskar <sanskarin@outlook.in>`

## Granular continuation commit record

The branch intentionally keeps focused commits rather than squashing unrelated work. Important commits from the extended audit include:

- `80f4a887e20d699a01f47d820280b9f45f97c7a1` — local player profile models
- `e11045e99872128cf3b7122236b8dbed41ca07af` — persisted/backed-up local profiles
- `cc22fed3ce1842dcef450fa4124b7d332a93dad2` — profile lifecycle in state
- `adb0572275cad32e8c3bb7a214cd632a7527e9ef` — profile/migration tests
- `d4ec46b411bca10275edb1d3db756696ba7a9642` — profile management UI
- `e4d2963e047d4615b939abb277e6cfabd10f0a23` — active profile across UI
- `0fd9c62ebcd61a1a8a20db69d9384511de47022e` — recent trend derivation
- `6317825774d7055d6cc77543227f7daa73c506a5` — trend parsing tests
- `7de3e0973aa9af57e8ca896b25f66ef88d087e96` — accessible trend card
- `a228759a7b1af482673835f43470ba1b7502a5c4` — domain/UI copy separation
- `2197d59329f3eef8c58324f6ca1e3895e007d124` — local turn domain state
- `d2396e458711d769ffc10cc0540b4cd192111dd8` — turn/achievement UI copy
- `57268aadeefb7a85aaa76928e34acc018874ef47` — backup preview model
- `d051e6f175f77fe69fbc4aa03557a4170d9a23e6` — preview/history restoration
- `e2d87cec038b60bb0ec64c853ed8f5e0f4320624` — atomic history validation
- `a836081ef0c4a55102bc65173a28209837a8ee6e` — backup safety tests
- `4dd6c1bee71db8478291505a9573c8d5344d6f0f` — backup preview/history undo
- `20575e761054e813b69163c5737e1467f162b0ae` — state preview/undo tests
- `4b611b34fac9ba693361b23e755e6eaf208817ac` — validated backup/undo controls
- `cfe0c6ddffd514770dadf0c4078509cc00138ee5` — Settings data controls
- `c2f4176ad1e311f1e2e19471251539ae7eee44b1` — committed-secret scanner
- `a3f333d6a1a83317fe8387a9e170620923861448` — security workflow/dependency review
- `cdb863543a17ff9eef944c160a8ff78e8283a4b2` — repository settings guide
- `b6837d688b2fded5ed253acd3e2174d8e9da3254` — privacy documentation
- `bc5e3031e97cb0b6aefd068e425a2d875578d77e` — architecture documentation
- `d1a9cf1f13d9b1b955e33ff90ebec4af16e7f11a` — README local-first feature alignment
- `b8b1585ccc4803ec4816459ee137ed5742c46021` — release-candidate roadmap
- `a3166fb903f4429c007ce486264b9750a6d6c737` — stronger release gates
- `7eec5de5f0cbd8b44019ae41ce5ce002a2fd4198` — accessibility alignment
- `0dfff6138a1777201962b87a56af3b5357852507` — unambiguous profiled trend history work
- `68d5cb02c2ce8ea31b9364f89854ab149ec1f8be` — non-interactive trend status tokens
- `733c09acf34e8a538caf15a831dbc0162d1f5981` — Compose UI-test dependency catalog work
- `0de9f12b1eb4ce3bc07956499aa4d911d1484df7` — UI-test source-set/runtime wiring
- `02023a407aa981e4e9902624a27bd51902e8c515` — stable UI tags
- `0003a1df28932d880f27ae97c2d47e7b85f606c2` — primary Compose UI regression test
- `732247f4e9929ad1767f0a501c06143ffee819be` — copy-result UI copy
- `ea26d3a72b679b28034638401fe4695512ae6c2f` — completed-round Copy result action
- `7f6e5f045140f9370f7e831bd8d7898b4a4ef911` — README UI-test/copy-result alignment
- `712579a8c86c5ea9912402ddc8d3fdb60763fd91` — testing guide alignment
- `1aa6ac9a542fde4d8c25c71737209dbc5ed267fe` — changelog alignment
- `3fad5498edfde3d8c881e7a3ff0d9d9e68cc6e78` — clipboard privacy documentation
- `93b279fb81a87062695fc66a1cd331dc92af87ce` — accessibility/copy-result/UI-test documentation
- `beabd44d81ba3c4fdc47a52d1b35ddb4a431bcbf` — prior detailed handoff refresh
- `5a95dad87d9933a31a1a0885db5d97ccbf30c90c` — expanded Unix verification
- `17eacfe0dd8d7187201120f2cea213b43f060ab6` — expanded PowerShell verification
- `69701b64f462aaf40b944a2392447b4615857ad2` — verification documentation alignment
- `5612dee5be1c36349530e616da4dd4d10f7c1103` — repository community/support hardening series
- `662dbf42d4d942ca22ff6eafa6092e5f33a2723c` — repository community/support hardening series
- `3ff784e8b10ab6ba61bbe33fb8611a010b422f1e` — repository community/support hardening series
- `a17e4a141fe43697ccd21b5f376456bb2893673c` — community conduct hardening
- `a1c07d8f6c49c7f5331b3acf434f466239873ee1` — CODEOWNERS
- `0da1a665e996fa536b8f6c819e2881ff6edf562d` — issue routing/config
- `607c224434b8f8ebf7c26ea31d08a81eaa9aeba3` — bug report template hardening
- `e1d3f5b7766263373273166821648ba25afcbcf0` — feature request template hardening
- `16e6b15e352f1ffee390137adc4165afcdb899c3` — pull-request template hardening
- `2617c26db4b299316aff9d29bc372711cc646599` — active-ruleset state guard
- `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` — state guard regression test
- `aa3d954c912e169403ff901636cd947f6de5e802` — centralized release version metadata
- `2bb2fd19fec23bf30d62e385433d9c9c52158b31` — Android catalog version wiring
- `dcd827b8569fb7777156a2d40536749ec30e3271` — desktop catalog version wiring
- `aa76452b289ff242bbc439c968467f13f3c3f058` — changelog rules/version update
- `56281426cdc0b58d3d31b60c22d945b4f4741b94` — release version-source documentation
- `62f17bc6cb0cf59633c631258b79cb84c4ffda1c` — state-boundary testing documentation

## Next continuation procedure

Start from the exact latest PR head, not from a remembered SHA.

1. Read PR #10 metadata and exact head SHA.
2. Fetch CI/CodeQL/Documentation/Security runs for that exact SHA.
3. If a job failed, inspect its job steps/logs and make a focused fix plus regression coverage where practical.
4. If checks are still queued/pending, do not claim they passed and do not merge merely because GitHub reports the PR as mergeable.
5. Avoid adding speculative v1 features while waiting; only fix concrete defects or documentation contradictions.
6. Refresh this ledger with exact workflow evidence after any corrective commit.
7. When every automated release gate is successful, complete/record the manual Android and desktop product/accessibility checklist and capture real screenshots.
8. Only then move PR #10 from draft to ready and merge without squashing if preserving granular history is still desired.
9. Verify the resulting `main` commit and tagged/manual release workflow before publishing artifacts.
