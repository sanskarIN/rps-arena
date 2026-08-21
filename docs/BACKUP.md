# Backup and Restore

RPS Arena supports an offline, human-readable, versioned backup format. The app never uploads backups and does not require Android network permission.

## Included data

A backup contains only data already stored locally by RPS Arena:

- appearance, accessibility, sound, haptics, rules, and onboarding settings;
- aggregate round statistics and streak counters;
- up to 30 recent round-history summaries.

Backups do not contain accounts, passwords, device identifiers, analytics identifiers, advertising identifiers, or network credentials.

## Using backups

Open **Settings → Backup & restore**.

- **Export backup** displays the complete backup text so it can be copied and stored somewhere the user trusts.
- **Import backup** accepts a complete backup text and validates every section before changing saved RPS Arena data.

An import replaces the saved settings, aggregate statistics, and recent history with the validated backup contents.

## Schema version 1

The first line is a magic identifier and schema version:

```text
RPSARENA_BACKUP|1
```

The remaining records are ordered and line-based:

```text
settings|<dark>|<system-theme>|<reduced-motion>|<sound>|<haptics>|<extended-rules>|<onboarding>
stats|<rounds>|<wins>|<losses>|<draws>|<best-streak>|<current-streak>
history|<count>
item|<history text>
```

There must be exactly `count` history item records. History text may contain `|`; line breaks are normalized to spaces during export. At most 30 history entries are accepted.

## Validation rules

Schema-v1 imports are rejected when any of the following is true:

- the backup is blank;
- the magic header is missing or malformed;
- the schema version is unsupported;
- a setting is not the literal `true` or `false`;
- a statistic is not a non-negative integer;
- `rounds != wins + losses + draws`;
- current streak exceeds best streak, or best streak exceeds total wins;
- the history count is outside `0..30`;
- the number or shape of history records does not match the declared count.

All sections are decoded and validated before repository writes begin. This prevents malformed or unsupported backup text from partially replacing valid local data.

## Forward compatibility

Only schema version 1 is currently supported. A future format change must increment the schema version and add an explicit migration/decoder path rather than silently interpreting new data as schema 1.

Older app versions are expected to reject newer schema versions safely.

## Privacy guidance

A backup can reveal gameplay history and preferences. Treat it as personal local data and store or share it only where appropriate.
