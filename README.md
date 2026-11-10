# SIM Chat Server

## Overview

`chat-server` is a multi-module Maven project that produces a Jakarta EE WAR for deployment to WildFly-compatible application servers.

Primary responsibilities:

- Connect to an upstream chat/workspace service
- Sync widget chat data into PostgreSQL
- Provide admin endpoints and UI for configuration, health checks, and sync operations
- Generate dashboard and summary/reporting data from synchronized chat content

Technical summary:

- Parent artifact: `com.sim.chatserver:chat-server-parent`
- Runtime packaging: `war` (from `sim-app`)
- Java: Maven `release 21` (see `pom.xml`)
- REST base path: `/api` (via `SIMApplication`)
- Primary database: PostgreSQL

Project modules:

- `sim-core`: core services, models, persistence helpers, and integration logic
- `sim-web`: servlet layer for admin and dashboard endpoints
- `sim-app`: WAR assembly and web assets
- `sim-email`: email-related integrations and utilities
- `sim-playwright`: browser-based integration/end-to-end tests

Database bootstrap and SQL utilities are available in [resources/postgres](resources/postgres).

## Build & Deploy

See [resources/BUILD.MD](resources/BUILD.MD) for build, packaging, and deployment workflows.

## Configuration

### Environment variables

- `DB_HOST`: PostgreSQL host
- `DB_PORT`: PostgreSQL port
- `DB_NAME`: PostgreSQL database name
- `DB_USER`: PostgreSQL user
- `DB_PASSWORD`: PostgreSQL password
- `CONFIG_ENCRYPTION_KEY`: required Base64 key used to protect stored secrets
- `SIM_TRANSLATE_URL`: optional translation service endpoint (for translation-enabled flows)
- `WIDGET_HEALTHCHECK_DEBUG_FAILURES`: enables verbose healthcheck failure diagnostics when `true`
- `WIDGET_HEALTHCHECK_REQUIRE_HTTPS_WITH_AUTH`: when `true`, healthcheck enforces HTTPS if auth material is configured
- `WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH`: when `true`, widget sync and daily summary enforce HTTPS if API key auth is configured
- `SIM_SERVER_DIAGNOSTIC_LOG_ENABLED`: enables file-based diagnostics logging
- `SIM_SERVER_DIAGNOSTIC_LOG_DIR`: diagnostics output directory when diagnostics logging is enabled
- `SIM_WORKSPACECLIENT_VERBOSE_WILDFLY_LOG`: adds upstream request/response snippets to WildFly logs when `true`

### Connection Checks

Test Connection validates connectivity and API authentication with a read-only
`GET /api/v1/system` request. Leaving the API Key field blank uses the stored key.
This check does not start inference or validate model capacity.

Test Inference is a separate, explicitly triggered action. After verifying the
system endpoint, it sends one small chat request to the configured workspace.
It never retries with alternate payloads or follows redirects. Inference failure
does not invalidate an otherwise successful connectivity check or prevent saving
the server configuration. Editing the server host, port, or API key requires a
new connectivity check before saving.

Failures identify the system or inference stage. Transport errors include an
error reference that can be matched to server logs. An inference timeout does
not prove that upstream processing stopped; check logs before resending.

### Summary Request Safety

Daily summaries send one HTTP/1.1 request using the configured server API key.
There are no automatic transport retries, authentication retries, larger-payload
fallbacks, or incremental batch requests for summary generation. The general
workspace retry setting does not apply to these requests.

The Summary Configuration request-byte limit applies to the complete UTF-8 JSON
body, including its envelope and escaped text (default: 4096 bytes). Evidence is
reduced when needed to fit. Summary request diagnostics record metadata, not the
full chat payload.

A failed, timed-out, empty, or aborted summary pauses automatic summaries. The
latest persisted summary status also keeps failed or interrupted runs paused
after restart. Review the SIM and AnythingLLM/model-provider logs before manually
generating the summary again. A timeout does not prove the upstream inference
stopped; wait until the server is ready before resending. A successful manual
summary resumes the normal schedule.

If chat data loads successfully but contains no entries, no upstream request is
sent and the summary panel shows a no-chats result. An empty-data check does not
clear an existing upstream-failure pause. Database-loading failures are reported
as failures, not as empty chat lists.

When a requested day/slot has no stored summary, the panel labels any saved
fallback with its actual day/slot. Failed requests show failure status rather
than raw upstream error pages or analysis sections; their entry count describes
chats collected, not successfully analyzed chats.

These safeguards reduce request amplification; AnythingLLM and its model provider
still need suitable memory, context, output-token, and concurrency limits.

#### Question-Focused Reviews

Newly generated summaries use four display sections: What Users Asked, Questions
and Answers, Candidate Knowledge Gaps, and Recommended Improvements. The existing
storage fields and single-request safety limits remain unchanged. Older summaries
retain their original labels.

Each topic should identify the question, what the supplied response covered,
the remaining gap, evidence references such as E1, and confidence based on the
visible excerpts. Long excerpts retain opening and closing context and are
explicitly marked as truncated, within the same character limits.

The built-in prompt no longer requests numerical performance scores. It separates
candidate content gaps from missing customer context and appropriate escalation,
and asks for specific article additions, priority, benefit and validation. The
returned Next action is stored explicitly rather than inferred from keywords.

Exact or whitespace-equivalent copies of the previous built-in default prompt
are upgraded when loaded; custom prompt text is preserved. No summary is resent
automatically. The new format applies to subsequent generation after deployment.

These reviews use a bounded sample, not necessarily every collected chat. They
cannot independently verify factual accuracy, resolution, customer satisfaction
or the absence of content from the actual knowledge base. Validate proposed gaps
against documentation and the existing corpus before adding content.

### Full Analysis

Full Analysis is a separate admin workflow; Quick Summary retains its sampled,
single-request behavior. Open the Full Analysis admin tab, choose a day, review
the conservative budgets, and explicitly start a job. Submission returns a job
ID immediately; polling does not start or resend inference.

The worker captures registered widget chats for the selected day up to the job's
request time in a read-only repeatable-read database snapshot. Full questions and
answers are retained, hashed, and divided into lossless Unicode-safe segments.
Complete source text is fetched only after checking its size. Oversized selections
fail explicitly before inference rather than being silently sampled or truncated.

Each segment is sent in a fresh reset context and processed once. Results require
structured question/answer coverage, gap classification, concrete improvements,
confidence, and a literal source quote verified against the segment. Completed
results are checkpointed before proceeding. A bounded hierarchy synthesizes the
saved findings; per-chat evidence remains available independently of the overview.

Defaults are a 4096-byte wire cap, a smaller 2048-byte conservative input budget,
a 10-second gap, 50 requests per run, and a 15-minute run budget. The effective
request cap is the smaller byte limit, including JSON escaping and the envelope.
These are byte budgets, not provider token counts: reserve model capacity for
AnythingLLM system prompts, retrieval context and output separately. Work budgets
are checked between requests; an already-running call can outlast a run deadline.

Explicit snapshot caps are 5000 chats, 8 MiB source text, and 20000 map segments.
Synthesis has level/work-item caps, and results have a 16 MiB work budget checked
before subsequent requests. Responses above 256 KiB are rejected, never accepted
as truncated completed findings. If a cap is reached, export saved partial work
and continue manually where supported or choose a smaller selection. The status
never marks a partial analysis complete.

SIM inference transports coordinate by canonical upstream host/port using local
and PostgreSQL locks. Use the same canonical server origin across features; aliases
and traffic from other applications still require upstream concurrency controls.
An interrupted request, gateway/capacity error, timeout, or orphaned active marker
can retain a shared pause. Test Connection's read-only system probe is not gated.
The explicit Test Inference action does honor the gate.

Pause and Cancel stop subsequent work, not necessarily an already-running model
request. Completed checkpoints are retained. Resume requires an explicit manual
action; an uncertain request additionally requires confirmation that upstream is
idle and authorization to resend it. Do not confirm idle merely because the client
timed out. The separate Confirm Server Idle control can release a reviewed shared
pause without sending inference. No jobs or uncertain requests replay on restart.

Full Analysis requires the existing durable workflow runtime and fails closed
when that storage is unavailable. Records use the existing workflow schema, with
a common 30-day deadline from job creation. Expired jobs cannot replay pending
work. Full text and findings are sensitive: apply source-chat database, backup,
access and retention policies. No API key is stored in job payloads or returned
to the browser, and payload text is not written to request logs.

The UI reports processed chats, source segments, pending work, uncertain requests
and attempted calls. Details are paginated, rendered as text, and not clipped to
the Quick Summary section limits. Export Saved Results streams NDJSON containing
job status and saved work items; a running or paused job is explicitly partial.
Successful downloads end with an `exportComplete: true` record and the expected
work-item count; a missing completion marker means the export is incomplete.
Completion means all snapshot segments were processed and synthesis finished,
not that the model independently proved correctness, exhaustive question detection,
customer satisfaction or absence of existing knowledge-base content.

A received response that fails the structured/evidence validation is saved as
unvalidated text, not counted as a completed analysis, and never automatically
retried. Review it in the work-item details before explicitly retrying that invalid
segment. This is distinct from a transport timeout whose upstream outcome may
still be uncertain. Cancelled uncertain work stays cancelled; idle confirmation
does not resume it or resend it.

The configured upstream key is never returned to the browser or copied into job
metadata. Full original chat text can itself contain sensitive user-provided
content; review data-handling policy before enabling this workflow. Check proxy
retry settings and configure provider context, retrieval, output-token and
concurrency limits before a live run; client-side limits alone cannot guarantee
that an upstream model instance will not exhaust its resources.

The admin/owner-protected API is `/admin/full-analysis`:

- `POST` JSON with `action: start`, `day`, `requestBytes`, `inputByteBudget`, `paceSeconds`, `requestsPerRun`, `minutesPerRun`.
- `GET action=list`, `GET action=status&jobId=...`, or `GET action=details&jobId=...&offset=...`.
- `POST` JSON with `action: pause|resume|cancel`, `jobId`, and explicit `confirmedIdle: true` when authorizing uncertain continuation.
- `POST` JSON with `action: confirmIdle` and explicit `confirmedIdle: true` to release a reviewed server pause without inference.
- `GET action=export&jobId=...` for the saved NDJSON report.

### Translation Support (LibreTranslate)

If translation features are enabled, run a LibreTranslate service and set `SIM_TRANSLATE_URL` so this app can reach it.

#### Translation Docker Container(quick run)

```bash
docker run -ti --rm -p 5000:5000 libretranslate/libretranslate
```

#### Docker Compose example

```yaml
services:
  libretranslate:
    image: libretranslate/libretranslate:latest
    container_name: libretranslate
    hostname: libretranslate
    stdin_open: true
    tty: true
    ports:
      - "5000:5000"
    networks:
      - sim-network
networks:
  sim-network:
    driver: bridge
```

(Use `http://localhost:5000/translate` if running outside Docker Compose on the host machine.)
