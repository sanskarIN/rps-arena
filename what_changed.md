# What Changed

This file is the authoritative implementation and validation handoff for the current RPS Arena production-quality audit. It intentionally contains the detailed work log that would otherwise be repeated in chat. It should be read before continuing the branch so completed work is not duplicated and release gates are not skipped.

## Current checkpoint

- Date: 2026-08-19
- Repository: `sanskarIN/rps-arena`
- Working branch: `chatgpt/final-audit-20260819`
- Pull request: `#10` — `fix: complete final quality and compatibility audit`
- Pull-request base: `main`
- Current main/base SHA at the last explicit branch check: `6b07e6d2c85b4d7867154509138a6b8e734ac2ad`
- PR state immediately before this ledger refresh: open, draft, mergeable
- Pre-ledger implementation/documentation head: `cf40c9d6ae690b6b648012df9a0f5c25b63e2444`
- PR commit count at that pre-ledger checkpoint: 169
- Release status: release-candidate implementation is substantially complete, but this is **not** yet a verified `v1.0.0` release. Do not tag or merge merely because the PR is mergeable. The exact latest commit must pass the required hosted workflows and the real-build manual product/accessibility checks must be completed first.

## Source prompt and project contract preserved

The implementation follows the uploaded RPS Arena master prompt and the repository requirements used throughout this project:

- Kotlin + Compose Multiplatform is the primary application stack.
- Android is the primary mobile target.
- Windows, macOS, and Linux are first-class desktop targets through Compose Desktop.
- iOS is deliberately deferred rather than presented as supported without verified packaging/accessibility quality.
- Rust is optional and used only where it provides independent rules/testing value.
- Classic Rock–Paper–Scissors and Rock–Paper–Scissors–Lizard–Spock are supported.
- CPU and same-device private two-player play remain available offline.
- CPU behavior is transparent, local, and reproducible from a seed.
- Multiple match modes, turn timers, profiles, history, stats, trends, achievements, settings, backup/restore, accessibility controls, and privacy-safe local behavior are included.
- No account is required.
- No analytics/advertising SDK is required.
- No production backend is required.
- Android v1 does not request Internet permission.
- Public repository documentation, CI, security automation, release automation, community files, and funding/support information are maintained alongside code.
- `Made by the Sanskar` branding is retained.
- Business email: `sanskarin@outlook.in`.
- Business email: `sanskarin.business@gmail.com`.
- Support email: `supportramsandesh@gmail.com`.
- GitHub profile: `https://github.com/sanskarIN`.
- Repository: `https://github.com/sanskarIN/rps-arena`.
- Buy Me a Coffee: `https://buymeacoffee.com/sanskarIN`.
- Project-owner commit email requested and observed in inspected Git metadata: `Sanskar <sanskarin@outlook.in>`.
- Changes are intentionally split into many focused commits instead of one monolithic commit.

## Phase 1 — Repository baseline and compatibility audit

Completed:

- Audited the existing repository instead of rebuilding over newer validated work.
- Reconciled the final-audit branch with newer `main` history through a merge rather than discarding either side.
- Preserved granular commit history.
- Corrected Kotlin source and imports whose leading package segment `in` is a Kotlin keyword. Source uses forms such as `package `in`.sanskar.rpsarena...`.
- Replaced preview Android API 37 with stable API 36 after hosted SDK installation proved the preview baseline unreliable.
- Kept Kotlin/AGP/Gradle versions in a supported compatibility range rather than independently using unrelated newest versions.
- Corrected Rust formatting after `cargo fmt --check` exposed the issue.
- Decoupled CodeQL from Android SDK/build availability through no-build Java/Kotlin analysis.
- Removed obsolete duplicate uppercase documentation guides after canonical lowercase replacements were added.
- Kept screenshots honest: no fabricated product screenshots are labeled as actual builds.

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

Historical draft references to API 37, AGP 9.3.0, or Gradle 9.5.1 are not the current production baseline.

## Phase 2 — Core rules and gameplay

Implemented and retained:

- canonical Kotlin `RulesEngine`;
- Classic RPS relationships;
- Lizard–Spock relationships;
- `Gesture.availableFor(variant)` as the shared allowed-gesture source;
- player-vs-CPU;
- same-device two-player pass-and-play;
- hidden first-player choice during local handoff;
- explicit `LocalTurnPhase` state instead of UI sentences stored in the domain model;
- Easy CPU behavior;
- Normal CPU behavior;
- Expert CPU behavior;
- seeded deterministic CPU behavior;
- Best-of-3;
- Best-of-5;
- Tournament first-to-5 behavior;
- Endless;
- Streak;
- configurable 5/10/15/30/60-second turn timers;
- deterministic timeout gesture selection;
- reset behavior that recreates seeded CPU state correctly.

### Public state-boundary ruleset protection

A final source audit found an integrity gap that the current UI masked: the UI correctly hid Lizard/Spock in Classic mode, but another caller could invoke `ArenaState.play(Gesture.LIZARD)` directly.

Fixed:

- `ArenaState.play` validates the supplied gesture against `Gesture.availableFor(config.variant)` before any CPU/local-two-player mutation.
- Invalid variant/gesture combinations are rejected.
- The rejection does not create a round.
- It does not change statistics.
- It does not write history.
- It does not create a pending local-player move.
- Safe logging records only bounded enum metadata (`gesture`, `variant`).

Regression coverage was added for a Classic-state Lizard invocation.

Relevant commits:

- `2617c26db4b299316aff9d29bc372711cc646599` — `fix: reject gestures outside active ruleset`
- `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` — `test: reject extended gestures in classic state`

## Phase 3 — Match configuration and release metadata

Persisted match configuration includes:

- game variant;
- opponent mode;
- CPU difficulty;
- match mode;
- deterministic seed;
- round timer seconds.

`ArenaState.updateConfig` normalizes timer values to `0..60`, persists the new setup, logs only technical enum/numeric metadata, and restarts the current match so configuration/state cannot drift.

### Single release-version authority

Android and desktop previously hard-coded the same version independently. That was a future drift risk.

The version catalog now owns:

- `appVersion = "1.0.0"`
- `appVersionCode = "1"`

Android consumes:

- `versionName = libs.versions.appVersion.get()`;
- `versionCode = libs.versions.appVersionCode.get().toInt()`.

Desktop consumes:

- `packageVersion = libs.versions.appVersion.get()`.

`docs/release.md` explicitly defines the version catalog as the release-version source of truth. Future releases should update the catalog once, then align changelog/release notes/tag to that value rather than reintroducing independent target versions.

Relevant commits:

- `aa3d954c912e169403ff901636cd947f6de5e802` — `build: centralize application release version`
- `2bb2fd19fec23bf30d62e385433d9c9c52158b31` — `build: source Android release version from catalog`
- `dcd827b8569fb7777156a2d40536749ec30e3271` — `build: source desktop release version from catalog`
- `aa76452b289ff242bbc439c968467f13f3c3f058` — `docs: record ruleset guard and shared release version`
- `56281426cdc0b58d3d31b60c22d945b4f4741b94` — `docs: define single release version source`
- `62f17bc6cb0cf59633c631258b79cb84c4ffda1c` — `docs: record active-ruleset state regression`

## Phase 4 — Persistence architecture

Implemented:

- shared `KeyValueStore` abstraction;
- default shared adapter delegating to platform storage;
- Android `SharedPreferences` storage;
- Desktop Java `Preferences` storage;
- injectable in-memory stores for common/state/UI tests;
- persisted settings;
- persisted stats;
- persisted match configuration;
- persisted profiles;
- persisted recent history.

The Android store uses a private `SharedPreferences` file named `rps_arena`.

No database, account service, cloud sync, or backend is required for v1 gameplay/progress.

## Phase 5 — Local player profiles

Implemented:

- up to six local profiles;
- internal bounded profile IDs;
- 1–24 character normalized display names;
- whitespace normalization;
- control/newline rejection;
- active-profile selection;
- create profile;
- rename profile;
- activate profile;
- delete profile;
- refusal to delete the final remaining profile;
- reset to default `Player 1` profile;
- profile persistence;
- profile backup/restore;
- V1 backup migration to default profile.

Profiles are local labels only. They are not authentication accounts, telemetry identities, cloud records, emails, passwords, or online handles.

Aggregate v1 statistics intentionally remain device-wide rather than per-profile. Any future per-profile-stat model must use an explicit migration rather than silently changing existing semantics.

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
- achievement UI copy separated from domain state;
- recent W/L/D trend derived from history instead of persisted twice;
- recent decisive win rate;
- text legend for W/L/D;
- semantic full-result descriptions;
- non-interactive trend status surfaces.

### Profile-name trend ambiguity fix

New player-one wins use an explicit role prefix:

`Player 1 (<profile name>) won`

This prevents a local display name such as `CPU` or `Player 2` from being mistaken for the opponent result by the legacy/current trend parser.

Regression tests cover reserved-looking local profile names.

## Phase 7 — Backup, restore, migration, preview, and atomicity

### Current export format

`RPS_ARENA_BACKUP_V2`

V2 includes:

- settings;
- aggregate statistics;
- match configuration;
- profile IDs and display names;
- active profile;
- recent history.

### Compatibility

- V1 remains accepted for import.
- V1 import migrates to the default local profile.
- New exports use V2.
- Unsupported headers are rejected.

### Defensive limits and validation

Implemented:

- maximum raw backup length: 32,768 characters;
- strict settings decoding;
- non-negative statistics validation;
- `rounds == wins + losses + draws` validation;
- streak consistency validation;
- match enum validation;
- timer range validation;
- bounded profile count;
- profile ID format validation;
- profile-name validation;
- active-profile membership validation;
- bounded history count;
- bounded history line length;
- history control-character validation;
- invalid history escape rejection;
- full required-section decoding before mutation.

### Atomic history validation fix

An earlier implementation staged most backup values but could encounter invalid history after another stage. This was corrected so history is decoded/validated before import writes begin. Malformed history no longer produces partial imported state.

### Strict key/value parser hardening

A later audit identified that the old key collection used `mapNotNull(...).toMap()`, which could silently ignore an unparseable row and silently let a later duplicate key overwrite an earlier value.

This is now fixed.

`parseBackupValues`:

- requires every post-header row to contain a non-empty key followed by `=`;
- validates keys against a bounded key pattern;
- rejects duplicate keys;
- returns failure before settings/stats/config/profile/history decoding if the envelope is ambiguous;
- is shared by backup preview and import because both use the same decoder.

Regression tests verify:

- duplicate `stats` keys are rejected;
- a row with no `=` is rejected;
- preview returns null for those envelopes;
- import returns false;
- pre-existing target statistics remain unchanged.

This supersedes the old handoff note that duplicate/unparseable backup rows remained a future hardening item. That limitation is **closed**.

Relevant commits:

- `e79f49e21ca42cc866ed7f0a7587209d6218a008` — `fix: reject malformed and duplicate backup keys`
- `682d987f836a72d1984003f94603a793842728d3` — `test: reject duplicate backup keys atomically`
- `b9bbea27aaec3115eccbac8f7883cbc3068ddda5` — `test: reject malformed backup rows atomically`
- `3ba9cfd3e02e189dbbc1816a87b77bb995f4f740` — `fix: record strict backup key parsing`
- `cf40c9d6ae690b6b648012df9a0f5c25b63e2444` — `docs: record strict backup parser regressions`

### Backup preview

`previewBackup` uses the exact decoder used by import and is non-mutating. It provides a safe summary including:

- format version;
- profile names;
- active profile;
- stats/config summary;
- history-entry count.

Settings keeps import unavailable until a valid preview exists.

### Undoable history clear

Recent-history clear retains one in-memory restoration snapshot:

- clear history;
- `Undo history clear` restores the snapshot once;
- a new round invalidates the snapshot;
- successful import invalidates it;
- full reset invalidates it.

Full application-data reset remains confirmation-gated.

## Phase 8 — Android OS backup/privacy boundary

A late privacy audit found that the original manifest used `android:allowBackup="true"`. That conflicted with the intended local-data model because Android system backup/transfer could become an additional portability path beyond the explicit RPS Arena export.

The Android backup boundary is now explicit.

### Manifest

`androidApp/src/main/AndroidManifest.xml` now sets:

- `android:allowBackup="false"`;
- `android:fullBackupContent="@xml/backup_rules"`;
- `android:dataExtractionRules="@xml/data_extraction_rules"`.

The v1 manifest still does **not** request `android.permission.INTERNET`.

### Legacy backup rules

`androidApp/src/main/res/xml/backup_rules.xml` excludes the entire shared-preference domain:

- domain: `sharedpref`;
- path: `.`.

### Android 12+ extraction rules

`androidApp/src/main/res/xml/data_extraction_rules.xml` excludes shared preferences from both:

- cloud backup;
- device transfer.

This makes the intended product policy explicit in source. The separate human-readable RPS Arena V2 text backup remains the user-controlled application portability mechanism.

Relevant commits:

- `9c1b0a21f9009714893d61fab00539a36e54a83a` — `privacy: exclude local preferences from legacy Android backup`
- `8aa08fb773df2b3a97ba4abf2d338170453637df` — `privacy: exclude local preferences from Android transfer`
- `3d1c42286c8acd286b14e417f7677f914fb061d3` — `privacy: disable Android backup for local arena data`
- `c5c58f881323ebe8095d1cc6264947b7d7eed295` — privacy policy alignment
- `d6188151e9f933ac58f67b66c33a403592e117b5` — README privacy alignment
- `49a24ee6a139151ced3cb36c1cc92db8b551cfc0` — changelog privacy alignment
- `b57e030aa1a74a9609f24c9c84afa79fae3e5781` — architecture privacy alignment

## Phase 9 — Android privacy regression validator

A deterministic repository-side privacy check was added so future manifest/resource changes cannot silently undo the v1 privacy contract.

`scripts/check_android_privacy.py` parses the Android manifest and backup XML files and fails when any of these invariants break:

- application element missing;
- `android:allowBackup` is not `false`;
- `android:fullBackupContent` does not point to `@xml/backup_rules`;
- `android:dataExtractionRules` does not point to `@xml/data_extraction_rules`;
- `android.permission.INTERNET` appears in the manifest;
- legacy backup XML is missing/invalid;
- legacy backup rules do not exclude all shared preferences;
- Android 12+ extraction XML is missing/invalid;
- cloud-backup rules do not exclude all shared preferences;
- device-transfer rules do not exclude all shared preferences.

The validator is wired into:

- hosted Security checks workflow;
- `scripts/verify.sh`;
- `scripts/verify.ps1`.

Documentation now includes it in security, validation, testing, release, architecture, README/privacy/changelog guidance.

Relevant commits:

- `95298c6ec7b4d86e3aa1def2dc0000db1114d74b` — `privacy: add Android manifest contract validator`
- `d811344030e9bea39f4df5a76cb4d0b092c71bb6` — `security: validate Android privacy contract in CI`
- `a01e7c599a91e31b0241b97df2a216c87ff1324f` — Unix verifier privacy gate
- `0dea92276caacd01795010ba85157ef0ca17c507` — PowerShell verifier privacy gate
- `b7144c45c7f871427393e4ebd7b110e4dd111194` — security policy privacy gate
- `2b648cd832a68035dc9f74fd30e606e9da53f7fb` — validation privacy gate
- `c3a5eac292a3c1fb1baa0f0c9507a111f900bc2f` — testing privacy command
- `9ffe7814e2ecbd8d2fcb25f76d6fbd97a4b9ec24` — release privacy gate

## Phase 10 — UI and UX

Implemented/retained shared Compose screens:

- onboarding;
- Home;
- Play;
- History;
- Stats;
- Achievements;
- Settings;
- About.

Current behavior includes:

- active local profile on Home;
- active profile on scoreboard;
- profile-management Settings card;
- opponent/rules/difficulty/mode/timer configuration;
- deterministic seed input;
- timer countdown and timeout handling;
- private local-player handoff text;
- gesture controls;
- completed-round result card;
- match restart/new match;
- reactive History;
- history clear/undo;
- Stats;
- recent trend card;
- Achievements;
- Settings appearance/accessibility controls;
- validated backup generation/preview/import;
- confirmed full local-data reset;
- About links for repository/profile/BMC/business/support;
- `Made by the Sanskar` branding.

Responsive work includes:

- primary content bounded on large desktop windows;
- fill-width behavior on narrow surfaces;
- horizontally scrollable dense chip rows rather than clipped controls.

## Phase 11 — Local completed-round Copy result

Implemented a local copy/share-preparation path without adding a network SDK or Internet permission.

- Every rendered completed-round card exposes `Copy result`.
- Copied text includes the RPS Arena name, both gesture labels, and the displayed result.
- Clipboard write occurs only after explicit user activation.
- The UI shows `Result copied for sharing.` afterward.
- RPS Arena does not read the previous clipboard contents.
- RPS Arena does not upload copied result text.
- Privacy/accessibility/testing docs record the clipboard boundary.

## Phase 12 — Accessibility and reduced motion

Implemented:

- Material controls for focus/touch semantics;
- 88 dp gesture buttons;
- explicit gesture content descriptions;
- textual outcomes;
- textual timer state;
- textual local-player turn state;
- W/L/D text legend;
- semantic full trend-result descriptions;
- active-profile text labels;
- text-labeled Copy result action;
- explicit copy-success state;
- persisted reduced-motion setting;
- `AnimatedContent` round-result transition only when reduced motion is disabled;
- equivalent static result rendering when reduced motion is enabled;
- destructive reset confirmation;
- undo path for recent-history clear.

`docs/accessibility.md` contains keyboard, screen-reader, scaling, status/contrast, motion, and manual release checks.

## Phase 13 — Localization-ready UI copy boundary

`ui/Strings.kt` centralizes current English UI copy for:

- application/navigation;
- onboarding;
- local profiles;
- match controls;
- difficulty/mode labels;
- timer/seed text;
- local-turn text;
- history;
- stats/trends;
- achievements;
- backup/restore/undo/reset;
- Copy result;
- About/support/funding.

Known localization debt remains intentionally documented:

- `Gesture.label` is still English domain data.

The app therefore describes itself as localization-ready, not already multilingual.

## Phase 14 — Structured local logging

`SafeLogger` includes:

- structured level/event model;
- bounded event-name validation;
- no-op default sink;
- sensitive key-name redaction;
- metadata value truncation;
- tests for safe fields, redaction, truncation, and invalid event names.

Sensitive-key patterns include:

- password;
- passwd;
- secret;
- token;
- authorization;
- cookie;
- email;
- backup;
- content;
- payload.

Intentional logging policy:

- no backup contents;
- no local profile names;
- no history text;
- no credentials/tokens/secrets;
- bounded technical metadata only for normal events.

## Phase 15 — Optional private-room/LAN architecture

Implemented as a tested architecture boundary, not a production network feature:

- `PrivateRoomTransport` suspend interface;
- Hello/Ready/Move/Leave commands;
- protocol-version validation;
- constrained six-character room code;
- sender ID bounds;
- message ID bounds;
- round number bounds;
- variant-compatible gesture validation;
- in-memory contract support/tests;
- ADR threat-model guidance.

v1 intentionally has:

- no production room transport;
- no automatic LAN discovery;
- no mandatory backend;
- no Android Internet permission;
- fully local CPU play;
- fully local same-device play;
- local rules as the authority rather than peer-provided result claims.

A future production transport must add transport-specific malformed-input, replay, disconnect, cancellation, concurrency, resource-bound, discovery, and security testing.

## Phase 16 — Optional Rust engine

Retained an optional standalone Rust rules mirror with:

- Rust 2024 crate;
- deterministic rule resolution;
- unit tests;
- formatting validation;
- Clippy with warnings denied;
- full Rust tests;
- benchmark support;
- Kotlin/Rust rule-contract fixtures/checks.

Kotlin remains the app runtime authority. Rust is not presented as a production runtime integration until such integration has measurable value and equivalent supported-platform behavior.

## Phase 17 — Compose UI regression coverage

Implemented real shared UI coverage rather than leaving all UI validation manual.

Changes include:

- Compose UI test artifact in version catalog;
- `commonTest` UI-test dependency;
- desktop test runtime dependency;
- stable `UiTags.kt` semantic tags;
- tagged onboarding entry action;
- tagged Home Play action;
- tagged gesture controls;
- tagged completed-round result;
- isolated in-memory repository for UI tests;
- `RpsArenaUiTest` primary journey.

Primary automated journey:

1. first render;
2. onboarding is visible;
3. onboarding completion;
4. Home reached;
5. Play opened;
6. Rock selected;
7. first round result rendered.

The selected `androidx.compose.ui.test.v2.runComposeUiTest` API/import was checked against JetBrains Compose Multiplatform source before freezing the test wiring.

## Phase 18 — CI workflow architecture and queue de-duplication

Workflows:

- `.github/workflows/ci.yml`;
- `.github/workflows/codeql.yml`;
- `.github/workflows/docs.yml`;
- `.github/workflows/security.yml`;
- `.github/workflows/release.yml`.

### CI validates

- shared desktop Kotlin compilation;
- all shared tests including Compose UI journey through desktop runtime;
- Android debug assembly;
- Android lint;
- desktop application classes;
- Rust formatting;
- Rust Clippy;
- Rust tests.

### CodeQL

- Java/Kotlin;
- no-build analysis;
- independent from Android SDK build availability;
- scheduled weekly scan retained.

### Documentation

- repository-local Markdown link validation through `scripts/check_docs_links.py`.

### Security

- committed-secret scanner;
- Android privacy contract validator;
- dependency review on pull requests.

### Duplicate-run problem and fix

The audit found CI, Documentation, Security checks, and CodeQL configured both:

- `push` on `main` + `chatgpt/**`;
- `pull_request` on `main`.

For the active PR branch this created duplicate push and PR executions with different `github.ref` concurrency keys. That wasted Actions capacity and contributed unnecessary queue noise.

The workflow trigger policy is now:

- proposed changes: `pull_request` targeting `main`;
- post-merge: `push` targeting `main`;
- CodeQL schedule retained;
- feature/PR branches are not duplicated under `push`.

Existing concurrency still cancels superseded executions within the same PR/ref.

Relevant commits:

- `afa5b7406cf8988e2eb0200f7ee012156a4fb749` — CI de-duplication
- `55acfc22eb5b88133b2951f2fcb7a879989211e2` — Documentation de-duplication
- `c772f6acbadad98bae9668d70602862d2b884890` — Security checks de-duplication
- `62098425881b28a5f6d292a816568c9a0980b69d` — CodeQL de-duplication
- `e41a58e9512dd631ad70797512d9e177419728fa` — validation trigger documentation
- `7eab5e07e0fc21577eac39a7318cf3dc8c3c8e06` — changelog trigger documentation

## Phase 19 — Local verification entry points

Both contributor verification scripts now mirror the meaningful local release checks instead of a reduced subset.

Scripts:

- `scripts/verify.sh`;
- `scripts/verify.ps1`.

They cover:

- shared desktop compilation;
- all shared tests;
- Android debug assembly;
- Android lint;
- desktop classes;
- documentation-link validation;
- Android privacy-contract validation;
- committed-secret scanning;
- optional Rust format/Clippy/tests when Cargo exists.

The scripts support the documented global Gradle 9.5.0 prerequisite until a complete standard wrapper can be safely committed.

Relevant verifier commits include:

- `5a95dad87d9933a31a1a0885db5d97ccbf30c90c`
- `17eacfe0dd8d7187201120f2cea213b43f060ab6`
- `69701b64f462aaf40b944a2392447b4615857ad2`
- `a01e7c599a91e31b0241b97df2a216c87ff1324f`
- `0dea92276caacd01795010ba85157ef0ca17c507`

## Phase 20 — Security and community governance

Repository-side governance now includes:

- `LICENSE`;
- `CONTRIBUTING.md`;
- `CODE_OF_CONDUCT.md`;
- `SECURITY.md`;
- `SUPPORT.md`;
- `PRIVACY.md`;
- `.github/CODEOWNERS`;
- bug-report issue form;
- feature-request issue form;
- issue-template config/routing;
- pull-request template;
- Dependabot;
- FUNDING configuration;
- repository-settings hardening guide;
- committed-secret scanner;
- Android privacy-contract checker;
- CodeQL;
- CI;
- docs checks;
- security workflow;
- release workflow.

Issue/PR templates request platform/reproduction/testing information and ask contributors to consider accessibility, privacy/security, persistence/migration, tests, and documentation impact where relevant.

Recent community/governance commits include:

- `5612dee5be1c36349530e616da4dd4d10f7c1103`
- `662dbf42d4d942ca22ff6eafa6092e5f33a2723c`
- `3ff784e8b10ab6ba61bbe33fb8611a010b422f1e`
- `a17e4a141fe43697ccd21b5f376456bb2893673c`
- `a1c07d8f6c49c7f5331b3acf434f466239873ee1`
- `0da1a665e996fa536b8f6c819e2881ff6edf562d`
- `607c224434b8f8ebf7c26ea31d08a81eaa9aeba3`
- `e1d3f5b7766263373273166821648ba25afcbcf0`
- `16e6b15e352f1ffee390137adc4165afcdb899c3`

## Phase 21 — Documentation audit

Canonical project documentation includes:

- `README.md`;
- `CHANGELOG.md`;
- `ROADMAP.md`;
- `PRIVACY.md`;
- `SECURITY.md`;
- `SUPPORT.md`;
- `CONTRIBUTING.md`;
- `CODE_OF_CONDUCT.md`;
- `LICENSE`;
- `docs/setup.md`;
- `docs/development.md`;
- `docs/testing.md`;
- `docs/architecture.md`;
- `docs/accessibility.md`;
- `docs/performance.md`;
- `docs/release.md`;
- `docs/troubleshooting.md`;
- `docs/repository-settings.md`;
- `docs/VALIDATION.md`;
- ADRs under `docs/adr/`;
- this `what_changed.md` handoff.

Documentation is intentionally conservative:

- no fabricated real-build screenshots;
- no iOS-support claim;
- no production-LAN claim;
- no claim that Rust is the app runtime;
- no claim that unsigned Android release artifacts are Play Store signed;
- no claim that queued checks passed;
- no claim that branch protection is enabled when it is not.

## Phase 22 — Privacy model

Current v1 privacy design:

- no account required;
- no cloud sync required;
- no analytics SDK;
- no ad SDK;
- no mandatory production backend;
- no Android Internet permission;
- Android application backup disabled;
- shared preferences excluded from configured legacy/current Android backup/transfer rules;
- local profile names remain app-local unless included in a user-generated text backup;
- backups are readable and explicitly not encryption/secret storage;
- backup preview/import share one strict decoder;
- logging excludes profile names/backups/history contents/credentials;
- Copy result writes only after explicit user action;
- application does not read existing clipboard contents;
- About external links open only after explicit user action.

## Automated regression coverage inventory

Shared/common tests now cover, among other areas:

- Classic rule relationships;
- Lizard–Spock rule relationships;
- active-ruleset state-boundary rejection;
- seeded CPU reproducibility;
- settings codec round-trip;
- legacy seven-field settings migration;
- stats codec round-trip;
- corrupt stats fallback;
- match config codec/persistence;
- local profile lifecycle;
- profile-name normalization/rejection;
- profile count limits;
- profile persistence;
- V2 backup profile round-trip;
- V1 backup migration;
- backup preview non-mutation;
- oversized backup rejection;
- invalid backup-history rejection;
- atomic invalid-history rejection;
- duplicate backup-key rejection;
- malformed backup-row rejection;
- validated history replacement;
- CPU timeout behavior;
- local two-player timeout handoff;
- disabled timer behavior;
- history clear/undo;
- undo invalidation after new history;
- full reset defaults;
- recent trend parsing/rate calculation;
- reserved-looking profile-name trend behavior;
- private-room protocol validation;
- in-memory transport contract behavior;
- SafeLogger redaction/truncation/event validation;
- Kotlin/Rust rule-contract fixtures;
- primary Compose UI onboarding-to-first-result journey.

Repository scripts/workflows additionally validate:

- Android privacy manifest/rule invariants;
- documentation links;
- high-confidence committed-secret patterns;
- Android build/lint;
- desktop compilation;
- CodeQL;
- dependency changes;
- Rust format/lint/tests.

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

Or use the platform-appropriate aggregate verifier:

- `scripts/verify.sh`
- `scripts/verify.ps1`

## Hosted validation defect/fix history

Observed workflow/build failures during the project were not hidden:

1. **Preview Android API 37 unavailable**
   - Hosted SDK installation failed.
   - Fixed by moving the production/CI baseline to stable API 36.
2. **Rust formatting check failure**
   - `cargo fmt --check` found formatting drift.
   - Rust source was formatted and the check retained.
3. **CodeQL coupled to Android SDK/build path**
   - Analysis reliability depended unnecessarily on Android setup.
   - Fixed with CodeQL no-build Java/Kotlin analysis.
4. **Kotlin keyword package failure**
   - Package path began with `in`, a Kotlin keyword.
   - Source/tests/imports were corrected to use escaped `` `in` `` identifiers.
5. **Audit branch/main divergence**
   - Newer validated main work existed during the audit.
   - Histories were reconciled rather than overwritten.
6. **Excess duplicate Actions executions**
   - PR feature branch was validated via both `push` and `pull_request`.
   - Feature-branch push triggers were removed; PR + main-post-merge validation remains.

Superseded workflow runs frequently show `cancelled` because concurrency cancels earlier commits. Cancelled older runs are not release evidence either way.

## Exact automated status before this ledger refresh

Pre-ledger head:

`cf40c9d6ae690b6b648012df9a0f5c25b63e2444`

PR #10 was open, draft, and mergeable at that checkpoint. GitHub had created the following pull-request workflows for that exact pre-ledger head:

- CodeQL — run `32220188727` — pending
- Documentation — run `32220188648` — queued
- Security checks — run `32220188930` — pending
- CI — run `32220188665` — queued

No failure log existed for those runs when this ledger refresh was prepared. Queued/pending is **not** counted as pass.

This `what_changed.md` update creates a newer branch head, so the authoritative validation set becomes the workflows attached to this new ledger commit. Always fetch PR #10 again and validate its exact latest SHA before deciding release readiness.

## Gradle wrapper audit and exact limitation

The repository currently uses a documented installed Gradle 9.5.0 prerequisite instead of a committed standard Gradle wrapper.

Work performed:

- inspected the repository tree and confirmed there is no partial `gradlew`/`gradlew.bat`/`gradle/wrapper` setup;
- verified the official `gradle-wrapper.jar` from the upstream Gradle `v9.5.0` source tag;
- attempted to transfer the official binary safely through the connected GitHub APIs.

Connector/environment limitation encountered:

- the upstream wrapper binary can be read as base64;
- Git object/blob SHAs are not portable into another repository's tree operation;
- GitHub rejected the foreign upstream blob SHA as not a valid blob for `sanskarIN/rps-arena`;
- the execution container cannot resolve GitHub externally to download and then re-upload the binary;
- committing only scripts/properties without the matching JAR would create a broken wrapper.

Therefore no fake or partial wrapper was committed. Gradle 9.5.0 remains an explicit prerequisite. A complete wrapper should be added only when all official wrapper components can be transferred together and then validated.

## Repository settings that remain outside file-based automation

The latest branch inspection showed `main` is not currently protected.

The connected GitHub tool does not expose a branch-protection/ruleset mutation action, so the audit did not pretend that file changes enable repository protection.

`docs/repository-settings.md` documents recommended settings including:

- branch/ruleset protection;
- exact required checks only after observing stable check names;
- GitHub-native secret scanning/push protection when available;
- dependency alerts;
- private vulnerability reporting.

These remain repository/account settings, not source files.

## Known limitations intentionally retained

- No iOS target in v1.
- No production LAN/private-room transport in v1; only a tested protocol/transport architecture boundary.
- Aggregate statistics are device-wide, not per profile.
- `Gesture.label` remains English domain data; full resource-backed localization is not complete.
- Compose UI automated coverage includes the primary journey but is not exhaustive for every Settings/profile/backup/accessibility flow.
- Real Android/desktop screenshots are not committed until captured from actually verified release-candidate builds.
- Android release automation produces an unsigned artifact; Play Store signing remains outside the public repository.
- A complete Gradle wrapper is not committed because the available integration cannot safely transfer the official wrapper JAR into this repository; the installed Gradle 9.5.0 prerequisite is documented instead.
- No cloud sync by design.

The earlier duplicate/unparseable-backup-key limitation is no longer present; it was fixed in the strict backup parser phase above.

## Manual release gates still required

Before `v1.0.0`:

1. Fetch PR #10 and confirm the exact latest head SHA.
2. Confirm `main` has not advanced unexpectedly or reconcile if it has.
3. Confirm the PR is mergeable.
4. Require successful exact-head CI Kotlin job.
5. Require successful exact-head CI Rust job.
6. Require successful exact-head CodeQL run.
7. Require successful exact-head Documentation run.
8. Require successful exact-head committed-secret scan.
9. Require successful exact-head Android privacy-contract validation.
10. Require successful dependency review when GitHub makes it available for the PR.
11. Review Dependabot/security alerts where repository settings provide them.
12. Verify first-run onboarding on a real Android build.
13. Verify Classic CPU at each difficulty.
14. Verify Lizard–Spock CPU play.
15. Verify same-device two-player hidden first move.
16. Verify Best-of-3.
17. Verify Best-of-5.
18. Verify Tournament first-to-5.
19. Verify Endless continuation.
20. Verify Streak continuation.
21. Verify deterministic seed replay with identical inputs.
22. Verify every timer preset.
23. Verify CPU timeout move.
24. Verify both local-player timeout phases.
25. Verify match/settings persistence after restart.
26. Verify profile create/rename/select/delete.
27. Verify profile persistence after restart.
28. Verify generated V2 backup preview.
29. Verify V2 import with multiple profiles/stats/config/history.
30. Verify valid V1 migration.
31. Verify malformed backup rejection without mutation.
32. Verify duplicate-key backup rejection without mutation.
33. Verify oversized backup rejection without mutation.
34. Verify history clear/undo.
35. Verify undo invalidation after new history.
36. Verify recent W/L/D trend display.
37. Verify Copy result clipboard content and success status.
38. Verify full local-data reset confirmation/result.
39. Verify light theme.
40. Verify dark theme.
41. Verify system theme.
42. Verify reduced-motion result rendering.
43. Verify keyboard navigation on desktop.
44. Verify screen-reader/semantic gesture labels where available.
45. Verify trend semantics do not depend on color.
46. Verify About repository/funding/business/support links.
47. Verify Android manifest has no INTERNET permission.
48. Verify Android backup/privacy validator passes.
49. Capture real Android release-candidate screenshots.
50. Capture real desktop release-candidate screenshots.
51. Move PR #10 out of draft only after automated/manual evidence is acceptable.
52. Merge without squashing if preserving the intentionally granular commit history remains desired.
53. Verify resulting `main` SHA.
54. Tag only the verified main commit.
55. Verify actual release workflow outputs before publishing artifacts.
56. Never publish the unsigned Android artifact as if it were store-signed.

## Commit identity

Requested project-owner identity:

`Sanskar <sanskarin@outlook.in>`

Git metadata inspected during this continuation for commit `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` showed both author and committer with that email.

## Granular commit record — major continuation checkpoints

The branch contains a large number of focused commits. Significant continuation commits include:

### Profiles, trends, state, data safety

- `80f4a887e20d699a01f47d820280b9f45f97c7a1` — local profile models
- `e11045e99872128cf3b7122236b8dbed41ca07af` — persisted/backed-up profiles
- `cc22fed3ce1842dcef450fa4124b7d332a93dad2` — profile lifecycle state wiring
- `adb0572275cad32e8c3bb7a214cd632a7527e9ef` — profile/migration tests
- `d4ec46b411bca10275edb1d3db756696ba7a9642` — profile management UI
- `e4d2963e047d4615b939abb277e6cfabd10f0a23` — active profile UI
- `0fd9c62ebcd61a1a8a20db69d9384511de47022e` — recent trend derivation
- `6317825774d7055d6cc77543227f7daa73c506a5` — trend parser tests
- `7de3e0973aa9af57e8ca896b25f66ef88d087e96` — accessible trend card
- `0dfff6138a1777201962b87a56af3b5357852507` — unambiguous profile/result history
- `68d5cb02c2ce8ea31b9364f89854ab149ec1f8be` — non-interactive trend status
- `57268aadeefb7a85aaa76928e34acc018874ef47` — backup preview model
- `d051e6f175f77fe69fbc4aa03557a4170d9a23e6` — backup preview/history restoration
- `e2d87cec038b60bb0ec64c853ed8f5e0f4320624` — history validation before mutation
- `a836081ef0c4a55102bc65173a28209837a8ee6e` — backup safety tests
- `4dd6c1bee71db8478291505a9573c8d5344d6f0f` — backup preview/history undo
- `20575e761054e813b69163c5737e1467f162b0ae` — preview/undo state tests
- `4b611b34fac9ba693361b23e755e6eaf208817ac` — validated backup/undo controls
- `cfe0c6ddffd514770dadf0c4078509cc00138ee5` — Settings data controls

### Localization, logging, UI tests, copy result

- `a228759a7b1af482673835f43470ba1b7502a5c4` — domain/UI copy separation
- `2197d59329f3eef8c58324f6ca1e3895e007d124` — local-turn domain state
- `d2396e458711d769ffc10cc0540b4cd192111dd8` — turn/achievement UI copy
- `c2f4176ad1e311f1e2e19471251539ae7eee44b1` — committed-secret scanner
- `a3f333d6a1a83317fe8387a9e170620923861448` — security/dependency review workflow
- `733c09acf34e8a538caf15a831dbc0162d1f5981` — Compose UI-test dependency catalog
- `0de9f12b1eb4ce3bc07956499aa4d911d1484df7` — UI-test source-set/runtime wiring
- `02023a407aa981e4e9902624a27bd51902e8c515` — stable UI tags
- `0003a1df28932d880f27ae97c2d47e7b85f606c2` — primary Compose UI test
- `732247f4e9929ad1767f0a501c06143ffee819be` — Copy result UI copy
- `ea26d3a72b679b28034638401fe4695512ae6c2f` — Copy result behavior
- `3fad5498edfde3d8c881e7a3ff0d9d9e68cc6e78` — clipboard privacy documentation
- `93b279fb81a87062695fc66a1cd331dc92af87ce` — accessibility/UI-test/copy docs

### Documentation, governance, verification

- `cdb863543a17ff9eef944c160a8ff78e8283a4b2` — repository settings guide
- `b6837d688b2fded5ed253acd3e2174d8e9da3254` — privacy documentation
- `bc5e3031e97cb0b6aefd068e425a2d875578d77e` — architecture documentation
- `d1a9cf1f13d9b1b955e33ff90ebec4af16e7f11a` — README feature alignment
- `b8b1585ccc4803ec4816459ee137ed5742c46021` — release-candidate roadmap
- `a3166fb903f4429c007ce486264b9750a6d6c737` — release gates
- `7eec5de5f0cbd8b44019ae41ce5ce002a2fd4198` — accessibility docs
- `beabd44d81ba3c4fdc47a52d1b35ddb4a431bcbf` — previous detailed handoff
- `5a95dad87d9933a31a1a0885db5d97ccbf30c90c` — Unix verifier expansion
- `17eacfe0dd8d7187201120f2cea213b43f060ab6` — PowerShell verifier expansion
- `69701b64f462aaf40b944a2392447b4615857ad2` — validation contract alignment
- `a1c07d8f6c49c7f5331b3acf434f466239873ee1` — CODEOWNERS
- `0da1a665e996fa536b8f6c819e2881ff6edf562d` — issue routing configuration
- `607c224434b8f8ebf7c26ea31d08a81eaa9aeba3` — bug template hardening
- `e1d3f5b7766263373273166821648ba25afcbcf0` — feature template hardening
- `16e6b15e352f1ffee390137adc4165afcdb899c3` — PR template hardening

### Final compatibility, privacy, workflow, parser hardening

- `2617c26db4b299316aff9d29bc372711cc646599` — state ruleset guard
- `df087d4a0fcf4833dcb2d7a7e0bc4fbeddbb3355` — state ruleset regression
- `aa3d954c912e169403ff901636cd947f6de5e802` — central release version
- `2bb2fd19fec23bf30d62e385433d9c9c52158b31` — Android version wiring
- `dcd827b8569fb7777156a2d40536749ec30e3271` — desktop version wiring
- `afa5b7406cf8988e2eb0200f7ee012156a4fb749` — CI PR-trigger de-duplication
- `55acfc22eb5b88133b2951f2fcb7a879989211e2` — docs PR-trigger de-duplication
- `c772f6acbadad98bae9668d70602862d2b884890` — security PR-trigger de-duplication
- `62098425881b28a5f6d292a816568c9a0980b69d` — CodeQL PR-trigger de-duplication
- `9c1b0a21f9009714893d61fab00539a36e54a83a` — legacy Android backup exclusion
- `8aa08fb773df2b3a97ba4abf2d338170453637df` — Android extraction/transfer exclusions
- `3d1c42286c8acd286b14e417f7677f914fb061d3` — manifest backup disablement
- `95298c6ec7b4d86e3aa1def2dc0000db1114d74b` — Android privacy validator
- `d811344030e9bea39f4df5a76cb4d0b092c71bb6` — privacy validator in Security checks
- `a01e7c599a91e31b0241b97df2a216c87ff1324f` — privacy check in Unix verifier
- `0dea92276caacd01795010ba85157ef0ca17c507` — privacy check in PowerShell verifier
- `e79f49e21ca42cc866ed7f0a7587209d6218a008` — strict backup key parser
- `682d987f836a72d1984003f94603a793842728d3` — duplicate-key regression
- `b9bbea27aaec3115eccbac8f7883cbc3068ddda5` — malformed-row regression
- `3ba9cfd3e02e189dbbc1816a87b77bb995f4f740` — parser changelog alignment
- `cf40c9d6ae690b6b648012df9a0f5c25b63e2444` — parser testing documentation

## Continuation procedure

For the next continuation, do not start by inventing another v1 feature. Begin from repository evidence:

1. Fetch PR #10 metadata and capture the exact latest head SHA.
2. Fetch CI/CodeQL/Documentation/Security workflows for that exact SHA.
3. If a job failed, fetch its jobs/steps/logs and fix only the observed problem with a focused commit and regression coverage where practical.
4. If checks are queued/pending, do not call them passed.
5. Confirm `main` has not advanced before final merge; reconcile if needed.
6. Refresh this file after any corrective source change.
7. Complete the real Android/desktop manual product and accessibility checklist only with actual build evidence.
8. Capture only real build screenshots.
9. Move PR #10 out of draft only after acceptable automated/manual evidence.
10. Merge without squash if preserving granular history is still desired.
11. Verify the resulting `main` commit and release artifacts before tagging/publishing.
