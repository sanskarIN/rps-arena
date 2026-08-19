# Architecture

## Goals

RPS Arena favors deterministic game logic, offline-first storage, a small dependency surface, and shared UI/business logic.

## Modules

- `shared`: KMP library targeting Android and JVM desktop. Contains models, rules, CPU strategy, repository, state, Compose UI, and common tests.
- `androidApp`: Android application packaging and `MainActivity` only.
- `desktopApp`: JVM desktop executable and native package configuration.
- `rust-engine`: optional independent rules mirror. It is not a runtime dependency.

## Data flow

`UI -> ArenaState -> RulesEngine/CpuStrategy -> ArenaRepository -> PlatformStore`

`PlatformStore` is an `expect/actual` boundary. Android uses `SharedPreferences`; desktop uses `java.util.prefs.Preferences`.

## Determinism

The CPU strategy receives an integer seed. Given the same seed, difficulty, ruleset, and player-history sequence, the random choices are reproducible.

## Privacy and security

No default network capability is needed. The Android manifest does not declare internet permission. Local storage contains only settings and gameplay summaries.
