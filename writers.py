#!/usr/bin/env python3
import json
import re
import logging
import asyncio
import inspect
import threading
from typing import List, Dict, Any, Tuple
from pathlib import Path
from config import now_iso, TRANSLATION_TARGET
from translate import translate_text, translator_available

_log = logging.getLogger(__name__)

# Module-level flag to control whether translations are enabled.
# This is set by main.perform_export(translate=...) so translations are opt-in.
TRANSLATION_ENABLED = False

# Try to import googletrans/Translator for detection; fall back to langdetect if available
try:
    from googletrans import Translator as _GT_Translator  # type: ignore
    _GT = _GT_Translator()
except Exception:
    _GT = None

try:
    from langdetect import detect as _ld_detect  # type: ignore
except Exception:
    _ld_detect = None


def _sync_run_coroutine(coro):
    """
    Run the awaitable 'coro' to completion in a fresh event loop on a new thread,
    and return the result. Raises any exception that occurred inside.
    This avoids using asyncio.run() in the current thread and prevents conflicts
    with existing event loops (useful on Windows with proactor).
    """
    if not inspect.isawaitable(coro):
        return coro

    result = {}
    exc = {}

    def _target():
        loop = None
        try:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            result['value'] = loop.run_until_complete(coro)
            # try to shutdown async generators cleanly
            try:
                loop.run_until_complete(loop.shutdown_asyncgens())
            except Exception:
                pass
        except Exception as e:
            exc['error'] = e
        finally:
            try:
                if loop is not None:
                    loop.close()
            except Exception:
                pass

    t = threading.Thread(target=_target, daemon=True)
    t.start()
    t.join()

    if 'error' in exc:
        raise exc['error']
    return result.get('value')


def detect_language(text: str) -> str:
    """
    Detect language of text. Tries googletrans first, then langdetect, otherwise returns 'unknown'.
    Handles both sync and async googletrans implementations by running awaitables in a separate thread loop.
    """
    if not text:
        return "unknown"

    # try googletrans
    try:
        if _GT is not None:
            det = _GT.detect(text)
            if inspect.isawaitable(det):
                try:
                    det = _sync_run_coroutine(det)
                except Exception:
                    _log.debug("googletrans detection coroutine failed", exc_info=True)
                    det = None
            lang = getattr(det, "lang", None) if det is not None else None
            if isinstance(lang, str) and lang:
                return lang
    except Exception:
        _log.debug("googletrans detection failed", exc_info=True)

    # try langdetect
    try:
        if _ld_detect is not None:
            return _ld_detect(text)
    except Exception:
        _log.debug("langdetect detection failed", exc_info=True)

    return "unknown"


# Helpers
def _safe_widget_filename(name: str, maxlen: int = 60) -> str:
    if not name:
        name = "unknown"
    s = re.sub(r"[^\w\-\.]+", "_", str(name))
    if len(s) > maxlen:
        s = s[:maxlen]
    return s


def _short_lang(code: str) -> str:
    if not code:
        return ""
    return code.split("-")[0].lower()


def ensure_translated_prompt_fields(d: Dict[str, Any]) -> str:
    """
    Translate the prompt if needed and set:
      - translated_prompt (new field; empty if no translation performed)
      - translated_from
      - translated_to
    Keeps prompt unchanged (original).
    Returns a status string: "translated", "skipped", or "none".
    """
    original = d.get("prompt", "") or ""
    # If already set, don't re-run translation
    if d.get("translated_prompt"):
        _log.debug("translated_prompt already set for chat_id=%s", d.get("chat_id"))
        return "none"

    target_short = _short_lang(TRANSLATION_TARGET or "en-US")

    # If translation feature is disabled, skip quickly
    if not TRANSLATION_ENABLED:
        d["translated_prompt"] = ""
        d["translated_from"] = ""
        d["translated_to"] = ""
        return "skipped"

    if not original.strip():
        d["translated_prompt"] = ""
        d["translated_from"] = ""
        d["translated_to"] = ""
        _log.debug("No prompt text to translate for chat_id=%s", d.get("chat_id"))
        return "none"

    if not translator_available():
        d["translated_prompt"] = ""
        d["translated_from"] = ""
        d["translated_to"] = ""
        # Only log the first time at INFO; subsequent calls will be silent to avoid spam.
        if not getattr(ensure_translated_prompt_fields, "_unavail_logged", False):
            _log.info("Translator unavailable — skipping prompt translations for this run.")
            ensure_translated_prompt_fields._unavail_logged = True
        return "skipped"

    try:
        maybe = translate_text(original, target_short)
        if inspect.isawaitable(maybe):
            try:
                translated_text, src_lang = _sync_run_coroutine(maybe)
            except Exception:
                _log.debug("translate_text coroutine failed", exc_info=True)
                translated_text, src_lang = original, ""
        else:
            translated_text, src_lang = maybe
    except Exception as e:
        _log.exception("Unexpected error during translate_text for chat_id=%s: %s", d.get("chat_id"), e)
        translated_text, src_lang = original, ""

    # If translator returned a source lang and it's different from target, and translation changed text
    if src_lang and _short_lang(src_lang) != target_short and translated_text and translated_text != original:
        d["translated_prompt"] = translated_text
        d["translated_from"] = src_lang
        d["translated_to"] = TRANSLATION_TARGET or "en-US"
        _log.info(
            "Translated prompt for chat_id=%s from %s to %s (len %d -> %d)",
            d.get("chat_id"),
            src_lang,
            d["translated_to"],
            len(original),
            len(translated_text),
        )
        return "translated"
    else:
        # No translation performed
        d["translated_prompt"] = ""
        d["translated_from"] = ""
        d["translated_to"] = ""
        _log.debug(
            "No translation needed for chat_id=%s (src=%s target=%s).",
            d.get("chat_id"),
            src_lang,
            target_short,
        )
        return "none"


def write_global_jsonl(path: str, docs: List[Dict[str, Any]]):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    translated = 0
    skipped = 0
    none = 0

    with open(path, "w", encoding="utf-8") as fh:
        for d in docs:
            # ensure prompt_date
            if "prompt_date" not in d or not d.get("prompt_date"):
                d["prompt_date"] = d.get("created_at", now_iso())

            if "translated_prompt" not in d:
                d["translated_prompt"] = ""

            status = ensure_translated_prompt_fields(d)
            if status == "translated":
                translated += 1
            elif status == "skipped":
                skipped += 1
            else:
                none += 1

            fh.write(json.dumps(d, ensure_ascii=False) + "\n")

    _log.info("write_global_jsonl: total=%d translated=%d skipped=%d unchanged=%d", len(docs), translated, skipped, none)


def write_human_readable(human_path: str, docs: List[Dict[str, Any]]):
    Path(human_path).parent.mkdir(parents=True, exist_ok=True)
    with open(human_path, "w", encoding="utf-8") as fh:
        fh.write(f"Export generated: {now_iso()}\n")
        fh.write(f"Total items: {len(docs)}\n\n")
        groups: Dict[str, List[Dict[str, Any]]] = {}
        for d in docs:
            widget = d.get("source", {}).get("embed_name") or d.get("source", {}).get("embed_uuid") or "unknown_widget"
            groups.setdefault(widget, []).append(d)
        for widget, items in sorted(groups.items(), key=lambda x: x[0]):
            fh.write("=" * 80 + "\n")
            fh.write(f"Embed / Widget: {widget}  (items: {len(items)})\n")
            fh.write("=" * 80 + "\n\n")
            try:
                items_sorted = sorted(items, key=lambda x: x.get("created_at") or "", reverse=False)
            except Exception:
                items_sorted = items
            for it in items_sorted:
                fh.write(f"Chat ID: {it.get('chat_id')}\n")
                sid = it.get("session_id")
                if sid:
                    fh.write(f"Session ID: {sid}\n")
                prompt_date = it.get("prompt_date") or it.get("created_at") or ""
                if prompt_date:
                    fh.write(f"Prompt date: {prompt_date}\n")
                fh.write(f"Created: {it.get('created_at')}\n")

                # original prompt (prompt is left unmodified)
                prompt = it.get("prompt", "") or ""
                fh.write(f"Prompt: {prompt}\n")

                # translated_prompt if available
                t_prompt = it.get("translated_prompt", "") or ""
                if t_prompt:
                    tf = it.get("translated_from") or it.get("language") or "unknown"
                    tt = it.get("translated_to") or TRANSLATION_TARGET or "en-US"
                    fh.write(f"Prompt (translated from {tf} to {tt}): {t_prompt}\n")

                # Answer / response
                text = it.get("text") or ""
                if text:
                    fh.write("Answer:\n")
                    fh.write(text.strip() + "\n")

                # If translated_text exists for answer, show its metadata (unchanged)
                if it.get("translated_text"):
                    tf = it.get("translated_from") or it.get("language") or "unknown"
                    tt = it.get("translated_to") or TRANSLATION_TARGET or "en-US"
                    fh.write(f"Answer (translated from {tf} to {tt}):\n")
                    fh.write(it.get("translated_text").strip() + "\n")

                src = it.get("source") or {}
                if src:
                    fh.write("\nSource metadata:\n")
                    try:
                        fh.write(json.dumps(src, ensure_ascii=False, indent=2) + "\n")
                    except Exception:
                        fh.write(str(src) + "\n")
                fh.write("-" * 80 + "\n\n")


def export_per_widget(
    base_jsonl_dir: Path,
    base_output_dir: Path,
    global_jsonl_name: str,
    global_human_name: str,
    docs_by_widget: Dict[str, List[Dict[str, Any]]],
) -> Tuple[Dict[str, str], Dict[str, str]]:
    base_jsonl_dir.mkdir(parents=True, exist_ok=True)
    base_output_dir.mkdir(parents=True, exist_ok=True)
    per_jsonl: Dict[str, str] = {}
    per_human: Dict[str, str] = {}
    name_counters: Dict[str, int] = {}
    for widget_display, docs in docs_by_widget.items():
        safe = _safe_widget_filename(widget_display)
        counter = name_counters.get(safe, 0)
        if counter == 0:
            jname = f"{Path(global_jsonl_name).stem}_{safe}.jsonl"
            hname = f"{Path(global_human_name).stem}_{safe}.txt"
        else:
            jname = f"{Path(global_jsonl_name).stem}_{safe}_{counter}.jsonl"
            hname = f"{Path(global_human_name).stem}_{safe}_{counter}.txt"
        name_counters[safe] = counter + 1
        jpath = str(base_jsonl_dir / jname)
        hpath = str(base_output_dir / hname)
        with open(jpath, "w", encoding="utf-8") as jf:
            for d in docs:
                if "prompt_date" not in d or not d.get("prompt_date"):
                    d["prompt_date"] = d.get("created_at", now_iso())
                if "translated_prompt" not in d:
                    d["translated_prompt"] = ""
                ensure_translated_prompt_fields(d)
                jf.write(json.dumps(d, ensure_ascii=False) + "\n")
        write_human_readable(hpath, docs)
        per_jsonl[f"{widget_display}"] = jpath
        per_human[f"{widget_display}"] = hpath
    return per_jsonl, per_human
