# Testing

## Shared Kotlin tests

```bash
gradle :shared:allTests
```

Coverage priorities:

- every classic rule direction;
- extended Lizard–Spock rules;
- draw behavior;
- seeded CPU determinism;
- classic-mode gesture constraints;
- persistence codec round trips.

## Android build smoke test

```bash
gradle :androidApp:assembleDebug
```

## Desktop build smoke test

```bash
gradle :desktopApp:packageDistributionForCurrentOS
```

## Rust engine

```bash
cargo test --manifest-path rust-engine/Cargo.toml
```

CI runs shared tests, Android assembly, desktop compilation, and Rust tests.
