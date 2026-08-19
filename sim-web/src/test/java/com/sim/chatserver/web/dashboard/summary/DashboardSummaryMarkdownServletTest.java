package com.sim.chatserver.web.dashboard.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;

class DashboardSummaryMarkdownServletTest {

    @Test
    void parseDay_acceptsValidDate_andFallsBackForInvalidDate() throws Exception {
        DashboardSummaryMarkdownServlet servlet = new DashboardSummaryMarkdownServlet();
        ZoneId zone = ZoneId.systemDefault();

        LocalDate parsed = (LocalDate) invoke(servlet, "parseDay", new Class[]{String.class, ZoneId.class}, "2026-08-01", zone);
        assertEquals(LocalDate.of(2026, 8, 1), parsed);

        LocalDate fallback = (LocalDate) invoke(servlet, "parseDay", new Class[]{String.class, ZoneId.class}, "bad", zone);
        assertEquals(LocalDate.now(zone), fallback);
    }

    @Test
    void parseSlotOrCurrent_usesValidatedSlotRange() throws Exception {
        DashboardSummaryMarkdownServlet servlet = new DashboardSummaryMarkdownServlet();
        ZoneId zone = ZoneId.systemDefault();

        assertEquals(2, invoke(servlet, "parseSlotOrCurrent", new Class[]{String.class, ZoneId.class}, "2", zone));

        int fallback = (int) invoke(servlet, "parseSlotOrCurrent", new Class[]{String.class, ZoneId.class}, "99", zone);
        assertTrue(fallback >= 0 && fallback <= 3);
    }

    @Test
    void suggestNextAction_prioritizesRunningAndQualitySignals() throws Exception {
        DashboardSummaryMarkdownServlet servlet = new DashboardSummaryMarkdownServlet();

        JsonObject runningPayload = Json.createObjectBuilder()
                .add("meta", Json.createObjectBuilder().add("statusText", "running"))
                .add("summary", Json.createObjectBuilder())
                .build();

        String runningAction = (String) invoke(servlet, "suggestNextAction", new Class[]{JsonObject.class}, runningPayload);
        assertTrue(runningAction.contains("still generating"));

        JsonObject qualityPayload = Json.createObjectBuilder()
                .add("meta", Json.createObjectBuilder().add("statusText", "idle"))
                .add("summary", Json.createObjectBuilder().add("quality", "low confidence"))
                .build();

        String qualityAction = (String) invoke(servlet, "suggestNextAction", new Class[]{JsonObject.class}, qualityPayload);
        assertTrue(qualityAction.contains("low-quality conversations"));
    }

    @Test
    void cssStatus_mapsKnownStates() throws Exception {
        DashboardSummaryMarkdownServlet servlet = new DashboardSummaryMarkdownServlet();

        assertEquals("error", invoke(servlet, "cssStatus", new Class[]{String.class}, "error"));
        assertEquals("running", invoke(servlet, "cssStatus", new Class[]{String.class}, "queued"));
        assertEquals("", invoke(servlet, "cssStatus", new Class[]{String.class}, "idle"));
    }

    private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
