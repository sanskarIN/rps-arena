# ADR 0003: Keep the Rust rules engine optional

- Status: Accepted
- Date: 2026-08-19

## Context

The repository includes a small Rust mirror of the game rules for experimentation, independent verification, and benchmarking. The Android/Desktop application already has a tested Kotlin rules engine and does not require native FFI for performance.

## Decision

Keep `rust-engine/` standalone and optional. Do not place it on the production application path unless a future change demonstrates a concrete reliability, educational, or performance benefit that justifies FFI/build complexity.

Rust CI still enforces formatting, Clippy with warnings denied, and tests so the optional component remains high quality.

## Consequences

- Kotlin/Compose builds stay simple and portable.
- The Rust mirror can independently validate rule semantics and host experiments.
- No native library packaging, ABI matrix, unsafe FFI boundary, or additional application crash surface is introduced in v1.
- Benchmark results from the Rust mirror cannot be presented as Android/Desktop app performance because the app does not execute that engine.

## Alternatives considered

### Rust as the production rules engine

Deferred. The domain is tiny and Kotlin performance is not a measured bottleneck, so FFI would currently add more complexity than value.

### Remove Rust entirely

Rejected because the mirror provides useful independent testing/learning value at low maintenance cost while it remains isolated and verified.
