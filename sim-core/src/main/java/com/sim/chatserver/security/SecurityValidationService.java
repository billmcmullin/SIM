package com.sim.chatserver.security;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Centralized security validation helpers for request validation and outbound
 * URL checks.
 *
 * Defensive defaults:
 * - JSON content-type required
 * - strict mode allow-list
 * - SSRF protections for upstream URL
 *
 * Update:
 * - Allows explicit allow-listed hosts (including localhost/127.0.0.1)
 *   even when private-network blocking is enabled.
 * - Adds detailed diagnostics for blocked URL decisions.
 */
public final class SecurityValidationService {

    private static final Logger log = Logger.getLogger(SecurityValidationService.class.getName());
    private static final Pattern IPV4_LITERAL = Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern IPV6_LITERAL_CHARS = Pattern.compile("^[0-9A-Fa-f:]+$");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");

    private static final Set<String> DEFAULT_ALLOWED_MODES = Set.of("chat", "query", "automatic");

    /**
     * If non-empty, upstream host must match this allow-list (exact or subdomain).
     */
    private final Set<String> allowedUpstreamHosts;

    private final Set<String> allowedModes;

    /**
     * If true, block private/local/reserved target addresses
     * (unless host is explicitly allow-listed).
     */
    private final boolean blockPrivateNetworkTargets;

    /**
     * If true, resolve host and reject if DNS maps to private/local/reserved ranges
     * (unless host is explicitly allow-listed).
     */
    private final boolean resolveDnsForValidation;

    public SecurityValidationService() {
        this(Set.of(), DEFAULT_ALLOWED_MODES, true, true);
    }

    public SecurityValidationService(Set<String> allowedUpstreamHosts, Set<String> allowedModes) {
        this(allowedUpstreamHosts, allowedModes, true, true);
    }

    public SecurityValidationService(Set<String> allowedUpstreamHosts,
                                     Set<String> allowedModes,
                                     boolean blockPrivateNetworkTargets,
                                     boolean resolveDnsForValidation) {
        this.allowedUpstreamHosts = normalizeSet(allowedUpstreamHosts);
        this.allowedModes = normalizeSet(
                (allowedModes == null || allowedModes.isEmpty()) ? DEFAULT_ALLOWED_MODES : allowedModes
        );
        this.blockPrivateNetworkTargets = blockPrivateNetworkTargets;
        this.resolveDnsForValidation = resolveDnsForValidation;
    }

    public boolean isJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String ct = canonicalizeInput(req.getContentType(), 128);
        if (ct.isBlank()) {
            return false;
        }
        int semicolon = ct.indexOf(';');
        String mediaType = (semicolon >= 0 ? ct.substring(0, semicolon) : ct).trim().toLowerCase(Locale.ROOT);
        if (mediaType.isBlank() || mediaType.indexOf('/') <= 0 || mediaType.startsWith("/") || mediaType.endsWith("/")) {
            return false;
        }
        return "application/json".equals(mediaType);
    }

    public boolean isModeAllowed(String mode) {
        if (mode == null || mode.isBlank()) {
            return false;
        }
        return allowedModes.contains(mode.trim().toLowerCase(Locale.ROOT));
    }

    public String normalizeModeOrDefault(String mode, String defaultMode) {
        String safeDefault = (defaultMode == null || defaultMode.isBlank())
                ? "chat"
                : defaultMode.trim().toLowerCase(Locale.ROOT);
        return isModeAllowed(mode) ? mode.trim().toLowerCase(Locale.ROOT) : safeDefault;
    }

    /**
     * Backward-compatible boolean validation.
     */
    public boolean isAllowedUpstreamUrl(String baseUrl) {
        return validateUpstreamUrl(canonicalizeUrlInput(baseUrl)).isAllowed();
    }

    /**
     * Detailed validation result for better blocked-URL logging.
     */
    public UrlValidationResult validateUpstreamUrl(String baseUrl) {
        String canonicalBaseUrl = canonicalizeUrlInput(baseUrl);
        if (canonicalBaseUrl.isBlank()) {
            return UrlValidationResult.blocked("URL is blank");
        }

        final String trimmed = canonicalBaseUrl;

        try {
            URI uri = URI.create(trimmed);

            String scheme = lower(uri.getScheme());
            String safeScheme = (scheme == null || scheme.isBlank()) ? "(missing)" : scheme;

            if (!"https".equals(scheme) && !"http".equals(scheme)) {
                return UrlValidationResult.blocked("Scheme not allowed: " + safeScheme);
            }

            String host = lower(uri.getHost());
            String safeHost = (host == null || host.isBlank()) ? "(missing)" : host;

            if (host == null || host.isBlank()) {
                return UrlValidationResult.blocked("Host missing in URL");
            }

            if (host.contains("..") || host.startsWith(".") || host.endsWith(".")) {
                return UrlValidationResult.blocked("Host failed sanity check: " + safeHost);
            }

            boolean hostExplicitlyAllowed = !allowedUpstreamHosts.isEmpty() && isHostAllowed(host);

            // If allow-list is configured and host not matched -> reject immediately
            if (!allowedUpstreamHosts.isEmpty() && !hostExplicitlyAllowed) {
                return UrlValidationResult.blocked(
                        "Host not in allow-list: " + safeHost + " | allowList=" + allowedUpstreamHosts
                );
            }

            // If host is explicitly allow-listed, permit without private-range rejection.
            // This supports deployments where upstream is localhost/container-private.
            if (hostExplicitlyAllowed) {
                return UrlValidationResult.allowed(host, scheme, "Host explicitly allow-listed");
            }

            if (resolveDnsForValidation && blockPrivateNetworkTargets) {
                InetAddress[] addrs = InetAddress.getAllByName(host);
                if (addrs == null || addrs.length == 0) {
                    return UrlValidationResult.blocked(
                            "DNS resolution returned no addresses for host: " + safeHost
                    );
                }

                for (InetAddress addr : addrs) {
                    if (isBlockedAddress(addr)) {
                        String addrText = (addr == null || addr.getHostAddress() == null)
                                ? "(unknown)"
                                : addr.getHostAddress();
                        return UrlValidationResult.blocked(
                                "Host resolved to blocked/private address: " + addrText
                        );
                    }
                }
            }

            return UrlValidationResult.allowed(host, scheme, "Validated");
        } catch (IllegalArgumentException | UnknownHostException | SecurityException ex) {
            String msg = ex.getMessage();
            String safeMsg = (msg == null || msg.isBlank()) ? "(no detail)" : msg;
            log.log(Level.FINE, "validateUpstreamUrl blocked invalid URL", ex);
            return UrlValidationResult.blocked(
                    "URL parse/validation exception: " + ex.getClass().getSimpleName() + ": " + safeMsg
            );
        }
    }

    public String extractClientIp(HttpServletRequest req) {
        if (req == null) {
            return "(unknown)";
        }

        String remote = canonicalizeInput(req.getRemoteAddr(), 64);
        if (isParseableAddress(remote)) {
            return remote;
        }

        // Only fall back to forwarding headers when remote address is unavailable.
        String xff = readForwardedHeaderToken(req, "X-Forwarded-For", 512);
        if (!xff.isBlank()) {
            String first = canonicalizeInput(xff.split(",")[0], 64);
            if (isParseableAddress(first)) {
                return first;
            }
        }

        String xri = readForwardedHeaderToken(req, "X-Real-IP", 64);
        if (isParseableAddress(xri)) {
            return xri;
        }

        return "(unknown)";
    }

    private boolean isParseableAddress(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (isValidIpv4Literal(value)) {
            return true;
        }
        return isLikelyIpv6Literal(value);
    }

    private boolean isValidIpv4Literal(String value) {
        if (!IPV4_LITERAL.matcher(value).matches()) {
            return false;
        }
        String[] parts = value.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isBlank() || part.length() > 3) {
                return false;
            }
            if (part.length() > 1 && part.startsWith("0")) {
                return false;
            }
            int octet;
            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (octet < 0 || octet > 255) {
                return false;
            }
        }
        return true;
    }

    private boolean isLikelyIpv6Literal(String value) {
        if (value.length() > 45 || !value.contains(":")) {
            return false;
        }
        if (!IPV6_LITERAL_CHARS.matcher(value).matches()) {
            return false;
        }
        if (value.contains(":::")) {
            return false;
        }
        long colonCount = value.chars().filter(ch -> ch == ':').count();
        return colonCount >= 2;
    }

    private boolean isHostAllowed(String host) {
        if (allowedUpstreamHosts.contains(host)) {
            return true;
        }
        for (String allowed : allowedUpstreamHosts) {
            if (host.equals(allowed) || host.endsWith("." + allowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlockedAddress(InetAddress addr) {
        if (addr == null) {
            return true;
        }

        if (addr.isAnyLocalAddress()
                || addr.isLoopbackAddress()
                || addr.isLinkLocalAddress()
                || addr.isSiteLocalAddress()
                || addr.isMulticastAddress()) {
            return true;
        }

        byte[] b = addr.getAddress();
        if (addr instanceof Inet4Address) {
            int o1 = b[0] & 0xFF;
            int o2 = b[1] & 0xFF;

            // 100.64.0.0/10 (CGNAT)
            if (o1 == 100 && (o2 >= 64 && o2 <= 127)) {
                return true;
            }
            // 169.254.0.0/16 (link-local)
            if (o1 == 169 && o2 == 254) {
                return true;
            }
            // 0.0.0.0/8
            if (o1 == 0) {
                return true;
            }
            // 240.0.0.0/4 reserved
            if (o1 >= 240) {
                return true;
            }

        } else if (addr instanceof Inet6Address a6) {
            if (a6.isIPv4CompatibleAddress()) {
                return true;
            }

            String h = a6.getHostAddress().toLowerCase(Locale.ROOT);
            // Unique local fc00::/7
            if (h.startsWith("fc") || h.startsWith("fd")) {
                return true;
            }
            // Link local fe80::/10
            if (h.startsWith("fe8") || h.startsWith("fe9") || h.startsWith("fea") || h.startsWith("feb")) {
                return true;
            }
        }

        return false;
    }

    private Set<String> normalizeSet(Set<String> input) {
        Set<String> out = new HashSet<>();
        if (input == null) {
            return out;
        }
        for (String v : input) {
            String n = lower(v);
            if (n != null && !n.isBlank()) {
                out.add(n);
            }
        }
        return out;
    }

    private String lower(String v) {
        return v == null ? null : v.trim().toLowerCase(Locale.ROOT);
    }

    private String canonicalizeUrlInput(String value) {
        return canonicalizeInput(value, 2048);
    }

    private String readForwardedHeaderToken(HttpServletRequest req, String headerName, int maxLen) {
        if (req == null || headerName == null || headerName.isBlank()) {
            return "";
        }
        Enumeration<String> headers = req.getHeaders(headerName);
        if (headers == null) {
            return "";
        }
        while (headers.hasMoreElements()) {
            String raw = headers.nextElement();
            if (raw == null || raw.isBlank()) {
                continue;
            }
            int comma = raw.indexOf(',');
            String token = comma >= 0 ? raw.substring(0, comma) : raw;
            String normalized = canonicalizeInput(token, maxLen);
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return "";
    }

    private String canonicalizeInput(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        String canonical = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (canonical.isBlank() || canonical.length() > maxLen) {
            return "";
        }
        if (CONTROL_CHARS.matcher(canonical).find()) {
            return "";
        }
        return canonical;
    }

    public static final class UrlValidationResult {

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

        public static UrlValidationResult allowed(String host, String scheme, String reason) {
            return new UrlValidationResult(true, reason, host, scheme);
        }

        public static UrlValidationResult blocked(String reason) {
            return new UrlValidationResult(false, reason, null, null);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public String getHost() {
            return host;
        }

        public String getScheme() {
            return scheme;
        }

        @Override
        public String toString() {
            return "UrlValidationResult{allowed=" + allowed
                    + ", reason='" + reason + '\''
                    + ", host='" + host + '\''
                    + ", scheme='" + scheme + '\''
                    + '}';
        }
    }
}
