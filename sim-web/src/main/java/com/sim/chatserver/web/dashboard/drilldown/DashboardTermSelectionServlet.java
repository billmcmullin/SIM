package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
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

    private static final String JSON_UTF8 = "application/json; charset=UTF-8";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = safeContextPath(req.getServletContext().getContextPath());
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            } else {
                req.getRequestDispatcher("/login").forward(req, resp);
            }
            return;
        }

        String rawTerm = firstParam(req, "term");
        if (rawTerm == null || rawTerm.isBlank()) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            }
            return;
        }

        String normalizedTerm = normalize(rawTerm);
    String mode = normalize(firstParam(req, "mode"));
        boolean increaseOnly = MODE_INCREASE_ONLY.equalsIgnoreCase(mode);
        boolean yesterdayOnly = MODE_YESTERDAY_ONLY.equalsIgnoreCase(mode);

        @SuppressWarnings("unchecked")
        Map<String, List<TermChatSnapshot>> allSnapshotsByTerm
                = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_SNAPSHOT_SESSION_KEY);

        if (allSnapshotsByTerm == null || allSnapshotsByTerm.isEmpty()) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No term data available.");
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No term data available.");
            }
            return;
        }

        List<TermChatSnapshot> snapshots;
        String selectionLabel;

        if (increaseOnly) {
            @SuppressWarnings("unchecked")
            Map<String, List<TermChatSnapshot>> increasedSnapshotsByTerm
                    = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_INCREASE_SNAPSHOT_SESSION_KEY);

            if (increasedSnapshotsByTerm == null || increasedSnapshotsByTerm.isEmpty()) {
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No increased term data available.");
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No increased term data available.");
                }
                return;
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
                            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                        }
                        return;
                    }
                } else {
                    log.fine(() -> "No increase snapshots for term='" + rawTerm + "' normalized='" + normalizedTerm + "'");
                    if (wantsJson(req)) {
                        writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No increased chats found for that term today.");
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No increased chats found for that term today.");
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
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No yesterday term data available.");
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No yesterday term data available.");
                }
                return;
            }

            snapshots = findSnapshotsByTerm(yesterdaySnapshotsByTerm, normalizedTerm);
            if (snapshots.isEmpty()) {
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for that term yesterday.");
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for that term yesterday.");
                }
                return;
            }
            selectionLabel = rawTerm + " (Yesterday)";
        } else {
            snapshots = findSnapshotsByTerm(allSnapshotsByTerm, normalizedTerm);
            if (snapshots.isEmpty()) {
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
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
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            }
            return;
        }

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

        req.getRequestDispatcher(reviewPath).forward(req, resp);
    }

    private boolean wantsJson(HttpServletRequest req) {
        if (req == null || req.getHttpServletMapping() == null) {
            return false;
        }
        String pattern = req.getHttpServletMapping().getPattern();
        return pattern != null && pattern.endsWith("/select");
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject body = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, body);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(body);
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

    private String firstParam(HttpServletRequest req, String name) {
        Map<String, String[]> params = req.getParameterMap();
        if (params == null) {
            return null;
        }
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    private String safeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (!trimmed.startsWith("/") || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeKey(String s) {
        return normalize(s).toLowerCase();
    }
}
