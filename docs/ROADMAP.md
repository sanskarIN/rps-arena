# Roadmap

## 1.0 baseline

- [x] Classic and Lizard–Spock rules
- [x] CPU and local two-player
- [x] Multiple match formats
- [x] Offline stats/history/settings
- [x] Achievements and onboarding
- [x] Android and desktop entry points
- [x] Accessibility-oriented settings
- [x] Optional Rust rules mirror
- [x] CI and project governance docs

## 1.1 offline portability

- [x] Import/export backup format with schema versioning

The schema-v1 backup feature validates all sections before repository writes and keeps backup handling fully offline. See [BACKUP.md](BACKUP.md).

## 1.2 localization foundation

- [x] Compose Multiplatform string resources
- [x] English fallback catalog
- [x] Hindi translation catalog
- [x] Locale catalog/placeholder validation in local verification and CI

Shared UI localization remains offline and follows the runtime locale. Existing `history_v1` summaries remain display-as-saved for compatibility with backup schema 1. See [LOCALIZATION.md](LOCALIZATION.md).

## Future-compatible enhancements

- [ ] Optional LAN/private-room play using an explicit opt-in networking module
- [ ] Dedicated UI automation suite on Android and desktop
- [ ] Structured localized history persistence with backward migration from `history_v1`
- [ ] Additional translation catalogs beyond English and Hindi
- [ ] Signed release automation after store/release credentials are configured securely

Future networking must remain optional and must not weaken the offline-first default.
