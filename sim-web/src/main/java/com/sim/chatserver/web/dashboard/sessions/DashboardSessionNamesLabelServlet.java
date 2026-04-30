package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.SessionLabelStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet(name = "DashboardSessionNamesLabelServlet", urlPatterns = {"/dashboard/session-names/label"})
@MultipartConfig
public class DashboardSessionNamesLabelServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionNamesLabelServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        String sessionId = req.getParameter("sessionId");
        String displayName = req.getParameter("displayName");
        String email = req.getParameter("email");

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = readPartValue(req, "sessionId");
        }

        resp.setContentType("application/json");
        if (sessionId == null || sessionId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"status\":\"error\",\"message\":\"sessionId is required\"}");
            return;
        }

        try {
            SessionLabelStore.saveLabel(sessionId, displayName, email);
            resp.getWriter().print("{\"status\":\"ok\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to persist session label", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"status\":\"error\",\"message\":\"Unable to save session label\"}");
        }
    }

    private String readPartValue(HttpServletRequest req, String name) {
        try {
            Part part = req.getPart(name);
            if (part != null) {
                byte[] bytes = part.getInputStream().readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            log.log(Level.FINER, "Unable to read multipart part '" + name + "'", e);
        }
        return null;
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
