# Testing

RPS Arena concentrates automated confidence on deterministic rules, CPU behavior, persistence, state transitions, data safety, primary UI behavior, and platform build integration.

## Shared tests

Current common tests cover:

- classic Rock–Paper–Scissors relationships;
- Lizard–Spock relationships;
- valid seeded CPU output and reproducibility;
- settings/stat/config codecs;
- migration of the previous seven-field settings format;
- corrupted-stat fallback;
- local profile create, rename, activate, delete, input normalization, and maximum-count validation;
- versioned V2 backup/restore including local profiles;
- migration of V1 backups to the default local profile;
- non-mutating backup preview;
- oversized and malformed backup rejection;
- atomic rejection before mutation when backup history is invalid;
- validated history replacement;
- match-configuration persistence;
- CPU timeout behavior;
- same-device two-player timeout handoff and explicit turn phases;
- timer-disabled behavior;
- one-step history-clear undo and invalidation when new history is written;
- complete local-data reset;
- recent-history win/loss/draw trend parsing, limits, reserved-looking local profile names, and decisive win rate;
- private-room protocol validation and in-memory transport contract behavior;
- Kotlin/Rust rule-contract fixtures.

## Compose UI regression test

`RpsArenaUiTest` exercises the primary shared UI journey using an isolated in-memory repository and stable semantic test tags:

1. first-run onboarding is rendered;
2. onboarding is completed;
3. Home is reached;
4. Play is opened;
5. Rock is selected;
6. the first round result is rendered.

The test intentionally targets semantic controls rather than visible English text so copy changes do not make the primary journey test fragile.

Run all shared target tests:

```bash
gradle --no-daemon :shared:allTests
```

The desktop shared suite, including the shared Compose UI test, can also be run directly:

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

## Documentation and security verification

```bash
python scripts/check_docs_links.py
python scripts/check_for_secrets.py
```

The committed-secret scanner intentionally looks only for high-confidence credential patterns and is complementary to, not a replacement for, GitHub-native secret scanning/push protection when those features are available.

## CI policy

`.github/workflows/ci.yml` runs Kotlin/platform verification and Rust checks. The shared test task includes the primary Compose UI regression test through the desktop test runtime.

`.github/workflows/codeql.yml` runs Java/Kotlin CodeQL analysis independently from the Android application build.

`.github/workflows/docs.yml` validates repository-local Markdown links.

`.github/workflows/security.yml` runs the committed-secret scanner on pushes and pull requests and GitHub dependency review on pull requests.

`.github/dependabot.yml` tracks Gradle, Cargo, and GitHub Actions dependency updates.

Once branch protection/rulesets are enabled, only exact check names that have been observed successfully should be made required. See `docs/repository-settings.md`.

## Regression policy

Every deterministic bug should receive a regression test when practical. Fixes should not rely on manual testing alone for rules, persistence, serialization, profile validation, timer behavior, backup safety, trend derivation, or state transitions.

Primary UI journeys should use stable semantic tags and assertions rather than screenshots or layout-coordinate taps.

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
- create, rename, select, and delete local profiles;
- profile persistence after restart;
- generated V2 backup preview and successful restore;
- V1 backup migration;
- malformed/oversized backup rejection without data loss;
- history clear and one-step undo;
- undo invalidation after new history is written;
- recent W/L/D trend and decisive win-rate display;
- completed-round `Copy result` action and copied text content;
- confirmed full local reset;
- light/dark/system themes;
- reduced-motion round-result behavior;
- About external links.

## Accessibility checklist

- keyboard navigation on desktop;
- gesture semantic labels with a screen reader;
- trend semantics that announce Win/Loss/Draw rather than relying on color;
- copy-result action has an explicit text label and success state;
- large text/scaling where supported;
- no clipped critical controls on narrow screens;
- bounded readable layout on wide desktop windows;
- text or symbols accompanying status, not color alone;
- reduced-motion behavior for result transitions.

## Release evidence

Do not call a commit release-ready solely because source inspection looks correct. The release candidate must have acceptable latest CI, CodeQL, documentation, and security workflow results. Production screenshots must be captured from an actual verified build rather than fabricated from design mockups.

## Future automated coverage

Expand Compose UI coverage beyond the existing primary journey to profile management, Settings, backup preview/import, history undo, reduced-motion behavior, and accessibility semantics where the test stack can represent them reliably. Android instrumentation and deeper desktop interaction tests should be added where they provide reproducible value rather than fragile snapshots.

Any production LAN/private-room transport requires malformed-input, disconnect, replay, concurrency, and fuzz/property testing before release.
