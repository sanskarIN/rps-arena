# What Changed

## Current milestone — v1.1.0 product completion and release hardening

Date: 2026-08-19
Repository: `sanskarIN/rps-arena`
Working PR: `#11` (`feature/phase-7-completion` -> `main`)
License: MIT
Primary product posture: offline-first; no account, analytics SDK, ads SDK, cloud model, or Android internet permission required for primary gameplay.

This file is the detailed repository handoff log. It intentionally contains implementation, migration, verification, limitation, documentation, and release information that would otherwise have been repeated in chat.

## Milestone scope

Version 1.1.0 completes the remaining practical product requirements that can be implemented safely in the public repository without private signing credentials, mandatory network infrastructure, or a paid/cloud backend.

The milestone covers:

- timed and replayable matches;
- stronger offline persistence and import/export;
- local profile/settings improvements;
- English/Hindi product localization;
- responsive and reduced-motion UI behavior;
- private-room multiplayer architecture and integrity boundaries;
- broader unit/protocol/UI test coverage;
- stricter CI/release automation;
- complete setup/development/testing/security/privacy/release documentation;
- exhaustive file-by-file repository documentation;
- GitHub governance and release-maintenance guidance.

## Deep documentation completion phase

The repository documentation was expanded beyond overview guides into a maintainable, file-complete reference set.

### Documentation coverage is now enforceable

Added:

```text
scripts/check_docs_coverage.py
```

The script:

1. executes `git ls-files -z` to obtain the authoritative list of Git-tracked paths;
2. reads `docs/repository-file-reference.md`;
3. requires every tracked path to appear exactly in backticks in that file;
4. prints every missing path and exits non-zero when coverage is incomplete.

This means a future source/config/resource/test/workflow/documentation file cannot be silently added without also being represented in the exhaustive repository file reference.

The checker validates path coverage. Human review still evaluates whether each explanation is accurate, useful, and sufficiently deep.

### Coverage gate integration

Documentation coverage is now included in:

- `.github/workflows/ci.yml`;
- `scripts/verify.sh`;
- `scripts/verify.ps1`;
- `.github/pull_request_template.md`;
- `CONTRIBUTING.md`;
- `docs/testing.md`;
- `docs/validation.md`;
- `docs/setup.md`;
- `README.md`;
- `docs/ci-cd.md`;
- `docs/command-reference.md`.

A change that adds/renames a tracked file without updating the exhaustive reference should fail the primary CI gate.

### Release-workflow boundary

An attempt was made to duplicate the documentation-coverage step directly inside `.github/workflows/release.yml`. The connected write was blocked by the connector safety layer, and no bypass or alternate unsafe write path was used.

The checked-in release workflow therefore remains unchanged in this respect. Release policy requires version tags to be created from validated `main`, whose normal CI includes the documentation-coverage gate.

This distinction is documented accurately in `docs/ci-cd.md` and `docs/validation.md` rather than claiming the tag workflow contains a step that was not actually committed.

### New deep documentation set

Added:

- `docs/documentation-index.md` — role/goal-based navigation across the documentation set;
- `docs/command-reference.md` — detailed meaning, prerequisites, side effects, diagnostics, and safety of repository Python/Gradle/Cargo/Git/tag commands;
- `docs/toolchain.md` — JDK/Gradle/Android SDK/IDE/Python/Git/Rust installation, inspection, environment variables, and safe upgrade workflow;
- `docs/build-system.md` — root Gradle/settings/properties/version catalog, modules, source sets, dependencies, tasks, outputs, packaging, and current no-Gradle-Wrapper architecture;
- `docs/domain-and-gameplay.md` — gestures, rules, CPU probabilities, deterministic seed behavior, match modes, state transitions, timers, scores, streaks, achievements, history grammar, and invariants;
- `docs/storage-and-backup.md` — PlatformStore implementations, exact keys/codecs, settings migration, stat invariants, history format, backup grammar/escaping/limits/import/reset/privacy compatibility;
- `docs/localization.md` — English/Hindi catalogs, enum-keyed labels, canonical vs localized data, achievements, persisted language compatibility, RTL/additional-language guidance;
- `docs/private-room-protocol.md` — room-code/session/event semantics, participant/lifecycle authority, current no-network adapter, and future LAN fairness/security/privacy/versioning/failure requirements;
- `docs/android-platform.md` — every Android build/source/manifest/resource/storage file, SDK/versioning, adaptive icons/theme, APK behavior, signing and offline checks;
- `docs/desktop-platform.md` — every desktop launcher/build/storage file, JVM behavior, Java Preferences, UI tests, and host-dependent DEB/MSI/DMG packaging/signing boundaries;
- `docs/rust-engine.md` — every optional Rust crate file, rule parity, Cargo tests/package, lock/dependency policy, FFI/WASM/security boundaries;
- `docs/test-catalog.md` — every tracked Kotlin/Compose automated test file and its exact regression responsibility/assertions;
- `docs/ci-cd.md` — every `.github` automation/configuration file, triggers, permissions, concurrency, CI/CodeQL/release jobs, artifacts/checksums, Dependabot, issue/PR/funding configuration;
- `docs/maintenance.md` — long-term versions/dependencies/gameplay/storage/language/network/platform/test/docs/release/secret-incident maintenance playbook;
- `docs/branding-assets.md` — root SVG, Android adaptive-icon, platform-theme/shared-theme ownership and rebranding/accessibility/export checklist;
- `docs/glossary.md` — project/build/KMP/Android/Desktop/Rust/CI/security/release terminology and repository-specific identifiers;
- `docs/repository-file-reference.md` — exhaustive file-by-file description of every Git-tracked repository file, including itself.

### Existing documentation connected to the deep set

Expanded/relinked:

- `README.md` — now exposes the complete documentation set, documents the no-Gradle-Wrapper setup, adds documentation coverage to quick-start/quality gates, and distinguishes project baseline versions from global-latest claims;
- `docs/setup.md` — adds Python/documentation/version prechecks, local Gradle explanation, deep documentation links, and generated/machine-specific file guidance;
- `CONTRIBUTING.md` — requires documentation coverage and updating the exhaustive file reference for every tracked-file addition/rename/removal;
- `.github/pull_request_template.md` — adds documentation-coverage command and every-file reference checklist;
- `docs/testing.md` — makes format/docs/version source gates explicit before compilation and links the per-test catalog;
- `docs/validation.md` — defines the every-file coverage gate and exact-head validation rule;
- `CHANGELOG.md` — records the exhaustive documentation/coverage milestone;
- `docs/ci-cd.md` — reflects the actual CI documentation gate and the unchanged release workflow.

### Every-file documentation policy

For every future tracked file addition, rename, or deletion:

1. update `docs/repository-file-reference.md` in the same change;
2. explain ownership/purpose/compatibility implications, not only the filename;
3. run `python3 scripts/check_docs_coverage.py`;
4. update `docs/documentation-index.md` when a new subject-area guide is added;
5. update `what_changed.md` for milestone-level work.

## Gameplay and match controls

### Round timers

- Added configurable round timers.
- Supported values are `Off`, `5s`, `10s`, `20s`, `30s`, and `60s`.
- `MatchConfig.ALLOWED_TIMER_SECONDS` is the single validated set used by the model and UI.
- Unsupported timer values are rejected at model construction.
- `0` means timer disabled.
- The countdown is visible as text plus progress feedback.
- Each local two-player turn receives a fresh countdown.
- The timer does not run after a finite match has finished.

### Timeout outcomes

- Added typed `RoundEndReason` values so gesture-played and timed-out rounds are distinguishable.
- CPU mode: Player 1 timeout awards the round to Player 2/CPU.
- Same-device local mode:
  - timeout before Player 1 selects awards the round to Player 2;
  - timeout after Player 1 locks a move and while Player 2 is choosing awards the round to Player 1.
- Timeout rounds update:
  - match score;
  - aggregate statistics;
  - streak state;
  - recent trend calculations;
  - recent history.
- Timeout history is explicit rather than pretending a gesture was selected.

### Replayable deterministic challenge seed

- The gameplay screen now exposes the CPU challenge seed.
- Players can enter an integer seed and apply it before replaying a match.
- Resetting/reconfiguring a match rebuilds `CpuStrategy` from the selected seed.
- Given the same seed, ruleset, difficulty, and player move history, the CPU decision sequence remains replayable.
- Added state-level regression coverage confirming two matches with the same seed and moves produce the same CPU gesture/outcome sequence.

### Existing modes preserved

The milestone keeps the existing supported modes intact:

- Classic Rock–Paper–Scissors;
- Rock–Paper–Scissors–Lizard–Spock;
- CPU opponent;
- same-device two-player pass-and-play;
- Best of 3;
- Best of 5;
- Endless;
- Streak;
- Tournament.

## Local profile, settings, statistics, and history

### Local player profile

- Added a local Player 1 display-name preference.
- Names are sanitized before persistence:
  - CR/LF line breaks become spaces;
  - surrounding whitespace is trimmed;
  - maximum length is 32 characters;
  - blank values safely fall back to `Player 1`.
- The saved local name appears in the home/score experience.

### Recent trend

- Added `ArenaTrend`.
- Statistics now include a recent 10-round W/L/D summary.
- Trend calculation reads only bounded recent local history.

### History hardening

- History remains capped at 30 entries.
- New history writes are newline-sanitized.
- Individual history lines are length-bounded before persistence.
- History remains local/offline.
- Stored canonical round summaries remain language-neutral/English-compatible data while the visible UI can localize known gesture/result forms at render time.

### Settings migration

- New settings key: `settings_v2`.
- Legacy key: `settings_v1`.
- `settings_v2` adds:
  - local player name;
  - interface language.
- If v2 is absent and a valid v1 record exists, the repository decodes the legacy record and immediately stores a v2 representation.
- Invalid persisted settings fall back to safe defaults rather than propagating corrupt state.

## Versioned backup/import

### Backup schema

Current header:

```text
RPS_ARENA_BACKUP|1
```

The backup contains:

- local settings;
- aggregate statistics;
- up to 30 recent history records.

### Export behavior

- Export is plain text so the user can copy/store it without cloud infrastructure.
- Delimiter/newline-sensitive values are escaped before serialization.
- The app does not upload the backup.

### Import validation

Import validates the complete payload before replacing validated state.

The importer rejects:

- oversized backup text;
- too many records;
- missing/unsupported header;
- malformed record structure;
- duplicate settings record;
- duplicate statistics record;
- unknown record types;
- invalid settings fields;
- invalid language values;
- invalid integer statistics;
- negative statistics;
- statistics where `roundsPlayed != wins + losses + draws`;
- impossible streak relationships;
- missing required settings/statistics records.

History import remains bounded and sanitized.

### Data-reset behavior

- Added an explicit local-data reset control.
- Reset requires a second confirmation action.
- It clears local statistics, recent history, player preferences, and current backup text.
- Onboarding completion is preserved for convenience.

## English and Hindi localization

The initial bilingual foundation has been expanded so Hindi is no longer limited to basic navigation.

### Localized core areas

- onboarding;
- home navigation;
- Play/Settings/Stats/History/Achievements/About headings;
- opponent labels;
- ruleset labels;
- CPU difficulty labels;
- match-mode labels;
- timer labels;
- seed explanation/apply action;
- Rock/Paper/Scissors/Lizard/Spock gesture labels;
- turn instructions;
- timeout feedback;
- round outcomes;
- visible recent-history rendering for recognized round records;
- statistics labels;
- appearance/accessibility/data/privacy settings;
- player-name controls;
- backup/import/reset controls;
- common backup validation/result messages;
- About version/license labels;
- achievement titles/descriptions.

### Localization architecture

- Core strings live in `ArenaStrings.kt`.
- Achievement copy lives in `AchievementStrings.kt`.
- Gesture/difficulty/match-mode copy is enum-keyed rather than recreated through display-name parsing.
- Stored game rules and enum identities remain language-independent.
- `APP_VERSION` and `APP_LICENSE` live in `AppMetadata.kt` so localization does not affect release metadata.

## UI, design, responsiveness, and accessibility

### Design foundation

- Added branded Material 3 light and dark color schemes.
- Added consistent rounded shape tokens.
- Added reusable layout tokens in `ArenaDesign.kt`.
- Primary content is constrained to a sensible desktop maximum width while remaining full-width on smaller devices.

### Narrow-screen configuration controls

- Match-mode and timer choices can exceed a phone-width row.
- Configuration choices now use wrapping `FlowRow` layout.
- This keeps all options reachable on narrow screens instead of clipping them off-screen.

### Reduced motion

- Result content uses a crossfade during normal motion mode.
- Reduced-motion mode renders the result without that animated transition.
- Timers can also be turned off completely.

### Accessibility baseline

- Important controls use visible text labels.
- Gesture controls combine emoji with text instead of communicating by color alone.
- Gesture controls retain large minimum touch targets.
- Score/timer/result/timeout states have text representations.
- Light/dark/system theme choices remain available.
- Desktop uses standard Compose focus/keyboard behavior.
- Manual TalkBack, text-scaling, contrast, timer, reduced-motion, and destructive-action checks are documented in `docs/accessibility.md`.

## Private-room multiplayer architecture

### Transport boundary

Added shared transport-neutral interfaces:

- `PrivateRoomGateway`;
- `PrivateRoomSession`;
- `RoomCode`;
- `RoomParticipant` / `RoomRole`;
- typed `RoomEvent` messages.

The current concrete implementation is `InMemoryPrivateRoomGateway`.

### Reference adapter behavior

- No network I/O.
- No Android network permission.
- Deterministic/same-process behavior for tests and architecture development.
- Maximum two participants.
- Host/guest roles.
- Six-character room-code validation.
- Ambiguous room-code characters such as `I`, `O`, `0`, and `1` are rejected.
- Participant display names are sanitized and bounded.

### Protocol integrity hardening

Client sessions may send only client-owned gameplay events.

The reference transport now:

- rejects forged participant IDs;
- rejects gesture events whose round number is not positive;
- rejects client attempts to send gateway-owned `ParticipantJoined` events;
- rejects client attempts to send gateway-owned `ParticipantLeft` events;
- broadcasts a leave lifecycle event when a real session closes;
- makes repeated `close()` calls idempotent so leave is not broadcast twice;
- rejects sends from closed/detached sessions.

### Future LAN boundary

A real LAN adapter remains optional and deliberately separate.

Any future network adapter must:

- remain behind `PrivateRoomGateway`;
- require explicit user intent;
- validate inputs/participants/messages;
- stop network activity when the room closes;
- avoid mandatory cloud infrastructure;
- preserve fully offline primary gameplay;
- receive a privacy/security review before release.

## Test coverage added/expanded

### Shared business and persistence tests

Coverage includes:

- timer allowed values;
- invalid timer rejection;
- finite match win targets;
- CPU timeout scoring;
- local Player 1 timeout scoring;
- local Player 2 timeout scoring;
- disabled-timer no-op behavior;
- deterministic seeded CPU replay through `ArenaState`;
- settings codec round trip;
- statistics codec round trip;
- legacy settings migration;
- invalid statistics invariant rejection;
- player-name sanitization;
- player-name length bounds;
- blank-name fallback;
- bounded/sanitized history;
- backup round trip;
- malformed backup non-destructive rejection;
- unknown backup record rejection;
- recent trend calculation;
- successful backup restore refreshing in-memory state.

### Localization tests

Coverage includes:

- English canonical gesture labels;
- Hindi gesture labels;
- Hindi CPU difficulty labels;
- Hindi match-mode labels;
- shared semantic version shape;
- localized achievement copy for every known achievement;
- safe fallback achievement copy for future/unknown IDs.

### Private-room tests

Coverage includes:

- valid room-code normalization;
- ambiguous/malformed room-code rejection;
- host/guest join event;
- valid gesture event exchange;
- forged participant rejection;
- two-participant room limit;
- invalid round rejection;
- lifecycle-event authority rejection;
- close-event delivery;
- repeated close idempotency.

### Compose desktop UI smoke tests

The desktop UI test suite now covers:

- onboarding -> Home;
- Home -> Play;
- visible classic gesture controls;
- Settings English -> Hindi switch;
- Hindi Settings rendering;
- Hindi gameplay gesture labels;
- Hindi Achievements title/description rendering;
- backup/import controls;
- destructive local-reset confirmation/cancel controls.

## CI and repository verification

### CI gates

`.github/workflows/ci.yml` now validates:

1. repository formatting;
2. exhaustive tracked-file documentation coverage;
3. cross-platform version consistency;
4. shared Kotlin tests, including desktop UI tests;
5. Android lint;
6. Android debug assembly;
7. desktop JVM compilation;
8. Rust tests.

Separate CodeQL automation remains enabled for Kotlin/Java security analysis.

### Formatting checker

`scripts/check_format.py` checks repository text files for:

- invalid UTF-8;
- missing final newline;
- accidental trailing spaces/tabs.

The standard two-space Markdown hard-break syntax is explicitly allowed so valid Markdown formatting does not become a false CI failure.

### Documentation coverage checker

`scripts/check_docs_coverage.py` checks:

- the current Git-tracked path list from `git ls-files -z`;
- exact path presence inside `docs/repository-file-reference.md`.

If one or more paths are missing, it prints the missing list and exits with failure. It does not mutate files.

### Version checker

`scripts/check_version.py` checks that:

- Android `versionName`;
- desktop `packageVersion`;
- shared `APP_VERSION`

all match.

It also verifies that the About screen actually renders `$APP_VERSION` rather than duplicating a localized/hard-coded version string.

### Local verification scripts

Updated:

- `scripts/verify.sh`;
- `scripts/verify.ps1`.

They now run formatting, documentation coverage, version consistency, Kotlin/Android/Desktop validation, and optional Rust tests when Cargo is available.

## Release engineering

### Version

- Android `versionCode = 2`.
- Android `versionName = "1.1.0"`.
- Desktop `packageVersion = "1.1.0"`.
- Shared `APP_VERSION = "1.1.0"`.
- Shared `APP_LICENSE = "MIT"`.

### Release workflow

Added `.github/workflows/release.yml`.

For manual/tag validation it can:

- run repository format/version checks;
- run shared tests;
- run Android release lint;
- build an unsigned Android release APK;
- compile/package Linux desktop `.deb` output;
- run/package the optional Rust crate;
- upload build artifacts.

For validated version tags it additionally:

- downloads the generated artifacts;
- generates SHA-256 checksums;
- creates a GitHub Release with generated notes.

The release workflow does not currently duplicate the new documentation-coverage command. Normal release policy therefore requires tagging a validated `main` commit whose CI already passed the documentation gate.

### Signing boundary

The public repository does not contain:

- Android keystores/passwords;
- Windows signing certificates/private keys;
- Apple signing/notarization credentials;
- store tokens;
- API secrets.

Signed store/notarized automation remains a separate credential-dependent task after authorized secrets are provisioned outside Git.

## Documentation completed/expanded

### Root documentation

- `README.md`
- `ROADMAP.md`
- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `PRIVACY.md`
- `SECURITY.md`
- `SUPPORT.md`
- `what_changed.md`

### Deep development/product documentation

- `docs/documentation-index.md`
- `docs/setup.md`
- `docs/toolchain.md`
- `docs/command-reference.md`
- `docs/build-system.md`
- `docs/development.md`
- `docs/architecture.md`
- `docs/domain-and-gameplay.md`
- `docs/storage-and-backup.md`
- `docs/localization.md`
- `docs/private-room-protocol.md`
- `docs/android-platform.md`
- `docs/desktop-platform.md`
- `docs/rust-engine.md`
- `docs/branding-assets.md`
- `docs/testing.md`
- `docs/test-catalog.md`
- `docs/validation.md`
- `docs/accessibility.md`
- `docs/performance.md`
- `docs/troubleshooting.md`
- `docs/ci-cd.md`
- `docs/release.md`
- `docs/github-settings.md`
- `docs/maintenance.md`
- `docs/glossary.md`
- `docs/repository-file-reference.md`
- `docs/ROADMAP.md` pointer to canonical root roadmap.

### Architecture decisions

- `docs/adr/0001-offline-first-kmp.md`
- `docs/adr/0002-private-room-boundary.md`

Legacy uppercase duplicate architecture/testing/release/validation documents were removed in favor of lowercase canonical files.

## GitHub governance and maintenance

Added/expanded:

- issue-template configuration;
- pull-request quality and documentation-coverage checklist;
- generated release-note category configuration;
- Dependabot configuration remains active for Gradle, Cargo, and GitHub Actions;
- CodeQL remains configured for Kotlin/Java;
- documented branch/ruleset guidance;
- documented recommended security settings;
- documented Discussions categories;
- documented recommended labels/milestones;
- documented merge policy that preserves meaningful granular commits;
- CI-enforced requirement that every tracked file be represented in the exhaustive file reference.

Security-sensitive issue creation is directed to `SECURITY.md` rather than a public blank issue.

## Key product/code paths changed

- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/model/GameModels.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaRepository.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/BackupModels.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/state/ArenaState.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/network/PrivateRoom.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AppMetadata.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaStrings.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AchievementStrings.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaDesign.kt`
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaTheme.kt`
- `shared/build.gradle.kts`
- `gradle/libs.versions.toml`
- `androidApp/build.gradle.kts`
- `desktopApp/build.gradle.kts`

## Key tests changed/added

- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryBackupTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryValidationTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStateTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/MatchConfigTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/PrivateRoomTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStringsTest.kt`
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/AchievementStringsTest.kt`
- `shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt`

## Key automation changed/added

- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
- `.github/release.yml`
- `.github/ISSUE_TEMPLATE/config.yml`
- `.github/pull_request_template.md`
- `scripts/check_format.py`
- `scripts/check_docs_coverage.py`
- `scripts/check_version.py`
- `scripts/verify.sh`
- `scripts/verify.ps1`

## Verification commands

Repository verification target:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_coverage.py
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

Release validation additionally exercises Android release lint/assembly, Linux desktop packaging, Rust packaging, artifact upload, and checksum generation.

## Validation status

### Established v1.0 baseline

Previous validation PR `#9` passed before merge:

- shared Kotlin tests: passed;
- Android debug assembly: passed;
- desktop JVM classes: passed;
- optional Rust engine tests: passed;
- CodeQL Kotlin/Java analysis: passed.

Validated v1.0 PR head: `4c2e93330055986d6b87ab002a97b7929c5a2275`.
Validation merge commit: `4b19247605ce7a94a8e6c819a63f6cd300d00d94`.

### v1.1 candidate

Pull request `#11` is the v1.1 validation gate.

This commit is intended to be the final documentation handoff/freeze for the current branch. Required CI and CodeQL must run on the exact post-handoff PR head before merge.

Repeated implementation/documentation commits intentionally restarted/cancelled obsolete workflow runs through `cancel-in-progress: true`; earlier green or queued SHAs do not validate the final candidate.

The final CI must now additionally prove `scripts/check_docs_coverage.py` passes, which mechanically confirms that every Git-tracked path is represented in `docs/repository-file-reference.md`.

If a required job fails, fix only the actual failure in a focused commit, update this handoff when materially necessary, and revalidate the new exact head. Do not bypass the gate.

## Migrations and compatibility

### Settings

Old record:

```text
settings_v1
```

New record:

```text
settings_v2
```

Valid legacy settings migrate automatically when v2 is absent.

### Statistics/history

Existing keys remain compatible:

```text
stats_v1
history_v1
```

New code adds stricter decode invariants and bounded history sanitization without changing those keys.

### Backup

Current schema:

```text
RPS_ARENA_BACKUP|1
```

Future incompatible backup changes must use a new schema/header and migration documentation instead of silently changing v1 meaning.

## Known limitations / intentional boundaries

- A real LAN socket/discovery adapter is not shipped in v1.1.0; the shared room contract plus no-network in-memory reference adapter are implemented and tested.
- Android device/emulator instrumentation UI tests are not part of the current hosted CI baseline; desktop Compose UI smoke tests plus documented Android manual accessibility/device checks remain the current practical coverage.
- Store signing/notarization is not automated with real credentials because authorized signing secrets are not stored in the repository.
- Public release artifacts are intentionally unsigned/reproducible until a controlled signing environment is configured.
- The release workflow does not currently repeat `check_docs_coverage.py`; version tags must be created from `main` only after the normal CI documentation gate is green.
- iOS packaging is not part of the current release gate.
- Sound/haptics preferences are persisted product settings; platform-specific effect engines are not added solely to inflate dependency count or permissions.
- The primary app remains fully offline and does not request Android internet permission.
- The repository currently relies on an installed/setup Gradle and does not track the standard Gradle Wrapper; this is explicitly documented rather than hidden.

## Open issues

No open repository issues were found during the start-of-milestone audit.

Any CI/CodeQL finding on PR `#11` takes priority over optional future roadmap work.

## Next optional tasks after a green v1.1 merge

1. Implement a real opt-in LAN adapter only after explicit product/security/privacy approval.
2. Add Android emulator/device Compose instrumentation to CI when runner cost/stability is accepted.
3. Configure signed Android/Desktop release automation only after authorized secrets are provisioned outside Git.
4. Evaluate optional iOS packaging as a separate milestone.
5. Expand localization to additional languages only when translations can be maintained and tested at the same quality level.
6. Consider adding the official Gradle Wrapper in a dedicated integrity-reviewed build-system change so local Gradle selection can be pinned by the repository itself.

## v1.1.0 release-notes draft

RPS Arena 1.1.0 expands the offline Android/Desktop arena with optional round timers, replayable CPU challenge seeds, local profile naming, recent W/L/D trends, versioned local backup/import, substantially broader Hindi localization, responsive configuration controls, reduced-motion result behavior, and a transport-neutral private-room architecture. Persistence/import validation is stricter, the private-room reference protocol now enforces lifecycle authority and input integrity, shared/desktop UI regression coverage is broader, CI enforces formatting/exhaustive tracked-file documentation/version consistency/Android lint, and tagged builds can generate unsigned Android/Linux/Rust artifacts with checksums. The repository now includes a role-based deep documentation set covering commands, toolchain/upgrades, build architecture, game rules/state, storage/backup, localization, room protocol, Android, desktop, Rust, tests, GitHub automation, branding, maintenance, terminology, and every tracked file. Primary gameplay remains account-free, telemetry-free, ad-free, cloud-free, and offline-first.

## Representative milestone commits

This milestone intentionally uses many small, cohesive commits rather than one large implementation commit. Representative implementation/quality messages include:

- `feat: extend game model for timers profiles and localization`
- `feat: add versioned backup migration and trend persistence`
- `feat: add timeout backup and recent trend state flows`
- `feat: add English and Hindi UI string catalog`
- `feat: add timed rounds backups trends profiles and bilingual UI`
- `feat: add private room multiplayer transport contract`
- `test: cover backup migration and recent trend persistence`
- `test: cover timeout and data restore state behavior`
- `test: verify private room protocol and participant limits`
- `test: harden persistence validation edge cases`
- `test: cover match timer and win target invariants`
- `test: verify replayable seeded CPU matches through state`
- `ci: enforce formatting and Android lint gates`
- `ci: verify synchronized release versions`
- `ci: add reproducible tagged release artifact workflow`
- `test: enable Compose desktop UI test runtime`
- `test: add desktop UI smoke coverage for primary journeys`
- `ui: add shared responsive layout design tokens`
- `ui: define calm branded light and dark theme tokens`
- `feat: complete Hindi gameplay and settings localization catalog`
- `feat: localize gameplay results and data management feedback`
- `refactor: centralize shared app metadata constants`
- `refactor: render about metadata from shared constants`
- `build: make version check localization-safe`
- `test: cover localization labels and shared version metadata`
- `feat: add bilingual achievement copy catalog`
- `test: cover bilingual achievement copy fallbacks`
- `ui: wire responsive layout tokens and localized achievements`
- `test: extend desktop UI coverage across Hindi gameplay`
- `fix: restrict private room lifecycle event authority`
- `test: cover private room lifecycle authority and close behavior`
- `fix: use valid code in room close regression test`
- `refactor: simplify private room event validation branch`
- `ui: wrap configuration chips on narrow screens`
- `ci: allow intentional Markdown hard break spacing`.

Representative deep-documentation/coverage messages include:

- `docs: add exhaustive command reference`
- `docs: add deep toolchain install and upgrade guide`
- `docs: explain complete Gradle build architecture`
- `docs: document complete game domain and state machine`
- `docs: document storage migration and backup schema deeply`
- `docs: document localization architecture and language expansion`
- `docs: define private room protocol and LAN security boundary`
- `docs: document every Android platform file and behavior`
- `docs: document every desktop platform file and package path`
- `docs: document optional Rust engine and parity policy`
- `docs: document every GitHub automation and CI release gate`
- `docs: catalog every tracked automated test file`
- `docs: add complete repository maintenance playbook`
- `docs: document branding assets and visual ownership`
- `docs: add comprehensive project terminology glossary`
- `docs: add role based documentation navigation index`
- `ci: add every tracked file documentation coverage check`
- `docs: add exhaustive every tracked file reference`
- `build: enforce documentation coverage in shell verification`
- `build: enforce documentation coverage in PowerShell verification`
- `ci: require complete tracked file documentation coverage`
- `chore: require tracked file documentation in pull requests`
- `docs: document tracked file coverage as a required quality gate`
- `docs: extend validation contract to exhaustive file coverage`
- `docs: connect setup guide to complete repository documentation`
- `docs: expose complete repository documentation from readme`
- `docs: synchronize CI guide with documentation coverage gate`
- `docs: explain documentation coverage and full verification commands`
- `docs: enforce exhaustive documentation in contributor workflow`
- `docs: record exhaustive repository documentation milestone`.

## Commit identity

The project documents `sanskarin@outlook.in` as the canonical owner commit email in `.mailmap`, contributor/setup documentation, and prior commit metadata. Commits created through the authenticated GitHub integration use the repository owner's configured GitHub identity.

**Made by the Sanskar.**
