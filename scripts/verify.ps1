$ErrorActionPreference = "Stop"

python scripts/check_format.py
python scripts/check_docs_links.py
python scripts/check_docs_coverage.py
python scripts/check_for_secrets.py
python scripts/check_android_privacy.py
python scripts/check_version.py
python scripts/verify_localizations.py

gradle `
    :shared:allTests `
    :shared:desktopTest `
    :shared:assembleAndroidDeviceTest `
    :androidApp:lintDebug `
    :androidApp:assembleDebug `
    :desktopApp:classes `
    :webApp:composeCompatibilityBrowserDistribution `
    --stacktrace

if (Get-Command cargo -ErrorAction SilentlyContinue) {
    cargo test --manifest-path rust-engine/Cargo.toml --all-targets
} else {
    Write-Host "cargo not installed; skipping optional Rust tests"
}
