package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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

import jakarta.enterprise.inject.spi.CDI;
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
    private static final String SCOPE_TERM_ENTRIES = "termEntries";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    AppDataSourceHolder dsHolder;
    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        LocalDate date = resolveDate(req, resp);
        if (date == null) {
            return; // error already sent
        }

        String rawTerm = firstParam(req, "term");
        String requestedTerm = (rawTerm == null) ? "" : rawTerm.trim();
        String scope = firstParam(req, "scope");
        boolean termEntriesOnly = SCOPE_TERM_ENTRIES.equalsIgnoreCase(scope == null ? "" : scope.trim());

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
            snapshots = filterSnapshotsByTerm(snapshots, requestedTerm);

            if (snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested day and term.");
                return;
            }
        } else if (termEntriesOnly) {
            snapshots = filterSnapshotsByKnownTerms(snapshots);

            if (snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No term entry chats found for the requested day.");
                return;
            }
        }

        String selectionLabel = requestedTerm.isBlank()
                ? (termEntriesOnly ? ("Date " + date + " • Term Entries") : ("Date " + date))
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

        req.setAttribute("selectionId", selectionId);
        req.getRequestDispatcher("/dashboard/widgets/drilldown/review").forward(req, resp);
    }

    private LocalDate resolveDate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String dateParam = firstParam(req, "date");
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                return LocalDate.parse(dateParam.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Invalid date parameter for relative date selection");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date value. Use YYYY-MM-DD.");
                return null;
            }
        }

        String day = firstParam(req, "day");
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

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
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
            allTerms = termsStore().listAll();
        } catch (SQLException | IllegalStateException e) {
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
            Pattern compiled = TermMatcher.buildStrictPattern(term);
            if (compiled == null) {
                continue;
            }
            activeTerms.add(term);
            compiledPatterns.add(compiled);
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

    private List<TermChatSnapshot> filterSnapshotsByKnownTerms(List<TermChatSnapshot> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore().listAll();
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load term definitions for term-entry filtering", e);
            return List.of();
        }

        if (allTerms == null || allTerms.isEmpty()) {
            return List.of();
        }

        List<Pattern> compiledPatterns = new ArrayList<>();
        for (TermDefinition term : allTerms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }

            String name = term.getName();
            if (name == null || name.isBlank() || OTHER_PARASOFT_LABEL.equalsIgnoreCase(name.trim())) {
                continue;
            }

            Pattern compiled = TermMatcher.buildStrictPattern(term);
            if (compiled != null) {
                compiledPatterns.add(compiled);
            }
        }

        if (compiledPatterns.isEmpty()) {
            return List.of();
        }

        List<TermChatSnapshot> out = new ArrayList<>();
        for (TermChatSnapshot snap : source) {
            String prompt = snap == null || snap.getPrompt() == null ? "" : snap.getPrompt();
            String sanitized = TextSanitizer.sanitizeForMatching(prompt);

            boolean matched = false;
            for (Pattern p : compiledPatterns) {
                Matcher m = p.matcher(sanitized);
                if (m.find()) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                out.add(snap);
            }
        }

        return out;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to list widgets for date review", e);
            return List.of();
        }
    }

    private String firstParam(HttpServletRequest req, String name) {
        return RequestParamContext.from(req).first(name);
    }

    private AppDataSourceHolder dataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private TermsStore termsStore() {
        if (termsStore != null) {
            return termsStore;
        }
        return CDI.current().select(TermsStore.class).get();
    }

    private static final class RequestParamContext {

        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String value = request.getParameter(name);
            if (value == null) {
                return null;
            }
            String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (normalized.isEmpty()) {
                return null;
            }
            return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
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
