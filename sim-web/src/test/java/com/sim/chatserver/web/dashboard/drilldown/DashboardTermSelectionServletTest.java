package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermsStore;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardTermSelectionServletTest {

    @Test
    void doGet_withoutSession_forwardsToLogin() throws Exception {
        DashboardTermSelectionServlet servlet = newServletWithMockedCdi();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getServletContext()).thenReturn(context);
        when(context.getContextPath()).thenReturn("/sim");
        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_jsonMode_withoutTerm_returnsBadRequestJson() throws Exception {
        DashboardTermSelectionServlet servlet = newServletWithMockedCdi();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        StringWriter out = new StringWriter();

        when(req.getServletContext()).thenReturn(context);
        when(context.getContextPath()).thenReturn("/sim");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/term-review/select");
        when(req.getParameter("term")).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("term parameter is required"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private DashboardTermSelectionServlet newServletWithMockedCdi() {
        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class)) {
            CDI cdi = mock(CDI.class);
            Instance<AppDataSourceHolder> dsInstance = (Instance<AppDataSourceHolder>) mock(Instance.class);
            Instance<TermsStore> termsInstance = (Instance<TermsStore>) mock(Instance.class);

            when(cdi.select(AppDataSourceHolder.class)).thenReturn((Instance) dsInstance);
            when(cdi.select(TermsStore.class)).thenReturn((Instance) termsInstance);
            when(dsInstance.get()).thenReturn(mock(AppDataSourceHolder.class));
            when(termsInstance.get()).thenReturn(mock(TermsStore.class));

            cdiStatic.when(CDI::current).thenReturn(cdi);
            return new DashboardTermSelectionServlet();
        }
    }
}
