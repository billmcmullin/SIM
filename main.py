#!/usr/bin/env python3
"""
main.py

Dispatcher for the exporter and search_chats.py.

Features:
- export only (default)
- search only (--search)
- export then search (--export-and-search) — search runs only if export succeeded
- translations opt-in via --translate
- embed map can be provided via --embed-map (JSON or CSV)
- logging flags (--log-dir, --log-file, --log-level, --console-level)
- forwarded args to search_chats.py after a `--` separator
"""
# --- early logging configuration (parse only logging-related CLI flags) ---
import argparse
import logging
from logging.handlers import RotatingFileHandler
from pathlib import Path
from datetime import datetime, timezone
import sys
import subprocess
import shlex

# parse only logging flags so we can configure logging before other imports
_log_parser = argparse.ArgumentParser(add_help=False)
_log_parser.add_argument("--log-dir", default="logs", help="Directory to write log files (default: ./logs)")
_log_parser.add_argument(
    "--log-file",
    default=None,
    help="Optional fixed log filename (inside --log-dir). If omitted a timestamped file is used.",
)
_log_parser.add_argument(
    "--log-level",
    default="DEBUG",
    choices=["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"],
    help="Minimum level to write to the log file (default: DEBUG).",
)
_log_parser.add_argument(
    "--console-level",
    default="WARNING",
    choices=["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"],
    help="Minimum level to show on console (default: WARNING).",
)
# parse known args so we don't consume application args
_log_args, _remaining_argv = _log_parser.parse_known_args()

# create log directory
log_dir = Path(_log_args.log_dir)
log_dir.mkdir(parents=True, exist_ok=True)

# choose logfile name; use timezone-aware UTC now
if _log_args.log_file:
    log_file = log_dir / _log_args.log_file
else:
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
    log_file = log_dir / f"export_{timestamp}.log"

# configure handlers
file_handler = RotatingFileHandler(str(log_file), maxBytes=10 * 1024 * 1024, backupCount=5, encoding="utf-8")
file_formatter = logging.Formatter("%(asctime)s %(levelname)-8s [%(name)s:%(lineno)d] %(message)s")
file_handler.setFormatter(file_formatter)
file_handler.setLevel(getattr(logging, _log_args.log_level.upper(), logging.DEBUG))

console_handler = logging.StreamHandler(sys.stdout)
console_formatter = logging.Formatter("%(levelname)-8s: %(message)s")
console_handler.setFormatter(console_formatter)
console_handler.setLevel(getattr(logging, _log_args.console_level.upper(), logging.WARNING))

root_logger = logging.getLogger()
# remove existing handlers to avoid duplicates if re-run in same process
for h in list(root_logger.handlers):
    root_logger.removeHandler(h)
root_logger.setLevel(logging.DEBUG)  # allow all levels through; handlers filter
root_logger.addHandler(file_handler)
root_logger.addHandler(console_handler)

# Redirect stdout/stderr to the logging system so print() and other writes are captured.
class StreamToLogger:
    """
    Redirect writes to a logger instance.
    Usage:
        sys.stdout = StreamToLogger(logging.getLogger('stdout'), logging.INFO)
        sys.stderr = StreamToLogger(logging.getLogger('stderr'), logging.ERROR)
    """
    def __init__(self, logger, level):
        self.logger = logger
        self.level = level
        self._buffer = ""

    def write(self, buf):
        # Accumulate and emit complete lines
        self._buffer += buf
        while "\n" in self._buffer:
            line, self._buffer = self._buffer.split("\n", 1)
            if line:
                self.logger.log(self.level, line)

    def flush(self):
        if self._buffer:
            self.logger.log(self.level, self._buffer)
            self._buffer = ""

# attach stdout/stderr redirection
stdout_logger = logging.getLogger("STDOUT")
stderr_logger = logging.getLogger("STDERR")
sys.stdout = StreamToLogger(stdout_logger, logging.INFO)
sys.stderr = StreamToLogger(stderr_logger, logging.ERROR)

# quiet noisy third-party loggers by default
logging.getLogger("googletrans").setLevel(logging.WARNING)
logging.getLogger("urllib3").setLevel(logging.WARNING)
# -------------------------------------------------------------------

logging.basicConfig(level=logging.INFO)   # fallback basic config

import json
from typing import List, Dict, Any

# config values; also import config module to allow runtime set_embed_map_file
import config
from config import (
    DEFAULT_INSECURE,
    DEFAULT_BASE,
    DEFAULT_API_KEY,
    DEFAULT_API_HEADER,
    DEFAULT_JSONL,
    DEFAULT_HUMAN,
    DEFAULT_DB,
    DEFAULT_CA_BUNDLE,
    DEFAULT_VERBOSE,
    now_iso,
)

from http_client import HTTPClient
from db import init_db, upsert_chat
from normalize import normalize_embed_chat
from writers import write_global_jsonl, write_human_readable, export_per_widget, detect_language
import writers as writers_module  # to set TRANSLATION_ENABLED flag

logger = logging.getLogger(__name__)

def run_search_chats(forward_args: List[str]) -> int:
    """
    Run search_chats.py as a subprocess, forwarding the provided args list.
    Returns the subprocess return code.
    """
    script_path = Path(__file__).parent / "search_chats.py"
    if not script_path.exists():
        logger.error("search_chats.py not found at %s", script_path)
        return 2
    cmd = [sys.executable, str(script_path)] + forward_args
    logger.info("Running search_chats: %s", " ".join(shlex.quote(x) for x in cmd))
    try:
        res = subprocess.run(cmd, check=False)
        logger.info("search_chats.py exited with returncode=%s", res.returncode)
        return res.returncode
    except Exception as e:
        logger.exception("Failed to run search_chats.py: %s", e)
        return 1

def build_headers(api_key: str, header_name: str):
    hdrs = {}
    if api_key:
        if api_key.lower().startswith("bearer "):
            hdrs[header_name] = api_key
        else:
            if header_name.lower() == "authorization":
                hdrs[header_name] = f"Bearer {api_key}"
            else:
                hdrs[header_name] = api_key
    return hdrs

def perform_export(base: str,
                   api_key: str,
                   api_header: str,
                   jsonl_out: str,
                   human_out: str,
                   db_file: str,
                   ca_bundle: str,
                   insecure: bool,
                   verbose: bool,
                   all_embeds: bool,
                   use_db: bool,
                   write_raw: bool,
                   raw_out: str,
                   translate: bool = False):
    """
    Run the export workflow. If translate=True, language detection and prompt translation
    will be enabled (incurs additional time).
    """
    # Set writers module flag to enable/disable translation behavior
    try:
        writers_module.TRANSLATION_ENABLED = bool(translate)
    except Exception:
        logger.debug("Could not set writers_module.TRANSLATION_ENABLED", exc_info=True)

    headers = build_headers(api_key, api_header)
    verify = True
    if insecure:
        verify = False
    elif ca_bundle:
        verify = ca_bundle

    client = HTTPClient(base=base, headers=headers, verify=verify)

    top_dir = Path(jsonl_out).parent if jsonl_out else Path.cwd()
    jsonl_dir = top_dir / "jsonl"
    output_dir = top_dir / "output"
    jsonl_dir.mkdir(parents=True, exist_ok=True)
    output_dir.mkdir(parents=True, exist_ok=True)

    global_jsonl_path = str(jsonl_dir / Path(jsonl_out).name)
    global_human_path = str(output_dir / Path(human_out).name)
    raw_path = str(jsonl_dir / Path(raw_out).name) if write_raw and raw_out else None

    if use_db:
        init_db(db_file)

    docs_all: List[Dict[str,Any]] = []
    docs_by_widget: Dict[str, List[Dict[str,Any]]] = {}

    embeds = client.fetch_embeds()
    if verbose:
        print("DEBUG: found embeds:", len(embeds))

    for embed in embeds:
        embed_uuid = str(embed.get("uuid") or embed.get("id") or embed.get("embed_uuid") or embed.get("embedId") or "")
        if not embed_uuid:
            continue
        embed_uuid_l = embed_uuid.lower()
        # Use config.EMBED_NAME_MAP so runtime changes (via --embed-map) take effect
        if (not all_embeds) and (embed_uuid_l not in config.EMBED_NAME_MAP):
            if verbose:
                print(f"DEBUG: skipping embed {embed_uuid} as not in default map")
            continue
        chats = client.fetch_chats_for_embed(embed_uuid)
        if verbose:
            print(f"DEBUG: embed {embed_uuid} returned {len(chats)} chats")
        for idx, raw in enumerate(chats):
            doc = normalize_embed_chat(embed, raw, idx=idx)

            # Ensure doc["source"] exists and is a dict
            if "source" not in doc or not isinstance(doc["source"], dict):
                doc["source"] = {}

            # Prefer mapping from embed UUID -> friendly name (from config.EMBED_NAME_MAP)
            display_name = config.EMBED_NAME_MAP.get(embed_uuid_l)
            if not display_name:
                # fall back to any embed_name returned by API (if present) or the UUID
                display_name = doc.get("source", {}).get("embed_name") or embed_uuid

            # Ensure the normalized doc includes the display name and uuid (useful for writers/search)
            doc["source"]["embed_name"] = display_name
            doc["source"]["embed_uuid"] = embed_uuid

            # Only run language detection if translation was requested
            if translate:
                try:
                    doc["language"] = detect_language(doc.get("text",""))
                except Exception:
                    doc["language"] = ""
            else:
                doc["language"] = ""

            docs_all.append(doc)

            # Use the friendly display_name (not raw API value) as the grouping key
            widget_name = display_name
            docs_by_widget.setdefault(widget_name, []).append(doc)

            if use_db:
                upsert_chat(doc, db_path=db_file)

    if docs_all:
        write_global_jsonl(global_jsonl_path, docs_all)
        write_human_readable(global_human_path, docs_all)

    per_jsonl, per_human = export_per_widget(jsonl_dir, output_dir, Path(global_jsonl_path).name, Path(global_human_path).name, docs_by_widget)

    return {"indexed": len(docs_all), "jsonl": global_jsonl_path, "human": global_human_path, "per_widget_jsonl": per_jsonl, "per_widget_human": per_human, "raw": raw_path}

def parse_args():
    # Use parse_known_args so we can forward unknown args to search_chats.py after a "--"
    parser = argparse.ArgumentParser(description="AnythingLLM embedded-chat exporter (modular)",
                                     parents=[_log_parser])
    parser.add_argument("--base", default=DEFAULT_BASE)
    parser.add_argument("--api-key", default=DEFAULT_API_KEY)
    parser.add_argument("--api-header", default=DEFAULT_API_HEADER)
    parser.add_argument("--jsonl", default=DEFAULT_JSONL)
    parser.add_argument("--human", default=DEFAULT_HUMAN)
    parser.add_argument("--raw", default="")
    parser.add_argument("--db", default=DEFAULT_DB)
    parser.add_argument("--no-db", action="store_true")
    parser.add_argument("--insecure", action="store_true")
    parser.add_argument("--ca-bundle", default=DEFAULT_CA_BUNDLE)
    parser.add_argument("--verbose", action="store_true", default=DEFAULT_VERBOSE)
    parser.add_argument("--all-embeds", action="store_true")
    # embed map file (runtime override)
    parser.add_argument("--embed-map", help="Path to embed map file (JSON or CSV). Overrides built-in embed map.")
    # New flags to control running search_chats.py
    parser.add_argument("--search", action="store_true", help="Run search_chats.py instead of (or in addition to) export. Use -- to forward args to search_chats.py")
    parser.add_argument("--export-and-search", action="store_true", help="Run export then run search_chats.py if export succeeded. Forward args to search_chats.py after --")
    # New flag to enable translations (disabled by default)
    parser.add_argument("--translate", action="store_true", help="Enable prompt translation and language detection (disabled by default)")
    # parse_known_args returns (known, unknown). unknown will include args after "--"
    known, unknown = parser.parse_known_args()
    return known, unknown

if __name__ == "__main__":
    args, forwarded = parse_args()

    # If user provided an embed map file via CLI, try to load it now (so export/search use it)
    if getattr(args, "embed_map", None):
        try:
            OK = config.set_embed_map_file(args.embed_map)
            if OK:
                logger.info("Loaded embed map from %s", args.embed_map)
            else:
                logger.warning("Failed to load embed map from %s; using defaults.", args.embed_map)
        except Exception:
            logger.exception("Error loading embed map from %s", args.embed_map)

    # Normalize forwarded args: strip leading "--" if present
    if forwarded and forwarded[0] == "--":
        forwarded = forwarded[1:]

    # If --search present and not asked to also export-and-search, run search and exit
    if args.search and not args.export_and_search:
        rc = run_search_chats(forwarded)
        sys.exit(rc)

    # If --export-and-search present: run exporter then run search if exporter succeeded
    if args.export_and_search:
        try:
            res = perform_export(base=args.base,
                                 api_key=args.api_key,
                                 api_header=args.api_header,
                                 jsonl_out=args.jsonl,
                                 human_out=args.human,
                                 db_file=args.db,
                                 ca_bundle=args.ca_bundle,
                                 insecure=args.insecure,
                                 verbose=args.verbose,
                                 all_embeds=args.all_embeds,
                                 use_db=not args.no_db,
                                 write_raw=bool(args.raw and args.raw.strip()),
                                 raw_out=args.raw,
                                 translate=args.translate)
            # consider export success when perform_export did not raise and returned a dict
            if isinstance(res, dict):
                logger.info("Export succeeded, now running search_chats.py")
                rc = run_search_chats(forwarded)
                sys.exit(rc)
            else:
                logger.error("Export did not return expected result; skipping search.")
                sys.exit(1)
        except Exception:
            logger.exception("Export failed; not running search_chats.py")
            sys.exit(1)

    # Otherwise run exporter only (existing behavior)
    res = perform_export(base=args.base,
                         api_key=args.api_key,
                         api_header=args.api_header,
                         jsonl_out=args.jsonl,
                         human_out=args.human,
                         db_file=args.db,
                         ca_bundle=args.ca_bundle,
                         insecure=args.insecure,
                         verbose=args.verbose,
                         all_embeds=args.all_embeds,
                         use_db=not args.no_db,
                         write_raw=bool(args.raw and args.raw.strip()),
                         raw_out=args.raw,
                         translate=args.translate)
    print("Export result:", json.dumps(res, indent=2))
