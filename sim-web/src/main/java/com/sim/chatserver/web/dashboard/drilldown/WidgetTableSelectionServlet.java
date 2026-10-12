package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.service.dashboard.WidgetTableSelectionQueryService;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "WidgetTableSelectionServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/select-ids"})
public class WidgetTableSelectionServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableSelectionServlet.class.getCanonicalName());
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_:-]{1,80}$");
    private static final WidgetTableSelectionQueryService QUERY_SERVICE = new WidgetTableSelectionQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isLoggedIn(req, resp)) {
            return;
        }
        String widgetId = sanitizeWidgetId(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, 256, true, true));
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId required.");
            return;
        }

        WidgetEntry plugin = findWidget(widgetId);
        if (plugin == null) {
            jsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Widget not found.");
            return;
        }

        try {
            List<String> chatIds = QUERY_SERVICE.selectChatIds(
                    widgetId,
                    ServletRequestParamUtil.firstParam(req, "filterPrompt", 256, true, true),
                    ServletRequestParamUtil.firstParam(req, "filterResponse", 256, true, true),
                    ServletRequestParamUtil.firstParam(req, CommonLiteralConstants.PARAM_SEARCH, 256, true, true)
            );

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            chatIds.forEach(arrayBuilder::add);

            JsonObject body = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                    .add("chatIds", arrayBuilder)
                    .add("totalRows", chatIds.size())
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, body);
        } catch (java.util.NoSuchElementException e) {
            log.log(Level.FINE, "Widget table not found for selection", e);
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Table for widget does not exist.");
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to collect chat ids", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch chat ids.");
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
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

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return false;
        }
        return true;
    }

    private WidgetEntry findWidget(String widgetId) {
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry widget : widgets) {
                if (widget != null && widgetId.equals(widget.getWidgetId())) {
                    return widget;
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_LIST_WIDGETS, e);
        }
        return null;
    }

    private String sanitizeWidgetId(String widgetId) {
        if (widgetId == null) {
            return null;
        }
        String trimmed = widgetId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 80) {
            trimmed = trimmed.substring(0, 80);
        }
        return SAFE_WIDGET_ID.matcher(trimmed).matches() ? trimmed : null;
    }

    private void jsonError(HttpServletResponse resp, int status, String message) {
        JsonObject payload = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, message == null ? "" : message)
                .build();
        writeJson(resp, status, payload);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        JsonObject safePayload = payload == null ? Json.createObjectBuilder().build() : payload;
        try {
            ServletJsonResponseUtil.writeJson(resp, status, safePayload);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to write widget table selection response", e);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback widget table selection error", sendErrorFailure);
            }
        }
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
