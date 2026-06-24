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

    private static final String JSON_UTF8 = "application/json; charset=UTF-8";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        String rawTerm = req.getParameter("term");
        if (rawTerm == null || rawTerm.isBlank()) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            }
            return;
        }

        String normalizedTerm = normalize(rawTerm);
        String mode = normalize(req.getParameter("mode"));
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
            if (snapshots == null || snapshots.isEmpty()) {
                log.fine(() -> "No increase snapshots for term='" + rawTerm + "' normalized='" + normalizedTerm + "'");
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No increased chats found for that term today.");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/dashboard?msg=noIncreaseForTerm");
                }
                return;
            }
            selectionLabel = rawTerm + " (Increase Only)";
        } else if (yesterdayOnly) {
            @SuppressWarnings("unchecked")
            Map<String, List<TermChatSnapshot>> yesterdaySnapshotsByTerm
                    = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_YESTERDAY_SNAPSHOT_SESSION_KEY);

            if (yesterdaySnapshotsByTerm == null || yesterdaySnapshotsByTerm.isEmpty()) {
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No yesterday term data available.");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/dashboard?msg=noYesterdayForTerm");
                }
                return;
            }

            snapshots = findSnapshotsByTerm(yesterdaySnapshotsByTerm, normalizedTerm);
            if (snapshots == null || snapshots.isEmpty()) {
                if (wantsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for that term yesterday.");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/dashboard?msg=noYesterdayForTerm");
                }
                return;
            }
            selectionLabel = rawTerm + " (Yesterday)";
        } else {
            snapshots = findSnapshotsByTerm(allSnapshotsByTerm, normalizedTerm);
            if (snapshots == null || snapshots.isEmpty()) {
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
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            if (wantsJson(req)) {
                writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            }
            return;
        }

        String reviewUrl = req.getContextPath() + "/dashboard/widgets/drilldown/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8);

        if (wantsJson(req)) {
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("selectionId", selectionId)
                    .add("reviewUrl", reviewUrl)
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, body.toString());
            return;
        }

        resp.sendRedirect(reviewUrl);
    }

    private boolean wantsJson(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri != null && uri.endsWith("/select")) {
            return true;
        }
        String accept = req.getHeader("Accept");
        return accept != null && accept.toLowerCase().contains("application/json");
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject body = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, body.toString());
    }

    private void writeJson(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);
        resp.getWriter().write(body);
    }

    private List<TermChatSnapshot> findSnapshotsByTerm(Map<String, List<TermChatSnapshot>> snapshotsByTerm, String rawTerm) {
        if (snapshotsByTerm == null || snapshotsByTerm.isEmpty()) {
            return null;
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
        return null;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeKey(String s) {
        return normalize(s).toLowerCase();
    }
}
