# Architecture

RPS Arena is a small modular monolith built with Kotlin and Compose Multiplatform. Core gameplay is deliberately offline-first: Android and desktop share the same rules, CPU strategy, state model, persistence codecs, and UI.

## Modules

- `shared/` — game models, rule resolution, CPU strategies, application state, persistence contract/codecs, local profile state, recent-trend derivation, private-room protocol boundary, shared Compose UI, and common tests.
- `androidApp/` — Android application entry point, Android resources, manifest, backup/extraction rules, and packaging.
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
- local profile collection and active-profile selection;
- persisted match configuration;
- active match snapshot;
- aggregate lifetime statistics;
- reactive recent history;
- a one-step history-clear undo snapshot;
- local two-player hidden-turn phase;
- deterministic CPU instance.

Changing match configuration persists the new configuration and restarts only the active match. Lifetime statistics and recent history are not discarded by a normal match restart. Changing the active local profile also restarts the active match so the scoreboard and subsequent history entries use one unambiguous player identity.

## Game engine

`RulesEngine` is the canonical winner resolver for classic and Lizard–Spock gestures. `CpuStrategy` receives the same active variant and may only select gestures valid for that variant. `ArenaState.play` independently validates the supplied gesture against the active ruleset so callers cannot bypass variant constraints by invoking state directly instead of using the current UI.

CPU behavior is deterministic when given the same seed and the same ordered player inputs. This enables reproducible challenges and regression tests without a remote service.

The optional Rust crate mirrors rules independently. Kotlin remains authoritative for the application until a future integration has a measurable reason, explicit interoperability tests, and equal behavior across supported targets.

## Persistence

Small local state is stored through platform-native preferences rather than a database. The repository persists:

- `ArenaSettings`;
- `ArenaStats`;
- `MatchConfig`;
- up to six validated local profile display names plus the active profile;
- up to 30 recent history lines.

Stored values are decoded defensively. Malformed values fall back to safe defaults. The settings codec accepts the previous seven-field representation so unused legacy flags can be removed without discarding valid preferences.

### Android platform-backup boundary

Android persistence uses a private `SharedPreferences` file named `rps_arena`. RPS Arena does not rely on operating-system backup as an application feature:

- the application manifest sets `android:allowBackup="false"`;
- `res/xml/backup_rules.xml` excludes shared preferences for legacy full-backup rules;
- `res/xml/data_extraction_rules.xml` excludes shared preferences from configured cloud-backup and device-transfer rules on current Android versions.

The explicit RPS Arena text export/import described below is the application-controlled portability path. It is separate from Android OS backup behavior and only occurs after user action.

### Backup format

The current export envelope begins with `RPS_ARENA_BACKUP_V2`. It contains settings, statistics, match configuration, local profiles, the active profile, and recent history.

Import remains backward-compatible with `RPS_ARENA_BACKUP_V1`; a V1 backup is migrated to the default local profile. Decoding and validation happen before writes. Inputs are size-bounded, profile names and IDs are validated, statistics must be internally consistent, timers are bounded, and history entries are bounded and validated before mutation.

`previewBackup` uses the same decoder as import and returns only a safe summary. It does not mutate local state. The Settings UI enables import only after a valid preview exists.

## Recent trends

Recent trends are derived from already-persisted history rather than stored as a second source of truth. `HistoryTrendParser` maps recognized recent result summaries to win/loss/draw values and caps the displayed window. The UI shows W/L/D text and a decisive win rate, so the trend does not depend on color perception.

## Local profiles

Profiles are local labels, not accounts. Profile IDs are repository-internal identifiers and display names are normalized and bounded. No email, password, token, cloud identifier, or authentication state is attached to a profile.

Aggregate statistics currently remain device-wide rather than per-profile. This is intentional for the v1 storage model and is documented rather than hidden behind ambiguous persistence.

## Networking

Core v1 gameplay has no required networking layer. The Android manifest requests no internet permission. About-screen links are opened only after a user action through the platform URI handler.

`PrivateRoomProtocol.kt` defines a pure, tested protocol and `PrivateRoomTransport` boundary for a future explicitly opt-in LAN/private-room implementation. It validates protocol version, room-code format, message identifiers, round bounds, and variant-compatible moves. There is no production transport in v1, no automatic discovery, no mandatory backend, and no networking dependency for CPU or pass-and-play modes.

Any production transport must preserve local rule authority, validate all peer input, define replay/disconnect behavior, remain cancellable, and receive separate security testing before it is exposed in the UI.

## UI and localization boundary

Compose Multiplatform provides the shared screens. Material 3 controls supply keyboard/touch semantics and platform-consistent focus behavior. Game gesture controls include explicit accessibility descriptions and large targets. Status is communicated with text in addition to symbols.

User-facing application, settings, achievement, turn-state, profile, backup, trend, and copy-result text is centralized in `Strings.kt` so a future resource-backed locale layer does not require changing domain/state behavior. Gesture labels remain a small known domain-model localization debt and are tracked rather than silently presented as fully localized.

The reduced-motion preference bypasses `AnimatedContent` for round-result transitions. With reduced motion disabled, the result card uses the shared Compose animation; with it enabled, the result is rendered directly.

The main content column is centered with a maximum width on larger windows while remaining fill-width on narrow Android layouts.

## Logging

`SafeLogger` records structured local lifecycle/debug events and redacts sensitive metadata keys. Free-form backup contents, profile names, history text, credentials, and tokens are intentionally excluded from event fields.

## Security and release architecture

- CI compiles shared/desktop Kotlin, runs shared tests, builds and lints Android, and verifies the optional Rust engine.
- CodeQL performs independent Java/Kotlin static analysis.
- `security.yml` runs a deterministic high-confidence committed-secret scan and GitHub dependency review for pull requests.
- Dependabot tracks Gradle, Cargo, and GitHub Actions dependency updates.
- Pull-request validation runs on `pull_request`; post-merge validation runs on `push` to `main`, avoiding duplicate branch/PR executions.
- Tagged/manual release automation creates an unsigned Android release APK and per-OS desktop distributables. Signing secrets are intentionally outside this repository.

See `docs/adr/`, `docs/repository-settings.md`, and `docs/release.md` for stable decisions and repository-side release controls.
