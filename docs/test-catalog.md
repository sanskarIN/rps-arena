# Test Catalog: Every Tracked Test File

This document explains every tracked Kotlin/Compose test file, the regression boundary it protects, and which production code it primarily covers. It complements `docs/testing.md`, which explains how to run the test tasks.

## Test source sets

```text
shared/src/commonTest/   platform-independent business/data/protocol tests
shared/src/desktopTest/  Compose desktop UI smoke tests
```

There is currently no tracked `androidInstrumentedTest`/device-test source set. Android device/TalkBack checks remain manual release validation. Android privacy source invariants are separately checked by `scripts/check_android_privacy.py`.

## `RulesEngineTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/RulesEngineTest.kt
```

Protects `RulesEngine`.

Assertions:

- Classic Player 1 wins: Rock > Scissors, Paper > Rock, Scissors > Paper.
- Extended examples: Lizard > Spock, Spock > Rock.
- Reverse example: Rock loses to Spock.
- Every gesture resolves to `DRAW` against itself.

Why it matters: a change to the defeat matrix can silently invert core game outcomes. This file is the direct rule-table smoke boundary.

## `CpuStrategyTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/CpuStrategyTest.kt
```

Protects seeded determinism and variant safety.

Assertions:

- Two `CpuStrategy(42)` instances produce identical 20-choice Easy/Classic sequences.
- Expert CPU in Classic mode never emits Lizard/Spock even against repeated Rock history across 100 choices.

Why it matters: deterministic challenge seeds and ruleset filtering are product promises.

## `MatchConfigTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/MatchConfigTest.kt
```

Protects configuration invariants.

Assertions:

- every value in `ALLOWED_TIMER_SECONDS` constructs successfully;
- unsupported 15-second timer throws `IllegalArgumentException`;
- Best of 3 target = 2;
- Best of 5 target = 3;
- Tournament target = 5;
- Endless/Streak target = null.

Why it matters: UI, persistence, and state depend on these model-level constraints rather than accepting arbitrary timer/match values.

## `ArenaRepositoryCodecTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryCodecTest.kt
```

Protects basic persistence codec round trips.

Assertions:

- representative `ArenaSettings` survives encode -> decode;
- a non-default `MatchConfig` containing Lizard–Spock, local two-player, Expert, Tournament, negative deterministic seed, and a 20-second timer survives encode -> decode exactly;
- representative valid `ArenaStats` survives encode -> decode.

Why it matters: persistence changes must not alter valid values during serialization, including the user's last selected gameplay setup.

## `ArenaRepositoryBackupTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryBackupTest.kt
```

Uses injected mutable-map storage to test repository behavior without platform APIs.

Assertions:

### Backup round trip

Exports/imports settings containing:

- theme/accessibility flags;
- `Sanskar | Player` name (tests delimiter escaping);
- Hindi language;
- valid statistics;
- win/draw history.

Then asserts settings, stats, and history are identical after import.

The established `RPS_ARENA_BACKUP|1` test scope intentionally does not include `match_config_v1`; that local convenience record requires a future backup schema version if portability is added.

### Malformed import is non-destructive

- stores valid existing stats;
- imports malformed `stats|broken` backup;
- expects `imported == false`;
- verifies original stats remain intact.

### Legacy settings migration

- seeds raw `settings_v1` seven-boolean record;
- loads settings;
- verifies fields decode;
- verifies default Player 1 name;
- verifies `settings_v2` is written.

### Recent trend uses newest records

Creates four history entries, requests limit 3, and verifies exactly one win/loss/draw from the newest three.

Why it matters: this file protects backup compatibility, migration, non-destructive failure, and recent-history semantics.

## `ArenaRepositoryValidationTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaRepositoryValidationTest.kt
```

Protects malformed/untrusted local/import data handling.

Assertions:

### Invalid statistics fall back to defaults

Tests records violating:

- rounds == wins + losses + draws;
- best streak <= wins;
- current streak <= best streak;
- non-negative values.

### Invalid match configuration falls back to defaults

Tests persisted match records containing:

- unknown variant enum;
- non-integer seed;
- unsupported 15-second timer;
- wrong field count.

Each must decode to a complete default `MatchConfig` rather than a partially corrupt runtime setup.

### Player-name sanitation

Confirms newline removal and 32-character maximum.

### Blank-name fallback

Whitespace/newline-only name becomes `Player 1`.

### Unknown backup record rejection

Adds `unknown|value` after valid settings/stats and verifies import fails without overwriting existing stats.

### History bounds/sanitation

Adds 35 newline-containing records, verifies:

- exactly 30 remain;
- newest is first;
- newline became space;
- oldest retained index is correct.

Why it matters: codecs are an input-validation/security boundary, not only serialization helpers.

## `ArenaStateTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStateTest.kt
```

Protects state-machine behavior.

Assertions:

### CPU timeout

With 5-second timer, calling `expireCurrentTurn()` produces Player 1 timeout, Player 2 win, and one loss.

### Local Player 2 timeout

- local two-player config;
- Player 1 locks Paper;
- timeout occurs before Player 2 selection;
- round retains Player 1 Paper, Player 2 gesture is null;
- reason is Player 2 timeout;
- Player 1 wins/stat increments;
- pending gesture clears.

### Timer disabled

`expireCurrentTurn()` with default timer changes neither rounds nor stats.

### Seeded replay through full state

Two independent states with seed `424242` play the same five moves and must produce identical CPU gestures and outcomes.

### Match configuration survives state/app reconstruction

- a shared in-memory repository represents durable platform storage;
- first state stores a non-default variant/opponent/difficulty/mode/seed/timer;
- a new `ArenaState` constructed over the same repository restores the exact config;
- the new `MatchSnapshot` starts with the restored config.

### Local-data reset resets persisted match configuration

- stores a non-default config;
- invokes `clearUserData()`;
- verifies the live state returns to default `MatchConfig`;
- creates another state over the same repository and verifies defaults remain persisted.

### Backup restore refreshes live state

Creates a timeout round, exports backup, clears data, imports backup, then verifies in-memory stats return to one round and feedback is present.

Why it matters: it verifies orchestration across model/CPU/repository, including restart/reset persistence behavior, rather than isolated helpers.

## `PrivateRoomTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/PrivateRoomTest.kt
```

Protects the no-network reference room protocol.

Assertions:

### Room-code validation

- `ABC234` accepted;
- lowercase/outer spaces normalized;
- ambiguous `I` rejected;
- wrong-length code rejected.

### Host/guest event exchange

- guest join creates `ParticipantJoined` event for host;
- valid host round-1 Rock event reaches guest;
- forged restart participant ID is rejected.

### Invalid round/lifecycle authority

- round 0 gesture rejected;
- client-sent fake `ParticipantJoined` rejected;
- client-sent `ParticipantLeft` rejected;
- rejected events never reach guest.

### Close idempotency

Closing guest twice produces exactly one `ParticipantLeft` event for host.

### Two-person limit

Third join returns null.

Why it matters: future transport work must preserve sender/lifecycle/participant boundaries.

## `ArenaStringsTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStringsTest.kt
```

Protects localization/catalog metadata.

Assertions:

- every current gesture has different English/Hindi display text;
- every difficulty has different English/Hindi display text;
- every match mode has different English/Hindi display text;
- English gesture display labels equal canonical `Gesture.label` values;
- `APP_VERSION` has exactly three numeric semantic-version components and no non-canonical numeric formatting.

Why it matters: enum display coverage and release metadata should fail loudly when catalogs drift.

## `AchievementStringsTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/AchievementStringsTest.kt
```

Known IDs:

```text
first_win
ten_rounds
streak_3
streak_7
century
```

Assertions:

- each known ID has English/Hindi titles that differ;
- descriptions differ;
- Hindi title/description are nonblank;
- unknown/future ID returns nonblank safe fallback in both languages.

Why it matters: adding an achievement without translation should be caught by catalog maintenance/tests.

## `SafeLoggerTest.kt`

Path:

```text
shared/src/commonTest/kotlin/in/sanskar/rpsarena/SafeLoggerTest.kt
```

Protects `SafeLogger`, whose default sink is intentionally no-op.

Assertions:

- ordinary fields reach an explicitly supplied test sink unchanged;
- keys containing email/backup/token-sensitive fragments are replaced with `[REDACTED]` before the sink receives the event;
- non-sensitive values are truncated to the 160-character maximum;
- invalid non-lowercase-snake-case event names are rejected.

Why it matters: if a future local/platform diagnostic sink is introduced, redaction and bounded output must happen before data reaches that sink. This is not telemetry enablement; the production default remains no-op.

## `RpsArenaUiTest.kt`

Path:

```text
shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt
```

Uses Compose `runComposeUiTest` and an in-memory `ArenaRepository`.

### Onboarding -> gameplay smoke journey

Verifies visible/clickable flow:

```text
Welcome to RPS Arena
-> Enter the Arena
-> Choose your arena
-> Play
-> Choose a gesture
-> Rock/Paper/Scissors exist
```

### Language setting journey

Navigates to Settings, switches Hindi, then verifies Hindi Settings/language copy exists.

### Hindi gameplay and achievements

After switching Hindi:

- enters Play;
- verifies पत्थर / कागज़ / कैंची;
- returns Home;
- opens Achievements;
- verifies first achievement Hindi title/description.

### Backup/reset controls

Verifies Settings exposes:

- Prepare backup;
- Import backup;
- Reset local data;
- after reset click, Confirm reset and Cancel.

Why it matters: these tests verify visible Compose integration, not merely catalog functions.

## Why tests use injected in-memory repository storage

Many tests construct:

```kotlin
ArenaRepository(
    readString = { ... },
    writeString = { ... },
)
```

This avoids:

- Android `Context`;
- desktop OS preference state;
- cross-test pollution;
- filesystem cleanup;
- mocking framework dependency.

It also makes destructive/non-destructive import assertions easy to inspect and lets state tests simulate restart persistence over one shared storage map.

## Test gaps that remain intentional/known

The current Kotlin/Compose suite does not yet provide hosted Android emulator instrumentation for:

- real Activity lifecycle;
- Android SharedPreferences persistence across an actual process restart;
- TalkBack;
- Android system text scaling;
- adaptive icon/system bars;
- actual OS backup transport execution;
- future network permissions.

The state-level in-memory test verifies match-config reconstruction semantics without claiming to be an Android process-lifecycle test. The source-level Android privacy checker verifies that the manifest disables automatic backup, the backup-policy XML excludes SharedPreferences, and the primary manifest has no internet permission. Manual/device testing still covers runtime platform behavior that static/common tests cannot prove.

The optional Rust tests also cover only representative rule pairs, while Kotlin remains the primary broader application rule suite.

## Which tests to update for common changes

### Rules/gestures

Review:

- `RulesEngineTest`;
- `CpuStrategyTest`;
- `ArenaStringsTest`;
- Rust tests if parity intended;
- UI test if visible controls change.

### Match mode/timer/config persistence

Review:

- `MatchConfigTest`;
- `ArenaRepositoryCodecTest`;
- `ArenaRepositoryValidationTest`;
- `ArenaStateTest`;
- UI tests when controls change;
- `docs/storage-and-backup.md` when the persisted record changes.

### Persistence/backup

Review:

- `ArenaRepositoryCodecTest`;
- `ArenaRepositoryBackupTest`;
- `ArenaRepositoryValidationTest`;
- `ArenaStateTest`;
- UI backup journey;
- `scripts/check_android_privacy.py` and platform policy docs if Android storage/backup behavior changes.

### Language/achievement

Review:

- `ArenaStringsTest`;
- `AchievementStringsTest`;
- `RpsArenaUiTest`;
- backup/migration test if `AppLanguage` persistence changes.

### Logging/diagnostics

Review `SafeLoggerTest` whenever redaction keys, event naming, value bounds, or a diagnostic sink contract changes.

### Private-room protocol

Review `PrivateRoomTest` first, then add transport-specific integration tests when a real transport exists.

## Naming policy

Test names should describe behavior/outcome, for example:

```text
malformedBackupDoesNotOverwriteExistingData
localSecondPlayerTimeoutAwardsRoundToPlayerOne
matchConfigurationPersistsAcrossStateInstances
sessionRejectsInvalidRoundAndLifecycleEvents
```

Prefer behavior descriptions over implementation names such as `testMethod1`.

## Regression-test policy for bug fixes

When fixing a reproducible defect:

1. write/add a test that fails for the defect when feasible;
2. implement the smallest correction;
3. verify the new test passes;
4. run nearby suite;
5. run full repository gate for cross-cutting changes.

If a bug cannot be automated (for example a platform-only accessibility issue), document exact manual reproduction and validation instead of pretending unit coverage exists.

## Running the catalog

All shared target tests:

```bash
gradle :shared:allTests --stacktrace
```

Desktop UI specifically:

```bash
gradle :shared:desktopTest --stacktrace
```

Optional Rust:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Fast source/security/privacy checks:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

Android lint/build checks are quality/build gates, not Kotlin unit test files:

```bash
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

See `docs/command-reference.md` and `docs/testing.md` for full execution guidance.
