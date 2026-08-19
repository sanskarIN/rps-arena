#!/usr/bin/env python3
"""Fail CI on basic repository text-formatting regressions."""

from __future__ import annotations

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SKIP_DIRS = {".git", ".gradle", ".idea", "build", "target", "node_modules"}
TEXT_SUFFIXES = {
    ".kt",
    ".kts",
    ".md",
    ".py",
    ".rs",
    ".sh",
    ".ps1",
    ".toml",
    ".yml",
    ".yaml",
    ".xml",
    ".properties",
    ".txt",
    ".svg",
}
TEXT_NAMES = {"LICENSE", ".editorconfig", ".gitattributes", ".gitignore", ".mailmap"}


def candidate(path: Path) -> bool:
    if any(part in SKIP_DIRS for part in path.parts):
        return False
    return path.is_file() and (path.suffix.lower() in TEXT_SUFFIXES or path.name in TEXT_NAMES)


def has_disallowed_trailing_whitespace(path: Path, line: str) -> bool:
    stripped = line.rstrip(" \t")
    if stripped == line:
        return False
    trailing = line[len(stripped):]
    # Two spaces are standard Markdown hard-break syntax and are intentional.
    return not (path.suffix.lower() == ".md" and trailing == "  ")


def main() -> int:
    failures: list[str] = []
    checked = 0

    for path in sorted(ROOT.rglob("*")):
        if not candidate(path):
            continue
        checked += 1
        relative = path.relative_to(ROOT)
        data = path.read_bytes()
        if not data:
            continue
        if b"\x00" in data:
            continue
        try:
            text = data.decode("utf-8")
        except UnicodeDecodeError:
            failures.append(f"{relative}: not valid UTF-8")
            continue

        if not text.endswith("\n"):
            failures.append(f"{relative}: missing final newline")

        for number, line in enumerate(text.splitlines(), start=1):
            if has_disallowed_trailing_whitespace(path, line):
                failures.append(f"{relative}:{number}: trailing whitespace")

    if failures:
        print("Formatting check failed:", file=sys.stderr)
        for failure in failures:
            print(f"- {failure}", file=sys.stderr)
        return 1

    print(f"Formatting check passed for {checked} text files.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
