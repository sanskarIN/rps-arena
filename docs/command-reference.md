# Command Reference

This guide explains the commands used by RPS Arena, what each command means, what it changes, and when to use it. Run commands from the repository root unless a section says otherwise.

## Important: this repository has no Gradle Wrapper

RPS Arena currently does **not** track `gradlew`, `gradlew.bat`, or `gradle/wrapper/*`. Commands therefore use the `gradle` executable installed on your machine or installed by GitHub Actions.

Check it with:

```bash
gradle --version
```

Meaning:

- `gradle` starts the installed Gradle command-line program.
- `--version` prints the Gradle version, JVM, operating system, and environment information without building the project.
- The validated CI version is Gradle 9.5.1.

If `gradle` is not found, install/configure Gradle before using the build commands. See `docs/toolchain.md`.

## Repository verification

### Full Unix-like verification

```bash
bash scripts/verify.sh
```

Meaning:

- `bash` runs the Bash interpreter.
- `scripts/verify.sh` is the repository verification script.
- `set -euo pipefail` inside the script causes failures, undefined variables, and failed pipeline commands to stop the verification instead of silently continuing.
- The script runs formatting, version consistency, shared tests, Android lint/build, desktop compilation, and Rust tests when Cargo exists.

### Full PowerShell verification

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify.ps1
```

Meaning:

- `powershell` starts Windows PowerShell.
- `-ExecutionPolicy Bypass` applies only to this process and allows the repository script to run when the machine policy otherwise blocks local scripts. It does not permanently change the system execution policy.
- `-File scripts/verify.ps1` tells PowerShell which script to execute.

If you are already inside PowerShell and local scripts are allowed:

```powershell
./scripts/verify.ps1
```

## Formatting verification

```bash
python3 scripts/check_format.py
```

Windows environments where Python is exposed as `python` can use:

```powershell
python scripts/check_format.py
```

Meaning:

- `python3`/`python` starts Python.
- `scripts/check_format.py` recursively inspects repository text files.
- It checks UTF-8 decoding, final newlines, and accidental trailing whitespace.
- Standard Markdown two-space hard breaks are intentionally allowed.
- It skips generated/cache directories such as `.git`, `.gradle`, `.idea`, `build`, `target`, and `node_modules`.
- Exit code `0` means success; a non-zero exit code makes CI fail.

This command does **not** rewrite files. It is a validator, not an auto-formatter.

## Version consistency verification

```bash
python3 scripts/check_version.py
```

Meaning:

- Reads Android `versionName` from `androidApp/build.gradle.kts`.
- Reads desktop `packageVersion` from `desktopApp/build.gradle.kts`.
- Reads shared `APP_VERSION` from `AppMetadata.kt`.
- Verifies that the About UI renders the shared version constant.
- Fails if those versions differ or if a required declaration cannot be found.

Use this after changing a release version.

## Gradle task syntax

A command such as:

```bash
gradle :shared:allTests --stacktrace
```

has three important parts:

- `gradle` — execute Gradle.
- `:shared:allTests` — run task `allTests` in module `shared`. The leading colon starts from the root project; the second colon separates project path from task name.
- `--stacktrace` — print a Java/Kotlin stack trace when a task fails. This is useful for diagnosing the real failure rather than only seeing a short error summary.

You can inspect tasks with:

```bash
gradle tasks
```

For one module:

```bash
gradle :shared:tasks
```

## Shared Kotlin tests

```bash
gradle :shared:allTests --stacktrace
```

Meaning:

- Compiles the shared Kotlin Multiplatform test source sets.
- Runs test tasks available for configured targets.
- Includes the shared business-rule/persistence tests and the configured desktop test target.
- Does not install an Android APK on a device.

For desktop UI tests specifically:

```bash
gradle :shared:desktopTest --stacktrace
```

This runs the Compose desktop UI smoke tests from `shared/src/desktopTest`.

## Android lint

```bash
gradle :androidApp:lintDebug --stacktrace
```

Meaning:

- Runs Android Lint against the debug variant.
- Checks Android resources, manifest/configuration, API usage, and other Android-specific correctness rules.
- Does not create a store-ready signed APK.

Release lint:

```bash
gradle :androidApp:lintRelease --stacktrace
```

This applies lint to the release variant and is used by release automation.

## Android APK builds

Debug APK:

```bash
gradle :androidApp:assembleDebug --stacktrace
```

Meaning:

- `assembleDebug` compiles and packages the debug variant.
- It resolves the `:shared` Android target automatically because `androidApp` depends on it.
- The result is written under `androidApp/build/outputs/apk/debug/`.
- Debug builds are for development/testing and are not equivalent to a signed Play Store release.

Release APK:

```bash
gradle :androidApp:assembleRelease --stacktrace
```

Meaning:

- Compiles/packages the release variant.
- The public repository does not contain private signing credentials.
- CI can therefore validate an unsigned/public release artifact, while store signing must happen in an authorized secret-bearing environment.

## Desktop development

Compile desktop classes:

```bash
gradle :desktopApp:classes --stacktrace
```

Meaning:

- Compiles desktop JVM code and the shared desktop target.
- Does not start the app.
- This is a fast CI build gate for desktop source compatibility.

Run the app:

```bash
gradle :desktopApp:run
```

Meaning:

- Compiles what is needed and launches the Compose Desktop application.
- The process stays attached to the terminal until the desktop app exits.

Package for the current OS:

```bash
gradle :desktopApp:packageDistributionForCurrentOS --stacktrace
```

Meaning:

- Uses Compose Desktop native-distribution tooling.
- Chooses the package format supported by the current host operating system.
- Native packages are generated in a build directory and are ignored by Git.

Linux Debian package:

```bash
gradle :desktopApp:packageDeb --stacktrace
```

Meaning:

- Builds the `.deb` format declared by `TargetFormat.Deb`.
- GitHub release automation runs this on Ubuntu.

## Cleaning generated Gradle output

```bash
gradle clean
```

Meaning:

- Deletes Gradle `build/` outputs owned by the project/modules.
- It does not delete source code, Git history, Android SDKs, or your global Gradle installation.
- It is useful when stale generated outputs cause confusing behavior, but it should not be the first response to every build error.

Refresh dependencies only when necessary:

```bash
gradle :shared:allTests --refresh-dependencies --stacktrace
```

`--refresh-dependencies` asks Gradle to re-check dependency metadata/artifacts instead of relying only on normal cache behavior. It can be slower and requires network access for uncached dependencies.

## Rust commands

Run from the repository root:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

Meaning:

- `cargo` is Rust's package/build tool.
- `test` compiles and runs tests.
- `--manifest-path rust-engine/Cargo.toml` points Cargo to the optional Rust engine without changing directories.
- `--all-targets` tests all crate target types configured by the package.

Equivalent from inside the Rust directory:

```bash
cd rust-engine
cargo test --all-targets
```

`cd rust-engine` changes the shell's current working directory.

Package the crate:

```bash
cargo package --manifest-path rust-engine/Cargo.toml
```

Meaning:

- Creates a distributable `.crate` archive after Cargo's package checks.
- Does not publish it to crates.io.
- Release CI uploads the package as a GitHub artifact.

Check Rust toolchain:

```bash
rustc --version
cargo --version
```

## Git commands used for development

Clone:

```bash
git clone https://github.com/sanskarIN/rps-arena.git
```

- `git clone` downloads repository history and creates a working directory.

Enter the project:

```bash
cd rps-arena
```

Create a branch:

```bash
git switch -c docs/example-change
```

- `switch` changes branches.
- `-c` creates a new branch before switching to it.

Inspect changes:

```bash
git status
git diff
```

- `git status` shows changed/untracked/staged files and current branch.
- `git diff` shows unstaged textual differences.

Stage and commit:

```bash
git add docs/example.md
git commit -m "docs: explain example"
```

- `git add` puts the selected content into Git's staging area.
- `git commit` creates a local immutable commit from staged changes.
- `-m` supplies the commit message directly.

Owner identity used by the project:

```bash
git config user.name "Sanskar"
git config user.email "sanskarin@outlook.in"
```

Without `--global`, these settings apply to the current repository only.

Push a new branch:

```bash
git push -u origin docs/example-change
```

- `origin` is the conventional name for the cloned remote.
- `-u` stores upstream tracking so later `git push`/`git pull` can omit branch names.

## GitHub release tag commands

Create an annotated tag after the exact `main` commit is validated:

```bash
git tag -a v1.1.0 -m "RPS Arena v1.1.0"
```

- `tag -a` creates an annotated tag object.
- `v1.1.0` follows the repository's semantic-version release convention.
- `-m` supplies the tag message.

Push only that tag:

```bash
git push origin v1.1.0
```

The release workflow listens for tags matching `v*` and can build/publish release artifacts.

Do not tag an unvalidated commit.

## Diagnostic options

Useful Gradle flags:

```bash
gradle :androidApp:assembleDebug --info
gradle :androidApp:assembleDebug --debug
gradle :androidApp:assembleDebug --scan
```

- `--info` increases logging moderately.
- `--debug` is much more verbose and may expose environment details; sanitize logs before sharing.
- `--scan` requests a Gradle Build Scan and can involve an external service. Do not use it when you need a strictly local diagnostic workflow or when sharing build metadata is not appropriate.

Prefer `--stacktrace` first because it is local and usually sufficient.

## Command safety rules

- Read an error before deleting caches or SDKs.
- Never paste signing passwords, tokens, API keys, private certificates, or personal backup data into command history, issue reports, or CI logs.
- Do not run release/tag commands unless the intended commit is validated.
- Prefer repository-scoped Git identity/configuration when you do not intend to change every local repository.
- Generated `build/`, `.gradle/`, and `rust-engine/target/` content can be recreated; source files and signing credentials cannot.
