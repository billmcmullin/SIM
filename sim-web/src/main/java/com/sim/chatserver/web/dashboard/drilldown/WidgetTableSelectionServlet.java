package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.service.dashboard.WidgetTableSelectionQueryService;
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

@WebServlet(name = "WidgetTableSelectionServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/select-ids"})
public class WidgetTableSelectionServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableSelectionServlet.class.getName());
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_:-]{1,80}$");
    private static final WidgetTableSelectionQueryService QUERY_SERVICE = new WidgetTableSelectionQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isLoggedIn(req, resp)) {
            return;
        }
        String widgetId = sanitizeWidgetId(ServletRequestParamUtil.firstParam(req, "widgetId", 256, true, true));
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
                    ServletRequestParamUtil.firstParam(req, "search", 256, true, true)
            );

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            chatIds.forEach(arrayBuilder::add);

            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
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
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
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
            log.log(Level.WARNING, "Unable to list widgets", e);
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
                .add("status", "error")
                .add("message", message == null ? "" : message)
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

}
