#!/usr/bin/env python3
"""Validate Compose Multiplatform string catalogs.

The default catalog is authoritative. Every locale must expose the same keys and
preserve printf-style placeholders so stringResource formatting cannot fail at
runtime.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path
from xml.etree import ElementTree

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "shared" / "src" / "commonMain" / "composeResources"
DEFAULT_FILE = RESOURCES / "values" / "strings.xml"
PLACEHOLDER = re.compile(r"%(?:\d+\$)?[a-zA-Z]")


def read_catalog(path: Path) -> dict[str, str]:
    try:
        root = ElementTree.parse(path).getroot()
    except (OSError, ElementTree.ParseError) as error:
        raise RuntimeError(f"cannot parse {path.relative_to(ROOT)}: {error}") from error

    catalog: dict[str, str] = {}
    for node in root.findall("string"):
        name = node.attrib.get("name", "").strip()
        if not name:
            raise RuntimeError(f"unnamed <string> in {path.relative_to(ROOT)}")
        if name in catalog:
            raise RuntimeError(f"duplicate string key {name!r} in {path.relative_to(ROOT)}")
        catalog[name] = "".join(node.itertext())
    return catalog


def placeholders(value: str) -> list[str]:
    return PLACEHOLDER.findall(value)


def validate_locale(default: dict[str, str], locale_file: Path) -> list[str]:
    locale = read_catalog(locale_file)
    errors: list[str] = []

    missing = sorted(default.keys() - locale.keys())
    extra = sorted(locale.keys() - default.keys())
    if missing:
        errors.append(f"{locale_file.parent.name}: missing keys: {', '.join(missing)}")
    if extra:
        errors.append(f"{locale_file.parent.name}: unexpected keys: {', '.join(extra)}")

    for key in sorted(default.keys() & locale.keys()):
        expected = placeholders(default[key])
        actual = placeholders(locale[key])
        if expected != actual:
            errors.append(
                f"{locale_file.parent.name}: placeholder mismatch for {key}: "
                f"expected {expected}, found {actual}"
            )
    return errors


def main() -> int:
    default = read_catalog(DEFAULT_FILE)
    locale_files = sorted(
        path / "strings.xml"
        for path in RESOURCES.glob("values-*")
        if path.is_dir() and (path / "strings.xml").exists()
    )

    if not locale_files:
        print("No localized string catalogs found.", file=sys.stderr)
        return 1

    errors: list[str] = []
    for locale_file in locale_files:
        errors.extend(validate_locale(default, locale_file))

    if errors:
        print("Localization validation failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(
        f"Localization catalogs OK: {len(default)} keys across "
        f"{1 + len(locale_files)} catalogs."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
