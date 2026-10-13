package com.sim.chatserver.web.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.model.SessionStat;
import com.sim.chatserver.model.SessionTimeline;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

class DashboardJdbcDataServiceTest {

    @Test
    void buildSessionChartPayload_fillsMissingSeriesWithZeroes() {
        DashboardJdbcDataService service = new DashboardJdbcDataService();

        List<String> labels = List.of("2026-10-01", "2026-10-02");
        Map<String, List<Integer>> countsBySession = new LinkedHashMap<>();
        countsBySession.put("s1", List.of(Integer.valueOf(3), Integer.valueOf(2)));

        SessionTimeline timeline = new SessionTimeline(labels, countsBySession);
        List<SessionStat> topSessions = List.of(
                new SessionStat("s1", 5, "-"),
                new SessionStat("s2", 1, "-"));
        SessionOverview overview = new SessionOverview(
                topSessions,
                timeline,
                2,
                2,
                0,
                7,
                0,
                0,
                new ProgressStat(0, 0),
                0,
                new ProgressStat(0, 0));

        String payload = service.buildSessionChartPayload(
                overview,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2));

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        JsonArray series = obj.getJsonArray("series");
        assertEquals(2, series.size());

        JsonArray firstCounts = series.getJsonObject(0).getJsonArray("counts");
        assertEquals(3, firstCounts.getInt(0));
        assertEquals(2, firstCounts.getInt(1));

        JsonArray secondCounts = series.getJsonObject(1).getJsonArray("counts");
        assertEquals(0, secondCounts.getInt(0));
        assertEquals(0, secondCounts.getInt(1));
    }

    @Test
    void buildEmptySessionPayload_returnsExpectedSkeleton() {
        DashboardJdbcDataService service = new DashboardJdbcDataService();

        String payload = service.buildEmptySessionPayload(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5));

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertEquals(0, obj.getJsonArray("labels").size());
        assertEquals(0, obj.getJsonArray("series").size());
        assertEquals("2026-10-01", obj.getString("rangeStart"));
        assertEquals("2026-10-05", obj.getString("rangeEnd"));
    }

    @Test
    void buildLastFiveDaysTrendJson_whenConnectionFails_returnsZeros() throws Exception {
        DashboardJdbcDataService service = new DashboardJdbcDataService();
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("db unavailable"));

        String payload = service.buildLastFiveDaysTrendJson(holder, List.of());

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertEquals(5, obj.getInt("days"));
        assertEquals(5, obj.getJsonArray("labels").size());
        JsonArray values = obj.getJsonArray("values");
        assertEquals(5, values.size());
        for (int i = 0; i < values.size(); i++) {
            assertEquals(0, values.getInt(i));
        }
    }

    @Test
    void loadWidgets_whenWidgetStoreFails_returnsEmptyList() throws Exception {
        DashboardJdbcDataService service = new DashboardJdbcDataService();

        try (MockedStatic<WidgetStore> widgetStoreMock = mockStatic(WidgetStore.class)) {
            widgetStoreMock.when(() -> WidgetStore.list(null)).thenThrow(new SQLException("boom"));
            assertEquals(0, service.loadWidgets().size());
        }
    }

    @Test
    void loadSessionLabels_whenStatsMissing_returnsEmptyMap() {
        DashboardJdbcDataService service = new DashboardJdbcDataService();
        assertEquals(0, service.loadSessionLabels(null).size());
        assertEquals(0, service.loadSessionLabels(List.of()).size());
    }

    @Test
    void readLocalDateColumn_supportsInstantAndTimestampFallback() {
        DashboardJdbcDataService service = new DashboardJdbcDataService();

        ResultSet rsInstant = mock(ResultSet.class);
        ResultSet rsTimestamp = mock(ResultSet.class);
        ResultSet rsInvalid = mock(ResultSet.class);

        try {
            when(rsInstant.getCharacterStream("day_value")).thenReturn(new StringReader("2026-08-24T10:15:30Z"));
            when(rsTimestamp.getCharacterStream("day_value")).thenReturn(new StringReader("2026-08-25 11:00:00"));
            when(rsInvalid.getCharacterStream("day_value")).thenReturn(new StringReader("not-a-date"));
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }

        LocalDate fromInstant = (LocalDate) invokePrivate(service, "readLocalDateColumn", new Class<?>[]{ResultSet.class, String.class}, rsInstant, "day_value");
        LocalDate fromTimestamp = (LocalDate) invokePrivate(service, "readLocalDateColumn", new Class<?>[]{ResultSet.class, String.class}, rsTimestamp, "day_value");
        LocalDate fromInvalid = (LocalDate) invokePrivate(service, "readLocalDateColumn", new Class<?>[]{ResultSet.class, String.class}, rsInvalid, "day_value");

        assertEquals(LocalDate.of(2026, 8, 24), fromInstant);
        assertEquals(LocalDate.of(2026, 8, 25), fromTimestamp);
        assertNull(fromInvalid);
    }

    @Test
    void quoteIdentifier_validatesIdentifierRules() {
        DashboardJdbcDataService service = new DashboardJdbcDataService();

        String quoted = (String) invokePrivate(service, "quoteIdentifier", new Class<?>[]{String.class}, "valid_name_1");
        assertEquals("\"valid_name_1\"", quoted);

        IllegalArgumentException invalid = assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(service, "quoteIdentifier", new Class<?>[]{String.class}, "bad-name"));
        assertEquals("Invalid SQL identifier", invalid.getMessage());
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
