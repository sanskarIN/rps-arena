#!/usr/bin/env python3
"""Ensure every Git-tracked file is documented in the repository file reference."""

from __future__ import annotations

from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = ROOT / "docs" / "repository-file-reference.md"


def tracked_files() -> list[str]:
    result = subprocess.run(
        ["git", "ls-files", "-z"],
        cwd=ROOT,
        check=True,
        stdout=subprocess.PIPE,
    )
    return sorted(
        item.decode("utf-8")
        for item in result.stdout.split(b"\0")
        if item
    )


def main() -> int:
    if not REFERENCE.is_file():
        print(f"Missing documentation reference: {REFERENCE.relative_to(ROOT)}", file=sys.stderr)
        return 1

    reference = REFERENCE.read_text(encoding="utf-8")
    missing = [path for path in tracked_files() if f"`{path}`" not in reference]

    if missing:
        print("Repository file-reference coverage failed.", file=sys.stderr)
        print(
            "Add every missing tracked path to docs/repository-file-reference.md:",
            file=sys.stderr,
        )
        for path in missing:
            print(f"- {path}", file=sys.stderr)
        return 1

    print(f"Documentation reference covers {len(tracked_files())} tracked files.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
