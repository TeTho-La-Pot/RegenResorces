"""
Decompiled Forge mod sources often contain SRG names (m_60734_). Remap to MCP/Parchment
using ForgeGradle's createMcpToSrg output.tsrg (named=left, srg=right in tsrg2 format).

Prefer the Gradle task (no Python required):

  .\\gradlew remapDecompiledSrg
"""
from __future__ import annotations

import re
import sys
from pathlib import Path


def parse_tsrg(path: Path) -> dict[str, str]:
    """Build srg -> named map from tsrg2 (ForgeGradle createMcpToSrg output.tsrg).

    Format: one-tab members map SRG (right) to MCP/official (left). Two-tab lines are
    method parameters — skip those here (optional p_* mapping can be added later).
    """
    srg_to_named: dict[str, str] = {}
    text = path.read_text(encoding="utf-8", errors="replace").splitlines()
    if not text or not text[0].startswith("tsrg2"):
        raise SystemExit(f"Unexpected tsrg header: {path}")

    for line in text[1:]:
        # Class names have no leading tab. Members use one tab; inner arg lines use two tabs.
        if not line.startswith("\t") or line.startswith("\t\t"):
            continue
        rest = line[1:].strip()
        if not rest or rest == "static":
            continue
        parts = rest.split()
        if len(parts) < 2:
            continue
        last = parts[-1]
        if re.match(r"^m_\d+_$", last):
            # Method: name (descriptor...) m_SRG — descriptor is second token, starts with '('
            if len(parts) >= 3 and parts[1].startswith("("):
                srg_to_named.setdefault(last, parts[0])
        elif re.match(r"^(?:f|p)_\d+_$", last):
            # Field (or top-level param slot like 0 p_* — rare at one-tab in vanilla export)
            if "(" in parts[0]:
                continue
            srg_to_named.setdefault(last, parts[0])
    return srg_to_named


def remap_java(src_root: Path, mapping: dict[str, str]) -> int:
    replacements = 0
    # Longest SRG first to avoid partial overlaps
    keys = sorted(mapping.keys(), key=len, reverse=True)
    for java in src_root.rglob("*.java"):
        t = java.read_text(encoding="utf-8", errors="replace")
        orig = t
        for srg in keys:
            named = mapping[srg]
            if named == srg:
                continue
            t = t.replace(srg, named)
        if t != orig:
            java.write_text(t, encoding="utf-8")
            replacements += 1
    return replacements


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    tsrg = root / "build" / "createMcpToSrg" / "output.tsrg"
    if not tsrg.is_file():
        print("ERROR: Run RegenResources compileJava once so build/createMcpToSrg/output.tsrg exists.", file=sys.stderr)
        sys.exit(1)
    src = root / "src" / "main" / "java"
    if not src.is_dir():
        sys.exit("No src/main/java")
    mp = parse_tsrg(tsrg)
    n = remap_java(src, mp)
    print(f"remap_decompiled_srg: updated {n} files, {len(mp)} mapping entries loaded")


if __name__ == "__main__":
    main()
