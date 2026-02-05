package com.sim.chatserver.term;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TermsStore {

    private static final Logger log = Logger.getLogger(TermsStore.class.getName());

    private static final String DEFAULT_MULTI = "Multi";
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
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS term_definition ("
                + "id SERIAL PRIMARY KEY, "
                + "name VARCHAR(255) UNIQUE NOT NULL, "
                + "description TEXT NOT NULL, "
                + "match_pattern VARCHAR(255) NOT NULL DEFAULT '', "
                + "match_type VARCHAR(50) NOT NULL DEFAULT 'WILDCARD', "
                + "system_flag BOOLEAN NOT NULL DEFAULT FALSE, "
                + "created_at TIMESTAMPTZ NOT NULL DEFAULT now()"
                + ")")) {
            stmt.execute();
        }
        ensureDefaultTerms();
    }

    private void ensureDefaultTerms() throws SQLException {
        upsertSystemTerm(DEFAULT_MULTI,
                "Matches when more than one term is found in the prompt or response.",
                ".*", "REGEX");
        upsertSystemTerm(DEFAULT_OTHER,
                "Fallback when no configured term is matched.",
                "", "OTHER");
    }

    private void upsertSystemTerm(String name, String description, String pattern, String type) throws SQLException {
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO term_definition (name, description, match_pattern, match_type, system_flag) "
                + "VALUES (?, ?, ?, ?, TRUE) "
                + "ON CONFLICT (name) DO UPDATE SET "
                + "description = EXCLUDED.description, "
                + "match_pattern = EXCLUDED.match_pattern, "
                + "match_type = EXCLUDED.match_type, "
                + "system_flag = TRUE")) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, pattern);
            ps.setString(4, type);
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
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO term_definition (name, description, match_pattern, match_type, system_flag) VALUES (?, ?, ?, ?, FALSE) RETURNING id")) {
            ps.setString(1, name.trim());
            ps.setString(2, description.trim());
            ps.setString(3, pattern == null ? "" : pattern.trim());
            ps.setString(4, type == null ? "WILDCARD" : type.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TermDefinition(rs.getLong(1), name.trim(), description.trim(), pattern, type, false);
                }
            }
        }
        return null;
    }

    public TermDefinition updateTerm(Long id, String name, String description, String pattern, String type) throws SQLException {
        if (isSystemTerm(id)) {
            return null;
        }
        try (Connection conn = dsHolder.getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE term_definition SET name = ?, description = ?, match_pattern = ?, match_type = ? WHERE id = ? RETURNING system_flag")) {
            ps.setString(1, name.trim());
            ps.setString(2, description.trim());
            ps.setString(3, pattern == null ? "" : pattern.trim());
            ps.setString(4, type == null ? "WILDCARD" : type.trim().toUpperCase());
            ps.setLong(5, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TermDefinition(id, name.trim(), description.trim(), pattern, type, rs.getBoolean("system_flag"));
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
}
