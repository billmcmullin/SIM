# search/__init__.py
"""
search package

This package contains the moved search_chats module.
The shim at the project root imports search.search_chats and calls main().
Expose main() here so callers can do: from search import main
"""

__all__ = ["main", "__version__"]

__version__ = "0.1.0"

try:
    from .search_chats import main  # type: ignore
except Exception as e:  # pragma: no cover - fall back if import fails
    def main(*args, **kwargs):
        raise RuntimeError(
            "search.search_chats could not be imported. Ensure search/search_chats.py exists and is importable."
        ) from e