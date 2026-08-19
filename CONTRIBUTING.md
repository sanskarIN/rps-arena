# Contributing to RPS Arena

Thanks for helping improve RPS Arena. Contributions should preserve the project's offline-first, privacy-conscious, accessible, and testable design.

## Before starting

1. Read `README.md`, `docs/architecture.md`, `docs/development.md`, and `docs/testing.md`.
2. Check existing issues and pull requests to avoid duplicate work.
3. For a larger architectural change, open an issue first so the design and migration impact can be discussed before implementation.
4. For security-sensitive findings, follow `SECURITY.md` instead of filing a public vulnerability report.

## Development setup

Follow `docs/setup.md`. The normal verification baseline uses JDK 17+, Gradle 9.5.0, and Android SDK 36 for Android builds. Rust is optional for application development but required when changing `rust-engine/`.

Useful local entry points:

```bash
./scripts/verify.sh
```

On Windows PowerShell:

```powershell
./scripts/verify.ps1
```

See `docs/testing.md` for individual commands.

## Contribution principles

- Keep Kotlin game rules independent from UI and platform APIs.
- Route persisted application data through `ArenaRepository`.
- Keep CPU behavior local, explainable, and reproducible when seeded.
- Do not make core gameplay depend on a network, account, analytics service, or advertising SDK.
- Treat local profile names and backup text as user-controlled local data.
- Do not log free-form profile names, history, backups, credentials, or tokens.
- Keep destructive actions confirmed or undoable where practical.
- Preserve accessibility semantics and non-color-only status communication.
- Keep user-facing application copy in the localization boundary rather than scattering new strings through domain/state code.
- Do not expose the private-room protocol through a production transport without the separate security/testing work documented in the roadmap.

## Tests

Add focused regression coverage for deterministic changes whenever practical. This is especially important for:

- rule relationships;
- CPU behavior;
- persistence and migrations;
- backup validation/import safety;
- local profiles;
- timers and match completion;
- recent history/trends;
- private-room protocol parsing;
- primary UI journeys and semantics.

Bug fixes should demonstrate the failure mode in a test when the behavior is reproducible.

## Documentation

Update documentation in the same pull request when behavior, setup, security/privacy boundaries, supported targets, release steps, or public APIs change.

Run:

```bash
python scripts/check_docs_links.py
```

Keep `CHANGELOG.md` updated for user-visible changes and keep `what_changed.md` useful as the current implementation handoff during large multi-session work.

## Security and secrets

Never commit:

- API keys or access tokens;
- passwords;
- Android keystores or signing keys;
- private certificates;
- private user data;
- production credentials/endpoints;
- generated secrets.

Run:

```bash
python scripts/check_for_secrets.py
```

before pushing security-sensitive changes. See `SECURITY.md` for responsible disclosure.

## Commit style

Prefer small, meaningful commits. Conventional Commit prefixes are encouraged:

- `feat:`
- `fix:`
- `test:`
- `refactor:`
- `perf:`
- `docs:`
- `ci:`
- `build:`
- `chore:`

Do not split work into meaningless commits solely to increase commit count. A commit should leave the repository understandable and should normally represent one coherent change.

Project-owner local commits use `sanskarin@outlook.in` as the configured Git email.

## Pull requests

A strong pull request should:

- explain what changed and why;
- identify affected platforms;
- mention migrations or compatibility changes;
- include tests or explain why a deterministic test is not practical;
- update docs/changelog when required;
- avoid unrelated formatting churn;
- pass the latest CI, documentation, CodeQL, and security checks before release-bound merging.

Use the repository pull-request template and keep review conversations focused on the code and product behavior.

## Licensing

By contributing, you agree that your contribution is provided under the repository's MIT License unless explicitly stated otherwise for material that is compatible and properly attributed.
