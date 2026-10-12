package com.sim.chatserver.email;

import java.util.logging.Logger;

public final class EmailConfigResolver {

    private static final Logger log = Logger.getLogger(EmailConfigResolver.class.getCanonicalName());
    private static final String RESOLVE_MSG_ENV_SMTP = "Using SMTP config from ENV";
    private static final String RESOLVE_MSG_PROPERTIES_SMTP = "Using SMTP config from properties";
    private static final String RESOLVE_MSG_DATABASE_GRAPH = "Using Graph config from database";
    private static final String RESOLVE_MSG_DATABASE_SMTP = "Using SMTP config from database";
    private static final String RESOLVE_MSG_NO_VALID_CONFIG =
            "No valid email configuration found in ENV, properties, or database.";

    private final DbEmailConfigProvider dbProvider;
    private final DbGraphEmailConfigProvider graphDbProvider; // optional, can be null

    private EmailConfigResolver(DbEmailConfigProvider dbProvider) {
        this(dbProvider, null);
    }

    private static EmailConfigResolver create(DbEmailConfigProvider dbProvider) {
        return new EmailConfigResolver(dbProvider);
    }

    public static ResolvedEmailConfig resolveEffectiveConfig(DbEmailConfigProvider dbProvider) {
        return create(dbProvider).resolve();
    }

    private EmailConfigResolver(DbEmailConfigProvider dbProvider, DbGraphEmailConfigProvider graphDbProvider) {
        this.dbProvider = dbProvider;
        this.graphDbProvider = graphDbProvider;
    }

    private final ResolvedEmailConfig resolve() {
        // 1) SMTP from ENV
        EmailConfig env = EmailConfigLoader.loadEnvOnly();
        if (isUsableSmtp(env)) {
            return ResolvedEmailConfig.smtp(env, EmailConfigSource.ENV, true, RESOLVE_MSG_ENV_SMTP);
        }

        // 2) SMTP from properties
        EmailConfig props = EmailConfigLoader.loadPropertiesOnly();
        if (isUsableSmtp(props)) {
            return ResolvedEmailConfig.smtp(props, EmailConfigSource.PROPERTIES, true, RESOLVE_MSG_PROPERTIES_SMTP);
        }

        // 3) GRAPH from DB (if provider present)
        GraphEmailConfig graph = null;
        if (graphDbProvider != null) {
            graph = graphDbProvider.load();
        }
        if (isUsableGraph(graph)) {
            return ResolvedEmailConfig.graph(graph, EmailConfigSource.DATABASE, true, RESOLVE_MSG_DATABASE_GRAPH);
        }

        // 4) SMTP from DB
        EmailConfig db = null;
        if (dbProvider != null) {
            db = dbProvider.load();
        }
        if (isUsableSmtp(db)) {
            return ResolvedEmailConfig.smtp(db, EmailConfigSource.DATABASE, true, RESOLVE_MSG_DATABASE_SMTP);
        }

        // 5) none
        return new ResolvedEmailConfig(
                null,
                EmailConfigSource.NONE,
                false,
            RESOLVE_MSG_NO_VALID_CONFIG
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
