# Repository Maintenance and Change Playbook

This guide explains how to maintain RPS Arena safely after the current milestone: version bumps, dependency upgrades, persistence changes, new languages, new gameplay features, documentation upkeep, Git hygiene, release preparation, and recovery from failed changes.

## Maintenance principle

Prefer small, cohesive, reviewable commits. A large project can have many commits without making them meaningless.

Good separation examples:

```text
feat: add model invariant
feat: wire state behavior
test: cover new state transition
docs: explain new behavior
ci: enforce new validation
```

Avoid splitting one inseparable code edit into artificial single-line commits solely to inflate count.

## Canonical owner identity

`.mailmap` declares:

```text
Sanskar <sanskarin@outlook.in>
```

For repository-local Git commits:

```bash
git config user.name "Sanskar"
git config user.email "sanskarin@outlook.in"
```

The authenticated GitHub integration can still attribute API-created commits according to the connected account.

## Repository text/style policy

### `.editorconfig`

Global defaults:

- UTF-8;
- LF line endings;
- final newline;
- spaces, 4-space indentation;
- trim trailing whitespace.

YAML/JSON/TOML use 2-space indentation.

Markdown disables editor-level trailing-whitespace trimming because two trailing spaces have valid Markdown hard-break meaning. `scripts/check_format.py` separately allows exactly that intentional Markdown form while rejecting accidental whitespace.

### `.gitattributes`

Normalizes text to LF in Git.

Special cases:

- `.bat` -> CRLF;
- PNG/ICO/JAR -> binary.

This reduces noisy cross-platform line-ending diffs.

### `.gitignore`

Excludes generated/local/sensitive paths including:

- Gradle cache/build output;
- class/log/out files;
- IDE metadata;
- `local.properties`;
- Android captures;
- keystore files;
- generated native installers;
- Rust `target/`;
- current Rust `Cargo.lock` policy.

Before adding a new generated tool, review whether its output belongs in `.gitignore`.

Do not use `.gitignore` as a security control after a secret was committed: remove/rotate the secret and follow Git/security incident procedures.

## Before starting a change

1. sync with current `main`;
2. inspect `ROADMAP.md` and open issues/PRs;
3. identify architecture owner layer;
4. create focused branch;
5. understand compatibility/security/privacy impact;
6. choose tests before implementation when fixing a bug.

Example:

```bash
git switch main
git pull --ff-only
git switch -c feat/example
```

`--ff-only` prevents Git from creating an unexpected merge commit during pull; if histories diverge, resolve deliberately.

## Version bump procedure

Current app semantic version is represented in three source locations:

- `androidApp/build.gradle.kts` -> `versionName`;
- `desktopApp/build.gradle.kts` -> `packageVersion`;
- `shared/.../ui/AppMetadata.kt` -> `APP_VERSION`.

Android also has independent integer:

```text
versionCode
```

### Patch example

For future `1.1.1`:

1. set Android `versionName = "1.1.1"`;
2. increment Android `versionCode` above 2;
3. set desktop `packageVersion = "1.1.1"`;
4. set shared `APP_VERSION = "1.1.1"`;
5. run:

```bash
python3 scripts/check_version.py
```

6. update `CHANGELOG.md`/release docs;
7. run full validation;
8. tag only after merged `main` is green.

Do not reuse an Android `versionCode` for a later distributed build.

## Dependency/toolchain maintenance

Primary source:

```text
gradle/libs.versions.toml
```

Environment pins also exist in workflows.

When changing Kotlin/Compose/AGP/Gradle/JDK/SDK versions:

- read compatibility/release notes;
- update one logical compatibility group;
- run all platform builds/tests;
- update toolchain/build/CI docs;
- inspect deprecations rather than suppressing them indefinitely.

See `docs/toolchain.md` and `docs/build-system.md`.

## Adding a dependency

Before adding:

1. prove existing standard/platform APIs are insufficient;
2. identify license;
3. review maintenance/security footprint;
4. check binary size/permissions/transitives;
5. add version through catalog when Gradle-managed;
6. add dependency to narrowest module/source set;
7. test all affected targets;
8. document why it exists.

Avoid adding Android-only libraries to `commonMain`.

## Removing a dependency

Search for:

- imports/API usage;
- Gradle catalog alias;
- module dependency declaration;
- docs;
- license notices if any;
- CI/workflow usage.

Run clean build after removal to avoid local cache hiding a missing declaration.

## Gameplay change workflow

For gestures/rules/match modes/timers/CPU:

1. update domain model;
2. update rule/state implementation;
3. add tests;
4. update localization labels;
5. update shared UI;
6. update optional Rust parity when in scope;
7. update domain/test docs;
8. run full gate.

See `docs/domain-and-gameplay.md`.

## Persistence change workflow

Treat changes as compatibility-sensitive.

1. define new schema/version behavior;
2. preserve old decode/migration path;
3. validate malformed input;
4. test non-destructive failure;
5. update backup behavior if settings/stats meaning changes;
6. review privacy/security;
7. document exact migration.

See `docs/storage-and-backup.md`.

## Localization change workflow

When adding a core visible string:

- add typed property to `ArenaStrings` when appropriate;
- fill every shipped language;
- update UI to use catalog value;
- update UI tests when stable user-facing text is intentionally asserted.

When adding language:

- add stable `AppLanguage` enum;
- full catalog;
- achievement copy;
- settings selector;
- persistence/backup tests;
- layout/accessibility review.

See `docs/localization.md`.

## Private-room/network workflow

Current release has no real LAN transport.

A future implementation must remain optional and pass security/privacy design review before adding permissions.

Do not start by adding `INTERNET` permission. Start with protocol/threat model and explicit product flow.

See `docs/private-room-protocol.md`.

## Android change workflow

For manifest/resources/build/activity/platform storage:

```bash
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

Also run shared/desktop checks when shared code changed.

Review merged permissions, min/target SDK behavior, signing, and accessibility.

## Desktop change workflow

At minimum:

```bash
gradle :desktopApp:classes --stacktrace
gradle :shared:desktopTest --stacktrace
```

Run desktop app manually for interaction changes. Package on target host for packaging changes.

## Rust change workflow

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
cargo package --manifest-path rust-engine/Cargo.toml
```

Keep optional crate scope explicit; do not make Kotlin app depend on Rust without an architecture decision.

## Test maintenance

When a test fails after intentional behavior change, ask:

- did behavior requirement change? -> update implementation + test + docs;
- is test asserting an implementation detail? -> rewrite around behavior;
- is production code broken? -> fix production code, do not weaken test.

Do not delete a regression test merely to make CI green without understanding why it failed.

See `docs/test-catalog.md`.

## Documentation maintenance

Every tracked file must be represented in `docs/repository-file-reference.md`.

The documentation coverage checker uses `git ls-files` so adding a new tracked path requires updating the file reference.

When changing behavior, review docs by dependency:

- public feature -> README + changelog + domain/platform docs;
- command/build -> command/build/toolchain docs;
- storage -> storage/privacy/security;
- networking -> protocol/privacy/security/architecture;
- CI -> CI/CD/validation/release;
- new test -> test catalog/testing;
- new file -> repository file reference.

## Root documentation ownership

### `README.md`

Public landing page and quick-start/high-level product truth.

### `CHANGELOG.md`

Release-visible changes by version.

### `ROADMAP.md`

Planned/completed milestone status, not a guarantee of delivery date.

### `CONTRIBUTING.md`

Contributor process and required checks.

### `SECURITY.md`

Private vulnerability reporting/security support policy.

### `PRIVACY.md`

Data collection/storage/network behavior.

### `SUPPORT.md`

Non-security help/reporting route.

### `CODE_OF_CONDUCT.md`

Community interaction expectations.

### `LICENSE`

MIT license text. Do not casually edit legal license wording.

### `what_changed.md`

Detailed project handoff/change checkpoint requested for this repository workflow.

## `what_changed.md` maintenance

Keep it useful rather than append-only noise.

Record:

- major implemented behaviors;
- migrations;
- validation status;
- known limitations;
- representative meaningful commits;
- remaining external/credential-dependent work.

When a final CI run changes from queued -> passed/failed, update the validation section accurately before merge if the workflow allows a final docs-only commit followed by revalidation.

## Changelog policy

Use sections such as:

```text
Added
Changed
Fixed
Security
Deprecated
Removed
```

Only document shipped/release-candidate behavior under its intended version.

Do not mark a feature Verified solely because code was written; use CI/manual evidence.

## Roadmap policy

Roadmap checkboxes should reflect implemented source, not aspirations.

Keep credential/platform-dependent items unchecked until actually delivered.

Examples:

- real LAN adapter;
- signed store artifact workflow;
- macOS notarization;
- iOS packaging.

## Pull request maintenance

Use `.github/pull_request_template.md`.

Important review dimensions:

- tests/builds;
- data compatibility;
- privacy/security;
- accessibility;
- version/release impact;
- docs.

Keep PR body updated when scope expands materially.

## Branch protection

Recommended settings are documented in `docs/github-settings.md`.

Do not bypass required status checks to merge a failing/queued candidate just because implementation appears complete.

## Release preparation

Before tag:

1. merge only green PR;
2. confirm `main` green;
3. confirm app version consistency;
4. review changelog/release notes;
5. run manual release/accessibility checks;
6. inspect privacy/security statements;
7. create/push annotated version tag;
8. observe release workflow;
9. verify artifact checksums/files;
10. only distribute signed/store artifacts through authorized signing process.

## Generated files policy

Do not review generated build output as if it were hand-maintained source unless diagnosing packaging.

Common generated paths:

```text
.gradle/
**/build/
rust-engine/target/
out/
```

They are ignored and reproducible.

## Secret incident response

If a real secret is committed:

1. revoke/rotate it immediately;
2. stop relying on deleting the file as sufficient remediation;
3. assess repository/history exposure;
4. follow provider-specific key/token revocation guidance;
5. clean history only when justified/coordinated;
6. update automation/secret storage to prevent recurrence.

Do not place revoked secret value into an issue while discussing the incident.

## Failed migration/release recovery

Prefer fix-forward versioned changes over rewriting published history.

For a broken release:

- stop/prominently mark distribution when needed;
- identify affected version;
- prepare regression test + fix;
- increment patch version;
- publish clear notes;
- preserve old tag/release history unless security/legal necessity requires stronger action.

## Documentation completion check

Before calling repository docs complete:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_coverage.py
```

Then run build/test validation because documentation changes can also affect CI YAML/scripts/links/version examples when those files are edited.

## Maintenance checklist for every merged change

- [ ] Correct owner layer changed.
- [ ] Tests added/updated when behavior changed.
- [ ] Cross-platform build impact considered.
- [ ] Persistence/backup compatibility considered.
- [ ] Security/privacy/network permission impact considered.
- [ ] Accessibility/localization impact considered.
- [ ] Relevant docs updated.
- [ ] Repository file reference includes any new path.
- [ ] `what_changed.md` updated for milestone-level work.
- [ ] Required CI/CodeQL passed on exact head.
