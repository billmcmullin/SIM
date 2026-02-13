package com.sim.chatserver.term;

import java.util.regex.Pattern;

/**
 * Utility to build and test stricter matching patterns for terms.
 *
 * Behavior: - If match_type == "REGEX" and a match_pattern is present, the
 * provided regex is used. We will only wrap it with word-safe lookarounds when
 * it is safe to do so (to avoid breaking complex regexes that already contain
 * inline flags/anchors/lookarounds). - If match_type == "WILDCARD" and the
 * pattern contains '*' or '?' we translate those into appropriate regex pieces
 * while preserving separator-tolerance between literal characters. - Otherwise
 * we treat the input as a literal token and build a separator-tolerant regex.
 */
public final class TermMatcher {

    // class used between literal characters (zero or more of allowed separators)
    // put '-' at the end to avoid ranges in character classes
    private static final String SEP_CLASS = "[\\s_.-]*";
    private static final String LEFT_BOUND = "(?<![\\p{L}\\p{N}])";
    private static final String RIGHT_BOUND = "(?![\\p{L}\\p{N}])";

    private TermMatcher() {
        // utility
    }

    /**
     * Build a Pattern suitable for strict matching according to term
     * definition.
     *
     * @param term term definition from DB
     * @return compiled Pattern
     */
    public static Pattern buildStrictPattern(TermDefinition term) {
        String type = term.getMatchType();
        String raw = term.getMatchPattern();
        if (raw == null || raw.trim().isEmpty()) {
            raw = term.getName() == null ? "" : term.getName().trim();
        }

        String finalRegex;
        if (type != null && "REGEX".equalsIgnoreCase(type.trim()) && raw.length() > 0) {
            // Use provided regex, but only wrap when it is safe to do so.
            if (isSafeToWrapRegex(raw)) {
                finalRegex = LEFT_BOUND + "(?:" + raw + ")" + RIGHT_BOUND;
            } else {
                // Leave the user's regex intact (they are responsible for anchors/lookarounds).
                finalRegex = raw;
            }
        } else {
            // Non-REGEX: support wildcard notation and literal/separator tolerant build.
            boolean looksLikeWildcard = raw.indexOf('*') >= 0 || raw.indexOf('?') >= 0;
            if (looksLikeWildcard) {
                finalRegex = LEFT_BOUND + buildWildcardRegex(raw) + RIGHT_BOUND;
            } else {
                finalRegex = LEFT_BOUND + buildSeparatedLiteralRegex(raw) + RIGHT_BOUND;
            }
        }

        return Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /**
     * Check if the term matches anywhere in the provided prompt text.
     *
     * @param term term definition
     * @param prompt sanitized prompt text to test
     * @return true if a match exists
     */
    public static boolean matches(TermDefinition term, String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return false;
        }
        Pattern p = buildStrictPattern(term);
        return p.matcher(prompt).find();
    }

    private static boolean isSafeToWrapRegex(String raw) {
        if (raw == null || raw.isEmpty()) {
            return true;
        }
        // Conservative checks for constructs that suggest the user controls anchors/lookarounds.
        // If present, do not add our own boundaries.
        String lowered = raw;
        if (lowered.contains("(?") // inline flags or lookaround/expression
                || lowered.contains("\\b")
                || lowered.contains("\\B")
                || lowered.contains("^")
                || lowered.contains("$")) {
            return false;
        }
        return true;
    }

    /**
     * Build a separator-tolerant regex from a literal string by inserting
     * SEP_CLASS between chars. Example: "SOATEST" -> S SEP O SEP A SEP T SEP E
     * SEP S SEP T
     */
    private static String buildSeparatedLiteralRegex(String literal) {
        StringBuilder sb = new StringBuilder(literal.length() * 4);
        String s = literal.trim();
        int len = s.codePointCount(0, s.length());
        for (int i = 0; i < len; i++) {
            int cp = s.codePointAt(s.offsetByCodePoints(0, i));
            String ch = new String(Character.toChars(cp));
            // Quote each char for regex safety
            String quoted = Pattern.quote(ch);
            // Pattern.quote wraps with \Q...\E; to avoid double-\Q we append the raw quoted
            sb.append(quoted);
            if (i < len - 1) {
                sb.append(SEP_CLASS);
            }
        }
        return sb.toString();
    }

    /**
     * Build a regex from a wildcard pattern that supports '*' => any sequence
     * and '?' => single char. Also keeps separator-tolerance between adjacent
     * literal characters.
     *
     * Example: "SOA*TEST" -> S SEP O SEP A .* T SEP E SEP S SEP T
     */
    private static String buildWildcardRegex(String pattern) {
        if (pattern == null) {
            return "";
        }
        String s = pattern.trim();
        StringBuilder sb = new StringBuilder(s.length() * 4);

        // iterate code points to support unicode properly
        int offset = 0;
        int total = s.length();
        while (offset < total) {
            int cp = s.codePointAt(offset);
            String ch = new String(Character.toChars(cp));
            offset += Character.charCount(cp);

            // wildcard tokens
            if ("*".equals(ch)) {
                // .* should be used — allow crossing separators and characters
                sb.append(".*");
                continue;
            } else if ("?".equals(ch)) {
                // single any-character
                sb.append(".");
                continue;
            }

            // treat user-specified separator characters as SEP_CLASS
            if (isSeparatorChar(ch)) {
                sb.append(SEP_CLASS);
                continue;
            }

            // literal char: append quoted char and, if next literal char follows, insert SEP_CLASS
            sb.append(Pattern.quote(ch));

            // look ahead to determine if we should insert SEP_CLASS between this literal and the next literal
            // (if the next significant token is a literal)
            int temp = offset;
            boolean nextIsLiteral = false;
            while (temp < total) {
                int nextCp = s.codePointAt(temp);
                String nextCh = new String(Character.toChars(nextCp));
                // wildcard or separator ends the "immediate literal adjacency" decision
                if ("*".equals(nextCh) || "?".equals(nextCh)) {
                    nextIsLiteral = false;
                    break;
                }
                if (isSeparatorChar(nextCh)) {
                    // separator exists in pattern; we will represent it as SEP_CLASS, so do not add an extra SEP_CLASS here
                    nextIsLiteral = false;
                    break;
                }
                // otherwise it's a literal character -> insert SEP_CLASS to allow separators between them
                nextIsLiteral = true;
                break;
            }
            if (nextIsLiteral) {
                sb.append(SEP_CLASS);
            }
        }

        return sb.toString();
    }

    private static boolean isSeparatorChar(String ch) {
        if (ch == null || ch.isEmpty()) {
            return false;
        }
        char c = ch.charAt(0);
        return c == ' ' || c == '_' || c == '.' || c == '-';
    }
}
