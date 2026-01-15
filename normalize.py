# normalize.py
from typing import Dict, Any, Optional
from datetime import datetime, timezone
from config import EMBED_NAME_MAP, now_iso

def _extract_session_id(chat: Dict[str,Any]) -> Optional[str]:
    for k in ("session_id","sessionId","sessionuuid","session_uuid","sessionUuid","session","sessionid"):
        if k in chat and chat[k]:
            return str(chat[k])
    for v in chat.values():
        if isinstance(v, dict):
            for k in ("session_id","sessionId","sessionUuid"):
                if k in v and v[k]:
                    return str(v[k])
    return None

def _to_iso_utc(val: Any) -> str:
    """
    Convert common timestamp formats to ISO8601 UTC string.
    Supports:
      - datetime objects
      - integer/float epoch seconds or milliseconds
      - ISO-like strings (with optional trailing 'Z')
      - numeric strings (epoch)
    Falls back to now_iso() on failure.
    """
    if val is None or (isinstance(val, str) and val.strip() == ""):
        return now_iso()
    # datetime already
    if isinstance(val, datetime):
        try:
            return val.astimezone(timezone.utc).isoformat()
        except Exception:
            return now_iso()
    # numeric epoch (seconds or milliseconds)
    if isinstance(val, (int, float)):
        try:
            ts = float(val)
            # if it's clearly milliseconds (>= 1e12) convert to seconds
            if ts > 1e12:
                ts = ts / 1000.0
            return datetime.fromtimestamp(ts, tz=timezone.utc).isoformat()
        except Exception:
            return now_iso()
    # string parsing
    if isinstance(val, str):
        s = val.strip()
        # handle ISO with trailing Z -> replace with +00:00
        try:
            if s.endswith("Z"):
                s2 = s[:-1] + "+00:00"
                return datetime.fromisoformat(s2).astimezone(timezone.utc).isoformat()
            # try direct iso parse
            return datetime.fromisoformat(s).astimezone(timezone.utc).isoformat()
        except Exception:
            # try numeric string epoch
            try:
                num = float(s)
                if num > 1e12:
                    num = num / 1000.0
                return datetime.fromtimestamp(num, tz=timezone.utc).isoformat()
            except Exception:
                # last resort: try common separators (replace space with T)
                try:
                    s3 = s.replace(" ", "T")
                    if s3.endswith("Z"):
                        s3 = s3[:-1] + "+00:00"
                    return datetime.fromisoformat(s3).astimezone(timezone.utc).isoformat()
                except Exception:
                    return now_iso()
    return now_iso()

def normalize_embed_chat(embed: Dict[str,Any], chat: Dict[str,Any], idx:int=0) -> Dict[str,Any]:
    embed_uuid_raw = embed.get("uuid") or embed.get("id") or embed.get("embed_uuid") or embed.get("embedId")
    embed_uuid = str(embed_uuid_raw) if embed_uuid_raw is not None else ""
    embed_name = EMBED_NAME_MAP.get(embed_uuid.lower()) if embed_uuid else None
    if not embed_name:
        embed_name = embed.get("workspace",{}).get("name") or embed.get("workspace_name") or embed.get("name") or embed_uuid

    raw_id = chat.get("id") or chat.get("session_id") or chat.get("uuid") or chat.get("chat_id") or f"{embed_uuid}-{idx}"
    chat_id = f"{embed_uuid}:{raw_id}"

    prompt = chat.get("prompt") or chat.get("message") or chat.get("user_message") or chat.get("content") or ""
    text = chat.get("response") or chat.get("textResponse") or chat.get("answer") or chat.get("assistant") or ""
    created = chat.get("createdAt") or chat.get("created_at") or chat.get("sentAt") or chat.get("timestamp") or ""

    # normalize created/prompt date to ISO8601 UTC
    try:
        created_iso = _to_iso_utc(created)
    except Exception:
        created_iso = now_iso()

    sources = chat.get("sources") or chat.get("source") or []
    page_url = None
    if isinstance(sources, list) and len(sources) > 0:
        s0 = sources[0]
        if isinstance(s0, dict):
            page_url = s0.get("url") or s0.get("sourceDocument") or page_url

    session_id = _extract_session_id(chat)

    return {
        "chat_id": str(chat_id),
        "prompt": prompt,
        "text": text,
        "translated_text": "",
        "language": "",  # language detection can be applied by writers or later pipeline
        "created_at": created_iso,
        "prompt_date": created_iso,
        "session_id": session_id,
        "source": {"embed_uuid": embed_uuid, "embed_name": embed_name, "page_url": page_url},
        "connection": embed,
        "raw_chat": chat
    }
