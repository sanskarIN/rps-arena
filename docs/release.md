# Release

RPS Arena releases should be reproducible from a clean checkout and should not be tagged until required verification succeeds.

## Versioning

Use semantic versioning:

- patch: compatible bug/security fixes;
- minor: backwards-compatible features;
- major: intentional incompatible behavior or data-format changes.

Keep the Android `versionName`, desktop `packageVersion`, `CHANGELOG.md`, and release notes aligned.

## Release candidate checklist

1. Start from a clean checkout of the intended commit.
2. Confirm no local secrets or untracked release inputs are required.
3. Run:

```bash
gradle :shared:desktopTest
gradle :shared:compileKotlinDesktop
gradle :desktopApp:compileKotlin
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

4. Run GitHub CodeQL and dependency checks.
5. Manually test the primary journeys from `docs/testing.md`.
6. Review `docs/accessibility.md` release checklist.
7. Build platform packages from the release commit.
8. Verify documentation and screenshots reflect the released UI.
9. Update `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md`.
10. Create an annotated version tag only after the candidate is accepted.

## Desktop packages

Compose Desktop is configured for:

- Windows MSI
- macOS DMG
- Linux DEB

Native installers are platform-specific; build the package on the corresponding operating system when required by the Compose packaging toolchain.

## Android

The repository currently verifies a debug APK in CI. Store-distribution signing material must remain outside Git and outside CI logs. A future Play release workflow should use encrypted repository/environment secrets and the minimum permissions necessary.

Never commit `.jks`, `.keystore`, private keys, passwords, or exported signing credentials.

## GitHub release automation

The release workflow builds testable artifacts from version tags. A tag is not proof that every native installer exists on every OS; release notes must state exactly which artifacts were produced by the workflow.

## Rollback

If a release introduces a serious defect, fix forward with a tested patch when possible. If distribution must be paused, mark the affected release clearly and do not delete security-relevant history.

## Release notes

Include:

- user-visible changes;
- fixed defects;
- accessibility/privacy/security changes;
- known limitations;
- supported platforms;
- exact commit/tag;
- migration or backup notes when storage behavior changes.
