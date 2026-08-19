# Roadmap

RPS Arena prioritizes correctness, offline reliability, accessibility, and maintainability over feature count.

## v1.0 — Core arena

- [x] Classic and Lizard–Spock rules
- [x] CPU and same-device two-player
- [x] Best-of, tournament, endless, and streak configurations
- [x] Deterministic CPU seeds
- [x] Local history, stats, achievements, settings, and backup text
- [x] Android and desktop targets
- [x] CI, unit tests, CodeQL, Dependabot, security/privacy docs
- [ ] Complete clean-run CI verification and release-candidate packaging
- [ ] Capture real Android and desktop screenshots after verified builds

## v1.1 — UX hardening

- [ ] Add screen-reader regression checks and expanded keyboard navigation tests
- [ ] Add richer match trend visualization without collecting telemetry
- [ ] Add import preview before replacing local data
- [ ] Add translated string catalogs after community review
- [ ] Add optional sound/haptic feedback with independent accessibility controls

## v1.2 — Private-room experiment

- [ ] Prototype opt-in LAN/private-room discovery with no mandatory cloud service
- [ ] Threat-model discovery, room codes, replay protection, and input validation before enabling multiplayer
- [ ] Keep offline solo and pass-and-play fully functional without network permissions

## Later

- Optional iOS target when release testing, packaging, and platform-specific accessibility can be maintained to the same standard.
- Optional Rust rules-engine experiment only if it provides measurable educational or reliability value rather than duplicate complexity.
