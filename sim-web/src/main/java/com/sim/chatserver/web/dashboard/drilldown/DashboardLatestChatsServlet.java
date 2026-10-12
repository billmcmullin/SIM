package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardDrilldownSelectionQueryService;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardLatestChatsServlet", urlPatterns = {"/dashboard/latest-chats"})
public class DashboardLatestChatsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardLatestChatsServlet.class.getCanonicalName());
    private static final String LOGIN_DISPATCH_PATH = "/log" + "in";
    private static final DashboardDrilldownSelectionQueryService QUERY_SERVICE =
            new DashboardDrilldownSelectionQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            forwardSafeToLogin(req, resp);
            return;
        }

        int limit = parseLimit(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT, 32, true, true), 200);

            List<TermChatSnapshot> snapshots = QUERY_SERVICE.collectLatestChats(limit);
        if (snapshots.isEmpty()) {
            req.setAttribute("latestChats", "empty");
            req.getRequestDispatcher("/dashboard").forward(req, resp);
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Latest Chats",
                snapshots,
            "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create latest-chats selection.");
            return;
        }

        req.setAttribute(com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID, selectionId);
        req.getRequestDispatcher("/dashboard/widgets/drilldown/review").forward(req, resp);
    
        } catch (IOException | ServletException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
        }
    }

    private void forwardSafeToLogin(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAllowedForwardPath(LOGIN_DISPATCH_PATH)) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_FORWARD_PATH);
            return;
        }
        try {
            req.getRequestDispatcher(LOGIN_DISPATCH_PATH).forward(req, resp);
        } catch (IOException | ServletException ex) {
            log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNABLE_TO_FORWARD_LOGIN, ex);
            sendErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
        }
    }

    private boolean isAllowedForwardPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String p = path.trim();
        if (p.isEmpty() || p.charAt(0) != '/' || p.contains(com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR)) {
            return false;
        }
        return LOGIN_DISPATCH_PATH.equals(p);
    }

    private int parseLimit(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            if (v <= 0) {
                return fallback;
            }
            return Math.min(v, 2000);
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid latest chats limit", e);
            return fallback;
        }
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
