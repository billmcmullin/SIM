#!/usr/bin/env python3
"""
translate.py

Provides:
- translate_text(text, target='en') -> (translated_text, src_lang)   # always synchronous
- translator_available() -> bool

Supports both synchronous and asynchronous translator implementations (e.g. newer googletrans).
If an async coroutine is returned, this module runs it in a fresh event loop on a separate thread.
"""
from typing import Tuple
import logging
import asyncio
import inspect
import threading

_log = logging.getLogger(__name__)

# Try to import googletrans (community package). This may be sync or async depending on version.
try:
    from googletrans import Translator as _GT_Translator  # type: ignore
    try:
        _GT = _GT_Translator()
    except Exception:
        _GT = None
except Exception:
    _GT = None


def translator_available() -> bool:
    """Return True if a translator instance is present (googletrans)"""
    return _GT is not None


def _short_target(target: str) -> str:
    if not target:
        return "en"
    return target.split("-")[0].lower()


def _sync_run_coroutine(coro):
    """
    Run the awaitable 'coro' to completion in a fresh event loop on a new thread,
    and return the result. Raises any exception that occurred inside.
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


def translate_text(text: str, target: str = "en") -> Tuple[str, str]:
    """
    Translate text to target language (synchronous interface).

    Returns:
      (translated_text, src_lang)  -- src_lang may be empty string if unknown.

    Behavior:
    - If googletrans is available, use it. Handles both sync and async variants by detecting awaitables.
    - On error or if no translator available, returns (original_text, "").
    """
    if not text:
        return "", ""

    tgt = _short_target(target)

    if _GT is None:
        _log.debug("translate_text: translator not available, returning original text")
        return text, ""

    try:
        maybe = _GT.translate(text, dest=tgt)
        if inspect.isawaitable(maybe):
            try:
                res = _sync_run_coroutine(maybe)
            except Exception:
                _log.debug("translate_text: coroutine failed", exc_info=True)
                return text, ""
        else:
            res = maybe

        translated_text = ""
        src_lang = ""

        if res is None:
            return text, ""

        if hasattr(res, "text"):
            translated_text = getattr(res, "text") or ""
        elif isinstance(res, dict):
            translated_text = res.get("text", "") or ""

        if hasattr(res, "src"):
            src_lang = getattr(res, "src") or ""
        elif isinstance(res, dict):
            src_lang = res.get("src", "") or ""

        if not translated_text:
            try:
                if isinstance(res, str):
                    translated_text = res
            except Exception:
                pass

        if not translated_text:
            translated_text = text

        return translated_text, src_lang or ""

    except Exception as e:
        _log.exception("translate_text: unexpected error during translation: %s", e)
        return text, ""
