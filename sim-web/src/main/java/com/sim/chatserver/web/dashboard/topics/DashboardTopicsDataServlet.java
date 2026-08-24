package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsDataServlet", urlPatterns = {"/dashboard/topics/data"})
public class DashboardTopicsDataServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsDataServlet.class.getName());
    private static final String OTHER_LABEL = "Other Parasoft Match";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final transient DashboardTopicsDataQueryService queryService = new DashboardTopicsDataQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "unauthorized").build());
            return;
        }

        boolean includeOther = parseBooleanFlag(ServletRequestParamUtil.firstParam(req, "includeOther", 256, true, true));
        DateWindow window = resolveDateWindow(req);

        TopicsAggregation aggregation;
        try {
            aggregation = queryService.collect(window, includeOther, OTHER_LABEL);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to build topics data", e);
            JsonObject err = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to build topics data")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
            return;
        }

        writeJson(resp, HttpServletResponse.SC_OK, buildTopicsPayload(window, includeOther, aggregation));
    
        } catch (Throwable e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private JsonObject buildTopicsPayload(DateWindow window, boolean includeOther, TopicsAggregation aggregation) {
        JsonArrayBuilder globalArray = Json.createArrayBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> e : sortTopicMap(aggregation.globalCounts)) {
            String topic = e.getKey();
            Set<String> ids = aggregation.globalChatIdsByTopic.getOrDefault(topic, Set.of());
            int mentions = e.getValue() == null ? 0 : e.getValue().intValue();

            JsonArrayBuilder idsArray = Json.createArrayBuilder();
            for (String id : ids) {
                idsArray.add(id);
            }

            globalArray.add(Json.createObjectBuilder()
                    .add("rank", rank++)
                    .add("topic", topic)
                    .add("mentions", mentions)
                    .add("selectedChatIds", idsArray));
        }

        JsonArrayBuilder widgetsArray = Json.createArrayBuilder();
        for (Map.Entry<String, Map<String, Integer>> e : aggregation.byWidgetCounts.entrySet()) {
            String widgetName = e.getKey();
            Map<String, Integer> widgetCounts = e.getValue();
            if (widgetCounts == null || widgetCounts.isEmpty()) {
                continue;
            }

            List<Map.Entry<String, Integer>> sorted = sortTopicMap(widgetCounts);
            if (sorted.isEmpty()) {
                continue;
            }

            Map<String, Set<String>> topicChats = aggregation.byWidgetChatIds.getOrDefault(widgetName, Map.of());
            JsonArrayBuilder topicsArray = Json.createArrayBuilder();
            int widgetRank = 1;
            for (Map.Entry<String, Integer> t : sorted) {
                String topic = t.getKey();
                Set<String> ids = topicChats.getOrDefault(topic, Set.of());
                int mentions = t.getValue() == null ? 0 : t.getValue().intValue();

                JsonArrayBuilder idsArray = Json.createArrayBuilder();
                for (String id : ids) {
                    idsArray.add(id);
                }

                topicsArray.add(Json.createObjectBuilder()
                        .add("rank", widgetRank++)
                        .add("topic", topic)
                        .add("mentions", mentions)
                        .add("selectedChatIds", idsArray));
            }

            widgetsArray.add(Json.createObjectBuilder()
                    .add("widgetName", widgetName)
                    .add("topics", topicsArray));
        }

        return Json.createObjectBuilder()
                .add("status", "ok")
                .add("query", "")
                .add("limit", "all")
                .add("includeOther", includeOther)
                .add("day", window.dayToken)
                .add("rangeStart", window.startInclusive.format(DATE_FMT))
                .add("rangeEnd", window.endExclusive.minusDays(1).format(DATE_FMT))
                .add("globalTopics", globalArray)
                .add("widgets", widgetsArray)
                .add("termsTotal", aggregation.totalMentions)
                .add("uniqueChatsTotal", aggregation.allMatchedChatIds.size())
                .build();
    }

    private DateWindow resolveDateWindow(HttpServletRequest req) {
        Optional<LocalDate> dayOpt = parseLocalDate(ServletRequestParamUtil.firstParam(req, "day", 256, true, true));
        if (dayOpt.isPresent()) {
            LocalDate d = dayOpt.get();
            return new DateWindow(d, d.plusDays(1), d.format(DATE_FMT));
        }

        Optional<LocalDate> startOpt = parseLocalDate(ServletRequestParamUtil.firstParam(req, "start", 256, true, true));
        Optional<LocalDate> endOpt = parseLocalDate(ServletRequestParamUtil.firstParam(req, "end", 256, true, true));

        if (startOpt.isPresent() || endOpt.isPresent()) {
            LocalDate s = startOpt.orElseGet(endOpt::get);
            LocalDate e = endOpt.orElseGet(startOpt::get);

            if (e.isBefore(s)) {
                LocalDate tmp = s;
                s = e;
                e = tmp;
            }

            String token = s.equals(e)
                    ? s.format(DATE_FMT)
                    : s.format(DATE_FMT) + "_to_" + e.format(DATE_FMT);

            return new DateWindow(s, e.plusDays(1), token);
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return new DateWindow(today, today.plusDays(1), today.format(DATE_FMT));
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), DATE_FMT));
        } catch (DateTimeParseException ex) {
            log.log(Level.FINE, "Invalid date parameter for dashboard topics");
            return Optional.empty();
        }
    }

    private boolean parseBooleanFlag(String raw) {
        if (raw == null) {
            return false;
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write dashboard topics JSON response", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, "Fallback sendError failed", ioe);
        }
    }

    private List<Map.Entry<String, Integer>> sortTopicMap(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue())
                        .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    static final class TopicPattern {

        final String name;
        final Pattern pattern;

        TopicPattern(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }
    }

    static final class DateWindow {

        final LocalDate startInclusive;
        final LocalDate endExclusive;
        final String dayToken;

        private DateWindow(LocalDate startInclusive, LocalDate endExclusive, String dayToken) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.dayToken = dayToken;
        }
    }

    static final class TopicsAggregation {

        final Map<String, Integer> globalCounts = new LinkedHashMap<>();
        final Map<String, Set<String>> globalChatIdsByTopic = new LinkedHashMap<>();
        final Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();
        final Map<String, Map<String, Set<String>>> byWidgetChatIds = new LinkedHashMap<>();
        final Set<String> allMatchedChatIds = new LinkedHashSet<>();
        long totalMentions = 0L;
    }

}
