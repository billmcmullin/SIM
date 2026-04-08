chat-server — SIM Chat Server
=============================

Overview

- Maven WAR using Java 21
- Package: com.sim.chatserver
- Includes Postgres + Hikari + Hibernate connectivity with H2 fallback
- Entities: UserAccount, Chat, AdminSettings
- Startup initializer creates default admin user if none exists.

Build

- mvn clean package

Deploy

- Deploy target/chat-server.war to WildFly (or other Jakarta EE 10+ server).

Configuration (env vars)

- DB_HOST (default: localhost)
- DB_PORT (default: 5432)
- DB_NAME (default: chat)
- DB_USER (default: postgres)
- DB_PASSWORD (password)
- SIM_TRANSLATE_URL: http://<hostname>>:5000/translate

The app will attempt to use environment variables to connect to Postgres. If not available it falls back to an embedded H2 database.

Translate support

Make sure to launch LibreTranslate server and the ENV is correctly configured to communicate with it.

Docker command:

```cmd
docker run -ti --rm -p 5000:5000 libretranslate/libretranslate
```

Docker compose:

```docker
libreTranslate:
    image: libretranslate/libretranslate:latest
    hostname: libretranslate
    container_name: libretranslate
    stdin_open: true
    tty: true
    networks:
      - <SIM NETWORK>
```

