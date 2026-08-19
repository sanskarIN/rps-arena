# Optional Rust Rules Engine Reference

The `rust-engine/` directory is an independent, optional Rust library that mirrors the Rock Paper Scissors rule table for experimentation, testing, and future interoperability research. The Android/Desktop Kotlin application does not load or call this crate at runtime.

## File inventory

```text
rust-engine/Cargo.toml
rust-engine/README.md
rust-engine/src/lib.rs
```

Generated Cargo output under `rust-engine/target/` is ignored and not source code.

`Cargo.lock` is also ignored in the current repository policy.

## Architectural role

Current runtime architecture:

```text
Android/Desktop apps -> Kotlin :shared RulesEngine
```

Not:

```text
Android/Desktop apps -> Rust FFI/native library
```

The Rust crate is therefore **not** required to:

- build the normal Kotlin shared module;
- build Android APK;
- run desktop application;
- play CPU/local matches.

CI tests it independently so the optional mirror does not silently rot.

## `Cargo.toml`

Current package metadata:

```toml
[package]
name = "rps-arena-engine"
version = "0.1.0"
edition = "2024"
license = "MIT"
description = "Optional deterministic rules engine for RPS Arena"
repository = "https://github.com/sanskarIN/rps-arena"

[lib]
crate-type = ["rlib"]
```

### Package name

`rps-arena-engine` is the Cargo crate/package name.

It is independent of the Kotlin app version. The crate is currently `0.1.0` while the app release candidate is `1.1.0`.

Do not assume app and optional crate versions must match unless a future release policy explicitly couples them.

### Rust edition 2024

The crate declares the Rust 2024 edition. Edition controls language/compiler behavior conventions, not the operating system target.

CI installs the stable Rust toolchain, which must support the declared edition.

### License

The crate declares MIT, aligned with repository license.

### `crate-type = ["rlib"]`

Builds a standard Rust library artifact for Rust linking.

It does not configure:

- `cdylib` for C-compatible dynamic library;
- `staticlib` for static C ABI;
- JNI bindings;
- Kotlin/Native bindings;
- WebAssembly output.

Therefore no FFI integration should be inferred.

## `src/lib.rs`

### `Gesture`

Rust enum mirrors the five shared gestures:

```rust
pub enum Gesture {
    Rock,
    Paper,
    Scissors,
    Lizard,
    Spock,
}
```

It derives:

- `Clone`;
- `Copy`;
- `Debug`;
- `Eq`;
- `PartialEq`.

These are useful for value-style enum behavior and tests.

### `Outcome`

Rust outcome enum:

```rust
pub enum Outcome {
    PlayerOne,
    PlayerTwo,
    Draw,
}
```

It is intentionally simpler than Kotlin's full `RoundRecord`/timeout model. The Rust crate resolves two supplied gestures only; it does not model timers, scores, history, settings, or match state.

## Rust `resolve()` algorithm

Signature:

```rust
pub fn resolve(a: Gesture, b: Gesture) -> Outcome
```

Flow:

1. if `a == b`, return `Draw`;
2. check whether `(a, b)` is one of the Player One winning pairs;
3. if yes, return `PlayerOne`;
4. otherwise return `PlayerTwo`.

Winning pairs:

```text
Rock -> Scissors
Rock -> Lizard
Paper -> Rock
Paper -> Spock
Scissors -> Paper
Scissors -> Lizard
Lizard -> Spock
Lizard -> Paper
Spock -> Scissors
Spock -> Rock
```

This matches the Kotlin `RulesEngine` defeat matrix.

## Rust tests

`#[cfg(test)] mod tests` currently includes:

### `classic_rules`

Asserts Player One wins for:

- Rock over Scissors;
- Paper over Rock;
- Scissors over Paper.

### `extended_rules`

Asserts:

- Lizard over Spock;
- Spock over Rock.

The Kotlin suite has broader rule coverage and remains the primary application test suite.

## Running Rust tests

From repository root:

```bash
cargo test --manifest-path rust-engine/Cargo.toml --all-targets
```

From inside the crate:

```bash
cd rust-engine
cargo test --all-targets
```

See `docs/command-reference.md` for flag meanings.

## CI behavior

The `rust` job in `.github/workflows/ci.yml`:

1. checks out repository;
2. installs stable Rust via `dtolnay/rust-toolchain@stable`;
3. sets working directory to `rust-engine`;
4. runs `cargo test --all-targets`.

The Rust job is separate from Kotlin/Android/Desktop job. A Rust failure still makes repository CI fail, but it does not mean the Android runtime directly called Rust.

## Release workflow

Release Rust job:

```bash
cargo test --all-targets
cargo package
```

Then uploads:

```text
rust-engine/target/package/*.crate
```

as artifact `rps-arena-rust-engine`.

`cargo package` creates an archive; it does not publish to crates.io.

GitHub tag release later includes the downloaded artifact/checksum with other release outputs.

## Why `Cargo.lock` is ignored

Current `.gitignore` excludes `Cargo.lock`.

For a reusable Rust library, omitting the lockfile can allow downstream/current resolution of compatible dependency versions. At present this crate has no external dependencies, so the practical impact is minimal.

If the crate later becomes an application/tool with reproducibility requirements or adds dependencies, revisit lockfile policy deliberately rather than following a blanket rule.

## Rule parity policy

When Kotlin rules change and Rust mirror is meant to remain equivalent:

1. update Kotlin `Gesture`/`RulesEngine` first or in same change;
2. update Rust enum/match pairs;
3. add tests for all new/changed relationships;
4. run Kotlin shared tests;
5. run Cargo tests;
6. update domain/Rust docs;
7. note behavioral change in changelog.

A parity test generated from a shared data file could be considered in a future refactor, but do not add build complexity unless it improves maintainability measurably.

## What does not need Rust parity

Rust mirror currently has no equivalents for:

- CPU strategy;
- seed/randomness;
- timers;
- timeout outcomes;
- match modes;
- scores/streaks;
- settings;
- history;
- backup/import;
- localization;
- private rooms;
- Compose UI.

Do not add these merely to make the crate larger. Keep its scope explicit.

## Adding dependencies

Before adding a Cargo dependency:

- verify it is actually needed;
- review maintenance/license/security posture;
- understand default features;
- disable unnecessary features when appropriate;
- review whether `Cargo.lock` policy should change;
- run tests and security/dependency tooling available to the project;
- update documentation and Dependabot expectations.

Dependabot already scans the Cargo ecosystem weekly under `/rust-engine`.

## Future FFI/native integration

If the project ever decides to call Rust from Android/Desktop, that would be a major architecture change.

It would require decisions around:

- crate output type (`cdylib`/`staticlib` etc.);
- C ABI boundary;
- JNI or another JVM binding layer;
- Android ABI builds (`arm64-v8a`, etc.);
- desktop native library packaging per OS/architecture;
- memory ownership/error handling across FFI;
- deterministic parity and tests;
- build-tool integration;
- binary size;
- security/update policy.

Do not expose Rust enums directly across an unstable ABI without a carefully defined representation.

The current application gains no practical benefit from FFI because the Kotlin rule engine is already small, deterministic, tested, and cross-platform.

## Future WebAssembly

WASM is also not configured. If used for a future web experiment, treat that as a separate target/package decision rather than assuming `rlib` output can be dropped into a browser.

## Formatting/style

Rust source should be formatted using standard Rust tooling:

```bash
cargo fmt --manifest-path rust-engine/Cargo.toml -- --check
```

Current CI does not yet enforce `cargo fmt`; if adding it, update CI/verification scripts/docs in the same change.

Static linting can be considered with:

```bash
cargo clippy --manifest-path rust-engine/Cargo.toml --all-targets -- -D warnings
```

Do not add `-D warnings` to CI without first ensuring the pinned/stable toolchain produces a clean result and deciding how compiler-version lint changes will be handled.

## Crate version update

If publishing/releasing a new meaningful Rust crate version:

1. update `Cargo.toml` package version;
2. update crate README if API/usage changed;
3. add/adjust tests;
4. run `cargo test --all-targets`;
5. run `cargo package`;
6. inspect packaged file list/metadata;
7. update changelog/release notes if distributed;
8. do not publish to crates.io unless the project explicitly decides to support that distribution channel.

## Security boundary

The crate currently processes enum values supplied by Rust callers and has no network/file parser or unsafe code.

If future code introduces:

- `unsafe` blocks;
- FFI;
- untrusted input parsing;
- network data;
- file formats;
- cryptography;

add focused security review/tests and update `SECURITY.md`.

## File-by-file summary

### `rust-engine/Cargo.toml`

Crate identity, edition, license, description, repository link, library artifact type.

### `rust-engine/README.md`

Short crate-specific explanation/commands. Keep it aligned with this deeper repository guide without duplicating every project concern.

### `rust-engine/src/lib.rs`

Five-gesture rule mirror plus its unit tests.

That is the complete tracked Rust engine at this stage.
