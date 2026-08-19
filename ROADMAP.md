# RPS Arena Roadmap

RPS Arena prioritizes reliable offline play, transparent game logic, accessibility, and maintainable releases over feature count.

## v1.0 — Release candidate

- [x] Classic and Lizard–Spock rules
- [x] CPU and same-device two-player play
- [x] Easy, Normal, and Expert transparent CPU strategies
- [x] Best-of-3, Best-of-5, Tournament, Endless, and Streak modes
- [x] Replayable seeded challenges
- [x] Configurable turn timers
- [x] Persisted match setup, local statistics, achievements, and recent history
- [x] Versioned local backup/restore and full-data reset
- [x] Android and desktop targets
- [x] Theme and reduced-motion preferences
- [x] CI, CodeQL, Dependabot, release-artifact workflow, and Rust verification
- [ ] Complete final clean CI verification after the audit branch settles
- [ ] Capture real Android and desktop screenshots from verified release-candidate builds

## v1.1 — Accessibility and UX hardening

- Expand automated Compose UI coverage for navigation, settings, and data controls.
- Add additional keyboard-focus and screen-reader regression coverage.
- Improve local trend visualization without analytics or telemetry.
- Introduce an externalized localization resource layer for additional languages after English copy stabilizes.
- Add import preview/diff before replacing local backup data.

## v1.2 — Optional private-room experiment

- Define a LAN/private-room transport behind explicit opt-in.
- Threat-model room discovery, input validation, replay protection, disconnects, and malformed packets before enabling UI.
- Preserve full CPU and pass-and-play functionality without networking.
- Do not require a cloud account or mandatory backend.

## Later

- Consider iOS only when native packaging and accessibility testing can be maintained at the same quality level.
- Keep the Rust engine optional unless it provides measurable testing, educational, or performance value.
- Add new modes only when they have clear game rules, tests, documentation, and accessible interaction design.
