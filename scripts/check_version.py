#!/usr/bin/env python3
"""Verify cross-platform package, native metadata, and shared UI versions."""

from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_FILES = {
    "android": ROOT / "androidApp" / "build.gradle.kts",
    "desktop": ROOT / "desktopApp" / "build.gradle.kts",
    "ios_plist": ROOT / "iosApp" / "iosApp" / "Info.plist",
    "ios_project": ROOT / "iosApp" / "iosApp.xcodeproj" / "project.pbxproj",
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


def extract_all(pattern: str, text: str, source: str) -> set[str]:
    matches = set(re.findall(pattern, text))
    if not matches:
        raise ValueError(f"Could not find version in {source}")
    return matches


def semantic_build_code(version: str) -> int:
    major, minor, patch = (int(part) for part in version.split("."))
    if minor > 99 or patch > 99:
        raise ValueError(
            "Semantic build-code mapping requires minor and patch values <= 99"
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

        ios_plist_text = EXPECTED_FILES["ios_plist"].read_text(encoding="utf-8")
        ios = extract(
            r"<key>CFBundleShortVersionString</key>\s*<string>([0-9]+\.[0-9]+\.[0-9]+)</string>",
            ios_plist_text,
            "iosApp/iosApp/Info.plist",
        )
        ios_code = int(
            extract(
                r"<key>CFBundleVersion</key>\s*<string>([0-9]+)</string>",
                ios_plist_text,
                "iosApp/iosApp/Info.plist CFBundleVersion",
            )
        )

        ios_project_text = EXPECTED_FILES["ios_project"].read_text(encoding="utf-8")
        ios_marketing_versions = extract_all(
            r"MARKETING_VERSION\s*=\s*([0-9]+\.[0-9]+\.[0-9]+);",
            ios_project_text,
            "iosApp/iosApp.xcodeproj/project.pbxproj MARKETING_VERSION",
        )
        ios_project_codes = {
            int(value)
            for value in extract_all(
                r"CURRENT_PROJECT_VERSION\s*=\s*([0-9]+);",
                ios_project_text,
                "iosApp/iosApp.xcodeproj/project.pbxproj CURRENT_PROJECT_VERSION",
            )
        }

        shared = extract(
            r'APP_VERSION\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"',
            EXPECTED_FILES["metadata"].read_text(encoding="utf-8"),
            "shared/.../ui/AppMetadata.kt",
        )
        app_text = EXPECTED_FILES["app"].read_text(encoding="utf-8")
        if 'Text("${strings.version}: $APP_VERSION")' not in app_text:
            raise ValueError("About UI is not rendering the shared APP_VERSION constant")

        expected_code = semantic_build_code(android)
        if android_code != expected_code:
            raise ValueError(
                "Android versionCode mismatch: "
                f"versionName={android} requires versionCode={expected_code}, "
                f"found {android_code}"
            )
        if ios_code != expected_code:
            raise ValueError(
                "iOS CFBundleVersion mismatch: "
                f"version={ios} requires build={expected_code}, found {ios_code}"
            )
        if ios_marketing_versions != {ios}:
            raise ValueError(
                "iOS Xcode MARKETING_VERSION mismatch: "
                f"plist={ios}, project={sorted(ios_marketing_versions)}"
            )
        if ios_project_codes != {ios_code}:
            raise ValueError(
                "iOS Xcode CURRENT_PROJECT_VERSION mismatch: "
                f"plist={ios_code}, project={sorted(ios_project_codes)}"
            )
    except (ValueError, OSError) as error:
        print(error, file=sys.stderr)
        return 1

    versions = {android, desktop, ios, shared}
    if len(versions) != 1:
        print(
            "Version mismatch: "
            f"android={android}, desktop={desktop}, ios={ios}, shared={shared}",
            file=sys.stderr,
        )
        return 1

    print(
        f"Version check passed: {android} "
        f"(Android versionCode {android_code}, iOS build {ios_code})"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
