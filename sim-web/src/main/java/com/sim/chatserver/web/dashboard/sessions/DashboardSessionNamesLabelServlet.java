package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
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

        String sessionId = ServletRequestParamUtil.firstParam(req, "sessionId", 512, true, true);
        String displayName = ServletRequestParamUtil.firstParam(req, "displayName", 512, true, true);
        String email = ServletRequestParamUtil.firstParam(req, "email", 512, true, true);

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = readPartValue(req, "sessionId");
        }

        if (sessionId == null || sessionId.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "sessionId is required")
                    .build());
            return;
        }

        try {
            SessionLabelStore.saveLabel(sessionId, displayName, email);
            writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add("status", "ok").build());
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid session label request", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Invalid session label request")
                    .build());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to persist session label", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to save session label")
                    .build());
        }
    }

    private String readPartValue(HttpServletRequest req, String name) {
        try {
            Part part = req.getPart(name);
            if (part != null) {
                final int maxBytes = 4096;
                byte[] bytes = part.getInputStream().readNBytes(maxBytes + 1);
                if (bytes.length > maxBytes) {
                    return null;
                }
                return new String(bytes, StandardCharsets.UTF_8).trim();
            }
        } catch (IOException | ServletException e) {
            log.log(Level.FINER, "Unable to read multipart part '" + name + "'", e);
        }
        return null;
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required.")
                    .build());
            return false;
        }
        return true;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        ServletJsonResponseUtil.writeJson(resp, status, payload);
    }

}
