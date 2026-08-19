# Development Guide

## Architecture boundaries

- `shared/.../model`: immutable product data and enums.
- `shared/.../engine`: rules and deterministic CPU decisions.
- `shared/.../data`: local persistence, migration, backup/import, and data validation.
- `shared/.../state`: product state transitions and match orchestration.
- `shared/.../ui`: Compose Multiplatform presentation and bilingual copy catalog.
- `shared/.../network`: transport-neutral private-room contracts plus the offline in-memory reference adapter.
- `androidApp` and `desktopApp`: platform entry points and packaging only.

Business rules should not be duplicated in platform modules or Composables.

## Working on a change

1. Create a focused branch.
2. Add or update the smallest relevant tests first when fixing a regression.
3. Implement the behavior in the owning layer.
4. Run the narrow verification task.
5. Run the full shared tests before opening a pull request.
6. Run Android and desktop compilation for cross-platform changes.
7. Update documentation and `what_changed.md` for milestone-level work.

## Verification commands

```bash
gradle :shared:allTests
gradle :androidApp:assembleDebug
gradle :desktopApp:classes
(cd rust-engine && cargo test --all-targets)
```

GitHub Actions repeats these checks and CodeQL analyzes Kotlin/Java changes.

## Persistence compatibility

Settings currently use a `settings_v2` record. The repository transparently migrates the legacy `settings_v1` representation. Never overwrite an older format without a migration path.

The text backup format begins with `RPS_ARENA_BACKUP|1`. Import is intentionally strict: malformed, oversized, duplicate, or unknown records are rejected before local data is replaced.

## Timed rounds

Allowed timer values are declared by `MatchConfig.ALLOWED_TIMER_SECONDS`. `0` means disabled. A timeout is a real round result and therefore updates match score, statistics, recent trends, and history consistently.

## Private-room development

`PrivateRoomGateway` is transport-neutral. The included `InMemoryPrivateRoomGateway` performs no networking and exists for deterministic development/testing. A LAN adapter must remain optional, explicitly enabled, two-player bounded, input validated, and independent of mandatory cloud infrastructure.

## UI and localization

Visible core copy belongs in `ArenaStrings.kt`. English and Hindi are currently shipped. New product copy should avoid reintroducing scattered hard-coded strings when a catalog entry is appropriate.

## Commit style

Prefer Conventional Commits and small cohesive changes, for example:

- `feat: add ...`
- `fix: handle ...`
- `test: cover ...`
- `docs: document ...`
- `refactor: simplify ...`
- `build: configure ...`
- `ci: verify ...`
