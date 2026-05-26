# SIM Chat Server

## Overview

`chat-server` is a Maven WAR application for Jakarta EE servers.
Inteded to read the Chat Server's messages and provide reported analysis on them. 

- Packaging: `war`
- Group/Artifact: `com.sim.chatserver:chat-server`
- Java: project currently compiles with Maven `release 21` (see `pom.xml`)
- REST base path: `/api` (via `SIMApplication`)
- Web module includes servlet-based admin endpoints
- Database stack:
  - PostgreSQL (primary, via env vars)
- Entities include:
  - `UserAccount`
  - `Chat Review`
  - `AdminSettings`
  - `Playwright Tests`
- Contains SQL scripts for initializing the Database and Default admin user
  - See [resources/posgres](resources/posgres) directory for scripts

## Build & Deploy

See [resources/BUILD.MD](resources/BUILD.MD) file for information

## Configuration

### Environment variables

- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `chat`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `password`)
- `CONFIG_ENCRYPTION_KEY` (required) Base64 encryption key. (Can generate your own)
- `SIM_TRANSLATE_URL` (example: `http://localhost:5000/translate`)

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
