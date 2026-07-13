package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.SessionLabelStore;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
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

        String sessionId = firstParam(req, "sessionId");
        String displayName = firstParam(req, "displayName");
        String email = firstParam(req, "email");

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
                byte[] bytes = part.getInputStream().readAllBytes();
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
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(payload == null ? Json.createObjectBuilder().build() : payload);
        }
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
}
