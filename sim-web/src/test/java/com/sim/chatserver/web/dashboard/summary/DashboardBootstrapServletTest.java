package com.sim.chatserver.web.dashboard.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

class DashboardBootstrapServletTest {

    @Test
    void formatTimestamp_handlesNullAndFormatsUsingSystemZone() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();

        assertEquals("\u2014", invoke(servlet, "formatTimestamp", new Class[]{Timestamp.class}, (Object) null));

        Timestamp ts = Timestamp.from(Instant.parse("2026-08-01T10:15:30Z"));
        String expected = ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        assertEquals(expected, invoke(servlet, "formatTimestamp", new Class[]{Timestamp.class}, ts));
    }

    @Test
    void quoteIdentifier_escapesEmbeddedQuotes() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();

        assertEquals("\"table\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "table"));
        assertEquals("\"a\"\"b\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "a\"b"));
    }

    @Test
    void sessionAccumulator_defaultsAreInitialized() {
        DashboardBootstrapServlet.SessionAccumulator acc = new DashboardBootstrapServlet.SessionAccumulator();
        assertEquals(0, acc.count);
        assertEquals(null, acc.lastEntry);
        assertNotNull(acc.widgetCounts);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
