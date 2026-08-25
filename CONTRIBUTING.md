# Contributing

Thanks for helping improve RPS Arena.

## Local setup

Follow [`docs/setup.md`](docs/setup.md) for the short setup path and [`docs/toolchain.md`](docs/toolchain.md) for deep installation/upgrade guidance. The validated project baseline uses JDK 17, Gradle 9.5.1 in CI, Kotlin 2.4.10, Compose Multiplatform 1.11.0, and Android SDK 36.

The repository currently does **not** track a Gradle Wrapper, so local commands use an installed `gradle` executable. See [`docs/build-system.md`](docs/build-system.md) before changing this architecture.

## Documentation map

Use [`docs/documentation-index.md`](docs/documentation-index.md) to find the correct deep reference for architecture, commands, platforms, storage, localization, networking, testing, release, maintenance, and terminology.

Every Git-tracked file must appear in [`docs/repository-file-reference.md`](docs/repository-file-reference.md). CI enforces this through `scripts/check_docs_coverage.py`.

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
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

On Windows, `scripts/verify.ps1` mirrors the repository checks. On Unix-like systems, use `scripts/verify.sh`.

The documentation coverage command uses Git's tracked-file list, so run it from a real Git checkout rather than an exported source folder with Git metadata removed.

## Pull requests

- Keep changes focused and explain user-visible/engineering impact.
- Add regression tests for bug fixes and tests for new business rules.
- Preserve deterministic seeded behavior unless the change explicitly revises the CPU contract.
- Preserve backup compatibility or add an explicit migration/version bump.
- Keep history and imported data bounded and validated.
- Preserve `android:allowBackup="false"`, SharedPreferences backup/device-transfer exclusions, and the no-internet primary manifest unless a deliberate reviewed privacy/networking policy change replaces them.
- Update accessibility behavior/docs when changing controls, timers, animation, focus, or copy.
- Update all shipped language catalogs/tests for core visible copy.
- Update `docs/repository-file-reference.md` for every added, renamed, or removed tracked file.
- Add new subject-area documentation to `docs/documentation-index.md` when appropriate.
- Update `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md` for milestone/release work.
- Do not add tracking, ads, mandatory cloud dependencies, or unnecessary network access.
- Never commit signing keys, API tokens, store credentials, certificates, personal user data, or generated secrets.
- Treat `scripts/check_for_secrets.py` as defense in depth; rotate/revoke a real credential if it is ever exposed rather than only removing it from the latest diff.
- Do not merge while required CI/Security/CodeQL checks on the exact current PR head are queued, cancelled without replacement, or failing.

## Adding or renaming files

The repository enforces exhaustive tracked-file documentation.

After adding a new tracked path such as:

```text
shared/src/commonMain/kotlin/in/sanskar/rpsarena/example/NewFeature.kt
```

add the exact backtick-wrapped path to `docs/repository-file-reference.md` with a useful explanation, then run:

```bash
python3 scripts/check_docs_coverage.py
```

A filename mention without meaningful ownership/purpose documentation may satisfy the mechanical path checker but should still fail human review.

## Android privacy changes

Read [`PRIVACY.md`](PRIVACY.md), [`SECURITY.md`](SECURITY.md), and [`docs/android-platform.md`](docs/android-platform.md) before changing the Android manifest, platform persistence, or backup rules.

Run:

```bash
python3 scripts/check_android_privacy.py
```

Do not remove the checker simply because a new platform feature conflicts with it. If the product's privacy policy intentionally changes, update implementation, policy resources, validation, documentation, and release notes together.

## Networking changes

Primary gameplay is offline-first. Networking changes must remain behind the optional `PrivateRoomGateway` boundary, document permissions/privacy impact, validate participant/message input, and keep the game usable when networking is unavailable or disabled.

Read [`docs/private-room-protocol.md`](docs/private-room-protocol.md) before implementing a real LAN transport. The current in-memory adapter is not production LAN multiplayer.

## Persistence and backup changes

Read [`docs/storage-and-backup.md`](docs/storage-and-backup.md). Persistence formats are compatibility contracts: preserve old decode/migration paths, validate imported data before writes, keep limits explicit, and add regression tests.

Android automatic backup is intentionally disabled; the explicit versioned text export/import is the supported user-controlled backup mechanism.

## Logging/diagnostics changes

`SafeLogger` has a no-op default sink and redacts sensitive key categories before any explicit sink receives fields. Do not introduce a remote/telemetry sink as a casual diagnostics change. Any sink that changes data collection/transmission behavior requires privacy/security review and documentation.

## Localization

Core user-facing copy belongs in `ArenaStrings.kt` or another typed localization structure such as `AchievementStrings.kt`. Update all shipped language catalogs when adding a new core string, or document why platform-specific technical copy cannot use the shared catalog.

See [`docs/localization.md`](docs/localization.md).

## Testing

[`docs/test-catalog.md`](docs/test-catalog.md) explains the responsibility of every tracked automated test. Add tests in the narrowest appropriate layer and do not remove a failing regression test merely to make CI green.

## Accessibility

Use [`docs/accessibility.md`](docs/accessibility.md) as the manual review checklist. Important actions need visible labels, appropriate target sizes, keyboard access on desktop, and non-color-only state feedback.

## Build/toolchain changes

Read [`docs/toolchain.md`](docs/toolchain.md), [`docs/build-system.md`](docs/build-system.md), and [`docs/ci-cd.md`](docs/ci-cd.md). Treat Gradle/Kotlin/Compose/AGP/JDK/SDK updates as compatibility changes requiring cross-platform validation.

## Release changes

Read [`docs/release.md`](docs/release.md) and [`docs/ci-cd.md`](docs/ci-cd.md). Signing credentials remain outside Git. Tag only a validated `main` commit.

## Security reports

Do not disclose suspected vulnerabilities in a public issue before following [`SECURITY.md`](SECURITY.md).

## Ownership

`.github/CODEOWNERS` routes default repository review ownership to `@sanskarIN` and explicitly covers security/automation, shared core, Rust, and platform packaging. Repository rulesets determine whether code-owner approval is mandatory.

## Long-term maintenance

[`docs/maintenance.md`](docs/maintenance.md) is the repository maintenance/change playbook covering versions, dependencies, persistence, languages, networking, platforms, tests, documentation, releases, and secret incidents.

**Made by the Sanskar.**
