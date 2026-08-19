# What Changed

This file is the authoritative implementation handoff for the current RPS Arena audit. It intentionally contains the detailed repository progress that would otherwise be repeated in chat.

## Current checkpoint

- Date: 2026-08-19
- Repository: `sanskarIN/rps-arena`
- Working branch: `chatgpt/final-audit-20260819`
- Pull request: `#10` — `fix: complete final quality and compatibility audit`
- Pull-request base: `main`
- PR state at this checkpoint: open, draft, mergeable
- Feature/documentation head immediately before this ledger refresh: `93b279fb81a87062695fc66a1cd331dc92af87ce`
- Release status: release-candidate implementation complete enough for final automated/manual gates; do not tag `v1.0.0` until the exact latest commit passes required workflows and the manual product/accessibility checklist.

## Source prompt scope preserved

The implementation continues to follow the uploaded RPS Arena master prompt:

- Kotlin + Compose Multiplatform as the primary stack;
- Android plus Windows/macOS/Linux desktop support;
- optional Rust rules/engine work only where it has explicit value;
- classic RPS and optional Lizard–Spock;
- CPU and same-device private play;
- difficulty presets and multiple match modes;
- offline-first local persistence;
- profiles/settings/history/statistics/trends/achievements;
- timers and deterministic seeded challenges;
- accessibility and reduced-motion behavior;
- localization-ready user-facing copy;
- structured privacy-conscious logging;
- backup/restore and destructive-action safety;
- optional private-room/LAN architecture without making networking mandatory;
- CI, security, release, documentation, and repository governance;
- small meaningful commits rather than one monolithic change.

## Phase 1 — Baseline and compatibility audit

Completed:

- Reconciled the final-audit branch with the latest validated `main` work rather than overwriting newer fixes.
- Corrected Kotlin source/test packages whose leading `in` segment must be escaped as `` `in` ``.
- Replaced the preview Android API baseline with stable Android 16 / API 36 for reproducible hosted CI.
- Aligned the actual version catalog and documentation to the implementation rather than stale planned versions.
- Fixed the Rust formatting issue found by workflow validation.
- Kept CodeQL independent from Android SDK installation.
- Preserved existing implementation work while removing obsolete duplicate uppercase docs.

### Current verified toolchain configuration in source

- Kotlin: `2.4.10`
- Compose Multiplatform: `1.11.0`
- Android Gradle Plugin: `9.1.0`
- Gradle verification baseline: `9.5.0`
- AndroidX Activity Compose: `1.13.0`
- kotlinx.coroutines: `1.11.0`
- JDK: `17+`
- Android min SDK: `26`
- Android compile/target SDK: `36`
- Rust crate: Rust 2024 edition

The previous handoff incorrectly mentioned API 37, AGP 9.3.0, and Gradle 9.5.1. Those stale values are superseded by the source-controlled values above.

## Phase 2 — Core gameplay and modes

Implemented and retained:

- classic Rock–Paper–Scissors rules;
- Rock–Paper–Scissors–Lizard–Spock variant;
- player-vs-CPU;
- same-device two-player pass-and-play with hidden first choice;
- Easy, Normal, and Expert CPU behavior;
- Best-of-3;
- Best-of-5;
- Endless;
- Streak;
- Tournament first-to-5 behavior;
- deterministic seeded CPU behavior;
- persisted match configuration;
- optional 5/10/15/30/60-second turn timers;
- deterministic timeout gesture selection;
- explicit local two-player turn phase rather than UI text stored in domain state.

## Phase 3 — Offline persistence and local profiles

Implemented:

- `KeyValueStore` abstraction with Android and desktop platform adapters;
- injectable in-memory stores for common tests;
- persisted settings;
- persisted aggregate statistics;
- persisted match configuration;
- bounded recent history;
- up to six local-only profiles;
- internal profile IDs;
- 1–24-character normalized display names;
- active-profile selection;
- create, rename, select, and delete lifecycle;
- refusal to delete the final remaining profile;
- reset back to the default `Player 1` profile.

Profile display names are local identities only. They are not accounts, authentication identities, cloud records, or telemetry identifiers.

Aggregate v1 statistics are intentionally device-wide rather than per-profile. This is a documented storage contract, not an accidental ambiguity. A future per-profile-stat implementation must use an explicit migration.

## Phase 4 — History, statistics, trends, and achievements

Implemented:

- reactive recent history after play/import/clear/undo/reset;
- maximum 30 stored history rows;
- bounded/sanitized history writes;
- lifetime rounds/wins/losses/draws;
- lifetime win rate;
- current and best streak;
- achievement unlock conditions;
- user-facing achievement title/description moved out of the domain model;
- recent W/L/D trend derived from the persisted history instead of duplicated storage;
- trend parser supports legacy `Player 1 won`, current profile-name wins, CPU losses, player-2 losses, and draws;
- recent decisive win rate;
- non-color-only W/L/D legend;
- semantic trend descriptions such as `Recent result 1: Win`;
- new Player-1 win history uses an explicit `Player 1 (<profile>) won` role prefix so profile names such as `CPU` or `Player 2` cannot be mistaken for an opponent result;
- regression coverage for reserved-looking local profile names;
- trend status tokens are non-interactive semantic surfaces instead of fake clickable controls.

## Phase 5 — Data safety, backup, restore, and undo

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

- V1 remains accepted for import.
- V1 imports migrate to the default local profile.
- Unknown backup versions are rejected.

### Defensive validation

Implemented:

- maximum backup input size of 32,768 characters;
- strict settings decoding;
- internally consistent non-negative statistic validation;
- bounded timer/config validation;
- bounded profile count;
- profile-ID validation;
- profile-name validation;
- bounded history count/line length;
- invalid escape rejection;
- full decode/validation before imported state is mutated.

A reliability issue was found during this audit: history validation initially had one post-write path after the other backup sections were staged. It was fixed so history validation completes in the shared decoder before any import writes begin.

### Preview

Implemented `previewBackup` using the exact same decoder used by import. The preview:

- does not mutate local state;
- exposes format version;
- exposes profile names and active profile;
- exposes aggregate round count/config summary data;
- exposes history-entry count;
- is used by Settings to keep import disabled until the backup validates.

### Undoable destructive action

Recent-history clear now keeps one in-memory undo snapshot:

- clear history;
- `Undo history clear` can restore it;
- a newly written history entry invalidates the snapshot;
- successful import invalidates it;
- full reset invalidates it.

Full local-data reset still requires explicit confirmation because it intentionally removes multiple independent categories of data.

## Phase 6 — UI, desktop responsiveness, accessibility, and motion

Implemented/retained:

- shared Compose Multiplatform UI;
- onboarding;
- Home/Play/History/Stats/Achievements/Settings/About screens;
- active local profile shown on Home and scoreboard;
- profile-management Settings card;
- validated backup preview/import controls;
- history clear/undo controls;
- local completed-round `Copy result` action;
- explicit copied-for-sharing state after the clipboard write;
- responsive primary content bounded to 960 dp on large windows;
- horizontally scrollable dense chip groups on narrow layouts;
- Material controls for keyboard/touch semantics;
- 88 dp gesture buttons;
- explicit gesture content descriptions;
- textual round outcomes and timer state;
- W/L/D trends with full semantic meanings;
- light, dark, and system theme behavior;
- persisted reduced-motion preference;
- animated round-result transition only when reduced motion is disabled;
- direct static result rendering when reduced motion is enabled;
- destructive reset confirmation;
- stable semantic UI test tags for onboarding, Home Play, gesture controls, and the rendered round result.

## Phase 7 — Localization-ready boundary

Implemented a central `ui/Strings.kt` catalog for the current English product copy, including:

- navigation/onboarding;
- settings;
- local profiles;
- match controls;
- timers/seeds;
- history/statistics/trends;
- achievement title/description;
- local two-player turn text;
- backup/restore/undo/reset text;
- Copy result/copy-success text;
- About/support/funding text.

Known localization debt:

- `Gesture.label` remains in the domain model in English.

This is explicitly tracked for a future resource-backed locale layer rather than claiming the app is already multilingual.

## Phase 8 — Logging and privacy

Implemented structured local logging with sensitive-key redaction.

Intentional rules:

- do not log backup contents;
- do not log local profile display names;
- do not log history text;
- do not log tokens/credentials/secrets;
- log bounded technical metadata such as profile/history counts, modes, difficulty, timer seconds, and outcomes.

Privacy documentation now covers local profiles, backups, logging, history undo, the clipboard boundary, and the non-production network boundary.

The completed-round Copy result action writes only the displayed result summary to the operating-system clipboard after an explicit user action. RPS Arena does not read clipboard contents and does not upload copied result text.

## Phase 9 — Optional private-room/LAN architecture

Implemented a pure shared protocol boundary:

- `PrivateRoomProtocol`;
- protocol version validation;
- six-character constrained room-code validation;
- bounded message IDs;
- bounded round numbers;
- variant-compatible move validation;
- Hello/Ready/Move/Leave commands;
- `PrivateRoomTransport` interface;
- in-memory contract/testing support.

Important v1 boundary:

- there is no production network transport;
- Android still requests no internet permission;
- there is no automatic discovery;
- there is no mandatory backend;
- CPU and pass-and-play remain fully local;
- peer-provided results are not intended to become authoritative over the local rules engine.

Production LAN transport remains a later milestone requiring threat modeling, replay/disconnect/resource-bound behavior, malformed-input testing, and transport-specific security review.

## Phase 10 — Optional Rust engine

Retained an optional standalone Rust rules mirror with:

- Rust 2024 edition crate;
- rule-resolution implementation;
- unit tests;
- formatting check;
- Clippy with warnings denied;
- benchmark support;
- Kotlin/Rust rule-contract fixtures/checks.

Rust remains optional. Kotlin is authoritative for the application until a future integration has a measurable reason and equal supported-target coverage.

## Phase 11 — CI, security, documentation, and release automation

### CI

`.github/workflows/ci.yml` validates:

- shared desktop compilation;
- all shared tests, including the shared Compose primary-journey test through the desktop test runtime;
- Android debug assembly;
- Android lint;
- desktop application compilation;
- Rust formatting;
- Rust Clippy;
- Rust tests.

### CodeQL

`.github/workflows/codeql.yml` performs Java/Kotlin analysis independently from Android SDK package availability.

### Documentation

`.github/workflows/docs.yml` runs repository-local Markdown link validation through `scripts/check_docs_links.py`.

### Security checks

Added:

- `scripts/check_for_secrets.py` — high-confidence committed-secret pattern scan;
- `.github/workflows/security.yml` — committed-secret scan on pushes/PRs plus GitHub dependency review on pull requests;
- `.github/dependabot.yml` coverage for Gradle, Cargo, and GitHub Actions.

GitHub-native secret scanning, push protection, dependency graph/alerts, private vulnerability reporting, and branch rules remain repository/account settings. `docs/repository-settings.md` documents the recommended configuration without falsely claiming settings that cannot be proven from repository files.

### Release automation

`.github/workflows/release.yml` supports manual/tagged artifact builds:

- unsigned Android release APK;
- Linux desktop distributable;
- Windows desktop distributable;
- macOS desktop distributable.

Distribution signing credentials remain intentionally outside Git.

## Phase 12 — Documentation/governance audit

Current documentation set includes or updates:

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

README, privacy, testing, architecture, accessibility, development, release, roadmap, and changelog have been aligned with the actual implemented behavior in this audit.

## Phase 13 — Compose primary-journey regression coverage

Implemented a real shared Compose UI regression path rather than leaving UI testing as documentation-only future work.

Changes:

- added `org.jetbrains.compose.ui:ui-test` at the same Compose version through the version catalog;
- configured `commonTest` with the Compose UI test artifact;
- configured `desktopTest` with `compose.desktop.currentOs` so the shared UI test has a desktop runtime;
- added `UiTags.kt` with stable semantic test tags instead of relying on visible English copy;
- tagged the onboarding entry action, Home Play action, gesture buttons, and rendered round-result card;
- added `RpsArenaUiTest.kt` using an isolated in-memory repository;
- the automated primary journey covers first render → onboarding completion → Home → Play → Rock → rendered first-round result.

The chosen `runComposeUiTest` v2 API/import was independently checked against JetBrains Compose Multiplatform source before freezing this change. Hosted CI remains the clean execution authority for the project itself.

## Phase 14 — Local completed-round Copy result

Implemented the prompt's share/copy-result direction without adding a network SDK, account requirement, or Android internet permission.

Behavior:

- every rendered completed-round card exposes `Copy result`;
- the copied text contains the RPS Arena name, the two gesture labels, and the displayed result;
- copy happens only after explicit user activation;
- the UI shows `Result copied for sharing.` afterward;
- the current implementation uses the common Compose clipboard manager for Android/desktop compatibility;
- privacy/accessibility/testing documentation explicitly records the clipboard boundary and manual verification expectation.

## Automated regression coverage added/expanded

Common Kotlin tests now cover, among other existing areas:

- classic rules;
- Lizard–Spock rules;
- seeded CPU behavior;
- settings/stat/config codecs;
- seven-field legacy settings migration;
- corrupt stat fallback;
- local profile lifecycle;
- profile name normalization/rejection;
- profile maximum count;
- profile persistence;
- V2 backup profile round trip;
- V1 backup migration;
- backup preview non-mutation;
- oversized backup rejection;
- malformed backup rejection;
- atomic invalid-history backup rejection;
- validated history replacement;
- match-config persistence;
- CPU timeout round creation;
- local two-player timeout phase handoff;
- disabled timer behavior;
- history clear/undo;
- undo invalidation after new history;
- reset to safe defaults;
- recent W/L/D parser behavior and rate calculation;
- reserved-looking local profile names in recent trend history;
- private-room protocol validation/contract behavior;
- primary shared Compose UI navigation/gameplay journey from onboarding to first result.

## Validation commands used by source-controlled workflows

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

The current execution environment cannot rely on an external networked local clone, so hosted GitHub Actions is the authoritative clean-run evidence for the final release gate.

## Workflow status at the final pre-ledger feature/document freeze checkpoint

For commit `93b279fb81a87062695fc66a1cd331dc92af87ce`, GitHub had created the exact-head workflow set and all four runs were still queued when this ledger update was prepared:

- Security checks — run `32216366105` — queued
- CodeQL — run `32216366142` — queued
- CI — run `32216366111` — queued
- Documentation — run `32216366119` — queued

The CI run contains separate Kotlin and Rust jobs; the Security run contains dependency-review and committed-secret-scan jobs. They were queued, not failed, and therefore had no actionable failure logs at that checkpoint.

Older runs commonly show `cancelled` because branch workflow concurrency intentionally cancels superseded commits. A cancelled older run is not treated as evidence for or against the newest head.

This `what_changed.md` commit creates a newer exact branch head and therefore a newer workflow set. The workflow results attached to this ledger commit—not the `93b279...` runs above—become authoritative for the next validation decision.

## Final static audit after feature freeze

Completed before this ledger update:

- searched repository source for `TODO`, `FIXME`, `XXX`, placeholder/not-implemented markers; no actionable matches were found;
- searched for stale preview toolchain claims such as Android API 37 / AGP 9.3.0 / Gradle 9.5.1; no current implementation claim requiring correction was found;
- verified the PR remained mergeable and draft;
- confirmed repeated exact-head workflow reads showed queued state rather than a reported failure;
- attempted an independent clean network clone in the execution container, but the container could not resolve GitHub externally; this was an environment limitation, not a repository pass/fail signal;
- no release-ready claim was made without hosted workflow execution evidence.

## Known limitations intentionally retained

- No iOS target in v1.
- No production LAN/private-room transport in v1; only a tested protocol/transport boundary.
- Aggregate statistics are device-wide, not per-profile.
- Gesture labels are still English domain-model values; full resource-backed localization is not implemented yet.
- Compose UI coverage now protects the primary onboarding-to-first-result journey, but profile/settings/backup/reduced-motion/accessibility interaction coverage is not yet as deep as common deterministic logic/state tests.
- Real Android/desktop screenshots are not committed until they can be captured from an actually verified release-candidate build.
- Android release workflow produces an unsigned artifact; Play Store signing is deliberately out-of-repository.
- The clipboard implementation uses the common Compose clipboard manager for broad target compatibility; it is intentionally isolated to an explicit Copy result action and can be migrated to newer platform clipboard APIs later without changing game state.

These are documented roadmap items or release constraints, not hidden unfinished behavior.

## Manual release gates still required

Before `v1.0.0`:

1. Confirm the exact latest PR head is mergeable and based on current `main`.
2. Require successful latest CI Kotlin job, including the shared Compose primary-journey test.
3. Require successful latest CI Rust job.
4. Require successful latest CodeQL run.
5. Require successful latest Documentation run.
6. Require successful latest committed-secret scan.
7. Require successful dependency review when GitHub supports it for the PR.
8. Run/verify the manual product checklist in `docs/testing.md` on Android and desktop, including Copy result clipboard content.
9. Run/verify the accessibility checklist in `docs/accessibility.md`.
10. Verify V2 backup preview/restore and V1 migration in an actual application build.
11. Capture real screenshots from verified Android/desktop builds.
12. Merge without squashing if preserving the intentionally granular commit history is desired.
13. Tag `v1.0.0` only from the verified `main` commit.
14. Verify release artifacts actually produced by the tag/manual release workflow before publishing them as supported downloads.

## Commit identity

The requested project-owner email is:

`Sanskar <sanskarin@outlook.in>`

Repository guidance documents this email for local Git configuration. GitHub commits created through the authenticated integration use the repository/account commit identity available to that integration; earlier inspected commit metadata in this audit showed `sanskarin@outlook.in` as the Git author/committer email.

## Recent atomic commits from this continuation

The following continuation commits were intentionally kept focused rather than squashed:

- `80f4a887e20d699a01f47d820280b9f45f97c7a1` — `feat: add local player profile models`
- `e11045e99872128cf3b7122236b8dbed41ca07af` — `feat: persist and back up local player profiles`
- `cc22fed3ce1842dcef450fa4124b7d332a93dad2` — `feat: wire local profile lifecycle into arena state`
- `adb0572275cad32e8c3bb7a214cd632a7527e9ef` — `test: cover local profiles and backup migration`
- `3b3e25535d3379f8ff4b07a80819b0c66231a7af` — `feat: add local profile UI copy`
- `d4ec46b411bca10275edb1d3db756696ba7a9642` — `feat: add local profile management UI`
- `e4d2963e047d4615b939abb277e6cfabd10f0a23` — `feat: surface active local profile across app UI`
- `0fd9c62ebcd61a1a8a20db69d9384511de47022e` — `feat: derive recent win loss draw trends`
- `6317825774d7055d6cc77543227f7daa73c506a5` — `test: cover recent trend parsing`
- `093b7494eca1066824a6369aa379dfb413377f59` — `feat: add recent trend accessibility copy`
- `7de3e0973aa9af57e8ca896b25f66ef88d087e96` — `feat: add accessible recent results trend card`
- `a228759a7b1af482673835f43470ba1b7502a5c4` — `refactor: keep achievement and turn copy out of domain models`
- `2197d59329f3eef8c58324f6ca1e3895e007d124` — `refactor: expose local turn state without UI copy`
- `d2396e458711d769ffc10cc0540b4cd192111dd8` — `refactor: externalize turn and achievement copy`
- `2ea6de320ea93d02bb9eca71266a749a405fce15` — `feat: show localized turn state and recent trends`
- `57268aadeefb7a85aaa76928e34acc018874ef47` — `feat: add validated backup preview model`
- `d051e6f175f77fe69fbc4aa03557a4170d9a23e6` — `feat: preview backups and support history restoration`
- `e2d87cec038b60bb0ec64c853ed8f5e0f4320624` — `fix: validate backup history before mutating local data`
- `a836081ef0c4a55102bc65173a28209837a8ee6e` — `test: cover backup preview and atomic history validation`
- `cd32fd9e980a1584cb811630e48f67b01b0d9f14` — `fix: make atomic backup rejection fixture unambiguously invalid`
- `4dd6c1bee71db8478291505a9573c8d5344d6f0f` — `feat: add backup preview and undoable history clearing`
- `20575e761054e813b69163c5737e1467f162b0ae` — `test: cover profile state backup preview and history undo`
- `c933991881be5ba4280ce691e932908a5ad4025f` — `feat: add backup preview and history undo copy`
- `4b611b34fac9ba693361b23e755e6eaf208817ac` — `feat: add validated backup and undo data controls`
- `cfe0c6ddffd514770dadf0c4078509cc00138ee5` — `feat: integrate safe local data controls into settings`
- `1aaf27604cd6f25a23effbae1deafe4b871582b3` — `fix: adapt history undo callback to unit action`
- `c2f4176ad1e311f1e2e19471251539ae7eee44b1` — `security: add deterministic secret pattern scanner`
- `a3f333d6a1a83317fe8387a9e170620923861448` — `security: add secret scan and dependency review workflow`
- `cdb863543a17ff9eef944c160a8ff78e8283a4b2` — `docs: add GitHub repository settings hardening guide`
- `b6837d688b2fded5ed253acd3e2174d8e9da3254` — `docs: document local profiles backups and logging privacy`
- `bc5e3031e97cb0b6aefd068e425a2d875578d77e` — `docs: align architecture with profiles trends backups and private rooms`
- `2b4680320f92b8ac302157e1362cb6a04abc2569` — `docs: expand verification coverage for profiles trends and safe data controls`
- `8c35ed6fe1efafb2c786f476324b9f4d23706651` — `docs: record profiles trends backup preview and security checks`
- `d1a9cf1f13d9b1b955e33ff90ebec4af16e7f11a` — `docs: align README with completed local-first feature set`
- `b8b1585ccc4803ec4816459ee137ed5742c46021` — `docs: refresh roadmap against implemented release candidate`
- `a3166fb903f4429c007ce486264b9750a6d6c737` — `docs: strengthen release gates for security and backup migration`
- `7eec5de5f0cbd8b44019ae41ce5ce002a2fd4198` — `docs: align accessibility guide with trends profiles and reduced motion`
- `9f2f68e8a408036c912d6c37f38d045bbf8624cb` — `docs: document current development quality boundaries`

Additional post-ledger atomic commit messages now represented in this file include:

- `fix: make profiled player one history outcomes unambiguous`
- `test: cover reserved-looking local profile trend names`
- `refactor: centralize trend accessibility labels`
- `fix: make recent trend status tokens non-interactive`
- `test: add Compose UI test dependency alias`
- `test: configure shared Compose UI testing runtime`
- `test: add stable primary journey UI tags`
- `test: tag stable primary UI journey controls`
- `test: cover onboarding to first round primary UI journey`
- `feat: add local copy result UI copy`
- `feat: add local copy result action`
- `docs: record primary Compose UI regression coverage`
- `docs: document copy result and primary UI test`
- `docs: record UI regression and copy result completion`
- `docs: document explicit clipboard result behavior`
- `docs: include copy result and UI test accessibility coverage`
- `docs: record final UI test and copy result audit`

Their exact SHAs remain available in PR #10 history; this handoff records the exact messages even where the connector session did not retain every intermediate SHA in compact context.

## Next action for a continuation session

Do not add new v1 features first. Start from the exact latest PR head and:

1. inspect the latest CI/CodeQL/Documentation/Security workflow results;
2. if a job fails, open its jobs/logs and fix only the observed defect with a focused commit plus regression coverage where applicable;
3. refresh this file with the new workflow evidence;
4. once all automated checks pass, run/record the manual Android and desktop release checklist and capture real screenshots;
5. only then move PR #10 out of draft, merge while preserving granular history, and proceed to the verified release/tag workflow.
