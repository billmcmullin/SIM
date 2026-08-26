package com.sim.chatserver.web.dashboard.topics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class DashboardTopicsDataQueryServiceTest {

    @Test
    void identifierAndTableHelpers_validateInputs() throws Exception {
        DashboardTopicsDataQueryService service = new DashboardTopicsDataQueryService(Logger.getLogger("test"));

        assertEquals("widget", invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, (Object) null));
        String normalized = invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, "1 bad-id");
        assertTrue(normalized.startsWith("w_"));

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdentifier", new Class<?>[]{String.class}, "good_name"));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> invokeObject(service, "quoteIdentifier", new Class<?>[]{String.class}, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void incrementAndRecordHelpers_updateAggregations() throws Exception {
        DashboardTopicsDataQueryService service = new DashboardTopicsDataQueryService(Logger.getLogger("test"));
        DashboardTopicsDataServlet.TopicsAggregation aggregation = new DashboardTopicsDataServlet.TopicsAggregation();

        Map<String, Integer> widgetMap = new LinkedHashMap<>();
        Map<String, Set<String>> widgetTopicChatIds = new LinkedHashMap<>();

        invokeObject(
                service,
                "recordMatchedTopics",
                new Class<?>[]{DashboardTopicsDataServlet.TopicsAggregation.class, Map.class, Map.class, String.class, Set.class},
                aggregation,
                widgetMap,
                widgetTopicChatIds,
                "chat-1",
                new LinkedHashSet<>(Set.of("alpha", "beta")));

        assertEquals(1, aggregation.globalCounts.get("alpha").intValue());
        assertEquals(1, aggregation.globalCounts.get("beta").intValue());
        assertEquals(1, widgetMap.get("alpha").intValue());
        assertTrue(aggregation.allMatchedChatIds.contains("chat-1"));
        assertEquals(2L, aggregation.totalMentions);

        invokeObject(
                service,
                "recordMatchedTopics",
                new Class<?>[]{DashboardTopicsDataServlet.TopicsAggregation.class, Map.class, Map.class, String.class, Set.class},
                aggregation,
                widgetMap,
                widgetTopicChatIds,
                "chat-1",
                Set.of("alpha"));

        assertEquals(2, aggregation.globalCounts.get("alpha").intValue());
        assertEquals(2, widgetMap.get("alpha").intValue());
        assertEquals(3L, aggregation.totalMentions);
        assertEquals(1, aggregation.globalChatIdsByTopic.get("alpha").size());
    }

    @Test
    void matchTopics_returnsExpectedTopicNames() throws Exception {
        DashboardTopicsDataQueryService service = new DashboardTopicsDataQueryService(Logger.getLogger("test"));

        List<DashboardTopicsDataServlet.TopicPattern> patterns = List.of(
                new DashboardTopicsDataServlet.TopicPattern("Error", Pattern.compile("\\berror\\b", Pattern.CASE_INSENSITIVE)),
                new DashboardTopicsDataServlet.TopicPattern("Billing", Pattern.compile("\\bbilling\\b", Pattern.CASE_INSENSITIVE))
        );

        @SuppressWarnings("unchecked")
        Set<String> matched = (Set<String>) invokeObject(
                service,
                "matchTopics",
                new Class<?>[]{String.class, List.class},
                "The billing system has an ERROR today.",
                patterns);

        assertTrue(matched.contains("Error"));
        assertTrue(matched.contains("Billing"));

        @SuppressWarnings("unchecked")
        Set<String> none = (Set<String>) invokeObject(
                service,
                "matchTopics",
                new Class<?>[]{String.class, List.class},
                "no matching keywords here",
                patterns);

        assertTrue(none.isEmpty());
    }

    private Object invokeObject(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private String invokeString(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return (String) invokeObject(target, methodName, paramTypes, args);
    }
}
