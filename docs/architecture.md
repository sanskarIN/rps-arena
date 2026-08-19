# Architecture

RPS Arena uses a small modular-monolith architecture shared across Android and desktop.

## Modules

- `shared`: platform-neutral domain rules, CPU strategy, persistence contracts, application state, Compose UI, and tests.
- `androidApp`: Android launcher, Android packaging, and a `SharedPreferences` storage adapter.
- `desktopApp`: desktop launcher, native packaging, and a Java Preferences storage adapter.

The application intentionally has no backend service. Offline play is the primary product, not a degraded fallback.

## Dependency direction

```text
platform entry points
        |
        v
shared UI -> AppController -> AppRepository -> KeyValueStore
                         |
                         v
                   domain rules
```

Domain rules do not depend on UI or platform APIs. `AppRepository` only depends on the `KeyValueStore` interface. Platform modules provide the concrete storage adapters.

## State model

`AppController` owns observable application state:

- `AppSettings`
- `GameStats`
- current `MatchState`
- capped recent `RoundRecord` history

Changes that affect match semantics restart only the active match, while settings and lifetime statistics remain persisted. Resetting all data requires an explicit user confirmation in the UI.

## Game engine

`GameRules` contains the canonical win graph for both supported variants. `CpuStrategy` delegates every result to the same rules engine and cannot choose gestures outside the active variant.

CPU randomness is deterministic for a supplied seed. This makes debugging and challenge reproduction possible while keeping CPU behavior transparent.

## Persistence

The v1 storage format is intentionally compact and local. Parsing is defensive: corrupt or unknown values fall back to safe defaults rather than crashing the app.

Backup format:

```text
RPS_ARENA_BACKUP_V1
settings=...
stats=...
history=...
```

The format is versioned so a future incompatible schema can be migrated instead of silently misread.

## Networking

The v1 application has no required networking layer and the Android manifest requests no internet permission. External support/repository/funding links are handed to the operating system.

Optional LAN/private-room work belongs in a future module behind explicit user opt-in and a dedicated threat model.

## UI

Shared Compose UI provides phone and desktop layouts. Narrow layouts use bottom navigation; wide layouts use a navigation rail. Material 3 theme selection supports light, dark, and system preferences.

Reduced motion disables animated round-result transitions. Interactive game gestures expose semantic labels and minimum touch heights.

## Architectural decisions

See `docs/adr/` for decisions that should remain stable across refactors.
