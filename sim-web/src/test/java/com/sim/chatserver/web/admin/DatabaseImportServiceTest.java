package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

class DatabaseImportServiceTest {

    @Test
    void readZipTables_whenZipContainsMixedEntries_readsOnlyCsvTables() throws Exception {
        DatabaseImportService service = new DatabaseImportService();

        byte[] zipBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8)) {
            zos.putNextEntry(new ZipEntry("tables/server_config.csv"));
            zos.write("id,name\n1,alpha\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("notes/readme.txt"));
            zos.write("ignore me".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("server_config.csv"));
            zos.write("id,name\n2,beta\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
            zipBytes = baos.toByteArray();
        }

        Method readZipTables = DatabaseImportService.class.getDeclaredMethod("readZipTables", InputStream.class);
        readZipTables.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> tables = (Map<String, Object>) readZipTables.invoke(service, new ByteArrayInputStream(zipBytes));
        assertEquals(1, tables.size());
        assertTrue(tables.containsKey("server_config"));

        Object csvData = tables.get("server_config");
        java.lang.reflect.Field rowsField = csvData.getClass().getDeclaredField("rows");
        rowsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) rowsField.get(csvData);
        assertEquals(1, rows.size());
        assertEquals("alpha", rows.get(0).get(1));
    }

    @Test
    void executeBatchWithContext_whenBatchUpdateFails_wrapsImportException() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        PreparedStatement ps = mock(PreparedStatement.class);

        BatchUpdateException bue = new BatchUpdateException("batch failed", new int[]{0});
        bue.setNextException(new SQLException("duplicate key"));
        when(ps.executeBatch()).thenThrow(bue);

        Method executeBatchWithContext = DatabaseImportService.class.getDeclaredMethod(
                "executeBatchWithContext",
                PreparedStatement.class,
                String.class,
                int.class);
        executeBatchWithContext.setAccessible(true);

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> executeBatchWithContext.invoke(service, ps, "server_config", 12));

        Throwable cause = thrown.getCause();
        assertEquals("ImportException", cause.getClass().getSimpleName());
        assertTrue(cause.getMessage().contains("server_config"));
        assertTrue(cause.getMessage().contains("12"));
    }

    @Test
    void executeBatchWithContext_whenSqlExceptionFails_wrapsImportException() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        PreparedStatement ps = mock(PreparedStatement.class);

        when(ps.executeBatch()).thenThrow(new SQLException("db down"));

        Method executeBatchWithContext = DatabaseImportService.class.getDeclaredMethod(
                "executeBatchWithContext",
                PreparedStatement.class,
                String.class,
                int.class);
        executeBatchWithContext.setAccessible(true);

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> executeBatchWithContext.invoke(service, ps, "widget_entries", 9));

        Throwable cause = thrown.getCause();
        assertEquals("ImportException", cause.getClass().getSimpleName());
        assertTrue(cause.getMessage().contains("widget_entries"));
    }

    @Test
    void handlePost_whenUnauthenticated_returnsUnauthorizedJson() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonObject payload = Json.createReader(new StringReader(body.toString(java.nio.charset.StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("Admin authentication required"));
    }

    @Test
    void handlePost_whenActionInvalid_returnsBadRequestJson() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("action")).thenReturn(new String[]{"noop"});

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject payload = Json.createReader(new StringReader(body.toString(java.nio.charset.StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("Invalid action"));
    }

    @Test
    void handlePost_precheckMissingUpload_returnsBadRequestJson() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("action")).thenReturn(new String[]{"precheck"});
        when(req.getPart("file")).thenReturn(null);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject payload = Json.createReader(new StringReader(body.toString(java.nio.charset.StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("Upload backup ZIP file"));
    }

    @Test
    void handlePost_runMissingUpload_returnsBadRequestJson() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("action")).thenReturn(new String[]{"run"});
        when(req.getPart("file")).thenReturn(null);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject payload = Json.createReader(new StringReader(body.toString(java.nio.charset.StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("Upload backup ZIP file"));
    }

    @Test
    void privateParsers_coverDateAndWidgetIdValidation() throws Exception {
        DatabaseImportService service = new DatabaseImportService();

        Method parseDateStrict = DatabaseImportService.class.getDeclaredMethod("parseDateStrict", String.class);
        parseDateStrict.setAccessible(true);

        assertEquals(LocalDate.of(2026, 8, 24), parseDateStrict.invoke(service, "2026-08-24"));
        InvocationTargetException parseEx = assertThrows(
            InvocationTargetException.class,
            () -> parseDateStrict.invoke(service, "2026-08-24T11:00:00Z"));
        assertTrue(parseEx.getCause() instanceof java.time.format.DateTimeParseException);

        InvocationTargetException parseLocalDateTimeEx = assertThrows(
            InvocationTargetException.class,
            () -> parseDateStrict.invoke(service, "2026-08-24T11:00:00"));
        assertTrue(parseLocalDateTimeEx.getCause() instanceof java.time.format.DateTimeParseException);

        InvocationTargetException invalidDateEx = assertThrows(
            InvocationTargetException.class,
            () -> parseDateStrict.invoke(service, "not-a-date"));
        assertTrue(invalidDateEx.getCause() instanceof java.time.format.DateTimeParseException);

        Method sanitizeWidgetId = DatabaseImportService.class.getDeclaredMethod("sanitizeWidgetId", String.class);
        sanitizeWidgetId.setAccessible(true);

        assertEquals("widget-1", sanitizeWidgetId.invoke(service, "widget-1"));
        assertNull(sanitizeWidgetId.invoke(service, "  "));
        assertNull(sanitizeWidgetId.invoke(service, "bad id"));
    }

    @Test
    void csvZipAndBinders_coverAdditionalBranches() throws Exception {
        DatabaseImportService service = new DatabaseImportService();

        byte[] zipPayload;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8)) {
            zos.putNextEntry(new ZipEntry("tables/server_config.csv"));
            zos.write("id,name\n1,alpha\n2,beta\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
            zipPayload = baos.toByteArray();
        }

        Method readZipTables = DatabaseImportService.class.getDeclaredMethod("readZipTables", InputStream.class);
        readZipTables.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> tables = (Map<String, Object>) readZipTables.invoke(service, new ByteArrayInputStream(zipPayload));
        assertTrue(tables.containsKey("server_config"));

        Method bindTyped = DatabaseImportService.class.getDeclaredMethod("bindTyped", PreparedStatement.class, int.class, String.class, int.class);
        bindTyped.setAccessible(true);
        PreparedStatement ps = mock(PreparedStatement.class);

        bindTyped.invoke(service, ps, 1, null, Types.INTEGER);
        verify(ps).setNull(1, Types.INTEGER);

        bindTyped.invoke(service, ps, 2, "", Types.VARCHAR);
        verify(ps).setString(2, "");

        bindTyped.invoke(service, ps, 3, "42", Types.BIGINT);
        verify(ps).setLong(3, 42L);

        bindTyped.invoke(service, ps, 4, "true", Types.BOOLEAN);
        verify(ps).setBoolean(4, true);

        bindTyped.invoke(service, ps, 5, "2026-08-24T10:15:30Z", Types.TIMESTAMP_WITH_TIMEZONE);
        verify(ps).setTimestamp(org.mockito.ArgumentMatchers.eq(5), org.mockito.ArgumentMatchers.any());

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> bindTyped.invoke(service, ps, 6, "bad-ts", Types.TIMESTAMP));
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    @Test
    void openPartInputStream_and_sanitizeRequestValue_coverBounds() throws Exception {
        DatabaseImportService service = new DatabaseImportService();

        Method openPartInputStream = DatabaseImportService.class.getDeclaredMethod("openPartInputStream", Part.class, String.class);
        openPartInputStream.setAccessible(true);
        Part part = mock(Part.class);
        byte[] bytes = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));

        try (InputStream in = (InputStream) openPartInputStream.invoke(service, part, "unit")) {
            assertEquals("hello", new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }

        assertEquals("abc", DatabaseImportService.sanitizeRequestValue(" a\r\nb\u0000c "));
    }

    @Test
    void helperNormalizers_coverSyncUrlSqlTypeAndColumnRules() throws Exception {
        DatabaseImportService service = new DatabaseImportService();

        Method sanitizeSyncUrlValue = DatabaseImportService.class.getDeclaredMethod("sanitizeSyncUrlValue", String.class);
        sanitizeSyncUrlValue.setAccessible(true);
        assertEquals(
                "https://example.test/api/v1/widgets/sync",
                sanitizeSyncUrlValue.invoke(null, "https://example.test/api/v1/widgets/sync"));
        assertEquals("", sanitizeSyncUrlValue.invoke(null, "ftp://example.test"));
        assertEquals("", sanitizeSyncUrlValue.invoke(null, "http://bad host"));

        Method sanitizeSqlType = DatabaseImportService.class.getDeclaredMethod("sanitizeSqlType", int.class);
        sanitizeSqlType.setAccessible(true);
        assertEquals(Types.INTEGER, sanitizeSqlType.invoke(service, Types.INTEGER));
        assertEquals(Types.VARCHAR, sanitizeSqlType.invoke(service, Integer.MIN_VALUE));

        Class<?> columnInfoClass = Class.forName("com.sim.chatserver.web.admin.DatabaseImportService$ColumnInfo");
        Constructor<?> columnInfoCtor = columnInfoClass.getDeclaredConstructor(int.class, boolean.class);
        columnInfoCtor.setAccessible(true);
        Object nonNullableText = columnInfoCtor.newInstance(Types.VARCHAR, false);

        Method normalizeValueForColumn = DatabaseImportService.class.getDeclaredMethod(
                "normalizeValueForColumn",
                String.class,
                String.class,
                String.class,
                columnInfoClass);
        normalizeValueForColumn.setAccessible(true);

        assertEquals("", normalizeValueForColumn.invoke(service, "term_definition", "match_pattern", null, nonNullableText));
        assertEquals("WILDCARD", normalizeValueForColumn.invoke(service, "term_definition", "match_type", "   ", nonNullableText));
        assertEquals("", normalizeValueForColumn.invoke(service, "other", "notes", "   ", nonNullableText));

        Method isSafeSyncEndpoint = DatabaseImportService.class.getDeclaredMethod("isSafeSyncEndpoint", java.net.URI.class);
        isSafeSyncEndpoint.setAccessible(true);
        assertTrue((boolean) isSafeSyncEndpoint.invoke(service, java.net.URI.create("https://example.test/api")));
        assertTrue(!(boolean) isSafeSyncEndpoint.invoke(service, java.net.URI.create("ftp://example.test/api")));

        Method findWidgetTables = DatabaseImportService.class.getDeclaredMethod("findWidgetTables", Set.class);
        findWidgetTables.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> widgetTables = (List<String>) findWidgetTables.invoke(service, Set.of("server_config", "widget_a", "widget_b"));
        assertEquals(2, widgetTables.size());
        assertTrue(widgetTables.contains("widget_a"));

        Method q = DatabaseImportService.class.getDeclaredMethod("q", String.class);
        q.setAccessible(true);
        assertEquals("\"good_name\"", q.invoke(service, "good_name"));
        InvocationTargetException qEx = assertThrows(InvocationTargetException.class, () -> q.invoke(service, "bad-name"));
        assertTrue(qEx.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void replaceTableData_whenNoMatchingColumns_throwsImportException() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        Connection conn = mock(Connection.class);
        PreparedStatement truncatePs = mock(PreparedStatement.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet columnsRs = mock(ResultSet.class);

        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(truncatePs);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(null, "public", "widget_entries", null)).thenReturn(columnsRs);
        when(columnsRs.next()).thenReturn(true, false);
        when(columnsRs.getString("COLUMN_NAME")).thenReturn("other_column");
        when(columnsRs.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR);
        when(columnsRs.getInt("NULLABLE")).thenReturn(ResultSetMetaData.columnNullable);
        when(columnsRs.wasNull()).thenReturn(false);

        Class<?> csvType = Class.forName("com.sim.chatserver.web.admin.DatabaseImportService$CsvTableData");
        Constructor<?> csvCtor = csvType.getDeclaredConstructor(List.class, List.class);
        csvCtor.setAccessible(true);
        Object csv = csvCtor.newInstance(
            List.of("missing_col"),
            List.of(List.of("value")));

        Method replaceTableData = DatabaseImportService.class.getDeclaredMethod(
            "replaceTableData",
            Connection.class,
            String.class,
            csvType);
        replaceTableData.setAccessible(true);

        InvocationTargetException ex = assertThrows(
            InvocationTargetException.class,
            () -> replaceTableData.invoke(service, conn, "widget_entries", csv));
        assertEquals("ImportException", ex.getCause().getClass().getSimpleName());
        assertTrue(ex.getCause().getMessage().contains("No matching columns"));
    }

    @Test
    void replaceTableData_whenRowsAreValid_insertsAndReturnsCount() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        Connection conn = mock(Connection.class);
        PreparedStatement truncatePs = mock(PreparedStatement.class);
        PreparedStatement insertPs = mock(PreparedStatement.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet columnsRs = mock(ResultSet.class);

        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(truncatePs, insertPs);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(null, "public", "widget_entries", null)).thenReturn(columnsRs);
        when(columnsRs.next()).thenReturn(true, true, true, true, false);
        when(columnsRs.getString("COLUMN_NAME")).thenReturn("name", "count", "active", "created_at");
        when(columnsRs.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR, Types.INTEGER, Types.BOOLEAN, Types.TIMESTAMP);
        when(columnsRs.getInt("NULLABLE")).thenReturn(
            ResultSetMetaData.columnNullable,
            ResultSetMetaData.columnNullable,
            ResultSetMetaData.columnNullable,
            ResultSetMetaData.columnNullable);
        when(columnsRs.wasNull()).thenReturn(false);
        when(insertPs.executeBatch()).thenReturn(new int[]{1, 1});

        Class<?> csvType = Class.forName("com.sim.chatserver.web.admin.DatabaseImportService$CsvTableData");
        Constructor<?> csvCtor = csvType.getDeclaredConstructor(List.class, List.class);
        csvCtor.setAccessible(true);
        Object csv = csvCtor.newInstance(
            List.of("name", "count", "active", "created_at"),
            List.of(
                List.of("alpha", "7", "true", "2026-08-24T10:15:30Z"),
                List.of("beta", "8", "0", "2026-08-24T10:15:31Z")));

        Method replaceTableData = DatabaseImportService.class.getDeclaredMethod(
            "replaceTableData",
            Connection.class,
            String.class,
            csvType);
        replaceTableData.setAccessible(true);

        int inserted = (int) replaceTableData.invoke(service, conn, "widget_entries", csv);
        assertEquals(2, inserted);
        verify(insertPs, org.mockito.Mockito.times(2)).addBatch();
        verify(insertPs).executeBatch();
    }

    @Test
    void realignSequenceBackedColumns_whenValidSequenceExists_executesSetval() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        Connection conn = mock(Connection.class);
        PreparedStatement lookupPs = mock(PreparedStatement.class);
        PreparedStatement setvalPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(lookupPs, setvalPs);
        when(lookupPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("column_name")).thenReturn("bad-name", "id");
        when(rs.getString("seq_name")).thenReturn("public.bad_seq", "public.widget_entries_id_seq");

        Method realign = DatabaseImportService.class.getDeclaredMethod(
            "realignSequenceBackedColumns",
            Connection.class,
            String.class);
        realign.setAccessible(true);
        realign.invoke(service, conn, "widget_entries");

        verify(setvalPs).setString(1, "public.widget_entries_id_seq");
        verify(setvalPs).execute();
    }

    @Test
    void normalizeZipWidgetTables_whenMappedTablesOverlap_mergesRows() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tablesRs = mock(ResultSet.class);
        PreparedStatement widgetLookupPs = mock(PreparedStatement.class);
        ResultSet widgetRs = mock(ResultSet.class);

        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(null, "public", "widget_entries", new String[]{"TABLE"})).thenReturn(tablesRs);
        when(tablesRs.next()).thenReturn(true);

        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(widgetLookupPs);
        when(widgetLookupPs.executeQuery()).thenReturn(widgetRs);
        when(widgetRs.next()).thenReturn(true, true, false);
        when(widgetRs.getString("widget_id")).thenReturn("widget-1", "widget_1");

        Class<?> csvType = Class.forName("com.sim.chatserver.web.admin.DatabaseImportService$CsvTableData");
        Constructor<?> csvCtor = csvType.getDeclaredConstructor(List.class, List.class);
        csvCtor.setAccessible(true);
        Object csvA = csvCtor.newInstance(List.of("chat_id", "prompt"), List.of(List.of("a", "p1")));
        Object csvB = csvCtor.newInstance(List.of("chat_id", "prompt"), List.of(List.of("b", "p2")));

        Map<String, Object> zipTables = new LinkedHashMap<>();
        zipTables.put("widget-1", csvA);
        zipTables.put("widget_1", csvB);

        Method normalize = DatabaseImportService.class.getDeclaredMethod(
            "normalizeZipWidgetTables",
            Connection.class,
            Map.class);
        normalize.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> normalized = (Map<String, Object>) normalize.invoke(service, conn, zipTables);

        assertTrue(normalized.containsKey("widget_1"));
        assertTrue(!normalized.containsKey("widget-1"));

        Object merged = normalized.get("widget_1");
        java.lang.reflect.Field rowsField = csvType.getDeclaredField("rows");
        rowsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<List<String>> mergedRows = (List<List<String>>) rowsField.get(merged);
        assertEquals(2, mergedRows.size());
    }

    private static final class CapturingOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate;

        private CapturingOutputStream(ByteArrayOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // no-op for unit tests
        }
    }

    private static byte[] zipWithCsv(String entryName, String csvContent) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        }
    }
}
