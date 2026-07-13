package com.sim.chatserver.web.dashboard.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sim.chatserver.startup.AppDataSourceHolder;

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

    @BeforeEach
    void setUp() throws Exception {
        underTest = new WidgetTableSelectIdsServlet();

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(req.getSession(anyBoolean())).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(new Object());
        when(req.getParameterMap()).thenReturn(Map.of());

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw, true));

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
        when(req.getParameterMap()).thenReturn(Map.of());
        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_happyPath_tableExists_noRows() throws Exception {
        when(req.getParameterMap()).thenReturn(Map.of("widgetId", new String[] {"widget-1"}));
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));

        verify(ds, atLeastOnce()).getConnection();
        verify(conn, atLeastOnce()).prepareStatement(anyString());
    }

    @Test
    void doGet_happyPath_tableExists_withRows() throws Exception {
        when(req.getParameterMap()).thenReturn(Map.of("widgetId", new String[] {"widget-1"}));
        when(rsMeta.next()).thenReturn(true);
        when(rsQuery.next()).thenReturn(true, false);
        when(rsQuery.getString(anyString())).thenReturn("id-1");

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_whenTableMissing_returnsGracefully() throws Exception {
        when(req.getParameterMap()).thenReturn(Map.of("widgetId", new String[] {"widget-1"}));
        when(rsMeta.next()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_metadataFailure_returnsGracefully() throws Exception {
        when(req.getParameterMap()).thenReturn(Map.of("widgetId", new String[] {"widget-1"}));
        when(conn.getMetaData()).thenThrow(new java.sql.SQLException("meta fail"));

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }

    @Test
    void doGet_queryFailure_returnsGracefully() throws Exception {
        when(req.getParameterMap()).thenReturn(Map.of("widgetId", new String[] {"widget-1"}));
        when(rsMeta.next()).thenReturn(true);
        when(ps.executeQuery()).thenThrow(new java.sql.SQLException("query fail"));

        assertDoesNotThrow(() -> underTest.doGet(req, resp));
    }
}
