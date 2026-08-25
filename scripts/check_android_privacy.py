#!/usr/bin/env python3
"""Validate Android privacy invariants that must remain true for RPS Arena v1."""

from __future__ import annotations

from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "androidApp/src/main/AndroidManifest.xml"
LEGACY_RULES = ROOT / "androidApp/src/main/res/xml/backup_rules.xml"
EXTRACTION_RULES = ROOT / "androidApp/src/main/res/xml/data_extraction_rules.xml"
ANDROID_NS = "http://schemas.android.com/apk/res/android"
ANDROID = f"{{{ANDROID_NS}}}"


def fail(message: str) -> None:
    print(f"Android privacy contract failed: {message}", file=sys.stderr)
    raise SystemExit(1)


def parse(path: Path) -> ET.Element:
    if not path.is_file():
        fail(f"missing required file: {path.relative_to(ROOT)}")
    try:
        return ET.parse(path).getroot()
    except ET.ParseError as error:
        fail(f"invalid XML in {path.relative_to(ROOT)}: {error}")


def has_sharedpref_root_exclusion(parent: ET.Element | None) -> bool:
    if parent is None:
        return False
    return any(
        child.tag == "exclude"
        and child.attrib.get("domain") == "sharedpref"
        and child.attrib.get("path") == "."
        for child in parent
    )


def main() -> None:
    manifest = parse(MANIFEST)
    application = manifest.find("application")
    if application is None:
        fail("AndroidManifest.xml has no <application> element")

    if application.attrib.get(f"{ANDROID}allowBackup") != "false":
        fail('android:allowBackup must remain "false"')

    if application.attrib.get(f"{ANDROID}fullBackupContent") != "@xml/backup_rules":
        fail("android:fullBackupContent must point to @xml/backup_rules")

    if application.attrib.get(f"{ANDROID}dataExtractionRules") != "@xml/data_extraction_rules":
        fail("android:dataExtractionRules must point to @xml/data_extraction_rules")

    internet_permission = "android.permission.INTERNET"
    for permission in manifest.findall("uses-permission"):
        if permission.attrib.get(f"{ANDROID}name") == internet_permission:
            fail("android.permission.INTERNET is not allowed in the offline-first v1 manifest")

    legacy = parse(LEGACY_RULES)
    if legacy.tag != "full-backup-content":
        fail("backup_rules.xml must use <full-backup-content>")
    if not has_sharedpref_root_exclusion(legacy):
        fail("legacy backup rules must exclude all shared preferences")

    extraction = parse(EXTRACTION_RULES)
    if extraction.tag != "data-extraction-rules":
        fail("data_extraction_rules.xml must use <data-extraction-rules>")
    if not has_sharedpref_root_exclusion(extraction.find("cloud-backup")):
        fail("cloud-backup rules must exclude all shared preferences")
    if not has_sharedpref_root_exclusion(extraction.find("device-transfer")):
        fail("device-transfer rules must exclude all shared preferences")

    print("Android privacy contract passed")


if __name__ == "__main__":
    main()
