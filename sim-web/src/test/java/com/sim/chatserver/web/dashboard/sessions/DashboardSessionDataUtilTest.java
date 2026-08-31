package com.sim.chatserver.web.dashboard.sessions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.widget.WidgetEntry;

class DashboardSessionDataUtilTest {

    @Test
    void mapWidgetDisplayNames_handlesNullsAndFallbacks() {
        WidgetEntry a = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "widget_a", "Widget A", Instant.now());
        WidgetEntry b = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(2, "widget_b", "   ", Instant.now());

        Map<String, String> map = DashboardSessionDataUtil.mapWidgetDisplayNames(Arrays.asList(a, null, b));
        assertEquals(2, map.size());
        assertEquals("Widget A", map.get("widget_a"));
        assertEquals("widget_b", map.get("widget_b"));
    }

    @Test
    void pickTopWidgetName_returnsDisplayNameOrFallback() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("w1", 2);
        counts.put("w2", 5);

        Map<String, String> display = Map.of("w1", "Widget One", "w2", "Widget Two");
        assertEquals("Widget Two", DashboardSessionDataUtil.pickTopWidgetName(counts, display));

        assertEquals("-", DashboardSessionDataUtil.pickTopWidgetName(Map.of(), display));
        assertEquals("w2", DashboardSessionDataUtil.pickTopWidgetName(counts, Map.of()));
    }

    @Test
    void sanitizeWidgetTableName_enforcesSafeIdentifierRules() {
        assertEquals("widget", DashboardSessionDataUtil.sanitizeWidgetTableName(null));
        assertEquals("w_9bad_id", DashboardSessionDataUtil.sanitizeWidgetTableName("9bad-id"));

        String longId = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789__extra";
        String sanitized = DashboardSessionDataUtil.sanitizeWidgetTableName(longId);
        assertTrue(sanitized.length() <= 60);
        assertTrue(Character.isLetter(sanitized.charAt(0)));
    }

    @Test
    void tableExists_handlesMetadataLookupAndErrors() throws Exception {
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet rs1 = mock(ResultSet.class);
        ResultSet rs2 = mock(ResultSet.class);
        ResultSet rs3 = mock(ResultSet.class);

        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(null, null, "my_table", new String[]{"TABLE"})).thenReturn(rs1);
        when(meta.getTables(null, null, "MY_TABLE", new String[]{"TABLE"})).thenReturn(rs2);
        when(meta.getTables(null, null, "my_table", new String[]{"TABLE"})).thenReturn(rs3);

        when(rs1.next()).thenReturn(false);
        when(rs2.next()).thenReturn(true);

        assertTrue(DashboardSessionDataUtil.tableExists(conn, "my_table", Logger.getLogger("test")));

        Connection failing = mock(Connection.class);
        when(failing.getMetaData()).thenThrow(new SQLException("boom"));
        assertFalse(DashboardSessionDataUtil.tableExists(failing, "my_table", Logger.getLogger("test")));
    }
}
