# Development

RPS Arena favors small, explicit changes and a single source of truth for game behavior.

## Workflow

1. Branch from the current default branch.
2. Reproduce an existing bug before fixing it when possible.
3. Change the smallest relevant layer.
4. Add a regression test for deterministic behavior.
5. Run the smallest relevant verification command.
6. Run the full quality suite before requesting merge.
7. Update documentation and `CHANGELOG.md` for user-visible changes.
8. Update `what_changed.md` before handing work to another session.

## Quality commands

```bash
gradle --no-daemon :shared:compileKotlinDesktop
gradle --no-daemon :shared:desktopTest
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
gradle --no-daemon :desktopApp:classes
```

Optional Rust verification:

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

## Domain changes

Do not duplicate winner rules in UI, persistence, or platform modules. Gesture relationships belong in the shared rules engine. CPU behavior must delegate to the same valid-gesture model and remain explainable in documentation.

When changing game modes, define the finishing condition explicitly and cover it with tests.

## Persistence changes

Persist application state through `ArenaRepository`, not directly from screens. Keep platform storage adapters small. New incompatible formats require an explicit version or migration path.

Backup import must validate all required sections before replacing user data. Never silently reinterpret an unknown backup version.

## Compose UI

- Route state changes through `ArenaState`.
- Prefer Material controls for reliable focus/touch semantics.
- Keep narrow layouts scrollable rather than clipping controls.
- Provide semantic descriptions when emoji/icons alone are ambiguous.
- Never communicate winner/error state only by color.
- Keep destructive actions confirmed when they cannot be easily undone.
- Avoid non-essential animation; any future motion must respect reduced-motion preference.

## Dependencies

Pin versions through `gradle/libs.versions.toml`. Add a dependency only for a clear product/engineering need. Prefer maintained upstream libraries and avoid copying untrusted binary artifacts into the repository.

## Secrets

Never commit:

- API keys or tokens;
- signing keys/keystores/passwords;
- production credentials;
- private user data;
- private endpoints;
- generated secrets.

RPS Arena v1 needs none of these at runtime.

## Commits

Use focused Conventional Commit messages when practical:

```text
feat: add ...
fix: handle ...
test: cover ...
refactor: simplify ...
perf: improve ...
docs: document ...
ci: verify ...
build: configure ...
chore: maintain ...
```

Project-owner local commits should configure `sanskarin@outlook.in` as the Git email.
