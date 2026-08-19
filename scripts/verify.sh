#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

GRADLE_BIN="${GRADLE_BIN:-gradle}"

echo "== Kotlin / platform verification =="
"$GRADLE_BIN" --no-daemon :shared:compileKotlinDesktop --stacktrace
"$GRADLE_BIN" --no-daemon :shared:allTests --stacktrace
"$GRADLE_BIN" --no-daemon :androidApp:assembleDebug --stacktrace
"$GRADLE_BIN" --no-daemon :androidApp:lintDebug --stacktrace
"$GRADLE_BIN" --no-daemon :desktopApp:classes --stacktrace

if command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN="python3"
elif command -v python >/dev/null 2>&1; then
  PYTHON_BIN="python"
else
  echo "Python 3 is required for documentation, privacy, and committed-secret checks." >&2
  exit 1
fi

echo "== Documentation verification =="
"$PYTHON_BIN" scripts/check_docs_links.py

echo "== Android privacy verification =="
"$PYTHON_BIN" scripts/check_android_privacy.py

echo "== Committed-secret verification =="
"$PYTHON_BIN" scripts/check_for_secrets.py

if command -v cargo >/dev/null 2>&1; then
  echo "== Optional Rust verification =="
  (
    cd rust-engine
    cargo fmt --all -- --check
    cargo clippy --all-targets --all-features -- -D warnings
    cargo test --all-targets --all-features
  )
else
  echo "cargo not installed; skipping optional Rust verification"
fi

echo "RPS Arena local verification completed successfully."
