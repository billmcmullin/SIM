package com.sim.chatserver.security.review;

public final class ValidationResult {

    private static final String EMPTY = "";
    private static final String TOSTRING_PREFIX = "TrustedUrlValidator.ValidationResult{";
    private static final String TOSTRING_VALID_PREFIX = "valid=";
    private static final String TOSTRING_REASON_PREFIX = ", reason='";
    private static final String TOSTRING_HOST_PREFIX = ", host='";
    private static final String TOSTRING_SCHEME_PREFIX = ", scheme='";
    private static final String TOSTRING_PORT_PREFIX = ", port=";

    private final boolean valid;
    private final String reason;
    private final String host;
    private final String scheme;
    private final int port;

    private ValidationResult(boolean valid, String reason, String host, String scheme, int port) {
        this.valid = valid;
        this.reason = reason == null ? EMPTY : reason;
        this.host = host == null ? EMPTY : host;
        this.scheme = scheme == null ? EMPTY : scheme;
        this.port = port;
    }

    static ValidationResult valid(String host, String scheme, int port) {
        return new ValidationResult(true, EMPTY, host, scheme, port);
    }

    static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason, EMPTY, EMPTY, -1);
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    String getHost() {
        return host;
    }

    String getScheme() {
        return scheme;
    }

    int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
                + TOSTRING_VALID_PREFIX + valid
                + TOSTRING_REASON_PREFIX + reason + '\''
                + TOSTRING_HOST_PREFIX + host + '\''
                + TOSTRING_SCHEME_PREFIX + scheme + '\''
                + TOSTRING_PORT_PREFIX + port
                + '}';
    }
}
