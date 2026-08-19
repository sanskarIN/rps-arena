#!/usr/bin/env bash
set -euo pipefail

python3 scripts/check_format.py
python3 scripts/check_docs_coverage.py
python3 scripts/check_version.py
gradle :shared:allTests --stacktrace
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
gradle :desktopApp:classes --stacktrace

if command -v cargo >/dev/null 2>&1; then
  cargo test --manifest-path rust-engine/Cargo.toml --all-targets
else
  echo "cargo not installed; skipping optional Rust tests"
fi
