# Security Policy

RPS Arena is an offline-first open-source game. Security reports are welcome for application code, persistence/backup handling, build/release automation, the optional Rust rules mirror, and any future private-room transport.

## Supported versions

Before the first stable tag, security fixes are made on the current `main` development line. After `v1.0.0`, the latest stable release and the current development line are the supported targets unless a release note explicitly states otherwise.

## Reporting a vulnerability

Prefer GitHub private vulnerability reporting when it is enabled for this repository. If that option is unavailable, contact:

- Security/support: `supportramsandesh@gmail.com`
- Business/maintainer: `sanskarin@outlook.in`

Please do not publish an undisclosed vulnerability, exploit details, credentials, private user data, or sensitive logs in a public issue.

A useful report includes:

- affected version or commit SHA;
- affected platform;
- concise impact description;
- minimal reproduction steps or a small proof of concept;
- expected versus observed behavior;
- whether the issue can expose or corrupt local data;
- any suggested mitigation, if known.

Do not include real credentials, signing material, personal data, or third-party secrets in a report. Use synthetic test data.

## Security boundaries in v1

- Core gameplay requires no account, backend, analytics SDK, advertising SDK, or Android internet permission.
- Android application backup is disabled and shared-preference data is excluded from the configured legacy/current backup and device-transfer rule sets.
- Local profiles are display-name-only device-local identities, not authentication accounts.
- Backup files/text are readable user-controlled exports, not encrypted secret storage.
- Backup decoding is versioned, size-bounded, validated before mutation, and regression tested.
- Structured logging intentionally excludes profile names, history text, backup contents, credentials, and tokens.
- `Copy result` writes only the selected round summary to the platform clipboard after explicit user action.
- The private-room code in v1 is a pure protocol/transport boundary; there is no production network transport.
- Release signing keys, keystores, certificates, passwords, and tokens must remain outside Git.

## Automated security checks

Repository automation includes:

- CodeQL analysis for Java/Kotlin;
- high-confidence committed-secret scanning with `scripts/check_for_secrets.py`;
- Android privacy-contract validation with `scripts/check_android_privacy.py`, including no INTERNET permission, disabled application backup, and required shared-preference exclusions;
- pull-request dependency review when supported by GitHub;
- Dependabot coverage for Gradle, Cargo, and GitHub Actions;
- normal build/test/lint verification before release.

The Android privacy validator is also invoked by both local verification entry points so manifest or extraction-rule changes are checked before a contributor relies on hosted CI.

GitHub-native secret scanning, push protection, dependency alerts, private vulnerability reporting, and branch rules should also be enabled when available. See `docs/repository-settings.md`.

## Future networking

A production LAN/private-room implementation must receive a separate security review before release. At minimum it must define and test:

- untrusted-message validation;
- replay/duplicate handling;
- disconnect and cancellation behavior;
- message/room/resource bounds;
- local rule authority;
- malformed input behavior;
- transport-specific concurrency behavior;
- permission and discovery behavior on each platform.

## Coordinated disclosure

Please allow a reasonable opportunity to investigate and prepare a fix before public disclosure. When a report is confirmed, the maintainer may coordinate a patch, release note, advisory, and credit according to the reporter's preference and the sensitivity of the issue.

Security reports should be factual and limited to systems/data you are authorized to test.
