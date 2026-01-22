# SIM - Python App for getting Chat Widgets Messages

This is python CLI that will make request to chat widgets and perform the following:

* Retrieve the chats performed from the Embedded Chat widgets
* Determine if they are coming from the Website, Portal, Forum
* Separate each chat widget responses into their own: CSV file, Text file, JSON file
* Able to determine what topic was being asked in the prompt

## How to use

Run the following to perform function all functions:

```
python main.py --export-and-search --embed-map <IDS_JSON_FILE> --api-key <API KEY> --base "<URL>" -- --per-widget --csv csv/output.csv --details-dir csv --terms-file <TERMS TEXT FILE>
```

Run to perform just export function:

```
python main.py --embed-map <IDS_JSON_FILE> --api-key <API KEY> --base "<URL>"
```

Run to perform just search function with options:

```
python main.py --search --per-widget --csv csv/output.csv --details-dir csv --terms-file <TERMS TEXT FILE>
```

## Example Files to use

Example terms.txt:

```
Term1
Term2
Term3=(?i)(?<!\w)(?:TERM3|Term3|TeRM3)(?!\w)
```

Example widgets_id.json:

```
{
    "<WIDGET ID>": "<NAME OF WIDGET>"
}
```

## Upload generated embedding JSON files to a workspace

This repository includes a small uploader script at sim/upload_embedded.py which posts JSON embedding files to a workspace ingest endpoint.

Installation
- Ensure Python dependencies are installed:
  pip install -r requirements.txt

Usage
- Environment variables:
  BASE_URL (required)        - base URL of the workspace (e.g. https://workspace.example.com)
  UPLOAD_ENDPOINT (optional) - endpoint path (default: /ingest)
  API_KEY (optional)         - API key for Authorization header
- Command line:
  python -m sim.upload_embedded --source ./embedded --base-url https://example.com --endpoint /api/v1/ingest --concurrency 4 --wrap

Notes
- The uploader sends JSON (application/json). By default each file is wrapped with metadata:
  { "filename": "...", "checksum": "...", "document": <file content>, "metadata": {...} }
  Use --wrap to include metadata, or omit --wrap (not recommended) to send the file content verbatim.
- Do not commit API keys into source control. Use CI secrets for production.

## Auto-update workspace embeddings after upload

The uploader can optionally call AnythingLLM's workspace embeddings update endpoint (/v1/workspace/{slug}/update-embeddings) to add the newly uploaded documents to a workspace:

Example usage (upload and auto-update):
BASE_URL="https://anythingllm.example" API_KEY="your_api_key" \
python -m sim.upload_embedded --source ./embedded --folder custom-documents --workspaces "my-workspace" --auto-update

- --workspaces / WORKSPACES: comma-separated workspace slug(s) to attach the uploaded documents to.
- --auto-update: when set, the uploader will call /v1/workspace/{slug}/update-embeddings with the list of uploaded document locations returned by the upload endpoint.

Notes
- The uploader uses the document API responses' `location` (preferred) or `name` values as the items to send in the "adds" array for update-embeddings.
- Ensure API_KEY has permission to upload and update workspaces.
