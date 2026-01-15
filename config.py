# config.py
"""
Configuration for the exporter.

EMBED_NAME_MAP may be loaded from a file. Supported formats:
- JSON: a top-level object mapping embed-uuid (or alias) -> display name
    Example embed_map.json:
    {
      "0f134195-08a0-451b-b6fb-62fd9547df95": "Portal",
      "bbf4213d-3f6c-43f4-8d63-af7b46817ae8": "Forum"
    }

- CSV: two columns (uuid,name) per line, optionally with a header.
    Example embed_map.csv:
    0f134195-08a0-451b-b6fb-62fd9547df95,Portal
    bbf4213d-3f6c-43f4-8d63-af7b46817ae8,Forum

Set the environment variable HEAVYARMS_EMBED_MAP_FILE to point to your file path.
If the file is not found or is invalid the builtin defaults are used.
"""
from datetime import datetime, timezone
import os
import json
import csv
from pathlib import Path
from typing import Dict, Optional
import warnings

# Helper to interpret environment truthy values
def _env_bool(var_name: str, default: str = "0") -> bool:
    """
    Return True if the environment variable var_name is set to a truthy value.
    Truthy values: "1", "true", "yes" (case-insensitive).
    Everything else is False.
    """
    return os.getenv(var_name, default).strip().lower() in ("1", "true", "yes")


# Defaults (override with env or CLI args)
DEFAULT_BASE = os.getenv("HEAVYARMS_BASE_URL", "").rstrip("/")
DEFAULT_API_KEY = os.getenv("HEAVYARMS_API_KEY", "")
DEFAULT_DB = os.getenv("CHAT_INDEX_DB", "heavyarms_chats.db")
DEFAULT_JSONL = os.getenv("HEAVYARMS_EXPORT_FILE", "heavyarms_chats_export.jsonl")
DEFAULT_HUMAN = os.getenv("HEAVYARMS_EXPORT_HUMAN", Path(DEFAULT_JSONL).with_suffix("").name + "_human.txt")
DEFAULT_API_HEADER = os.getenv("HEAVYARMS_API_HEADER", "Authorization").strip()

# SSL behavior:
# DEFAULT_INSECURE = False by default (i.e., verify SSL). Set HEAVYARMS_INSECURE=1 or "true"/"yes"
# to disable verification (not recommended in production).
DEFAULT_INSECURE = _env_bool("HEAVYARMS_INSECURE", "0")
DEFAULT_CA_BUNDLE = os.getenv("HEAVYARMS_CA_BUNDLE", "").strip()

# Verbose flag (defaults to True unless explicitly disabled via env)
DEFAULT_VERBOSE = _env_bool("HEAVYARMS_VERBOSE", "1")

# Translation target default
TRANSLATION_TARGET = "en-US"  # default translation target; change if you want e.g. "en"


# Built-in fallback embed map (lowercase keys)
_EMBED_NAME_MAP_FALLBACK: Dict[str, str] = {

}

# File to load (override with env var HEAVYARMS_EMBED_MAP_FILE)
_EMBED_MAP_FILE = os.getenv("HEAVYARMS_EMBED_MAP_FILE", "").strip()


def _load_embed_map_from_file(path: Path) -> Optional[Dict[str, str]]:
    """
    Attempt to load embed map from the given Path.
    Supports JSON (object/dict) and CSV (two columns: key,name).
    Returns a dict with lowercased keys -> display names, or None on failure.
    """
    if not path.exists():
        return None
    try:
        suffix = path.suffix.lower()
        if suffix == ".json":
            text = path.read_text(encoding="utf-8")
            data = json.loads(text)
            if not isinstance(data, dict):
                warnings.warn(f"Embed map JSON {path} does not contain an object at top level; ignoring.")
                return None
            return {str(k).lower(): str(v) for k, v in data.items() if k is not None}
        elif suffix in (".csv", ".tsv", ""):
            # CSV (default) - attempt to read two columns per row
            with path.open("r", encoding="utf-8", newline="") as fh:
                reader = csv.reader(fh)
                out: Dict[str, str] = {}
                for row in reader:
                    if not row:
                        continue
                    # skip header row if it looks like one (common headers)
                    if len(row) >= 2 and row[0].strip().lower() in ("uuid", "embed_id", "embed_uuid", "id") and row[1].strip().lower() in ("name", "embed_name"):
                        continue
                    key = row[0].strip()
                    name = row[1].strip() if len(row) > 1 else ""
                    if key:
                        out[key.lower()] = name
                if out:
                    return out
                return None
        else:
            warnings.warn(f"Embed map file {path} has unsupported extension '{suffix}'; supported: .json, .csv")
            return None
    except Exception as exc:
        warnings.warn(f"Failed to load embed map from {path}: {exc}")
        return None

def set_embed_map_file(path: str) -> bool:
    """
    Load embed map from 'path' (JSON or CSV) and set EMBED_NAME_MAP.
    Returns True on success, False otherwise.
    """
    global EMBED_NAME_MAP
    if not path:
        return False
    try:
        p = Path(path)
        loaded = _load_embed_map_from_file(p)
        if loaded:
            EMBED_NAME_MAP = loaded
            return True
        else:
            warnings.warn(f"set_embed_map_file: could not load embed map from {path}")
            return False
    except Exception as e:
        warnings.warn(f"set_embed_map_file: exception loading {path}: {e}")
        return False


# Build EMBED_NAME_MAP by attempting to load from file, falling back to hardcoded map.
if _EMBED_MAP_FILE:
    try:
        _p = Path(_EMBED_MAP_FILE)
        _loaded = _load_embed_map_from_file(_p)
        if _loaded:
            EMBED_NAME_MAP: Dict[str, str] = _loaded
        else:
            warnings.warn(f"Could not load embed map from '{_EMBED_MAP_FILE}'; using built-in defaults.")
            EMBED_NAME_MAP = {k.lower(): v for k, v in _EMBED_NAME_MAP_FALLBACK.items()}
    except Exception:
        warnings.warn("Error while initializing EMBED_NAME_MAP; using built-in defaults.")
        EMBED_NAME_MAP = {k.lower(): v for k, v in _EMBED_NAME_MAP_FALLBACK.items()}
else:
    # no file specified; use built-in defaults
    EMBED_NAME_MAP = {k.lower(): v for k, v in _EMBED_NAME_MAP_FALLBACK.items()}


def now_iso():
    return datetime.now(timezone.utc).isoformat()
