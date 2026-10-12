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


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardTopicsDataServlet", urlPatterns = {"/dashboard/topics/data"})
public class DashboardTopicsDataServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsDataServlet.class.getCanonicalName());
    private static final String OTHER_LABEL = "Other Parasoft Match";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DashboardTopicsDataQueryService QUERY_SERVICE = new DashboardTopicsDataQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.TEXT_UNAUTHORIZED).build());
            return;
        }

        boolean includeOther = parseBooleanFlag(ServletRequestParamUtil.firstParam(req, "includeOther", 256, true, true));
        DateWindow window = resolveDateWindow(req);

        TopicsAggregation aggregation;
        try {
            aggregation = QUERY_SERVICE.collect(window, includeOther, OTHER_LABEL);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to build topics data", e);
            JsonObject err = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to build topics data")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
            return;
        }

        writeJson(resp, HttpServletResponse.SC_OK, buildTopicsPayload(window, includeOther, aggregation));
    
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
        }
    }

    private JsonObject buildTopicsPayload(DateWindow window, boolean includeOther, TopicsAggregation aggregation) {
        JsonArrayBuilder globalArray = Json.createArrayBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> e : sortTopicMap(aggregation.globalCounts)) {
            String topic = e.getKey();
            Set<String> ids = aggregation.globalChatIdsByTopic.getOrDefault(topic, Set.of());
            Object globalMentions = e.getValue();
            int mentions = safeInt(globalMentions);

            JsonArrayBuilder idsArray = Json.createArrayBuilder();
            for (String id : ids) {
                idsArray.add(id);
            }

            globalArray.add(Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANK, rank++)
                    .add("topic", topic)
                    .add("mentions", mentions)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_SELECTED_CHAT_IDS, idsArray));
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
                Object widgetMentions = t.getValue();
                int mentions = safeInt(widgetMentions);

                JsonArrayBuilder idsArray = Json.createArrayBuilder();
                for (String id : ids) {
                    idsArray.add(id);
                }

                topicsArray.add(Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANK, widgetRank++)
                        .add("topic", topic)
                        .add("mentions", mentions)
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_SELECTED_CHAT_IDS, idsArray));
            }

            widgetsArray.add(Json.createObjectBuilder()
                    .add("widgetName", widgetName)
                    .add("topics", topicsArray));
        }

        return Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_QUERY, "")
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT, com.sim.chatserver.util.CommonLiteralConstants.VALUE_ALL)
                .add("includeOther", includeOther)
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, window.dayToken)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_START, window.startInclusive.format(DATE_FMT))
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_END, window.endExclusive.minusDays(1).format(DATE_FMT))
                .add("globalTopics", globalArray)
                .add("widgets", widgetsArray)
                .add("termsTotal", aggregation.totalMentions)
                .add("uniqueChatsTotal", aggregation.allMatchedChatIds.size())
                .build();
    }

    private DateWindow resolveDateWindow(HttpServletRequest req) {
        Optional<LocalDate> dayOpt = parseLocalDate(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, 256, true, true));
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
        return "1".equals(v) || com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE.equals(v) || com.sim.chatserver.util.CommonLiteralConstants.VALUE_YES.equals(v) || "on".equals(v);
    }

    private int safeInt(Object value) {
        if (value == null) {
            return 0;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid integer value for dashboard topics", ex);
            return 0;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write dashboard topics JSON response", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
        }
    }

    private List<Map.Entry<String, Integer>> sortTopicMap(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue())
                        .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}

