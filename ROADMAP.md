# RPS Arena Roadmap

RPS Arena prioritizes reliable offline play, transparent game logic, accessibility, privacy, and maintainable releases over feature count.

## v1.0 — Release candidate

- [x] Classic and Lizard–Spock rules
- [x] CPU and same-device two-player play
- [x] Easy, Normal, and Expert transparent CPU strategies
- [x] Best-of-3, Best-of-5, Tournament, Endless, and Streak modes
- [x] Replayable seeded challenges
- [x] Configurable turn timers with deterministic timeout moves
- [x] Persisted match setup, lifetime statistics, achievements, and recent history
- [x] Up to six local-only player profiles with validated display names
- [x] Recent W/L/D trend and decisive win rate derived from local history
- [x] Versioned V2 local backup/restore with V1 migration
- [x] Non-mutating backup preview before import
- [x] Confirmed full-data reset and one-step recent-history clear undo
- [x] Android and Windows/macOS/Linux desktop targets
- [x] System/light/dark theme and reduced-motion preferences
- [x] Localization-ready centralized English application copy
- [x] Structured redacting local logging
- [x] Tested private-room protocol/transport boundary with no production network dependency
- [x] CI, CodeQL, Dependabot, docs validation, release-artifact workflow, Rust verification, committed-secret scan, and dependency review
- [x] Setup, architecture, testing, accessibility, performance, release, troubleshooting, ADR, privacy/security, and repository-settings documentation
- [ ] Complete final clean workflow verification after the audit branch settles
- [ ] Complete the final manual Android/desktop release-candidate checklist
- [ ] Capture real Android and desktop screenshots from verified release-candidate builds

## v1.1 — Accessibility, localization, and UX hardening

- Expand automated Compose UI coverage for onboarding, navigation, profiles, settings, backup controls, and the primary play journey.
- Add platform-specific keyboard-focus and screen-reader regression coverage.
- Move the remaining gesture labels from the domain model into a resource-backed localization layer.
- Add translated resources only after English resource keys and layout behavior are stable.
- Consider richer local trend visualizations while retaining text equivalents and zero analytics/telemetry.
- Evaluate optional per-profile statistics only with an explicit migration plan; v1 lifetime statistics are intentionally device-wide.
- Add platform-native file picker/export integration for backup text if it remains reliable across supported targets.

## v1.2 — Optional private-room transport experiment

The pure protocol and `PrivateRoomTransport` boundary already exist. A production transport remains deliberately disabled.

Before exposing private rooms in the product:

- threat-model room discovery, peer identity assumptions, replay handling, malformed packets, disconnects, cancellation, and resource exhaustion;
- add transport-level malformed-input, replay, concurrency, and fuzz/property tests;
- implement explicit host/join UX with no automatic external connectivity;
- keep local rules authoritative instead of trusting a peer-provided result;
- preserve full CPU and pass-and-play functionality without networking;
- do not require a cloud account or mandatory backend;
- add Android network permissions only if an actual reviewed Android transport requires them.

## Later

- Consider iOS only when native packaging and accessibility testing can be maintained at the same quality level.
- Keep the Rust engine optional unless it provides measurable testing, educational, or performance value.
- Add new modes only when they have clear game rules, tests, documentation, and accessible interaction design.
- Revisit release signing automation only when signing credentials can be managed securely outside the repository.
