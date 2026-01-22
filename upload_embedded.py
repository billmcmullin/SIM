# sim/upload_embedded.py
import os
import json
import time
import logging
import mimetypes
from typing import Optional, List, Dict, Tuple
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")


def post_file_to_anythingllm(
    base_url: str,
    api_key: Optional[str],
    file_path: str,
    folder: Optional[str] = None,
    add_to_workspaces: Optional[str] = None,
    metadata: Optional[Dict] = None,
    timeout: int = 30,
    max_retries: int = 3,
    backoff: float = 1.0,
    verify: bool = True,
) -> requests.Response:
    endpoint = "/v1/document/upload"
    if folder:
        endpoint = f"/v1/document/upload/{folder}"

    url = base_url.rstrip("/") + endpoint
    headers = {}
    if api_key:
        headers["Authorization"] = f"Bearer {api_key}"

    filename = os.path.basename(file_path)
    content_type, _ = mimetypes.guess_type(filename)
    if content_type is None:
        content_type = "application/octet-stream"

    data = {}
    if add_to_workspaces:
        data["addToWorkspaces"] = add_to_workspaces
    if metadata:
        data["metadata"] = json.dumps(metadata)

    last_exc = None
    for attempt in range(1, max_retries + 1):
        try:
            with open(file_path, "rb") as f:
                files = {"file": (filename, f, content_type)}
                logger.info("POST %s (file=%s) attempt %d", url, filename, attempt)
                r = requests.post(url, headers=headers, files=files, data=data, timeout=timeout, verify=verify)
            if 200 <= r.status_code < 300:
                return r
            if 400 <= r.status_code < 500:
                logger.error("Permanent error uploading %s: %s %s", filename, r.status_code, r.text)
                return r
            logger.warning("Transient error uploading %s: %s %s (attempt %d)", filename, r.status_code, r.text, attempt)
        except requests.RequestException as e:
            last_exc = e
            logger.warning("Request failed uploading %s (attempt %d): %s", filename, attempt, e)
        time.sleep(backoff * attempt)
    raise RuntimeError(f"Failed to upload {file_path} after {max_retries} attempts; last error: {last_exc}")


def post_raw_text_to_anythingllm(
    base_url: str,
    api_key: Optional[str],
    text_content: str,
    add_to_workspaces: Optional[str] = None,
    metadata: Optional[Dict] = None,
    timeout: int = 30,
    max_retries: int = 3,
    backoff: float = 1.0,
    verify: bool = True,
) -> requests.Response:
    url = base_url.rstrip("/") + "/v1/document/raw-text"
    headers = {"Content-Type": "application/json"}
    if api_key:
        headers["Authorization"] = f"Bearer {api_key}"

    payload = {"textContent": text_content}
    if add_to_workspaces:
        payload["addToWorkspaces"] = add_to_workspaces
    if metadata:
        payload["metadata"] = metadata

    last_exc = None
    for attempt in range(1, max_retries + 1):
        try:
            logger.info("POST %s (raw-text) attempt %d", url, attempt)
            r = requests.post(url, headers=headers, json=payload, timeout=timeout, verify=verify)
            if 200 <= r.status_code < 300:
                return r
            if 400 <= r.status_code < 500:
                logger.error("Permanent error posting raw text: %s %s", r.status_code, r.text)
                return r
            logger.warning("Transient error posting raw text: %s %s (attempt %d)", r.status_code, r.text, attempt)
        except requests.RequestException as e:
            last_exc = e
            logger.warning("Request failed posting raw text (attempt %d): %s", attempt, e)
        time.sleep(backoff * attempt)
    raise RuntimeError(f"Failed to post raw text after {max_retries} attempts; last error: {last_exc}")


def parse_uploaded_document_locations(resp: requests.Response) -> List[str]:
    """
    Parse a document upload response and return a list of 'location' (preferred) or 'name' values.
    If response is not JSON, returns empty list (and logs raw text).
    """
    try:
        j = resp.json()
    except Exception:
        logger.warning("Upload response not JSON: %s", resp.text[:1000])
        return []

    docs = j.get("documents") or j.get("document") or []
    locations: List[str] = []
    if isinstance(docs, dict):
        docs = [docs]
    for d in docs:
        if not isinstance(d, dict):
            continue
        loc = d.get("location") or d.get("name")
        if loc:
            locations.append(loc)
    return locations


def update_workspace_embeddings(
    base_url: str,
    api_key: Optional[str],
    workspace_slug: str,
    adds: List[str],
    deletes: Optional[List[str]] = None,
    timeout: int = 30,
    max_retries: int = 3,
    backoff: float = 1.0,
    verify: bool = True,
) -> bool:
    """
    POST to /v1/workspace/{slug}/update-embeddings with payload {"adds": [...], "deletes": [...]}
    On 400, retry with basename-only names.
    """
    if deletes is None:
        deletes = []

    url = base_url.rstrip("/") + f"/v1/workspace/{workspace_slug}/update-embeddings"
    headers = {"Content-Type": "application/json"}
    if api_key:
        headers["Authorization"] = f"Bearer {api_key}"

    payload = {"adds": adds, "deletes": deletes}

    last_exc = None
    for attempt in range(1, max_retries + 1):
        try:
            logger.info("POST %s (update-embeddings) attempt %d; adds=%d deletes=%d", url, attempt, len(adds), len(deletes))
            r = requests.post(url, headers=headers, json=payload, timeout=timeout, verify=verify)
            if 200 <= r.status_code < 300:
                logger.info("Workspace %s update-embeddings OK (status %s)", workspace_slug, r.status_code)
                return True
            if 400 <= r.status_code < 500:
                # Log server body for debugging and attempt basename fallback if 400
                logger.error("Permanent error updating workspace %s: %s %s", workspace_slug, r.status_code, r.text[:1000])
                if r.status_code == 400 and any("/" in a for a in adds):
                    # try basename-only
                    alt_adds = [os.path.basename(a) for a in adds]
                    logger.info("Retrying update-embeddings with basename-only (first 5): %s", alt_adds[:5])
                    payload2 = {"adds": alt_adds, "deletes": deletes}
                    r2 = requests.post(url, headers=headers, json=payload2, timeout=timeout, verify=verify)
                    if 200 <= r2.status_code < 300:
                        logger.info("Workspace %s update-embeddings OK with basename (status %s)", workspace_slug, r2.status_code)
                        return True
                    logger.error("Fallback update-embeddings failed: %s %s", r2.status_code, r2.text[:1000])
                return False
            logger.warning("Transient error updating workspace %s: %s %s (attempt %d)", workspace_slug, r.status_code, r.text, attempt)
        except requests.RequestException as e:
            last_exc = e
            logger.warning("Request failed updating workspace %s (attempt %d): %s", workspace_slug, attempt, e)
        time.sleep(backoff * attempt)
    logger.error("Failed to update workspace %s after %d attempts, last error: %s", workspace_slug, max_retries, last_exc)
    return False


def upload_file(
    path: str,
    base_url: str,
    api_key: Optional[str],
    folder: Optional[str] = None,
    add_to_workspaces: Optional[str] = None,
    metadata: Optional[Dict] = None,
    verify: bool = True,
) -> Tuple[bool, List[str]]:
    """
    Upload a single file (or raw text for .txt/.md).
    Returns (success, list_of_uploaded_document_locations).
    """
    ext = os.path.splitext(path)[1].lower()

    # Ensure required metadata (title) exists; default to filename
    if metadata is None:
        metadata = {}
    if "title" not in metadata or not metadata.get("title"):
        metadata["title"] = os.path.basename(path)

    try:
        if ext in {".txt", ".md"}:
            with open(path, "r", encoding="utf-8", errors="ignore") as fh:
                text = fh.read()
            resp = post_raw_text_to_anythingllm(base_url, api_key, text, add_to_workspaces=add_to_workspaces, metadata=metadata, verify=verify)
        else:
            resp = post_file_to_anythingllm(base_url, api_key, path, folder=folder, add_to_workspaces=add_to_workspaces, metadata=metadata, verify=verify)

        if 200 <= resp.status_code < 300:
            locations = parse_uploaded_document_locations(resp)
            logger.info("Upload succeeded for %s -> %s (returned %d document locations)", path, base_url, len(locations))
            return True, locations
        else:
            logger.error("Upload failed for %s: %s %s", path, resp.status_code, resp.text)
            return False, []
    except Exception as e:
        logger.exception("Exception uploading %s: %s", path, e)
        return False, []


def upload_folder_or_file(
    source: str,
    base_url: str,
    api_key: Optional[str],
    folder: Optional[str] = None,
    add_to_workspaces: Optional[str] = None,
    metadata_for_all: Optional[Dict] = None,
    concurrency: int = 4,
    allowed_ext: Optional[List[str]] = None,
    auto_update_workspaces: bool = False,
    verify: bool = True,
) -> List[str]:
    """
    Upload either a single file (if source is a file) or all matching files in a directory.
    Supports .jsonl as a file type.
    Returns list of uploaded document locations (aggregated).
    """
    if allowed_ext is None:
        allowed_ext = [".json", ".jsonl", ".csv", ".txt", ".md", ".pdf", ".docx", ".odt"]

    paths: List[str] = []
    if os.path.isfile(source):
        paths = [source]
    elif os.path.isdir(source):
        paths = [
            os.path.join(source, f)
            for f in os.listdir(source)
            if os.path.isfile(os.path.join(source, f)) and os.path.splitext(f)[1].lower() in allowed_ext
        ]
    else:
        logger.error("Source is not a file or directory: %s", source)
        return []

    if not paths:
        logger.info("No files to upload from source: %s", source)
        return []

    logger.info("Found %d files to upload in %s", len(paths), source)
    uploaded_locations: List[str] = []
    successes = []

    with ThreadPoolExecutor(max_workers=concurrency) as ex:
        futures = {
            ex.submit(upload_file, path, base_url, api_key, folder, add_to_workspaces, metadata_for_all, verify): path
            for path in paths
        }
        for fut in as_completed(futures):
            path = futures[fut]
            try:
                ok, locations = fut.result()
                if ok:
                    successes.append(path)
                    uploaded_locations.extend(locations)
            except Exception as e:
                logger.exception("Unhandled exception uploading %s: %s", path, e)

    logger.info("Upload finished: %d succeeded, %d failed", len(successes), len(paths) - len(successes))

    # Optionally update workspace embeddings
    if auto_update_workspaces and add_to_workspaces:
        workspace_slugs = [s.strip() for s in add_to_workspaces.split(",") if s.strip()]
        for slug in workspace_slugs:
            if uploaded_locations:
                ok = update_workspace_embeddings(base_url, api_key, slug, adds=uploaded_locations, deletes=[], verify=verify)
                if not ok:
                    logger.error("update-embeddings failed for workspace %s", slug)
            else:
                logger.info("No uploaded document locations to add to workspace %s", slug)
    return uploaded_locations


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Upload files to AnythingLLM workspace via its API and optionally update workspace embeddings")
    parser.add_argument("--source", "-s", required=True, help="File or directory with files to upload")
    parser.add_argument("--base-url", "-b", default=os.getenv("BASE_URL"), help="Base URL of AnythingLLM (e.g. https://your-anythingllm.com/api)")
    parser.add_argument("--api-key", "-k", default=os.getenv("API_KEY"), help="API key (Bearer) for AnythingLLM")
    parser.add_argument("--folder", "-f", default=os.getenv("UPLOAD_FOLDER"), help="Optional remote folder name to upload into (e.g. custom-documents)")
    parser.add_argument("--workspaces", "-w", default=os.getenv("WORKSPACES"), help="Comma-separated workspace slugs to add the document to after upload (e.g. ws1,ws2)")
    parser.add_argument("--metadata", "-m", default=os.getenv("UPLOAD_METADATA"), help="Optional metadata JSON string or path to JSON file to attach to every uploaded document")
    parser.add_argument("--concurrency", "-c", type=int, default=4)
    parser.add_argument("--auto-update", action="store_true", help="After upload, automatically call update-embeddings on the provided workspaces (requires --workspaces)")
    parser.add_argument("--insecure", action="store_true", help="Disable TLS certificate verification (dev only)")
    args = parser.parse_args()

    if not args.base_url:
        parser.error("BASE_URL is required (--base-url or BASE_URL env)")

    # parse metadata if provided (file path or JSON string)
    metadata_obj = None
    if args.metadata:
        if os.path.exists(args.metadata):
            try:
                with open(args.metadata, "r", encoding="utf-8") as fh:
                    metadata_obj = json.load(fh)
            except Exception as e:
                parser.error(f"Failed to load metadata file {args.metadata}: {e}")
        else:
            try:
                metadata_obj = json.loads(args.metadata)
            except Exception as e:
                parser.error(f"Failed to parse metadata JSON: {e}")

    verify_flag = not args.insecure

    uploaded = upload_folder_or_file(
        args.source,
        args.base_url,
        args.api_key,
        folder=args.folder,
        add_to_workspaces=args.workspaces,
        metadata_for_all=metadata_obj,
        concurrency=args.concurrency,
        auto_update_workspaces=args.auto_update,
        verify=verify_flag,
    )

    logger.info("Total uploaded document locations: %d", len(uploaded))
