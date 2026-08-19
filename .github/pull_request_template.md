## What changed

Describe the focused change.

## Validation

- [ ] `gradle :shared:allTests`
- [ ] `gradle :androidApp:assembleDebug` when Android is affected
- [ ] `gradle :desktopApp:classes` when desktop is affected
- [ ] `cargo test --manifest-path rust-engine/Cargo.toml` when Rust is affected
- [ ] Documentation and `what_changed.md` updated when appropriate

## Safety / privacy

- [ ] No secrets, signing keys, tokens, or private credentials were committed.
- [ ] No new tracking, ads, or network permission was introduced without explicit design documentation.
