package com.sim.chatserver.email;

public record GraphEmailConfig(
        String tenantId,
        String clientId,
        String clientSecret,
        String senderUser,
        String authorityHost // optional, defaults to login.microsoftonline.com if blank
        ) {

    public String effectiveAuthorityHost() {
        return (authorityHost == null || authorityHost.isBlank())
                ? "login.microsoftonline.com"
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
