# Architecture

RPS Arena is a small modular monolith built with Kotlin and Compose Multiplatform. Core gameplay is deliberately offline-first: Android and desktop share the same rules, CPU strategy, state model, persistence codecs, and UI.

## Modules

- `shared/` — game models, rule resolution, CPU strategies, application state, persistence contract/codecs, shared Compose UI, and common tests.
- `androidApp/` — Android application entry point, Android resources, manifest, and packaging.
- `desktopApp/` — desktop JVM entry point and native packaging configuration.
- `rust-engine/` — optional standalone rules mirror used for independent experimentation and contract verification.

## Dependency direction

```text
Android / Desktop entry points
            |
            v
      Shared Compose UI
            |
            v
         ArenaState
       /            \
      v              v
Rules + CPU      ArenaRepository
                     |
                     v
                KeyValueStore
               /             \
              v               v
 SharedPreferences       Java Preferences
```

Rules and CPU behavior do not depend on UI or platform APIs. Persistence depends on the small `KeyValueStore` interface so common tests can inject deterministic in-memory storage.

## State

`ArenaState` is the shared observable application state. It owns:

- current screen;
- appearance/accessibility settings;
- persisted match configuration;
- active match snapshot;
- aggregate lifetime statistics;
- reactive recent history;
- local two-player hidden-turn state;
- deterministic CPU instance.

Changing match configuration persists the new configuration and restarts only the active match. Lifetime statistics and recent history are not discarded by a normal match restart.

## Game engine

`RulesEngine` is the canonical winner resolver for classic and Lizard–Spock gestures. `CpuStrategy` receives the same active variant and may only select gestures valid for that variant.

CPU behavior is deterministic when given the same seed and the same ordered player inputs. This enables reproducible challenges and regression tests without a remote service.

## Persistence

Small local state is stored through platform-native preferences rather than a database. The repository persists:

- `ArenaSettings`;
- `ArenaStats`;
- `MatchConfig`;
- up to 30 recent history lines.

Stored values are decoded defensively. Malformed values fall back to safe defaults. The settings codec accepts the previous seven-field representation so the audit can remove unused settings without discarding existing local preferences.

Backup/restore uses a versioned plain-text envelope beginning with `RPS_ARENA_BACKUP_V1`. Import validates every required section before mutating local state.

## Networking

Core v1 code has no required networking layer. The Android manifest requests no internet permission. About-screen links are opened only after a user action through the platform URI handler.

Optional LAN/private-room work is intentionally deferred behind a future architecture/security review. It must never become a prerequisite for local CPU or pass-and-play modes.

## UI

Compose Multiplatform provides the shared screens. Material 3 controls supply keyboard/touch semantics and platform-consistent focus behavior. Game gesture controls include explicit accessibility descriptions and large targets. Status is communicated with text in addition to symbols.

The reduced-motion setting is retained as part of the accessibility contract; the current audited UI avoids non-essential game animations entirely.

## Release architecture

- CI compiles shared/desktop Kotlin, runs shared tests, builds and lints Android, and verifies the optional Rust engine.
- CodeQL performs independent Java/Kotlin static analysis without requiring the Android toolchain.
- Tagged/manual release automation creates an unsigned Android release APK and per-OS desktop distributables. Signing secrets are intentionally outside this repository.

See `docs/adr/` for stable architectural decisions.
