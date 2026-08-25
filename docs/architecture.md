# Architecture

## Goals

RPS Arena favors deterministic game logic, offline-first storage, a small dependency surface, accessible shared UI, and transport boundaries that keep optional networking out of the primary experience.

## Modules

- `shared`: Kotlin Multiplatform library targeting Android and JVM desktop.
- `androidApp`: Android application packaging and `MainActivity`.
- `desktopApp`: JVM desktop executable and native package configuration.
- `rust-engine`: optional independent rules mirror; not a runtime dependency.

## Shared layers

```text
ui -> state -> engine/model
          -> data -> PlatformStore
          -> network contracts (optional private-room path)
```

- `model`: game variants, match configuration, outcomes, settings, trends, and achievements.
- `engine`: rules resolution and deterministic seeded CPU behavior.
- `data`: local settings/stats/history persistence, settings migration, trend aggregation, and versioned backup/import.
- `state`: match orchestration, timeout handling, backup/reset flows, achievements, and navigation state.
- `ui`: Compose Multiplatform screens, theme, bilingual string catalog, reduced-motion behavior, and responsive controls.
- `network`: private-room protocol contracts plus an in-memory reference transport. No production socket adapter is part of the offline baseline.

## Persistence

`PlatformStore` is an `expect/actual` boundary:

- Android: private `SharedPreferences`.
- Desktop: `java.util.prefs.Preferences`.

Settings use a `settings_v2` record and migrate compatible `settings_v1` data automatically. Statistics/history keep their established keys. The backup format is explicit and versioned with header `RPS_ARENA_BACKUP|1`.

Import validates size, record count, record type, duplicates, settings fields, statistics invariants, and history bounds before writing validated state.

## Determinism

`CpuStrategy` receives an integer seed. Given the same seed, difficulty, ruleset, and sequence of player moves, CPU decisions are replayable. The UI exposes the seed for challenge reproduction.

## Timed rounds

`MatchConfig.ALLOWED_TIMER_SECONDS` defines supported limits. `0` disables the timer. A timeout is represented as a typed `RoundEndReason`, scored like any other completed round, persisted in history, and included in aggregate statistics/trends.

## Optional private-room boundary

`PrivateRoomGateway` and `PrivateRoomSession` separate room behavior from a concrete transport. The reference `InMemoryPrivateRoomGateway` is deterministic, two-player bounded, validates sender identity, and performs no network I/O.

A future LAN adapter can implement the same boundary in a separate opt-in platform module. Primary gameplay must continue to work without it.

## Privacy and security

The primary Android manifest does not request internet permission. No account, analytics SDK, ads SDK, cloud model, or background sync is required. Local storage contains only game preferences, aggregate statistics, and recent gameplay summaries.

See ADRs in [`docs/adr/`](adr/) for the rationale behind the offline-first and optional-networking decisions.
