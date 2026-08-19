# Storage, Migration, History, and Backup Format

RPS Arena stores only small local strings through a multiplatform storage boundary. This guide documents the exact storage keys, platform implementations, codecs, migration path, validation rules, history grammar, backup schema, escaping, limits, reset behavior, and compatibility rules.

## Storage architecture

```text
ArenaState
   |
   v
ArenaRepository
   |
   v
PlatformStore (expect)
   |-----------------------------|
   v                             v
Android actual              Desktop actual
SharedPreferences           java.util.prefs.Preferences
```

`ArenaRepository` owns product-level serialization/validation. Platform adapters only read/write strings.

## `PlatformStore` common contract

File: `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.kt`.

```kotlin
expect object PlatformStore {
    fun initialize(platformContext: Any? = null)
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}
```

`expect` declares that each platform target must provide a matching `actual object PlatformStore`.

Why the API is deliberately small:

- keeps persistence implementation replaceable;
- lets `ArenaRepository` remain common/testable code;
- avoids leaking Android `Context` or desktop Java preferences APIs into common business logic;
- makes tests inject in-memory read/write lambdas without platform storage.

## Android storage

File: `shared/src/androidMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.android.kt`.

Android initialization requires a `Context`:

```kotlin
PlatformStore.initialize(this)
```

`MainActivity` calls this before rendering `RpsArenaApp`.

The adapter opens:

```text
SharedPreferences name: rps_arena
mode: Context.MODE_PRIVATE
```

`MODE_PRIVATE` means the preferences file is app-private through Android's normal application sandbox.

Writes use:

```kotlin
preferences.edit().putString(key, value).apply()
```

`apply()` updates the in-memory preference immediately and schedules disk persistence asynchronously. The repository is designed for tiny settings/stat/history strings, not large files or databases.

If `getString()` is called before initialization, the adapter returns the supplied default. `putString()` before initialization has no effect. The Android launcher therefore intentionally initializes storage first.

## Desktop storage

File: `shared/src/desktopMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.desktop.kt`.

Desktop uses:

```java
java.util.prefs.Preferences
```

Node:

```text
in/sanskar/rpsarena
```

The underlying physical storage location is operating-system/JVM dependent. Do not document one hard-coded registry/file location as universal.

Desktop `initialize()` is a no-op because the Java Preferences node is created lazily and does not need an Android-like context.

## Repository dependency injection

`ArenaRepository` constructor accepts:

```kotlin
readString: (String, String) -> String
writeString: (String, String) -> Unit
```

Defaults call `PlatformStore`, but tests supply a mutable map.

Benefits:

- codecs/migrations can be tested without Android/Desktop;
- malformed-import tests can verify no writes occur;
- state tests can use deterministic isolated storage;
- repository code does not depend on a mocking framework.

## Current storage keys

| Key | Meaning | Current format |
|---|---|---|
| `settings_v1` | legacy settings | 7 booleans separated by `|` |
| `settings_v2` | current settings | 7 booleans + escaped name + language |
| `stats_v1` | aggregate statistics | 6 integers separated by `|` |
| `history_v1` | recent canonical round summaries | newline-separated strings |

The suffix is part of the compatibility strategy. Do not reuse an old key for an incompatible schema.

## Settings v1 format

Legacy v1 contains exactly seven pipe-separated strict booleans in this order:

```text
darkTheme|followSystemTheme|reducedMotion|soundEnabled|hapticsEnabled|extendedVariant|onboardingComplete
```

Example:

```text
false|true|false|true|true|false|true
```

`toBooleanStrictOrNull()` is used, so only valid strict boolean text is accepted. Corrupt values do not silently become `false`.

## Settings v2 format

Current v2 contains nine fields:

```text
darkTheme
followSystemTheme
reducedMotion
soundEnabled
hapticsEnabled
extendedVariant
onboardingComplete
escapedPlayerName
languageEnumName
```

Serialized with `|` separators.

Example conceptual record:

```text
false|true|false|true|true|false|true|Sanskar|ENGLISH
```

The actual name may contain escaped percent/pipe sequences.

## Player-name normalization

Before settings are persisted, the player name is normalized:

1. `\n` -> space;
2. `\r` -> space;
3. trim outer whitespace;
4. keep at most 32 characters;
5. if blank, replace with `Player 1`.

This prevents a display name from injecting extra lines into simple local serialization/history UI.

## Settings field escaping

The player-name field can contain `|`, which is also the settings delimiter.

Encoding order:

```text
%  -> %25
|  -> %7C
```

Decoding reverses:

```text
%7C -> |
%25 -> %
```

Percent is escaped first so an original literal string such as `%7C` is not accidentally interpreted as an encoded pipe after round trip.

## Settings migration

`loadSettings()` checks current v2 first.

Flow:

```text
settings_v2 exists and nonblank
    -> decode v2/current-compatible representation

else settings_v1 exists and nonblank
    -> decode legacy 7-field record
    -> save decoded settings through saveSettings()
    -> settings_v2 is now persisted

else
    -> ArenaSettings defaults
```

This is a forward migration; it does not delete `settings_v1`.

### Invalid settings behavior

`decodeSettings()` returns defaults when `decodeSettingsOrNull()` rejects the raw record.

The repository favors safe defaults over partially interpreting corrupt settings.

Future schema changes should add a new key/version or an explicitly compatible decoder rather than changing field meaning in place.

## Statistics format

`stats_v1` has six integer fields:

```text
roundsPlayed|wins|losses|draws|bestStreak|currentStreak
```

Example:

```text
20|12|5|3|4|2
```

## Statistics validation invariants

A statistics record is rejected unless all of these hold:

1. exactly six fields parse as integers;
2. every integer is non-negative;
3. `roundsPlayed == wins + losses + draws`;
4. `currentStreak <= bestStreak`;
5. `bestStreak <= wins`.

Why:

- a round must have exactly one aggregate outcome category;
- a current streak cannot exceed the best historical streak;
- a winning streak cannot contain more wins than total wins.

Invalid persisted stats load as default zeroed `ArenaStats`.

## History format

`history_v1` is a single string containing one canonical history entry per line.

Newest entries are stored first.

Maximum retained entries:

```text
30
```

### History write sanitation

When a line is added:

- CR/LF are replaced with spaces;
- outer whitespace is trimmed;
- maximum length is 160 characters;
- blank results are ignored;
- new line is prepended;
- list is truncated to 30;
- entries are joined with `\n`.

### History read behavior

Reading:

- splits by lines;
- trims each entry;
- removes blanks;
- takes at most 30.

## Canonical history grammar

Normal played round:

```text
<Gesture> vs <Gesture> — <Outcome>
```

Example:

```text
Rock vs Scissors — Player 1 won
```

Allowed outcome suffixes currently recognized by trend/localization logic:

```text
Player 1 won
Player 2 won
Draw
```

Timeout examples:

```text
Player 1 timed out — Player 2 won
Player 2 timed out — Player 1 won
```

Because recent-trend code checks suffixes and UI localization parses known canonical strings, history grammar is a de facto persistence contract. Change it only with migration/parser compatibility tests.

## Recent trend derivation

`loadRecentTrend(limit)` clamps `limit` to `1..30`.

It examines newest history records and increments counters by suffix:

```text
endsWith("Player 1 won") -> wins
endsWith("Player 2 won") -> losses
endsWith("Draw")         -> draws
```

The default caller uses 10.

This means history remains the source of recent trend; aggregate stats are the source of lifetime stats.

## Backup result model

File: `BackupModels.kt`.

`BackupImportResult` contains:

- `imported: Boolean`;
- `message: String`.

Factory helpers:

- `success(...)` -> `imported = true`;
- `failure(...)` -> `imported = false`.

The UI uses the boolean to decide whether to reload state and the message for user feedback/localization.

## Backup v1 grammar

Header, exactly:

```text
RPS_ARENA_BACKUP|1
```

Required record types:

```text
settings|<escaped-settings-value>
stats|<escaped-stats-value>
```

Optional/repeated:

```text
history|<escaped-history-line>
```

Example structural form:

```text
RPS_ARENA_BACKUP|1
settings|false%7Ctrue%7C...
stats|20%7C12%7C5%7C3%7C4%7C2
history|Rock vs Scissors — Player 1 won
```

The exact settings value itself contains `|`, so those separators are escaped again inside the backup record value.

## Backup escaping

Backup value encoder applies:

```text
%    -> %25
|    -> %7C
LF   -> %0A
CR   -> %0D
```

Decoder reverses in this order:

```text
%0D -> CR
%0A -> LF
%7C -> |
%25 -> %
```

Escaping is a transport encoding, not encryption. Anyone with the backup can read/reconstruct the local player name, settings, aggregate stats, and recent history.

## Backup limits

Repository import maximum text length:

```text
128 * 1024 characters
```

UI backup editor also caps input to the same 128 KiB character count.

Maximum backup line count:

```text
64
```

Maximum imported history retained:

```text
30
```

These bounds prevent the small local settings feature from becoming an unbounded memory/data parser.

## Import algorithm

Import follows this sequence:

1. reject payload if character length exceeds 128 KiB;
2. split into lines and normalize trailing `\r` from line endings;
3. require exact v1 header;
4. reject more than 64 total lines;
5. create temporary in-memory variables for settings/stats/history;
6. parse each nonblank record at first `|`;
7. unescape record value;
8. validate recognized record type;
9. reject duplicate settings/stat records;
10. decode/validate settings/stat values;
11. collect bounded sanitized history;
12. reject unknown record type;
13. require settings record;
14. require stats record;
15. only after all validation succeeds, write settings/stats/history;
16. return successful `BackupImportResult`.

## Transaction-like validation property

The repository does not provide an ACID database transaction, but it deliberately validates the entire backup's required structured data **before** writing validated settings/stats/history.

Therefore failures discovered during parsing/validation do not intentionally partially import earlier valid records.

Tests explicitly check that malformed/unknown input does not overwrite existing statistics.

## Duplicate records

Only one `settings` and one `stats` record are accepted.

Duplicates are rejected because accepting "first wins" or "last wins" semantics would make edited/untrusted backup interpretation ambiguous.

History is intentionally repeatable.

## Unknown records

Unknown backup record types are rejected.

This strictness matters for schema versioning: a v1 reader should not silently ignore a future record that might change meaning/security assumptions.

Future incompatible extensions should normally use a new backup header/schema version.

## Export flow

`exportBackup()` reads current normalized state:

1. header;
2. encoded current settings;
3. encoded current stats;
4. each current recent history entry.

`trimEnd()` removes the final line terminator from the generated text.

The UI places this text into its local editor for the user to copy/save through their chosen external mechanism.

RPS Arena itself does not upload the backup.

## Import UI flow

`ArenaState.importBackup()`:

1. passes `backupText` to repository;
2. exposes result message;
3. when successful, reloads persisted settings/stats;
4. resets current match so it cannot continue with state inconsistent with newly imported data.

History is retrieved from repository on demand, so no separate in-memory history reload field is required.

## Reset behavior

`ArenaRepository.clearUserData(preserveOnboarding = true)`:

1. reads whether onboarding is complete;
2. saves default `ArenaSettings`, optionally retaining onboarding complete;
3. saves zeroed `ArenaStats`;
4. clears history string.

`ArenaState.clearUserData()` additionally:

- reloads settings/stats;
- clears backup text;
- resets current match;
- exposes a reset feedback message.

## What reset does not do

The current reset helper does not delete the physical SharedPreferences file/Java Preferences node. It rewrites known application keys to default/empty values.

It also does not clear unrelated data belonging to other applications.

## Android `allowBackup`

The Android manifest currently sets:

```xml
android:allowBackup="true"
```

This is an Android platform backup capability distinct from RPS Arena's explicit plain-text backup feature. Platform backup behavior depends on Android/device policy.

When making privacy/release decisions, review both:

- app-controlled text backup/import;
- Android platform backup behavior.

Do not assume they are the same mechanism.

## Privacy classification

Local storage can contain:

- local display name;
- interface/preferences;
- aggregate gameplay statistics;
- recent round summaries;
- onboarding flag.

No password/account/payment data is part of this storage model.

However, a player's chosen name/history can still be personal data. Treat exported backups/log samples carefully.

## Backward-compatibility rules

When changing persistence:

- never reorder existing encoded fields without a migration;
- never reuse a versioned key for incompatible semantics;
- never change history grammar without preserving old parser behavior/migrating history;
- never change backup v1 meaning incompatibly under the same header;
- add tests for old -> new migration;
- validate before writing imported data;
- keep input/history/name limits explicit;
- document reset/uninstall behavior accurately.

## Adding settings

Safer options:

1. if the format can remain backward compatible, add explicit decoding logic for old/new field counts;
2. otherwise introduce `settings_v3` and migrate from v2/v1.

Required work:

- update `ArenaSettings`;
- codec encode/decode;
- default behavior;
- migration tests;
- backup round-trip tests;
- settings UI/localization;
- privacy documentation if data meaning changes.

## Changing stats

If adding a new persisted statistic, do not simply append it and keep `stats_v1` decoder expecting a different field count. Either maintain backward-compatible decode variants deliberately or introduce `stats_v2` with migration.

Define mathematical invariants and tests before accepting imported/persisted values.

## Changing backup schema

For incompatible changes:

1. choose a new header such as a future `RPS_ARENA_BACKUP|2`;
2. decide whether v2 reader also accepts v1;
3. write explicit migration/import mapping;
4. preserve strict size/count bounds;
5. reject ambiguous/duplicate required records;
6. test malicious/malformed edge cases;
7. update privacy/security/release docs;
8. keep old backup fixtures for compatibility regression tests.

## Files to review for storage changes

- `GameModels.kt`;
- `ArenaRepository.kt`;
- `BackupModels.kt`;
- `PlatformStore.kt`;
- Android/Desktop PlatformStore actuals;
- `ArenaState.kt`;
- settings UI/localization;
- repository codec/backup/validation/state tests;
- `PRIVACY.md`;
- `SECURITY.md`;
- `docs/testing.md`;
- `docs/architecture.md`;
- this guide;
- `CHANGELOG.md` for release-visible changes.
