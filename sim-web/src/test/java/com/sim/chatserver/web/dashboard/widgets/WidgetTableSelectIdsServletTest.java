package com.sim.chatserver.web.dashboard.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Timeout(3)
class WidgetTableSelectIdsServletTest {

    private WidgetTableSelectIdsServlet underTest;

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;

    private AppDataSourceHolder dsHolder;
    private DataSource ds;
    private Connection conn;
    private DatabaseMetaData meta;
    private PreparedStatement ps;
    private ResultSet rsMeta;
    private ResultSet rsQuery;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new WidgetTableSelectIdsServlet();

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(req.getSession(anyBoolean())).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(new Object());

        responseWriter = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter, true));

        dsHolder = mock(AppDataSourceHolder.class);
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        meta = mock(DatabaseMetaData.class);
        ps = mock(PreparedStatement.class);
        rsMeta = mock(ResultSet.class);
        rsQuery = mock(ResultSet.class);

        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(any(), any(), any(), any())).thenReturn(rsMeta);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rsQuery);

        // defaults: table exists, query returns no rows
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(false);

        // field appears package-visible in your original tests
        underTest.dsHolder = dsHolder;
    }

    @Test
    void doGet_noSession_returnsGracefully() throws Exception {
        when(req.getSession(anyBoolean())).thenReturn(null);
        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_missingUserSessionAttr_returnsGracefully() throws Exception {
        when(session.getAttribute(anyString())).thenReturn(null);
        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_noWidgetParam_returnsGracefully() throws Exception {
        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_happyPath_tableExists_noRows() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(ds, atLeastOnce()).getConnection();
        verify(conn, atLeastOnce()).prepareStatement(anyString());
    }

    @Test
    void doGet_happyPath_tableExists_withRows() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(true, false);
        when(rsQuery.getString(anyString())).thenReturn("id-1");

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_whenTableMissing_returnsGracefully() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(rsMeta.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_metadataFailure_returnsGracefully() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(conn.getMetaData()).thenThrow(new java.sql.SQLException("meta fail"));

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_queryFailure_returnsGracefully() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(rsMeta.next()).thenReturn(true);
        when(ps.executeQuery()).thenThrow(new java.sql.SQLException("query fail"));

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_invalidDate_returnsBadRequestWithoutDbCall() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(req.getParameterValues("date")).thenReturn(new String[] {"2026/01/15"});

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(ds, never()).getConnection();
    }

    @Test
    void doGet_validDate_bindsDateWindowParameters() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(req.getParameterValues("date")).thenReturn(new String[] {"2026-01-15"});
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(ps, atLeastOnce()).setTimestamp(anyInt(), any(java.sql.Timestamp.class));
    }

    @Test
    void doGet_filtersBlankChatIds_andTrimsValuesInJson() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(true, true, false);
        when(rsQuery.getString("widget_chat_id")).thenReturn(" id-1 ", "   ");

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        String body = responseWriter.toString();
        assertEquals(true, body.contains("\"status\":\"ok\""));
        assertEquals(true, body.contains("\"total\":1"));
        assertEquals(true, body.contains("id-1"));
    }

    @Test
    void doGet_missingWidgetId_primarySendErrorIOException_usesFallbackSendError() throws Exception {
        org.mockito.Mockito.doThrow(new java.io.IOException("primary failed"))
                .when(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
        when(resp.isCommitted()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doGet_invalidDate_jsonWriterFailure_usesFallbackSendError() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(req.getParameterValues("date")).thenReturn(new String[] {"invalid-date"});
        when(resp.getOutputStream()).thenThrow(new IllegalStateException("stream unavailable"));
        when(resp.getWriter()).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doGet_invalidDate_jsonWriterFailure_andFallbackSendErrorFailure_isSwallowed() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(req.getParameterValues("date")).thenReturn(new String[] {"invalid-date"});
        when(resp.getOutputStream()).thenThrow(new IllegalStateException("stream unavailable"));
        when(resp.getWriter()).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);
        org.mockito.Mockito.doThrow(new java.io.IOException("fallback failed"))
                .when(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_widgetIdSanitization_appliesQuotedSafeTableNameInSql() throws Exception {
        String longId = "1 bad-id-with-many-invalid-characters-abcdefghijklmnopqrstuvwxyz-1234567890";
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {longId});
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(conn).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertEquals(true, sql.contains("FROM \"w_"));
        assertEquals(false, sql.contains("bad-id"));
        assertEquals(false, sql.contains("bad id"));
    }

    @Test
    void doGet_whenDsHolderNull_resolvesThroughCdi() throws Exception {
        underTest.dsHolder = null;
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(false);

        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = mock(Instance.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);
        when(instance.get()).thenReturn(dsHolder);

        try (MockedStatic<CDI> cdiMock = org.mockito.Mockito.mockStatic(CDI.class)) {
            cdiMock.when(CDI::current).thenReturn(cdi);

            assertDoesNotThrow(() -> underTest.doGet(req, resp));
        }

        verify(ds, atLeastOnce()).getConnection();
    }

    @Test
    void doGet_noSession_sendErrorIOException_usesFallback() throws Exception {
        when(req.getSession(anyBoolean())).thenReturn(null);
        org.mockito.Mockito.doThrow(new java.io.IOException("unauthorized failed"))
                .when(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        when(resp.isCommitted()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(resp, org.mockito.Mockito.atLeast(2)).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doGet_withSearchAndFilters_bindsSearchPromptResponseAndDateParams() throws Exception {
        when(req.getParameterValues("widgetId")).thenReturn(new String[] {"widget-1"});
        when(req.getParameterValues("search")).thenReturn(new String[] {"  Alpha "});
        when(req.getParameterValues("filterPrompt")).thenReturn(new String[] {"  Prompt "});
        when(req.getParameterValues("filterResponse")).thenReturn(new String[] {"  Reply "});
        when(req.getParameterValues("date")).thenReturn(new String[] {"2026-01-15"});
        when(rsMeta.next()).thenReturn(false, true);
        when(rsQuery.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(ps).setString(1, "%alpha%");
        verify(ps).setString(2, "%alpha%");
        verify(ps).setString(3, "%alpha%");
        verify(ps).setString(4, "%prompt%");
        verify(ps).setString(5, "%reply%");
        verify(ps).setTimestamp(org.mockito.ArgumentMatchers.eq(6), any(java.sql.Timestamp.class));
        verify(ps).setTimestamp(org.mockito.ArgumentMatchers.eq(7), any(java.sql.Timestamp.class));
    }

    @Test
    void doGet_reqSessionThrows_hitsOuterFallback500() throws Exception {
        when(req.getSession(anyBoolean())).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void helper_sanitizeWidgetTableName_handlesBlankPrefixAndLength() throws Exception {
        Method sanitize = WidgetTableSelectIdsServlet.class
                .getDeclaredMethod("sanitizeWidgetTableName", String.class);
        sanitize.setAccessible(true);

        String blank = (String) sanitize.invoke(underTest, "   ");
        String prefixed = (String) sanitize.invoke(underTest, "1-bad id");
        String longName = (String) sanitize.invoke(underTest,
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        assertEquals("widget", blank);
        assertEquals(true, prefixed.startsWith("w_"));
        assertEquals(true, longName.length() <= 60);
    }

    @Test
    void helper_quoteIdentifier_escapesEmbeddedQuotes() throws Exception {
        Method quote = WidgetTableSelectIdsServlet.class
                .getDeclaredMethod("quoteIdentifier", String.class);
        quote.setAccessible(true);

        String quoted = (String) quote.invoke(underTest, "abc\"def");
        assertEquals("\"abc\"\"def\"", quoted);
    }
}
