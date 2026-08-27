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
import java.sql.PreparedStatement;
import java.sql.Types;
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
}
