package com.sim.chatserver.web.dashboard.inactiveusers;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

final class InactiveUsersFrustrationTextUtil {
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
            "MISRA", "OWASP", "CWE", "CVE", "NIST", "ISO", "IEC", "SOC", "PCI", "HIPAA", "GDPR", "PII"
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

        return ((double) capsCount / (double) nonCodeCount) >= 0.60d;
    }

    private static boolean looksLikeCodeText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String value = text;
        String lower = value.toLowerCase();

        if (value.contains("```")) {
            return true;
        }

        int codeHints = 0;
        String[] keywords = {
                "select ", " from ", " where ", " join ", "insert ", "update ", "delete ",
                " function ", " class ", " public ", " private ", " protected ", " return ",
                " if(", " if (", " for(", " for (", " while(", " while ("
        };
        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                codeHints++;
            }
        }

        if (value.contains("{") || value.contains("}") || value.contains(";") || value.contains("=>") || value.contains("::")) {
            codeHints++;
        }

        int symbolCount = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ("{}[]();=<>/_\\\\|".indexOf(c) >= 0) {
                symbolCount++;
            }
        }

        double symbolRatio = value.isEmpty() ? 0.0d : ((double) symbolCount / (double) value.length());
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
        if (text.matches(".*\\b\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}.*")) {
            hints++;
        }
        if (text.matches(".*\\b\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?\\b.*")) {
            hints++;
        }
        if (text.contains(" - ") || text.contains(" | ") || text.contains("::")) {
            hints++;
        }
        if (text.contains("Exception") || text.contains("Stacktrace") || text.contains("at com.")) {
            hints++;
        }

        return hints >= 2;
    }

    private static boolean containsOnlySafeAcronymCaps(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String[] tokens = text.split("[^A-Za-z0-9_]+");
        boolean sawCapsToken = false;

        for (String token : tokens) {
            if (token == null || token.isBlank() || token.length() < 2) {
                continue;
            }

            boolean isAllCaps = token.equals(token.toUpperCase()) && token.matches("[A-Z0-9_]+");
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