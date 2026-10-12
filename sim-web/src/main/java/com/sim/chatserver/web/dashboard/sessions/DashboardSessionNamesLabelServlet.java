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

import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardSessionNamesLabelServlet", urlPatterns = {"/dashboard/session-names/label"})
@MultipartConfig
public class DashboardSessionNamesLabelServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionNamesLabelServlet.class.getCanonicalName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!requireAuth(req, resp)) {
            return;
        }

        String sessionId = ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID, 512, true, true);
        String displayName = ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_DISPLAY_NAME, 512, true, true);
        String email = ServletRequestParamUtil.firstParam(req, "email", 512, true, true);

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = readPartValue(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID);
        }

        if (sessionId == null || sessionId.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "sessionId is required")
                    .build());
            return;
        }

        try {
            SessionLabelStore.saveLabel(sessionId, displayName, email);
            writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok").build());
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid session label request", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Invalid session label request")
                    .build());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to persist session label", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to save session label")
                    .build());
        }
    
        } catch (IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private String readPartValue(HttpServletRequest req, String name) {
        try {
            Part part = req.getPart(name);
            if (part != null) {
                final int maxBytes = 4096;
                byte[] bytes = part.getInputStream().readAllBytes();
                if (bytes == null) {
                    return null;
                }
                if (bytes.length > maxBytes) {
                    return null;
                }
                return new String(bytes, StandardCharsets.UTF_8).trim();
            }
        } catch (IOException | ServletException e) {
            log.log(Level.FINER, "Unable to read multipart part '" + name + '\'', e);
        }
        return null;
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED)
                    .build());
            return false;
        }
        return true;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write session label response", e);
            throw new IllegalStateException(com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE_NO_PERIOD, e);
        }
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
