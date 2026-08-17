package com.sim.chatserver.web.dashboard.newuser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardNewUsersDrilldownServlet", urlPatterns = {"/dashboard/new-users/drilldown"})
public class DashboardNewUsersDrilldownServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardNewUsersDrilldownServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_new_users_drilldown.html";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            try {
                req.getRequestDispatcher("/login").forward(req, resp);
            } catch (ServletException | IOException e) {
                log.log(Level.WARNING, "Unable to forward unauthenticated user to login.", e);
                sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
            }
            return;
        }

        String contextPath = ServletPathUtil.safeContextPathNoEmptyGuard(req.getContextPath());

        int page = parsePositiveIntOrDefault(ServletRequestParamUtil.firstParam(req, "page", 256, true, true), 1);
        int pageSize = parsePositiveIntOrDefault(ServletRequestParamUtil.firstParam(req, "pageSize", 256, true, true), 10);
        if (pageSize != 10 && pageSize != 25 && pageSize != 50) {
            pageSize = 10;
        }

        LocalDate dayFilter = parseDateOrNull(ServletRequestParamUtil.firstParam(req, "day", 256, true, true));

        List<Row> allRows = new ArrayList<>();

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets for new users drilldown", e);
            widgets = List.of();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unexpected runtime error listing widgets", e);
            widgets = List.of();
        }

        Map<String, Timestamp> earliestBySession = queryService().findEarliestBySession(widgets);
        Map<String, Integer> totalChatsBySession = queryService().findTotalChatsBySession(widgets);

        List<Map.Entry<String, Timestamp>> sorted = new ArrayList<>(earliestBySession.entrySet());
        sorted.sort(Map.Entry.<String, Timestamp>comparingByValue(Comparator.reverseOrder()));

        Set<String> ids = new LinkedHashSet<>();
        for (Map.Entry<String, Timestamp> entry : sorted) {
            ids.add(entry.getKey());
        }

        Map<String, SessionLabelStore.SessionLabel> labels;
        try {
            labels = ids.isEmpty() ? Map.of() : SessionLabelStore.mapDisplayNames(ids);
        } catch (SQLException | IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to resolve session display labels", ex);
            labels = Map.of();
        }

        for (Map.Entry<String, Timestamp> entry : sorted) {
            String sid = entry.getKey();
            Timestamp ts = entry.getValue();

            LocalDate firstSeenDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (dayFilter != null && !firstSeenDate.equals(dayFilter)) {
                continue;
            }

            String display = SessionLabelStore.resolveDisplayLabel(sid, labels.get(sid));
            String firstSeen = TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()));
            int totalChats = safeInt(totalChatsBySession.get(sid));
            String chatUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId="
                    + java.net.URLEncoder.encode(sid, StandardCharsets.UTF_8);

            allRows.add(new Row(display, firstSeen, totalChats, chatUrl));
        }

        int total = allRows.size();
        int totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
        if (page > totalPages) {
            page = totalPages;
        }

        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(total, from + pageSize);
        List<Row> pageRows = allRows.subList(from, to);

        String rowsJsonB64 = buildRowsJsonBase64(pageRows, from);

        String dayParam = dayFilter != null ? "&day=" + urlEncode(dayFilter.format(DATE_FMT)) : "";
        String prevHref = page > 1
            ? contextPath + "/dashboard/new-users/drilldown?page=" + (page - 1) + "&pageSize=" + pageSize + dayParam
                : "";
        String nextHref = page < totalPages
            ? contextPath + "/dashboard/new-users/drilldown?page=" + (page + 1) + "&pageSize=" + pageSize + dayParam
                : "";

        String filterTitle = dayFilter == null
            ? "All Session IDs / Users (Newest First)"
            : "Users first seen on " + dayFilter.format(DATE_FMT);

        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
            .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${user}", escapeHtml(String.valueOf(session.getAttribute("user"))))
                .replace("${rowsJsonB64}", rowsJsonB64)
                .replace("${totalUsers}", String.valueOf(total))
                .replace("${page}", String.valueOf(page))
                .replace("${totalPages}", String.valueOf(totalPages))
                .replace("${pageSize}", String.valueOf(pageSize))
                .replace("${prevHref}", escapeHtml(prevHref))
                .replace("${nextHref}", escapeHtml(nextHref))
                .replace("${prevDisabled}", prevHref.isBlank() ? "disabled" : "")
                .replace("${nextDisabled}", nextHref.isBlank() ? "disabled" : "")
                .replace("${selected10}", pageSize == 10 ? "selected" : "")
                .replace("${selected25}", pageSize == 25 ? "selected" : "")
                .replace("${selected50}", pageSize == 50 ? "selected" : "")
                .replace("${filterTitle}", escapeHtml(filterTitle))
                .replace("${dayQuery}", dayFilter == null ? "" : "&day=" + escapeHtml(dayFilter.format(DATE_FMT)));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to render newest users drilldown page", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private String buildRowsJsonBase64(List<Row> rows, int offset) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        int rank = offset + 1;
        for (Row r : rows) {
            builder.add(Json.createObjectBuilder()
                    .add("rank", rank++)
                    .add("display", safeJsonText(r.display))
                    .add("firstSeen", safeJsonText(r.firstSeen))
                    .add("totalChats", r.totalChats)
                    .add("chatEntriesUrl", safeJsonText(r.chatEntriesUrl)));
        }
        byte[] utf8 = builder.build().toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(utf8);
    }

    private int parsePositiveIntOrDefault(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int n = Integer.parseInt(value.trim());
            return n > 0 ? n : fallback;
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid positive integer parameter value: {0}", sanitizeForLog(value));
            return fallback;
        }
    }

    private LocalDate parseDateOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            log.log(Level.FINE, "Invalid day parameter for new users drilldown: {0}", sanitizeForLog(value));
            return null;
        }
    }

    private String loadTemplate(HttpServletRequest req, String path) {
        if (req == null || req.getServletContext() == null || path == null || path.isBlank()) {
            return "";
        }
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, "Template not found: {0}", path);
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
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
            char ch = s.charAt(i);
            switch (ch) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '\"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(ch);
            }
        }
        return escaped.toString();
    }

    private String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private DashboardNewUsersDrilldownQueryService queryService() {
        return new DashboardNewUsersDrilldownQueryService(CDI.current().select(com.sim.chatserver.startup.AppDataSourceHolder.class).get(), log);
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending fallback server error.", ioe);
        }
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private String safeJsonText(String value) {
        return value == null ? "" : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value.intValue();
    }

    private static final class Row {

        final String display;
        final String firstSeen;
        final int totalChats;
        final String chatEntriesUrl;

        private Row(String display, String firstSeen, int totalChats, String chatEntriesUrl) {
            this.display = display;
            this.firstSeen = firstSeen;
            this.totalChats = totalChats;
            this.chatEntriesUrl = chatEntriesUrl;
        }
    }

}
