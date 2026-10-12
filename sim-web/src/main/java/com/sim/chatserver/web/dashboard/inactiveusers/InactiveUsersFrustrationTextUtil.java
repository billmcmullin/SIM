package com.sim.chatserver.web.dashboard.inactiveusers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

final class InactiveUsersFrustrationTextUtil {
    private static final char SEMICOLON_CHAR = (char) 59;
    private static final String CODE_BLOCK_MARKER = "```";
    private static final String CODE_SYMBOLS = "{}[]()" + SEMICOLON_CHAR + "=<>/_\\\\|";
    private static final String CODE_BLOCK_OPEN = "{";
    private static final String CODE_BLOCK_CLOSE = "}";
    private static final String LAMBDA_ARROW = "=>";
    private static final String DOUBLE_COLON = "::";
    private static final String LOG_DASH_SEPARATOR = " - ";
    private static final String LOG_PIPE_SEPARATOR = " | ";
    private static final String LOG_EXCEPTION = "Exception";
    private static final String LOG_STACKTRACE = "Stacktrace";
    private static final String LOG_AT_COM = "at com.";
    private static final Pattern LOG_DATE_TIME_PATTERN = Pattern.compile(".*\\b\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}.*");
    private static final Pattern LOG_TIME_PATTERN = Pattern.compile(".*\\b\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?\\b.*");
    private static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("[^A-Za-z0-9_]+");
    private static final Pattern ALL_CAPS_TOKEN_PATTERN = Pattern.compile("[A-Z0-9_]+");
    private static final String[] CODE_KEYWORDS = {
            "select ", " from ", " where ", " join ", "insert ", "update ", "delete ",
            " function ", " class ", " public ", " private ", " protected ", " return ",
            " if(", " if (", " for(", " for (", " while(", " while ("
    };

    private static final Pattern ALL_CAPS_WORD = Pattern.compile("\\b[A-Z]{4,}\\b");
    private static final Pattern LOGGER_TOKEN = Pattern.compile("\\b(INFO|DEBUG|TRACE|WARN|WARNING|ERROR|FATAL)\\b");
    private static final Pattern PROFANITY_PATTERN = Pattern.compile(
            "\\b(fuck|fucking|shit|bullshit|damn|wtf|crap|asshole)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FRUSTRATION_PHRASE_PATTERN = Pattern.compile(
            "\\b(not what i asked|that is not what i asked|you (didn'?t|do not) understand|wrong answer|incorrect answer|"
                    + "you (are|re) not listening|this (still )?doesn'?t work|not working|still broken|fix this|"
                    + "answer the question|stop ignoring|why is this wrong)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> SAFE_ACRONYMS = Set.of(
            "API", "SDK", "CLI", "GUI", "SQL", "JSON", "XML", "HTTP", "HTTPS", "URL", "URI", "JWT", "SSO", "SAML", "OIDC", "TLS", "SSL",
            "TCP", "UDP", "DNS", "IP", "CPU", "GPU", "RAM", "OS", "DB", "ETL", "CI", "CD", "IDE", "LTS", "JDK", "JVM",
            "MISRA", com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP, "CWE", "CVE", "NIST", "ISO", "IEC", "SOC", "PCI", "HIPAA", "GDPR", "PII"
    );

    private InactiveUsersFrustrationTextUtil() {
    }

    static boolean isNonFrustrationContext(String text) {
        return looksLikeCodeText(text) || looksLikeLogText(text) || containsOnlySafeAcronymCaps(text);
    }

    static boolean hasExplicitFrustrationSignal(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (PROFANITY_PATTERN.matcher(text).find()) {
            return true;
        }
        return FRUSTRATION_PHRASE_PATTERN.matcher(text).find();
    }

    static boolean isConsistentCapsStyle(List<String> prompts) {
        if (prompts == null || prompts.size() < 3) {
            return false;
        }

        int capsCount = 0;
        int nonCodeCount = 0;

        for (String prompt : prompts) {
            if (prompt == null || prompt.isBlank()) {
                continue;
            }
            if (isNonFrustrationContext(prompt)) {
                continue;
            }
            nonCodeCount++;
            if (ALL_CAPS_WORD.matcher(prompt).find()) {
                capsCount++;
            }
        }

        if (nonCodeCount < 3) {
            return false;
        }

        return BigDecimal.valueOf(capsCount)
                .divide(BigDecimal.valueOf(nonCodeCount), 6, RoundingMode.HALF_UP)
                .compareTo(BigDecimal.valueOf(6L, 1)) >= 0;
    }

    private static boolean looksLikeCodeText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String value = text;
        String lower = value.toLowerCase();

        if (value.contains(CODE_BLOCK_MARKER)) {
            return true;
        }

        int codeHints = 0;
        for (String keyword : CODE_KEYWORDS) {
            if (lower.contains(keyword)) {
                codeHints++;
            }
        }

        if (value.contains(CODE_BLOCK_OPEN) || value.contains(CODE_BLOCK_CLOSE)
                || value.contains(String.valueOf(SEMICOLON_CHAR))
                || value.contains(LAMBDA_ARROW) || value.contains(DOUBLE_COLON)) {
            codeHints++;
        }

        int symbolCount = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (CODE_SYMBOLS.indexOf(c) >= 0) {
                symbolCount++;
            }
        }

        double symbolRatio = value.isEmpty()
                ? 0.0d
                : BigDecimal.valueOf(symbolCount)
                        .divide(BigDecimal.valueOf(value.length()), 6, RoundingMode.HALF_UP)
                        .doubleValue();
        if (symbolRatio > 0.08d) {
            codeHints++;
        }

        return codeHints >= 2;
    }

    private static boolean looksLikeLogText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        int hints = 0;
        if (LOGGER_TOKEN.matcher(text).find()) {
            hints++;
        }
        if (LOG_DATE_TIME_PATTERN.matcher(text).matches()) {
            hints++;
        }
        if (LOG_TIME_PATTERN.matcher(text).matches()) {
            hints++;
        }
        if (text.contains(LOG_DASH_SEPARATOR) || text.contains(LOG_PIPE_SEPARATOR) || text.contains(DOUBLE_COLON)) {
            hints++;
        }
        if (text.contains(LOG_EXCEPTION) || text.contains(LOG_STACKTRACE) || text.contains(LOG_AT_COM)) {
            hints++;
        }

        return hints >= 2;
    }

    private static boolean containsOnlySafeAcronymCaps(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String[] tokens = TOKEN_SPLIT_PATTERN.split(text);
        boolean sawCapsToken = false;

        for (String token : tokens) {
            if (token == null || token.isBlank() || token.length() < 2) {
                continue;
            }

            boolean isAllCaps = token.equals(token.toUpperCase()) && ALL_CAPS_TOKEN_PATTERN.matcher(token).matches();
            if (!isAllCaps) {
                continue;
            }

            sawCapsToken = true;
            if (!SAFE_ACRONYMS.contains(token)) {
                return false;
            }
        }

        return sawCapsToken;
    }
}