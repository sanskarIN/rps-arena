# Testing Guide

## Required shared suite

```bash
gradle :shared:allTests --stacktrace
```

Coverage includes:

- every classic rule direction and draw behavior;
- extended Lizard–Spock rules;
- seeded CPU determinism and allowed-gesture constraints;
- replayable seeded CPU behavior through `ArenaState`;
- match timer and win-target invariants;
- settings/stat codec round trips;
- legacy settings migration;
- invalid-statistics rejection;
- local player-name sanitization and bounds;
- recent-history bounds and newline sanitization;
- versioned backup export/import, unknown-record rejection, and non-destructive malformed-import behavior;
- recent win/loss/draw trend aggregation;
- CPU and local-two-player timeout scoring;
- backup restore refreshing in-memory state;
- English/Hindi gesture, difficulty, match-mode, version metadata, and achievement-copy catalogs;
- private-room code validation, two-participant limits, sender validation, positive-round validation, lifecycle-event authority, event exchange, and idempotent close behavior.

## Compose desktop UI smoke tests

The `desktopTest` source set uses Compose Multiplatform's UI test runtime. The current smoke suite verifies:

- onboarding reaches the home screen and primary Play journey;
- Rock/Paper/Scissors controls are rendered on the primary gameplay screen;
- Settings can switch core navigation copy from English to Hindi;
- Hindi gameplay renders localized Rock/Paper/Scissors labels;
- Hindi achievements render localized title and description copy;
- backup/import controls are exposed;
- local-data reset requires explicit confirmation.

Run them directly with:

```bash
gradle :shared:desktopTest --stacktrace
```

They are also included in the shared test gate for the supported desktop target.

## Android quality/build verification

```bash
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

This verifies Android lint, packaging, shared Android compilation, resources, launcher assets, and the primary entry point.

Android device/emulator instrumentation remains a platform-dependent follow-up; the repository does not pretend that a desktop UI runner is equivalent to TalkBack or device behavior.

## Desktop build verification

```bash
gradle :desktopApp:classes --stacktrace
```

For native packaging on a supported host OS:

```bash
gradle :desktopApp:packageDistributionForCurrentOS --stacktrace
```

## Rust engine

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

The Rust engine is optional and independent of the Kotlin app, but its tests remain part of CI.

## Manual product checks

Before release, verify:

1. onboarding completion persists;
2. CPU match works in Classic and Lizard–Spock variants;
3. local two-player hides Player 1's gesture before Player 2 chooses;
4. every match format resets and finishes correctly;
5. replaying the same seed and player move sequence produces the same CPU choices;
6. timers restart per turn and score the correct timeout winner;
7. recent trend numbers match recent history;
8. player name and language persist across restart;
9. English/Hindi changes update gameplay choices, round results, history rendering, settings feedback, and achievements without changing stored game rules;
10. match-mode and timer chips wrap instead of clipping on a narrow phone-width viewport;
11. backup export can restore settings, stats, and history after a reset;
12. malformed backup text is rejected without overwriting valid local data;
13. reduced-motion mode removes result transition animation;
14. keyboard and TalkBack/accessible navigation checks from `docs/accessibility.md` pass.

## CI gate

`.github/workflows/ci.yml` runs formatting/version checks, shared tests (including desktop UI smoke tests), Android lint/debug assembly, desktop classes, and Rust tests. `.github/workflows/codeql.yml` performs Kotlin/Java static security analysis.

A release candidate should not be merged while any required check is failing or still pending.
