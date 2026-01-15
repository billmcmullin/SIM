#!/usr/bin/env python3
"""
Robust shim for the moved search/search_chats.py.

This loads the implementation by file path (so search/ does NOT need to be
a package with __init__.py). It preserves CLI behavior: running
`python search_chats.py ...` will execute search/search_chats.py.main().
"""
from pathlib import Path
import sys
import traceback
import importlib.util

def _run():
    # Determine script directory (project root) and the expected relocated module path
    script_dir = Path(__file__).resolve().parent
    target = script_dir / "search" / "search_chats.py"

    if not target.exists():
        print(f"Cannot find implementation at {target!s}. Ensure you moved search_chats.py into search/search_chats.py", file=sys.stderr)
        raise SystemExit(2)

    try:
        spec = importlib.util.spec_from_file_location("search_search_chats_impl", str(target))
        if spec is None or spec.loader is None:
            raise ImportError(f"Could not create import spec for {target}")
        module = importlib.util.module_from_spec(spec)
        # Execute the module in its own namespace
        spec.loader.exec_module(module)
    except Exception:
        print(f"Failed to load module from {target!s}", file=sys.stderr)
        traceback.print_exc()
        raise

    # Expect the moved module to expose a main() function
    if not hasattr(module, "main"):
        raise RuntimeError(f"Module {target!s} does not expose a main() function")
    # Forward execution
    module.main()

if __name__ == "__main__":
    _run()
