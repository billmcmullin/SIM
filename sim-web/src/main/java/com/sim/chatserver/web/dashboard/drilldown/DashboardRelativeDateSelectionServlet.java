package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Convenience endpoint for date drilldowns: -
 * /dashboard/sessions/drilldown/date-review-relative?day=today|yesterday -
 * /dashboard/sessions/drilldown/date-review-relative?date=YYYY-MM-DD Optional
 * term filtering: - ...&term=Your%20Term
 */
@WebServlet(name = "DashboardRelativeDateSelectionServlet", urlPatterns = {"/dashboard/sessions/drilldown/date-review-relative"})
public class DashboardRelativeDateSelectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardRelativeDateSelectionServlet.class.getName());
    private static final String OTHER_PARASOFT_LABEL = "Other Parasoft Match";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    AppDataSourceHolder dsHolder;

    @Inject
    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        LocalDate date = resolveDate(req, resp);
        if (date == null) {
            return; // error already sent
        }

        String rawTerm = req.getParameter("term");
        String requestedTerm = (rawTerm == null) ? "" : rawTerm.trim();

        List<TermChatSnapshot> snapshots;
        try {
            snapshots = collectDateEntries(date);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load date entries for review", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load chats for day.");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested day.");
            return;
        }

        if (!requestedTerm.isBlank()) {
            try {
                snapshots = filterSnapshotsByTerm(snapshots, requestedTerm);
            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to filter day entries by term", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to filter chats by term.");
                return;
            }

            if (snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested day and term.");
                return;
            }
        }

        String selectionLabel = requestedTerm.isBlank()
                ? ("Date " + date)
                : ("Date " + date + " • " + requestedTerm);

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                selectionLabel,
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create a review selection.");
            return;
        }

        StringBuilder redirect = new StringBuilder()
                .append(req.getContextPath())
                .append("/dashboard/widgets/drilldown/review?selectionId=")
                .append(URLEncoder.encode(selectionId, StandardCharsets.UTF_8))
                .append("&date=")
                .append(URLEncoder.encode(date.toString(), StandardCharsets.UTF_8));

        if (!requestedTerm.isBlank()) {
            redirect.append("&term=").append(URLEncoder.encode(requestedTerm, StandardCharsets.UTF_8));
        }

        resp.sendRedirect(redirect.toString());
    }

    private LocalDate resolveDate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String dateParam = req.getParameter("date");
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                return LocalDate.parse(dateParam.trim(), DATE_FMT);
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date value. Use YYYY-MM-DD.");
                return null;
            }
        }

        String day = req.getParameter("day");
        if (day == null || day.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Provide day=today|yesterday or date=YYYY-MM-DD.");
            return null;
        }

        return switch (day.trim().toLowerCase()) {
            case "today" ->
                LocalDate.now(ZoneId.systemDefault());
            case "yesterday" ->
                LocalDate.now(ZoneId.systemDefault()).minusDays(1);
            default -> {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid day value. Use today or yesterday.");
                yield null;
            }
        };
    }

    private List<TermChatSnapshot> collectDateEntries(LocalDate date) throws SQLException {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return snapshots;
        }

        Timestamp startTs = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(date.plusDays(1).atStartOfDay());

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ? ORDER BY created_at DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, startTs);
                    ps.setTimestamp(2, endTs);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");
                            String response = rs.getString("response_text");
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = rs.getString("session_id");

                            snapshots.add(new TermChatSnapshot(
                                    date.toString(),
                                    widgetId,
                                    chatId == null ? "" : chatId,
                                    prompt == null ? "" : prompt,
                                    response == null ? "" : response,
                                    createdAt,
                                    sessionId == null ? "" : sessionId
                            ));
                        }
                    }
                }
            }
        }

        return snapshots;
    }

    private List<TermChatSnapshot> filterSnapshotsByTerm(List<TermChatSnapshot> source, String requestedTerm) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore.listAll();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load term definitions for date+term filtering", e);
            return List.of();
        }

        if (allTerms == null || allTerms.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> activeTerms = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();

        for (TermDefinition term : allTerms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            activeTerms.add(term);
            compiledPatterns.add(TermMatcher.buildStrictPattern(term));
        }

        if (activeTerms.isEmpty()) {
            return List.of();
        }

        List<TermChatSnapshot> out = new ArrayList<>();
        String target = requestedTerm.trim();

        for (TermChatSnapshot snap : source) {
            String prompt = snap == null || snap.getPrompt() == null ? "" : snap.getPrompt();
            String sanitized = TextSanitizer.sanitizeForMatching(prompt);

            TermDefinition bestTerm = null;
            int bestStart = Integer.MAX_VALUE;

            for (int i = 0; i < compiledPatterns.size(); i++) {
                try {
                    Matcher m = compiledPatterns.get(i).matcher(sanitized);
                    if (m.find()) {
                        int start = m.start();
                        if (start < bestStart) {
                            bestStart = start;
                            bestTerm = activeTerms.get(i);
                            if (bestStart == 0) {
                                break;
                            }
                        }
                    }
                } catch (Exception ignore) {
                    // skip broken regex
                }
            }

            String classified = (bestTerm != null && bestTerm.getName() != null && !bestTerm.getName().isBlank())
                    ? bestTerm.getName()
                    : OTHER_PARASOFT_LABEL;

            if (classified.equalsIgnoreCase(target)) {
                out.add(snap);
            }
        }

        return out;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets for date review", e);
            return List.of();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}
