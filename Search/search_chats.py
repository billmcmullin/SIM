#!/usr/bin/env python3
"""
search_chats.py

Search chat exports for given terms and report occurrences (message-level),
show which widget/embed each matching message came from, and write CSVs.

Usage examples:
  # aggregated (deduped) summary + details with per-widget columns and translation summary
  python search_chats.py --csv summary.csv --details matches.csv --per-widget --translation-csv trans_summary.csv

  # per-file view (no dedupe) and write per-file CSVs into ./per_file_csv and per-widget details
  python search_chats.py --per-file --file-csv-dir per_file_csv --details-dir per_widget_details --per-widget
"""
import argparse
import json
import re
import csv
from pathlib import Path
from typing import Dict, List, Tuple, Any, Optional, Set

# Default search terms and their regex (case-insensitive)
DEFAULT_TERM_PATTERNS = {
    #ex     "TERM": r"(?i)(?<!\w)TERM(?!\w)",
}

OTHER_TERM_LABEL = "Other (no-term)"
MULTIPLE_LABEL = "Multiple"

_PROMPT_LINE_RE = re.compile(r"^\s*Prompt\s*:\s*(.+)$", flags=re.IGNORECASE)


def sanitize_csv_cell(text: Optional[str], max_len: Optional[int] = None) -> str:
    """Return a single-line safe string for CSV cells."""
    if not text:
        return ""
    s = str(text)
    s = s.replace("\r", " ").replace("\n", " ")
    s = re.sub(r"\s+", " ", s).strip()
    if max_len and len(s) > max_len:
        return s[: max_len - 3] + "..."
    return s


def compile_patterns(term_patterns: Dict[str, str]) -> Dict[str, re.Pattern]:
    return {term: re.compile(p) for term, p in term_patterns.items()}


def contains_term(pat: re.Pattern, text: str) -> bool:
    if not text:
        return False
    return bool(pat.search(text))


def first_match_snippet(pat: re.Pattern, text: str, ctx: int = 30) -> Optional[str]:
    if not text:
        return None
    m = pat.search(text)
    if not m:
        return None
    start, end = m.start(), m.end()
    s = max(0, start - ctx)
    e = min(len(text), end + ctx)
    snippet = text[s:e].replace("\n", " ").strip()
    return snippet


def extract_widget_name(obj: dict) -> str:
    src = obj.get("source") or {}
    if isinstance(src, dict):
        for k in ("embed_name", "embedName", "name"):
            v = src.get(k)
            if isinstance(v, str) and v:
                return v
        v = src.get("embed_uuid") or src.get("embedId")
        if isinstance(v, str) and v:
            return v
    conn = obj.get("connection") or {}
    if isinstance(conn, dict):
        for k in ("name", "workspace", "id", "uuid"):
            v = conn.get(k)
            if isinstance(v, str) and v:
                return v
        ws = conn.get("workspace") or {}
        if isinstance(ws, dict) and ws.get("name"):
            return ws.get("name")
    return "<unknown>"


def build_full_message(obj: dict) -> str:
    parts: List[str] = []
    if isinstance(obj.get("prompt"), str) and obj.get("prompt").strip():
        parts.append(obj.get("prompt").strip())
    if isinstance(obj.get("text"), str) and obj.get("text").strip():
        parts.append(obj.get("text").strip())
    raw = obj.get("raw_chat") or obj.get("chat") or {}
    if isinstance(raw, dict):
        for k in ("message", "response", "content", "user_message", "assistant", "answer"):
            v = raw.get(k)
            if isinstance(v, str) and v.strip():
                parts.append(v.strip())
    return " ".join(parts).strip()


def extract_prompt(obj: dict) -> str:
    if isinstance(obj.get("prompt"), str) and obj.get("prompt").strip():
        return obj.get("prompt").strip()
    for k in ("message", "user_message", "content"):
        v = obj.get(k)
        if isinstance(v, str) and v.strip():
            return v.strip()
    raw = obj.get("raw_chat") or obj.get("chat") or {}
    if isinstance(raw, dict):
        for k in ("prompt", "message", "user_message", "content"):
            v = raw.get(k)
            if isinstance(v, str) and v.strip():
                return v.strip()
    return ""


def extract_prompt_from_human_line(line: str) -> Optional[str]:
    m = _PROMPT_LINE_RE.match(line)
    if m:
        return m.group(1).strip()
    return None


def make_unique_id(obj: dict, path_name: str, line_no: int) -> str:
    uid = obj.get("chat_id") or obj.get("id") or obj.get("session_id") or obj.get("sessionId")
    if uid:
        return str(uid)
    embed = (obj.get("source") or {}).get("embed_uuid") or (obj.get("connection") or {}).get("uuid") or ""
    return f"{embed}::{path_name}::line{line_no}"


def aggregate_maps(map_list: List[Dict[str, int]]) -> Dict[str, int]:
    agg: Dict[str, int] = {}
    for m in map_list:
        for k, v in m.items():
            agg[k] = agg.get(k, 0) + v
    return agg


def process_file_no_dedupe(
    fp: Path,
    patterns: Dict[str, re.Pattern],
    term_patterns: Dict[str, str],
    args,
) -> Tuple[Dict[str, int], List[Dict[str, Any]], Dict[str, int], Dict[str,int], Dict[str,Dict[str,int]], Set[str]]:
    msgs_this_file: Dict[str, int] = {t: 0 for t in term_patterns.keys()}
    msgs_this_file[MULTIPLE_LABEL] = 0
    msgs_this_file[OTHER_TERM_LABEL] = 0
    details_rows: List[Dict[str, Any]] = []
    widget_totals: Dict[str, int] = {}
    widget_trans_totals: Dict[str,int] = {}
    widget_trans_by_lang: Dict[str, Dict[str,int]] = {}
    langs_seen: Set[str] = set()

    with fp.open("r", encoding="utf-8", errors="replace") as fh:
        for line_no, line in enumerate(fh, start=1):
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            prompt = extract_prompt(obj)
            widget = extract_widget_name(obj)
            widget_totals[widget] = widget_totals.get(widget, 0) + (1 if prompt else 0)

            translated_flag = bool(obj.get("translated_text") or obj.get("translated_prompt"))
            translated_from = (obj.get("translated_from") or obj.get("language") or "").strip() if translated_flag else ""
            if translated_flag:
                widget_trans_totals[widget] = widget_trans_totals.get(widget, 0) + 1
                lang = translated_from or "unknown"
                widget_trans_by_lang.setdefault(widget, {})
                widget_trans_by_lang[widget][lang] = widget_trans_by_lang[widget].get(lang, 0) + 1
                langs_seen.add(lang)

            if not prompt:
                msgs_this_file[OTHER_TERM_LABEL] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": obj.get("chat_id") or obj.get("id") or "",
                            "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                            "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                            "term": OTHER_TERM_LABEL,
                            "matched_terms": "",
                            "full_message": build_full_message(obj),
                            "translated_from": translated_from,
                            "translated_to": obj.get("translated_to",""),
                        }
                    )
                continue

            matched_terms = [term for term, pat in patterns.items() if contains_term(pat, prompt)]

            if len(matched_terms) == 0:
                msgs_this_file[OTHER_TERM_LABEL] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": obj.get("chat_id") or obj.get("id") or "",
                            "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                            "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                            "term": OTHER_TERM_LABEL,
                            "matched_terms": "",
                            "full_message": build_full_message(obj),
                            "translated_from": translated_from,
                            "translated_to": obj.get("translated_to",""),
                        }
                    )
            elif len(matched_terms) == 1:
                term = matched_terms[0]
                msgs_this_file[term] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": obj.get("chat_id") or obj.get("id") or "",
                            "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                            "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                            "term": term,
                            "matched_terms": term,
                            "full_message": build_full_message(obj),
                            "translated_from": translated_from,
                            "translated_to": obj.get("translated_to",""),
                        }
                    )
            else:
                msgs_this_file[MULTIPLE_LABEL] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": obj.get("chat_id") or obj.get("id") or "",
                            "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                            "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                            "term": MULTIPLE_LABEL,
                            "matched_terms": ",".join(matched_terms),
                            "full_message": build_full_message(obj),
                            "translated_from": translated_from,
                            "translated_to": obj.get("translated_to",""),
                        }
                    )
    return msgs_this_file, details_rows, widget_totals, widget_trans_totals, widget_trans_by_lang, langs_seen


def process_human_file_no_dedupe(
    fp: Path,
    patterns: Dict[str, re.Pattern],
    term_patterns: Dict[str, str],
    args,
) -> Tuple[Dict[str, int], List[Dict[str, Any]], Dict[str, int], Dict[str,int], Dict[str,Dict[str,int]], Set[str]]:
    msgs_this_file: Dict[str, int] = {t: 0 for t in term_patterns.keys()}
    msgs_this_file[MULTIPLE_LABEL] = 0
    msgs_this_file[OTHER_TERM_LABEL] = 0
    details_rows: List[Dict[str, Any]] = []
    widget_totals: Dict[str, int] = {}
    widget_trans_totals: Dict[str,int] = {}
    widget_trans_by_lang: Dict[str, Dict[str,int]] = {}
    langs_seen: Set[str] = set()

    with fp.open("r", encoding="utf-8", errors="replace") as fh:
        for line_no, raw_line in enumerate(fh, start=1):
            line = raw_line.strip()
            if not line:
                continue
            prompt = extract_prompt_from_human_line(line)
            if not prompt:
                continue
            widget = fp.stem
            widget_totals[widget] = widget_totals.get(widget, 0) + 1
            matched_terms = [term for term, pat in patterns.items() if contains_term(pat, prompt)]
            if len(matched_terms) == 0:
                msgs_this_file[OTHER_TERM_LABEL] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": "",
                            "session_id": "",
                            "created_at": "",
                            "term": OTHER_TERM_LABEL,
                            "matched_terms": "",
                            "full_message": prompt,
                            "translated_from": "",
                            "translated_to": "",
                        }
                    )
            elif len(matched_terms) == 1:
                term = matched_terms[0]
                msgs_this_file[term] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": "",
                            "session_id": "",
                            "created_at": "",
                            "term": term,
                            "matched_terms": term,
                            "full_message": prompt,
                            "translated_from": "",
                            "translated_to": "",
                        }
                    )
            else:
                msgs_this_file[MULTIPLE_LABEL] += 1
                if args.details or args.details_dir:
                    details_rows.append(
                        {
                            "widget": widget,
                            "chat_id": "",
                            "session_id": "",
                            "created_at": "",
                            "term": MULTIPLE_LABEL,
                            "matched_terms": ",".join(matched_terms),
                            "full_message": prompt,
                            "translated_from": "",
                            "translated_to": "",
                        }
                    )
    return msgs_this_file, details_rows, widget_totals, widget_trans_totals, widget_trans_by_lang, langs_seen


def print_file_summary(fp: Path, msgs: Dict[str, int], widget_totals: Optional[Dict[str, int]] = None):
    total_prompts = sum(v for k, v in msgs.items() if k not in (MULTIPLE_LABEL, OTHER_TERM_LABEL) and k in msgs) + msgs.get(MULTIPLE_LABEL, 0) + msgs.get(OTHER_TERM_LABEL, 0)
    print(f"\nFile: {fp}  (messages with prompts: {total_prompts})")
    print(f"{'Term':25} {'Messages containing':>18}")
    print("-" * 46)
    for term in list(msgs.keys()):
        print(f"{term:25} {msgs.get(term,0):18}")
    if widget_totals:
        print("\nPer-widget prompt totals in this file:")
        for w, tot in sorted(widget_totals.items(), key=lambda x: x[0].lower()):
            print(f"  {w:30} {tot:6}")


def write_per_file_csv(fp: Path, msgs: Dict[str, int], out_dir: Path):
    out_dir.mkdir(parents=True, exist_ok=True)
    csvp = out_dir / (fp.stem + "_summary.csv")
    with csvp.open("w", newline="", encoding="utf-8") as cf:
        writer = csv.writer(cf)
        writer.writerow(["term", "messages_containing"])
        for term in list(msgs.keys()):
            writer.writerow([term, msgs.get(term,0)])
    return csvp


def write_translation_csv(path: Path, per_widget_trans_totals: Dict[str,int], per_widget_trans_by_lang: Dict[str, Dict[str,int]], global_trans_by_lang: Dict[str,int]):
    path.parent.mkdir(parents=True, exist_ok=True)
    all_langs = sorted({lang for d in per_widget_trans_by_lang.values() for lang in d.keys()}.union(global_trans_by_lang.keys()))
    with path.open("w", newline="", encoding="utf-8") as cf:
        writer = csv.writer(cf)
        header = ["widget", "translated_total"] + all_langs
        writer.writerow(header)
        for widget in sorted(per_widget_trans_totals.keys(), key=lambda x: x.lower()):
            total = per_widget_trans_totals.get(widget, 0)
            row = [widget, total]
            by_lang = per_widget_trans_by_lang.get(widget, {})
            for lang in all_langs:
                row.append(by_lang.get(lang, 0))
            writer.writerow(row)
        agg_total = sum(per_widget_trans_totals.values())
        agg_row = ["All widgets", agg_total]
        for lang in all_langs:
            agg_row.append(global_trans_by_lang.get(lang, 0))
        writer.writerow([])
        writer.writerow(["Aggregate"] + [""]*(len(header)-1))
        writer.writerow(agg_row)


def write_details_single(path: Path, rows: List[Dict[str, Any]]):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as cf:
        fieldnames = [
            "widget",
            "chat_id",
            "session_id",
            "created_at",
            "term",
            "matched_terms",
            "translated_from",
            "translated_to",
            "full_message",
        ]
        writer = csv.DictWriter(cf, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        for row in rows:
            fm = sanitize_csv_cell(row.get("full_message", ""), max_len=None)
            writer.writerow(
                {
                    "widget": row.get("widget", ""),
                    "chat_id": row.get("chat_id", ""),
                    "session_id": row.get("session_id", ""),
                    "created_at": row.get("created_at", ""),
                    "term": row.get("term", ""),
                    "matched_terms": row.get("matched_terms", ""),
                    "translated_from": row.get("translated_from", ""),
                    "translated_to": row.get("translated_to", ""),
                    "full_message": fm,
                }
            )


def write_details_per_widget(dirpath: Path, rows: List[Dict[str, Any]]):
    dirpath.mkdir(parents=True, exist_ok=True)
    groups: Dict[str, List[Dict[str, Any]]] = {}
    for r in rows:
        w = r.get("widget") or "unknown_widget"
        groups.setdefault(w, []).append(r)
    for widget, items in groups.items():
        safe = re.sub(r"[^\w\-\.]+", "_", str(widget))[:60] or "unknown_widget"
        p = dirpath / f"details_{safe}.csv"
        write_details_single(p, items)


def main():
    p = argparse.ArgumentParser(description="Search chat JSONL files for topic terms (prompt-only) with translation summary")
    p.add_argument("--jsonl-dir", default="jsonl", help="Directory containing JSONL files (default: ./jsonl)")
    p.add_argument("--scan-human", action="store_true", help="Also scan human-readable text files in ./output (only Prompt: lines)")
    p.add_argument("--per-widget", action="store_true", help="Aggregate results per widget/embed (prints all widgets)")
    p.add_argument("--per-file", action="store_true", help="Show counts for each input file individually (no dedupe across files)")
    p.add_argument("--file-csv-dir", help="Directory to write per-file CSV summaries (used with --per-file)")
    p.add_argument("--details", help="Write detailed CSV with per-message matches (single file)")
    p.add_argument("--details-dir", help="Write per-widget detailed CSVs into this directory")
    p.add_argument("--csv", help="Write summary CSV with totals per term (aggregated, deduped). Not produced when --per-file is used")
    p.add_argument("--translation-csv", help="Write translation summary CSV (translated prompts by source language per widget)")
    p.add_argument("--terms-file", help="Optional file with one search term per line (overrides built-in list). Lines can be:\n - literal term (newline-separated) OR\n - label=regex (explicit regex, not escaped) OR\n - r:regex (raw regex without label; an auto label will be generated)")
    p.add_argument("--verbose", action="store_true")
    p.add_argument("--max-snippet", type=int, default=120, help="Max snippet length in details CSV (default 120)")
    args = p.parse_args()

    if args.terms_file:
        term_patterns = {}
        idx = 0
        for raw_ln in Path(args.terms_file).read_text(encoding="utf-8").splitlines():
            ln = raw_ln.strip()
            if not ln or ln.startswith("#"):
                continue
            # label=pattern -> use pattern as provided (raw regex)
            if "=" in ln:
                label, pattern = ln.split("=", 1)
                label = label.strip() or f"regex:{idx}"
                pattern = pattern.strip()
                term_patterns[label] = pattern
                idx += 1
            # r:pattern -> raw regex with auto label
            elif ln.startswith("r:"):
                pattern = ln[2:].strip()
                label = f"regex:{idx}"
                term_patterns[label] = pattern
                idx += 1
            else:
                # literal term, escape it and make a whole-word case-insensitive regex
                esc = re.escape(ln)
                pat = rf"(?i)(?<!\w){esc}(?!\w)"
                term_patterns[ln] = pat
    else:
        term_patterns = DEFAULT_TERM_PATTERNS

    patterns = compile_patterns(term_patterns)

    jsonl_path = Path(args.jsonl_dir)
    if not jsonl_path.exists() or not jsonl_path.is_dir():
        print("JSONL directory not found:", jsonl_path)
        return

    # Per-file mode (no dedupe across files)
    if args.per_file:
        per_file_csv_out_dir = Path(args.file_csv_dir) if args.file_csv_dir else None
        all_details_rows: List[Dict[str, Any]] = []
        aggregate_widget_totals: Dict[str,int] = {}
        aggregate_widget_trans_totals: Dict[str,int] = {}
        aggregate_widget_trans_by_lang: Dict[str,Dict[str,int]] = {}
        global_trans_by_lang: Dict[str,int] = {}

        for fp in sorted(jsonl_path.glob("*.jsonl")):
            if args.verbose:
                print("Processing file (no dedupe):", fp)
            msgs_this_file, details_rows, widget_totals, widget_trans_totals, widget_trans_by_lang, langs_seen = process_file_no_dedupe(fp, patterns, term_patterns, args)
            print_file_summary(fp, msgs_this_file, widget_totals if args.per_widget else None)
            if per_file_csv_out_dir:
                csvp = write_per_file_csv(fp, msgs_this_file, per_file_csv_out_dir)
                if args.verbose:
                    print("Wrote per-file CSV:", csvp)
            if args.details or args.details_dir:
                all_details_rows.extend(details_rows)
            for w, t in widget_totals.items():
                aggregate_widget_totals[w] = aggregate_widget_totals.get(w, 0) + t
            for w, t in widget_trans_totals.items():
                aggregate_widget_trans_totals[w] = aggregate_widget_trans_totals.get(w, 0) + t
            for w, d in widget_trans_by_lang.items():
                dest = aggregate_widget_trans_by_lang.setdefault(w, {})
                for lang, c in d.items():
                    dest[lang] = dest.get(lang, 0) + c
                    global_trans_by_lang[lang] = global_trans_by_lang.get(lang, 0) + c

        if args.scan_human:
            out_dir = Path("output")
            if out_dir.exists() and out_dir.is_dir():
                for fp in sorted(out_dir.glob("*.txt")):
                    if args.verbose:
                        print("Processing human file (no dedupe):", fp)
                    msgs_this_file, details_rows, widget_totals, widget_trans_totals, widget_trans_by_lang, langs_seen = process_human_file_no_dedupe(fp, patterns, term_patterns, args)
                    print_file_summary(fp, msgs_this_file, widget_totals if args.per_widget else None)
                    if per_file_csv_out_dir:
                        csvp = write_per_file_csv(fp, msgs_this_file, per_file_csv_out_dir)
                        if args.verbose:
                            print("Wrote per-file CSV:", csvp)
                    if args.details or args.details_dir:
                        all_details_rows.extend(details_rows)
                    for w, t in widget_totals.items():
                        aggregate_widget_totals[w] = aggregate_widget_totals.get(w, 0) + t
            else:
                if args.verbose:
                    print("output directory not found; skipping human files.")

        if args.per_widget:
            print("\nAggregate per-widget total prompts (per-file mode):")
            for w, tot in sorted(aggregate_widget_totals.items(), key=lambda x: x[0].lower()):
                print(f"  {w:30} {tot:6}")
            if aggregate_widget_trans_totals:
                print("\nAggregate per-widget translated prompts (per-file mode):")
                for w, tot in sorted(aggregate_widget_trans_totals.items(), key=lambda x: x[0].lower()):
                    print(f"  {w:30} {tot:6}")

        if args.details:
            dp = Path(args.details)
            write_details_single(dp, all_details_rows)
            print("Detailed matches written to:", dp)
        if args.details_dir:
            write_details_per_widget(Path(args.details_dir), all_details_rows)
            print("Per-widget detailed CSVs written to:", args.details_dir)

        if args.translation_csv:
            trans_csv_path = Path(args.translation_csv)
            write_translation_csv(trans_csv_path, aggregate_widget_trans_totals, aggregate_widget_trans_by_lang, global_trans_by_lang)
            print("Translation CSV written to:", trans_csv_path)

        print("Per-file mode complete.")
        return

    # Aggregated deduped mode
    per_file_msgs: Dict[str, Dict[str, int]] = {}
    total_unique_messages_scanned = 0
    details_rows: List[Dict[str, Any]] = []
    per_widget_msgs: Dict[str, Dict[str, int]] = {}
    per_widget_totals: Dict[str,int] = {}
    per_widget_trans_totals: Dict[str,int] = {}
    per_widget_trans_by_lang: Dict[str, Dict[str,int]] = {}
    global_trans_by_lang: Dict[str,int] = {}
    seen_ids: Set[str] = set()

    for fp in sorted(jsonl_path.glob("*.jsonl")):
        if args.verbose:
            print("Processing", fp)
        msgs_this_file: Dict[str, int] = {t: 0 for t in term_patterns.keys()}
        msgs_this_file[MULTIPLE_LABEL] = 0
        msgs_this_file[OTHER_TERM_LABEL] = 0
        new_count = 0
        with fp.open("r", encoding="utf-8", errors="replace") as fh:
            for line_no, line in enumerate(fh, start=1):
                line = line.strip()
                if not line:
                    continue
                try:
                    obj = json.loads(line)
                except Exception:
                    continue
                uid = make_unique_id(obj, fp.name, line_no)
                if uid in seen_ids:
                    continue
                seen_ids.add(uid)
                new_count += 1

                prompt = extract_prompt(obj)
                widget = extract_widget_name(obj)
                if widget not in per_widget_msgs:
                    per_widget_msgs[widget] = {t: 0 for t in term_patterns.keys()}
                    per_widget_msgs[widget][MULTIPLE_LABEL] = 0
                    per_widget_msgs[widget][OTHER_TERM_LABEL] = 0
                per_widget_totals[widget] = per_widget_totals.get(widget, 0) + (1 if prompt else 0)

                translated_flag = bool(obj.get("translated_text") or obj.get("translated_prompt"))
                translated_from = (obj.get("translated_from") or obj.get("language") or "").strip() if translated_flag else ""
                if translated_flag:
                    per_widget_trans_totals[widget] = per_widget_trans_totals.get(widget, 0) + 1
                    by_lang = per_widget_trans_by_lang.setdefault(widget, {})
                    lang = translated_from or "unknown"
                    by_lang[lang] = by_lang.get(lang, 0) + 1
                    global_trans_by_lang[lang] = global_trans_by_lang.get(lang, 0) + 1

                if not prompt:
                    msgs_this_file[OTHER_TERM_LABEL] += 1
                    per_widget_msgs[widget][OTHER_TERM_LABEL] += 1
                    if args.details or args.details_dir:
                        details_rows.append(
                            {
                                "widget": widget,
                                "chat_id": obj.get("chat_id") or obj.get("id") or "",
                                "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                                "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                                "term": OTHER_TERM_LABEL,
                                "matched_terms": "",
                                "full_message": build_full_message(obj),
                                "translated_from": translated_from,
                                "translated_to": obj.get("translated_to",""),
                            }
                        )
                    continue

                matched_terms = [term for term, pat in patterns.items() if contains_term(pat, prompt)]

                if len(matched_terms) == 0:
                    msgs_this_file[OTHER_TERM_LABEL] += 1
                    per_widget_msgs[widget][OTHER_TERM_LABEL] += 1
                    if args.details or args.details_dir:
                        details_rows.append(
                            {
                                "widget": widget,
                                "chat_id": obj.get("chat_id") or obj.get("id") or "",
                                "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                                "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                                "term": OTHER_TERM_LABEL,
                                "matched_terms": "",
                                "full_message": build_full_message(obj),
                                "translated_from": translated_from,
                                "translated_to": obj.get("translated_to",""),
                            }
                        )
                elif len(matched_terms) == 1:
                    term = matched_terms[0]
                    msgs_this_file[term] += 1
                    per_widget_msgs[widget][term] += 1
                    if args.details or args.details_dir:
                        details_rows.append(
                            {
                                "widget": widget,
                                "chat_id": obj.get("chat_id") or obj.get("id") or "",
                                "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                                "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                                "term": term,
                                "matched_terms": term,
                                "full_message": build_full_message(obj),
                                "translated_from": translated_from,
                                "translated_to": obj.get("translated_to",""),
                            }
                        )
                else:
                    msgs_this_file[MULTIPLE_LABEL] += 1
                    per_widget_msgs[widget][MULTIPLE_LABEL] += 1
                    if args.details or args.details_dir:
                        details_rows.append(
                            {
                                "widget": widget,
                                "chat_id": obj.get("chat_id") or obj.get("id") or "",
                                "session_id": obj.get("session_id") or obj.get("sessionId") or "",
                                "created_at": obj.get("created_at") or obj.get("createdAt") or "",
                                "term": MULTIPLE_LABEL,
                                "matched_terms": ",".join(matched_terms),
                                "full_message": build_full_message(obj),
                                "translated_from": translated_from,
                                "translated_to": obj.get("translated_to",""),
                            }
                        )
        per_file_msgs[str(fp)] = msgs_this_file
        total_unique_messages_scanned += int(new_count or 0)

    if args.scan_human:
        out_dir = Path("output")
        if out_dir.exists() and out_dir.is_dir():
            for fp in sorted(out_dir.glob("*.txt")):
                if args.verbose:
                    print("Processing human file", fp)
                msgs_this_file, hr_details_rows, widget_totals, widget_trans_totals, widget_trans_by_lang, langs_seen = process_human_file_no_dedupe(fp, patterns, term_patterns, args)
                # accumulate
                if args.details or args.details_dir:
                    details_rows.extend(hr_details_rows)
                for w, t in widget_totals.items():
                    per_widget_totals[w] = per_widget_totals.get(w, 0) + t
        else:
            if args.verbose:
                print("output directory not found; skipping human files.")

    totals_msgs = aggregate_maps(list(per_file_msgs.values())) if per_file_msgs else {t: 0 for t in term_patterns.keys()}
    totals_msgs[MULTIPLE_LABEL] = totals_msgs.get(MULTIPLE_LABEL, 0)
    totals_msgs[OTHER_TERM_LABEL] = totals_msgs.get(OTHER_TERM_LABEL, 0)

    print()
    print("SUMMARY (message-level counts; prompt-only, deduplicated across files)")
    print("Total unique messages scanned:", total_unique_messages_scanned)
    print()
    print(f"{'Term':25} {'Messages containing':>18}")
    print("-" * 46)
    for term in list(term_patterns.keys()) + [MULTIPLE_LABEL, OTHER_TERM_LABEL]:
        print(f"{term:25} {totals_msgs.get(term,0):18}")
    print()

    if args.per_widget:
        print("Per-widget breakdown (message-level; prompt-only):")
        for widget in sorted(per_widget_msgs.keys(), key=lambda x: x.lower()):
            print(f"\nWidget: {widget}")
            for term in list(term_patterns.keys()) + [MULTIPLE_LABEL, OTHER_TERM_LABEL]:
                print(f"  {term:20} messages: {per_widget_msgs.get(widget,{}).get(term,0):6}")
        print("\nTotal prompts per widget:")
        for w, tot in sorted(per_widget_totals.items(), key=lambda x: x[0].lower()):
            print(f"  {w:30} {tot:6}")
        if per_widget_trans_totals:
            print("\nTranslated prompts per widget (counts):")
            for w, tot in sorted(per_widget_trans_totals.items(), key=lambda x: x[0].lower()):
                print(f"  {w:30} {tot:6}")
            print("\nTranslated prompts per widget by source language:")
            for w, by_lang in sorted(per_widget_trans_by_lang.items(), key=lambda x: x[0].lower()):
                parts = ", ".join(f"{lang}:{count}" for lang, count in sorted(by_lang.items(), key=lambda x: x[0]))
                print(f"  {w:30} {parts}")
        print()

    if args.csv:
        csvp = Path(args.csv)
        csvp.parent.mkdir(parents=True, exist_ok=True)

        # Use only the widget display names present (deduped, preserve order)
        widget_cols = sorted(per_widget_msgs.keys(), key=lambda x: x.lower())
        widget_cols_unique = list(dict.fromkeys(widget_cols))

        header = ["term", "total_messages_containing"] + widget_cols_unique
        with csvp.open("w", newline="", encoding="utf-8") as cf:
            writer = csv.writer(cf)
            writer.writerow(header)
            for term in list(term_patterns.keys()) + [MULTIPLE_LABEL, OTHER_TERM_LABEL]:
                total = totals_msgs.get(term, 0)
                row = [term, total]
                for w in widget_cols_unique:
                    row.append(per_widget_msgs.get(w, {}).get(term, 0))
                writer.writerow(row)
        print("Summary CSV written to:", csvp)

    if args.translation_csv:
        trans_csv_path = Path(args.translation_csv)
        write_translation_csv(trans_csv_path, per_widget_trans_totals, per_widget_trans_by_lang, global_trans_by_lang)
        print("Translation CSV written to:", trans_csv_path)

    # Write details (single file and/or per-widget files)
    if args.details:
        dp = Path(args.details)
        write_details_single(dp, details_rows)
        print("Detailed matches written to:", dp)
    if args.details_dir:
        write_details_per_widget(Path(args.details_dir), details_rows)
        print("Per-widget detailed CSVs written to:", args.details_dir)

    print("Done.")


if __name__ == "__main__":
    main()
