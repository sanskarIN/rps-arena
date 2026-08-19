# Optional Rust Rules Engine

This crate mirrors the shared Kotlin rules in a tiny, deterministic Rust library. It is intentionally **not required** to run the Android or desktop apps. It exists for experimentation, benchmarking, and future native integration without making the core app harder to build.

```bash
cargo test --manifest-path rust-engine/Cargo.toml
```

Any future FFI integration should keep the Kotlin engine as the reference implementation until parity tests pass on every target.
