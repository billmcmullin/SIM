package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ForumDatabaseExplorerServletTest {

    @Test
    void doPost_withoutAdminSession_returnsForbidden() throws Exception {
        ForumDatabaseExplorerServlet servlet = new ForumDatabaseExplorerServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject payload = parseJson(out);
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").toLowerCase().contains("admin role required"));
    }

    @Test
    void tryRunGdnForumThreadQuery_withUserTable_buildsNameJoinAndReturnsRows() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        Connection conn = mock(Connection.class);
        DatabaseMetaData md = mock(DatabaseMetaData.class);
        List<String> seenSql = new ArrayList<>();

        when(conn.getMetaData()).thenReturn(md);
        when(conn.getCatalog()).thenReturn("chat");
        when(conn.getSchema()).thenReturn("public");

        when(md.getTables(any(), any(), anyString(), any())).thenAnswer(inv ->
                tableNamesResultSet("GDN_Discussion", "GDN_Comment", "GDN_User"));
        when(md.getColumns(any(), any(), anyString(), anyString())).thenAnswer(inv -> {
            String table = inv.getArgument(2, String.class);
            if ("GDN_Discussion".equalsIgnoreCase(table)) {
                return tableColumnsResultSet("DiscussionID", "Name", "Body", "InsertUserID", "DateInserted");
            }
            if ("GDN_User".equalsIgnoreCase(table)) {
                return tableColumnsResultSet("UserID", "Name");
            }
            return tableColumnsResultSet();
        });

        when(conn.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0, String.class);
            seenSql.add(sql);

            if (sql.startsWith("SELECT COUNT(*)")) {
                return preparedStatementReturning(countResultSet(1));
            }
            if (sql.startsWith("SELECT `message_id`")) {
                return preparedStatementReturning(rowsResultSet(List.of(row(
                        "message_id", "42",
                        "conversation_id", "42",
                        "subject", "Welcome",
                        "actor", "Alice",
                        "role", "user",
                        "created_at", "2026-09-18T12:00:00Z",
                        "message_text", "Original post",
                        "response_text", null,
                        "chain_key", "42"
                ))));
            }

            throw new AssertionError("Unexpected SQL: " + sql);
        });

        Object dbTypeMySql = dbType("MYSQL");
        Object queryResult = invokePrivate(
            service,
                "tryRunGdnForumThreadQuery",
                new Class<?>[]{Connection.class, dbTypeMySql.getClass(), String.class, int.class, int.class},
                new Object[]{conn, dbTypeMySql, "", 25, 0}
        );

        assertNotNull(queryResult);

        String dataSql = seenSql.stream().filter(sql -> sql.startsWith("SELECT `message_id`")).findFirst().orElse("");
        assertTrue(dataSql.contains("LEFT JOIN `GDN_User` u ON"));
        assertTrue(dataSql.contains("COALESCE(NULLIF(TRIM("));

        String tableName = (String) getPrivateField(queryResult, "tableName");
        assertEquals("GDN_Discussion+GDN_Comment+GDN_User", tableName);

        JsonArray rows = ((JsonArrayBuilder) getPrivateField(queryResult, "rows")).build();
        assertEquals(1, rows.size());
        JsonObject first = rows.getJsonObject(0);
        assertEquals("Alice", first.getString("actor"));
        assertEquals("Original post", first.getString("message_text"));
    }

    @Test
    void tryRunGdnForumThreadQuery_withoutUserTable_skipsJoinAndKeepsActorId() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        Connection conn = mock(Connection.class);
        DatabaseMetaData md = mock(DatabaseMetaData.class);
        List<String> seenSql = new ArrayList<>();

        when(conn.getMetaData()).thenReturn(md);
        when(conn.getCatalog()).thenReturn("chat");
        when(conn.getSchema()).thenReturn("public");

        when(md.getTables(any(), any(), anyString(), any())).thenAnswer(inv ->
                tableNamesResultSet("GDN_Discussion", "GDN_Comment"));
        when(md.getColumns(any(), any(), anyString(), anyString())).thenAnswer(inv -> {
            String table = inv.getArgument(2, String.class);
            if ("GDN_Discussion".equalsIgnoreCase(table)) {
                return tableColumnsResultSet("DiscussionID", "Name", "Body", "InsertUserID", "DateInserted");
            }
            return tableColumnsResultSet();
        });

        when(conn.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0, String.class);
            seenSql.add(sql);

            if (sql.startsWith("SELECT COUNT(*)")) {
                return preparedStatementReturning(countResultSet(1));
            }
            if (sql.startsWith("SELECT `message_id`")) {
                return preparedStatementReturning(rowsResultSet(List.of(row(
                        "message_id", "42",
                        "conversation_id", "42",
                        "subject", "Welcome",
                        "actor", "101",
                        "role", "user",
                        "created_at", "2026-09-18T12:00:00Z",
                        "message_text", "Original post",
                        "response_text", null,
                        "chain_key", "42"
                ))));
            }

            throw new AssertionError("Unexpected SQL: " + sql);
        });

        Object dbTypeMySql = dbType("MYSQL");
        Object queryResult = invokePrivate(
            service,
                "tryRunGdnForumThreadQuery",
                new Class<?>[]{Connection.class, dbTypeMySql.getClass(), String.class, int.class, int.class},
                new Object[]{conn, dbTypeMySql, "", 25, 0}
        );

        assertNotNull(queryResult);

        String dataSql = seenSql.stream().filter(sql -> sql.startsWith("SELECT `message_id`")).findFirst().orElse("");
        assertFalse(dataSql.contains("LEFT JOIN `GDN_User` u ON"));

        String tableName = (String) getPrivateField(queryResult, "tableName");
        assertEquals("GDN_Discussion+GDN_Comment", tableName);

        JsonArray rows = ((JsonArrayBuilder) getPrivateField(queryResult, "rows")).build();
        assertEquals("101", rows.getJsonObject(0).getString("actor"));
    }

    @Test
    void handleThreadReplies_withDiscussionAndUserJoin_returnsReplyRows() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Connection conn = mock(Connection.class);
        DatabaseMetaData md = mock(DatabaseMetaData.class);
        List<String> seenSql = new ArrayList<>();

        when(req.getParameterValues("discussionId")).thenReturn(new String[]{"42"});
        when(req.getParameterValues("limit")).thenReturn(new String[]{"5"});
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        when(conn.getMetaData()).thenReturn(md);
        when(conn.getCatalog()).thenReturn("chat");
        when(conn.getSchema()).thenReturn("public");

        when(md.getTables(any(), any(), anyString(), any())).thenAnswer(inv ->
                tableNamesResultSet("GDN_Discussion", "GDN_Comment", "GDN_User"));
        when(md.getColumns(any(), any(), anyString(), anyString())).thenAnswer(inv -> {
            String table = inv.getArgument(2, String.class);
            if ("GDN_Discussion".equalsIgnoreCase(table)) {
                return tableColumnsResultSet("DiscussionID", "Name", "Body", "InsertUserID", "DateInserted");
            }
            if ("GDN_Comment".equalsIgnoreCase(table)) {
                return tableColumnsResultSet("CommentID", "DiscussionID", "Body", "InsertUserID", "DateInserted");
            }
            if ("GDN_User".equalsIgnoreCase(table)) {
                return tableColumnsResultSet("UserID", "Name");
            }
            return tableColumnsResultSet();
        });

        when(conn.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0, String.class);
            seenSql.add(sql);

            if (sql.startsWith("SELECT COALESCE")) {
                return preparedStatementReturning(rowsResultSet(List.of(row(
                        "actor", "Bob",
                        "role", "reply",
                        "created_at", "2026-09-18T12:10:00Z",
                        "message_text", "First reply"
                ))));
            }

            throw new AssertionError("Unexpected SQL: " + sql);
        });

        Object dbTypeMySql = dbType("MYSQL");
        invokePrivate(
            service,
                "handleThreadReplies",
                new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class, Connection.class, dbTypeMySql.getClass()},
                new Object[]{req, resp, conn, dbTypeMySql}
        );

        String querySql = seenSql.stream().filter(sql -> sql.startsWith("SELECT COALESCE")).findFirst().orElse("");
        assertTrue(querySql.contains("LEFT JOIN `GDN_User` u ON"));
        assertTrue(querySql.contains("ORDER BY cm.`CommentID` ASC") || querySql.contains("ORDER BY cm.`comment_id` ASC"));

        JsonObject payload = parseJson(out);
        assertEquals("ok", payload.getString("status"));
        assertEquals(1, payload.getInt("replyCount"));
        JsonArray replies = payload.getJsonArray("replies");
        assertEquals(1, replies.size());
        assertEquals("Bob", replies.getJsonObject(0).getString("actor"));
        assertEquals("First reply", replies.getJsonObject(0).getString("message_text"));
    }

    @Test
    void handleThreadReplies_withoutDiscussionId_returnsBadRequest() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        Object dbTypeMySql = dbType("MYSQL");
        invokePrivate(
            service,
                "handleThreadReplies",
                new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class, Connection.class, dbTypeMySql.getClass()},
                new Object[]{req, resp, mock(Connection.class), dbTypeMySql}
        );

        JsonObject payload = parseJson(out);
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").toLowerCase().contains("discussionid is required"));
    }

    private static Object dbType(String enumName) throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends Enum> dbTypeClass = (Class<? extends Enum>) Class.forName(
            "com.sim.chatserver.web.admin.ForumDatabaseExplorerService$DbType");
        return Enum.valueOf(dbTypeClass, enumName);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] argTypes, Object[] args) throws Exception {
        var method = target.getClass().getDeclaredMethod(methodName, argTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static JsonObject parseJson(ByteArrayOutputStream out) {
        String body = out.toString(StandardCharsets.UTF_8);
        return Json.createReader(new StringReader(body)).readObject();
    }

    private static PreparedStatement preparedStatementReturning(ResultSet rs) throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);
        return ps;
    }

    private static ResultSet metadataOnlyResultSet(String... columns) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData meta = resultSetMetaData(columns);
        when(rs.getMetaData()).thenReturn(meta);
        return rs;
    }

    private static ResultSet tableColumnsResultSet(String... columns) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        AtomicInteger idx = new AtomicInteger(-1);
        when(rs.next()).thenAnswer(inv -> idx.incrementAndGet() < columns.length);
        when(rs.getString("COLUMN_NAME")).thenAnswer(inv -> {
            int i = idx.get();
            if (i < 0 || i >= columns.length) {
                return null;
            }
            return columns[i];
        });
        when(rs.getInt("DATA_TYPE")).thenAnswer(inv -> {
            int i = idx.get();
            if (i < 0 || i >= columns.length) {
                return Types.VARCHAR;
            }
            String col = columns[i].toLowerCase();
            if (col.contains("id")) {
                return Types.INTEGER;
            }
            if (col.contains("date") || col.contains("created") || col.contains("time")) {
                return Types.TIMESTAMP;
            }
            return Types.VARCHAR;
        });
        when(rs.getString("TYPE_NAME")).thenAnswer(inv -> {
            int i = idx.get();
            if (i < 0 || i >= columns.length) {
                return "VARCHAR";
            }
            String col = columns[i].toLowerCase();
            if (col.contains("id")) {
                return "INTEGER";
            }
            if (col.contains("date") || col.contains("created") || col.contains("time")) {
                return "TIMESTAMP";
            }
            return "VARCHAR";
        });
        when(rs.getInt("ORDINAL_POSITION")).thenAnswer(inv -> idx.get() + 1);
        return rs;
    }

    private static ResultSet rowsResultSet(List<Map<String, Object>> rows) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        AtomicInteger idx = new AtomicInteger(-1);
        when(rs.next()).thenAnswer(inv -> idx.incrementAndGet() < rows.size());
        when(rs.getObject(anyString())).thenAnswer(inv -> {
            int i = idx.get();
            if (i < 0 || i >= rows.size()) {
                return null;
            }
            return rows.get(i).get(inv.getArgument(0, String.class));
        });
        return rs;
    }

    private static ResultSet countResultSet(int count) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        AtomicInteger idx = new AtomicInteger(0);
        when(rs.next()).thenAnswer(inv -> idx.getAndIncrement() == 0);
        when(rs.getInt(anyInt())).thenReturn(count);
        return rs;
    }

    private static ResultSetMetaData resultSetMetaData(String... columns) throws Exception {
        ResultSetMetaData md = mock(ResultSetMetaData.class);
        when(md.getColumnCount()).thenReturn(columns.length);
        when(md.getColumnLabel(anyInt())).thenAnswer(inv -> {
            int i = inv.getArgument(0, Integer.class);
            return columns[i - 1];
        });
        when(md.getColumnName(anyInt())).thenAnswer(inv -> {
            int i = inv.getArgument(0, Integer.class);
            return columns[i - 1];
        });
        when(md.getColumnType(anyInt())).thenAnswer(inv -> {
            String col = columns[inv.getArgument(0, Integer.class) - 1].toLowerCase();
            if (col.contains("id")) {
                return Types.INTEGER;
            }
            if (col.contains("date") || col.contains("created") || col.contains("time")) {
                return Types.TIMESTAMP;
            }
            return Types.VARCHAR;
        });
        when(md.getColumnTypeName(anyInt())).thenAnswer(inv -> {
            String col = columns[inv.getArgument(0, Integer.class) - 1].toLowerCase();
            if (col.contains("id")) {
                return "INTEGER";
            }
            if (col.contains("date") || col.contains("created") || col.contains("time")) {
                return "TIMESTAMP";
            }
            return "VARCHAR";
        });
        return md;
    }

    private static ResultSet tableNamesResultSet(String... tableNames) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        AtomicInteger idx = new AtomicInteger(-1);
        when(rs.next()).thenAnswer(inv -> idx.incrementAndGet() < tableNames.length);
        when(rs.getString("TABLE_NAME")).thenAnswer(inv -> {
            int i = idx.get();
            if (i < 0 || i >= tableNames.length) {
                return null;
            }
            return tableNames[i];
        });
        return rs;
    }

    private static Map<String, Object> row(Object... keyValues) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            row.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return row;
    }

    private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
        return new ServletOutputStream() {
            @Override
            public void write(int b) {
                out.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // no-op for tests
            }
        };
    }
}
