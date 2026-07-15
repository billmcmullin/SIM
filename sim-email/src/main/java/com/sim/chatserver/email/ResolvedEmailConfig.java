package com.sim.chatserver.email;

public record ResolvedEmailConfig(
        EmailConfig config,
        EmailConfigSource source,
        boolean valid,
        String message,
        EmailProviderType providerType,
        Object providerConfig
        ) {

    public ResolvedEmailConfig(
            EmailConfig config,
            EmailConfigSource source,
            boolean valid,
            String message,
            EmailProviderType providerType,
            Object providerConfig
    ) {
        this.config = config;
        this.source = (source == null) ? EmailConfigSource.NONE : source;
        this.valid = valid;
        this.message = (message == null) ? "" : message;
        this.providerType = (providerType == null) ? EmailProviderType.SMTP : providerType;

        Object resolvedProviderConfig = providerConfig;
        if (resolvedProviderConfig == null) {
            resolvedProviderConfig = (this.providerType == EmailProviderType.SMTP) ? config : null;
        }
        this.providerConfig = resolvedProviderConfig;
    }

    // Backward-compatible ctor (existing SMTP call sites)
    public ResolvedEmailConfig(
            EmailConfig config,
            EmailConfigSource source,
            boolean valid,
            String message
    ) {
        this(
                config,
                source == null ? EmailConfigSource.NONE : source,
                valid,
                message == null ? "" : message,
                EmailProviderType.SMTP,
                config
        );
    }

    public static ResolvedEmailConfig smtp(
            EmailConfig config,
            EmailConfigSource source,
            boolean valid,
            String message
    ) {
        return new ResolvedEmailConfig(
                config,
                source,
                valid,
                message,
                EmailProviderType.SMTP,
                config
        );
    }

    public static ResolvedEmailConfig graph(
            GraphEmailConfig config,
            EmailConfigSource source,
            boolean valid,
            String message
    ) {
        return new ResolvedEmailConfig(
                null,
                source,
                valid,
                message,
                EmailProviderType.GRAPH,
                config
        );
    }

    public GraphEmailConfig graphConfigOrNull() {
        return (providerConfig instanceof GraphEmailConfig g) ? g : null;
    }
}
