# Backup and Restore

RPS Arena supports an offline, human-readable, versioned backup format. The app never uploads backups and does not require Android network permission.

## Included data

A backup contains only data already stored locally by RPS Arena:

- appearance, accessibility, sound, haptics, rules, and onboarding settings;
- aggregate round statistics and streak counters;
- up to 30 recent history entries.

Backups do not contain accounts, passwords, device identifiers, analytics identifiers, advertising identifiers, or network credentials.

## Using backups

Open **Settings → Backup & restore**.

- **Export backup** displays the complete backup text so it can be copied and stored somewhere the user trusts.
- **Import backup** accepts a complete backup text and validates every section before changing saved RPS Arena data.

An import replaces the saved settings, aggregate statistics, and recent history with the validated backup contents.

## Current schema version 2

New exports start with:

```text
RPSARENA_BACKUP|2
```

The settings and statistics records remain ordered and line-based:

```text
settings|<dark>|<system-theme>|<reduced-motion>|<sound>|<haptics>|<extended-rules>|<onboarding>
stats|<rounds>|<wins>|<losses>|<draws>|<best-streak>|<current-streak>
history|<count>
```

Each schema-v2 history record is one of:

```text
round|<player-one-gesture>|<player-two-gesture>|<outcome>
legacy|<escaped-history-text>
```

Structured `round` records use stable enum identifiers such as `ROCK`, `SPOCK`, and `PLAYER_ONE_WIN`. This allows the UI to render restored rounds in the current language rather than freezing the display text to the language used when the round was played.

`legacy` records preserve older human-readable summaries. Reserved `%` and `|` characters are escaped in schema-v2 legacy records, and line breaks are normalized to spaces. At most 30 history entries are accepted.

## Schema version 1 compatibility

The original format remains accepted for import:

```text
RPSARENA_BACKUP|1
settings|<dark>|<system-theme>|<reduced-motion>|<sound>|<haptics>|<extended-rules>|<onboarding>
stats|<rounds>|<wins>|<losses>|<draws>|<best-streak>|<current-streak>
history|<count>
item|<history text>
```

Schema-v1 `item` records are migrated to legacy history entries. After a successful schema-v1 import, the next export uses schema version 2. Existing schema-v1 text is therefore readable without preventing the app from moving forward to structured history.

## Validation rules

Imports are rejected when any of the following is true:

- the backup is blank;
- the magic header is missing or malformed;
- the schema version is neither 1 nor 2;
- a setting is not the literal `true` or `false`;
- a statistic is not a non-negative integer;
- `rounds != wins + losses + draws`;
- current streak exceeds best streak, or best streak exceeds total wins;
- the history count is outside `0..30`;
- the number or shape of history records does not match the declared count;
- a schema-v2 round contains an unknown gesture or outcome identifier;
- a schema-v2 legacy entry is blank or malformed.

All sections are decoded and validated before repository writes begin. This prevents malformed or unsupported backup text from partially replacing valid local data.

## Compatibility contract

- Current exports use schema version 2.
- Imports support schema versions 1 and 2.
- Schema-v1 history is preserved as legacy text.
- Schema-v2 structured rounds remain localizable after restore.
- Future incompatible format changes must increment the schema version and add an explicit decoder/migration path.
- Older app versions are expected to reject newer schema versions safely rather than guessing their meaning.

## Privacy guidance

A backup can reveal gameplay history and preferences. Treat it as personal local data and store or share it only where appropriate.
