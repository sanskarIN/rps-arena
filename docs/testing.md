# Testing Guide

## Required shared suite

```bash
gradle :shared:allTests --stacktrace
```

Coverage includes:

- every classic rule direction and draw behavior;
- extended Lizard–Spock rules;
- seeded CPU determinism and allowed-gesture constraints;
- settings/stat codec round trips;
- legacy settings migration;
- versioned backup export/import and malformed-backup rejection;
- recent win/loss/draw trend aggregation;
- CPU and local-two-player timeout scoring;
- backup restore refreshing in-memory state;
- private-room code validation, two-participant limits, sender validation, and event exchange.

## Android build verification

```bash
gradle :androidApp:assembleDebug --stacktrace
```

This verifies Android packaging, shared Android compilation, resources, launcher assets, and the primary entry point.

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
9. backup export can restore settings, stats, and history after a reset;
10. malformed backup text is rejected without overwriting valid local data;
11. reduced-motion mode removes result transition animation;
12. keyboard and TalkBack/accessible navigation checks from `docs/accessibility.md` pass.

## CI gate

`.github/workflows/ci.yml` runs shared tests, Android debug assembly, desktop classes, and Rust tests. `.github/workflows/codeql.yml` performs Kotlin/Java static security analysis.

A release candidate should not be merged while any required check is failing or still pending.
