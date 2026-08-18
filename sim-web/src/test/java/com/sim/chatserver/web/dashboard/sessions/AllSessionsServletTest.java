package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletMapping;
/**
 * Parasoft Jtest UTA: Test class for AllSessionsServlet
 *
 * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet
 * @author bmcmullin
 */
public class AllSessionsServletTest
{
    private MockedStatic<CDI> cdiMock;

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = null; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

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
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/chats"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

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
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = null; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

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
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = null; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost9() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost10() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost11() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

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
        underTest.doPost(req, resp);

    }



    // Merged from AllSessionsServletCoverageTest
    
    
        @AfterEach
        void resetStatics() {
            if (cdiMock != null) {
                cdiMock.close();
                cdiMock = null;
            }
        }
    
        @Test
        void doGet_summaryPath_authenticatedAndNoWidgets_returnsOk() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/data");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of());
    
                servlet.doGet(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("ok", body.getString("status"));
            assertEquals("all", body.getString("activity"));
            assertEquals(0, body.getInt("totalSessions"));
            assertEquals(1, body.getInt("page"));
        }
    
        @Test
        void doGet_chatsPath_missingSessionId_returns400() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpSession session = authedSession();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/chats");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("sessionid required"));
        }
    
        @Test
        void doPost_selectPath_invalidContentLength_returns400() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpSession session = authedSession();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn(-1L);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("invalid json payload"));
        }
    
        @Test
        void doPost_selectPath_invalidJson_returns400() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpSession session = authedSession();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn(8L);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{bad")));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("invalid json payload"));
        }
    
        @Test
        void doPost_selectPath_missingSelectedChatIds_returns400() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpSession session = authedSession();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentType()).thenReturn("application/json");
            String payload = "{\"other\":\"x\"}";
            when(req.getContentLengthLong()).thenReturn((long) payload.length());
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(payload)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("selectedchatids required"));
        }
    
        @Test
        void doPost_selectPath_noValidChatIds_returns400() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpSession session = authedSession();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
                when(req.getContentType()).thenReturn("application/json");
            when(req.getContentLengthLong()).thenReturn(128L);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                    "\"selectedChatIds\":[\"   \",\"bad id\",\"***\",123]" +
                    "}")));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("no valid chat ids"));
        }
    
        @Test
        void doPost_unknownPath_returnsMethodNotAllowed() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/other/path");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("method not allowed"));
        }
    
        @Test
        void doGet_summaryPath_withWidgetAndNoRows_returnsOkAndWidgetNames() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/data");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rows = mock(ResultSet.class);
    
            when(holder.getDataSource()).thenReturn(ds);
            when(ds.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.<String[]>any()))
                .thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(org.mockito.ArgumentMatchers.contains("GROUP BY session_id"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rows);
            when(rows.next()).thenReturn(false);
    
            servlet = servletWithDataSourceHolder(holder);
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null))
                        .thenReturn(List.of(new WidgetEntry(1, "wid-1", "Widget One", Instant.now())));
    
                servlet.doGet(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("ok", body.getString("status"));
            assertEquals(0, body.getInt("totalSessions"));
            assertEquals("Widget One", body.getJsonObject("widgetNames").getString("wid-1"));
        }
    
        @Test
        void doGet_chatsPath_withRows_returnsOkRows() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/chats");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("sessionId")).thenReturn(new String[]{"sid-1"});
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rows = mock(ResultSet.class);
    
            when(holder.getDataSource()).thenReturn(ds);
            when(ds.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.<String[]>any()))
                .thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(org.mockito.ArgumentMatchers.contains("WHERE session_id = ?"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rows);
            when(rows.next()).thenReturn(true, false);
            when(rows.getString("widget_chat_id")).thenReturn("chat-1");
            when(rows.getString("prompt")).thenReturn("prompt one");
            when(rows.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-07T00:00:00Z")));
    
            servlet = servletWithDataSourceHolder(holder);
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null))
                        .thenReturn(List.of(new WidgetEntry(1, "wid-1", "Widget One", Instant.now())));
    
                servlet.doGet(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("ok", body.getString("status"));
            assertEquals(1, body.getJsonArray("rows").size());
            assertEquals("chat-1", body.getJsonArray("rows").getJsonObject(0).getString("chatId"));
        }
    
        @Test
        void doPost_selectPath_withValidSelection_returnsOk() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            ServletContext servletContext = mock(ServletContext.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getServletContext()).thenReturn(servletContext);
            when(servletContext.getContextPath()).thenReturn("/sim");
            when(req.getContentType()).thenReturn("application/json");
            when(req.getContentLengthLong()).thenReturn(64L);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"selectedChatIds\":[\"chat-1\"]}")));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rows = mock(ResultSet.class);
    
            when(holder.getDataSource()).thenReturn(ds);
            when(ds.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.<String[]>any()))
                .thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(org.mockito.ArgumentMatchers.contains("WHERE widget_chat_id = ?"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rows);
            when(rows.next()).thenReturn(true, false);
            when(rows.getString("widget_chat_id")).thenReturn("chat-1");
            when(rows.getString("prompt")).thenReturn("prompt one");
            when(rows.getString("response_text")).thenReturn("response one");
            when(rows.getString("session_id")).thenReturn("sid-1");
            when(rows.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-07T00:00:00Z")));
    
            servlet = servletWithDataSourceHolder(holder);
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null))
                        .thenReturn(List.of(new WidgetEntry(1, "wid-1", "Widget One", Instant.now())));
    
                servlet.doPost(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("ok", body.getString("status"));
            assertTrue(body.getString("selectionId") != null && !body.getString("selectionId").isBlank());
            assertEquals(1, body.getInt("count"));
        }
    
        @Test
        void privateHelpers_parseAndSanitize_coverBranches() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
    
            assertEquals(5, (Integer) invoke(servlet, "parseInteger", new Class[]{String.class, int.class}, "5", 9));
            assertEquals(9, (Integer) invoke(servlet, "parseInteger", new Class[]{String.class, int.class}, "x", 9));
            assertEquals(1, (Integer) invoke(servlet, "clamp", new Class[]{int.class, int.class, int.class}, -3, 1, 10));
            assertEquals(10, (Integer) invoke(servlet, "clamp", new Class[]{int.class, int.class, int.class}, 33, 1, 10));
            assertTrue((Boolean) invoke(servlet, "parseBooleanParam", new Class[]{String.class}, "true"));
            assertFalse((Boolean) invoke(servlet, "parseBooleanParam", new Class[]{String.class}, "t"));
    
            assertNull(invoke(servlet, "sanitizeTextParam", new Class[]{String.class, int.class}, null, 4));
            assertEquals("", invoke(servlet, "sanitizeTextParam", new Class[]{String.class, int.class}, "   ", 4));
            assertEquals("abcd", invoke(servlet, "sanitizeTextParam", new Class[]{String.class, int.class}, "abcdef", 4));
            assertEquals("all", invoke(servlet, "sanitizeActivity", new Class[]{String.class}, "junk"));
            assertEquals("active", invoke(servlet, "sanitizeActivity", new Class[]{String.class}, " ACTIVE "));
    
            assertEquals("abc_1", invoke(servlet, "sanitizeSessionId", new Class[]{String.class}, " abc_1 "));
            assertNull(invoke(servlet, "sanitizeSessionId", new Class[]{String.class}, "bad id"));
            assertEquals("chat-1", invoke(servlet, "sanitizeChatId", new Class[]{String.class}, " chat-1 "));
            assertNull(invoke(servlet, "sanitizeChatId", new Class[]{String.class}, "@@@"));
    
            assertEquals("widget", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, ""));
            assertEquals("w_123abc", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123abc"));
            assertEquals("abc____", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "abc-:.$"));
    
            assertEquals("\"valid_name\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "valid_name"));
            assertThrowsIllegalArgument(() -> invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
        }
    
        @Test
        void privateHelpers_pathFormattingAndBody_coverBranches() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
    
            assertEquals("/dashboard/sessions/data", invoke(servlet, "normalizeServletPath", new Class[]{String.class}, (Object) null));
            assertEquals("/dashboard/sessions/chats", invoke(servlet, "normalizeServletPath", new Class[]{String.class}, " /dashboard/sessions/chats "));
            assertEquals("/dashboard/sessions/data", invoke(servlet, "normalizeServletPath", new Class[]{String.class}, "/unknown"));

            assertEquals("x y", invoke(servlet, "safeDbText", new Class[]{String.class, int.class}, "x\u0000\ny", 10));
            assertEquals("abcd", invoke(servlet, "safeDbText", new Class[]{String.class, int.class}, "abcdef", 4));
        }
    
        @Test
        void privateHelpers_tableExistsAndReadDbText_coverBranches() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
    
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet rs1 = mock(ResultSet.class);
            ResultSet rs2 = mock(ResultSet.class);
            ResultSet rs3 = mock(ResultSet.class);
    
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, null, "widget", new String[]{"TABLE"})).thenReturn(rs1);
            when(meta.getTables(null, null, "WIDGET", new String[]{"TABLE"})).thenReturn(rs2);
            when(meta.getTables(null, null, "widget", new String[]{"TABLE"})).thenReturn(rs1, rs3);
            when(rs1.next()).thenReturn(false);
            when(rs2.next()).thenReturn(false);
            when(rs3.next()).thenReturn(true);
    
            boolean exists = (Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, conn, "widget");
            assertTrue(exists);
    
            Connection badConn = mock(Connection.class);
            when(badConn.getMetaData()).thenThrow(new SQLException("meta boom"));
            boolean missing = (Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, badConn, "widget");
            assertFalse(missing);
    
            ResultSet dbRow = mock(ResultSet.class);
            when(dbRow.getString("session_id")).thenReturn(" ab\ncd ");
            assertEquals("abcd", invoke(servlet, "readDbText", new Class[]{ResultSet.class, String.class, int.class}, dbRow, "session_id", 16));
    
            ResultSet badRow = mock(ResultSet.class);
            when(badRow.getString("session_id")).thenThrow(new SQLException("read boom"));
            assertNull(invoke(servlet, "readDbText", new Class[]{ResultSet.class, String.class, int.class}, badRow, "session_id", 16));
        }
    
        @Test
        void doGet_chatsPath_sqlFailure_returns500() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/chats");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("sessionId")).thenReturn(new String[]{"sid-1"});
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
    
            when(holder.getDataSource()).thenReturn(ds);
            when(ds.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.<String[]>any()))
                .thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(org.mockito.ArgumentMatchers.contains("WHERE session_id = ?"))).thenReturn(ps);
            org.mockito.Mockito.doThrow(new SQLException("bind fail")).when(ps).setString(1, "sid-1");
    
            servlet = servletWithDataSourceHolder(holder);
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null))
                        .thenReturn(List.of(new WidgetEntry(1, "wid-1", "Widget One", Instant.now())));
    
                servlet.doGet(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("unable to load chats"));
        }
    
        @Test
        void doPost_selectPath_validIdsButNoRows_returns404() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentType()).thenReturn("application/json");
            when(req.getContentLengthLong()).thenReturn(64L);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"selectedChatIds\":[\"chat-1\"]}")));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rows = mock(ResultSet.class);
    
            when(holder.getDataSource()).thenReturn(ds);
            when(ds.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.<String[]>any()))
                .thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(org.mockito.ArgumentMatchers.contains("WHERE widget_chat_id = ?"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rows);
            when(rows.next()).thenReturn(false);
    
            servlet = servletWithDataSourceHolder(holder);
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null))
                        .thenReturn(List.of(new WidgetEntry(1, "wid-1", "Widget One", Instant.now())));
    
                servlet.doPost(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("no matching chats"));
        }
    
        @Test
        void privateHelpers_authConnectionAndWriters_coverFallbackBranches() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
    
            HttpServletResponse unauthorizedResp = mock(HttpServletResponse.class);
            ByteArrayOutputStream unauthorizedOut = new ByteArrayOutputStream();
            when(unauthorizedResp.getOutputStream()).thenReturn(servletOutput(unauthorizedOut));
            assertFalse((Boolean) invoke(servlet, "requireAuth", new Class[]{HttpServletRequest.class, HttpServletResponse.class}, null, unauthorizedResp));
            JsonObject unauthorizedBody = jsonBody(unauthorizedOut);
            assertEquals("error", unauthorizedBody.getString("status"));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            DataSource ds = mock(DataSource.class);
            when(holder.getDataSource()).thenReturn(ds);
            when(ds.getConnection()).thenThrow(new SQLException("conn fail"));
            servlet = servletWithDataSourceHolder(holder);
            try {
                invoke(servlet, "openConnectionSafe", new Class[]{});
                throw new AssertionError("Expected IllegalStateException");
            } catch (InvocationTargetException ite) {
                assertTrue(ite.getCause() instanceof IllegalStateException);
            }
    
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement("SELECT 1")).thenThrow(new SQLException("prep fail"));
            try {
                invoke(servlet, "prepareStatementSafe", new Class[]{Connection.class, String.class}, conn, "SELECT 1");
                throw new AssertionError("Expected IllegalStateException");
            } catch (InvocationTargetException ite) {
                assertTrue(ite.getCause() instanceof IllegalStateException);
            }
    
            HttpServletResponse committedResp = mock(HttpServletResponse.class);
            when(committedResp.getOutputStream()).thenThrow(new IOException("write fail"));
            when(committedResp.isCommitted()).thenReturn(true);
            invoke(servlet, "writeJson", new Class[]{HttpServletResponse.class, int.class, JsonObject.class},
                    committedResp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add("status", "ok").build());
            verify(committedResp, never()).sendError(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyString());
    
            HttpServletResponse fallbackResp = mock(HttpServletResponse.class);
            when(fallbackResp.getOutputStream()).thenThrow(new IOException("write fail"));
            when(fallbackResp.isCommitted()).thenReturn(false);
            org.mockito.Mockito.doThrow(new IOException("send fail")).when(fallbackResp)
                    .sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");
            invoke(servlet, "writeError", new Class[]{HttpServletResponse.class, int.class, String.class},
                    fallbackResp, HttpServletResponse.SC_BAD_REQUEST, "bad");
        }
    
        @Test
        void privateHelpers_searchResolutionAndParsing_coverAdditionalBranches() throws Exception {
            AllSessionsServlet servlet = new AllSessionsServlet();
    
            assertEquals(7, (Integer) invoke(servlet, "parseInteger", new Class[]{String.class, int.class}, "7", 1));
            assertEquals(1, (Integer) invoke(servlet, "parseInteger", new Class[]{String.class, int.class}, "9999999999", 1));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            when(req.getHttpServletMapping()).thenReturn(null);
            assertEquals("/dashboard/sessions/data", invoke(servlet, "resolveRequestPath", new Class[]{HttpServletRequest.class}, req));
    
            Map<String, Object> sessions = new LinkedHashMap<>();
            sessions.put("sid-1", newSessionSummary("sid-1"));
            sessions.put("foo-2", newSessionSummary("foo-2"));
            @SuppressWarnings("unchecked")
            Set<String> ids = (Set<String>) invoke(servlet,
                    "gatherSessionIdsByIdMatch",
                    new Class[]{Map.class, String.class},
                    sessions,
                    "sid");
            assertTrue(ids.contains("sid-1"));
            assertFalse(ids.contains("foo-2"));
    
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(org.mockito.ArgumentMatchers.contains("SELECT DISTINCT session_id"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("session_id")).thenReturn("sid-1", "bad id");
    
            @SuppressWarnings("unchecked")
            Set<String> found = (Set<String>) invoke(servlet,
                    "gatherSessionIdsForSearch",
                    new Class[]{Connection.class, String.class, String.class},
                    conn,
                    "widget_table",
                    "%x%");
            assertEquals(Set.of("sid-1"), found);
    
            Connection badConn = mock(Connection.class);
            PreparedStatement badPs = mock(PreparedStatement.class);
            when(badConn.prepareStatement(org.mockito.ArgumentMatchers.contains("SELECT DISTINCT session_id"))).thenReturn(badPs);
            when(badPs.executeQuery()).thenThrow(new SQLException("search fail"));
            @SuppressWarnings("unchecked")
            Set<String> empty = (Set<String>) invoke(servlet,
                    "gatherSessionIdsForSearch",
                    new Class[]{Connection.class, String.class, String.class},
                    badConn,
                    "widget_table",
                    "%x%");
            assertTrue(empty.isEmpty());
        }
    
        private static Object newSessionSummary(String sessionId) throws Exception {
            Class<?> summaryClass = Class.forName("com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet$SessionSummary");
            var ctor = summaryClass.getDeclaredConstructor(String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(sessionId);
        }
    
        private static HttpSession authedSession() {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn("admin");
            return session;
        }
    
        private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = findMethodInHierarchy(target.getClass(), methodName, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        }

        private static Method findMethodInHierarchy(Class<?> type, String methodName, Class<?>[] types) throws NoSuchMethodException {
            Class<?> current = type;
            while (current != null) {
                try {
                    return current.getDeclaredMethod(methodName, types);
                } catch (NoSuchMethodException ignored) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchMethodException(methodName);
        }
    
        private static void assertThrowsIllegalArgument(ThrowingRunnable action) {
            try {
                action.run();
            } catch (Exception ex) {
                Throwable cause = ex;
                if (ex instanceof InvocationTargetException ite && ite.getCause() != null) {
                    cause = ite.getCause();
                } else if (ex.getCause() != null) {
                    cause = ex.getCause();
                }
                assertTrue(cause instanceof IllegalArgumentException);
                return;
            }
            throw new AssertionError("Expected IllegalArgumentException");
        }
    
        @FunctionalInterface
        private interface ThrowingRunnable {
            void run() throws Exception;
        }
    
        private static JsonObject jsonBody(ByteArrayOutputStream out) {
            return Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        }
    
        private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }
    
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // no-op
                }
    
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }
            };
        }
        private AllSessionsServlet servletWithDataSourceHolder(AppDataSourceHolder dsHolder) {
            if (cdiMock != null) {
                cdiMock.close();
            }
            cdiMock = org.mockito.Mockito.mockStatic(CDI.class);

            CDI<Object> cdi = mock(CDI.class);
            @SuppressWarnings("unchecked")
            Instance<AppDataSourceHolder> dsHolderInstance = mock(Instance.class);
            when(cdi.select(AppDataSourceHolder.class)).thenReturn(dsHolderInstance);
            when(dsHolderInstance.get()).thenReturn(dsHolder);
            cdiMock.when(CDI::current).thenReturn(cdi);

            return new AllSessionsServlet();
        }
}

