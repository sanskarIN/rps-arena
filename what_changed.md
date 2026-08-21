# What Changed

## 2026-08-21 — Offline portability: versioned backup and restore

RPS Arena now has an offline, human-readable backup/restore path for portable local progress without adding accounts, networking, analytics, advertising, or new Android permissions.

### Milestone

- Roadmap milestone: **1.1 offline portability**.
- Feature branch: `feature/versioned-backups`.
- Pull request: `#12` — `feat: add versioned offline backup and restore`.
- Backup schema: `RPSARENA_BACKUP|1`.

### Completed work

- Added `ArenaBackup`, typed backup errors, decode/import result types, and `ArenaBackupCodec`.
- Added strict schema-v1 encoding/decoding for settings, aggregate statistics, and up to 30 recent history summaries.
- Added validation for the magic header, schema version, booleans, non-negative statistics, round totals, streak invariants, history count, and history-record shape.
- Added history sanitization so line breaks cannot corrupt the line-oriented backup structure.
- Added repository-level `exportBackup()` and pre-validated `importBackup()` operations.
- Added state integration so imported settings and statistics refresh immediately in the Compose UI.
- Added **Settings → Backup & restore** with manual text export/import dialogs and clear validation feedback.
- Added common tests covering round trips, history sanitization/limits, blank input, future schemas, inconsistent statistics, and history-count mismatches.
- Added `docs/BACKUP.md` with format, compatibility, validation, privacy, and forward-migration rules.
- Updated README, privacy policy, changelog, and roadmap for the new feature.
- Corrected a UI block-structure regression found during pull-request diff review before merge.

### Changed modules and files

- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaBackup.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaRepository.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/state/ArenaState.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaBackupCodecTest.kt`
- `docs/BACKUP.md`
- `docs/ROADMAP.md`
- `README.md`
- `PRIVACY.md`
- `CHANGELOG.md`
- `what_changed.md`

### Validation

Independent Kotlin/JVM checks were run against the new codec/model logic and shared constant linkage. Results:

- schema-v1 round trip with Unicode and `|` in history: **passed**;
- blank backup rejection: **passed**;
- unsupported future schema rejection: **passed**;
- inconsistent statistics rejection: **passed**;
- history-count mismatch rejection: **passed**;
- history sanitization and 30-entry truncation: **passed**;
- Kotlin compilation for the codec/model validation harness: **passed**;
- validation harness result: **`ALL_BACKUP_CHECKS_OK`**.

GitHub PR checks are required before merge. The repository CI covers shared Kotlin tests, Android debug assembly, desktop JVM classes, and Rust tests; CodeQL runs separately for Kotlin/Java security analysis.

### Compatibility and migration notes

- Existing persistence keys remain unchanged: `settings_v1`, `stats_v1`, and `history_v1`.
- No migration is required for existing installations.
- Backup schema versioning is independent from the internal persistence-key version names.
- Schema 1 is intentionally strict; newer schema versions are rejected unless an explicit decoder/migration path is added.
- Imports validate the complete backup before any repository write starts. Platform storage writes are then applied sequentially; they are not a transactional database operation.

### Current limitations

- Backup transfer is manual copy/paste text; no platform file picker is included yet.
- Only schema version 1 is supported.
- Backup data is not encrypted by the app; exported text should be stored only where the user considers appropriate.
- The feature remains deliberately offline and does not provide cloud sync.

### Next exact tasks

After this milestone is validated and merged, the next roadmap work should prioritize:

1. localized string resources beyond English;
2. dedicated UI automation coverage for Android and desktop;
3. optional platform file import/export UX if it can remain permission-minimal and offline-first;
4. optional LAN/private-room play only as an explicit opt-in networking module;
5. signed release automation only after release credentials are configured securely.

### Recent focused commits

- `52189a78d9d8d42d6bfa2e78c0f77947b9b6efe6` — `feat: add versioned arena backup codec`
- `f98dab6527e3af6f37da0805351739e593dc6033` — `test: cover arena backup schema validation`
- `2acfc91512244bfea8347f2e0de63390825c6056` — `feat: integrate backup import and export with repository`
- `10fdeca1a057365cb767b133cf30b7a91ea5ea2a` — `feat: expose backup operations through arena state`
- `b81824ad223de9e934762b4d0fde033eb874b84e` — `feat: add offline backup and restore controls`
- `c72839d18fa18d242983a0c1827b95796a3394aa` — `docs: document backup schema and privacy behavior`
- `fa295d82dbe5452e8578cd35c9794504034a3ea7` — `docs: mark versioned backups complete in roadmap`
- `70ffe00ceb22178ec84ee07531ad51cfaf9f7b63` — `docs: record offline backup feature in changelog`
- `8f88e115887e733351ec8080cda1ee3c136f1cfb` — `docs: add backup and restore to project overview`
- `f795e07cb03cb416b3fa27d3063f1d06c6d128ee` — `docs: clarify privacy behavior for exported backups`
- `abc0f97dd76b6b4fb133985b1bfcc123b9f8ca19` — `fix: correct match mode UI block structure`

## 2026-08-19 — Complete repository baseline

The repository was initialized as a public MIT-licensed RPS Arena project and completed through a dedicated build-validation audit.

### Product implementation

- Added Kotlin Multiplatform + Compose Multiplatform architecture using separate `shared`, `androidApp`, and `desktopApp` modules.
- Added Android and desktop runnable entry points.
- Added classic Rock–Paper–Scissors and optional Lizard–Spock rules.
- Added deterministic seeded CPU logic with Easy, Normal, and Expert presets.
- Added same-device two-player pass-and-play.
- Added Best-of-3, Best-of-5, Endless, Streak, and Tournament configurations.
- Added offline settings, aggregate stats, recent history, achievements, and onboarding.
- Added light/dark/system theme controls and a reduced-motion preference.
- Added Android launcher artwork and repository logo/splash artwork.
- Added optional standalone Rust rules engine with unit tests.

### Engineering and quality

- Added shared rules, CPU, and persistence-codec unit tests.
- Added Android API 26+ support with compile/target SDK 36, using the stable Android SDK available to reproducible CI runners.
- Added Kotlin 2.4.10, Compose Multiplatform 1.11.0, Android Gradle Plugin 9.3.0, and CI-pinned Gradle 9.5.1.
- Added repository hygiene, CI/security automation, dependency updates, verification scripts, issue forms, and pull-request templates.
- Updated the Android Kotlin Multiplatform DSL for the current Android Gradle Plugin.
- Removed obsolete Compose tooling accessors that blocked the Android build.
- Escaped the Kotlin keyword package segment as `` `in`.sanskar... `` in source, tests, and app entry points while preserving the canonical Android/JVM package name `in.sanskar...`.
- Added the required Material 3 experimental opt-in for the top app bar.
- Modernized CI to current checkout/setup actions, Android SDK setup, CodeQL v4, and concurrency cancellation for stale branch runs.

### Documentation and governance

- Added README, MIT license, privacy policy, security policy, support guide, contributing guide, code of conduct, changelog, roadmap, architecture, validation, testing, and release docs.
- Added Buy Me a Coffee funding metadata and highlighted `https://buymeacoffee.com/sanskarIN` in project documentation.
- Added business/support contacts and the required “Made by the Sanskar” credit.
- Aligned README and contributor setup documentation with Android SDK 36.

### Commit email note

The repository documents `sanskarin@outlook.in` as the owner commit email in `.mailmap` and `CONTRIBUTING.md`. GitHub connector/API commits use the identity attached to the authenticated GitHub integration and do not expose an author-email override field; local Git commits should use the documented email.

### Validation audit

Validation was performed on pull request #9 (`validation/build-audit`) and merged into `main` with a merge commit so the focused commits remain preserved.

The final audited head passed all required checks before merge:

- shared Kotlin tests: **passed**;
- Android debug assembly: **passed**;
- desktop JVM classes: **passed**;
- optional Rust engine tests: **passed**;
- CodeQL Kotlin/Java build and analysis: **passed**.

The build audit also fixed the Android SDK setup, stable SDK target, AGP 9 KMP DSL, obsolete Compose accessors, Kotlin package-keyword syntax, and Material 3 opt-in issues discovered by CI. The resulting production baseline was merged only after CI and CodeQL completed successfully.

### Repository checkpoint

- Validation PR: `#9` — merged.
- Validated PR head: `4c2e93330055986d6b87ab002a97b7929c5a2275`.
- Validation merge commit: `4b19247605ce7a94a8e6c819a63f6cd300d00d94`.
- Default branch: `main`.
- License: MIT.
- Privacy default: offline-first, no account, no analytics SDK, no ads SDK, and no Android internet permission.
