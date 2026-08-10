package com.sim.chatserver.web.dashboard.newuser;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
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
 * Parasoft Jtest UTA: Test class for DashboardNewUsersServlet
 *
 * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet
 * @author bmcmullin
 */
public class DashboardNewUsersServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = "/data"; // UTA: configured value
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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = "/data"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet11() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet12() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet13() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet14() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }



    // Merged from DashboardNewUsersServletCoverageTest
    
    
        @Test
        void doGet_whenUnauthenticatedPage_forwardsToLogin() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
            HttpServletMapping mapping = mock(HttpServletMapping.class);
    
            when(mapping.getPattern()).thenReturn("/dashboard/new-users");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(null);
            when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);
    
            servlet.doGet(req, resp);
    
            verify(dispatcher).forward(req, resp);
        }
    
        @Test
        void doGet_whenUnauthenticatedData_returns401Json() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(mapping.getPattern()).thenReturn("/dashboard/new-users/data");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("authentication"));
        }
    
        @Test
        void doGet_dataPath_authenticated_returnsOkWithEmptyRows() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(mapping.getPattern()).thenReturn("/dashboard/new-users/data");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContextPath()).thenReturn("/chat-server");
            when(req.getParameterValues("days")).thenReturn(new String[]{"14"});
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("ok", body.getString("status"));
            assertTrue(body.containsKey("trend"));
            assertEquals(0, body.getJsonArray("latest").size());
        }
    
        @Test
        void doGet_dayPath_missingDay_returns400() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(mapping.getPattern()).thenReturn("/dashboard/new-users/day");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("day")).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doGet(req, resp);
    
            JsonObject body = jsonBody(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("invalid day"));
        }
    
        @Test
        void doGet_dayDataPath_validDay_returnsOkWhenNoRows() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            ServletContext context = mock(ServletContext.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(mapping.getPattern()).thenReturn("/dashboard/new-users/day-data");
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("day")).thenReturn(new String[]{"2026-08-01"});
            when(req.getServletContext()).thenReturn(context);
            when(context.getContextPath()).thenReturn("/chat-server");
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (MockedStatic<WidgetStore> widgetStore = org.mockito.Mockito.mockStatic(WidgetStore.class)) {
                widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of());
                servlet.doGet(req, resp);
            }
    
            JsonObject body = jsonBody(out);
            assertEquals("ok", body.getString("status"));
            assertEquals(0, body.getInt("count"));
        }
    
        @Test
        void doGet_pagePath_authenticated_rendersTemplate() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = authedSession();
            ServletContext context = mock(ServletContext.class);
            StringWriter text = new StringWriter();
            PrintWriter writer = new PrintWriter(text);
    
            String template = "${contextPath}|${user}|${trendJson}|${latestRows}|${rangeStart}|${rangeEnd}|${selectedDays}";
    
            when(req.getHttpServletMapping()).thenReturn(null);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContextPath()).thenReturn("/ctx");
            when(req.getParameterValues("days")).thenReturn(new String[]{"30"});
            when(req.getServletContext()).thenReturn(context);
            when(context.getResourceAsStream("/WEB-INF/views/dashboard_new_users.html"))
                    .thenReturn(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
            when(resp.getWriter()).thenReturn(writer);
    
            servlet.doGet(req, resp);
            writer.flush();
    
            String rendered = text.toString();
            assertTrue(rendered.contains("/ctx"));
            assertTrue(rendered.contains("admin"));
            assertTrue(rendered.contains("30"));
        }
    
        @Test
        void privateHelpers_parseAndStringUtilities_coverBranches() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
    
            assertEquals(OptionalInt.of(7), invoke(servlet, "parseDays", new Class[]{String.class}, "7"));
            assertEquals(OptionalInt.empty(), invoke(servlet, "parseDays", new Class[]{String.class}, "8"));
            assertEquals(OptionalInt.empty(), invoke(servlet, "parseDays", new Class[]{String.class}, "x"));
            assertEquals(OptionalInt.empty(), invoke(servlet, "parseDays", new Class[]{String.class}, "   "));
    
            assertEquals(Optional.of(LocalDate.of(2026, 8, 1)), invoke(servlet, "parseLocalDate", new Class[]{String.class}, "2026-08-01"));
            assertEquals(Optional.empty(), invoke(servlet, "parseLocalDate", new Class[]{String.class}, "2026/08/01"));
    
            assertEquals("widget", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, ""));
            assertEquals("w_123", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123"));
            assertEquals("abc___", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "abc-.$"));
    
            assertEquals("\"valid_name\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "valid_name"));
            assertThrowsIllegalArgument(() -> invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
    
            assertEquals("a&amp;b&lt;c&gt;d&quot;e&#39;", invoke(servlet, "escapeHtml", new Class[]{String.class}, "a&b<c>d\"e'"));
            assertEquals("a\\\\b\\'c\\n", invoke(servlet, "escapeForJs", new Class[]{String.class}, "a\\b'c\n"));
            assertEquals("", invoke(servlet, "safe", new Class[]{String.class}, new Object[]{null}));
            assertEquals("abc", invoke(servlet, "safe", new Class[]{String.class}, "abc"));
    
            assertEquals(2, invoke(servlet, "getTotalChats", new Class[]{Map.class, String.class}, Map.of("s1", Integer.valueOf(2)), "s1"));
            assertEquals(0, invoke(servlet, "getTotalChats", new Class[]{Map.class, String.class}, null, "s1"));
            assertEquals(0, invoke(servlet, "getTotalChats", new Class[]{Map.class, String.class}, Map.of("s1", Integer.valueOf(2)), "s2"));
    
            assertEquals("/dashboard/new-users", invoke(servlet, "normalizeServletPath", new Class[]{String.class}, "/unknown"));
            assertEquals("/dashboard/new-users/day", invoke(servlet, "normalizeServletPath", new Class[]{String.class}, "/dashboard/new-users/day"));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletMapping mapping = mock(HttpServletMapping.class);
            when(req.getHttpServletMapping()).thenReturn(mapping);
            when(mapping.getPattern()).thenReturn("/dashboard/new-users/data");
            assertEquals("/dashboard/new-users/data", invoke(servlet, "resolveRequestPath", new Class[]{HttpServletRequest.class}, req));
        }
    
        @Test
        void privateHelpers_tableAndTemplate_coverBranches() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
    
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
    
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, null, "widget", new String[]{"TABLE"})).thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
    
            boolean exists = (Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, conn, "widget");
            assertTrue(exists);
    
            Connection badConn = mock(Connection.class);
            when(badConn.getMetaData()).thenThrow(new java.sql.SQLException("meta failed"));
            boolean missing = (Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, badConn, "widget");
            assertFalse(missing);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            ServletContext context = mock(ServletContext.class);
            when(req.getServletContext()).thenReturn(context);
            when(context.getResourceAsStream("/tpl"))
                    .thenReturn(new ByteArrayInputStream("line1\nline2".getBytes(StandardCharsets.UTF_8)));
            String loaded = (String) invoke(servlet, "loadTemplate", new Class[]{HttpServletRequest.class, String.class}, req, "/tpl");
            assertTrue(loaded.contains("line1"));
            assertTrue(loaded.contains("line2"));
    
            assertEquals("", invoke(servlet, "loadTemplate", new Class[]{HttpServletRequest.class, String.class}, null, "/tpl"));
            assertEquals("", invoke(servlet, "loadTemplate", new Class[]{HttpServletRequest.class, String.class}, req, " "));
        }
    
        @Test
        void privateHelpers_findEarliestAndTotals_coverSqlLoops() throws Exception {
            DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
    
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement psEarliest = mock(PreparedStatement.class);
            PreparedStatement psTotals = mock(PreparedStatement.class);
            ResultSet rsEarliest = mock(ResultSet.class);
            ResultSet rsTotals = mock(ResultSet.class);
    
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, null, "widget_1", new String[]{"TABLE"})).thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true, true);
    
            when(conn.prepareStatement(anyString())).thenReturn(psEarliest, psTotals);
    
            when(psEarliest.executeQuery()).thenReturn(rsEarliest);
            when(rsEarliest.next()).thenReturn(true, true, true, false);
            when(rsEarliest.getString("session_id")).thenReturn("s1", " s1 ", " ");
            when(rsEarliest.getTimestamp("first_seen")).thenReturn(
                    Timestamp.from(Instant.parse("2026-08-01T10:00:00Z")),
                    Timestamp.from(Instant.parse("2026-08-01T09:00:00Z")),
                    Timestamp.from(Instant.parse("2026-08-01T08:00:00Z")));
    
            when(psTotals.executeQuery()).thenReturn(rsTotals);
            when(rsTotals.next()).thenReturn(true, true, true, false);
            when(rsTotals.getString("session_id")).thenReturn("s1", "s2", " ");
            when(rsTotals.getInt("c")).thenReturn(2, 5, 9);
    
            List<WidgetEntry> widgets = new java.util.ArrayList<>();
            widgets.add(null);
            widgets.add(new WidgetEntry(1, "widget-1", "Widget One", Instant.parse("2026-08-01T00:00:00Z")));
            widgets.add(new WidgetEntry(2, "   ", "Ignore", Instant.parse("2026-08-01T00:00:00Z")));
    
            @SuppressWarnings("unchecked")
            Map<String, Timestamp> earliest = (Map<String, Timestamp>) invoke(
                    servlet,
                    "findEarliestBySession",
                    new Class[]{Connection.class, List.class},
                    conn,
                    widgets);
    
            assertEquals(1, earliest.size());
            assertTrue(earliest.containsKey("s1"));
            assertEquals(Timestamp.from(Instant.parse("2026-08-01T09:00:00Z")), earliest.get("s1"));
    
            @SuppressWarnings("unchecked")
            Map<String, Integer> totals = (Map<String, Integer>) invoke(
                    servlet,
                    "findTotalChatsBySession",
                    new Class[]{Connection.class, List.class},
                    conn,
                    widgets);
    
            assertEquals(Integer.valueOf(2), totals.get("s1"));
            assertEquals(Integer.valueOf(5), totals.get("s2"));
        }
    
        private static HttpSession authedSession() {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn("admin");
            return session;
        }
    
        private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = target.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(target, args);
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
            String text = out.toString(StandardCharsets.UTF_8);
            return Json.createReader(new StringReader(text)).readObject();
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
}
