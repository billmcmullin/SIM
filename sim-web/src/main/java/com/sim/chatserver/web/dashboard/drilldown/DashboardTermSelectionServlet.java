package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTermSelectionServlet", urlPatterns = {"/dashboard/term-review", "/dashboard/term-review/select"})
public class DashboardTermSelectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardTermSelectionServlet.class.getName());

    private static final String TERM_SNAPSHOT_SESSION_KEY = "termDistributionSnapshots";
    private static final String TERM_INCREASE_SNAPSHOT_SESSION_KEY = "termDistributionIncreaseSnapshots";
    private static final String TERM_YESTERDAY_SNAPSHOT_SESSION_KEY = "termDistributionYesterdaySnapshots";

    private static final String MODE_INCREASE_ONLY = "increaseOnly";
    private static final String MODE_YESTERDAY_ONLY = "yesterdayOnly";
    private static final String OTHER_PARASOFT_LABEL = "Other Parasoft Match";
    private static final String REVIEW_FORWARD_PATH = "/dashboard/widgets/drilldown/review";

    private static final Set<String> SAFE_FORWARD_PATHS = Set.of("/login", REVIEW_FORWARD_PATH);
    private static final Pattern SAFE_TERM_PATH = Pattern.compile("^/dashboard/widgets/drilldown/review\\?selectionId=[A-Za-z0-9%._-]+$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String contextPath = ServletPathUtil.safeContextPathStrict(req.getServletContext().getContextPath());
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            } else {
                forwardSafe(req, resp, "/login", HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        String rawTerm = ServletRequestParamUtil.firstParam(req, "term", 256, true, true);
        if (rawTerm == null || rawTerm.isBlank()) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            } else {
                sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            }
            return;
        }

        String normalizedTerm = normalize(rawTerm);
        String mode = normalize(ServletRequestParamUtil.firstParam(req, "mode", 64, true, true));
        boolean increaseOnly = MODE_INCREASE_ONLY.equalsIgnoreCase(mode);
        boolean yesterdayOnly = MODE_YESTERDAY_ONLY.equalsIgnoreCase(mode);

        @SuppressWarnings("unchecked")
        Map<String, List<TermChatSnapshot>> allSnapshotsByTerm
                = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_SNAPSHOT_SESSION_KEY);

        if (allSnapshotsByTerm == null || allSnapshotsByTerm.isEmpty()) {
            allSnapshotsByTerm = loadSnapshotsForRange(null, LocalDate.now(ZoneId.systemDefault()));
            storeSnapshotsOnSession(session, TERM_SNAPSHOT_SESSION_KEY, allSnapshotsByTerm);
        }

        List<TermChatSnapshot> snapshots;
        String selectionLabel;

        if (increaseOnly) {
            @SuppressWarnings("unchecked")
            Map<String, List<TermChatSnapshot>> increasedSnapshotsByTerm
                    = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_INCREASE_SNAPSHOT_SESSION_KEY);

            if (increasedSnapshotsByTerm == null || increasedSnapshotsByTerm.isEmpty()) {
                LocalDate today = LocalDate.now(ZoneId.systemDefault());
                increasedSnapshotsByTerm = loadSnapshotsForRange(today, today);
                storeSnapshotsOnSession(session, TERM_INCREASE_SNAPSHOT_SESSION_KEY, increasedSnapshotsByTerm);
            }

            snapshots = findSnapshotsByTerm(increasedSnapshotsByTerm, normalizedTerm);
            if (snapshots.isEmpty()) {
                // Backward compatibility: some UI links for Other Parasoft Match used increaseOnly while
                // actually intending "show all" term chats.
                if (OTHER_PARASOFT_LABEL.equalsIgnoreCase(normalizedTerm)) {
                    snapshots = findSnapshotsByTerm(allSnapshotsByTerm, normalizedTerm);
                    if (!snapshots.isEmpty()) {
                        selectionLabel = rawTerm;
                    } else {
                        if (wantsJson(req)) {
                            writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                        } else {
                            sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                        }
                        return;
                    }
                } else {
                    if (increasedSnapshotsByTerm == null || increasedSnapshotsByTerm.isEmpty()) {
                        if (wantsJson(req)) {
                            writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No increased term data available.");
                        } else {
                            sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No increased term data available.");
                        }
                        return;
                    }
                    log.fine(() -> "No increase snapshots for term=" + '\'' + rawTerm + '\''
                            + " normalized=" + '\'' + normalizedTerm + '\'');
                    if (wantsJson(req)) {
                        writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No increased chats found for that term today.");
                    } else {
                        sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No increased chats found for that term today.");
                    }
                    return;
                }
            } else {
                selectionLabel = rawTerm + " (Increase Only)";
            }
        } else if (yesterdayOnly) {
            @SuppressWarnings("unchecked")
            Map<String, List<TermChatSnapshot>> yesterdaySnapshotsByTerm
                    = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_YESTERDAY_SNAPSHOT_SESSION_KEY);

            if (yesterdaySnapshotsByTerm == null || yesterdaySnapshotsByTerm.isEmpty()) {
                LocalDate yesterday = LocalDate.now(ZoneId.systemDefault()).minusDays(1);
                yesterdaySnapshotsByTerm = loadSnapshotsForRange(yesterday, yesterday);
                storeSnapshotsOnSession(session, TERM_YESTERDAY_SNAPSHOT_SESSION_KEY, yesterdaySnapshotsByTerm);
            }

            snapshots = findSnapshotsByTerm(yesterdaySnapshotsByTerm, normalizedTerm);
            if (snapshots.isEmpty()) {
                if (yesterdaySnapshotsByTerm == null || yesterdaySnapshotsByTerm.isEmpty()) {
                    if (wantsJson(req)) {
                        writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No yesterday term data available.");
                    } else {
                        sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No yesterday term data available.");
                    }
                    return;
                }
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for that term yesterday.");
                } else {
                    sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for that term yesterday.");
                }
                return;
            }
            selectionLabel = rawTerm + " (Yesterday)";
        } else {
            snapshots = findSnapshotsByTerm(allSnapshotsByTerm, normalizedTerm);
            if (snapshots.isEmpty()) {
                if (allSnapshotsByTerm == null || allSnapshotsByTerm.isEmpty()) {
                    if (wantsJson(req)) {
                        writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No term data available.");
                    } else {
                        sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No term data available.");
                    }
                    return;
                }
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                } else {
                    sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                }
                return;
            }
            selectionLabel = rawTerm;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                selectionLabel,
                snapshots,
            contextPath + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            } else {
                sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            }
            return;
        }

        String reviewForwardPath = REVIEW_FORWARD_PATH;
        String reviewPath = "/dashboard/widgets/drilldown/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8);

        if (wantsJson(req)) {
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("selectionId", selectionId)
                    .add("reviewUrl", contextPath + reviewPath)
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, body);
            return;
        }

        if (!isAllowedForwardPath(reviewForwardPath)) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        req.setAttribute("selectionId", selectionId);
        forwardSafe(req, resp, reviewForwardPath, HttpServletResponse.SC_BAD_REQUEST);
    }

    private boolean wantsJson(HttpServletRequest req) {
        if (req == null || req.getHttpServletMapping() == null) {
            return false;
        }
        String pattern = req.getHttpServletMapping().getPattern();
        return pattern != null && pattern.endsWith("/select");
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) {
        JsonObject body = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, body);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write term-selection JSON response", ex);
            sendErrorSafe(resp, status);
        }
    }

    private List<TermChatSnapshot> findSnapshotsByTerm(Map<String, List<TermChatSnapshot>> snapshotsByTerm, String rawTerm) {
        if (snapshotsByTerm == null || snapshotsByTerm.isEmpty()) {
            return List.of();
        }

        List<TermChatSnapshot> direct = snapshotsByTerm.get(rawTerm);
        if (direct != null && !direct.isEmpty()) {
            return direct;
        }

        String normalizedTarget = normalizeKey(rawTerm);
        for (Map.Entry<String, List<TermChatSnapshot>> entry : snapshotsByTerm.entrySet()) {
            if (normalizeKey(entry.getKey()).equals(normalizedTarget)) {
                List<TermChatSnapshot> value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return List.of();
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeKey(String s) {
        return normalize(s).toLowerCase(Locale.ROOT);
    }

    private Map<String, List<TermChatSnapshot>> loadSnapshotsForRange(LocalDate rangeStart, LocalDate rangeEnd) {
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return Map.of();
        }

        DashboardTermService termService = new DashboardTermService(termsStore());
        Connection conn = null;
        try {
            conn = dataSourceHolder().getDataSource().getConnection();
            var summary = termService.buildTermSummary(
                    conn,
                    widgets,
                    termService.loadAllTerms(),
                    rangeStart,
                    rangeEnd
            );
            if (summary == null) {
                return Map.of();
            }
            return summary.copyTermSnapshots();
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to rebuild term snapshots on demand", ex);
            return Map.of();
        } finally {
            closeQuietly(conn);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // ignore close failure
            }
        }
    }

    private void storeSnapshotsOnSession(HttpSession session, String key, Map<String, List<TermChatSnapshot>> snapshots) {
        if (session == null || key == null || key.isBlank()) {
            return;
        }
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }
        session.setAttribute(key, snapshots);
    }

    private List<WidgetEntry> listWidgets() {
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            return widgets == null ? List.of() : widgets;
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to list widgets for term selection", ex);
            return List.of();
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

    private void forwardSafe(HttpServletRequest req, HttpServletResponse resp, String path, int fallbackStatus) {
        if (!isAllowedForwardPath(path)) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            req.getRequestDispatcher(path).forward(req, resp);
        } catch (ServletException | IOException ex) {
            log.log(Level.FINE, "Unable to forward term selection request", ex);
            sendErrorSafe(resp, fallbackStatus);
        }
    }

    private boolean isAllowedForwardPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String p = path.trim();
        if (p.isEmpty() || p.charAt(0) != '/' || p.contains("://") || p.contains("\r") || p.contains("\n")) {
            return false;
        }
        if (SAFE_FORWARD_PATHS.contains(p)) {
            return true;
        }
        return SAFE_TERM_PATH.matcher(p).matches();
    }

    private void sendErrorSafe(HttpServletResponse resp, int status) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send term-selection error response", ex);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status, Objects.toString(message, ""));
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send term-selection error response", ex);
            sendErrorSafe(resp, status);
        }
    }
}
