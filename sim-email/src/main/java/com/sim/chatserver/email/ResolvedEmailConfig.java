package com.sim.chatserver.email;

public record ResolvedEmailConfig(
        EmailConfig config,
        EmailConfigSource source,
        boolean valid,
        String message,
        EmailProviderType providerType,
        Object providerConfig
        ) {

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

    public ResolvedEmailConfig {
        source = (source == null) ? EmailConfigSource.NONE : source;
        message = (message == null) ? "" : message;
        providerType = (providerType == null) ? EmailProviderType.SMTP : providerType;

        // Keep providerConfig aligned with providerType when omitted
        if (providerConfig == null) {
            providerConfig = (providerType == EmailProviderType.SMTP) ? config : null;
        }
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
