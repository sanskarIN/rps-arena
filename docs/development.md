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
gradle --no-daemon :shared:allTests
gradle --no-daemon :androidApp:assembleDebug
gradle --no-daemon :androidApp:lintDebug
gradle --no-daemon :desktopApp:classes
python scripts/check_docs_links.py
python scripts/check_for_secrets.py
```

Optional Rust verification:

```bash
cd rust-engine
cargo fmt --all -- --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-targets --all-features
```

## Domain changes

Do not duplicate winner rules in UI, persistence, networking, or platform modules. Gesture relationships belong in the shared rules engine. CPU behavior must delegate to the same valid-gesture model and remain explainable in documentation.

When changing game modes, define the finishing condition explicitly and cover it with tests.

Keep user-facing copy out of domain/state objects when practical. Current application/settings/achievement/turn/profile/data/trend copy belongs in `ui/Strings.kt`. Gesture labels are the remaining known localization debt and should move to resources before adding multiple locales.

## Local profiles

Profiles are local identities, not accounts. Keep them free of passwords, tokens, email addresses, remote IDs, or authentication state.

- Normalize and bound display names through `ArenaRepository`.
- Keep profile IDs internal and deterministic enough for local storage/backup references.
- Do not write profile display names to structured logs.
- Keep the maximum profile count explicit and tested.
- If per-profile statistics are ever introduced, add a storage migration instead of changing the meaning of existing device-wide statistics silently.

## Persistence changes

Persist application state through `ArenaRepository`, not directly from screens. Keep platform storage adapters small. New incompatible formats require an explicit version or migration path.

Backup decoding must validate all required sections before replacing user data. Never silently reinterpret an unknown backup version. `previewBackup` and `importBackup` must share validation rules so a preview cannot approve data that import interprets differently.

Destructive operations should be undoable when practical. Recent-history clear therefore keeps one in-memory undo snapshot until new history/import/reset invalidates it. Full reset remains confirmed because it intentionally removes multiple independent state categories.

Recent trends are derived from persisted history. Do not create a second trend persistence format unless there is a clear migration/integrity reason.

## Private-room boundary

`PrivateRoomProtocol` is pure shared code and `PrivateRoomTransport` is only an interface in v1. Do not add a production transport casually.

Any implementation must:

- remain opt-in;
- validate untrusted peer messages;
- keep the local rules engine authoritative;
- define cancellation, replay, disconnect, and resource bounds;
- add transport-specific tests;
- avoid making CPU/pass-and-play depend on networking.

## Compose UI

- Route state changes through `ArenaState`.
- Prefer Material controls for reliable focus/touch semantics.
- Keep narrow layouts scrollable rather than clipping controls.
- Bound primary content width on large desktop windows.
- Provide semantic descriptions when emoji/icons/single-letter trend values alone are ambiguous.
- Never communicate winner/error/selection/trend state only by color.
- Keep destructive actions confirmed when they cannot be easily undone.
- Keep backup import disabled until a valid non-mutating preview exists.
- Any non-essential motion must respect the reduced-motion preference.

## Dependencies

Pin versions through `gradle/libs.versions.toml`. Add a dependency only for a clear product/engineering need. Prefer maintained upstream libraries and avoid copying untrusted binary artifacts into the repository.

Dependabot covers Gradle, Cargo, and GitHub Actions. Pull requests run GitHub dependency review when supported by the repository settings.

## Secrets

Never commit:

- API keys or tokens;
- signing keys/keystores/passwords;
- production credentials;
- private user data;
- private endpoints;
- generated secrets.

RPS Arena v1 needs none of these at runtime. Run `python scripts/check_for_secrets.py` before pushing security-sensitive changes; GitHub-native secret scanning/push protection should also be enabled when available.

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
