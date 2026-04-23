package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTermSelectionServlet", urlPatterns = {"/dashboard/term-review"})
public class DashboardTermSelectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardTermSelectionServlet.class.getName());

    private static final String TERM_SNAPSHOT_SESSION_KEY = "termDistributionSnapshots";

    // New key for enhancement: increase-only drilldown selection snapshots.
    // This should be populated by DashboardServlet for the current selected range.
    private static final String TERM_INCREASE_SNAPSHOT_SESSION_KEY = "termDistributionIncreaseSnapshots";

    private static final String MODE_INCREASE_ONLY = "increaseOnly";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String rawTerm = req.getParameter("term");
        if (rawTerm == null || rawTerm.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "term parameter is required.");
            return;
        }

        String normalizedTerm = normalize(rawTerm);

        String mode = normalize(req.getParameter("mode"));
        boolean increaseOnly = MODE_INCREASE_ONLY.equalsIgnoreCase(mode);

        @SuppressWarnings("unchecked")
        Map<String, List<TermChatSnapshot>> allSnapshotsByTerm
                = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_SNAPSHOT_SESSION_KEY);

        if (allSnapshotsByTerm == null || allSnapshotsByTerm.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No term data available.");
            return;
        }

        List<TermChatSnapshot> snapshots;

        if (increaseOnly) {
            @SuppressWarnings("unchecked")
            Map<String, List<TermChatSnapshot>> increasedSnapshotsByTerm
                    = (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_INCREASE_SNAPSHOT_SESSION_KEY);

            if (increasedSnapshotsByTerm == null || increasedSnapshotsByTerm.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No increased term data available.");
                return;
            }

            snapshots = findSnapshotsByTerm(increasedSnapshotsByTerm, normalizedTerm);
            if (snapshots == null || snapshots.isEmpty()) {
                log.fine(() -> "No increase snapshots for term='" + rawTerm + "' normalized='" + normalizedTerm + "'");
                resp.sendRedirect(req.getContextPath() + "/dashboard?msg=noIncreaseForTerm");
                return;
            }
        } else {
            snapshots = findSnapshotsByTerm(allSnapshotsByTerm, normalizedTerm);
            if (snapshots == null || snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
                return;
            }
        }

        String selectionLabel = increaseOnly ? rawTerm + " (Increase Only)" : rawTerm;

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                selectionLabel,
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            return;
        }

        String redirectUrl = req.getContextPath() + "/dashboard/widgets/drilldown/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8);
        resp.sendRedirect(redirectUrl);
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
            String key = entry.getKey();
            if (normalizeKey(key).equals(normalizedTarget)) {
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
