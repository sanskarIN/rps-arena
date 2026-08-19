#!/usr/bin/env python3
"""Verify that cross-platform package and shared UI versions stay synchronized."""

from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_FILES = {
    "android": ROOT / "androidApp" / "build.gradle.kts",
    "desktop": ROOT / "desktopApp" / "build.gradle.kts",
    "metadata": ROOT
    / "shared"
    / "src"
    / "commonMain"
    / "kotlin"
    / "in"
    / "sanskar"
    / "rpsarena"
    / "ui"
    / "AppMetadata.kt",
    "app": ROOT
    / "shared"
    / "src"
    / "commonMain"
    / "kotlin"
    / "in"
    / "sanskar"
    / "rpsarena"
    / "ui"
    / "App.kt",
}


def extract(pattern: str, text: str, source: str) -> str:
    match = re.search(pattern, text)
    if not match:
        raise ValueError(f"Could not find version in {source}")
    return match.group(1)


def semantic_version_code(version: str) -> int:
    major, minor, patch = (int(part) for part in version.split("."))
    if minor > 99 or patch > 99:
        raise ValueError(
            "Android semantic versionCode mapping requires minor and patch values <= 99"
        )
    return major * 10_000 + minor * 100 + patch


def main() -> int:
    try:
        android_text = EXPECTED_FILES["android"].read_text(encoding="utf-8")
        android = extract(
            r'versionName\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"',
            android_text,
            "androidApp/build.gradle.kts",
        )
        android_code = int(
            extract(
                r"versionCode\s*=\s*([0-9]+)",
                android_text,
                "androidApp/build.gradle.kts versionCode",
            )
        )
        desktop = extract(
            r'packageVersion\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"',
            EXPECTED_FILES["desktop"].read_text(encoding="utf-8"),
            "desktopApp/build.gradle.kts",
        )
        shared = extract(
            r'APP_VERSION\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"',
            EXPECTED_FILES["metadata"].read_text(encoding="utf-8"),
            "shared/.../ui/AppMetadata.kt",
        )
        app_text = EXPECTED_FILES["app"].read_text(encoding="utf-8")
        if 'Text("${strings.version}: $APP_VERSION")' not in app_text:
            raise ValueError("About UI is not rendering the shared APP_VERSION constant")

        expected_android_code = semantic_version_code(android)
        if android_code != expected_android_code:
            raise ValueError(
                "Android versionCode mismatch: "
                f"versionName={android} requires versionCode={expected_android_code}, "
                f"found {android_code}"
            )
    except (ValueError, OSError) as error:
        print(error, file=sys.stderr)
        return 1

    versions = {android, desktop, shared}
    if len(versions) != 1:
        print(
            f"Version mismatch: android={android}, desktop={desktop}, shared={shared}",
            file=sys.stderr,
        )
        return 1

    print(f"Version check passed: {android} (Android versionCode {android_code})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
