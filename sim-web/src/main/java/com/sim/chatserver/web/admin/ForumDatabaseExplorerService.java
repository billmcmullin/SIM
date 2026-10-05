package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.security.email.EmailSecretCrypto;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin-only read/search explorer for external forum databases.
 *
 * Supported entities: users, conversations, messages.
 * Supported engines: PostgreSQL, MySQL.
 *
 * This endpoint only runs SELECT queries and marks the JDBC connection read-only.
 */
final class ForumDatabaseExplorerService {

    private static final Logger LOG = Logger.getLogger(ForumDatabaseExplorerService.class.getCanonicalName());

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]{1,253}$");
    private static final Pattern DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,128}$");
    private static final Pattern USER_PATTERN = Pattern.compile("^[^\\u0000\\r\\n]{1,128}$");

    private static final int MAX_PASSWORD_LEN = 1024;
    private static final int MAX_SEARCH_LEN = 256;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final int DEFAULT_REPLY_LIMIT = 200;
    private static final int MAX_REPLY_LIMIT = 500;
    private static final int MAX_OFFSET = 50_000;
    private static final int MAX_CELL_TEXT_LEN = 4_000;
    private static final int MAX_SHARE_TO_LEN = 4_000;
    private static final int MAX_SHARE_SUBJECT_LEN = 512;
    private static final int CONFIG_ROW_ID = 1;
    private static final String CONFIG_TABLE = "forum_db_explorer_config";

    private static final List<String> ENTITY_ORDER = List.of("messages", "conversations", "users");
    private static final Set<String> ALLOWED_ENTITIES = Set.copyOf(ENTITY_ORDER);

    private static final Map<String, List<String>> ENTITY_TABLE_ALIASES = Map.of(
            "users", List.of("users", "user", "user_account", "user_accounts"),
            "conversations", List.of("conversations", "conversation", "chat_threads", "threads", "chats", "sessions"),
            "messages", List.of("messages", "message", "chat_messages", "posts", "forum_messages", "chat_history", "widget_chat"));

        private static final List<String> RELAXED_MESSAGE_NAME_TOKENS = List.of(
            "message", "messages", "chat", "forum", "post", "thread", "conversation", "widget");
        private static final List<String> RELAXED_MESSAGE_TEXT_COLUMNS = List.of(
            "prompt", "message", "body", "content", "text", "question", "user_message", "user_text", "post_text", "input", "query");
        private static final List<String> RELAXED_RESPONSE_COLUMNS = List.of(
            "response_text", "response", "assistant_response", "reply", "answer", "bot_response");
        private static final List<String> RELAXED_ACTOR_COLUMNS = List.of(
            "username", "user", "author", "sender", "sender_name", "created_by", "full_name", "name");
        private static final List<String> RELAXED_CONVERSATION_COLUMNS = List.of(
            "session_id", "conversation_id", "thread_id", "chat_id", "dialog_id", "topic_id", "widget_chat_id");
        private static final List<String> RELAXED_CREATED_AT_COLUMNS = List.of(
            "created_at", "created", "createdon", "timestamp", "sent_at", "message_time", "time", "date", "event_time");

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";

    void handlePost(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return;
        }

        String action = normalizeParam(ServletRequestParamUtil.firstParam(req, "action", 16, true, true));
        if (action == null) {
            action = "query";
        }

        if ("loadConfig".equalsIgnoreCase(action)) {
            handleLoadConfig(resp);
            return;
        }
        if ("saveConfig".equalsIgnoreCase(action)) {
            handleSaveConfig(req, resp);
            return;
        }

        DbType dbType = DbType.from(ServletRequestParamUtil.firstParam(req, "dbType", 32, true, true));
        if (dbType == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "dbType must be 'postgresql' or 'mysql'.");
            return;
        }

        String host = normalizeParam(ServletRequestParamUtil.firstParam(req, "host", 300, true, true));
        String database = normalizeParam(ServletRequestParamUtil.firstParam(req, "database", 128, true, true));
        String username = normalizeParam(ServletRequestParamUtil.firstParam(req, "username", 128, true, true));
        String password = normalizeParam(ServletRequestParamUtil.firstParam(req, "password", MAX_PASSWORD_LEN, true, true));
        String portRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "port", 8, true, true));

        if (!isValidHost(host)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "A valid host is required.");
            return;
        }
        if (!isValidDatabaseName(database)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "A valid database name is required (letters, digits, underscore, hyphen).");
            return;
        }
        if (!isValidUsername(username)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "A valid username is required.");
            return;
        }
        if (password == null || password.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "A database password is required.");
            return;
        }

        int port;
        try {
            port = parseIntWithBounds(portRaw, 1, 65535, "port");
        } catch (IllegalArgumentException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }

        try (Connection connection = openExternalConnection(dbType, host, port, database, username, password)) {
            connection.setReadOnly(true);

            if ("connect".equalsIgnoreCase(action)) {
                writeConnectResponse(resp, dbType, connection);
                return;
            }

            if ("threadReplies".equalsIgnoreCase(action)) {
                handleThreadReplies(req, resp, connection, dbType);
                return;
            }

            if (!"query".equalsIgnoreCase(action)) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsupported action.");
                return;
            }

            String entity = normalizeParam(ServletRequestParamUtil.firstParam(req, "entity", 64, true, true));
            String search = normalizeParam(ServletRequestParamUtil.firstParam(req, "search", MAX_SEARCH_LEN, true, true));

            int limit = DEFAULT_LIMIT;
            String limitRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "limit", 8, true, true));
            if (limitRaw != null) {
                limit = parseIntWithBounds(limitRaw, 1, MAX_LIMIT, "limit");
            }

            int offset = 0;
            String offsetRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "offset", 16, true, true));
            if (offsetRaw != null) {
                offset = parseIntWithBounds(offsetRaw, 0, MAX_OFFSET, "offset");
            }

            QueryResult result = runReadOnlyQuery(connection, dbType, entity, search, limit, offset);

            JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("dbType", dbType.id)
                    .add("entity", result.entity)
                    .add("table", result.tableName)
                    .add("limit", result.limit)
                    .add("offset", result.offset)
                    .add("returned", result.returned)
                    .add("total", result.total)
                    .add("columns", result.columns)
                    .add("rows", result.rows);
            addOptionalString(payloadBuilder, "messageTextColumn", result.messageTextColumn);
            addOptionalString(payloadBuilder, "responseTextColumn", result.responseTextColumn);
            addOptionalString(payloadBuilder, "actorColumn", result.actorColumn);
            addOptionalString(payloadBuilder, "roleColumn", result.roleColumn);
            addOptionalString(payloadBuilder, "conversationColumn", result.conversationColumn);
            addOptionalString(payloadBuilder, "createdAtColumn", result.createdAtColumn);
            JsonObject payload = payloadBuilder.build();
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (IllegalArgumentException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, safeErrorMessage(ex));
        } catch (ForumExplorerDataAccessException ex) {
            LOG.log(Level.WARNING, "Forum DB explorer query failed", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Unable to read from database: " + safeErrorMessage(ex));
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Forum DB explorer query failed", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Unable to read from database: " + safeErrorMessage(ex));
        }
    }

    private void handleLoadConfig(HttpServletResponse resp) {
        try (Connection conn = openAppConnection()) {
            ensureConfigTable(conn);
            SavedConfig config = loadSavedConfig(conn);

            JsonObjectBuilder payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "Configuration loaded.");
            addSavedConfigJson(payload, config);
            writeJson(resp, HttpServletResponse.SC_OK, payload.build());
        } catch (ForumExplorerDataAccessException ex) {
            LOG.log(Level.WARNING, "Unable to load forum explorer configuration", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load forum explorer configuration.");
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Unable to load forum explorer configuration", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load forum explorer configuration.");
        }
    }

    private void handleSaveConfig(HttpServletRequest req, HttpServletResponse resp) {
        String dbTypeRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "dbType", 32, true, true));
        DbType dbType = DbType.from(dbTypeRaw);
        if (dbTypeRaw != null && dbType == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "dbType must be 'postgresql' or 'mysql'.");
            return;
        }

        String host = normalizeParam(ServletRequestParamUtil.firstParam(req, "host", 300, true, true));
        String database = normalizeParam(ServletRequestParamUtil.firstParam(req, "database", 128, true, true));
        String username = normalizeParam(ServletRequestParamUtil.firstParam(req, "username", 128, true, true));
        String password = normalizeParam(ServletRequestParamUtil.firstParam(req, "password", MAX_PASSWORD_LEN, true, true));
        String portRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "port", 8, true, true));
        String readableOnlyRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "readableOnly", 8, true, true));
        String shareTo = normalizeMultilineParam(ServletRequestParamUtil.firstParam(req, "shareTo", MAX_SHARE_TO_LEN, true, true));
        String shareSubject = normalizeParam(ServletRequestParamUtil.firstParam(req, "shareSubject", MAX_SHARE_SUBJECT_LEN, true, true));

        if (host != null && !isValidHost(host)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Host is invalid.");
            return;
        }
        if (database != null && !isValidDatabaseName(database)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Database name is invalid (letters, digits, underscore, hyphen).");
            return;
        }
        if (username != null && !isValidUsername(username)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username is invalid.");
            return;
        }

        Integer port = null;
        if (portRaw != null) {
            try {
                port = Integer.valueOf(parseIntWithBounds(portRaw, 1, 65535, "port"));
            } catch (IllegalArgumentException ex) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                return;
            }
        }

        boolean readableOnly = parseBooleanParam(readableOnlyRaw, true);
        String updatedBy = resolveUpdatedBy(req);

        try (Connection conn = openAppConnection()) {
            ensureConfigTable(conn);
            SavedConfig existing = loadSavedConfig(conn);

            String savedPasswordEnc = existing == null ? null : existing.passwordEnc;
            if (password != null && !password.isBlank()) {
                savedPasswordEnc = encryptSecret(password);
            }

            SavedConfig toSave = new SavedConfig(
                    dbType == null ? null : dbType.id,
                    host,
                    port,
                    database,
                    username,
                    savedPasswordEnc,
                    readableOnly,
                    shareTo,
                    shareSubject,
                    updatedBy,
                    Timestamp.from(Instant.now())
            );

            upsertSavedConfig(conn, toSave);
            SavedConfig saved = loadSavedConfig(conn);

            JsonObjectBuilder payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "Configuration saved.");
            addSavedConfigJson(payload, saved);
            writeJson(resp, HttpServletResponse.SC_OK, payload.build());
        } catch (ForumExplorerDataAccessException ex) {
            LOG.log(Level.WARNING, "Unable to save forum explorer configuration", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save forum explorer configuration.");
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Unable to save forum explorer configuration", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save forum explorer configuration.");
        }
    }

    private Connection openExternalConnection(DbType dbType,
            String host,
            int port,
            String database,
            String username,
            String password) {
        try {
            DataSource dataSource = dbType.externalDataSource(host, port, database, username, password);
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw dataAccess("Unable to connect to external forum database.", ex);
        }
    }

    private void ensureConfigTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS " + CONFIG_TABLE + " ("
                + "id INTEGER PRIMARY KEY, "
                + "db_type VARCHAR(16), "
                + "host VARCHAR(253), "
                + "port INTEGER, "
                + "database_name VARCHAR(128), "
                + "username VARCHAR(128), "
                + "password_enc TEXT, "
                + "readable_only BOOLEAN NOT NULL DEFAULT TRUE, "
                + "share_to TEXT, "
                + "share_subject VARCHAR(512), "
                + "updated_by VARCHAR(128), "
                + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException ex) {
            throw dataAccess("Unable to initialize forum explorer configuration table.", ex);
        }
    }

    private SavedConfig loadSavedConfig(Connection conn) {
        String sql = "SELECT db_type, host, port, database_name, username, password_enc, readable_only, share_to, share_subject, "
                + "updated_by, updated_at FROM " + CONFIG_TABLE + " WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, CONFIG_ROW_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String dbType = normalizeParam(rs.getString("db_type"));
                String host = normalizeParam(rs.getString("host"));
                Integer port = readNullableInt(rs, "port");
                String database = normalizeParam(rs.getString("database_name"));
                String username = normalizeParam(rs.getString("username"));
                String passwordEnc = normalizeParam(rs.getString("password_enc"));
                boolean readableOnly = rs.getBoolean("readable_only");
                String shareTo = normalizeMultilineParam(rs.getString("share_to"));
                String shareSubject = normalizeParam(rs.getString("share_subject"));
                String updatedBy = normalizeParam(rs.getString("updated_by"));
                Timestamp updatedAt = rs.getTimestamp("updated_at");

                return new SavedConfig(
                        dbType,
                        host,
                        port,
                        database,
                        username,
                        passwordEnc,
                        readableOnly,
                        shareTo,
                        shareSubject,
                        updatedBy,
                        updatedAt
                );
            }
        } catch (SQLException ex) {
            throw dataAccess("Unable to load saved forum explorer configuration.", ex);
        }
    }

    private void upsertSavedConfig(Connection conn, SavedConfig config) {
        String updateSql = "UPDATE " + CONFIG_TABLE
                + " SET db_type = ?, host = ?, port = ?, database_name = ?, username = ?, password_enc = ?, readable_only = ?, "
                + "share_to = ?, share_subject = ?, updated_by = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            bindSavedConfig(ps, config);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                return;
            }
        } catch (SQLException ex) {
            throw dataAccess("Unable to update saved forum explorer configuration.", ex);
        }

        String insertSql = "INSERT INTO " + CONFIG_TABLE
                + " (db_type, host, port, database_name, username, password_enc, readable_only, share_to, share_subject, updated_by, updated_at, id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            bindSavedConfig(ps, config);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw dataAccess("Unable to save forum explorer configuration.", ex);
        }
    }

    private void bindSavedConfig(PreparedStatement ps, SavedConfig config) {
        try {
            ps.setString(1, config.dbType);
            ps.setString(2, config.host);
            if (config.port == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, config.port.intValue());
            }
            ps.setString(4, config.database);
            ps.setString(5, config.username);
            ps.setString(6, config.passwordEnc);
            ps.setBoolean(7, config.readableOnly);
            ps.setString(8, config.shareTo);
            ps.setString(9, config.shareSubject);
            ps.setString(10, config.updatedBy);
            ps.setTimestamp(11, config.updatedAt);
            ps.setInt(12, CONFIG_ROW_ID);
        } catch (SQLException ex) {
            throw dataAccess("Unable to bind forum explorer configuration statement.", ex);
        }
    }

    private void addSavedConfigJson(JsonObjectBuilder payload, SavedConfig config) {
        if (payload == null) {
            return;
        }

        if (config == null) {
            payload.add("dbType", "postgresql")
                    .add("host", "")
                    .add("port", "")
                    .add("database", "")
                    .add("username", "")
                    .add("passwordStored", false)
                    .add("readableOnly", true)
                    .add("shareTo", "")
                    .add("shareSubject", "Cleaned Forum Human Message")
                    .add("updatedBy", "")
                    .add("updatedAt", "");
            return;
        }

        payload.add("dbType", config.dbType == null ? "postgresql" : config.dbType)
                .add("host", config.host == null ? "" : config.host)
                .add("port", config.port == null ? "" : String.valueOf(config.port.intValue()))
                .add("database", config.database == null ? "" : config.database)
                .add("username", config.username == null ? "" : config.username)
                .add("passwordStored", config.passwordEnc != null && !config.passwordEnc.isBlank())
                .add("readableOnly", config.readableOnly)
                .add("shareTo", config.shareTo == null ? "" : config.shareTo)
                .add("shareSubject", config.shareSubject == null ? "Cleaned Forum Human Message" : config.shareSubject)
                .add("updatedBy", config.updatedBy == null ? "" : config.updatedBy)
                .add("updatedAt", config.updatedAt == null ? "" : config.updatedAt.toInstant().toString());
    }

    private Connection openAppConnection() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException ex) {
            throw dataAccess("Unable to open application database connection.", ex);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private static Integer readNullableInt(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            if (rs.wasNull()) {
                return null;
            }
            return Integer.valueOf(value);
        } catch (SQLException ex) {
            throw dataAccess("Unable to read numeric column: " + column + '.', ex);
        }
    }

    private static boolean parseBooleanParam(String raw, boolean fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "on".equals(lower)) {
            return true;
        }
        if ("false".equals(lower) || "0".equals(lower) || "no".equals(lower) || "off".equals(lower)) {
            return false;
        }
        return fallback;
    }

    private static String resolveUpdatedBy(HttpServletRequest req) {
        if (req == null) {
            return "unknown";
        }
        HttpSession session = req.getSession(false);
        if (session == null) {
            return "unknown";
        }
        Object user = session.getAttribute(SESSION_USER);
        if (user == null) {
            return "unknown";
        }
        String normalized = normalizeParam(String.valueOf(user));
        return normalized == null ? "unknown" : normalized;
    }

    private static String encryptSecret(String plainText) {
        return EmailSecretCrypto.encrypt(plainText == null ? "" : plainText);
    }

    private void writeConnectResponse(HttpServletResponse resp, DbType dbType, Connection connection) {
        JsonArrayBuilder available = Json.createArrayBuilder();
        JsonObjectBuilder resolvedTables = Json.createObjectBuilder();
        String preferredEntity = null;
        for (String entity : ENTITY_ORDER) {
            String resolvedTable = findTableName(connection, entity);
            if (resolvedTable != null) {
                available.add(entity);
                resolvedTables.add(entity, resolvedTable);
                if (preferredEntity == null) {
                    preferredEntity = entity;
                }
            }
        }

        JsonObjectBuilder payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("dbType", dbType.id)
                .add("message", "Connection successful.")
                .add("availableEntities", available)
                .add("resolvedTables", resolvedTables);

        if (preferredEntity != null) {
            payload.add("preferredEntity", preferredEntity);
        }

        writeJson(resp, HttpServletResponse.SC_OK, payload.build());
    }

    private QueryResult runReadOnlyQuery(Connection connection,
            DbType dbType,
            String entity,
            String search,
            int limit,
            int offset) {
        String normalizedEntity = entity == null ? "" : entity.toLowerCase(Locale.ROOT);
        if (!ALLOWED_ENTITIES.contains(normalizedEntity)) {
            throw new IllegalArgumentException("entity must be one of: users, conversations, messages.");
        }

        if ("messages".equals(normalizedEntity)) {
            QueryResult gdnThreadResult = tryRunGdnForumThreadQuery(connection, dbType, search, limit, offset);
            if (gdnThreadResult != null) {
                return gdnThreadResult;
            }
        }

        String tableName = findTableName(connection, normalizedEntity);
        if (tableName == null) {
            throw new IllegalArgumentException("Table not found in selected database for entity: " + normalizedEntity);
        }

        QueryResult primary = runReadOnlyQueryForTable(connection, dbType, normalizedEntity, tableName, search, limit, offset);

        if ("messages".equals(normalizedEntity) && primary.total <= 0) {
            String alternate = findAlternativeMessageTableWithRows(connection, dbType, tableName);
            if (alternate != null && !alternate.equalsIgnoreCase(tableName)) {
                QueryResult alternateResult = runReadOnlyQueryForTable(connection, dbType, normalizedEntity, alternate, search, limit, offset);
                if (alternateResult.total > 0) {
                    LOG.log(Level.INFO, "Forum DB explorer switched messages table from {0} to {1} because primary table had no rows.",
                            new Object[]{tableName, alternate});
                    return alternateResult;
                }
            }
        }

        return primary;
    }

    private QueryResult runReadOnlyQueryForTable(Connection connection,
            DbType dbType,
            String normalizedEntity,
            String tableName,
            String search,
            int limit,
            int offset) {

        TableInfo tableInfo = loadTableInfo(connection, dbType, tableName);
        if (tableInfo.columns.isEmpty()) {
            throw new IllegalArgumentException("Selected table has no readable columns.");
        }

        String trimmedSearch = search == null ? "" : search.trim();
        boolean hasSearch = !trimmedSearch.isEmpty() && !tableInfo.searchableColumns.isEmpty();
        ColumnHints hints = detectColumnHints(tableInfo.columns);

        String quotedTable = quoteIdentifier(dbType, tableName);
        String whereClause = buildWhereClause(dbType, tableInfo.searchableColumns, hasSearch);

        String orderColumn = tableInfo.columns.contains("id") ? "id" : tableInfo.columns.get(0);

        String dataSql = "SELECT "
                + joinQuotedColumns(dbType, tableInfo.columns)
                + " FROM " + quotedTable
                + whereClause
                + " ORDER BY " + quoteIdentifier(dbType, orderColumn) + " DESC LIMIT ? OFFSET ?";

        try {
            JsonArrayBuilder rows = Json.createArrayBuilder();
            int returned = 0;
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                int nextParam = bindSearchParams(statement, tableInfo.searchableColumns, trimmedSearch, hasSearch, dbType);
                statement.setInt(nextParam++, limit);
                statement.setInt(nextParam, offset);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        JsonObjectBuilder row = Json.createObjectBuilder();
                        for (String column : tableInfo.columns) {
                            addJsonCell(row, column, rs.getObject(column));
                        }
                        rows.add(row);
                        returned++;
                    }
                }
            }

            String countSql = "SELECT COUNT(*) FROM " + quotedTable + whereClause;
            int total = 0;
            try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
                bindSearchParams(countStmt, tableInfo.searchableColumns, trimmedSearch, hasSearch, dbType);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }

            JsonArrayBuilder columns = Json.createArrayBuilder();
            for (String column : tableInfo.columns) {
                columns.add(column);
            }

            return new QueryResult(
                    normalizedEntity,
                    tableName,
                    limit,
                    offset,
                    returned,
                    total,
                    columns,
                    rows,
                    hints.messageTextColumn,
                    hints.responseTextColumn,
                    hints.actorColumn,
                    hints.roleColumn,
                    hints.conversationColumn,
                    hints.createdAtColumn
            );
        } catch (SQLException ex) {
            throw dataAccess("Unable to query table: " + tableName + '.', ex);
        }
    }

    private void handleThreadReplies(HttpServletRequest req,
            HttpServletResponse resp,
            Connection connection,
            DbType dbType) {
        String discussionId = normalizeParam(ServletRequestParamUtil.firstParam(req, "discussionId", 128, true, true));
        if (discussionId == null) {
            discussionId = normalizeParam(ServletRequestParamUtil.firstParam(req, "conversationId", 128, true, true));
        }
        if (discussionId == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "discussionId is required.");
            return;
        }

        int limit = DEFAULT_REPLY_LIMIT;
        String limitRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, "limit", 8, true, true));
        if (limitRaw != null) {
            limit = parseIntWithBounds(limitRaw, 1, MAX_REPLY_LIMIT, "limit");
        }

        try {
            String discussionTable = findNamedTableIgnoreCase(connection, "GDN_Discussion");
            String commentTable = findNamedTableIgnoreCase(connection, "GDN_Comment");
            String userTable = findNamedTableIgnoreCase(connection, "GDN_User");
            if (discussionTable == null || commentTable == null) {
                JsonObject payload = Json.createObjectBuilder()
                        .add("status", "ok")
                        .add("replyCount", 0)
                        .add("replies", Json.createArrayBuilder())
                        .build();
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            TableInfo discussionInfo = loadTableInfo(connection, dbType, discussionTable);
            TableInfo commentInfo = loadTableInfo(connection, dbType, commentTable);
            TableInfo userInfo = userTable == null ? null : loadTableInfo(connection, dbType, userTable);
            String discussionIdCol = detectFirstColumn(discussionInfo.columns,
                List.of("discussion_id", "discussionid", "id"));
            String commentBodyCol = detectFirstColumn(commentInfo.columns,
                    List.of("body", "comment", "message", "content", "text"));
            String commentDiscussionIdCol = detectFirstColumn(commentInfo.columns,
                List.of("discussion_id", "discussionid", "conversation_id", "conversationid"));
            String userIdCol = userInfo == null ? null
                : detectFirstColumn(userInfo.columns, List.of("user_id", "userid", "id"));
            String userNameCol = userInfo == null ? null
                : detectFirstColumn(userInfo.columns, List.of("name", "username", "full_name", "display_name"));
            if (discussionIdCol == null || commentBodyCol == null || commentDiscussionIdCol == null) {
                JsonObject payload = Json.createObjectBuilder()
                        .add("status", "ok")
                        .add("replyCount", 0)
                        .add("replies", Json.createArrayBuilder())
                        .build();
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

        String commentActorCol = detectFirstColumn(commentInfo.columns,
                List.of("insert_user_id", "insertuserid", "user_id", "userid", "created_by", "author_id", "sender_id"));
        String commentCreatedAtCol = detectFirstColumn(commentInfo.columns,
                List.of("date_inserted", "dateinserted", "created_at", "createdon", "created", "timestamp", "inserted_at"));
        String commentIdCol = detectFirstColumn(commentInfo.columns,
                List.of("comment_id", "commentid", "id"));

        String quotedDiscussionTable = quoteIdentifier(dbType, discussionTable);
        String quotedCommentTable = quoteIdentifier(dbType, commentTable);
        String actorExpr = commentActorCol == null ? "NULL" : dbType.castToTextExpression(quoteQualified(dbType, "cm", commentActorCol));
        String userJoinSql = "";
        if (commentActorCol != null && userTable != null && userIdCol != null && userNameCol != null) {
            String commentActorTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "cm", commentActorCol));
            String userIdTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "u", userIdCol));
            String userNameTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "u", userNameCol));
            actorExpr = "COALESCE(NULLIF(TRIM(" + userNameTextExpr + "),''), " + commentActorTextExpr + ')';
            userJoinSql = " LEFT JOIN " + quoteIdentifier(dbType, userTable) + " u ON "
                + commentActorTextExpr + " = " + userIdTextExpr + ' ';
        }
        String createdExpr = commentCreatedAtCol == null ? "NULL" : quoteQualified(dbType, "cm", commentCreatedAtCol);
        String bodyExpr = quoteQualified(dbType, "cm", commentBodyCol);

        String orderExpr = commentIdCol != null
            ? quoteQualified(dbType, "cm", commentIdCol)
            : (commentCreatedAtCol != null ? quoteQualified(dbType, "cm", commentCreatedAtCol) : bodyExpr);

        String sql = "SELECT "
                + actorExpr + " AS actor, "
                + "'reply' AS role, "
                + createdExpr + " AS created_at, "
                + bodyExpr + " AS message_text "
                + "FROM " + quotedCommentTable + " cm "
                + "JOIN " + quotedDiscussionTable + " d ON "
                + quoteQualified(dbType, "cm", commentDiscussionIdCol) + " = " + quoteQualified(dbType, "d", discussionIdCol) + ' '
                + userJoinSql
                + "WHERE " + quoteQualified(dbType, "d", discussionIdCol) + " = ? "
                + "ORDER BY " + orderExpr + " ASC "
                + "LIMIT ?";

        JsonArrayBuilder replies = Json.createArrayBuilder();
        int count = 0;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, discussionId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    addJsonCell(row, "actor", rs.getObject("actor"));
                    addJsonCell(row, "role", rs.getObject("role"));
                    addJsonCell(row, "created_at", rs.getObject("created_at"));
                    addJsonCell(row, "message_text", rs.getObject("message_text"));
                    replies.add(row);
                    count++;
                }
            }
        }

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("replyCount", count)
                    .add("replies", replies)
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException ex) {
            throw dataAccess("Unable to query forum thread replies.", ex);
        }
    }

    private QueryResult tryRunGdnForumThreadQuery(Connection connection,
            DbType dbType,
            String search,
            int limit,
            int offset) {
        String discussionTable = findNamedTableIgnoreCase(connection, "GDN_Discussion");
        String commentTable = findNamedTableIgnoreCase(connection, "GDN_Comment");
        String userTable = findNamedTableIgnoreCase(connection, "GDN_User");
        String tagDiscussionTable = findNamedTableIgnoreCase(connection, "GDN_TagDiscussion");
        String tagTable = findNamedTableIgnoreCase(connection, "GDN_Tag");

        String statusTable = findNamedTableIgnoreCase(connection, "GDN_RecordStatus");
        if (statusTable == null) {
            statusTable = findNamedTableIgnoreCase(connection, "recordStatus");
        }
        if (statusTable == null) {
            statusTable = findNamedTableIgnoreCase(connection, "GDN_Status");
        }

        if (discussionTable == null) {
            return null;
        }

        TableInfo discussionInfo = loadTableInfo(connection, dbType, discussionTable);
        TableInfo userInfo = userTable == null ? null : loadTableInfo(connection, dbType, userTable);
        TableInfo tagDiscussionInfo = tagDiscussionTable == null ? null : loadTableInfo(connection, dbType, tagDiscussionTable);
        TableInfo tagInfo = tagTable == null ? null : loadTableInfo(connection, dbType, tagTable);
        TableInfo statusInfo = statusTable == null ? null : loadTableInfo(connection, dbType, statusTable);

        String discussionIdCol = detectFirstColumn(discussionInfo.columns,
            List.of("discussion_id", "discussionid", "id"));
        String subjectCol = detectFirstColumn(discussionInfo.columns,
            List.of("name", "title", "subject"));
        String discussionBodyCol = detectFirstColumn(discussionInfo.columns,
                List.of("body", "message", "content", "text"));
        String discussionActorCol = detectFirstColumn(discussionInfo.columns,
                List.of("insert_user_id", "insertuserid", "user_id", "userid", "created_by", "author_id", "sender_id"));
        String discussionCreatedAtCol = detectFirstColumn(discussionInfo.columns,
                List.of("date_inserted", "dateinserted", "created_at", "createdon", "created", "timestamp", "inserted_at"));
        String discussionStatusIdCol = detectFirstColumn(discussionInfo.columns,
            List.of("status_id", "statusid", "record_status_id", "recordstatusid"));
        String discussionQnaCol = detectFirstColumn(discussionInfo.columns,
            List.of("qna", "question_status", "qa_status"));

        String tagDiscussionIdCol = tagDiscussionInfo == null ? null : detectFirstColumn(tagDiscussionInfo.columns,
            List.of("discussion_id", "discussionid", "record_id", "recordid"));
        String tagDiscussionTagIdCol = tagDiscussionInfo == null ? null : detectFirstColumn(tagDiscussionInfo.columns,
            List.of("tag_id", "tagid"));
        String tagIdCol = tagInfo == null ? null : detectFirstColumn(tagInfo.columns,
            List.of("tag_id", "tagid", "id"));
        String tagNameCol = tagInfo == null ? null : detectFirstColumn(tagInfo.columns,
            List.of("fullname", "name", "tag"));

        String statusIdCol = statusInfo == null ? null : detectFirstColumn(statusInfo.columns,
            List.of("status_id", "statusid", "record_status_id", "recordstatusid", "id"));
        String statusNameCol = statusInfo == null ? null : detectFirstColumn(statusInfo.columns,
            List.of("name", "label", "status"));
        String userIdCol = userInfo == null ? null
            : detectFirstColumn(userInfo.columns, List.of("user_id", "userid", "id"));
        String userNameCol = userInfo == null ? null
            : detectFirstColumn(userInfo.columns, List.of("name", "username", "full_name", "display_name"));

        if (discussionIdCol == null || discussionBodyCol == null) {
            return null;
        }

        String quotedDiscussionTable = quoteIdentifier(dbType, discussionTable);
        String userJoinSql = "";
        String statusJoinSql = "";

        String dDiscussionIdExpr = quoteQualified(dbType, "d", discussionIdCol);
        String dSubjectExpr = subjectCol == null ? "NULL" : quoteQualified(dbType, "d", subjectCol);
        String dBodyExpr = quoteQualified(dbType, "d", discussionBodyCol);
        String dActorExpr = discussionActorCol == null ? "NULL" : dbType.castToTextExpression(quoteQualified(dbType, "d", discussionActorCol));
        String dStatusIdExpr = discussionStatusIdCol == null ? "NULL" : quoteQualified(dbType, "d", discussionStatusIdCol);
        String dQnaExpr = discussionQnaCol == null ? "NULL" : quoteQualified(dbType, "d", discussionQnaCol);
        String statusNameExpr = "NULL";
        if (discussionActorCol != null && userTable != null && userIdCol != null && userNameCol != null) {
            String discussionActorTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "d", discussionActorCol));
            String userIdTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "u", userIdCol));
            String userNameTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "u", userNameCol));
            dActorExpr = "COALESCE(NULLIF(TRIM(" + userNameTextExpr + "),''), " + discussionActorTextExpr + ')';
            userJoinSql = " LEFT JOIN " + quoteIdentifier(dbType, userTable) + " u ON "
                + discussionActorTextExpr + " = " + userIdTextExpr + ' ';
        }

        if (statusTable != null
            && discussionStatusIdCol != null
            && statusIdCol != null
            && statusNameCol != null) {
            String discussionStatusTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "d", discussionStatusIdCol));
            String statusIdTextExpr = dbType.castToTextExpression(quoteQualified(dbType, "rs", statusIdCol));
            statusJoinSql = " LEFT JOIN " + quoteIdentifier(dbType, statusTable) + " rs ON "
                + discussionStatusTextExpr + " = " + statusIdTextExpr + ' ';
            statusNameExpr = quoteQualified(dbType, "rs", statusNameCol);
        }

        String tagNamesExpr = "NULL";
        if (tagDiscussionTable != null
            && tagTable != null
            && tagDiscussionIdCol != null
            && tagDiscussionTagIdCol != null
            && tagIdCol != null
            && tagNameCol != null) {
            tagNamesExpr = buildTagNamesExpression(
                dbType,
                tagDiscussionTable,
                tagTable,
                tagDiscussionIdCol,
                tagDiscussionTagIdCol,
                tagIdCol,
                tagNameCol,
                dDiscussionIdExpr
            );
        }

        String dCreatedExpr = discussionCreatedAtCol == null ? "NULL" : quoteQualified(dbType, "d", discussionCreatedAtCol);

        String originalSelect = "SELECT "
            + dDiscussionIdExpr + " AS message_id, "
            + dDiscussionIdExpr + " AS conversation_id, "
            + dSubjectExpr + " AS subject, "
            + dActorExpr + " AS actor, "
                + "'user' AS role, "
            + dCreatedExpr + " AS created_at, "
            + dBodyExpr + " AS message_text, "
                + "NULL AS response_text, "
                + dStatusIdExpr + " AS status_id, "
                + statusNameExpr + " AS status_name, "
                + dQnaExpr + " AS qna_status, "
                + tagNamesExpr + " AS tag_names, "
                + dDiscussionIdExpr + " AS chain_key "
                + "FROM " + quotedDiscussionTable + " d"
                + userJoinSql
                + statusJoinSql;

        String wrappedFrom = " FROM (" + originalSelect + ") forum_rows";
        String trimmedSearch = search == null ? "" : search.trim();
        boolean hasSearch = !trimmedSearch.isEmpty();
        String searchWhere = hasSearch
                ? " WHERE ("
                        + "LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "subject")) + ",'')) LIKE ?"
                        + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "conversation_id")) + ",'')) LIKE ?"
                        + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "actor")) + ",'')) LIKE ?"
                        + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "message_text")) + ",'')) LIKE ?"
                    + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "message_id")) + ",'')) LIKE ?"
                        + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "status_name")) + ",'')) LIKE ?"
                        + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "qna_status")) + ",'')) LIKE ?"
                        + " OR LOWER(COALESCE(" + dbType.castToTextExpression(quoteIdentifier(dbType, "tag_names")) + ",'')) LIKE ?"
                        + ')'
                : "";

        try {
            String countSql = "SELECT COUNT(*)" + wrappedFrom + searchWhere;
            int total = 0;
            try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
                if (hasSearch) {
                    bindLikeParams(countStmt, 1, trimmedSearch, 8);
                }
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }

        String qnaTextExpr = dbType.castToTextExpression(quoteIdentifier(dbType, "qna_status"));
        String statusNameTextExpr = dbType.castToTextExpression(quoteIdentifier(dbType, "status_name"));
        String tagNamesTextExpr = dbType.castToTextExpression(quoteIdentifier(dbType, "tag_names"));
        String communityConfirmedExpr = "CASE "
                + "WHEN LOWER(COALESCE(" + qnaTextExpr + ",'')) IN ('answered','accepted') THEN 1 "
                + "WHEN LOWER(COALESCE(" + statusNameTextExpr + ",'')) IN ('answered','accepted') THEN 1 "
                + "WHEN LOWER(COALESCE(" + tagNamesTextExpr + ",'')) LIKE '%answered%' THEN 1 "
                + "WHEN LOWER(COALESCE(" + tagNamesTextExpr + ",'')) LIKE '%accepted%' THEN 1 "
                + "ELSE 0 END AS " + quoteIdentifier(dbType, "community_confirmed");

        String dataSql = "SELECT "
            + quoteIdentifier(dbType, "message_id") + ','
                + quoteIdentifier(dbType, "conversation_id") + ','
                + quoteIdentifier(dbType, "subject") + ','
                + quoteIdentifier(dbType, "actor") + ','
                + quoteIdentifier(dbType, "role") + ','
                + quoteIdentifier(dbType, "created_at") + ','
                + quoteIdentifier(dbType, "message_text") + ','
                + quoteIdentifier(dbType, "response_text") + ','
                + quoteIdentifier(dbType, "status_id") + ','
                + quoteIdentifier(dbType, "status_name") + ','
                + quoteIdentifier(dbType, "qna_status") + ','
                + quoteIdentifier(dbType, "tag_names") + ','
                + quoteIdentifier(dbType, "chain_key") + ','
                + communityConfirmedExpr
                + wrappedFrom
                + searchWhere
                + " ORDER BY " + quoteIdentifier(dbType, "created_at") + " DESC"
                + " LIMIT ? OFFSET ?";

            JsonArrayBuilder rows = Json.createArrayBuilder();
            int returned = 0;
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                int idx = 1;
                if (hasSearch) {
                    idx = bindLikeParams(statement, idx, trimmedSearch, 8);
                }
                statement.setInt(idx++, limit);
                statement.setInt(idx, offset);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        JsonObjectBuilder row = Json.createObjectBuilder();
                        addJsonCell(row, "message_id", rs.getObject("message_id"));
                        addJsonCell(row, "conversation_id", rs.getObject("conversation_id"));
                        addJsonCell(row, "subject", rs.getObject("subject"));
                        addJsonCell(row, "actor", rs.getObject("actor"));
                        addJsonCell(row, "role", rs.getObject("role"));
                        addJsonCell(row, "created_at", rs.getObject("created_at"));
                        addJsonCell(row, "message_text", rs.getObject("message_text"));
                        addJsonCell(row, "response_text", rs.getObject("response_text"));
                        addJsonCell(row, "status_id", rs.getObject("status_id"));
                        addJsonCell(row, "status_name", rs.getObject("status_name"));
                        addJsonCell(row, "qna_status", rs.getObject("qna_status"));
                        addJsonCell(row, "tag_names", rs.getObject("tag_names"));
                        addJsonCell(row, "chain_key", rs.getObject("chain_key"));
                        addJsonCell(row, "community_confirmed", rs.getObject("community_confirmed"));
                        rows.add(row);
                        returned++;
                    }
                }
            }

            JsonArrayBuilder columns = Json.createArrayBuilder();
            columns.add("message_id");
            columns.add("conversation_id");
            columns.add("subject");
            columns.add("actor");
            columns.add("role");
            columns.add("created_at");
            columns.add("message_text");
            columns.add("response_text");
            columns.add("status_id");
            columns.add("status_name");
            columns.add("qna_status");
            columns.add("tag_names");
            columns.add("chain_key");
            columns.add("community_confirmed");

            return new QueryResult(
                    "messages",
                        joinTablesForProjection(discussionTable, commentTable, userTable, statusTable, tagDiscussionTable, tagTable),
                    limit,
                    offset,
                    returned,
                    total,
                    columns,
                    rows,
                    "message_text",
                    "response_text",
                    "actor",
                    "role",
                    "conversation_id",
                    "created_at"
            );
        } catch (SQLException ex) {
            throw dataAccess("Unable to run discussion thread projection query.", ex);
        }
    }

    private String joinTablesForProjection(String... tableNames) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                if (tableName == null || tableName.isBlank()) {
                    continue;
                }
                ordered.add(tableName);
            }
        }
        if (ordered.isEmpty()) {
            return "";
        }
        return String.join("+", ordered);
    }

    private String buildTagNamesExpression(DbType dbType,
            String tagDiscussionTable,
            String tagTable,
            String tagDiscussionIdCol,
            String tagDiscussionTagIdCol,
            String tagIdCol,
            String tagNameCol,
            String discussionIdExpr) {
        String tdDiscussionExpr = quoteQualified(dbType, "td", tagDiscussionIdCol);
        String tdTagIdExpr = quoteQualified(dbType, "td", tagDiscussionTagIdCol);
        String tgTagIdExpr = quoteQualified(dbType, "tg", tagIdCol);
        String tgNameExpr = dbType.castToTextExpression(quoteQualified(dbType, "tg", tagNameCol));

        String aggregate;
        if (dbType == DbType.MYSQL) {
            aggregate = "GROUP_CONCAT(DISTINCT " + tgNameExpr + " ORDER BY " + tgNameExpr + " SEPARATOR ', ')";
        } else {
            aggregate = "STRING_AGG(DISTINCT " + tgNameExpr + ", ', ')";
        }

        return "(SELECT "
                + aggregate
                + " FROM " + quoteIdentifier(dbType, tagDiscussionTable) + " td"
                + " JOIN " + quoteIdentifier(dbType, tagTable) + " tg ON " + tdTagIdExpr + " = " + tgTagIdExpr
                + " WHERE " + tdDiscussionExpr + " = " + discussionIdExpr
                + ')';
    }

    private static int bindLikeParams(PreparedStatement statement, int startIndex, String rawSearch, int count) {
        int idx = startIndex;
        String likeValue = '%' + (rawSearch == null ? "" : rawSearch.toLowerCase(Locale.ROOT)) + '%';
        try {
            for (int i = 0; i < count; i++) {
                statement.setString(idx++, likeValue);
            }
        } catch (SQLException ex) {
            throw dataAccess("Unable to bind search parameters.", ex);
        }
        return idx;
    }

    private String quoteQualified(DbType dbType, String alias, String column) {
        return alias + '.' + quoteIdentifier(dbType, column);
    }

    private String findNamedTableIgnoreCase(Connection connection, String wantedTableName) {
        if (wantedTableName == null || wantedTableName.isBlank()) {
            return null;
        }

        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();

            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add("public");
            schemaCandidates.add(null);

            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        if (tableName != null && tableName.equalsIgnoreCase(wantedTableName)) {
                            return tableName;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw dataAccess("Unable to locate table: " + wantedTableName + '.', ex);
        }

        return null;
    }

    private String findAlternativeMessageTableWithRows(Connection connection,
            DbType dbType,
            String currentTable) {
        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();

            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add("public");
            schemaCandidates.add(null);

            String bestTable = null;
            int bestScore = Integer.MIN_VALUE;

            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        if (tableName == null || tableName.isBlank()) {
                            continue;
                        }
                        if (currentTable != null && tableName.equalsIgnoreCase(currentTable)) {
                            continue;
                        }

                        List<String> columns = loadTableColumns(md, catalog, schema, tableName);
                        int score = scoreRelaxedMessageTable(tableName, columns);
                        if (score < 120) {
                            continue;
                        }
                        if (!tableHasAtLeastOneRow(connection, dbType, tableName)) {
                            continue;
                        }

                        if (bestTable == null || score > bestScore) {
                            bestTable = tableName;
                            bestScore = score;
                        }
                    }
                }
            }

            return bestTable;
        } catch (SQLException ex) {
            throw dataAccess("Unable to find alternate messages table.", ex);
        }
    }

    private boolean tableHasAtLeastOneRow(Connection connection, DbType dbType, String tableName) {
        String sql = "SELECT 1 FROM " + quoteIdentifier(dbType, tableName) + " LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "Skipping candidate table without readable rows: " + tableName, ex);
            return false;
        }
    }

    private static ColumnHints detectColumnHints(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return new ColumnHints(null, null, null, null, null, null);
        }

        String messageTextColumn = detectFirstColumn(columns,
                List.of("prompt", "message", "body", "content", "text", "question", "user_message", "user_text", "post_text"));
        String responseTextColumn = detectFirstColumn(columns,
                List.of("response_text", "response", "assistant_response", "reply", "answer", "bot_response"));
        String actorColumn = detectFirstColumn(columns,
            List.of(
                "username",
                "user_name",
                "display_name",
                "author_name",
                "sender_name",
                "full_name",
                "insert_user_id",
                "insertuserid",
                "user_id",
                "author_id",
                "sender_id",
                "created_by",
                "created_by_user",
                "owner_id",
                "user",
                "author",
                "sender"
            ));
        String roleColumn = detectFirstColumn(columns,
                List.of("role", "sender_type", "author_type", "message_type", "origin", "source", "direction"));
        String conversationColumn = detectFirstColumn(columns,
            List.of(
                "session_id",
                "sessionid",
                "session",
                "conversation_id",
                "conversationid",
                "conversation",
                "ai_conversation_id",
                "aiconversationid",
                "thread_id",
                "threadid",
                "thread",
                "chat_id",
                "chatid",
                "chat",
                "dialog_id",
                "dialogid",
                "dialog",
                "discussion_id",
                "discussionid",
                "discussion",
                "topic_id",
                "topicid",
                "topic",
                "widget_chat_id",
                "widgetchatid",
                "conversation_key",
                "session_key"
            ));
        String createdAtColumn = detectFirstColumn(columns,
            List.of(
                "date_inserted",
                "dateinserted",
                "created_at",
                "created_on",
                "created",
                "createdon",
                "createdat",
                "timestamp",
                "sent_at",
                "message_time",
                "event_time",
                "posted_at",
                "inserted_at",
                "inserted",
                "time",
                "date"
            ));

        if (messageTextColumn == null) {
            messageTextColumn = detectFallbackMessageColumn(columns);
        }

        return new ColumnHints(
                messageTextColumn,
                responseTextColumn,
                actorColumn,
                roleColumn,
                conversationColumn,
                createdAtColumn
        );
    }

    private static String detectFirstColumn(List<String> columns, List<String> candidates) {
        if (columns == null || columns.isEmpty() || candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (String candidate : candidates) {
            for (String col : columns) {
                if (col != null && col.equalsIgnoreCase(candidate)) {
                    return col;
                }
            }
        }

        for (String candidate : candidates) {
            String normalizedCandidate = normalizeNameToken(candidate);
            for (String col : columns) {
                if (col == null) {
                    continue;
                }
                String normalizedColumn = normalizeNameToken(col);
                if (!normalizedColumn.isEmpty() && normalizedColumn.contains(normalizedCandidate)) {
                    return col;
                }
            }
        }

        return null;
    }

    private static String detectFallbackMessageColumn(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        for (String col : columns) {
            if (col == null || col.isBlank()) {
                continue;
            }
            String normalized = normalizeNameToken(col);
            if (normalized.endsWith("id") || normalized.equals("id") || normalized.contains("time") || normalized.contains("date")) {
                continue;
            }
            return col;
        }
        return null;
    }

    private int bindSearchParams(PreparedStatement statement,
            List<String> searchableColumns,
            String search,
            boolean hasSearch,
            DbType dbType) {
        int idx = 1;
        if (!hasSearch) {
            return idx;
        }
        String likeValue = '%' + search.toLowerCase(Locale.ROOT) + '%';
        int columnCount = searchableColumns.size();
        try {
            for (int i = 0; i < columnCount; i++) {
                statement.setString(idx++, likeValue);
            }
        } catch (SQLException ex) {
            throw dataAccess("Unable to bind table search parameters.", ex);
        }
        return idx;
    }

    private String buildWhereClause(DbType dbType, List<String> searchableColumns, boolean hasSearch) {
        if (!hasSearch) {
            return "";
        }

        StringBuilder where = new StringBuilder(" WHERE (");
        for (int i = 0; i < searchableColumns.size(); i++) {
            if (i > 0) {
                where.append(" OR ");
            }
            String quoted = quoteIdentifier(dbType, searchableColumns.get(i));
            where.append("LOWER(COALESCE(")
                    .append(dbType.castToTextExpression(quoted))
                    .append(",'')) LIKE ?");
        }
        where.append(')');
        return where.toString();
    }

    private TableInfo loadTableInfo(Connection connection, DbType dbType, String tableName) {
        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();
            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add("public");
            schemaCandidates.add(null);

            List<ColumnTypeInfo> typedColumns = new ArrayList<>();
            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getColumns(catalog, schema, tableName, "%")) {
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        if (columnName == null || columnName.isBlank()) {
                            continue;
                        }
                        int columnType = rs.getInt("DATA_TYPE");
                        String typeName = rs.getString("TYPE_NAME");
                        int ordinal = rs.getInt("ORDINAL_POSITION");
                        typedColumns.add(new ColumnTypeInfo(columnName, columnType, typeName, ordinal));
                    }
                }
                if (!typedColumns.isEmpty()) {
                    break;
                }
            }

            typedColumns.sort((left, right) -> Integer.compare(left.ordinalPosition, right.ordinalPosition));
            List<String> columns = new ArrayList<>();
            List<String> searchableColumns = new ArrayList<>();
            for (ColumnTypeInfo column : typedColumns) {
                columns.add(column.name);
                if (isTextLikeColumn(column.sqlType, column.sqlTypeName)) {
                    searchableColumns.add(column.name);
                }
            }
            return new TableInfo(columns, searchableColumns);
        } catch (SQLException ex) {
            throw dataAccess("Unable to inspect table metadata for: " + tableName + '.', ex);
        }
    }

    private String joinQuotedColumns(DbType dbType, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(quoteIdentifier(dbType, columns.get(i)));
        }
        return sb.toString();
    }

    private static boolean isTextLikeColumn(int sqlType, String sqlTypeName) {
        if (sqlType == Types.CHAR
                || sqlType == Types.VARCHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.NVARCHAR
                || sqlType == Types.LONGNVARCHAR
                || sqlType == Types.CLOB) {
            return true;
        }

        if (sqlTypeName == null) {
            return false;
        }
        String type = sqlTypeName.toLowerCase(Locale.ROOT);
        return type.contains("text") || type.contains("char") || type.contains("json") || type.contains("xml")
                || type.contains("uuid");
    }

    private String findTableName(Connection connection, String wantedEntity) {
        if (wantedEntity == null || wantedEntity.isBlank()) {
            return null;
        }
        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();
            List<String> probes = entityProbes(wantedEntity);

            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add("public");
            schemaCandidates.add(null);

            if ("messages".equalsIgnoreCase(wantedEntity)) {
                String relaxedMessageTable = findRelaxedMessagesTable(md, catalog, schemaCandidates);
                if (relaxedMessageTable != null) {
                    return relaxedMessageTable;
                }
            }

            for (String schema : schemaCandidates) {
                String exact = findTableNameInSchema(md, catalog, schema, probes);
                if (exact != null) {
                    return exact;
                }
            }
            return null;
        } catch (SQLException ex) {
            throw dataAccess("Unable to resolve table for entity: " + wantedEntity + '.', ex);
        }
    }

    private String findTableNameInSchema(DatabaseMetaData md,
            String catalog,
            String schema,
            List<String> probes) {
        try {
            for (String probe : probes) {
                try (ResultSet rs = md.getTables(catalog, schema, probe, new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        if (tableName != null && matchesProbe(tableName, probe)) {
                            return tableName;
                        }
                    }
                }
            }

            // Fallback: find closest table name containing one of the probe tokens.
            Map<String, Integer> candidates = new LinkedHashMap<>();
            try (ResultSet rs = md.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName == null || tableName.isBlank()) {
                        continue;
                    }
                    int score = scoreTableCandidate(tableName, probes);
                    if (score > 0) {
                        candidates.put(tableName, score);
                    }
                }
            }

            String bestTable = null;
            int bestScore = 0;
            for (Map.Entry<String, Integer> entry : candidates.entrySet()) {
                if (entry.getValue().intValue() > bestScore) {
                    bestScore = entry.getValue().intValue();
                    bestTable = entry.getKey();
                }
            }

            if (bestScore > 0) {
                return bestTable;
            }

            return null;
        } catch (SQLException ex) {
            throw dataAccess("Unable to resolve table in schema.", ex);
        }
    }

    private String findRelaxedMessagesTable(DatabaseMetaData md,
            String catalog,
            Set<String> schemaCandidates) {
        try {
            String bestTable = null;
            int bestScore = 0;

            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        if (tableName == null || tableName.isBlank()) {
                            continue;
                        }

                        List<String> columns = loadTableColumns(md, catalog, schema, tableName);
                        int score = scoreRelaxedMessageTable(tableName, columns);
                        if (score > bestScore) {
                            bestScore = score;
                            bestTable = tableName;
                        }
                    }
                }
            }

            return bestScore >= 120 ? bestTable : null;
        } catch (SQLException ex) {
            throw dataAccess("Unable to find relaxed messages table candidate.", ex);
        }
    }

    private static List<String> loadTableColumns(DatabaseMetaData md,
            String catalog,
            String schema,
            String tableName) {
        List<String> columns = new ArrayList<>();
        try (ResultSet rs = md.getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if (col == null || col.isBlank()) {
                    continue;
                }
                columns.add(col);
            }
        } catch (SQLException ex) {
            throw dataAccess("Unable to inspect columns for table: " + tableName + '.', ex);
        }
        return columns;
    }

    private static int scoreRelaxedMessageTable(String tableName, List<String> columns) {
        if (tableName == null || tableName.isBlank() || columns == null || columns.isEmpty()) {
            return 0;
        }

        String messageTextCol = detectFirstColumn(columns, RELAXED_MESSAGE_TEXT_COLUMNS);
        String responseCol = detectFirstColumn(columns, RELAXED_RESPONSE_COLUMNS);
        String actorCol = detectFirstColumn(columns, RELAXED_ACTOR_COLUMNS);
        String conversationCol = detectFirstColumn(columns, RELAXED_CONVERSATION_COLUMNS);
        String createdAtCol = detectFirstColumn(columns, RELAXED_CREATED_AT_COLUMNS);

        if (messageTextCol == null && responseCol == null) {
            return 0;
        }

        int score = 0;
        String normalizedTable = normalizeNameToken(tableName);

        for (String token : RELAXED_MESSAGE_NAME_TOKENS) {
            String normalizedToken = normalizeNameToken(token);
            if (!normalizedToken.isEmpty() && normalizedTable.contains(normalizedToken)) {
                score += 16;
            }
        }

        if (messageTextCol != null) {
            score += 90;
        }
        if (responseCol != null) {
            score += 45;
        }
        if (actorCol != null) {
            score += 30;
        }
        if (conversationCol != null) {
            score += 35;
        }
        if (createdAtCol != null) {
            score += 35;
        }

        if (messageTextCol != null && responseCol != null) {
            score += 25;
        }

        if (normalizedTable.contains("archive")
                || normalizedTable.contains("backup")
                || normalizedTable.contains("audit")
                || normalizedTable.contains("history")) {
            score -= 40;
        }

        return score;
    }

    private static List<String> entityProbes(String entity) {
        String normalized = entity == null ? "" : entity.toLowerCase(Locale.ROOT);
        List<String> aliases = ENTITY_TABLE_ALIASES.getOrDefault(normalized, List.of(normalized));
        List<String> probes = new ArrayList<>();
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                continue;
            }
            if (!probes.contains(alias)) {
                probes.add(alias);
            }
            String upper = alias.toUpperCase(Locale.ROOT);
            if (!probes.contains(upper)) {
                probes.add(upper);
            }
        }
        return probes;
    }

    private static boolean matchesProbe(String tableName, String probe) {
        return tableName.equalsIgnoreCase(probe);
    }

    private static int scoreTableCandidate(String tableName, List<String> probes) {
        String normalizedTable = normalizeNameToken(tableName);
        if (normalizedTable.isEmpty()) {
            return 0;
        }

        int best = 0;
        for (String probe : probes) {
            String normalizedProbe = normalizeNameToken(probe);
            if (normalizedProbe.isEmpty()) {
                continue;
            }

            if (normalizedTable.equals(normalizedProbe)) {
                return 100;
            }
            if (normalizedTable.startsWith(normalizedProbe)) {
                best = Math.max(best, 80);
            }
            if (normalizedTable.contains(normalizedProbe)) {
                best = Math.max(best, 60);
            }
        }
        return best;
    }

    private static String normalizeNameToken(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = Character.toLowerCase(raw.charAt(i));
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String quoteIdentifier(DbType dbType, String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Invalid identifier.");
        }
        if (dbType == DbType.MYSQL) {
            return '`' + identifier.replace("`", "``") + '`';
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private static String normalizeParam(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace("\u0000", "")
                .replace(System.lineSeparator(), "")
                .replace(System.lineSeparator(), "")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeMultilineParam(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace("\u0000", "")
                .replace(System.lineSeparator(), "")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean isValidHost(String host) {
        return host != null && HOST_PATTERN.matcher(host).matches();
    }

    private static boolean isValidDatabaseName(String database) {
        return database != null && DATABASE_PATTERN.matcher(database).matches();
    }

    private static boolean isValidUsername(String username) {
        return username != null && USER_PATTERN.matcher(username).matches();
    }

    private static int parseIntWithBounds(String rawValue, int min, int max, String fieldName) {
        if (rawValue == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            int parsed = Integer.parseInt(rawValue);
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + '.');
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.", ex);
        }
    }

    private static ForumExplorerDataAccessException dataAccess(String message, SQLException ex) {
        return new ForumExplorerDataAccessException(message, ex);
    }

    private static boolean isAdmin(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? "" : String.valueOf(roleObj);
        return "ADMIN".equalsIgnoreCase(role);
    }

    private static void addJsonCell(JsonObjectBuilder row, String column, Object value) {
        if (value == null) {
            row.addNull(column);
            return;
        }
        if (value instanceof Boolean boolVal) {
            row.add(column, boolVal.booleanValue());
            return;
        }
        if (value instanceof byte[] bytes) {
            row.add(column, Base64.getEncoder().encodeToString(bytes));
            return;
        }
        if (value instanceof TemporalAccessor) {
            row.add(column, truncateCellValue(String.valueOf(value)));
            return;
        }
        row.add(column, truncateCellValue(String.valueOf(value)));
    }

    private static String truncateCellValue(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\u0000", "");
        if (normalized.length() > MAX_CELL_TEXT_LEN) {
            return safeSlice(normalized, 0, MAX_CELL_TEXT_LEN);
        }
        return normalized;
    }

    private static String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return "Unknown error";
        }
        String msg = throwable.getMessage()
                .replace('\u0000', ' ')
                .replace(System.lineSeparator().charAt(0), ' ')
                .replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ')
                .trim();
        if (msg.length() > 300) {
            return safeSlice(msg, 0, 300);
        }
        return msg;
    }

    private static String safeSlice(String value, int beginIndex, int endIndex) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int safeBegin = Math.max(0, beginIndex);
        int safeEnd = Math.max(safeBegin, Math.min(endIndex, value.length()));
        int length = safeEnd - safeBegin;
        if (length <= 0) {
            return "";
        }
        char[] chars = value.toCharArray();
        return new String(chars, safeBegin, length);
    }

    private static void addOptionalString(JsonObjectBuilder builder, String key, String value) {
        if (builder == null || key == null || key.isBlank()) {
            return;
        }
        if (value == null || value.isBlank()) {
            builder.addNull(key);
            return;
        }
        builder.add(key, value);
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException ioe) {
            LOG.log(Level.FINE, "Unable to write explorer error payload", ioe);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message);
                } catch (IOException ignored) {
                    LOG.log(Level.FINE, "Fallback sendError failed", ignored);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ioe) {
            LOG.log(Level.FINE, "Unable to write explorer JSON payload", ioe);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
                } catch (IOException ignored) {
                    LOG.log(Level.FINE, "Fallback sendError failed", ignored);
                }
            }
        }
    }

    private enum DbType {
        POSTGRESQL("postgresql"),
        MYSQL("mysql");

        private final String id;

        DbType(String id) {
            this.id = id;
        }

        private static DbType from(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if ("postgres".equals(normalized) || "postgresql".equals(normalized)) {
                return POSTGRESQL;
            }
            if ("mysql".equals(normalized)) {
                return MYSQL;
            }
            return null;
        }

        private DataSource externalDataSource(String host, int port, String database, String username, String password) {
            if (this == MYSQL) {
                MysqlDataSource ds = new MysqlDataSource();
                ds.setServerName(host);
                ds.setPort(port);
                ds.setDatabaseName(database);
                ds.setUser(username);
                ds.setPassword(password);
                return ds;
            }

            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerNames(new String[]{host});
            ds.setPortNumbers(new int[]{port});
            ds.setDatabaseName(database);
            ds.setUser(username);
            ds.setPassword(password);
            return ds;
        }

        private String castToTextExpression(String quotedIdentifier) {
            if (this == MYSQL) {
                return "CAST(" + quotedIdentifier + " AS CHAR)";
            }
            return "CAST(" + quotedIdentifier + " AS TEXT)";
        }
    }

    private static final class QueryResult {
        private final String entity;
        private final String tableName;
        private final int limit;
        private final int offset;
        private final int returned;
        private final int total;
        private final JsonArrayBuilder columns;
        private final JsonArrayBuilder rows;
        private final String messageTextColumn;
        private final String responseTextColumn;
        private final String actorColumn;
        private final String roleColumn;
        private final String conversationColumn;
        private final String createdAtColumn;

        private QueryResult(String entity,
                String tableName,
                int limit,
                int offset,
                int returned,
                int total,
                JsonArrayBuilder columns,
                JsonArrayBuilder rows,
                String messageTextColumn,
                String responseTextColumn,
                String actorColumn,
                String roleColumn,
                String conversationColumn,
                String createdAtColumn) {
            this.entity = entity;
            this.tableName = tableName;
            this.limit = limit;
            this.offset = offset;
            this.returned = returned;
            this.total = total;
            this.columns = columns;
            this.rows = rows;
            this.messageTextColumn = messageTextColumn;
            this.responseTextColumn = responseTextColumn;
            this.actorColumn = actorColumn;
            this.roleColumn = roleColumn;
            this.conversationColumn = conversationColumn;
            this.createdAtColumn = createdAtColumn;
        }
    }

    private static final class ColumnHints {
        private final String messageTextColumn;
        private final String responseTextColumn;
        private final String actorColumn;
        private final String roleColumn;
        private final String conversationColumn;
        private final String createdAtColumn;

        private ColumnHints(String messageTextColumn,
                String responseTextColumn,
                String actorColumn,
                String roleColumn,
                String conversationColumn,
                String createdAtColumn) {
            this.messageTextColumn = messageTextColumn;
            this.responseTextColumn = responseTextColumn;
            this.actorColumn = actorColumn;
            this.roleColumn = roleColumn;
            this.conversationColumn = conversationColumn;
            this.createdAtColumn = createdAtColumn;
        }
    }

    private static final class SavedConfig {
        private final String dbType;
        private final String host;
        private final Integer port;
        private final String database;
        private final String username;
        private final String passwordEnc;
        private final boolean readableOnly;
        private final String shareTo;
        private final String shareSubject;
        private final String updatedBy;
        private final Timestamp updatedAt;

        private SavedConfig(String dbType,
                String host,
                Integer port,
                String database,
                String username,
                String passwordEnc,
                boolean readableOnly,
                String shareTo,
                String shareSubject,
                String updatedBy,
                Timestamp updatedAt) {
            this.dbType = dbType;
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.passwordEnc = passwordEnc;
            this.readableOnly = readableOnly;
            this.shareTo = shareTo;
            this.shareSubject = shareSubject;
            this.updatedBy = updatedBy;
            this.updatedAt = updatedAt;
        }
    }

    private static final class ColumnTypeInfo {
        private final String name;
        private final int sqlType;
        private final String sqlTypeName;
        private final int ordinalPosition;

        private ColumnTypeInfo(String name, int sqlType, String sqlTypeName, int ordinalPosition) {
            this.name = name;
            this.sqlType = sqlType;
            this.sqlTypeName = sqlTypeName;
            this.ordinalPosition = ordinalPosition;
        }
    }

    private static final class ForumExplorerDataAccessException extends RuntimeException {
        private ForumExplorerDataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class TableInfo {
        private final List<String> columns;
        private final List<String> searchableColumns;

        private TableInfo(List<String> columns, List<String> searchableColumns) {
            this.columns = columns;
            this.searchableColumns = searchableColumns;
        }
    }
}
