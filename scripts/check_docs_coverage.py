#!/usr/bin/env python3
"""Ensure every Git-tracked file is documented by an approved file reference."""

from __future__ import annotations

from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
REFERENCES = (
    ROOT / "docs" / "repository-file-reference.md",
    ROOT / "docs" / "reconciliation-file-reference.md",
)


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
    missing_references = [path for path in REFERENCES if not path.is_file()]
    if missing_references:
        for path in missing_references:
            print(
                f"Missing documentation reference: {path.relative_to(ROOT)}",
                file=sys.stderr,
            )
        return 1

    reference_text = "\n".join(path.read_text(encoding="utf-8") for path in REFERENCES)
    tracked = tracked_files()
    missing = [path for path in tracked if f"`{path}`" not in reference_text]

    if missing:
        print("Repository file-reference coverage failed.", file=sys.stderr)
        print(
            "Add every missing tracked path to an approved file reference:",
            file=sys.stderr,
        )
        for path in missing:
            print(f"- {path}", file=sys.stderr)
        return 1

    print(
        f"Documentation references cover {len(tracked)} tracked files "
        f"across {len(REFERENCES)} reference documents."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
