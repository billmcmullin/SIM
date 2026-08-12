package com.sim.chatserver.term;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for sanitizing prompt text retrieved from the database and for
 * preparing text for storage, matching, and display.
 *
 * Added: sanitizeMarkdownForDisplay - prepares Markdown text for safe HTML
 * rendering while preserving Unicode/smart punctuation.
 */
public final class TextSanitizer {

    private static final Logger LOG = Logger.getLogger(TextSanitizer.class.getName());

    private static final Pattern XML_HEADER = Pattern.compile("^\\s*<\\?xml[^>]*\\?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY = Pattern.compile("&(#(x)?[0-9a-fA-F]+|[a-zA-Z]+);");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private static final Map<String, String> NAMED_ENTITY_MAP = buildNamedEntityMap();

    private static Map<String, String> buildNamedEntityMap() {
        Map<String, String> m = new HashMap<>();
        m.put("lt", "<");
        m.put("gt", ">");
        m.put("amp", "&");
        m.put("quot", "\"");
        m.put("apos", "'");
        m.put("nbsp", " ");
        m.put("rsquo", "'");
        m.put("lsquo", "'");
        m.put("ldquo", "\"");
        m.put("rdquo", "\"");
        m.put("ndash", "-");
        m.put("mdash", "-");
        m.put("hellip", "...");
        m.put("copy", "(c)");
        m.put("reg", "(r)");
        m.put("trade", "(tm)");
        m.put("euro", "€");
        return m;
    }

    private TextSanitizer() {
    }

    // --- Existing helpers kept (matching/storage) ---
    public static String sanitizeForMatching(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String s = input;
        s = normalizeUnicodePunctuation(s);
        s = XML_HEADER.matcher(s).replaceFirst("");
        s = TAGS.matcher(s).replaceAll(" ");
        s = decodeHtmlEntities(s);
        s = CONTROL_CHARS.matcher(s).replaceAll(" ");
        s = WHITESPACE.matcher(s).replaceAll(" ").trim();
        s = toAscii(s);
        return s;
    }

    public static String sanitizeForStorage(String input) {
        return sanitizeForMatching(input);
    }

    public static String sanitizePatternForStorage(String pattern, String matchType) {
        if (pattern == null) {
            return "";
        }
        String p = pattern.trim();
        if (p.isEmpty()) {
            return "";
        }
        p = normalizeUnicodePunctuation(p);
        p = decodeHtmlEntities(p);
        p = CONTROL_CHARS.matcher(p).replaceAll(" ");
        p = WHITESPACE.matcher(p).replaceAll(" ").trim();
        if ("REGEX".equalsIgnoreCase(matchType)) {
            // keep regex metacharacters intact
            return p;
        } else {
            return toAscii(p);
        }
    }

    // --- New: Markdown-display sanitizer (preserve Unicode) ---
    /**
     * Sanitize Markdown text for safe HTML rendering while preserving Unicode
     * characters (smart quotes, dashes, etc.). It decodes entities, normalizes
     * punctuation, escapes raw HTML angle-brackets and ampersands so the
     * Markdown renderer displays raw tags literally unless the Markdown
     * intentionally creates HTML, and collapses whitespace.
     *
     * Use the result as input to a Markdown renderer on the client (or server),
     * then sanitize the rendered HTML with a trusted HTML sanitizer (e.g.,
     * DOMPurify).
     */
    public static String sanitizeMarkdownForDisplay(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        String s = markdown;
        // Normalize punctuation but keep Unicode characters (do not transliterate)
        s = normalizeUnicodePunctuation(s);
        // Decode entities like &amp; &nbsp; -> & and space (so Markdown text shows correctly)
        s = decodeHtmlEntities(s);

        // Null-safe guard for static analysis and defensive safety
        if (s == null) {
            return "";
        }

        // Escape literal ampersands and angle-brackets so raw HTML doesn't break display
        s = escapeHtmlChars(s);
        // Remove control chars and collapse whitespace
        s = CONTROL_CHARS.matcher(s).replaceAll(" ");
        s = WHITESPACE.matcher(s).replaceAll(" ").trim();
        return s;
    }

    // --- Helpers ---
    private static String normalizeUnicodePunctuation(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        s = s.replace('\u2018', '\'');
        s = s.replace('\u2019', '\'');
        s = s.replace('\u201A', '\'');
        s = s.replace('\u201C', '"');
        s = s.replace('\u201D', '"');
        s = s.replace('\u201E', '"');
        s = s.replace('\u2013', '-');
        s = s.replace('\u2014', '-');
        s = s.replace("\u2026", "...");
        s = s.replace('\u00A0', ' ');
        return s;
    }

    private static String decodeHtmlEntities(String s) {
        if (s == null || s.indexOf('&') == -1) {
            return s;
        }
        return HTML_ENTITY.matcher(s)
                .replaceAll(match -> Matcher.quoteReplacement(decodeEntity(match.group(1))));
    }

    private static String decodeEntity(String ent) {
        if (ent == null || ent.isEmpty()) {
            return "";
        }
        if (ent.charAt(0) == '#') {
            try {
                if (ent.length() > 1 && (ent.charAt(1) == 'x' || ent.charAt(1) == 'X')) {
                    int code = Integer.parseInt(ent.substring(2), 16);
                    if (Character.isValidCodePoint(code)) {
                        return new String(Character.toChars(code));
                    }
                    return "";
                } else {
                    int code = Integer.parseInt(ent.substring(1));
                    if (Character.isValidCodePoint(code)) {
                        return new String(Character.toChars(code));
                    }
                    return "";
                }
            } catch (IllegalArgumentException e) {
                LOG.log(Level.FINE, "Failed to decode HTML numeric entity", e);
                return "";
            }
        }
        String lower = ent.toLowerCase();
        String mapped = NAMED_ENTITY_MAP.get(lower);
        if (mapped != null) {
            return mapped;
        }
        return "";
    }

    private static String toAscii(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFKD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[^\\p{ASCII}]", "");
        return normalized;
    }

    private static String escapeHtmlChars(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '&') {
                sb.append("&amp;");
            } else if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
