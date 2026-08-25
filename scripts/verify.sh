#!/usr/bin/env bash
set -euo pipefail

python3 scripts/check_format.py
python3 scripts/check_docs_links.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_for_secrets.py
python3 scripts/check_android_privacy.py
python3 scripts/check_version.py
python3 scripts/verify_localizations.py

gradle \
  :shared:allTests \
  :shared:desktopTest \
  :shared:assembleAndroidDeviceTest \
  :androidApp:lintDebug \
  :androidApp:assembleDebug \
  :desktopApp:classes \
  :webApp:composeCompatibilityBrowserDistribution \
  --stacktrace

if [[ "$(uname -s)" == "Darwin" ]]; then
  gradle :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
fi

if command -v cargo >/dev/null 2>&1; then
  cargo test --manifest-path rust-engine/Cargo.toml --all-targets
else
  echo "cargo not installed; skipping optional Rust tests"
fi
