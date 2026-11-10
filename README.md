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
