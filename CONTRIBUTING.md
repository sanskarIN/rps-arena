# Contributing

Thanks for helping improve RPS Arena.

## Local setup

Follow [`docs/setup.md`](docs/setup.md) for the full toolchain and platform setup. The validated baseline uses JDK 17, Gradle 9.5.1, Kotlin 2.4.10, Compose Multiplatform 1.11.0, and Android SDK 36.

## Commit identity

For owner-authored local commits, configure:

```bash
git config user.name "Sanskar"
git config user.email "sanskarin@outlook.in"
```

The GitHub connector/API may use the identity attached to the authenticated integration; `.mailmap` documents the canonical owner email.

## Commit style

Prefer small, cohesive Conventional Commit-style changes:

- `feat:` user-visible behavior;
- `fix:` defect correction;
- `test:` test-only coverage;
- `docs:` documentation;
- `refactor:` behavior-preserving structure change;
- `perf:` measured performance improvement;
- `build:` build/dependency configuration;
- `ci:` automation and quality gates;
- `chore:` maintenance that does not fit another category.

Do not create empty commits, meaningless one-line churn, or split inseparable code/test changes only to inflate commit count.

## Required verification

Before opening or updating a pull request, run the smallest relevant checks and then the repository verification suite for broad changes:

```bash
python3 scripts/check_format.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

On Windows, `scripts/verify.ps1` mirrors the repository checks. On Unix-like systems, use `scripts/verify.sh`.

## Pull requests

- Keep changes focused and explain user-visible impact.
- Add regression tests for bug fixes and tests for new business rules.
- Preserve deterministic seeded behavior unless the change explicitly revises the CPU contract.
- Preserve backup compatibility or add an explicit migration/version bump.
- Keep history and imported data bounded and validated.
- Update accessibility behavior/docs when changing core controls, timers, animation, or copy.
- Update `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md` for milestone/release work.
- Do not add tracking, ads, mandatory cloud dependencies, or unnecessary network access.
- Never commit signing keys, API tokens, store credentials, certificates, personal user data, or generated secrets.

## Networking changes

Primary gameplay is offline-first. Networking changes must remain behind the optional `PrivateRoomGateway` boundary, document permissions and privacy impact, validate participant/message input, and keep the game usable when networking is unavailable or disabled.

## Localization

Core user-facing copy belongs in `ArenaStrings.kt`. Update all shipped language catalogs when adding a new core string, or document why platform-specific copy cannot use the shared catalog.

## Accessibility

Use [`docs/accessibility.md`](docs/accessibility.md) as the manual review checklist. Important actions need visible labels, appropriate target sizes, keyboard access on desktop, and non-color-only state feedback.

## Security reports

Do not disclose suspected vulnerabilities in a public issue before following [`SECURITY.md`](SECURITY.md).

**Made by the Sanskar.**
