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
- `sim-testng-tests`: isolated TestNG-only unit test module (targets shared code such as sim-core)
- `sim-playwright`: browser-based integration/end-to-end tests

Database bootstrap and SQL utilities are available in [resources/postgres](resources/postgres).

## Build & Deploy

See [resources/BUILD.MD](resources/BUILD.MD) for build, packaging, and deployment workflows.

## TestNG Unit Test Module

Run the dedicated TestNG unit-test module:

```bash
mvn -pl sim-core -Ddependency-check.skip=true -DskipTests install
mvn -pl sim-testng-tests -Ddependency-check.skip=true test
```

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
