#!/usr/bin/env bash
set -euo pipefail

gradle :shared:allTests
gradle :androidApp:assembleDebug
gradle :desktopApp:classes

if command -v cargo >/dev/null 2>&1; then
  cargo test --manifest-path rust-engine/Cargo.toml
else
  echo "cargo not installed; skipping optional Rust tests"
fi
