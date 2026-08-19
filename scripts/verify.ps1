$ErrorActionPreference = "Stop"

python scripts/check_format.py
python scripts/check_docs_links.py
python scripts/check_docs_coverage.py
python scripts/check_for_secrets.py
python scripts/check_android_privacy.py
python scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace

if (Get-Command cargo -ErrorAction SilentlyContinue) {
    cargo test --manifest-path rust-engine/Cargo.toml --all-targets
} else {
    Write-Host "cargo not installed; skipping optional Rust tests"
}
