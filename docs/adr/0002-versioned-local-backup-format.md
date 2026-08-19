# ADR 0002: Versioned plain-text local backup format

- Status: Accepted
- Date: 2026-08-19

## Context

RPS Arena stores only a small amount of local settings, aggregate statistics, and recent round history. Users should be able to move or preserve this data without requiring a cloud account.

## Decision

Use an explicit version header followed by compact local values:

```text
RPS_ARENA_BACKUP_V1
settings=...
stats=...
history=...
```

Import rejects an unknown or malformed header. Repository parsing remains defensive and falls back to safe defaults for corrupt stored values.

The backup is not encrypted and is not presented as secure secret storage.

## Consequences

- Backups remain portable and easy to inspect.
- No cloud account, file server, or cryptographic key management is required.
- Future incompatible formats can introduce a new header and a migration path.
- Users must avoid putting sensitive information into the backup field because the format is intentionally readable.

## Alternatives considered

### Cloud sync

Rejected for the initial product because it would introduce accounts, networking, privacy obligations, and service availability dependencies.

### Custom encryption

Rejected because game state does not justify inventing or managing cryptographic storage. Platform-level device protection remains the appropriate protection for local preferences.

### Unversioned JSON

Rejected because a visible schema/version marker makes incompatible future changes safer to detect and migrate.
