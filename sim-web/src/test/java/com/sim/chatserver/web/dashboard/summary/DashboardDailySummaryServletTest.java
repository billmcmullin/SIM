package com.sim.chatserver.web.dashboard.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class DashboardDailySummaryServletTest {

    @Test
    void doGet_withoutSession_returnsUnauthorizedJson() throws Exception {
        DashboardDailySummaryServlet servlet = new DashboardDailySummaryServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void parseHelpers_acceptValidAndFallbackOnInvalid() throws Exception {
        DashboardDailySummaryServlet servlet = new DashboardDailySummaryServlet();

        Method parseDay = DashboardDailySummaryServlet.class
                .getDeclaredMethod("parseDay", String.class, ZoneId.class);
        parseDay.setAccessible(true);

        Method parseSlotOrCurrent = DashboardDailySummaryServlet.class
                .getDeclaredMethod("parseSlotOrCurrent", String.class, ZoneId.class);
        parseSlotOrCurrent.setAccessible(true);

        ZoneId zone = ZoneId.systemDefault();

        LocalDate explicit = (LocalDate) parseDay.invoke(servlet, "2026-08-24", zone);
        assertEquals(LocalDate.of(2026, 8, 24), explicit);

        LocalDate fallback = (LocalDate) parseDay.invoke(servlet, "not-a-date", zone);
        assertEquals(LocalDate.now(zone), fallback);

        int slot = (Integer) parseSlotOrCurrent.invoke(servlet, "2", zone);
        assertEquals(2, slot);

        int fallbackSlot = (Integer) parseSlotOrCurrent.invoke(servlet, "99", zone);
        assertTrue(fallbackSlot >= 0 && fallbackSlot <= 3);
    }
}
