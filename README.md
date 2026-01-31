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

The app will attempt to use environment variables to connect to Postgres. If not available it falls back to an embedded H2 database.

