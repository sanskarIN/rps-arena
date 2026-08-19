# Testing

RPS Arena concentrates automated confidence on deterministic rules, CPU behavior, persistence, state transitions, and platform build integration.

## Shared tests

Current common tests cover:

- classic Rock–Paper–Scissors relationships;
- Lizard–Spock relationships;
- valid seeded CPU output and reproducibility;
- settings/stat/config codecs;
- migration of the previous seven-field settings format;
- corrupted-stat fallback;
- versioned backup/restore and invalid-backup rejection;
- match-configuration persistence;
- CPU timeout behavior;
- same-device two-player timeout handoff;
- timer-disabled behavior;
- complete local-data reset.

Run:

```bash
gradle --no-daemon :shared:desktopTest
```

## Kotlin/platform verification

```bash
gradle --no-daemon :shared:compileKotlinDesktop
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
gradle --no-daemon :desktopApp:classes
```

## Rust verification

The optional Rust rules mirror must stay warning-free and formatted:

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

## CI policy

`.github/workflows/ci.yml` runs Kotlin/platform verification and Rust checks. Once branch protection is enabled, required CI should block merging failures to `main`.

`.github/workflows/codeql.yml` runs Java/Kotlin CodeQL analysis independently from Android SDK availability.

## Regression policy

Every deterministic bug should receive a regression test when practical. Fixes should not rely on manual testing alone for rules, persistence, serialization, timer behavior, or state transitions.

## Manual product checklist

Before a release candidate, verify:

- first-run onboarding;
- Classic CPU match at each difficulty;
- Lizard–Spock CPU match;
- local two-player hidden first move;
- Best of 3 and Best of 5 completion;
- Tournament first-to-5 completion;
- Endless and Streak continuing without forced completion;
- seed replay with identical player inputs;
- each timer preset and timeout move;
- local two-player timeout on both turns;
- settings/config persistence after restart;
- backup generation and successful restore;
- malformed backup rejection without data loss;
- history clear;
- confirmed full local reset;
- light/dark/system themes;
- About external links.

## Accessibility checklist

- keyboard navigation on desktop;
- gesture semantic labels with a screen reader;
- large text/scaling where supported;
- no clipped critical controls on narrow screens;
- text or symbols accompanying status, not color alone;
- reduced-motion behavior for any future animation.

## Future automated coverage

After the shared compile baseline is continuously green, add stable Compose UI tests for onboarding/navigation, settings, backup controls, and the primary play journey. Android instrumentation and desktop interaction tests should be added where they provide reproducible value rather than fragile snapshots.

Any future LAN/private-room protocol requires malformed-input, disconnect, replay, and fuzz/property testing before release.
