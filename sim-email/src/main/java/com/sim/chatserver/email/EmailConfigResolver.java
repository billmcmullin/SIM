package com.sim.chatserver.email;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class EmailConfigResolver {

    private static final Logger log = Logger.getLogger(EmailConfigResolver.class.getName());

    private final DbEmailConfigProvider dbProvider;
    private final DbGraphEmailConfigProvider graphDbProvider; // optional, can be null

    public EmailConfigResolver(DbEmailConfigProvider dbProvider) {
        this(dbProvider, null);
    }

    public EmailConfigResolver(DbEmailConfigProvider dbProvider, DbGraphEmailConfigProvider graphDbProvider) {
        this.dbProvider = dbProvider;
        this.graphDbProvider = graphDbProvider;
    }

    public ResolvedEmailConfig resolve() {
        // 1) SMTP from ENV
        EmailConfig env = EmailConfigLoader.loadEnvOnly();
        if (isUsableSmtp(env)) {
            return ResolvedEmailConfig.smtp(env, EmailConfigSource.ENV, true, "Using SMTP config from ENV");
        }

        // 2) SMTP from properties
        EmailConfig props = EmailConfigLoader.loadPropertiesOnly();
        if (isUsableSmtp(props)) {
            return ResolvedEmailConfig.smtp(props, EmailConfigSource.PROPERTIES, true, "Using SMTP config from properties");
        }

        // 3) GRAPH from DB (if provider present)
        GraphEmailConfig graph = null;
        if (graphDbProvider != null) {
            try {
                graph = graphDbProvider.load();
            } catch (RuntimeException e) {
                log.log(Level.WARNING, "Failed loading Graph config from database", e);
            }
        }
        if (isUsableGraph(graph)) {
            return ResolvedEmailConfig.graph(graph, EmailConfigSource.DATABASE, true, "Using Graph config from database");
        }

        // 4) SMTP from DB
        EmailConfig db = null;
        if (dbProvider != null) {
            try {
                db = dbProvider.load();
            } catch (RuntimeException e) {
                log.log(Level.WARNING, "Failed loading SMTP config from database", e);
            }
        }
        if (isUsableSmtp(db)) {
            return ResolvedEmailConfig.smtp(db, EmailConfigSource.DATABASE, true, "Using SMTP config from database");
        }

        // 5) none
        return new ResolvedEmailConfig(
                null,
                EmailConfigSource.NONE,
                false,
                "No valid email configuration found in ENV, properties, or database."
        );
    }

    private boolean isUsableSmtp(EmailConfig c) {
        return c != null
                && hasText(c.host())
                && c.port() > 0 && c.port() <= 65535;
    }

    private boolean isUsableGraph(GraphEmailConfig c) {
        return c != null && c.isUsable();
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
