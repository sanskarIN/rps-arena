# Release Guide

## Release gate

A release candidate is eligible only when:

- `main` CI is green on the exact candidate;
- focused Security checks are green;
- CodeQL is green;
- repository formatting passes;
- relative documentation links pass;
- exhaustive tracked-file documentation coverage passes;
- high-confidence committed-secret scanning passes;
- Android privacy-contract validation passes;
- cross-platform version consistency passes;
- shared tests pass;
- Android lint and compilation pass as configured;
- desktop classes/package tasks pass on the relevant host;
- Web JS+Wasm compatibility distribution builds;
- iOS simulator framework and SwiftUI/Xcode host build on macOS;
- Rust tests pass;
- changelog, roadmap, platform docs, privacy notes, and `what_changed.md` match the shipped behavior;
- no signing key, store credential, token, certificate, provisioning secret, or private user data is committed.

## v2.5.8 exact-head validation

The merged v2.5.8 release revision is:

```text
4136aff448e9489a3e8252ceea7c1e9e79d17c19
```

The following exact-head gates are green on that revision:

- CI push run `32853891608` — successful;
- Security checks push run `32853891297` — successful;
- CodeQL push run `32853891464` — successful;
- version/build-code consistency — `2.5.8` / `20508`;
- cross-platform shared/UI/Android/desktop/Web/iOS/Rust validation — successful through the CI workflow.

This evidence is source validation, not proof that the tag-triggered release packaging has completed. The `v2.5.8` tag must point to the audited final release revision and the generated artifacts/checksums must be verified before publishing the release.

## Version locations

For v2.5.8 the public version is declared in:

- `androidApp/build.gradle.kts` (`versionCode = 20508`, `versionName = "2.5.8"`);
- `desktopApp/build.gradle.kts` (`packageVersion = "2.5.8"`);
- `iosApp/iosApp/Info.plist` (`CFBundleShortVersionString = 2.5.8`, `CFBundleVersion = 20508`);
- `iosApp/iosApp.xcodeproj/project.pbxproj` (`MARKETING_VERSION = 2.5.8`, `CURRENT_PROJECT_VERSION = 20508`);
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AppMetadata.kt` (`APP_VERSION = "2.5.8"`);
- About UI, which renders the shared version constant;
- `CHANGELOG.md`.

The mobile build number follows:

```text
major * 10000 + minor * 100 + patch
```

`scripts/check_version.py` validates the Android/iOS numeric mapping and synchronizes Android, desktop, iOS, Xcode, and shared semantic metadata.

The Web application renders the shared `APP_VERSION`, so it does not maintain a second independent semantic version declaration.

## Local verification

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
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

On macOS additionally run:

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

Run the manual checks in `docs/testing.md`, `docs/accessibility.md`, and the relevant platform guide before tagging a stable release.

## Android privacy preflight

Before release, verify that the primary Android manifest still has no internet permission, keeps `android:allowBackup="false"`, and points to both backup-policy XML files. The XML policies exclude the complete SharedPreferences domain from legacy backup, Android cloud backup, and device-to-device transfer.

`scripts/check_android_privacy.py` enforces these source invariants in CI, the focused security workflow, local verification, and release preflight.

## GitHub release artifacts

The tag workflow is designed to build reproducible public artifacts without repository signing secrets. It re-runs the fast source/security/privacy/version gates before platform build/package work.

Current release jobs produce or validate:

- Android unsigned/public release APK;
- Linux desktop `.deb` package;
- Web JS+Wasm compatibility distribution ZIP (`rps-arena-web.zip`);
- iOS device and simulator `RpsArenaShared.framework` ZIPs plus an unsigned simulator-host validation build;
- optional Rust `.crate` package;
- SHA-256 checksums for published GitHub Release artifacts.

The iOS framework ZIPs are reproducible integration artifacts, **not** signed App Store IPA files. Windows/macOS desktop native package signing and App Store/TestFlight distribution remain credential-bearing release steps outside the public workflow.

Recommended release tag:

```text
v2.5.8
```

Tag only the audited final `main` commit. The tag workflow is an additional release gate and does not replace green pull-request CI, Security checks, and CodeQL evidence on that exact source.

## Release finalization checklist

1. Merge the release-finalization documentation/reference cleanup branch after its exact-head CI is green.
2. Confirm `main` still resolves to the intended v2.5.8 release revision.
3. Create `v2.5.8` from that exact revision.
4. Wait for the tag-triggered Release workflow to finish.
5. Inspect every configured Android, desktop, Web, iOS, and Rust artifact.
6. Verify every generated SHA-256 checksum against the corresponding artifact.
7. Publish/finalize the GitHub release notes only after all artifact checks pass.
8. Confirm post-release `main` CI remains green.

Do not start the v2.5.9 runtime/package version transition until this sequence is complete.

## Android signing

Keep keystores, passwords, Play credentials, and signing configuration outside Git. Signed store artifacts should be produced in an authorized environment using private credentials injected at release time.

## iOS/App Store signing

Keep Apple signing certificates/private keys, provisioning credentials, App Store Connect keys, and account credentials outside Git. Public CI validates the simulator host and produces framework artifacts without requiring those secrets.

A signed TestFlight/App Store archive should be produced only in an authorized environment with controlled credentials.

## Desktop signing/notarization

Windows signing and macOS signing/notarization require external certificates/accounts. Keep these credentials outside the repository. Unsigned packages can still be generated for source/build validation where platform policy permits.

## Web deployment

The release Web ZIP contains static compatibility-distribution output. Deploy its contents to a static host that preserves generated paths/MIME behavior. Test both modern Wasm startup and the JS compatibility path before calling a hosted deployment stable.

## Release notes

Include:

- user-visible features and fixes;
- Android/iOS/iPadOS/Windows/Linux/macOS/Web support status;
- Web upstream stability/limitations when relevant;
- privacy/networking and automatic-backup behavior;
- migration/explicit-backup compatibility notes;
- signing/distribution limitations;
- verification summary;
- checksum information for published artifacts;
- contact/support and MIT license links.

## Rollback

If a release artifact or tag points to an incorrect commit, stop distribution, document the problem, fix forward on a new patch version, and avoid rewriting published release history unless there is a compelling security reason.
