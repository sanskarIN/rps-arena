# Storage, Match Configuration, History, and Backup

RPS Arena stores small local strings behind a multiplatform persistence boundary. This guide documents the current reconciled storage architecture, keys, validation rules, backup compatibility, and platform privacy boundary.

For the exact backup schema grammar, also see [`BACKUP.md`](BACKUP.md).

## Storage architecture

```text
ArenaState
   |
   v
ArenaRepository
   |
   v
ArenaStore
   |
   v
PlatformArenaStore
   |
   v
PlatformStore (expect/actual)
```

`ArenaRepository` owns serialization, validation, history limits, match-configuration persistence, and backup behavior. `ArenaStore` is an injectable key-value boundary used by UI/state tests. Production `PlatformArenaStore` delegates to the existing platform `PlatformStore` implementation.

This design keeps tests deterministic without changing production persistence behavior.

## Platform stores

| Platform | Physical backend |
|---|---|
| Android | app-private SharedPreferences |
| iOS/iPadOS | NSUserDefaults |
| Windows/Linux/macOS | java.util.prefs.Preferences |
| Web JS/Wasm | browser localStorage |

Platform hosts initialize storage when their backend requires it, then render the shared application.

## Current keys

```text
settings_v1
match_config_v1
stats_v1
history_v1
```

Do not change the meaning of an existing key incompatibly. Introduce an explicit new key/schema when a persisted representation cannot remain backward compatible.

## Settings v1

`settings_v1` contains exactly seven pipe-separated boolean fields:

```text
darkTheme|followSystemTheme|reducedMotion|soundEnabled|hapticsEnabled|extendedVariant|onboardingComplete
```

Malformed field counts fall back to default `ArenaSettings`. Individual invalid boolean values also fall back to each setting's safe default.

The older pre-reconciliation `settings_v2` player-name/language format is not the current reconciled storage model and must not be treated as active merely because it exists in branch history.

## Match configuration v1

`match_config_v1` contains five fields:

```text
variant|opponentMode|difficulty|matchMode|seed
```

Example:

```text
LIZARD_SPOCK|LOCAL_TWO_PLAYER|EXPERT|TOURNAMENT|-424242
```

It preserves:

- `GameVariant`;
- `OpponentMode`;
- `Difficulty`;
- `MatchMode`;
- deterministic integer CPU seed.

`ArenaState` loads this value at construction. `updateConfig()` persists the new value before resetting the active match.

### Validation

The decoder requires exactly five fields. Enum names must exist in the running build and the seed must parse as a Kotlin `Int`. Any malformed value causes the complete configuration to fall back to `MatchConfig()` defaults instead of partially restoring corrupt state.

Round-timer configuration is **not** part of the current five-field schema. If timers are restored, persisted configuration must gain an explicit compatibility/migration strategy and tests rather than silently changing how old five-field records are interpreted.

## Statistics

`stats_v1` contains:

```text
roundsPlayed|wins|losses|draws|bestStreak|currentStreak
```

A persisted statistics record is accepted only when:

1. all six fields are integers;
2. every value is non-negative;
3. `roundsPlayed == wins + losses + draws`;
4. `currentStreak <= bestStreak`;
5. `bestStreak <= wins`.

Invalid records load as default zeroed statistics.

## History

`history_v1` stores newest-first human-readable round summaries separated by newlines.

Repository limits:

```text
maximum retained entries: 30
maximum written entry length: 160 characters
```

Before a new entry is stored:

- CR and LF are replaced with spaces;
- outer whitespace is trimmed;
- blank results are ignored;
- text is truncated to 160 characters;
- the newest entry is prepended;
- the list is truncated to 30.

Reading also removes blank lines and returns at most 30 entries.

History remains human-readable for compatibility with existing `history_v1` data and backup schema 1. A future structured-history format requires explicit migration compatibility.

## Backup schema 1

The authoritative header is exactly:

```text
RPSARENA_BACKUP|1
```

Schema 1 is line-oriented:

```text
RPSARENA_BACKUP|1
settings|<7 strict booleans>
stats|<6 validated non-negative integers>
history|<count>
item|<history text>
...
```

The history item count must match the number of `item` records and must be between 0 and 30.

The current codec is implemented in:

```text
shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaBackup.kt
```

Repository integration lives in `ArenaRepository`.

### What schema 1 contains

- settings;
- aggregate statistics;
- up to 30 recent history entries.

### What schema 1 does not contain

`match_config_v1` is not included. Import therefore updates the data owned by backup schema 1 while leaving the receiving installation's match configuration untouched.

Adding match configuration under the same schema-1 header would change existing compatibility semantics. If portable match setup is desired later, introduce an explicit schema migration/version rather than silently extending v1.

## Backup validation

Imports reject:

- blank input;
- malformed/missing magic header;
- unsupported schema versions;
- malformed settings;
- malformed or inconsistent statistics;
- history counts outside `0..30`;
- history item count/shape mismatches.

The full backup is decoded and validated before repository writes begin. Platform writes are small sequential key-value writes, not an ACID database transaction, but parsing failures are detected before the intended replacement writes start.

## Export behavior

`exportBackup()` reads normalized current settings, statistics, and history, builds an `ArenaBackup`, and serializes it through `ArenaBackupCodec`.

History line breaks are normalized so one entry cannot corrupt the line-based format.

The application does not upload backup text. Transfer is currently explicit copy/paste through the Settings backup dialogs.

## Import behavior

`importBackup(raw)`:

1. decodes and validates through `ArenaBackupCodec`;
2. returns a typed `ArenaBackupError` on failure;
3. writes settings/statistics/history only after successful decoding;
4. returns the imported history count on success;
5. causes `ArenaState` to reload current settings/statistics in memory.

Raw backup contents are not written to `SafeLogger`.

## Android automatic-backup boundary

The in-app explicit backup feature is separate from Android system backup.

The Android application intentionally:

- sets automatic app backup off;
- references backup/data-extraction rule files;
- excludes SharedPreferences from platform cloud/device-transfer backup;
- keeps the primary manifest free of `android.permission.INTERNET`.

`scripts/check_android_privacy.py` protects those invariants in CI and local verification.

## Data reset and history-clear status

The older pre-reconciliation branch contains reset/history-management implementations, but they are not current reconciled runtime behavior unless reintroduced against this architecture with tests and UI confirmation.

Planned next-version work includes explicit reset confirmation and reversible history clearing; see [`NEXT_VERSION.md`](NEXT_VERSION.md).

## Compatibility rules

When changing persisted data:

1. never silently reinterpret an incompatible existing key;
2. keep old readers/migrations when compatibility is required;
3. version backup changes explicitly;
4. reject malformed/unsupported data rather than partially guessing it;
5. bound user-controlled text and collections;
6. add repository/state regression tests;
7. update [`BACKUP.md`](BACKUP.md), this guide, release notes, and exhaustive file references when file ownership changes.

## Privacy notes

Local storage can contain user preferences and gameplay history. Web data follows browser-origin storage semantics; uninstall/clear-site-data behavior is platform controlled. Exported backup text should be treated as personal local data and stored only where the user considers appropriate.
