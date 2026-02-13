package com.sim.chatserver.term;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TermsStore {

    private static final Logger log = Logger.getLogger(TermsStore.class.getName());

    private static final String DEFAULT_OTHER = "Other Parasoft Match";

    private AppDataSourceHolder dsHolder;

    public TermsStore() {
        // proxyable no-arg constructor
    }

    @Inject
    public void setDataSourceHolder(AppDataSourceHolder dsHolder) {
        this.dsHolder = dsHolder;
    }

    public void ensureTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS term_definition ("
                + "id SERIAL PRIMARY KEY, "
                + "name TEXT UNIQUE NOT NULL, "
                + "description TEXT NOT NULL, "
                + "match_pattern TEXT NOT NULL DEFAULT '', "
                + "match_type TEXT NOT NULL DEFAULT 'WILDCARD', "
                + "system_flag BOOLEAN NOT NULL DEFAULT FALSE, "
                + "created_at TIMESTAMPTZ NOT NULL DEFAULT now()"
                + ")";
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        }
        ensureDefaultTerms();
    }

    private void ensureDefaultTerms() throws SQLException {
        upsertSystemTerm(DEFAULT_OTHER,
                "Fallback when no configured term is matched.",
                "", "OTHER");
    }

    private void upsertSystemTerm(String name, String description, String pattern, String type) throws SQLException {
        // Sanitize inputs before storing
        final String sanitizedName = TextSanitizer.sanitizeForStorage(name);
        final String sanitizedDescription = TextSanitizer.sanitizeForStorage(description);
        final String sanitizedType = normalizeMatchType(type);
        final String sanitizedPattern = TextSanitizer.sanitizePatternForStorage(pattern, sanitizedType);

        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO term_definition (name, description, match_pattern, match_type, system_flag) "
                + "VALUES (?, ?, ?, ?, TRUE) "
                + "ON CONFLICT (name) DO UPDATE SET "
                + "description = EXCLUDED.description, "
                + "match_pattern = EXCLUDED.match_pattern, "
                + "match_type = EXCLUDED.match_type, "
                + "system_flag = TRUE")) {
            ps.setString(1, sanitizedName);
            ps.setString(2, sanitizedDescription);
            ps.setString(3, sanitizedPattern);
            ps.setString(4, sanitizedType);
            ps.executeUpdate();
        }
    }

    public List<TermDefinition> listAll() throws SQLException {
        List<TermDefinition> terms = new ArrayList<>();
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, description, match_pattern, match_type, system_flag FROM term_definition ORDER BY name ASC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                terms.add(new TermDefinition(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("match_pattern"),
                        rs.getString("match_type"),
                        rs.getBoolean("system_flag")
                ));
            }
        }
        return terms;
    }

    public TermDefinition createTerm(String name, String description, String pattern, String type) throws SQLException {
        // Sanitize inputs before storing
        final String sanitizedName = TextSanitizer.sanitizeForStorage(name);
        final String sanitizedDescription = TextSanitizer.sanitizeForStorage(description);
        final String sanitizedType = normalizeMatchType(type);
        final String sanitizedPattern = TextSanitizer.sanitizePatternForStorage(pattern, sanitizedType);

        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO term_definition (name, description, match_pattern, match_type, system_flag) VALUES (?, ?, ?, ?, FALSE) RETURNING id")) {
            ps.setString(1, sanitizedName);
            ps.setString(2, sanitizedDescription);
            ps.setString(3, sanitizedPattern);
            ps.setString(4, sanitizedType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TermDefinition(rs.getLong(1), sanitizedName, sanitizedDescription, sanitizedPattern, sanitizedType, false);
                }
            }
        }
        return null;
    }

    public TermDefinition updateTerm(Long id, String name, String description, String pattern, String type) throws SQLException {
        if (isSystemTerm(id)) {
            return null;
        }

        // Sanitize inputs before storing
        final String sanitizedName = TextSanitizer.sanitizeForStorage(name);
        final String sanitizedDescription = TextSanitizer.sanitizeForStorage(description);
        final String sanitizedType = normalizeMatchType(type);
        final String sanitizedPattern = TextSanitizer.sanitizePatternForStorage(pattern, sanitizedType);

        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE term_definition SET name = ?, description = ?, match_pattern = ?, match_type = ? WHERE id = ? RETURNING system_flag")) {
            ps.setString(1, sanitizedName);
            ps.setString(2, sanitizedDescription);
            ps.setString(3, sanitizedPattern);
            ps.setString(4, sanitizedType);
            ps.setLong(5, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TermDefinition(id, sanitizedName, sanitizedDescription, sanitizedPattern, sanitizedType, rs.getBoolean("system_flag"));
                }
            }
        }
        return null;
    }

    public boolean deleteTerm(Long id) throws SQLException {
        if (isSystemTerm(id)) {
            return false;
        }
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM term_definition WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    protected boolean isSystemTerm(Long id) throws SQLException {
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT system_flag FROM term_definition WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("system_flag");
            }
        }
    }

    /**
     * Find the TermDefinition that has the earliest occurrence within the
     * provided prompt text.
     *
     * The prompt text is sanitized to plain text before matching.
     *
     * @param prompt prompt text to test (may be null or empty)
     * @return TermDefinition with the earliest match (leftmost) or null if none
     * matched
     * @throws SQLException on db errors
     */
    public TermDefinition findFirstMatchingTermForPrompt(String prompt) throws SQLException {
        if (prompt == null || prompt.isEmpty()) {
            return null;
        }
        // Sanitize prompt text before matching
        final String sanitized = TextSanitizer.sanitizeForMatching(prompt);
        if (sanitized.isEmpty()) {
            return null;
        }

        List<TermDefinition> terms = listAll();
        TermDefinition bestTerm = null;
        int bestStart = Integer.MAX_VALUE;

        for (TermDefinition td : terms) {
            try {
                Pattern p = TermMatcher.buildStrictPattern(td);
                Matcher m = p.matcher(sanitized);
                if (m.find()) {
                    int start = m.start();
                    // prefer the leftmost occurrence
                    if (start < bestStart) {
                        bestStart = start;
                        bestTerm = td;
                        // if it's at the very start of the prompt, we can stop early
                        if (bestStart == 0) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Be defensive: log and continue to next term (do not break existing functionality)
                log.warning("Failed to test term '" + td.getName() + "': " + e.getMessage());
            }
        }
        return bestTerm;
    }

    /**
     * Given an ordered collection of prompts, examine them in the provided
     * order and return the first TermDefinition (from DB order resolved to
     * earliest-in-text occurrence within each prompt) that matches any prompt.
     *
     * Prompts are sanitized before matching.
     *
     * @param prompts iterable of prompt texts (order matters)
     * @return first matching TermDefinition or null if none found
     * @throws SQLException on db errors
     */
    public TermDefinition findFirstMatchingTermInPrompts(Iterable<String> prompts) throws SQLException {
        if (prompts == null) {
            return null;
        }
        for (String prompt : prompts) {
            if (prompt == null || prompt.isEmpty()) {
                continue;
            }
            final String sanitized = TextSanitizer.sanitizeForMatching(prompt);
            if (sanitized.isEmpty()) {
                continue;
            }
            TermDefinition found = findFirstMatchingTermForPrompt(sanitized);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // -----------------------
    // Helper sanitization methods used before storing inputs in DB
    // -----------------------
    private static String normalizeMatchType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "WILDCARD";
        }
        return type.trim().toUpperCase();
    }
}
