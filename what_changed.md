# What Changed

## Current milestone — v1.1.0 product completion and release hardening

Date: 2026-08-19
Repository: `sanskarIN/rps-arena`
Working PR: `#11` (`feature/phase-7-completion` -> `main`)
License: MIT
Primary product posture: offline-first; no account, analytics SDK, ads SDK, cloud model, or Android internet permission required for primary gameplay.

This file is the repository handoff log. It records implementation details that would otherwise have been repeated in chat.

## v1.1.0 completed work

### Gameplay and match controls

- Added configurable round timers: Off, 5s, 10s, 20s, 30s, and 60s.
- Added typed timeout reasons so timeout rounds are distinguishable from gesture-played rounds.
- CPU timeout awards the round to the CPU/opponent.
- Same-device two-player timeout awards the round to the player who completed the previous/available turn correctly.
- Timeout rounds update match score, aggregate statistics, recent trends, and history consistently.
- Added editable integer challenge seed controls so deterministic CPU matches can be replayed from the UI.
- Preserved Classic and Lizard–Spock rules, CPU/local modes, all existing match formats, and deterministic CPU difficulty behavior.

### Local profile, statistics, history, and settings

- Added a local player display-name preference.
- Player names are newline-sanitized, trimmed, bounded to 32 characters, and default safely when blank.
- Added a recent 10-round W/L/D trend model derived from bounded local history.
- History remains capped at 30 records and now sanitizes line breaks and line length before persistence.
- Added English and Hindi shared UI string catalogs for the core product surfaces.
- Added versioned `settings_v2` persistence with transparent migration from compatible `settings_v1` records.
- Added a confirmed local-data reset flow.

### Backup/import

- Added plain-text versioned backup format `RPS_ARENA_BACKUP|1`.
- Backup includes local settings, aggregate statistics, and up to 30 recent history records.
- Added import validation before writes.
- Import rejects oversized payloads, excessive line counts, malformed records, duplicate settings/stat records, unknown record types, invalid settings, invalid statistics invariants, and invalid state relationships.
- Backup field escaping protects delimiter/newline structure.
- Invalid imports do not partially replace otherwise valid local data.
- Successful import refreshes in-memory settings/statistics and resets current match state safely.

### Private-room multiplayer architecture

- Added transport-neutral `PrivateRoomGateway` and `PrivateRoomSession` contracts.
- Added six-character unambiguous `RoomCode` validation.
- Added room participant roles and typed room events.
- Added deterministic two-player `InMemoryPrivateRoomGateway` reference adapter for tests and same-process development.
- Reference sessions reject forged participant IDs and enforce a two-participant maximum.
- Current adapter performs no network I/O and does not change the Android permission model.
- A real LAN adapter remains an explicit optional future transport, not a hidden or mandatory dependency.

### UI, accessibility, and design

- Added timer progress/status feedback and explicit timeout result text.
- Added reduced-motion-aware result behavior: static results when reduced motion is enabled, crossfade otherwise.
- Added local player-name editing, language controls, backup/import controls, seed controls, and destructive reset confirmation.
- Added recent-trend display in Statistics.
- Added branded Material 3 light/dark color schemes and reusable layout tokens.
- Added documented keyboard, TalkBack, text-scaling, contrast, timer, reduced-motion, and destructive-action accessibility checks.
- Primary gesture controls keep text labels plus emoji and large minimum targets so state is not communicated by color alone.

### Testing added

Shared/unit coverage now includes:

- timer allowed-value and match-target invariants;
- CPU timeout scoring;
- local two-player timeout scoring;
- timer-disabled no-op behavior;
- replayable seeded CPU behavior through `ArenaState`;
- backup round-trip preservation;
- malformed/unknown backup rejection without destructive writes;
- legacy settings migration;
- recent trend calculation;
- invalid statistics invariant rejection;
- player-name sanitization and bounds;
- bounded/sanitized history behavior;
- private-room code validation;
- private-room sender validation;
- private-room event exchange;
- private-room two-participant limit.

Compose desktop UI smoke tests now cover:

- onboarding -> home -> primary Play navigation;
- primary Rock/Paper/Scissors controls;
- English -> Hindi settings copy update;
- backup/import controls;
- destructive reset confirmation controls.

### CI, automation, and release engineering

- Added `scripts/check_format.py` to reject trailing whitespace, missing final newlines, and invalid UTF-8 in repository text files.
- Added `scripts/check_version.py` to keep Android `versionName`, desktop `packageVersion`, and About UI version synchronized.
- Updated Unix and PowerShell verification scripts to mirror repository quality gates.
- Added formatting and version checks to CI.
- Added Android `lintDebug` to CI.
- Existing shared tests, Android debug assembly, desktop JVM classes, Rust tests, and CodeQL remain required validation layers.
- Added Compose desktop UI test runtime to the shared KMP test configuration.
- Added a tag/manual release workflow that can build/test and upload unsigned Android APK, Linux `.deb`, and Rust `.crate` artifacts.
- Tagged publishing generates SHA-256 checksums and uses GitHub's release mechanism without embedding repository secrets.
- Added generated release-note category configuration.
- Android signing, Windows signing, and Apple signing/notarization intentionally remain outside the public repository until authorized credentials are configured securely.

### Versioning

- Android: `versionCode = 2`, `versionName = "1.1.0"`.
- Desktop: `packageVersion = "1.1.0"`.
- About UI: `Version: 1.1.0`.
- Cross-platform version synchronization is now checked automatically.

### Documentation completed/expanded

- `README.md` — full product surface, preview artwork, platform status, quick start, privacy, backup, timers, private-room architecture, quality gates, accessibility, release, support, funding.
- `ROADMAP.md` — canonical v1.0/v1.1 roadmap plus explicit optional/platform-dependent follow-ups.
- `CHANGELOG.md` — complete v1.1.0 feature/security/reliability record.
- `CONTRIBUTING.md` — setup, commit identity, granular commit rules, validation, compatibility, networking, localization, accessibility, security.
- `PRIVACY.md` — local profile, backup, retention, reset, tracking/network boundaries.
- `SECURITY.md` — supported versions, reporting, backup/import security, optional networking, dependency/CI posture, secrets policy.
- `SUPPORT.md` — support workflow, diagnostics, safe-reporting guidance, contacts, funding.
- `docs/setup.md` — tool installation/setup and verification entry point.
- `docs/development.md` — architecture boundaries and day-to-day workflow.
- `docs/architecture.md` — modules, shared layers, persistence, migration, timers, determinism, private-room boundary.
- `docs/testing.md` — unit, persistence, protocol, desktop UI, Android build/lint, manual/release checks.
- `docs/validation.md` — executable CI/release validation contract.
- `docs/release.md` — version locations, release gate, unsigned artifacts, external signing, rollback.
- `docs/troubleshooting.md` — JDK/SDK/Gradle/Desktop/backup/timer/private-room/CI troubleshooting.
- `docs/accessibility.md` — manual accessibility and reduced-motion/timer policy.
- `docs/performance.md` — bounded-data and performance budgets/measurement workflow.
- `docs/github-settings.md` — branch rules, Actions permissions, security features, Discussions, labels, milestones, metadata, release settings.
- `docs/adr/0001-offline-first-kmp.md` — offline-first KMP architecture decision.
- `docs/adr/0002-private-room-boundary.md` — optional transport boundary/security decision.
- Legacy uppercase architecture/testing/release/validation documents were retired in favor of lowercase canonical files; `docs/ROADMAP.md` points to the root canonical roadmap.

### GitHub repository workflow/governance

- Added issue-template configuration that directs sensitive security reports to the security policy and support questions to the support guide.
- Expanded the pull-request template with formatting, version, shared/Desktop UI, Android lint/build, desktop, Rust, accessibility, migration, backup, privacy, networking, and release checks.
- Added repository-settings guidance for branch protection/rulesets, security features, release settings, Discussions, labels, and milestones.
- Existing Dependabot coverage remains configured for Gradle, Cargo, and GitHub Actions.
- Existing CodeQL security scanning remains configured for Kotlin/Java.

## Files/modules added or changed in this milestone

Key product/code paths:

- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/model/GameModels.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaRepository.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/BackupModels.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/state/ArenaState.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/network/PrivateRoom.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaStrings.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaDesign.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaTheme.kt`
- `shared/build.gradle.kts`
- `gradle/libs.versions.toml`
- `androidApp/build.gradle.kts`
- `desktopApp/build.gradle.kts`

Key test paths:

- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryBackupTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryValidationTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStateTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/MatchConfigTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/PrivateRoomTest.kt`
- `shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt`

Key automation paths:

- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
- `.github/release.yml`
- `.github/ISSUE_TEMPLATE/config.yml`
- `.github/pull_request_template.md`
- `scripts/check_format.py`
- `scripts/check_version.py`
- `scripts/verify.sh`
- `scripts/verify.ps1`

## Validation commands

Repository verification target:

```bash
python3 scripts/check_format.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Dedicated desktop UI command:

```bash
gradle :shared:desktopTest --stacktrace
```

Release workflow additionally runs Android release lint/assembly, Linux desktop `.deb` packaging, Rust packaging, artifact upload, and checksum generation.

## Validation status

### Established v1.0 baseline

The previous validation PR `#9` passed before merge:

- shared Kotlin tests: passed;
- Android debug assembly: passed;
- desktop JVM classes: passed;
- optional Rust engine tests: passed;
- CodeQL Kotlin/Java analysis: passed.

Validated v1.0 PR head: `4c2e93330055986d6b87ab002a97b7929c5a2275`.
Validation merge commit: `4b19247605ce7a94a8e6c819a63f6cd300d00d94`.

### v1.1 candidate

Pull request `#11` is the v1.1 validation gate. At the time this handoff update was committed, the latest CI/CodeQL run had been queued but had not yet produced a final conclusion. The PR must not be merged until the final candidate head is green. Any failure discovered by CI must be fixed in a separate focused commit and recorded here before release.

## Migrations and compatibility

### Settings migration

- Old key: `settings_v1` with seven boolean fields.
- New key: `settings_v2` with the existing booleans plus escaped local player name and language.
- When `settings_v2` is absent and a valid `settings_v1` exists, the repository decodes the legacy record and immediately saves the migrated v2 representation.
- Invalid legacy data falls back to safe defaults rather than propagating corrupt state.

### Backup schema

- Current header: `RPS_ARENA_BACKUP|1`.
- The importer is strict and transactional at the repository level: all recognized settings/stat records are validated before writes.
- Future incompatible backup changes require a new schema/header and migration guidance rather than silently reinterpreting v1 data.

### Statistics/history

Existing `stats_v1` and `history_v1` storage keys are retained. New validation rejects internally inconsistent statistics and keeps history bounded.

## Known limitations / intentional boundaries

- A real LAN socket/discovery transport is not shipped. Only the transport-neutral private-room architecture and no-network in-memory reference adapter are present.
- Android device/emulator instrumentation UI tests are not part of the current CI runner; desktop Compose UI smoke automation and manual Android accessibility/device checks cover the current feasible baseline.
- Store signing/notarization is not automated with real credentials because no authorized signing secrets are stored in the repository. Public release automation remains unsigned and reproducible.
- iOS packaging is not part of the current release gate.
- Sound/haptics preferences are persisted UI preferences; platform-specific sound/haptic effect engines are not introduced merely to add dependencies or permissions without a product requirement.

## Open issues

No open repository issues were found during the start-of-milestone audit. CI findings on PR `#11`, if any, take priority over optional roadmap work.

## Next optional tasks after a green v1.1 merge

1. Add a real opt-in LAN adapter only after explicit product/security/privacy approval.
2. Add Android emulator/device Compose instrumentation to CI when the runner/cost/stability trade-off is accepted.
3. Configure signed Android/Desktop release automation only after authorized secrets are provisioned outside Git.
4. Evaluate iOS packaging as a separate milestone.

## v1.1.0 release-notes draft

RPS Arena 1.1.0 expands the offline Android/Desktop arena with optional round timers, replayable CPU challenge seeds, local profile naming, recent trends, versioned local backup/import, Hindi core UI support, improved reduced-motion/result feedback, and a transport-neutral private-room architecture. Persistence and imported data are more strictly validated, regression coverage is substantially broader, desktop Compose UI smoke tests are included, CI now enforces formatting/version consistency/Android lint, and tagged builds can generate unsigned public Android/Linux/Rust artifacts with checksums. Primary gameplay remains account-free, telemetry-free, ad-free, cloud-free, and offline-first.

## Recent milestone commits

This milestone intentionally uses many small, cohesive commits. Representative recent commits include:

- `5d4b7b7` — `fix: remove trailing markdown whitespace from readme`
- `14c1286` — `chore: expand pull request quality and compatibility checklist`
- `b39b63e` — `chore: guide issue creation to security and support paths`
- `1d5566b` — `docs: synchronize changelog with final quality additions`
- `7a91434` — `docs: document desktop UI and validation edge coverage`
- `a670c91` — `docs: record desktop UI automation milestone`
- `b8218f8` — `test: add desktop UI smoke coverage for primary journeys`
- `f6af63b` — `test: enable Compose desktop UI test runtime`
- `bece141` — `ui: define calm branded light and dark theme tokens`
- `61b801d` — `ui: add shared responsive layout design tokens`
- `6c1be76` — `chore: configure categorized GitHub release notes`
- `c0e4771` — `docs: harden security policy for backups networking and releases`
- `2f6c8f5` — `test: verify replayable seeded CPU matches through state`
- `ff64f03` — `test: cover match timer and win target invariants`
- `e866f97` — `test: harden persistence validation edge cases`
- `47b7934` — `ci: add reproducible tagged release artifact workflow`
- `43ab6cb` — `ci: verify synchronized release versions`
- `b17753d` — `ci: enforce formatting and Android lint gates`
- `0c71c75` — `fix: use valid unambiguous private room test code`
- `19874c9` — `feat: add private room multiplayer transport contract`
- `1f68d0e` — `feat: add timed rounds backups trends profiles and bilingual UI`
- `805bf22` — `feat: add timeout backup and recent trend state flows`
- `06b269a` — `feat: add versioned backup migration and trend persistence`
- `64365e6` — `feat: extend game model for timers profiles and localization`

## Commit identity note

The repository documents `sanskarin@outlook.in` as the owner commit email in `.mailmap`, `CONTRIBUTING.md`, and setup guidance. The GitHub repository commits created through the authenticated integration are attributed to the repository owner's configured GitHub identity; the observed Git commit author for this work is `Sanskar <sanskarin@outlook.in>`.

**Made by the Sanskar.**
