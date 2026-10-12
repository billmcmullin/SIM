package com.sim.chatserver.term;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.CommonLiteralConstants;

/**
 * Utility for sanitizing prompt text retrieved from the database and for
 * preparing text for storage, matching, and display.
 *
 * Added: sanitizeMarkdownForDisplay - prepares Markdown text for safe HTML
 * rendering while preserving Unicode/smart punctuation.
 */
public final class TextSanitizer {

    private static final Logger LOG = Logger.getLogger(TextSanitizer.class.getCanonicalName());

    private static final Pattern XML_HEADER = Pattern.compile("^\\s*<\\?xml[^>]*\\?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY = Pattern.compile("&(#(x)?[0-9a-fA-F]+|[a-zA-Z]+);");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]");
    private static final Pattern WHITESPACE = Pattern.compile(CommonLiteralConstants.REGEX_WHITESPACE);

    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String REGEX_MATCH_TYPE = "REGEX";
    private static final String ENTITY_LT = "lt";
    private static final String ENTITY_GT = "gt";
    private static final String ENTITY_AMP = "amp";
    private static final String ENTITY_QUOT = "quot";
    private static final String ENTITY_APOS = "apos";
    private static final String ENTITY_NBSP = "nbsp";
    private static final String ENTITY_RSQUO = "rsquo";
    private static final String ENTITY_LSQUO = "lsquo";
    private static final String ENTITY_LDQUO = "ldquo";
    private static final String ENTITY_RDQUO = "rdquo";
    private static final String ENTITY_NDASH = "ndash";
    private static final String ENTITY_MDASH = "mdash";
    private static final String ENTITY_HELLIP = "hellip";
    private static final String ENTITY_COPY = "copy";
    private static final String ENTITY_REG = "reg";
    private static final String ENTITY_TRADE = "trade";
    private static final String ENTITY_EURO = "euro";
    private static final String VALUE_LT = "<";
    private static final String VALUE_GT = ">";
    private static final String VALUE_AMP = "&";
    private static final String VALUE_QUOTE = "\"";
    private static final String VALUE_APOS = "'";
    private static final String VALUE_DASH = "-";
    private static final String VALUE_ELLIPSIS = CommonLiteralConstants.ELLIPSIS;
    private static final String UNICODE_ELLIPSIS = "\u2026";
    private static final String VALUE_COPY = "(c)";
    private static final String VALUE_REG = "(r)";
    private static final String VALUE_TRADE = "(tm)";
    private static final String VALUE_EURO = "\u20AC";
    private static final String NUMERIC_ENTITY_DECODE_FAILED = "Failed to decode HTML numeric entity";
    private static final String REGEX_COMBINING_MARKS = "\\p{M}";
    private static final String REGEX_NON_ASCII = "[^\\p{ASCII}]";
    private static final String HTML_ESCAPE_AMP = CommonLiteralConstants.HTML_ESCAPE_AMP;
    private static final String HTML_ESCAPE_LT = CommonLiteralConstants.HTML_ESCAPE_LT;
    private static final String HTML_ESCAPE_GT = CommonLiteralConstants.HTML_ESCAPE_GT;

    private static final Map<String, String> NAMED_ENTITY_MAP = buildNamedEntityMap();

    private static Map<String, String> buildNamedEntityMap() {
        Map<String, String> m = new HashMap<>();
        m.put(ENTITY_LT, VALUE_LT);
        m.put(ENTITY_GT, VALUE_GT);
        m.put(ENTITY_AMP, VALUE_AMP);
        m.put(ENTITY_QUOT, VALUE_QUOTE);
        m.put(ENTITY_APOS, VALUE_APOS);
        m.put(ENTITY_NBSP, SPACE);
        m.put(ENTITY_RSQUO, VALUE_APOS);
        m.put(ENTITY_LSQUO, VALUE_APOS);
        m.put(ENTITY_LDQUO, VALUE_QUOTE);
        m.put(ENTITY_RDQUO, VALUE_QUOTE);
        m.put(ENTITY_NDASH, VALUE_DASH);
        m.put(ENTITY_MDASH, VALUE_DASH);
        m.put(ENTITY_HELLIP, VALUE_ELLIPSIS);
        m.put(ENTITY_COPY, VALUE_COPY);
        m.put(ENTITY_REG, VALUE_REG);
        m.put(ENTITY_TRADE, VALUE_TRADE);
        m.put(ENTITY_EURO, VALUE_EURO);
        return m;
    }

    private TextSanitizer() {
    }

    // --- Existing helpers kept (matching/storage) ---
    public static String sanitizeForMatching(String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY;
        }
        String s = input;
        s = normalizeUnicodePunctuation(s);
        s = XML_HEADER.matcher(s).replaceFirst(EMPTY);
        s = TAGS.matcher(s).replaceAll(SPACE);
        s = decodeHtmlEntities(s);
        s = CONTROL_CHARS.matcher(s).replaceAll(SPACE);
        s = WHITESPACE.matcher(s).replaceAll(SPACE).trim();
        s = toAscii(s);
        return s;
    }

    static String sanitizeForStorage(String input) {
        return sanitizeForMatching(input);
    }

    static String sanitizePatternForStorage(String pattern, String matchType) {
        if (pattern == null) {
            return EMPTY;
        }
        String p = pattern.trim();
        if (p.isEmpty()) {
            return EMPTY;
        }
        p = normalizeUnicodePunctuation(p);
        p = decodeHtmlEntities(p);
        p = CONTROL_CHARS.matcher(p).replaceAll(SPACE);
        p = WHITESPACE.matcher(p).replaceAll(SPACE).trim();
        if (REGEX_MATCH_TYPE.equalsIgnoreCase(matchType)) {
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
    static String sanitizeMarkdownForDisplay(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return EMPTY;
        }
        String s = markdown;
        // Normalize punctuation but keep Unicode characters (do not transliterate)
        s = normalizeUnicodePunctuation(s);
        // Decode entities like &amp; &nbsp; -> & and space (so Markdown text shows correctly)
        s = decodeHtmlEntities(s);

        // Null-safe guard for static analysis and defensive safety
        if (s == null) {
            return EMPTY;
        }

        // Escape literal ampersands and angle-brackets so raw HTML doesn't break display
        s = escapeHtmlChars(s);
        // Remove control chars and collapse whitespace
        s = CONTROL_CHARS.matcher(s).replaceAll(SPACE);
        s = WHITESPACE.matcher(s).replaceAll(SPACE).trim();
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
        s = s.replace(UNICODE_ELLIPSIS, VALUE_ELLIPSIS);
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
            return EMPTY;
        }
        if (ent.charAt(0) == '#') {
            try {
                if (ent.length() > 1 && (ent.charAt(1) == 'x' || ent.charAt(1) == 'X')) {
                    int code = Integer.parseInt(ent.substring(2), 16);
                    if (Character.isValidCodePoint(code)) {
                        return new String(Character.toChars(code));
                    }
                    return EMPTY;
                } else {
                    int code = Integer.parseInt(ent.substring(1));
                    if (Character.isValidCodePoint(code)) {
                        return new String(Character.toChars(code));
                    }
                    return EMPTY;
                }
            } catch (IllegalArgumentException e) {
                LOG.log(Level.FINE, NUMERIC_ENTITY_DECODE_FAILED, e);
                return EMPTY;
            }
        }
        String lower = ent.toLowerCase();
        String mapped = NAMED_ENTITY_MAP.get(lower);
        if (mapped != null) {
            return mapped;
        }
        return EMPTY;
    }

    private static String toAscii(String s) {
        if (s == null || s.isEmpty()) {
            return EMPTY;
        }
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFKD);
        normalized = normalized.replaceAll(REGEX_COMBINING_MARKS, EMPTY);
        normalized = normalized.replaceAll(REGEX_NON_ASCII, EMPTY);
        return normalized;
    }

    private static String escapeHtmlChars(String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '&') {
                sb.append(HTML_ESCAPE_AMP);
            } else if (ch == '<') {
                sb.append(HTML_ESCAPE_LT);
            } else if (ch == '>') {
                sb.append(HTML_ESCAPE_GT);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
