package com.sim.chatserver.web.dashboard.sessions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardSessionAggregationQueryService;

import org.junit.jupiter.api.Test;

class DashboardSessionAggregationQueryServiceTest {

    @Test
    void sessionAccumulatorData_defaultsAreInitialized() {
        DashboardSessionAggregationQueryService.SessionAccumulatorData data =
                new DashboardSessionAggregationQueryService.SessionAccumulatorData();

        assertEquals(0, data.count);
        assertEquals(null, data.lastEntry);
        assertNotNull(data.widgetCounts);
    }

    @Test
    void quoteIdentifier_validatesIdentifier() throws Exception {
        DashboardSessionAggregationQueryService service =
                new DashboardSessionAggregationQueryService(Logger.getLogger("test"));

        Method quote = DashboardSessionAggregationQueryService.class
                .getDeclaredMethod("quoteIdentifier", String.class);
        quote.setAccessible(true);

        assertEquals("\"widget_entries\"", quote.invoke(service, "widget_entries"));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> quote.invoke(service, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }
}
