# sim/split_files.py
"""
Split large files into smaller chunks for upload/embedding.

Supports:
 - JSONL (one JSON object per line) -> splits by lines_per_file
 - JSON array (a single JSON array of objects) -> splits by items_per_file
 - Text (.txt, .md) -> splits by max_chars (approximate)
 - CSV -> splits by rows_per_file

CLI examples:
  python -m sim.split_files --input jsonl/heavyarms_chats_export.jsonl --outdir output/split --jsonl-lines 200
  python -m sim.split_files --input data/large.json --outdir output/split --json-items 500
  python -m sim.split_files --input docs/large.txt --outdir output/split --max-chars 50000
  python -m sim.split_files --input data/large.csv --outdir output/split --csv-rows 1000
"""

import os
import json
import csv
import math
from typing import List, Optional
from pathlib import Path


def ensure_outdir(outdir: str):
    os.makedirs(outdir, exist_ok=True)


def numbered_filename(base: str, part: int, total_parts: Optional[int], ext: str) -> str:
    if total_parts:
        width = max(3, len(str(total_parts)))
    else:
        width = max(3, len(str(part)))
    return f"{base}-part{str(part).zfill(width)}{ext}"


def split_jsonl_file(input_path: str, outdir: str, lines_per_file: int = 500) -> List[str]:
    ensure_outdir(outdir)
    base = Path(input_path).stem
    out_paths = []
    part = 1
    buffer = []
    total_lines = 0
    # First pass maybe count lines for nicer naming; optional
    with open(input_path, "r", encoding="utf-8", errors="ignore") as fh:
        for _ in fh:
            total_lines += 1
    total_parts = math.ceil(total_lines / lines_per_file) if total_lines else None

    with open(input_path, "r", encoding="utf-8", errors="ignore") as fh:
        for i, line in enumerate(fh, start=1):
            buffer.append(line.rstrip("\n"))
            if len(buffer) >= lines_per_file:
                out_name = numbered_filename(base, part, total_parts, ".jsonl")
                out_path = os.path.join(outdir, out_name)
                with open(out_path, "w", encoding="utf-8") as out_f:
                    out_f.write("\n".join(buffer) + "\n")
                out_paths.append(out_path)
                buffer = []
                part += 1
        # leftover
        if buffer:
            out_name = numbered_filename(base, part, total_parts, ".jsonl")
            out_path = os.path.join(outdir, out_name)
            with open(out_path, "w", encoding="utf-8") as out_f:
                out_f.write("\n".join(buffer) + "\n")
            out_paths.append(out_path)
    return out_paths


def split_json_array_file(input_path: str, outdir: str, items_per_file: int = 500) -> List[str]:
    ensure_outdir(outdir)
    base = Path(input_path).stem
    out_paths = []
    with open(input_path, "r", encoding="utf-8", errors="ignore") as fh:
        data = json.load(fh)
    if not isinstance(data, list):
        raise ValueError("JSON file does not contain a top-level array.")
    total = len(data)
    total_parts = math.ceil(total / items_per_file) if total else 0
    for part, start in enumerate(range(0, total, items_per_file), start=1):
        chunk = data[start : start + items_per_file]
        out_name = numbered_filename(base, part, total_parts, ".json")
        out_path = os.path.join(outdir, out_name)
        with open(out_path, "w", encoding="utf-8") as out_f:
            json.dump(chunk, out_f, ensure_ascii=False, indent=2)
        out_paths.append(out_path)
    return out_paths


def split_text_file(input_path: str, outdir: str, max_chars: int = 50000, split_on_lines: bool = True) -> List[str]:
    ensure_outdir(outdir)
    base = Path(input_path).stem
    out_paths = []
    with open(input_path, "r", encoding="utf-8", errors="ignore") as fh:
        text = fh.read()
    if split_on_lines:
        # prefer splitting on paragraph/line boundaries to avoid cutting sentences
        paragraphs = text.split("\n\n")
        buffer = ""
        part = 1
        for p in paragraphs:
            # include paragraph plus double newline
            candidate = (buffer + "\n\n" + p).strip() if buffer else p
            if len(candidate) > max_chars and buffer:
                out_name = numbered_filename(base, part, None, ".txt")
                out_path = os.path.join(outdir, out_name)
                with open(out_path, "w", encoding="utf-8") as out_f:
                    out_f.write(buffer.strip() + "\n")
                out_paths.append(out_path)
                part += 1
                buffer = p
            else:
                buffer = candidate
        if buffer:
            out_name = numbered_filename(base, part, None, ".txt")
            out_path = os.path.join(outdir, out_name)
            with open(out_path, "w", encoding="utf-8") as out_f:
                out_f.write(buffer.strip() + "\n")
            out_paths.append(out_path)
    else:
        # simple fixed-size slicing
        total = len(text)
        parts = math.ceil(total / max_chars) if total else 0
        for i in range(parts):
            start = i * max_chars
            out_name = numbered_filename(base, i + 1, parts, ".txt")
            out_path = os.path.join(outdir, out_name)
            with open(out_path, "w", encoding="utf-8") as out_f:
                out_f.write(text[start : start + max_chars])
            out_paths.append(out_path)
    return out_paths


def split_csv_file(input_path: str, outdir: str, rows_per_file: int = 1000) -> List[str]:
    ensure_outdir(outdir)
    base = Path(input_path).stem
    out_paths = []
    with open(input_path, newline="", encoding="utf-8", errors="ignore") as fh:
        reader = csv.reader(fh)
        header = next(reader, None)
        part = 1
        rows = []
        for row in reader:
            rows.append(row)
            if len(rows) >= rows_per_file:
                out_name = numbered_filename(base, part, None, ".csv")
                out_path = os.path.join(outdir, out_name)
                with open(out_path, "w", newline="", encoding="utf-8") as out_f:
                    writer = csv.writer(out_f)
                    if header:
                        writer.writerow(header)
                    writer.writerows(rows)
                out_paths.append(out_path)
                rows = []
                part += 1
        if rows:
            out_name = numbered_filename(base, part, None, ".csv")
            out_path = os.path.join(outdir, out_name)
            with open(out_path, "w", newline="", encoding="utf-8") as out_f:
                writer = csv.writer(out_f)
                if header:
                    writer.writerow(header)
                writer.writerows(rows)
            out_paths.append(out_path)
    return out_paths


def split_file_auto(input_path: str, outdir: str,
                    jsonl_lines: int = 500,
                    json_items: int = 500,
                    max_chars: int = 50000,
                    csv_rows: int = 1000) -> List[str]:
    """
    Autodetect file type and split appropriately.
    Returns list of generated file paths.
    """
    ext = Path(input_path).suffix.lower()
    if ext == ".jsonl":
        return split_jsonl_file(input_path, outdir, lines_per_file=jsonl_lines)
    if ext == ".json":
        # try to detect if it's an array
        with open(input_path, "r", encoding="utf-8", errors="ignore") as fh:
            head = fh.read(2048).lstrip()
            if head.startswith("["):
                return split_json_array_file(input_path, outdir, items_per_file=json_items)
            else:
                # fallback: treat as single JSON doc, write copy
                ensure_outdir(outdir)
                out_path = os.path.join(outdir, os.path.basename(input_path))
                with open(input_path, "r", encoding="utf-8") as src, open(out_path, "w", encoding="utf-8") as dst:
                    dst.write(src.read())
                return [out_path]
    if ext in {".txt", ".md"}:
        return split_text_file(input_path, outdir, max_chars=max_chars)
    if ext == ".csv":
        return split_csv_file(input_path, outdir, rows_per_file=csv_rows)
    # unknown: copy as-is
    ensure_outdir(outdir)
    out_path = os.path.join(outdir, os.path.basename(input_path))
    with open(input_path, "rb") as src, open(out_path, "wb") as dst:
        dst.write(src.read())
    return [out_path]


# CLI
if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Split large file(s) into smaller chunks for upload/embedding")
    parser.add_argument("--input", "-i", required=True, help="Input file or directory")
    parser.add_argument("--outdir", "-o", default="./split_output", help="Output directory for chunk files")
    parser.add_argument("--jsonl-lines", type=int, default=500, help="Lines per chunk for .jsonl files")
    parser.add_argument("--json-items", type=int, default=500, help="Items per chunk for JSON array files")
    parser.add_argument("--max-chars", type=int, default=50000, help="Max chars per text chunk")
    parser.add_argument("--csv-rows", type=int, default=1000, help="Rows per CSV chunk")
    args = parser.parse_args()

    input_path = args.input
    outdir = args.outdir
    generated = []

    if os.path.isdir(input_path):
        # split all matching files in directory
        for fname in os.listdir(input_path):
            fpath = os.path.join(input_path, fname)
            if os.path.isfile(fpath):
                generated.extend(split_file_auto(fpath, outdir,
                                                jsonl_lines=args.jsonl_lines,
                                                json_items=args.json_items,
                                                max_chars=args.max_chars,
                                                csv_rows=args.csv_rows))
    else:
        generated = split_file_auto(input_path, outdir,
                                    jsonl_lines=args.jsonl_lines,
                                    json_items=args.json_items,
                                    max_chars=args.max_chars,
                                    csv_rows=args.csv_rows)

    print("Generated files:")
    for p in generated:
        print(p)
