#!/usr/bin/env python3
"""Fail when a relative Markdown link outside code points to a missing repository path."""

from __future__ import annotations

import re
import sys
from pathlib import Path
from urllib.parse import unquote

ROOT = Path(__file__).resolve().parents[1]
LINK_RE = re.compile(r"!?\[[^\]]*\]\(([^)]+)\)")
INLINE_CODE_RE = re.compile(r"(`+)(.*?)\1")
FENCE_RE = re.compile(r"^\s*(`{3,}|~{3,})")
SKIPPED_PREFIXES = ("http://", "https://", "mailto:", "tel:", "data:", "#")


def normalize_target(raw: str) -> str:
    target = raw.strip()
    if target.startswith("<") and ">" in target:
        target = target[1 : target.index(">")]
    elif " \"" in target:
        target = target.split(" \"", 1)[0]
    elif " '" in target:
        target = target.split(" '", 1)[0]
    return unquote(target.split("#", 1)[0].strip())


def markdown_without_code(text: str) -> str:
    """Remove fenced blocks and inline code before Markdown-link scanning."""
    visible_lines: list[str] = []
    fence_char: str | None = None
    fence_length = 0

    for line in text.splitlines():
        fence = FENCE_RE.match(line)
        if fence:
            marker = fence.group(1)
            marker_char = marker[0]
            marker_length = len(marker)
            if fence_char is None:
                fence_char = marker_char
                fence_length = marker_length
                continue
            if marker_char == fence_char and marker_length >= fence_length:
                fence_char = None
                fence_length = 0
                continue

        if fence_char is not None:
            continue

        visible_lines.append(INLINE_CODE_RE.sub("", line))

    return "\n".join(visible_lines)


def markdown_files() -> list[Path]:
    ignored = {".git", ".gradle", "build", "target", ".idea"}
    return sorted(
        path
        for path in ROOT.rglob("*.md")
        if not any(part in ignored for part in path.relative_to(ROOT).parts)
    )


def main() -> int:
    errors: list[str] = []
    checked = 0

    for document in markdown_files():
        text = markdown_without_code(document.read_text(encoding="utf-8"))
        for raw_target in LINK_RE.findall(text):
            target = normalize_target(raw_target)
            if not target or target.startswith(SKIPPED_PREFIXES):
                continue

            resolved = (document.parent / target).resolve()
            try:
                resolved.relative_to(ROOT)
            except ValueError:
                errors.append(f"{document.relative_to(ROOT)} -> path escapes repository: {raw_target}")
                continue

            checked += 1
            if not resolved.exists():
                errors.append(f"{document.relative_to(ROOT)} -> missing: {target}")

    if errors:
        print("Documentation link check failed:")
        for error in errors:
            print(f"- {error}")
        return 1

    print(f"Documentation link check passed ({checked} relative links checked).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
