package com.sim.chatserver.security;

final class UrlValidationResult {

    private static final String TOSTRING_PREFIX = "SecurityValidationService.UrlValidationResult{allowed=";
    private static final String TOSTRING_REASON_PREFIX = ", reason='";
    private static final String TOSTRING_HOST_PREFIX = ", host='";
    private static final String TOSTRING_SCHEME_PREFIX = ", scheme='";

    private final boolean allowed;
    private final String reason;
    private final String host;
    private final String scheme;

    private UrlValidationResult(boolean allowed, String reason, String host, String scheme) {
        this.allowed = allowed;
        this.reason = reason;
        this.host = host;
        this.scheme = scheme;
    }

    static UrlValidationResult allowed(String host, String scheme, String reason) {
        return new UrlValidationResult(true, reason, host, scheme);
    }

    static UrlValidationResult blocked(String reason) {
        return new UrlValidationResult(false, reason, null, null);
    }

    boolean isAllowed() {
        return allowed;
    }

    String getReason() {
        return reason;
    }

    String getHost() {
        return host;
    }

    String getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX + allowed
                + TOSTRING_REASON_PREFIX + reason + '\''
                + TOSTRING_HOST_PREFIX + host + '\''
                + TOSTRING_SCHEME_PREFIX + scheme + '\''
                + '}';
    }
}
