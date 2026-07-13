package com.sim.chatserver.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.Database;

public final class CustomerIdentityStore {

    private static final Logger log = Logger.getLogger(CustomerIdentityStore.class.getName());
    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");

    private static final String IDENTITY_TABLE = "customer_identity";
    private static final String SESSION_TABLE = "customer_identity_session";

    private static volatile boolean initialized = false;

    private CustomerIdentityStore() {
    }

    private static void ensureInitialized() throws SQLException {
        if (initialized) {
            return;
        }
        synchronized (CustomerIdentityStore.class) {
            if (initialized) {
                return;
            }
            ensureTables();
            initialized = true;
            log.info("CustomerIdentityStore schema initialization complete.");
        }
    }

    public static void ensureTables() throws SQLException {
        try (Connection c = Database.getConnection()) {
            ensureIdentityTable(c);
            ensureSessionTable(c);

            // identity columns
            ensureColumn(c, IDENTITY_TABLE, "identity_id", "BIGSERIAL");
            ensureColumn(c, IDENTITY_TABLE, "canonical_email", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "canonical_name", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "salesforce_contact_id", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "salesforce_account_id", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "email_enc", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "phone_enc", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "title_enc", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "department_enc", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "raw_json_enc", "TEXT");
            ensureColumn(c, IDENTITY_TABLE, "confidence", "VARCHAR(32)");
            ensureColumn(c, IDENTITY_TABLE, "created_at", "TIMESTAMPTZ");
            ensureColumn(c, IDENTITY_TABLE, "updated_at", "TIMESTAMPTZ");
            ensureColumn(c, IDENTITY_TABLE, "last_synced_at", "TIMESTAMPTZ");

            // session columns
            ensureColumn(c, SESSION_TABLE, "session_id", "TEXT");
            ensureColumn(c, SESSION_TABLE, "identity_id", "BIGINT");
            ensureColumn(c, SESSION_TABLE, "display_name_snapshot", "TEXT");
            ensureColumn(c, SESSION_TABLE, "contact_email_snapshot", "TEXT");
            ensureColumn(c, SESSION_TABLE, "linked_at", "TIMESTAMPTZ");
            ensureColumn(c, SESSION_TABLE, "updated_at", "TIMESTAMPTZ");

            // LEGACY cleanup: old schema sometimes had session_id on identity table
            migrateLegacyIdentitySessionColumn(c);

            ensureNowDefault(c, IDENTITY_TABLE, "created_at");
            ensureNowDefault(c, IDENTITY_TABLE, "updated_at");
            ensureNowDefault(c, SESSION_TABLE, "linked_at");
            ensureNowDefault(c, SESSION_TABLE, "updated_at");

            ensurePrimaryKey(c, IDENTITY_TABLE, "identity_id", "pk_customer_identity");
            ensurePrimaryKey(c, SESSION_TABLE, "session_id", "pk_customer_identity_session");

            ensureForeignKey(c, SESSION_TABLE, "identity_id", IDENTITY_TABLE, "identity_id",
                    "fk_customer_identity_session_identity");

            try (PreparedStatement ps1 = c.prepareStatement("CREATE INDEX IF NOT EXISTS idx_customer_identity_email ON customer_identity (LOWER(canonical_email))");
                    PreparedStatement ps2 = c.prepareStatement("CREATE INDEX IF NOT EXISTS idx_customer_identity_name ON customer_identity (LOWER(canonical_name))");
                    PreparedStatement ps3 = c.prepareStatement("CREATE INDEX IF NOT EXISTS idx_customer_identity_session_identity ON customer_identity_session (identity_id)")) {
                ps1.executeUpdate();
                ps2.executeUpdate();
                ps3.executeUpdate();
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to ensure customer identity schema", e);
            throw e;
        }
    }

    private static void ensureIdentityTable(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
                CREATE TABLE IF NOT EXISTS customer_identity (
                    identity_id BIGSERIAL PRIMARY KEY,
                    canonical_email TEXT,
                    canonical_name TEXT,
                    salesforce_contact_id TEXT,
                    salesforce_account_id TEXT,
                    email_enc TEXT,
                    phone_enc TEXT,
                    title_enc TEXT,
                    department_enc TEXT,
                    raw_json_enc TEXT,
                    confidence VARCHAR(32) NOT NULL DEFAULT 'high',
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    last_synced_at TIMESTAMPTZ
                )
            """)) {
            ps.executeUpdate();
        }
    }

    private static void ensureSessionTable(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
                CREATE TABLE IF NOT EXISTS customer_identity_session (
                    session_id TEXT PRIMARY KEY,
                    identity_id BIGINT NOT NULL,
                    display_name_snapshot TEXT,
                    contact_email_snapshot TEXT,
                    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """)) {
            ps.executeUpdate();
        }
    }

    private static void migrateLegacyIdentitySessionColumn(Connection c) {
        try {
            if (!columnExists(c, IDENTITY_TABLE, "session_id")) {
                return;
            }

            // Drop NOT NULL so inserts without session_id no longer fail.
            try (PreparedStatement ps = c.prepareStatement("ALTER TABLE customer_identity ALTER COLUMN session_id DROP NOT NULL")) {
                ps.executeUpdate();
            } catch (SQLException ignore) {
            }

            // Optional: drop old unique/index constraints tied to session_id if they exist.
            dropConstraintIfExists(c, IDENTITY_TABLE, "uq_customer_identity_session_id");
            dropConstraintIfExists(c, IDENTITY_TABLE, "customer_identity_session_id_key");

            // We intentionally keep legacy column for compatibility instead of dropping automatically.
            // If you want to remove it later, do explicit migration.
        } catch (SQLException e) {
            log.log(Level.WARNING, "Legacy migration for customer_identity.session_id failed", e);
        }
    }

    public static CustomerIdentity findBySessionId(String sessionId) throws SQLException {
        ensureInitialized();
        if (isBlank(sessionId)) {
            return null;
        }

        String sql = """
            SELECT ci.identity_id, ci.canonical_email, ci.canonical_name,
                   ci.salesforce_contact_id, ci.salesforce_account_id,
                   ci.email_enc, ci.phone_enc, ci.title_enc, ci.department_enc, ci.raw_json_enc,
                   ci.confidence, ci.created_at, ci.updated_at, ci.last_synced_at
              FROM customer_identity ci
              JOIN customer_identity_session cis ON cis.identity_id = ci.identity_id
             WHERE cis.session_id = ?
             LIMIT 1
            """;

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sessionId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapIdentity(rs) : null;
            }
        }
    }

    public static CustomerIdentity findByCanonicalEmail(String email) throws SQLException {
        ensureInitialized();
        if (isBlank(email)) {
            return null;
        }

        String sql = """
            SELECT identity_id, canonical_email, canonical_name,
                   salesforce_contact_id, salesforce_account_id,
                   email_enc, phone_enc, title_enc, department_enc, raw_json_enc,
                   confidence, created_at, updated_at, last_synced_at
              FROM customer_identity
             WHERE LOWER(canonical_email) = LOWER(?)
             LIMIT 1
            """;

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapIdentity(rs) : null;
            }
        }
    }

    public static CustomerIdentity findByCanonicalName(String name) throws SQLException {
        ensureInitialized();
        if (isBlank(name)) {
            return null;
        }

        String sql = """
            SELECT identity_id, canonical_email, canonical_name,
                   salesforce_contact_id, salesforce_account_id,
                   email_enc, phone_enc, title_enc, department_enc, raw_json_enc,
                   confidence, created_at, updated_at, last_synced_at
              FROM customer_identity
             WHERE LOWER(canonical_name) = LOWER(?)
             ORDER BY updated_at DESC
             LIMIT 1
            """;

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapIdentity(rs) : null;
            }
        }
    }

    public static long insertIdentity(String canonicalEmail, String canonicalName, String confidence) throws SQLException {
        ensureInitialized();

        String sql = """
            INSERT INTO customer_identity (canonical_email, canonical_name, confidence)
            VALUES (?, ?, ?)
            RETURNING identity_id
            """;

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nullIfBlank(canonicalEmail));
            ps.setString(2, nullIfBlank(canonicalName));
            ps.setString(3, isBlank(confidence) ? "high" : confidence.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Failed to insert identity");
                }
                return rs.getLong(1);
            }
        }
    }

    public static void upsertSessionLink(long identityId, String sessionId, String displayName, String email) throws SQLException {
        ensureInitialized();
        if (isBlank(sessionId)) {
            throw new IllegalArgumentException("sessionId is required");
        }

        String sql = """
            INSERT INTO customer_identity_session (session_id, identity_id, display_name_snapshot, contact_email_snapshot)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (session_id) DO UPDATE SET
                identity_id = EXCLUDED.identity_id,
                display_name_snapshot = EXCLUDED.display_name_snapshot,
                contact_email_snapshot = EXCLUDED.contact_email_snapshot,
                updated_at = NOW()
            """;

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sessionId.trim());
            ps.setLong(2, identityId);
            ps.setString(3, nullIfBlank(displayName));
            ps.setString(4, nullIfBlank(email));
            ps.executeUpdate();
        }
    }

    public static List<CustomerIdentitySessionLink> listSessionLinks(long identityId) throws SQLException {
        ensureInitialized();

        String sql = """
            SELECT session_id, identity_id, display_name_snapshot, contact_email_snapshot, linked_at, updated_at
              FROM customer_identity_session
             WHERE identity_id = ?
             ORDER BY updated_at DESC
            """;

        List<CustomerIdentitySessionLink> out = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, identityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerIdentitySessionLink link = new CustomerIdentitySessionLink();
                    link.setSessionId(sanitizeDbText(rs.getString("session_id"), 256));
                    link.setIdentityId(Math.max(0L, rs.getLong("identity_id")));
                    link.setDisplayNameSnapshot(sanitizeDbText(rs.getString("display_name_snapshot"), 512));
                    link.setContactEmailSnapshot(sanitizeDbText(rs.getString("contact_email_snapshot"), 512));

                    Timestamp linkedAt = rs.getTimestamp("linked_at");
                    if (linkedAt != null) {
                        link.setLinkedAt(linkedAt.toInstant().atOffset(ZoneOffset.UTC));
                    }
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        link.setUpdatedAt(updatedAt.toInstant().atOffset(ZoneOffset.UTC));
                    }

                    out.add(link);
                }
            }
        }
        return out;
    }

    private static CustomerIdentity mapIdentity(ResultSet rs) throws SQLException {
        CustomerIdentity x = new CustomerIdentity();
        x.setIdentityId(Math.max(0L, rs.getLong("identity_id")));
        x.setCanonicalEmail(sanitizeDbText(rs.getString("canonical_email"), 512));
        x.setCanonicalName(sanitizeDbText(rs.getString("canonical_name"), 512));
        x.setSalesforceContactId(sanitizeDbText(rs.getString("salesforce_contact_id"), 256));
        x.setSalesforceAccountId(sanitizeDbText(rs.getString("salesforce_account_id"), 256));
        x.setEmail(sanitizeDbText(rs.getString("email_enc"), 4096));
        x.setPhone(sanitizeDbText(rs.getString("phone_enc"), 1024));
        x.setTitle(sanitizeDbText(rs.getString("title_enc"), 512));
        x.setDepartment(sanitizeDbText(rs.getString("department_enc"), 512));
        x.setRawJson(sanitizeDbText(rs.getString("raw_json_enc"), 20000));
        x.setConfidence(sanitizeDbText(rs.getString("confidence"), 32));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            x.setCreatedAt(created.toInstant().atOffset(ZoneOffset.UTC));
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            x.setUpdatedAt(updated.toInstant().atOffset(ZoneOffset.UTC));
        }
        Timestamp synced = rs.getTimestamp("last_synced_at");
        if (synced != null) {
            x.setLastSyncedAt(synced.toInstant().atOffset(ZoneOffset.UTC));
        }
        return x;
    }

    private static void ensureColumn(Connection conn, String table, String column, String sqlType) throws SQLException {
        if (columnExists(conn, table, column)) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE " + q(table) + " ADD COLUMN " + q(column) + " " + sqlType)) {
            ps.executeUpdate();
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    private static void ensureNowDefault(Connection conn, String table, String column) {
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE " + q(table) + " ALTER COLUMN " + q(column) + " SET DEFAULT NOW()")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to set NOW() default for {0}.{1}", new Object[]{table, column});
        }
    }

    private static void ensurePrimaryKey(Connection conn, String table, String column, String constraintName) {
        String check = """
            SELECT 1
              FROM information_schema.table_constraints
             WHERE table_name = ?
               AND constraint_type = 'PRIMARY KEY'
            """;
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to verify primary key for table {0}", table);
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE " + q(table) + " ADD CONSTRAINT " + q(constraintName)
                + " PRIMARY KEY (" + q(column) + ")")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to add primary key {0} on table {1}", new Object[]{constraintName, table});
        }
    }

    private static void ensureForeignKey(Connection conn, String sourceTable, String sourceColumn,
            String targetTable, String targetColumn, String fkName) {
        String check = """
            SELECT 1
              FROM information_schema.table_constraints
             WHERE table_name = ?
               AND constraint_name = ?
               AND constraint_type = 'FOREIGN KEY'
            """;
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, sourceTable);
            ps.setString(2, fkName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to verify foreign key {0} on table {1}", new Object[]{fkName, sourceTable});
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE " + q(sourceTable)
                + " ADD CONSTRAINT " + q(fkName)
                + " FOREIGN KEY (" + q(sourceColumn) + ") REFERENCES "
                + q(targetTable) + " (" + q(targetColumn) + ") ON DELETE CASCADE")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to add foreign key {0} on table {1}", new Object[]{fkName, sourceTable});
        }
    }

    private static void dropConstraintIfExists(Connection conn, String table, String constraint) {
        String check = """
            SELECT 1
              FROM information_schema.table_constraints
             WHERE table_name = ?
               AND constraint_name = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, table);
            ps.setString(2, constraint);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to verify constraint {0} on table {1}", new Object[]{constraint, table});
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE " + q(table) + " DROP CONSTRAINT " + q(constraint))) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to drop constraint {0} on table {1}", new Object[]{constraint, table});
        }
    }

    private static String q(String ident) {
        if (ident == null || !SQL_IDENTIFIER.matcher(ident).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return "\"" + ident + "\"";
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private static String nullIfBlank(String v) {
        return isBlank(v) ? null : v.trim();
    }

    private static String sanitizeDbText(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (maxChars <= 0 || trimmed.length() <= maxChars) {
            return trimmed;
        }
        return trimmed.substring(0, maxChars);
    }
}
