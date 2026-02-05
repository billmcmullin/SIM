package com.sim.chatserver.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sim.chatserver.term.TermChatSnapshot;

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

        @SuppressWarnings("unchecked")
        Map<String, List<TermChatSnapshot>> map =
                (Map<String, List<TermChatSnapshot>>) session.getAttribute(TERM_SNAPSHOT_SESSION_KEY);

        if (map == null || map.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No term data available.");
            return;
        }

        List<TermChatSnapshot> snapshots = map.get(rawTerm);
        if (snapshots == null || snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the selected term.");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                rawTerm,
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create term selection.");
            return;
        }

        String redirectUrl = req.getContextPath() + "/admin/widgets/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8);
        resp.sendRedirect(redirectUrl);
    }
}
