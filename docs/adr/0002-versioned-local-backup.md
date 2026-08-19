# ADR 0002: Versioned plain-text local backup format

- Status: Accepted
- Date: 2026-08-19

## Context

RPS Arena stores a small amount of local state: settings, statistics, match setup, and recent history. Users should be able to preserve or move this state without creating a cloud account.

## Decision

Use a human-readable backup envelope beginning with:

```text
RPS_ARENA_BACKUP_V1
```

The backup contains encoded settings, aggregate stats, match configuration, and bounded recent history. Import validates every required section before changing any persisted value.

The format is intentionally not encrypted and is not described as secret storage.

## Consequences

- Backups can be inspected and copied without proprietary tools.
- No cloud account, sync server, or cryptographic key-management system is required.
- Unknown or malformed versions can be rejected safely.
- Future incompatible formats can introduce another explicit version and migration path.
- Users must not use the backup text field for sensitive secrets.

## Alternatives considered

### Mandatory cloud synchronization

Rejected because it would add accounts, network availability, and additional privacy/security obligations to a local game.

### Custom encrypted backup

Rejected because RPS Arena game state does not justify inventing or operating cryptographic key management. Device-level protection remains the appropriate baseline for local preferences.

### Unversioned serialization

Rejected because silent reinterpretation of future incompatible state is riskier than an explicit version marker.
