# chat-server — SIM Chat Server

## Overview

`chat-server` is a Maven WAR application for Jakarta EE servers.

- Packaging: `war`
- Group/Artifact: `com.sim.chatserver:chat-server`
- Java: project currently compiles with Maven `release 18` (see `pom.xml`)
- REST base path: `/api` (via `SIMApplication`)
- Web module includes servlet-based admin endpoints
- Database stack:
  - PostgreSQL (primary, via env vars)
  - HikariCP connection pooling
  - Hibernate ORM (JPA provider)
  - Embedded H2 fallback when Postgres config is unavailable
- Entities include:
  - `UserAccount`
  - `Chat Review`
  - `AdminSettings`
- Startup initialization creates a default admin user if none exists.

## Build

```bash
mvn clean package
```

Output artifact:

- `target/chat-server.war`

Run tests:

```bash
mvn test
```

## Deploy

Deploy `target/chat-server.war` to a Jakarta EE 10+ compatible server (for example, WildFly).

## Configuration

### Database environment variables

- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `chat`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `password`)
- `CONFIG_ENCRYPTION_KEY` (required) Base64 encryption key. (Can generate your own)

Behavior:

- App attempts PostgreSQL connection using env vars above.
- If unavailable, app falls back to embedded H2.

### Translation environment variable

- `SIM_TRANSLATE_URL` (example: `http://localhost:5000/translate`)

> Note: corrected example format:  
> `http://<hostname>:5000/translate`

## Web / API Endpoints

### REST API

- Base path: `/api`
- Registered via:
  - `com.sim.chatserver.SIMApplication`
  - `@ApplicationPath("/api")`

### Servlet routes (from `web.xml`)

- `/admin` → `AdminConfigServlet`
- `/admin/widgets` → `WidgetApiServlet`
- `/admin/save-config` → `SaveConfigServlet`

- Welcome page: `login`

## Translation Support (LibreTranslate)

If translation features are enabled, run a LibreTranslate service and set `SIM_TRANSLATE_URL` so this app can reach it.

### Docker (quick run)

```bash
docker run -ti --rm -p 5000:5000 libretranslate/libretranslate
```

### Docker Compose example

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

Then configure:

```bash
SIM_TRANSLATE_URL=http://libretranslate:5000/translate
```

(Use `http://localhost:5000/translate` if running outside Docker Compose on the host machine.)

## Notes

- `pom.xml` is the source of truth for Java/compiler and dependency versions.
- If README and build config diverge, prefer updating README to match `pom.xml`.