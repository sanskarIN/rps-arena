# Release Guide

1. Ensure `main` CI is green.
2. Update `CHANGELOG.md` and version numbers in `androidApp/build.gradle.kts` and `desktopApp/build.gradle.kts`.
3. Run Kotlin tests, Android debug assembly, and Rust tests.
4. Build signed Android artifacts using private signing credentials outside the repository.
5. Build desktop packages on their native operating systems.
6. Create a Git tag such as `v1.0.0` and a GitHub release with checksums and release notes.
7. Never commit signing keys, store credentials, tokens, or private certificates.

The open-source repository should remain buildable without release secrets.
