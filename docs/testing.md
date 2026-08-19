# Testing

RPS Arena treats the domain rules and deterministic CPU engine as the highest-confidence test targets because every UI and platform delegates to them.

## Current automated suites

### Domain

`GameRulesTest` covers classic outcomes, Lizard–Spock relationships, and match finish thresholds.

`CpuStrategyTest` checks deterministic sequences, variant-safe moves, and valid counter behavior.

### Persistence

`PersistenceTest` covers settings/stats/history round trips, portable backup import, invalid backup rejection, and corrupt-setting fallback.

### Application state

`AppControllerTest` covers private pass-and-play handoff, scoring, best-of match completion, and restart behavior that preserves lifetime stats.

## Local commands

```bash
gradle :shared:desktopTest
gradle :shared:compileKotlinDesktop
gradle :desktopApp:compileKotlin
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

## CI policy

`.github/workflows/ci.yml` runs compilation, shared tests, Android debug build, and Android lint. A failure should block merging into the default branch once branch protection is enabled.

`.github/workflows/codeql.yml` performs Java/Kotlin static security analysis.

## Regression policy

Every reproducible bug in deterministic domain, parsing, persistence, or state behavior should receive a regression test before or with the fix.

## Manual accessibility checks

Before a release candidate:

- increase system text scaling and confirm critical controls remain reachable;
- verify gesture choices expose meaningful labels to accessibility services;
- verify keyboard focus order on desktop;
- test light and dark themes for readable contrast;
- enable reduced motion and confirm animated result transitions stop;
- confirm status is communicated with text/symbols, not color alone.

## Manual product checks

- Classic CPU match from fresh data
- Lizard–Spock match
- same-device two-player privacy handoff
- Best of 3 / Best of 5 / Tournament completion
- Endless and Streak play without forced completion
- timer expiry
- seed replay behavior
- persistence after relaunch
- backup generation and valid/invalid import
- history clear and full local-data reset
- external About links

## Future coverage

After the first clean CI baseline, add Compose UI tests for navigation and settings, Android instrumentation smoke tests, and desktop interaction tests where they provide stable value. LAN/private-room work must add protocol fuzzing and malformed-input tests before release.
