package com.sim.chatserver.security;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.util.CommonLiteralConstants;

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

    private static final Logger log = Logger.getLogger(SecurityValidationService.class.getCanonicalName());
    private static final Pattern IPV4_LITERAL = Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern IPV6_LITERAL_CHARS = Pattern.compile("^[0-9A-Fa-f:]+$");
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);
    private static final char SLASH_CHAR = '/';
    private static final String EMPTY = "";
    private static final String DEFAULT_MODE = CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String JSON_MEDIA_TYPE = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String HTTPS_SCHEME = CommonLiteralConstants.SCHEME_HTTPS;
    private static final String HTTP_SCHEME = CommonLiteralConstants.SCHEME_HTTP;
    private static final String MISSING_MARKER = "(missing)";
    private static final String UNKNOWN_MARKER = CommonLiteralConstants.MARKER_UNKNOWN;
    private static final String NO_DETAIL_MARKER = "(no detail)";
    private static final String HOST_DOT_DOT = "..";
    private static final String IPV6_TRIPLE_COLON = ":::";
    private static final char COLON_CHAR = IPV6_TRIPLE_COLON.charAt(0);
    private static final String IPV4_DOT_SPLIT_REGEX = "\\.";
    private static final String IPV6_FC_PREFIX = "fc";
    private static final String IPV6_FD_PREFIX = "fd";
    private static final String IPV6_FE8_PREFIX = "fe8";
    private static final String IPV6_FE9_PREFIX = "fe9";
    private static final String IPV6_FEA_PREFIX = "fea";
    private static final String IPV6_FEB_PREFIX = "feb";
    private static final String ERR_URL_BLANK = "URL is blank";
    private static final String ERR_SCHEME_NOT_ALLOWED_PREFIX = "Scheme not allowed: ";
    private static final String ERR_HOST_MISSING = "Host missing in URL";
    private static final String ERR_HOST_SANITY_PREFIX = "Host failed sanity check: ";
    private static final String ERR_HOST_NOT_ALLOW_LIST_PREFIX = "Host not in allow-list: ";
    private static final String ERR_ALLOW_LIST_SEPARATOR = " | allowList=";
    private static final String ERR_DNS_NO_ADDRESSES_PREFIX = "DNS resolution returned no addresses for host: ";
    private static final String ERR_BLOCKED_ADDRESS_PREFIX = "Host resolved to blocked/private address: ";
    private static final String ERR_URL_EXCEPTION_PREFIX = "URL parse/validation exception: ";
    private static final String COLON_SPACE = ": ";
    private static final String ALLOW_LIST_REASON = "Host explicitly allow-listed";
    private static final String VALIDATED_REASON = "Validated";
    private static final String LOG_URL_BLOCKED = "validateUpstreamUrl blocked invalid URL";
    private static final String LOG_INVALID_IPV4_OCTET = "Invalid IPv4 octet encountered while parsing client address.";

    private static final Set<String> DEFAULT_ALLOWED_MODES = Set.of(DEFAULT_MODE, com.sim.chatserver.util.CommonLiteralConstants.FIELD_QUERY, "automatic");

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

    private SecurityValidationService(Set<String> allowedUpstreamHosts, Set<String> allowedModes) {
        this(allowedUpstreamHosts, allowedModes, true, true);
    }

    private SecurityValidationService(Set<String> allowedUpstreamHosts,
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

    boolean isJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String ct = canonicalizeInput(req.getContentType(), 128);
        if (ct.isBlank()) {
            return false;
        }
        int semicolon = ct.indexOf((char) 59);
        String mediaType = (semicolon >= 0 ? ct.substring(0, semicolon) : ct).trim().toLowerCase(Locale.ROOT);
        if (mediaType.isBlank() || mediaType.indexOf(SLASH_CHAR) <= 0 || mediaType.charAt(0) == SLASH_CHAR
                || mediaType.endsWith(String.valueOf(SLASH_CHAR))) {
            return false;
        }
        return JSON_MEDIA_TYPE.equals(mediaType);
    }

    private boolean isModeAllowed(String mode) {
        if (mode == null || mode.isBlank()) {
            return false;
        }
        return allowedModes.contains(mode.trim().toLowerCase(Locale.ROOT));
    }

    String normalizeModeOrDefault(String mode, String defaultMode) {
        String safeDefault = (defaultMode == null || defaultMode.isBlank())
                ? DEFAULT_MODE
                : defaultMode.trim().toLowerCase(Locale.ROOT);
        return isModeAllowed(mode) ? mode.trim().toLowerCase(Locale.ROOT) : safeDefault;
    }

    /**
     * Backward-compatible boolean validation.
     */
    boolean isAllowedUpstreamUrl(String baseUrl) {
        return validateUpstreamUrl(canonicalizeUrlInput(baseUrl)).isAllowed();
    }

    /**
     * Detailed validation result for better blocked-URL logging.
     */
    private UrlValidationResult validateUpstreamUrl(String baseUrl) {
        String canonicalBaseUrl = canonicalizeUrlInput(baseUrl);
        if (canonicalBaseUrl.isBlank()) {
            return UrlValidationResult.blocked(ERR_URL_BLANK);
        }

        final String trimmed = canonicalBaseUrl;

        try {
            URI uri = URI.create(trimmed);

            String scheme = lower(uri.getScheme());
            String safeScheme = (scheme == null || scheme.isBlank()) ? MISSING_MARKER : scheme;

            if (!HTTPS_SCHEME.equals(scheme) && !HTTP_SCHEME.equals(scheme)) {
                return UrlValidationResult.blocked(ERR_SCHEME_NOT_ALLOWED_PREFIX + safeScheme);
            }

            String host = lower(uri.getHost());
            String safeHost = (host == null || host.isBlank()) ? MISSING_MARKER : host;

            if (host == null || host.isBlank()) {
                return UrlValidationResult.blocked(ERR_HOST_MISSING);
            }

            boolean startsOrEndsWithDot = !host.isEmpty()
                    && (host.charAt(0) == '.' || host.charAt(host.length() - 1) == '.');
            if (host.contains(HOST_DOT_DOT) || startsOrEndsWithDot) {
                return UrlValidationResult.blocked(ERR_HOST_SANITY_PREFIX + safeHost);
            }

            boolean hostExplicitlyAllowed = !allowedUpstreamHosts.isEmpty() && isHostAllowed(host);

            // If allow-list is configured and host not matched -> reject immediately
            if (!allowedUpstreamHosts.isEmpty() && !hostExplicitlyAllowed) {
                return UrlValidationResult.blocked(
                    ERR_HOST_NOT_ALLOW_LIST_PREFIX + safeHost + ERR_ALLOW_LIST_SEPARATOR + allowedUpstreamHosts
                );
            }

            // If host is explicitly allow-listed, permit without private-range rejection.
            // This supports deployments where upstream is localhost/container-private.
            if (hostExplicitlyAllowed) {
                return UrlValidationResult.allowed(host, scheme, ALLOW_LIST_REASON);
            }

            if (resolveDnsForValidation && blockPrivateNetworkTargets) {
                InetAddress[] addrs = InetAddress.getAllByName(host);
                if (addrs == null || addrs.length == 0) {
                    return UrlValidationResult.blocked(
                            ERR_DNS_NO_ADDRESSES_PREFIX + safeHost
                    );
                }

                for (InetAddress addr : addrs) {
                    if (isBlockedAddress(addr)) {
                        String addrText = (addr == null || addr.getHostAddress() == null)
                                ? UNKNOWN_MARKER
                                : addr.getHostAddress();
                        return UrlValidationResult.blocked(
                                ERR_BLOCKED_ADDRESS_PREFIX + addrText
                        );
                    }
                }
            }

            return UrlValidationResult.allowed(host, scheme, VALIDATED_REASON);
        } catch (IllegalArgumentException | UnknownHostException | SecurityException ex) {
            String msg = ex.getMessage();
            String safeMsg = (msg == null || msg.isBlank()) ? NO_DETAIL_MARKER : msg;
            log.log(Level.FINE, LOG_URL_BLOCKED, ex);
            return UrlValidationResult.blocked(
                    ERR_URL_EXCEPTION_PREFIX + ex.getClass().getSimpleName() + COLON_SPACE + safeMsg
            );
        }
    }

    String extractClientIp(HttpServletRequest req) {
        if (req == null) {
            return UNKNOWN_MARKER;
        }

        String remote = canonicalizeInput(req.getRemoteHost(), 255);
        if (isParseableAddress(remote)) {
            return remote;
        }

        return UNKNOWN_MARKER;
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
        String[] parts = value.split(IPV4_DOT_SPLIT_REGEX);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isBlank() || part.length() > 3) {
                return false;
            }
            if (part.length() > 1 && part.charAt(0) == '0') {
                return false;
            }
            int octet;
            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                log.log(Level.FINE, LOG_INVALID_IPV4_OCTET, ex);
                return false;
            }
            if (octet < 0 || octet > 255) {
                return false;
            }
        }
        return true;
    }

    private boolean isLikelyIpv6Literal(String value) {
        if (value.length() > 45 || !value.contains(String.valueOf(COLON_CHAR))) {
            return false;
        }
        if (!IPV6_LITERAL_CHARS.matcher(value).matches()) {
            return false;
        }
        if (value.contains(IPV6_TRIPLE_COLON)) {
            return false;
        }
        long colonCount = value.chars().filter(ch -> ch == (char) 58).count();
        return colonCount >= 2;
    }

    private boolean isHostAllowed(String host) {
        if (allowedUpstreamHosts.contains(host)) {
            return true;
        }
        for (String allowed : allowedUpstreamHosts) {
            if (host.equals(allowed) || host.endsWith('.' + allowed)) {
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
            if (h.startsWith(IPV6_FC_PREFIX) || h.startsWith(IPV6_FD_PREFIX)) {
                return true;
            }
            // Link local fe80::/10
            if (h.startsWith(IPV6_FE8_PREFIX) || h.startsWith(IPV6_FE9_PREFIX)
                    || h.startsWith(IPV6_FEA_PREFIX) || h.startsWith(IPV6_FEB_PREFIX)) {
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

    private String canonicalizeInput(String value, int maxLen) {
        if (value == null) {
            return EMPTY;
        }
        String canonical = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (canonical.isBlank() || canonical.length() > maxLen) {
            return EMPTY;
        }
        if (CONTROL_CHARS.matcher(canonical).find()) {
            return EMPTY;
        }
        return canonical;
    }
}
