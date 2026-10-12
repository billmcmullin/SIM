package com.sim.chatserver.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.Database;
import com.sim.chatserver.util.CommonLiteralConstants;

public final class CustomerIdentityStore {

    private static final Logger log = Logger.getLogger(CustomerIdentityStore.class.getCanonicalName());
    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");

    private static final String IDENTITY_TABLE = "customer_identity";
    private static final String SESSION_TABLE = "customer_identity_session";
    private static final String COL_IDENTITY_ID = "identity_id";
    private static final String COL_CREATED_AT = CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String COL_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_UPDATED_AT;
    private static final String COL_SESSION_ID = CommonLiteralConstants.SQL_COL_SESSION_ID;
    private static final String COL_CANONICAL_EMAIL = "canonical_email";
    private static final String COL_CANONICAL_NAME = "canonical_name";
    private static final String COL_SF_CONTACT_ID = "salesforce_contact_id";
    private static final String COL_SF_ACCOUNT_ID = "salesforce_account_id";
    private static final String COL_EMAIL_ENC = "email_enc";
    private static final String COL_PHONE_ENC = "phone_enc";
    private static final String COL_TITLE_ENC = "title_enc";
    private static final String COL_DEPARTMENT_ENC = "department_enc";
    private static final String COL_RAW_JSON_ENC = "raw_json_enc";
    private static final String COL_CONFIDENCE = "confidence";
    private static final String COL_LAST_SYNCED_AT = "last_synced_at";
    private static final String COL_DISPLAY_NAME_SNAPSHOT = "display_name_snapshot";
    private static final String COL_CONTACT_EMAIL_SNAPSHOT = "contact_email_snapshot";
    private static final String COL_LINKED_AT = "linked_at";
    private static final String SQL_TYPE_TIMESTAMPTZ = "TIMESTAMPTZ";
    private static final String SQL_TYPE_BIGSERIAL = "BIGSERIAL";
    private static final String SQL_TYPE_BIGINT = "BIGINT";
    private static final String SQL_TYPE_TEXT = "TEXT";
    private static final String SQL_TYPE_VARCHAR_32 = "VARCHAR(32)";
    private static final String DEFAULT_CONFIDENCE_HIGH = "high";
    private static final String INVALID_SQL_IDENTIFIER = CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String EMPTY = "";
    private static final String VALUE_Z = "Z";
    private static final String REGEX_SIGNED_LONG = "^-?\\d{1,18}$";
    private static final String MSG_SCHEMA_INIT_COMPLETE = "CustomerIdentityStore schema initialization complete.";
    private static final String MSG_SCHEMA_ENSURE_FAILED = "Failed to ensure customer identity schema";
    private static final String MSG_LEGACY_MIGRATION_FAILED = "Legacy migration for customer_identity.session_id failed";
    private static final String MSG_INSERT_IDENTITY_FAILED = "Failed to insert identity";
    private static final String MSG_SESSION_ID_REQUIRED = "sessionId is required";
    private static final String MSG_NOW_DEFAULT_FAILED = "Unable to set NOW() default for {0}.{1}";
    private static final String MSG_VERIFY_PK_FAILED = "Unable to verify primary key for table {0}";
    private static final String MSG_ADD_PK_FAILED = "Unable to add primary key {0} on table {1}";
    private static final String MSG_VERIFY_FK_FAILED = "Unable to verify foreign key {0} on table {1}";
    private static final String MSG_ADD_FK_FAILED = "Unable to add foreign key {0} on table {1}";
    private static final String MSG_VERIFY_CONSTRAINT_FAILED = "Unable to verify constraint {0} on table {1}";
    private static final String MSG_DROP_CONSTRAINT_FAILED = "Unable to drop constraint {0} on table {1}";
    private static final String MSG_PARSE_LONG_FAILED_PREFIX = "Unable to parse long value for column ";
    private static final String MSG_PARSE_INSTANT_FAILED_PREFIX = "Instant parse failed for column ";
    private static final String MSG_PARSE_INSTANT_FAILED_SUFFIX = ", trying SQL timestamp parse";
    private static final String MSG_PARSE_TIMESTAMP_FAILED_PREFIX = "Unable to parse timestamp for column ";
    private static final String MSG_READ_STRING_FAILED_PREFIX = "String read failed for column ";
    private static final String SQL_ALTER_TABLE_PREFIX = "ALTER TABLE ";
    private static final String SQL_ADD_COLUMN = " ADD COLUMN ";
    private static final String SQL_ALTER_COLUMN = " ALTER COLUMN ";
    private static final String SQL_SET_DEFAULT_NOW = " SET DEFAULT NOW()";
    private static final String SQL_ADD_CONSTRAINT = " ADD CONSTRAINT ";
    private static final String SQL_PRIMARY_KEY_OPEN = " PRIMARY KEY (";
    private static final String SQL_FOREIGN_KEY_OPEN = " FOREIGN KEY (";
    private static final String SQL_OPEN_PAREN = " (";
    private static final String SQL_REFERENCES = ") REFERENCES ";
    private static final String SQL_ON_DELETE_CASCADE = ") ON DELETE CASCADE";
    private static final String SQL_DROP_CONSTRAINT = " DROP CONSTRAINT ";
    private static final String CONSTRAINT_PK_IDENTITY = "pk_customer_identity";
    private static final String CONSTRAINT_PK_SESSION = "pk_customer_identity_session";
    private static final String CONSTRAINT_FK_SESSION_IDENTITY = "fk_customer_identity_session_identity";
    private static final String CONSTRAINT_UQ_LEGACY_SESSION = "uq_customer_identity_session_id";
    private static final String CONSTRAINT_LEGACY_SESSION_KEY = "customer_identity_session_id_key";
    private static final String SQL_DROP_IDENTITY_SESSION_NOT_NULL = "ALTER TABLE customer_identity ALTER COLUMN session_id DROP NOT NULL";
    private static final String SQL_CREATE_INDEX_IDENTITY_EMAIL = "CREATE INDEX IF NOT EXISTS idx_customer_identity_email ON customer_identity (LOWER(canonical_email))";
    private static final String SQL_CREATE_INDEX_IDENTITY_NAME = "CREATE INDEX IF NOT EXISTS idx_customer_identity_name ON customer_identity (LOWER(canonical_name))";
    private static final String SQL_CREATE_INDEX_SESSION_IDENTITY = "CREATE INDEX IF NOT EXISTS idx_customer_identity_session_identity ON customer_identity_session (identity_id)";
    private static final String SQL_SELECT_SESSION_FLAG_BY_ID = "SELECT system_flag FROM term_definition WHERE id = ?";
    private static final String SQL_SELECT_SESSION_FLAG_BY_IDENTITY = "SELECT system_flag FROM customer_identity WHERE identity_id = ?";

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
            log.info(MSG_SCHEMA_INIT_COMPLETE);
        }
    }

    private static void ensureTables() throws SQLException {
        try (Connection c = Database.getConnection()) {
            ensureIdentityTable(c);
            ensureSessionTable(c);

            // identity columns
            ensureColumn(c, IDENTITY_TABLE, COL_IDENTITY_ID, SQL_TYPE_BIGSERIAL);
            ensureColumn(c, IDENTITY_TABLE, COL_CANONICAL_EMAIL, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_CANONICAL_NAME, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_SF_CONTACT_ID, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_SF_ACCOUNT_ID, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_EMAIL_ENC, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_PHONE_ENC, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_TITLE_ENC, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_DEPARTMENT_ENC, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_RAW_JSON_ENC, SQL_TYPE_TEXT);
            ensureColumn(c, IDENTITY_TABLE, COL_CONFIDENCE, SQL_TYPE_VARCHAR_32);
                ensureColumn(c, IDENTITY_TABLE, COL_CREATED_AT, SQL_TYPE_TIMESTAMPTZ);
                ensureColumn(c, IDENTITY_TABLE, COL_UPDATED_AT, SQL_TYPE_TIMESTAMPTZ);
                ensureColumn(c, IDENTITY_TABLE, COL_LAST_SYNCED_AT, SQL_TYPE_TIMESTAMPTZ);

            // session columns
                ensureColumn(c, SESSION_TABLE, COL_SESSION_ID, SQL_TYPE_TEXT);
                ensureColumn(c, SESSION_TABLE, COL_IDENTITY_ID, SQL_TYPE_BIGINT);
            ensureColumn(c, SESSION_TABLE, COL_DISPLAY_NAME_SNAPSHOT, SQL_TYPE_TEXT);
            ensureColumn(c, SESSION_TABLE, COL_CONTACT_EMAIL_SNAPSHOT, SQL_TYPE_TEXT);
                ensureColumn(c, SESSION_TABLE, COL_LINKED_AT, SQL_TYPE_TIMESTAMPTZ);
                ensureColumn(c, SESSION_TABLE, COL_UPDATED_AT, SQL_TYPE_TIMESTAMPTZ);

            // LEGACY cleanup: old schema sometimes had session_id on identity table
            migrateLegacyIdentitySessionColumn(c);

                ensureNowDefault(c, IDENTITY_TABLE, COL_CREATED_AT);
                ensureNowDefault(c, IDENTITY_TABLE, COL_UPDATED_AT);
            ensureNowDefault(c, SESSION_TABLE, COL_LINKED_AT);
                ensureNowDefault(c, SESSION_TABLE, COL_UPDATED_AT);

                ensurePrimaryKey(c, IDENTITY_TABLE, COL_IDENTITY_ID, CONSTRAINT_PK_IDENTITY);
                ensurePrimaryKey(c, SESSION_TABLE, COL_SESSION_ID, CONSTRAINT_PK_SESSION);

                ensureForeignKey(c, SESSION_TABLE, COL_IDENTITY_ID, IDENTITY_TABLE, COL_IDENTITY_ID,
                    CONSTRAINT_FK_SESSION_IDENTITY);

            try (PreparedStatement ps1 = c.prepareStatement(SQL_CREATE_INDEX_IDENTITY_EMAIL);
                    PreparedStatement ps2 = c.prepareStatement(SQL_CREATE_INDEX_IDENTITY_NAME);
                    PreparedStatement ps3 = c.prepareStatement(SQL_CREATE_INDEX_SESSION_IDENTITY)) {
                ps1.executeUpdate();
                ps2.executeUpdate();
                ps3.executeUpdate();
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_SCHEMA_ENSURE_FAILED, e);
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
            if (!columnExists(c, IDENTITY_TABLE, COL_SESSION_ID)) {
                return;
            }

            // Drop NOT NULL so inserts without session_id no longer fail.
            try (PreparedStatement ps = c.prepareStatement(SQL_DROP_IDENTITY_SESSION_NOT_NULL)) {
                ps.executeUpdate();
            } catch (SQLException ignore) {
            }

            // Optional: drop old unique/index constraints tied to session_id if they exist.
            dropConstraintIfExists(c, IDENTITY_TABLE, CONSTRAINT_UQ_LEGACY_SESSION);
            dropConstraintIfExists(c, IDENTITY_TABLE, CONSTRAINT_LEGACY_SESSION_KEY);

            // We intentionally keep legacy column for compatibility instead of dropping automatically.
            // If you want to remove it later, do explicit migration.
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_LEGACY_MIGRATION_FAILED, e);
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
            ps.setString(3, isBlank(confidence) ? DEFAULT_CONFIDENCE_HIGH : confidence.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(MSG_INSERT_IDENTITY_FAILED);
                }
                return rs.getLong(1);
            }
        }
    }

    public static void upsertSessionLink(long identityId, String sessionId, String displayName, String email) throws SQLException {
        ensureInitialized();
        if (isBlank(sessionId)) {
            throw new IllegalArgumentException(MSG_SESSION_ID_REQUIRED);
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
                    link.setSessionId(readSanitizedDbText(rs, COL_SESSION_ID, 256));
                    link.setIdentityId(readNonNegativeLongObject(rs, COL_IDENTITY_ID));
                    link.setDisplayNameSnapshot(readSanitizedDbText(rs, COL_DISPLAY_NAME_SNAPSHOT, 512));
                    link.setContactEmailSnapshot(readSanitizedDbText(rs, COL_CONTACT_EMAIL_SNAPSHOT, 512));

                    Timestamp linkedAt = readSafeTimestamp(rs, COL_LINKED_AT);
                    if (linkedAt != null) {
                        link.setLinkedAt(linkedAt.toInstant().atOffset(ZoneOffset.UTC));
                    }
                    Timestamp updatedAt = readSafeTimestamp(rs, COL_UPDATED_AT);
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
        x.setIdentityId(readNonNegativeLongObject(rs, COL_IDENTITY_ID));
        x.setCanonicalEmail(readSanitizedDbText(rs, COL_CANONICAL_EMAIL, 512));
        x.setCanonicalName(readSanitizedDbText(rs, COL_CANONICAL_NAME, 512));
        x.setSalesforceContactId(readSanitizedDbText(rs, COL_SF_CONTACT_ID, 256));
        x.setSalesforceAccountId(readSanitizedDbText(rs, COL_SF_ACCOUNT_ID, 256));
        x.setEmail(readSanitizedDbText(rs, COL_EMAIL_ENC, 4096));
        x.setPhone(readSanitizedDbText(rs, COL_PHONE_ENC, 1024));
        x.setTitle(readSanitizedDbText(rs, COL_TITLE_ENC, 512));
        x.setDepartment(readSanitizedDbText(rs, COL_DEPARTMENT_ENC, 512));
        x.setRawJson(readSanitizedDbText(rs, COL_RAW_JSON_ENC, 20000));
        x.setConfidence(readSanitizedDbText(rs, COL_CONFIDENCE, 32));

        Timestamp created = readSafeTimestamp(rs, COL_CREATED_AT);
        if (created != null) {
            x.setCreatedAt(created.toInstant().atOffset(ZoneOffset.UTC));
        }
        Timestamp updated = readSafeTimestamp(rs, COL_UPDATED_AT);
        if (updated != null) {
            x.setUpdatedAt(updated.toInstant().atOffset(ZoneOffset.UTC));
        }
        Timestamp synced = readSafeTimestamp(rs, COL_LAST_SYNCED_AT);
        if (synced != null) {
            x.setLastSyncedAt(synced.toInstant().atOffset(ZoneOffset.UTC));
        }
        return x;
    }

    private static void ensureColumn(Connection conn, String table, String column, String sqlType) throws SQLException {
        if (columnExists(conn, table, column)) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_ALTER_TABLE_PREFIX + q(table) + SQL_ADD_COLUMN + q(column) + ' ' + sqlType)) {
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
        try (PreparedStatement ps = conn.prepareStatement(SQL_ALTER_TABLE_PREFIX + q(table) + SQL_ALTER_COLUMN + q(column) + SQL_SET_DEFAULT_NOW)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_NOW_DEFAULT_FAILED, new Object[]{table, column});
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
            log.log(Level.FINE, MSG_VERIFY_PK_FAILED, table);
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_ALTER_TABLE_PREFIX + q(table) + SQL_ADD_CONSTRAINT + q(constraintName)
            + SQL_PRIMARY_KEY_OPEN + q(column) + ')')) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_ADD_PK_FAILED, new Object[]{constraintName, table});
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
            log.log(Level.FINE, MSG_VERIFY_FK_FAILED, new Object[]{fkName, sourceTable});
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_ALTER_TABLE_PREFIX + q(sourceTable)
                + SQL_ADD_CONSTRAINT + q(fkName)
                + SQL_FOREIGN_KEY_OPEN + q(sourceColumn) + SQL_REFERENCES
                + q(targetTable) + SQL_OPEN_PAREN + q(targetColumn) + SQL_ON_DELETE_CASCADE)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_ADD_FK_FAILED, new Object[]{fkName, sourceTable});
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
            log.log(Level.FINE, MSG_VERIFY_CONSTRAINT_FAILED, new Object[]{constraint, table});
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_ALTER_TABLE_PREFIX + q(table) + SQL_DROP_CONSTRAINT + q(constraint))) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_DROP_CONSTRAINT_FAILED, new Object[]{constraint, table});
        }
    }

    private static String q(String ident) {
        if (ident == null || !SQL_IDENTIFIER.matcher(ident).matches()) {
            throw new IllegalArgumentException(INVALID_SQL_IDENTIFIER);
        }
        return new StringBuilder(ident.length() + 2)
            .append('"')
            .append(ident)
            .append('"')
            .toString();
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private static String nullIfBlank(String v) {
        return isBlank(v) ? null : v.trim();
    }

    private static String readSanitizedDbText(ResultSet rs, String column, int maxChars) throws SQLException {
        return sanitizeDbText(readRawDbText(rs, column), maxChars);
    }

    private static Long readNonNegativeLongObject(ResultSet rs, String column) throws SQLException {
        String text = readSanitizedDbText(rs, column, 64);
        if (text == null || text.isBlank()) {
            return 0L;
        }

        String trimmed = text.trim();
        if (!trimmed.matches(REGEX_SIGNED_LONG)) {
            return 0L;
        }

        long value;
        try {
            value = Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_PARSE_LONG_FAILED_PREFIX + column, ex);
            return 0L;
        }
        return validateTaintedLong(value);
    }

    private static Timestamp readSafeTimestamp(ResultSet rs, String column) throws SQLException {
        String text = readSanitizedDbText(rs, column, 128);
        if (text == null || text.isEmpty()) {
            return null;
        }

        try {
            return Timestamp.from(Instant.parse(text));
        } catch (DateTimeException ex) {
            log.log(Level.FINE, MSG_PARSE_INSTANT_FAILED_PREFIX + column + MSG_PARSE_INSTANT_FAILED_SUFFIX, ex);
        }

        try {
            String legacy = text.endsWith(VALUE_Z) ? text.substring(0, text.length() - 1) : text;
            return Timestamp.valueOf(legacy.replace('T', ' '));
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, MSG_PARSE_TIMESTAMP_FAILED_PREFIX + column, ex);
            return null;
        }
    }

    private static String sanitizeDbText(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        String trimmed = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (maxChars <= 0 || trimmed.length() <= maxChars) {
            return trimmed;
        }
        return new String(trimmed.toCharArray(), 0, maxChars);
    }

    private static String readRawDbText(ResultSet rs, String column) {
        try {
            String raw = java.util.Objects.requireNonNullElse(rs.getString(column), EMPTY);
            return sanitizeDbText(raw, 4096);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_READ_STRING_FAILED_PREFIX + column, ex);
        }
        return null;
    }

    private static Long validateTaintedLong(long value) {
        return value < 0L ? 0L : value;
    }


}
