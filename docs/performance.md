# Performance

RPS Arena is intentionally lightweight. Core play has no remote API, media feed, large database, or background synchronization, so performance work focuses on bounded state, fast rule resolution, responsive Compose rendering, and avoiding unnecessary allocations.

## Engineering budgets

These are design targets rather than published benchmark claims:

- winner resolution stays constant-time;
- CPU choice remains effectively instant for the small local move history;
- persisted recent history remains capped at 30 entries;
- timer countdown work is one lightweight coroutine per active timed turn;
- no artificial loading delays;
- no network work on the game path;
- no unbounded in-memory or persisted round list outside the active match.

Do not publish numeric startup, FPS, CPU, memory, or battery claims until they are measured on real release-candidate builds.

## Current safeguards

- The winner table is a fixed finite rule set.
- CPU strategies operate on a small gesture set and current-match history only.
- Persistent history is capped.
- Preferences store compact local values.
- Backup import validates bounded history before saving.
- No image-loading framework, analytics SDK, ad SDK, or polling worker is included.

## Profiling

### Android

Use Android Studio profiling and Compose tooling on a release-like build. Measure startup, recomposition hot spots, memory, and timer behavior before optimizing.

### Desktop

Use JVM tooling such as Java Flight Recorder for measured regressions. Test on the operating system whose package is being released.

### Rust engine

Benchmarks belong in `rust-engine/benches/`. The Rust mirror is optional, so performance results must not be presented as Kotlin application performance unless the runtime actually uses that engine.

## Optimization rules

- Measure first.
- Do not add caches without documented ownership, invalidation, and bounds.
- Do not sacrifice correctness, determinism, privacy, or accessibility for micro-optimizations.
- Prefer simpler shared code until profiling identifies a real bottleneck.

## Future private-room mode

Before shipping LAN/private-room multiplayer, define budgets for discovery interval, message size, queue length, reconnect behavior, memory, and battery/network use. Network retries must be bounded and must not interfere with offline game modes.
