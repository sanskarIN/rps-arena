# Development

## Working principles

RPS Arena favors small, explicit components and a single source of truth for game rules. Keep domain logic independent from platform APIs and avoid duplicating rule relationships inside UI code.

## Recommended workflow

1. Create a focused branch.
2. Reproduce the behavior before changing it when fixing a bug.
3. Update the smallest relevant domain or adapter layer.
4. Add or update regression tests.
5. Run the smallest relevant verification command.
6. Run the complete local verification before opening a pull request.
7. Update documentation and `CHANGELOG.md` for user-visible behavior.

## Verification commands

```bash
gradle :shared:desktopTest
gradle :shared:compileKotlinDesktop
gradle :desktopApp:compileKotlin
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

## Adding a rule or variant

Rules belong in `shared/.../domain/GameRules.kt`. A new gesture must be represented in the domain model, included only in the appropriate variant, covered by exhaustive outcome tests, and understood by the CPU strategy.

Never infer a winner independently inside a screen.

## Adding persistence

Prefer extending `AppRepository` while preserving defensive parsing and versioned migration behavior. Do not make platform entry points parse application data.

If a storage format becomes incompatible, introduce a new version and migration path rather than reinterpreting old data.

## Compose UI

- Keep screens stateless where practical and route mutations through `AppController`.
- Use shared design tokens from `Theme.kt`.
- Support narrow and wide layouts.
- Provide semantic labels for ambiguous controls.
- Avoid color-only communication.
- Respect reduced-motion preference for new transitions.
- Keep destructive actions reversible or confirmed when meaningful.

## Logging

The current release does not require application telemetry. If structured local logging is introduced later, never record backup contents, tokens, private file paths, emails entered by users, or other personal data.

## Dependencies

Add versions through `gradle/libs.versions.toml`. Prefer maintained libraries with a clear need. Avoid adding a dependency for behavior that the standard library or existing stack already provides safely.

## Commit conventions

Use Conventional Commits when practical. Examples:

```text
feat: add ...
fix: handle ...
test: cover ...
refactor: simplify ...
perf: reduce ...
docs: document ...
ci: verify ...
build: configure ...
chore: maintain ...
```

The project-owner Git email is `sanskarin@outlook.in`.
