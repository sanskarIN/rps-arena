# Command Reference

This guide explains the commands used by RPS Arena, what each command does, and when to use it. Run commands from the repository root unless a section says otherwise.

## Important: no Gradle Wrapper is currently tracked

The repository does **not** currently contain `gradlew`, `gradlew.bat`, or `gradle/wrapper/*`.

Use the installed/setup Gradle command:

```bash
gradle --version
```

The validated CI baseline is Gradle 9.5.1 with JDK 17.

The Xcode direct-integration script also uses `gradle`, so local macOS/iOS work requires Gradle to be available to the Xcode build environment.

## Full repository verification

Unix-like systems:

```bash
bash scripts/verify.sh
```

PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify.ps1
```

These helpers run the portable source/build gates configured in the repository. Host-specific iOS/Xcode validation still requires macOS.

## Fast source gates

Run before expensive compilation:

```bash
python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
```

### Formatting

```bash
python3 scripts/check_format.py
```

Checks repository text for:

- UTF-8 decoding;
- final newline;
- accidental trailing whitespace;
- intentional Markdown two-space hard breaks without treating them as generic errors.

The checker is read-only.

### Documentation links

```bash
python3 scripts/check_docs_links.py
```

Validates repository-relative Markdown links and rejects paths that resolve outside the repository or point to missing local targets.

External links are not fetched.

### Documentation coverage

```bash
python3 scripts/check_docs_coverage.py
```

Runs `git ls-files -z` and requires every tracked path to appear exactly in backticks in:

```text
docs/repository-file-reference.md
```

Add/rename/delete a tracked file -> update the exhaustive reference in the same change.

### Committed-secret patterns

```bash
python3 scripts/check_for_secrets.py
```

Checks several high-confidence private-key/credential patterns. It reports category/path/line without printing the matched secret value.

It supplements, but does not replace, GitHub secret scanning and human review.

### Android privacy contract

```bash
python3 scripts/check_android_privacy.py
```

Requires the primary Android app to preserve the documented offline/privacy boundary:

- automatic backup disabled;
- correct backup-rule references;
- SharedPreferences excluded from legacy/cloud/device-transfer backup;
- no primary Android Internet permission.

### Cross-platform version consistency

```bash
python3 scripts/check_version.py
```

Checks:

- Android `versionName`;
- Android `versionCode`;
- desktop `packageVersion`;
- iOS `CFBundleShortVersionString`;
- iOS `CFBundleVersion`;
- Xcode `MARKETING_VERSION`;
- Xcode `CURRENT_PROJECT_VERSION`;
- shared `APP_VERSION`;
- About rendering of shared `APP_VERSION`.

Mobile build codes must equal:

```text
major * 10000 + minor * 100 + patch
```

For v2.5.8 the required numeric code is:

```text
20508
```

## Gradle task syntax

Example:

```bash
gradle :shared:allTests --stacktrace
```

Meaning:

- `gradle` runs installed Gradle;
- `:shared:allTests` addresses the `allTests` task in module `shared`;
- `--stacktrace` prints a Java/Kotlin stack trace when Gradle fails.

Useful inspection commands:

```bash
gradle tasks
gradle :shared:tasks
gradle :webApp:tasks
```

## Shared tests

```bash
gradle :shared:allTests --stacktrace
```

Runs configured shared multiplatform tests, including common business/data/protocol/localization/logging coverage and configured desktop UI tests.

Dedicated desktop UI tests:

```bash
gradle :shared:desktopTest --stacktrace
```

## Android commands

### Lint

```bash
gradle :androidApp:lintDebug --stacktrace
```

Release lint:

```bash
gradle :androidApp:lintRelease --stacktrace
```

### Debug APK

```bash
gradle :androidApp:assembleDebug --stacktrace
```

Typical output is under:

```text
androidApp/build/outputs/apk/debug/
```

### Release APK

```bash
gradle :androidApp:assembleRelease --stacktrace
```

The public repository contains no private store-signing credentials, so a public release build is not automatically a signed Play Store artifact.

## Windows/Linux/macOS desktop commands

### Compile

```bash
gradle :desktopApp:classes --stacktrace
```

### Run

```bash
gradle :desktopApp:run
```

### Package for current host

```bash
gradle :desktopApp:packageDistributionForCurrentOS --stacktrace
```

Declared native formats include DMG, MSI, and DEB, but native packaging is host-dependent.

### Linux DEB

```bash
gradle :desktopApp:packageDeb --stacktrace
```

The tagged public release workflow currently exercises this Linux package path.

## Web commands

RPS Arena has both Kotlin/Wasm and Kotlin/JS browser targets.

### Run Wasm development target

```bash
gradle :webApp:wasmJsBrowserDevelopmentRun --stacktrace
```

Gradle starts a local development server and prints the URL/port.

### Run JavaScript development target

```bash
gradle :webApp:jsBrowserDevelopmentRun --stacktrace
```

Use this to test the JS path directly.

### Build Wasm-only production distribution

```bash
gradle :webApp:wasmJsBrowserDistribution --stacktrace
```

### Build preferred JS+Wasm compatibility distribution

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

Expected output:

```text
webApp/build/dist/composeWebCompatibility/productionExecutable/
```

This is the Web build used by CI and release packaging.

The tagged release workflow zips this directory into:

```text
rps-arena-web.zip
```

See `docs/web-platform.md` for deployment/runtime considerations.

## iPhone/iPad commands

These commands require macOS for Apple framework/Xcode validation.

### Debug iOS simulator framework

```bash
gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
```

### Release iOS simulator framework

```bash
gradle :shared:linkReleaseFrameworkIosSimulatorArm64 --stacktrace
```

### Release physical-device framework

```bash
gradle :shared:linkReleaseFrameworkIosArm64 --stacktrace
```

Generated output is under:

```text
shared/build/bin/iosSimulatorArm64/<buildType>Framework/
shared/build/bin/iosArm64/<buildType>Framework/
```

### Xcode simulator host build

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "RPS Arena" \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

This is suitable for public CI because it does not require signing credentials.

### Open the iOS project

```bash
open iosApp/iosApp.xcodeproj
```

Then select the `RPS Arena` scheme and an iPhone/iPad simulator or authorized device.

### Xcode direct KMP integration

The Xcode project's Kotlin build phase invokes:

```bash
gradle :shared:embedAndSignAppleFrameworkForXcode
```

Do not replace this with an arbitrary downloaded binary/script. If the project later adopts a Gradle Wrapper, update the Xcode build phase and docs together.

## Cross-platform CI parity

Portable/common commands:

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

macOS CI additionally runs the iOS simulator framework and Xcode host commands above.

## Rust commands

From repository root:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Package without publishing:

```bash
cargo package --manifest-path rust-engine/Cargo.toml
```

Inside the crate:

```bash
cd rust-engine
cargo test --all-targets
```

Inspect toolchain:

```bash
rustc --version
cargo --version
```

## Cleaning generated output

Gradle output:

```bash
gradle clean
```

This removes Gradle-owned project build output. It does not delete source code, Git history, SDK installations, Xcode, or signing credentials.

Web caches such as `node_modules/`/`kotlin-js-store/`, Xcode `DerivedData/`, and Rust `target/` are generated and ignored. Remove them only when diagnosing a relevant generated-state problem, not as a default reaction to every build failure.

## Git development commands

Clone:

```bash
git clone https://github.com/sanskarIN/rps-arena.git
cd rps-arena
```

Create branch:

```bash
git switch -c feature/example
```

Inspect:

```bash
git status
git diff
```

Owner repository identity:

```bash
git config user.name "Sanskar"
git config user.email "sanskarin@outlook.in"
```

Stage/commit:

```bash
git add <paths>
git commit -m "type(scope): focused message"
```

Push:

```bash
git push -u origin feature/example
```

## Release tag commands

Only after the exact `main` commit is fully validated:

```bash
git tag -a v2.5.8 -m "RPS Arena v2.5.8"
git push origin v2.5.8
```

The release workflow listens for tags matching `v*`.

Do not tag an unvalidated commit.

## Diagnostics

Useful Gradle verbosity options:

```bash
gradle <task> --stacktrace
gradle <task> --info
gradle <task> --debug
```

Start with `--stacktrace`. `--debug` can reveal environment details; sanitize logs before sharing.

`--scan` can send build metadata to an external Gradle service, so do not use it when a strictly local diagnostic workflow is required.

Xcode diagnostics:

```bash
xcodebuild -version
xcodebuild -list -project iosApp/iosApp.xcodeproj
```

Browser builds:

```bash
gradle :webApp:tasks --all
```

## Exit codes and CI

Normal convention:

```text
0 = success
non-zero = failure
```

Repository verification scripts stop on failing commands so later successful steps cannot hide an earlier failure.

## Command safety rules

- Read the actual error before deleting caches/SDKs.
- Never commit or paste signing passwords, private keys, provisioning credentials, tokens, API keys, or private backups into logs/issues.
- If a committed-secret check finds a real secret, remove it from candidate source and rotate/revoke the credential; do not simply weaken the detector.
- Do not add Android/Apple/Windows/macOS signing credentials to make public CI produce store artifacts.
- Do not remove a failing privacy/security/documentation/build gate merely to make CI green; determine whether source or checker is wrong.
- Do not infer that common-code tests prove every platform host builds; keep Android/Desktop/Web/iOS platform compilation gates active.
- Do not tag or merge a candidate until required checks are green on the exact current head.
