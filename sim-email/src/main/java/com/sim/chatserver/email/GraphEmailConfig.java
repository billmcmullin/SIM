package com.sim.chatserver.email;

public record GraphEmailConfig(
        String tenantId,
        String clientId,
        String clientSecret,
        String senderUser,
        String authorityHost // optional, defaults to login.microsoftonline.com if blank
        ) {

    private static final String DEFAULT_AUTHORITY_HOST = "login.microsoftonline.com";

    public String effectiveAuthorityHost() {
        return (authorityHost == null || authorityHost.isBlank())
                ? DEFAULT_AUTHORITY_HOST
                : authorityHost.trim();
    }

    public boolean isUsable() {
        return hasText(tenantId)
                && hasText(clientId)
                && hasText(clientSecret)
                && hasText(senderUser);
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
