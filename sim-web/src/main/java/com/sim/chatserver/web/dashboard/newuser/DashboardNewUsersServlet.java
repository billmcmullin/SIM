package com.sim.chatserver.web.dashboard.newuser;

import com.sim.chatserver.util.SessionLabel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
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
@WebServlet(
        name = "DashboardNewUsersServlet",
        urlPatterns = {
            "/dashboard/new-users",
            "/dashboard/new-users/data",
            "/dashboard/new-users/day",
            "/dashboard/new-users/day-data" // optional alias for clearer API intent; keeps existing /day intact
        }
)
public class DashboardNewUsersServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardNewUsersServlet.class.getCanonicalName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_new_users.html";
    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern(com.sim.chatserver.util.CommonLiteralConstants.DATETIME_PATTERN_SECONDS);

    private static final int DEFAULT_DAYS = 7;

    private static final String PATH_PAGE = "/dashboard/new-users";
    private static final String PATH_DATA = "/dashboard/new-users/data";
    private static final String PATH_DAY = "/dashboard/new-users/day";
    private static final String PATH_DAY_DATA = "/dashboard/new-users/day-data";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (req == null) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
            return;
        }
        String path = resolveRequestPath(req);
        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
                if (reqExpectsJson(path)) {
                    writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
                } else {
                    com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
                }
                return;
            }

            if (PATH_DATA.equals(path)) {
                handleData(req, resp);
                return;
            }
            if (PATH_DAY.equals(path) || PATH_DAY_DATA.equals(path)) {
                handleDay(req, resp);
                return;
            }

            handlePage(req, resp, session);
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            log.log(Level.SEVERE, "Failed in /dashboard/new-users flow", e);
            if (!resp.isCommitted()) {
                if (reqExpectsJson(path)) {
                    writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load new user metrics.");
                } else {
                    writePlainTextError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load new user metrics.");
                }
            }
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private boolean reqExpectsJson(String path) {
        return PATH_DATA.equals(path) || PATH_DAY.equals(path) || PATH_DAY_DATA.equals(path);
    }

    private void handlePage(HttpServletRequest req, HttpServletResponse resp, HttpSession session) {
        if (req == null || resp == null || session == null) {
            return;
        }
        int days = parseDays(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_DAYS, 256, true, false)).orElse(DEFAULT_DAYS);
        LocalDate end = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = end.minusDays(days - 1);

        String contextPath = ServletPathUtil.safeContextPathStrict(req.getContextPath());
        Metrics metrics = loadMetrics(start, end, contextPath);

        String user = String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER));
        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH, escapeHtml(contextPath))
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER, escapeHtml(user))
                .replace("${trendJson}", escapeForJs(metrics.toTrendJson()))
                .replace("${latestRows}", "")
                .replace("${rangeStart}", escapeHtml(start.format(DATE_FMT)))
                .replace("${rangeEnd}", escapeHtml(end.format(DATE_FMT)))
                .replace("${selectedDays}", String.valueOf(days));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8_SPACED);
        try {
            PrintWriter out = resp.getWriter();
            try {
                out.print(rendered);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write dashboard new users page response", e);
        }
    }

    private void handleData(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null || resp == null) {
            return;
        }
        LocalDate rangeEnd = LocalDate.now(ZoneId.systemDefault());
        int days = parseDays(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_DAYS, 256, true, false)).orElse(DEFAULT_DAYS);
        LocalDate rangeStart = rangeEnd.minusDays(days - 1);

        Metrics metrics = loadMetrics(rangeStart, rangeEnd, ServletPathUtil.safeContextPathStrict(req.getContextPath()));

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        metrics.byDay.forEach((d, v) -> {
            labels.add(d.format(DATE_FMT));
            int safeValue = safeIntegerValue(v);
            values.add(safeValue);
        });

        JsonArrayBuilder latest = Json.createArrayBuilder();
        int rank = 1;
        for (LatestRow r : metrics.latest) {
            latest.add(Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANK, rank++)
                    .add("display", safe(r.display))
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID, safe(r.rawSessionId))
                    .add("firstSeen", safe(r.firstSeen))
                    .add("totalChats", r.totalChats)
                    .add("chatEntriesUrl", safe(r.chatEntriesUrl)));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_START, rangeStart.format(DATE_FMT))
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_END, rangeEnd.format(DATE_FMT))
                .add("trend", Json.createObjectBuilder()
            .add(CommonLiteralConstants.FIELD_LABELS, labels)
            .add(CommonLiteralConstants.FIELD_VALUES, values))
                .add("latest", latest)
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private void handleDay(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null || resp == null) {
            return;
        }
        Optional<LocalDate> dayOpt = parseLocalDate(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, 256, true, false));
        if (dayOpt.isEmpty()) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid day.");
            return;
        }
        LocalDate day = dayOpt.get();

        DayResult result = loadDayFirstSeen(day, ServletPathUtil.safeContextPathStrict(req.getServletContext().getContextPath()));

        JsonArrayBuilder rows = Json.createArrayBuilder();
        int rank = 1;
        for (LatestRow r : result.rows) {
            rows.add(Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANK, rank++)
                    .add("display", safe(r.display))
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID, safe(r.rawSessionId))
                    .add("firstSeen", safe(r.firstSeen))
                    .add("totalChats", r.totalChats)
                    .add("chatEntriesUrl", safe(r.chatEntriesUrl)));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, day.format(DATE_FMT))
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT, result.rows.size())
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_ROWS, rows)
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private Metrics loadMetrics(LocalDate rangeStart, LocalDate rangeEnd, String contextPath) {
        Metrics metrics = new Metrics(rangeStart, rangeEnd);
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_LIST_WIDGETS, e);
            widgets = List.of();
        }
        computeNewSessionMetrics(widgets, metrics, contextPath);
        return metrics;
    }

    private DayResult loadDayFirstSeen(LocalDate day, String contextPath) {
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_LIST_WIDGETS, e);
            widgets = List.of();
        }

        try {
            DashboardNewUsersDrilldownQueryService queryService = drilldownQueryService();
            Map<String, java.sql.Timestamp> earliestBySession = queryService.findEarliestBySession(widgets);
            Map<String, Integer> totalsBySession = queryService.findTotalChatsBySession(widgets);

            List<Map.Entry<String, java.sql.Timestamp>> forDay = earliestBySession.entrySet().stream()
                    .filter(e -> {
                        LocalDate d = e.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        return d.equals(day);
                    })
                    .sorted(Map.Entry.<String, java.sql.Timestamp>comparingByValue(Comparator.reverseOrder()))
                    .toList();

            Set<String> ids = new LinkedHashSet<>();
            for (Map.Entry<String, java.sql.Timestamp> e : forDay) {
                ids.add(e.getKey());
            }

            Map<String, SessionLabel> labels;
            try {
                labels = ids.isEmpty() ? Map.of() : SessionLabelStore.mapDisplayNames(ids);
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Unable to resolve session labels", ex);
                labels = Map.of();
            }

            List<LatestRow> rows = new ArrayList<>();
            for (Map.Entry<String, java.sql.Timestamp> e : forDay) {
                String sid = e.getKey();
                java.sql.Timestamp ts = e.getValue();
                String display = SessionLabelStore.resolveDisplayLabel(sid, labels.get(sid));
                String firstSeen = TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()));
                int totalChats = getTotalChats(totalsBySession, sid);
                String chatEntriesUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + urlEncode(sid);
                rows.add(new LatestRow(display, sid, firstSeen, totalChats, chatEntriesUrl));
            }

            return new DayResult(rows);
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load day first-seen data", e);
            return new DayResult(List.of());
        }
    }

    private void computeNewSessionMetrics(List<WidgetEntry> widgets, Metrics metrics, String contextPath) {
        DashboardNewUsersDrilldownQueryService queryService = drilldownQueryService();
        Map<String, java.sql.Timestamp> earliestBySession = queryService.findEarliestBySession(widgets);
        Map<String, Integer> totalsBySession = queryService.findTotalChatsBySession(widgets);

        for (Map.Entry<String, java.sql.Timestamp> e : earliestBySession.entrySet()) {
            LocalDate d = e.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            metrics.incrementDay(d);
        }

        List<Map.Entry<String, java.sql.Timestamp>> sorted = new ArrayList<>(earliestBySession.entrySet());
        sorted.sort(Map.Entry.<String, java.sql.Timestamp>comparingByValue(Comparator.reverseOrder()));

        Set<String> ids = new LinkedHashSet<>();
        for (Map.Entry<String, java.sql.Timestamp> e : sorted) {
            ids.add(e.getKey());
        }

        Map<String, SessionLabel> labels;
        try {
            labels = ids.isEmpty() ? Map.of() : SessionLabelStore.mapDisplayNames(ids);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to resolve session labels", e);
            labels = Map.of();
        }

        int take = Math.min(10, sorted.size());
        for (int i = 0; i < take; i++) {
            String sid = sorted.get(i).getKey();
            java.sql.Timestamp ts = sorted.get(i).getValue();
            String display = SessionLabelStore.resolveDisplayLabel(sid, labels.get(sid));
            String firstSeen = TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()));
            int totalChats = getTotalChats(totalsBySession, sid);

            String chatEntriesUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + urlEncode(sid);
            metrics.latest.add(new LatestRow(display, sid, firstSeen, totalChats, chatEntriesUrl));
        }
    }

    private int getTotalChats(Map<String, Integer> totalsBySession, String sid) {
        if (totalsBySession == null || sid == null) {
            return 0;
        }
        Integer total = totalsBySession.get(sid);
        return safeIntegerValue(total);
    }

    private OptionalInt parseDays(String value) {
        if (value == null || value.isBlank()) {
            return OptionalInt.empty();
        }
        try {
            int d = Integer.parseInt(value.trim());
            return switch (d) {
                case 7, 14, 30, 90 -> OptionalInt.of(d);
                default -> OptionalInt.empty();
            };
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid days parameter", e);
            return OptionalInt.empty();
        }
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value, DATE_FMT));
        } catch (DateTimeParseException e) {
            log.log(Level.FINE, "Invalid date parameter", e);
            return Optional.empty();
        }
    }

    private String loadTemplate(HttpServletRequest req, String path) {
        if (req == null || req.getServletContext() == null || path == null || path.isBlank()) {
            return "";
        }
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_TEMPLATE_NOT_FOUND_FORMAT, path);
                return "";
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    b.append(line).append(System.lineSeparator());
                }
                return b.toString();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load template " + path, e);
            return "";
        }
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_AMP);
                    break;
                case '<':
                    escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_LT);
                    break;
                case '>':
                    escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_GT);
                    break;
                case '"':
                    escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_QUOT);
                    break;
                case '\'':
                    escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_APOS);
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO)
                .replace("'", com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_SINGLE_QUOTE)
                .replace(System.lineSeparator(), com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO)
                .replace(System.lineSeparator(), com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_CARRIAGE_RETURN);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static int safeIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid integer value", ex);
            return 0;
        }
    }

    private String urlEncode(String v) {
        return java.net.URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private DashboardNewUsersDrilldownQueryService drilldownQueryService() {
        return new DashboardNewUsersDrilldownQueryService(dataSourceHolder(), log);
    }

    private com.sim.chatserver.startup.AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(com.sim.chatserver.startup.AppDataSourceHolder.class).get();
    }

    private String normalizeServletPath(String servletPath) {
        if (PATH_DATA.equals(servletPath) || PATH_DAY.equals(servletPath) || PATH_DAY_DATA.equals(servletPath) || PATH_PAGE.equals(servletPath)) {
            return servletPath;
        }
        return PATH_PAGE;
    }

    private String resolveRequestPath(HttpServletRequest req) {
        if (req == null || req.getHttpServletMapping() == null) {
            return PATH_PAGE;
        }
        return normalizeServletPath(req.getHttpServletMapping().getPattern());
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message);
        } catch (IOException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_ERROR_RESPONSE, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_RESPONSE, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writePlainTextError(HttpServletResponse resp, int status, String message) {
        if (resp == null) {
            return;
        }
        try {
            resp.setStatus(status);
            resp.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message);
            }
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to write plain text error", e);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}

final class DayResult {

    final List<LatestRow> rows;

    DayResult(List<LatestRow> rows) {
        this.rows = rows;
    }
}

final class LatestRow {

    final String display;
    final String rawSessionId;
    final String firstSeen;
    final int totalChats;
    final String chatEntriesUrl;

    LatestRow(String display, String rawSessionId, String firstSeen, int totalChats, String chatEntriesUrl) {
        this.display = display;
        this.rawSessionId = rawSessionId;
        this.firstSeen = firstSeen;
        this.totalChats = totalChats;
        this.chatEntriesUrl = chatEntriesUrl;
    }
}

final class Metrics {

    final LocalDate start;
    final LocalDate end;
    final Map<LocalDate, Integer> byDay = new LinkedHashMap<>();
    final List<LatestRow> latest = new ArrayList<>();

    Metrics(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
        long days = ChronoUnit.DAYS.between(start, end);
        for (int i = 0; i <= days; i++) {
            byDay.put(start.plusDays(i), Integer.valueOf(0));
        }
    }

    void incrementDay(LocalDate day) {
        if (day == null || day.isBefore(start) || day.isAfter(end)) {
            return;
        }
        Integer current = byDay.get(day);
        int next = safeIntegerValue(current) + 1;
        byDay.put(day, Integer.valueOf(next));
    }

    String toTrendJson() {
        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        byDay.forEach((d, c) -> {
            labels.add(d.format(DashboardNewUsersServlet.DATE_FMT));
            int count = safeIntegerValue(c);
            values.add(count);
        });
        JsonObject o = Json.createObjectBuilder()
                .add(CommonLiteralConstants.FIELD_LABELS, labels)
                .add(CommonLiteralConstants.FIELD_VALUES, values)
                .build();
        return o.toString();
    }

    private static int safeIntegerValue(Integer value) {
        return value == null ? 0 : value.intValue();
    }
}

