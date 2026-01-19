Chat Server (WildFly) - Backend scaffold
=======================================

Overview
--------
This is a Java (Jakarta EE) WAR scaffold that provides:
- JAX-RS REST API
- JPA entities (Hibernate)
- Flyway migrations for PostgreSQL (pg_trgm and pgvector enabled)
- JWT-based authentication skeleton
- Startup migrator that runs Flyway using WildFly datasource

It is a starting point; core sync logic and export generation are left as TODOs to integrate your existing normalization and HTTP client logic.

Prerequisites
-------------
- Java 17 JDK
- Maven 3.8+
- WildFly 30+ (or compatible Jakarta EE server)
- PostgreSQL instance with pgvector and pg_trgm extensions available (Flyway migration will attempt to create them)

Configure WildFly datasource
----------------------------
1. Create a datasource in WildFly with JNDI name:
   java:jboss/datasources/ChatDS

   Point it to your Postgres database and ensure the user has permissions.
2. Add the PostgreSQL JDBC driver as a module in WildFly (or place the driver JAR into the server).

Build the WAR
-------------
From the project root:

```
mvn clean package
```
This produces target/chat-server.war.

Deployment
----------
- Deploy the WAR to WildFly (management console or copy to deployments/).
- WildFly will run the application; on startup Flyway will run migrations against the configured datasource.
- An initial admin user will be created with username "admin" and password "admin" (see BootstrapData). Change this in production.

Environment
-----------
- Provide a JWT secret via environment variable `CHAT_JWT_SECRET` on the WildFly server to change default signing key.
- Store API keys securely (this scaffold does not include secure storage; implement encryption and config pages).

Usage
-----
- Authentication:
  POST /api/auth/login  { "username":"admin", "password":"admin" }
  Response: { "token": "<jwt>" }

- Use the token in Authorization header:
  Authorization: Bearer <token>

- Trigger sync (admin):
  POST /api/admin/sync  (body: JSON SyncRequest)

