#!/usr/bin/env python3
"""Verify that user-visible package versions stay synchronized."""

from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_FILES = {
    "android": ROOT / "androidApp" / "build.gradle.kts",
    "desktop": ROOT / "desktopApp" / "build.gradle.kts",
    "about": ROOT / "shared" / "src" / "commonMain" / "kotlin" / "in" / "sanskar" / "rpsarena" / "ui" / "App.kt",
}


def extract(pattern: str, text: str, source: str) -> str:
    match = re.search(pattern, text)
    if not match:
        raise ValueError(f"Could not find version in {source}")
    return match.group(1)


def main() -> int:
    try:
        android = extract(
            r'versionName\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"',
            EXPECTED_FILES["android"].read_text(encoding="utf-8"),
            "androidApp/build.gradle.kts",
        )
        desktop = extract(
            r'packageVersion\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"',
            EXPECTED_FILES["desktop"].read_text(encoding="utf-8"),
            "desktopApp/build.gradle.kts",
        )
        about = extract(
            r'Text\("Version:\s*([0-9]+\.[0-9]+\.[0-9]+)"\)',
            EXPECTED_FILES["about"].read_text(encoding="utf-8"),
            "shared/.../ui/App.kt",
        )
    except ValueError as error:
        print(error, file=sys.stderr)
        return 1

    versions = {android, desktop, about}
    if len(versions) != 1:
        print(
            f"Version mismatch: android={android}, desktop={desktop}, about={about}",
            file=sys.stderr,
        )
        return 1

    print(f"Version check passed: {android}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
