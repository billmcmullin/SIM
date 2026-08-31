// src/main/java/com/sim/chatserver/security/review/TrustedUrlValidator.java
package com.sim.chatserver.security.review;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * URL allow/deny validator to reduce SSRF risk for workspace calls.
 *
 * Policy: - only http/https - host required - optional allowlist (exact host or
 * suffix like ".example.com") - blocks
 * localhost/loopback/link-local/site-local/multicast by default - blocks
 * obvious local aliases (localhost, *.local, *.internal)
 */
public final class TrustedUrlValidator {

    private static final Logger LOG = Logger.getLogger(TrustedUrlValidator.class.getName());

    private final Set<String> allowedHosts;     // exact lowercase hosts
    private final Set<String> allowedSuffixes;  // lowercase suffixes, e.g. ".example.com"
    private final boolean allowPrivateNetworks;

    public TrustedUrlValidator() {
        this(Set.of(), Set.of(), false);
    }

    public TrustedUrlValidator(Set<String> allowedHosts, Set<String> allowedSuffixes, boolean allowPrivateNetworks) {
        this.allowedHosts = normalizeSet(allowedHosts);
        this.allowedSuffixes = normalizeSet(allowedSuffixes);
        this.allowPrivateNetworks = allowPrivateNetworks;
    }

    /**
     * Validate URL string against trust rules.
     */
    public ValidationResult validate(String rawUrl) {
        String canonicalUrl = canonicalizeUrlInput(rawUrl);
        if (canonicalUrl.isBlank()) {
            return ValidationResult.invalid("URL is blank.");
        }

        final URI uri;
        try {
            uri = URI.create(canonicalUrl).normalize();
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, "URL parse failed during trust validation", ex);
            return ValidationResult.invalid("URL is invalid.");
        }

        String scheme = lower(uri.getScheme());
        if (!"https".equals(scheme) && !"http".equals(scheme)) {
            return ValidationResult.invalid("Only http/https URLs are allowed.");
        }

        String host = lower(uri.getHost());
        if (host.isBlank()) {
            return ValidationResult.invalid("URL host is missing.");
        }

        if (isObviouslyLocalAlias(host)) {
            return ValidationResult.invalid("Local/internal host is not allowed.");
        }

        // If allowlist configured, enforce it.
        if (!allowedHosts.isEmpty() || !allowedSuffixes.isEmpty()) {
            if (!isHostAllowedByAllowlist(host)) {
                return ValidationResult.invalid("Host is not in allowlist.");
            }
        }

        // Resolve and reject local/private ranges unless explicitly allowed.
        try {
            InetAddress address = InetAddress.getByName(host);
            if (!allowPrivateNetworks && isPrivateOrLocalAddress(address)) {
                return ValidationResult.invalid("Private/local network address is not allowed.");
            }
        } catch (UnknownHostException | SecurityException ex) {
            LOG.log(Level.FINE, "Host resolution failed during trust validation", ex);
            return ValidationResult.invalid("Host DNS resolution failed.");
        }

        return ValidationResult.valid(host, scheme, uri.getPort());
    }

    boolean isTrusted(String rawUrl) {
        String canonicalUrl = canonicalizeUrlInput(rawUrl);
        if (canonicalUrl.isBlank()) {
            return false;
        }
        return validate(canonicalUrl).isValid();
    }

    private boolean isHostAllowedByAllowlist(String host) {
        if (allowedHosts.contains(host)) {
            return true;
        }
        for (String suffix : allowedSuffixes) {
            if (suffix.isBlank()) {
                continue;
            }
            String normalizedSuffix = (suffix.charAt(0) == '.') ? suffix.substring(1) : suffix;
            if (normalizedSuffix.isBlank()) {
                continue;
            }
            // Support "example.com" (exact) and ".example.com" (subdomain suffix)
            if (host.equals(normalizedSuffix) || host.endsWith('.' + normalizedSuffix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isObviouslyLocalAlias(String host) {
        return "localhost".equals(host)
                || host.endsWith(".local")
                || host.endsWith(".internal")
                || host.endsWith(".localhost")
                || host.endsWith(".localdomain");
    }

    private boolean isPrivateOrLocalAddress(InetAddress a) {
        return a.isAnyLocalAddress()
                || a.isLoopbackAddress()
                || a.isLinkLocalAddress()
                || a.isSiteLocalAddress()
                || a.isMulticastAddress();
    }

    private Set<String> normalizeSet(Set<String> input) {
        Set<String> out = new HashSet<>();
        if (input == null) {
            return out;
        }
        for (String s : input) {
            String v = lower(s);
            if (!v.isBlank()) {
                out.add(v);
            }
        }
        return out;
    }

    private String lower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private String canonicalizeUrlInput(String rawUrl) {
        if (rawUrl == null) {
            return "";
        }
        return Normalizer.normalize(rawUrl, Normalizer.Form.NFKC)
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .trim();
    }

    public static final class ValidationResult {

        private final boolean valid;
        private final String reason;
        private final String host;
        private final String scheme;
        private final int port;

        private ValidationResult(boolean valid, String reason, String host, String scheme, int port) {
            this.valid = valid;
            this.reason = reason == null ? "" : reason;
            this.host = host == null ? "" : host;
            this.scheme = scheme == null ? "" : scheme;
            this.port = port;
        }

        private static ValidationResult valid(String host, String scheme, int port) {
            return new ValidationResult(true, "", host, scheme, port);
        }

        private static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason, "", "", -1);
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
            return "ValidationResult{"
                    + "valid=" + valid
                    + ", reason='" + reason + '\''
                    + ", host='" + host + '\''
                    + ", scheme='" + scheme + '\''
                    + ", port=" + port
                    + '}';
        }
    }
}
