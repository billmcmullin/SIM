package com.sim.chatserver.web.dashboard.inactiveusers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "InactiveUsersListPageServlet", urlPatterns = {"/dashboard/inactive-users/list"})
public class InactiveUsersListPageServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(InactiveUsersListPageServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users_list.html";
    private static final int DEFAULT_DAYS = 7;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_SEARCH_LENGTH = 128;
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;

    private static final int FRUSTRATION_PROMPT_SCAN_LIMIT = 8;

    static final class Row {

        String sessionId;
        String displayLabel;
        String widgetId;
        String widgetLabel;
        long chatCount;
        Timestamp lastEntry;

        boolean frustrationDetected;
        double frustrationScore;
        String frustrationReason;
    }

    static final class FrustrationResult {

        boolean detected;
        double score;
        String reason;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        String contextPath = req.getContextPath() == null ? "" : req.getContextPath();
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String scope = nvl(ServletRequestParamUtil.firstParam(req, "scope", 256, false, false)).trim();
        if (!"widget".equalsIgnoreCase(scope)) {
            scope = "all";
        }

        String widgetIdFilter = sanitizeWidgetId(ServletRequestParamUtil.firstParam(req, "widgetId", 256, false, false));
        String search = sanitizeSearch(ServletRequestParamUtil.firstParam(req, "search", 256, false, false));
        String searchLower = search.toLowerCase();
        boolean hasSearch = !searchLower.isBlank();

        int days = parseInt(ServletRequestParamUtil.firstParam(req, "days", 256, false, false), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
        }

        int page = parseInt(ServletRequestParamUtil.firstParam(req, "page", 256, false, false), 1);
        if (page < 1) {
            page = 1;
        }

        int limit = parseInt(ServletRequestParamUtil.firstParam(req, "limit", 256, false, false), DEFAULT_LIMIT);
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load widgets", e);
            widgets = List.of();
        }

        Map<String, String> widgetNameById = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String id = w.getWidgetId().trim();
            String name = (w.getDisplayName() == null || w.getDisplayName().isBlank()) ? id : w.getDisplayName().trim();
            widgetNameById.put(id, name);
        }

        List<Row> allRows = queryService().loadRows(
                scope,
                widgetIdFilter,
                widgetNameById,
                cutoff,
                FRUSTRATION_PROMPT_SCAN_LIMIT,
                this::detectFrustration
        );

        Set<String> ids = allRows.stream().map(r -> r.sessionId).collect(Collectors.toSet());
        Map<String, SessionLabelStore.SessionLabel> labels = Map.of();
        try {
            if (!ids.isEmpty()) {
                labels = SessionLabelStore.mapDisplayNames(ids);
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load session labels", e);
        }

        for (Row r : allRows) {
            r.displayLabel = SessionLabelStore.resolveDisplayLabel(r.sessionId, labels.get(r.sessionId));
        }

        if (hasSearch) {
            allRows = allRows.stream().filter(r -> {
                String sid = r.sessionId == null ? "" : r.sessionId.toLowerCase();
                String display = r.displayLabel == null ? "" : r.displayLabel.toLowerCase();
                return sid.contains(searchLower) || display.contains(searchLower);
            }).collect(Collectors.toList());
        }

        allRows.sort(Comparator.comparing((Row r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));

        int total = allRows.size();
        long pagesLong = (Math.max(0L, (long) total) + Math.max(1L, (long) limit) - 1L) / Math.max(1L, (long) limit);
        pagesLong = Math.max(1L, pagesLong);
        int totalPages = pagesLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.toIntExact(pagesLong);
        if (page > totalPages) {
            page = totalPages;
        }

        int start = Math.min((page - 1) * limit, total);
        int end = Math.min(start + limit, total);
        List<Row> pageRows = allRows.subList(start, end);

        String jsonData = buildJson(pageRows);
        String template = sanitizeTemplate(loadTemplate(req.getServletContext(), TEMPLATE_PATH));

        String title = "all".equals(scope)
                ? "All Inactive Users"
                : "Inactive Users: " + widgetNameById.getOrDefault(widgetIdFilter, widgetIdFilter);

        String sessionUser = safeSessionUser(s);
        String rendered = template
            .replace("${contextPath}", escapeHtml(contextPath))
            .replace("${user}", escapeHtml(sessionUser))
                .replace("${title}", escapeHtml(title))
                .replace("${scope}", escapeHtml(scope))
                .replace("${widgetId}", escapeHtml(widgetIdFilter))
                .replace("${days}", String.valueOf(days))
                .replace("${page}", String.valueOf(page))
                .replace("${limit}", String.valueOf(limit))
                .replace("${total}", String.valueOf(total))
                .replace("${totalPages}", String.valueOf(totalPages))
                .replace("${search}", escapeHtml(search))
                .replace("${rowsJson}", escapeForJs(jsonData));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        resp.getOutputStream().write(rendered.getBytes(StandardCharsets.UTF_8));
    
        } catch (IOException | ServletException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
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

    private FrustrationResult detectFrustration(List<String> prompts) {
        FrustrationResult out = new FrustrationResult();
        out.detected = false;
        out.score = 0.0;
        out.reason = "";

        if (prompts == null || prompts.isEmpty()) {
            return out;
        }

        double score = 0.0;
        String reason = "";

        String[] strong = {"frustrated", "angry", "annoyed", "furious", "ridiculous", "useless", "terrible"};
        String[] misunderstood = {"you don't understand", "you dont understand", "not what i asked", "wrong again", "not listening", "misunderstood"};

        boolean consistentCapsStyle = InactiveUsersFrustrationTextUtil.isConsistentCapsStyle(prompts);

        for (String raw : prompts) {
            String t = raw == null ? "" : raw.trim();
            String lower = t.toLowerCase();

            for (String k : strong) {
                if (lower.contains(k)) {
                    score += 0.30;
                    if (reason.isEmpty()) {
                        reason = "keyword:" + k;
                    }
                }
            }
            for (String k : misunderstood) {
                if (lower.contains(k)) {
                    score += 0.35;
                    if (reason.isEmpty()) {
                        reason = "misunderstood:" + k;
                    }
                }
            }
            if (lower.contains("!!!") || lower.contains("???")) {
                score += 0.15;
                if (reason.isEmpty()) {
                    reason = "punctuation";
                }
            }

            boolean nonFrustrationContext = InactiveUsersFrustrationTextUtil.isNonFrustrationContext(t);

            if (!nonFrustrationContext
                    && InactiveUsersFrustrationTextUtil.hasExplicitFrustrationSignal(t)
                    && !consistentCapsStyle) {
                score += 0.20;
                if (reason.isEmpty()) {
                    reason = "frustration_phrase";
                }
            }
        }

        if (score > 1.0) {
            score = 1.0;
        }
        out.score = score;
        out.detected = score >= 0.40;
        out.reason = reason;
        return out;
    }

    private String buildJson(List<Row> rows) {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Row r : rows) {
            arr.add(Json.createObjectBuilder()
                    .add("sessionId", nvl(r.sessionId))
                    .add("displayLabel", nvl(r.displayLabel))
                    .add("widgetId", nvl(r.widgetId))
                    .add("widgetLabel", nvl(r.widgetLabel))
                    .add("chatCount", r.chatCount)
                    .add("lastEntry", formatTimestamp(r.lastEntry))
                    .add("frustrationDetected", r.frustrationDetected)
                    .add("frustrationScore", r.frustrationScore)
                    .add("frustrationReason", nvl(r.frustrationReason)));
        }
        JsonObject obj = Json.createObjectBuilder().add("rows", arr).build();
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(); JsonWriter writer = Json.createWriter(bos)) {
            writer.writeObject(obj);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize inactive users JSON", e);
        }
    }

    private String loadTemplate(ServletContext context, String path) {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, "Template not found: {0}", path);
                return "";
            }
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load inactive users template: " + path, e);
            return "";
        }
    }

    private String sanitizeTemplate(String template) {
        if (template == null) {
            return null;
        }
        String normalized = template.replace("\u0000", "").replace("\r", "");
        if (normalized.length() > 500_000) {
            return normalized.substring(0, 500_000);
        }
        return normalized;
    }

    private int parseInt(String v, int fallback) {
        if (v == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid numeric parameter value", e);
            return fallback;
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private InactiveUsersListQueryService queryService() {
        return new InactiveUsersListQueryService(dataSourceHolder(), log);
    }

    private String safeSessionUser(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object user = session.getAttribute("user");
        if (!(user instanceof String userText)) {
            return "";
        }
        String normalized = userText.replace("\r", "").replace("\n", "").trim();
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String sanitizeWidgetId(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return SAFE_WIDGET_ID.matcher(trimmed).matches() ? trimmed : "";
    }

    private String sanitizeSearch(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_SEARCH_LENGTH) {
            return trimmed.substring(0, MAX_SEARCH_LENGTH);
        }
        return trimmed;
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? "" : ISO_INSTANT_FMT.format(value.toInstant());
    }

    private String escapeHtml(String in) {
        if (in == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}
