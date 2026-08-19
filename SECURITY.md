# Security Policy

## Supported versions

Security fixes are prioritized for the latest released version and the current `main` branch.

## Reporting a vulnerability

Please do **not** open a public issue containing exploit details, credentials, private data, or sensitive reproduction material.

Report security concerns privately to:

- `sanskarin@outlook.in`
- `supportramsandesh@gmail.com`

Include the affected version, platform, impact, a minimal reproduction, and suggested mitigation if known. Remove secrets and personal data from logs before sending them.

## Project security model

RPS Arena is offline-first and intentionally avoids account systems, analytics SDKs, ads SDKs, and mandatory networking. Local settings and recent game data are stored in platform-provided preferences. The app does not implement custom cryptography and does not treat game history as secret storage.

## Dependency and source controls

- Dependencies are pinned in `gradle/libs.versions.toml`.
- Dependabot checks Gradle and GitHub Actions dependencies.
- CodeQL analyzes Java/Kotlin source.
- CI compiles, tests, builds Android, and runs Android lint.
- Secrets, signing keys, and local environment files are excluded by `.gitignore` and must never be committed.
