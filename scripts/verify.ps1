$ErrorActionPreference = "Stop"

python scripts/verify_localizations.py
gradle :shared:allTests
gradle :androidApp:assembleDebug
gradle :desktopApp:classes

if (Get-Command cargo -ErrorAction SilentlyContinue) {
    cargo test --manifest-path rust-engine/Cargo.toml
} else {
    Write-Host "cargo not installed; skipping optional Rust tests"
}
