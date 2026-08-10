package com.sim.chatserver.web.dashboard.drilldown;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetStore;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewDataServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet
 * @author bmcmullin
 */
public class WidgetReviewDataServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        WidgetReviewDataServlet underTest = new WidgetReviewDataServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        WidgetReviewDataServlet underTest = new WidgetReviewDataServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        WidgetReviewDataServlet underTest = new WidgetReviewDataServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        WidgetReviewDataServlet underTest = new WidgetReviewDataServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        WidgetReviewDataServlet underTest = new WidgetReviewDataServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "*"; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        WidgetReviewDataServlet underTest = new WidgetReviewDataServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "*"; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }



    // Merged from WidgetReviewDataServletCoverageTest
    
    
        private MockedStatic<CDI> cdiMock;
    
        @AfterEach
        void tearDown() throws Exception {
            if (cdiMock != null) {
                cdiMock.close();
                cdiMock = null;
            }
            clearStaticCache("tableExistsCache");
            clearStaticCache("widgetNameCache");
        }
    
        @Test
        void doGet_whenUnauthenticated_returns401() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter body = new StringWriter();
    
            when(req.getSession(false)).thenReturn(null);
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            assertTrue(body.toString().toLowerCase(Locale.ROOT).contains("authentication required"));
        }
    
        @Test
        void doGet_whenSelectionIdInvalid_returns400() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSessionWithSelection(null);
            StringWriter body = new StringWriter();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"***"});
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            assertTrue(body.toString().contains("selectionId required"));
        }
    
        @Test
        void doGet_whenDateInvalid_returns400() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSessionWithSelection(null);
            StringWriter body = new StringWriter();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sel1"});
            when(req.getParameterValues("date")).thenReturn(new String[]{"not-a-date"});
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            assertTrue(body.toString().contains("Invalid date"));
        }
    
        @Test
        void doGet_whenSelectionMissing_returns404() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSessionWithSelection(null);
            StringWriter body = new StringWriter();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
            assertTrue(body.toString().contains("Selection not found"));
        }
    
        @Test
        void doGet_whenNoChatIdsInSelection_returns400() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            WidgetReviewStartServlet.Selection selection = newSelection(null, List.of(), null, null, "2026-08-07");
            HttpSession session = authedSessionWithSelection(selection);
            StringWriter body = new StringWriter();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            assertTrue(body.toString().contains("No chat IDs specified"));
        }
    
        @Test
        void doGet_whenTableDoesNotExist_returns400() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            WidgetReviewStartServlet.Selection selection = newSelection(null, List.of("chat-1"), null, null, null);
            HttpSession session = authedSessionWithSelection(selection);
            StringWriter body = new StringWriter();
    
            DataSource dataSource = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet rs1 = mock(ResultSet.class);
            ResultSet rs2 = mock(ResultSet.class);
            ResultSet rs3 = mock(ResultSet.class);
    
            when(dataSource.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(any(), any(), anyString(), any())).thenReturn(rs1, rs2, rs3);
            when(rs1.next()).thenReturn(false);
            when(rs2.next()).thenReturn(false);
            when(rs3.next()).thenReturn(false);
    
            mockCdiDataSource(dataSource);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            assertTrue(body.toString().contains("Table does not exist"));
        }
    
        @Test
        void doGet_whenSqlFails_returns500() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            WidgetReviewStartServlet.Selection selection = newSelection(null, List.of("chat-1"), null, null, null);
            HttpSession session = authedSessionWithSelection(selection);
            StringWriter body = new StringWriter();
    
            DataSource dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenThrow(new SQLException("db down"));
            mockCdiDataSource(dataSource);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            servlet.doGet(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertTrue(body.toString().contains("Unable to load selection"));
        }
    
        @Test
        void doGet_whenDataQueryReturnsNoRows_returnsOkPayload() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WidgetReviewStartServlet.Selection selection = newSelection(null, List.of("chat-1"), null, null, null);
            HttpSession session = authedSessionWithSelection(selection);
    
            DataSource dataSource = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet dataRs = mock(ResultSet.class);
            Array sqlArray = mock(Array.class);
    
            when(dataSource.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(conn.createArrayOf(eq("text"), Mockito.<Object[]>any())).thenReturn(sqlArray);
            when(ps.executeQuery()).thenReturn(dataRs);
            when(dataRs.next()).thenReturn(false);
    
            mockCdiDataSource(dataSource);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(req.getParameterValues("date")).thenReturn(new String[]{"2026-08-06"});
            when(req.getParameterValues("search")).thenReturn(new String[]{"abc"});
            when(req.getParameterValues("sortColumn")).thenReturn(new String[]{"prompt"});
            when(req.getParameterValues("sortDir")).thenReturn(new String[]{"asc"});
            when(req.getParameterValues("page")).thenReturn(new String[]{"2"});
            when(req.getParameterValues("limit")).thenReturn(new String[]{"25"});
            when(resp.getStatus()).thenReturn(0);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            verify(ps, times(3)).setString(anyInt(), eq("%abc%"));
            verify(ps, times(2)).setTimestamp(anyInt(), any(Timestamp.class));
    
            JsonObject body = Json.createReader(new StringReader(out.toString())).readObject();
            assertEquals("ok", body.getString("status"));
            assertEquals(0, body.getInt("totalRows"));
            assertEquals(1, body.getInt("totalPages"));
            assertEquals(2, body.getInt("page"));
            assertEquals(25, body.getInt("limit"));
            assertEquals(0, body.getJsonArray("rows").size());
        }
    
        @Test
        void doGet_whenSelectionDateComesFromSelection_appliesDateFilter() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WidgetReviewStartServlet.Selection selection = newSelection(null, List.of("chat-1"), null, null, "2026-08-07");
            HttpSession session = authedSessionWithSelection(selection);
    
            DataSource dataSource = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet dataRs = mock(ResultSet.class);
            Array sqlArray = mock(Array.class);
    
            when(dataSource.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(conn.createArrayOf(eq("text"), Mockito.<Object[]>any())).thenReturn(sqlArray);
            when(ps.executeQuery()).thenReturn(dataRs);
    
            when(dataRs.next()).thenReturn(false);
    
            mockCdiDataSource(dataSource);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(req.getParameterValues("date")).thenReturn(null);
            when(req.getParameterValues("search")).thenReturn(null);
            when(req.getParameterValues("sortColumn")).thenReturn(new String[]{"created_at"});
            when(req.getParameterValues("sortDir")).thenReturn(new String[]{"desc"});
            when(req.getParameterValues("page")).thenReturn(new String[]{"1"});
            when(req.getParameterValues("limit")).thenReturn(new String[]{"10"});
            when(resp.getStatus()).thenReturn(0);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            verify(ps, times(2)).setTimestamp(anyInt(), any(Timestamp.class));
    
            JsonObject body = Json.createReader(new StringReader(out.toString())).readObject();
            assertEquals("ok", body.getString("status"));
            assertEquals(0, body.getInt("totalRows"));
            assertEquals(0, body.getJsonArray("rows").size());
        }
    
        @Test
        void doGet_whenSelectionDateMalformed_ignoresDateFilterAndContinues() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WidgetReviewStartServlet.Selection selection = newSelection(null, List.of("chat-1"), null, null, "not-a-date");
            HttpSession session = authedSessionWithSelection(selection);
    
            DataSource dataSource = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet dataRs = mock(ResultSet.class);
            Array sqlArray = mock(Array.class);
    
            when(dataSource.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(conn.createArrayOf(eq("text"), Mockito.<Object[]>any())).thenReturn(sqlArray);
            when(ps.executeQuery()).thenReturn(dataRs);
            when(dataRs.next()).thenReturn(false);
    
            mockCdiDataSource(dataSource);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("selectionId")).thenReturn(new String[]{"sid"});
            when(req.getParameterValues("date")).thenReturn(null);
            when(req.getParameterValues("search")).thenReturn(null);
            when(req.getParameterValues("sortColumn")).thenReturn(new String[]{"created_at"});
            when(req.getParameterValues("sortDir")).thenReturn(new String[]{"desc"});
            when(req.getParameterValues("page")).thenReturn(new String[]{"1"});
            when(req.getParameterValues("limit")).thenReturn(new String[]{"10"});
            when(resp.getStatus()).thenReturn(0);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            verify(ps, times(0)).setTimestamp(anyInt(), any(Timestamp.class));
            JsonObject body = Json.createReader(new StringReader(out.toString())).readObject();
            assertEquals("ok", body.getString("status"));
        }
    
        @Test
        void doGet_whenUnhandledExceptionAndFallbackSendErrorFails_swallowsFallbackFailure() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            when(req.getSession(false)).thenThrow(new RuntimeException("boom"));
            when(resp.isCommitted()).thenReturn(false);
            Mockito.doThrow(new IOException("send down"))
                    .when(resp)
                    .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    
            servlet.doGet(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    
        @Test
        void helperMethods_writeJsonFallbacksAndInternalDtos_coverBranches() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
    
            try (MockedStatic<WidgetStore> widgets = Mockito.mockStatic(WidgetStore.class)) {
                widgets.when(() -> WidgetStore.list(null)).thenThrow(new SQLException("widget down"));
                assertEquals("wid-1", invoke(servlet, "resolveWidgetDisplayNameUncached", new Class[]{String.class}, "wid-1"));
            }
    
            HttpServletResponse textResp = mock(HttpServletResponse.class);
            when(textResp.getWriter()).thenThrow(new IOException("writer down"));
            when(textResp.isCommitted()).thenReturn(false);
            Mockito.doThrow(new IOException("send down"))
                    .when(textResp)
                    .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
    
            invoke(servlet, "writeJson", new Class[]{HttpServletResponse.class, String.class}, textResp, "{}");
            verify(textResp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
    
            HttpServletResponse objResp = mock(HttpServletResponse.class);
            when(objResp.getStatus()).thenReturn(0);
            when(objResp.isCommitted()).thenReturn(false);
            Mockito.doThrow(new IOException("send down"))
                    .when(objResp)
                    .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
    
            try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
                jsonUtil.when(() -> ServletJsonResponseUtil.writeJson(eq(objResp), anyInt(), any(JsonObject.class)))
                        .thenThrow(new IOException("json down"));
                invoke(servlet, "writeJson", new Class[]{HttpServletResponse.class, JsonObject.class}, objResp,
                        Json.createObjectBuilder().add("status", "ok").build());
            }
            verify(objResp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
    
            Class<?> qpClass = Class.forName("com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet$QueryParts");
            Constructor<?> qpCtor = qpClass.getDeclaredConstructor(String.class);
            qpCtor.setAccessible(true);
            Object qp = qpCtor.newInstance("SELECT 1");
            Field sql = qpClass.getDeclaredField("sql");
            sql.setAccessible(true);
            assertEquals("SELECT 1", sql.get(qp));
    
            Class<?> stClass = Class.forName("com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet$SearchTerms");
            Constructor<?> stCtor = stClass.getDeclaredConstructor(String.class, String.class, String.class);
            stCtor.setAccessible(true);
            Object st = stCtor.newInstance("g", "p", "r");
            Field global = stClass.getDeclaredField("global");
            global.setAccessible(true);
            assertEquals("g", global.get(st));
    
            Class<?> rowClass = Class.forName("com.sim.chatserver.web.dashboard.drilldown.WidgetReviewDataServlet$ChatRow");
            Constructor<?> rowCtor = rowClass.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class);
            rowCtor.setAccessible(true);
            Object row = rowCtor.newInstance("c", "p", "r", "t", "s");
            Field chatId = rowClass.getDeclaredField("chatId");
            chatId.setAccessible(true);
            assertEquals("c", chatId.get(row));
    
            assertFalse((Boolean) invoke(servlet, "containsIgnoreCase", new Class[]{String.class, String.class}, "abc", null));
        }
    
        @Test
        void snapshotPathAndHelpers_coverBranches() throws Exception {
            WidgetReviewDataServlet servlet = new WidgetReviewDataServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            WidgetReviewStartServlet.Selection snapshotSelection = newSelection(
                    "widget-1",
                    List.of(),
                    new ArrayList<>(),
                    newSearchTerms("global", "prompt", "response"),
                    null);
    
            when(req.getParameterValues("search")).thenReturn(new String[]{"q"});
            when(req.getParameterValues("sortColumn")).thenReturn(new String[]{"session_id"});
            when(req.getParameterValues("sortDir")).thenReturn(new String[]{"desc"});
            when(req.getParameterValues("limit")).thenReturn(new String[]{"all"});
            when(req.getParameterValues("page")).thenReturn(new String[]{"9"});
            when(resp.getStatus()).thenReturn(0);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            invoke(
                    servlet,
                    "handleSnapshotSelection",
                    new Class[]{WidgetReviewStartServlet.Selection.class, HttpServletRequest.class, HttpServletResponse.class, long.class},
                    snapshotSelection,
                    req,
                    resp,
                    System.nanoTime());
    
            JsonObject snapshotBody = Json.createReader(new StringReader(out.toString())).readObject();
            assertEquals("ok", snapshotBody.getString("status"));
            assertEquals(0, snapshotBody.getInt("totalRows"));
            assertEquals(1, snapshotBody.getInt("page"));
            assertEquals(20000, snapshotBody.getInt("limit"));
    
            assertEquals("abc_1", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "abc-1"));
            assertEquals("w_123", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123"));
            assertNull(invoke(servlet, "sanitizeSelectionId", new Class[]{String.class}, "***"));
            assertEquals("sid_1", invoke(servlet, "sanitizeSelectionId", new Class[]{String.class}, "sid_1"));
    
            assertEquals("created_at", invoke(servlet, "parseSortColumn", new Class[]{String.class}, "BAD_COL"));
            assertEquals("ASC", invoke(servlet, "parseSortDirection", new Class[]{String.class}, "asc"));
            assertEquals("DESC", invoke(servlet, "parseSortDirection", new Class[]{String.class}, "ignored"));
    
            assertEquals(50, invoke(servlet, "clampLimit", new Class[]{int.class}, 0));
            assertEquals(true, invoke(servlet, "isUnlimitedLimit", new Class[]{String.class, Integer.class}, "all", null));
            assertEquals(true, invoke(servlet, "isUnlimitedLimit", new Class[]{String.class, Integer.class}, "-1", Integer.valueOf(-1)));
            assertNull(invoke(servlet, "parseIntegerOrNull", new Class[]{String.class}, "not-a-number"));
            assertEquals(7, invoke(servlet, "valueOrDefault", new Class[]{Integer.class, int.class}, null, 7));
            assertEquals(9, invoke(servlet, "parseInteger", new Class[]{String.class, int.class}, "bad", 9));
    
            assertNull(invoke(servlet, "trimToNull", new Class[]{String.class}, "  \n  "));
            assertEquals("", invoke(servlet, "nullToEmpty", new Class[]{String.class}, new Object[]{null}));
            assertEquals("1.24", invoke(servlet, "twoDecimals", new Class[]{double.class}, 1.236));
    
            @SuppressWarnings("unchecked")
            List<TermChatSnapshot> filtered = (List<TermChatSnapshot>) invoke(
                    servlet,
                    "filterSnapshots",
                    new Class[]{List.class, String.class},
                    List.of(
                            new TermChatSnapshot("t", "w", "c1", "Prompt A", "Resp A", Timestamp.from(Instant.now()), "s1"),
                            new TermChatSnapshot("t", "w", "c2", "Prompt B", "Resp B", Timestamp.from(Instant.now()), "s2")),
                    "resp b");
            assertEquals(1, filtered.size());
    
            ResultSet rs = mock(ResultSet.class);
            when(rs.getString("col")).thenThrow(new SQLException("bad read"));
            assertEquals("", invoke(servlet, "nullable", new Class[]{ResultSet.class, String.class}, rs, "col"));
    
            String ts = (String) invoke(
                    servlet,
                    "formatTimestamp",
                    new Class[]{Timestamp.class},
                    Timestamp.from(Instant.parse("2026-08-07T00:00:00Z")));
            assertNotNull(ts);
            assertTrue(ts.startsWith("2026-08-07T00:00:00Z"));
    
            Object qp = invoke(
                    servlet,
                    "buildQuery",
                    new Class[]{String.class, String.class, String.class, String.class, LocalDate.class},
                    "widget",
                    "prompt",
                    "ASC",
                    "abc",
                    LocalDate.parse("2026-08-07"));
            Field sql = qp.getClass().getDeclaredField("sql");
            sql.setAccessible(true);
            String builtSql = (String) sql.get(qp);
            assertTrue(builtSql.contains("ILIKE"));
            assertTrue(builtSql.contains("created_at >= ?"));
    
            assertThrows(IllegalArgumentException.class,
                    () -> invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
        }
    
        private void mockCdiDataSource(DataSource dataSource) {
            AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
            when(dsHolder.getDataSource()).thenReturn(dataSource);
    
            @SuppressWarnings("unchecked")
            CDI<Object> cdi = mock(CDI.class);
            @SuppressWarnings("unchecked")
            Instance<AppDataSourceHolder> instance = mock(Instance.class);
            when(instance.get()).thenReturn(dsHolder);
            when(cdi.select(AppDataSourceHolder.class)).thenReturn((Instance) instance);
    
            cdiMock = Mockito.mockStatic(CDI.class);
            cdiMock.when(CDI::current).thenReturn(cdi);
        }
    
        private static HttpSession authedSessionWithSelection(WidgetReviewStartServlet.Selection selection) {
            HttpSession session = mock(HttpSession.class);
            Map<String, WidgetReviewStartServlet.Selection> selections = new HashMap<>();
            if (selection != null) {
                selections.put("sid", selection);
            }
            when(session.getAttribute("user")).thenReturn("admin");
            when(session.getAttribute("widgetReviewSelections")).thenReturn(selections);
            return session;
        }
    
        private static WidgetReviewStartServlet.SearchTerms newSearchTerms(String global, String prompt, String response)
                throws Exception {
            Class<?> searchTermsClass = Class.forName(
                    "com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet$SearchTerms");
            Constructor<?> searchCtor = searchTermsClass.getDeclaredConstructor(String.class, String.class, String.class);
            searchCtor.setAccessible(true);
            return (WidgetReviewStartServlet.SearchTerms) searchCtor.newInstance(global, prompt, response);
        }
    
        private static WidgetReviewStartServlet.Selection newSelection(
                String widgetId,
                List<String> chatIds,
                List<TermChatSnapshot> snapshots,
                WidgetReviewStartServlet.SearchTerms searchTerms,
                String date) throws Exception {
            Class<?> searchTermsClass = Class.forName(
                    "com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet$SearchTerms");
            Class<?> selectionClass = Class.forName(
                    "com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet$Selection");
    
            Object st = searchTerms;
            if (st == null) {
                Constructor<?> searchCtor = searchTermsClass.getDeclaredConstructor(String.class, String.class, String.class);
                searchCtor.setAccessible(true);
                st = searchCtor.newInstance("", "", "");
            }
    
            Constructor<?> selectionCtor = selectionClass.getDeclaredConstructor(
                    String.class,
                    String.class,
                    String.class,
                    List.class,
                    List.class,
                    searchTermsClass,
                    String.class);
            selectionCtor.setAccessible(true);
    
            return (WidgetReviewStartServlet.Selection) selectionCtor.newInstance(
                    widgetId,
                    widgetId,
                    null,
                    chatIds == null ? List.of() : chatIds,
                    snapshots,
                    st,
                    date);
        }
    
        private static void clearStaticCache(String fieldName) throws Exception {
            Field field = WidgetReviewDataServlet.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            if (value instanceof Map<?, ?> map) {
                map.clear();
            }
        }
    
        private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }
    
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // no-op for tests
                }
    
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }
            };
        }
    
        private static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
            Method method = target.getClass().getDeclaredMethod(name, types);
            method.setAccessible(true);
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Exception exception) {
                    throw exception;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw ite;
            }
        }
}
