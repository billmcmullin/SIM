# db.py
import sqlite3
import json
import traceback
from typing import Dict, Any, Optional
from pathlib import Path

from config import DEFAULT_DB, DEFAULT_VERBOSE

VERBOSE = DEFAULT_VERBOSE


def get_conn(db_path: Optional[str] = None) -> sqlite3.Connection:
    p = db_path or DEFAULT_DB
    Path(p).parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(p, check_same_thread=False)
    conn.execute("PRAGMA journal_mode=WAL;")
    return conn


def init_db(db_path: Optional[str] = None):
    conn = get_conn(db_path)
    conn.execute(
        """
    CREATE TABLE IF NOT EXISTS chats (
      chat_id TEXT PRIMARY KEY,
      prompt TEXT,
      translated_prompt TEXT,
      text TEXT,
      translated_text TEXT,
      language TEXT,
      created_at TEXT,
      prompt_date TEXT,
      translated_from TEXT,
      translated_to TEXT,
      session_id TEXT,
      source_json TEXT,
      connection_info TEXT
    )"""
    )
    try:
        # include translated_prompt in FTS if available
        conn.execute(
            "CREATE VIRTUAL TABLE IF NOT EXISTS chats_fts USING fts5(chat_id, prompt, translated_prompt, text, translated_text)"
        )
    except Exception:
        if VERBOSE:
            print("DEBUG: FTS5 not available; skipping full-text index.")
    conn.commit()
    conn.close()


def ensure_column(conn: sqlite3.Connection, table: str, column: str, coltype: str = "TEXT"):
    try:
        cur = conn.execute(f"PRAGMA table_info({table});")
        cols = [r[1] for r in cur.fetchall()]
        if column not in cols:
            if VERBOSE:
                print(f"DEBUG: adding missing column '{column}' to table '{table}'")
            conn.execute(f"ALTER TABLE {table} ADD COLUMN {column} {coltype};")
            conn.commit()
    except Exception as e:
        if VERBOSE:
            print("DEBUG: ensure_column error:", e)


def upsert_chat(doc: Dict[str, Any], db_path: Optional[str] = None):
    conn = None
    try:
        conn = get_conn(db_path)
        # Ensure runtime columns exist (safe to call every upsert)
        try:
            ensure_column(conn, "chats", "session_id", "TEXT")
            ensure_column(conn, "chats", "prompt_date", "TEXT")
            ensure_column(conn, "chats", "translated_from", "TEXT")
            ensure_column(conn, "chats", "translated_to", "TEXT")
            ensure_column(conn, "chats", "translated_prompt", "TEXT")
        except Exception:
            if VERBOSE:
                print("DEBUG: ensure_column failed (continuing)")

        chat_id = str(doc.get("chat_id", "<no-id>"))
        prompt = doc.get("prompt", "")  # original prompt (left unmodified in writer)
        translated_prompt = doc.get("translated_prompt", "")  # new field
        text = doc.get("text", "")
        translated_text = doc.get("translated_text", "")
        language = doc.get("language", "")
        created_at = doc.get("created_at", "")
        prompt_date = doc.get("prompt_date", "")
        translated_from = doc.get("translated_from", "")
        translated_to = doc.get("translated_to", "")
        session_id = (
            doc.get("session_id", "")
            or (doc.get("source") or {}).get("session_id", "")
            or ""
        )

        try:
            source_json = json.dumps(doc.get("source", {}), ensure_ascii=False, default=str)
        except Exception:
            source_json = json.dumps(
                {"_error": "source serialization failed", "repr": repr(doc.get("source", {}))}
            )

        try:
            connection_info = json.dumps(
                doc.get("connection", {}), ensure_ascii=False, default=str
            )
        except Exception:
            connection_info = json.dumps(
                {"_error": "connection serialization failed", "repr": repr(doc.get("connection", {}))}
            )

        conn.execute(
            """
          INSERT INTO chats(chat_id, prompt, translated_prompt, text, translated_text, language, created_at, prompt_date, translated_from, translated_to, session_id, source_json, connection_info)
          VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
          ON CONFLICT(chat_id) DO UPDATE SET
            prompt=excluded.prompt,
            translated_prompt=excluded.translated_prompt,
            text=excluded.text,
            translated_text=excluded.translated_text,
            language=excluded.language,
            created_at=excluded.created_at,
            prompt_date=excluded.prompt_date,
            translated_from=excluded.translated_from,
            translated_to=excluded.translated_to,
            session_id=excluded.session_id,
            source_json=excluded.source_json,
            connection_info=excluded.connection_info
        """,
            (
                chat_id,
                prompt,
                translated_prompt,
                text,
                translated_text,
                language,
                created_at,
                prompt_date,
                translated_from,
                translated_to,
                session_id,
                source_json,
                connection_info,
            ),
        )
        try:
            # Insert into FTS table for searching; ignore errors if table not available
            conn.execute(
                "INSERT INTO chats_fts(chat_id, prompt, translated_prompt, text, translated_text) VALUES(?,?,?,?,?)",
                (chat_id, prompt, translated_prompt, text, translated_text),
            )
        except Exception:
            if VERBOSE:
                print("DEBUG: chats_fts insert failed (ignored)")
        conn.commit()
    except Exception as e:
        print("ERROR: upsert_chat failed for", doc.get("chat_id"), ":", e)
        if VERBOSE:
            traceback.print_exc()
            try:
                preview = {
                    "chat_id": doc.get("chat_id"),
                    "prompt": (doc.get("prompt", "")[:200] + "...")
                    if isinstance(doc.get("prompt", ""), str)
                    and len(doc.get("prompt", "")) > 200
                    else doc.get("prompt", ""),
                    "translated_prompt": doc.get("translated_prompt"),
                    "created_at": doc.get("created_at"),
                    "prompt_date": doc.get("prompt_date"),
                    "translated_from": doc.get("translated_from"),
                    "translated_to": doc.get("translated_to"),
                    "session_id": doc.get("session_id"),
                }
                print("ERROR: doc preview:", json.dumps(preview, ensure_ascii=False, default=str))
            except Exception:
                print("ERROR: could not produce doc preview")
    finally:
        if conn:
            try:
                conn.close()
            except Exception:
                pass
