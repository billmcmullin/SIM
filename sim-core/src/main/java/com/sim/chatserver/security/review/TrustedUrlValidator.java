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

import com.sim.chatserver.util.CommonLiteralConstants;

/**
 * URL allow/deny validator to reduce SSRF risk for workspace calls.
 *
 * Policy: - only http/https - host required - optional allowlist (exact host or
 * suffix like ".example.com") - blocks
 * localhost/loopback/link-local/site-local/multicast by default - blocks
 * obvious local aliases (localhost, *.local, *.internal)
 */
public final class TrustedUrlValidator {

    private static final Logger LOG = Logger.getLogger(TrustedUrlValidator.class.getCanonicalName());
    private static final String EMPTY = "";
    private static final String HTTPS_SCHEME = CommonLiteralConstants.SCHEME_HTTPS;
    private static final String HTTP_SCHEME = CommonLiteralConstants.SCHEME_HTTP;
    private static final String HOST_LOCALHOST = com.sim.chatserver.util.CommonLiteralConstants.HOST_LOCALHOST;
    private static final String SUFFIX_LOCAL = ".local";
    private static final String SUFFIX_INTERNAL = ".internal";
    private static final String SUFFIX_LOCALHOST = ".localhost";
    private static final String SUFFIX_LOCALDOMAIN = ".localdomain";
    private static final String CONTROL_CHARS_REGEX = com.sim.chatserver.util.CommonLiteralConstants.REGEX_CTRL_EXCEPT_CRLF_TAB;
    private static final String LOG_URL_PARSE_FAILED = "URL parse failed during trust validation";
    private static final String LOG_HOST_RESOLUTION_FAILED = "Host resolution failed during trust validation";
    private static final String ERR_URL_BLANK = "URL is blank.";
    private static final String ERR_URL_INVALID = "URL is invalid.";
    private static final String ERR_SCHEME = "Only http/https URLs are allowed.";
    private static final String ERR_HOST_MISSING = "URL host is missing.";
    private static final String ERR_LOCAL_HOST = "Local/internal host is not allowed.";
    private static final String ERR_ALLOWLIST = "Host is not in allowlist.";
    private static final String ERR_PRIVATE_NETWORK = "Private/local network address is not allowed.";
    private static final String ERR_DNS_FAILED = "Host DNS resolution failed.";

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
            return ValidationResult.invalid(ERR_URL_BLANK);
        }

        final URI uri;
        try {
            uri = URI.create(canonicalUrl).normalize();
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, LOG_URL_PARSE_FAILED, ex);
            return ValidationResult.invalid(ERR_URL_INVALID);
        }

        String scheme = lower(uri.getScheme());
        if (!HTTPS_SCHEME.equals(scheme) && !HTTP_SCHEME.equals(scheme)) {
            return ValidationResult.invalid(ERR_SCHEME);
        }

        String host = lower(uri.getHost());
        if (host.isBlank()) {
            return ValidationResult.invalid(ERR_HOST_MISSING);
        }

        if (isObviouslyLocalAlias(host)) {
            return ValidationResult.invalid(ERR_LOCAL_HOST);
        }

        // If allowlist configured, enforce it.
        if (!allowedHosts.isEmpty() || !allowedSuffixes.isEmpty()) {
            if (!isHostAllowedByAllowlist(host)) {
                return ValidationResult.invalid(ERR_ALLOWLIST);
            }
        }

        // Resolve and reject local/private ranges unless explicitly allowed.
        try {
            InetAddress address = InetAddress.getByName(host);
            if (!allowPrivateNetworks && isPrivateOrLocalAddress(address)) {
                return ValidationResult.invalid(ERR_PRIVATE_NETWORK);
            }
        } catch (UnknownHostException | SecurityException ex) {
            LOG.log(Level.FINE, LOG_HOST_RESOLUTION_FAILED, ex);
            return ValidationResult.invalid(ERR_DNS_FAILED);
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
        return HOST_LOCALHOST.equals(host)
            || host.endsWith(SUFFIX_LOCAL)
            || host.endsWith(SUFFIX_INTERNAL)
            || host.endsWith(SUFFIX_LOCALHOST)
            || host.endsWith(SUFFIX_LOCALDOMAIN);
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
        return s == null ? EMPTY : s.trim().toLowerCase(Locale.ROOT);
    }

    private String canonicalizeUrlInput(String rawUrl) {
        if (rawUrl == null) {
            return EMPTY;
        }
        return Normalizer.normalize(rawUrl, Normalizer.Form.NFKC)
                .replaceAll(CONTROL_CHARS_REGEX, EMPTY)
                .trim();
    }
}
