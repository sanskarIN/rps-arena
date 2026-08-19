# Security Policy

## Supported versions

Security fixes are applied to the latest supported release and the `main` branch. Older releases may receive a fix only when the maintainer explicitly documents continued support.

| Version | Supported |
|---|---|
| 1.1.x | Yes |
| 1.0.x | Security-critical fixes until 1.1.0 adoption is established |
| < 1.0 | No |

## Reporting a vulnerability

Do not publish sensitive vulnerability details, private data, credentials, or exploit material in a public issue.

Report suspected vulnerabilities privately to `sanskarin@outlook.in` and include:

- repository name and affected version/commit;
- affected platform;
- impact and realistic prerequisites;
- minimal safe reproduction steps;
- whether the issue involves backup/import, local persistence, release artifacts, or optional room/network architecture;
- any suggested mitigation that does not require disclosing secrets.

Support contact: `supportramsandesh@gmail.com`.

## Security boundaries

RPS Arena intentionally keeps the primary attack surface small:

- no account or authentication service;
- no Android internet permission in the primary app;
- Android automatic backup disabled and SharedPreferences excluded from cloud/device-transfer backup policy;
- no analytics, advertising, telemetry, or cloud-model SDK;
- bounded recent history;
- bounded, versioned backup input with strict validation before writes;
- no custom cryptography;
- release credentials and signing material remain outside the repository;
- private-room contracts are optional and currently ship with a no-network in-memory reference adapter only.

## Backup/import security

Treat backup text as untrusted input. The importer must keep size and record-count limits, reject unknown/duplicate malformed records, validate statistics invariants, and avoid partial replacement when validation fails.

Do not expand the backup format with file paths, executable content, scripts, remote URLs, credentials, or arbitrary serialized objects.

Android automatic backup is not used as an alternate backup channel. The explicit app backup format is the supported user-controlled portability mechanism.

## Optional networking security

Any future LAN implementation must stay behind `PrivateRoomGateway`, require explicit user opt-in, validate room/message input, stop network activity when a room closes, and keep primary offline play functional without network permission.

Room codes are convenience rendezvous identifiers for game moves, not a substitute for high-assurance authentication. Do not use the room channel for sensitive personal information.

## Logging security

`SafeLogger` is sink-neutral and uses a no-op sink by default, so the current app does not emit telemetry. Before an optional sink receives an event, values whose keys resemble password, secret, token, authorization, cookie, email, backup, content, or payload data are replaced with `[REDACTED]`; non-sensitive values are length-bounded.

Do not treat redaction as permission to log arbitrary personal data. Any future sink must remain local/explicit unless the privacy policy and product architecture are deliberately changed and reviewed.

## Dependencies and CI

Dependabot and CodeQL are configured in the repository. Primary CI also runs shared tests, Android lint/build, desktop compilation, Rust tests, formatting, relative documentation-link validation, exhaustive tracked-file documentation coverage, committed-secret pattern checks, Android privacy-contract validation, and version consistency.

The focused `Security checks` workflow independently re-runs the committed-secret and Android privacy checks and performs pull-request dependency review with high-severity findings configured to fail.

A green CI/security run is evidence for known checks, not proof that no future vulnerability exists.

## Secrets

Never commit:

- Android keystores or passwords;
- Apple/Windows signing certificates or private keys;
- GitHub, store, cloud, or email tokens;
- real API keys;
- private user backups or gameplay data collected from another person;
- generated credentials in examples.

Use placeholders in documentation and authorized secret stores for release credentials. `scripts/check_for_secrets.py` provides an additional fail-closed check for several high-confidence credential formats, but it does not replace human review or platform secret scanning.

## Ownership and review

`.github/CODEOWNERS` routes default review ownership to `@sanskarIN` and explicitly covers automation/security, shared core code, Rust, and platform packaging paths. Repository rulesets can require code-owner review when desired.

## Disclosure handling

The maintainer will reproduce safely, assess affected supported versions, prepare a fix and regression test when practical, update release/security notes, and coordinate disclosure after users have a reasonable mitigation path.

**Made by the Sanskar.**
