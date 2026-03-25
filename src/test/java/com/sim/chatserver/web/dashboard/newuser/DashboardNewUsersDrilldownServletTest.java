package com.sim.chatserver.web.dashboard.newuser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardNewUsersDrilldownServletTest {

    private DashboardNewUsersDrilldownServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private ServletContext servletContext;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DashboardNewUsersDrilldownServlet();

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);

        when(req.getServletContext()).thenReturn(servletContext);
        when(req.getContextPath()).thenReturn("/chat-server");

        // inject dsHolder with mock datasource/connection
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(holder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        Field f = DashboardNewUsersDrilldownServlet.class.getDeclaredField("dsHolder");
        f.setAccessible(true);
        f.set(servlet, holder);
    }

    @Test
    void redirectsToLoginWhenNotAuthenticated() throws Exception {
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/chat-server/login");
    }

    @Test
    void parsePositiveIntWorks() throws Exception {
        Method m = DashboardNewUsersDrilldownServlet.class.getDeclaredMethod("parsePositiveInt", String.class);
        m.setAccessible(true);

        assertEquals(Optional.of(10), m.invoke(servlet, "10"));
        assertEquals(Optional.empty(), m.invoke(servlet, "0"));
        assertEquals(Optional.empty(), m.invoke(servlet, "-1"));
        assertEquals(Optional.empty(), m.invoke(servlet, "abc"));
        assertEquals(Optional.empty(), m.invoke(servlet, ""));
    }

    @Test
    void parseDateWorks() throws Exception {
        Method m = DashboardNewUsersDrilldownServlet.class.getDeclaredMethod("parseDate", String.class);
        m.setAccessible(true);

        assertEquals(Optional.of(LocalDate.of(2026, 3, 25)), m.invoke(servlet, "2026-03-25"));
        assertEquals(Optional.empty(), m.invoke(servlet, "03/25/2026"));
        assertEquals(Optional.empty(), m.invoke(servlet, "bad"));
    }

    @Test
    void sanitizeWidgetTableNameNormalizes() throws Exception {
        Method m = DashboardNewUsersDrilldownServlet.class.getDeclaredMethod("sanitizeWidgetTableName", String.class);
        m.setAccessible(true);

        String result = (String) m.invoke(servlet, "123 bad-id");
        assertTrue(result.startsWith("w_"));
        assertFalse(result.contains("-"));
        assertFalse(result.contains(" "));
    }
}
