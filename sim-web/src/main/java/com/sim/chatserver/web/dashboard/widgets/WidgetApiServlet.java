package com.sim.chatserver.web.dashboard.widgets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import com.sim.chatserver.widget.WidgetStore.DuplicateWidgetIdException;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetApiServlet", urlPatterns = {"/dashboard/widgets"})
public class WidgetApiServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(WidgetApiServlet.class.getName());
    private static final String APPLICATION_JSON = "application/json; charset=UTF-8";
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        RequestParamContext requestContext = RequestParamContext.from(req);
        String filter = requestContext.first("filter", 256);
        try {
            List<WidgetEntry> widgets = WidgetStore.list(filter);
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (WidgetEntry widget : widgets) {
                arr.add(widgetToJson(widget));
            }
            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("widgets", arr)
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Unable to load widget entries", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Unable to load widget entries.")
                            .build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        RequestParamContext requestContext = RequestParamContext.from(req);
        String widgetId = sanitizeWidgetId(requestContext.first("widgetId", 256));
        String displayName = sanitizeDisplayName(requestContext.first("displayName", 256));
        String idValue = requestContext.first("id", 256);

        if (widgetId == null || displayName == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Widget ID and name are required.")
                            .build());
            return;
        }

        Integer id = null;
        if (idValue != null && !idValue.isBlank()) {
            try {
                id = Integer.valueOf(idValue.trim());
            } catch (NumberFormatException ex) {
                LOG.log(Level.FINE, "Invalid widget ID parameter", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Json.createObjectBuilder()
                                .add("status", "error")
                                .add("message", "ID must be an integer.")
                                .build());
                return;
            }
        }

        try {
            WidgetEntry saved = WidgetStore.save(id, widgetId, displayName);
            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("widget", widgetToJson(saved))
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (DuplicateWidgetIdException ex) {
            LOG.log(Level.FINE, "Duplicate widget ID", ex);
            writeJson(resp, HttpServletResponse.SC_CONFLICT,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Widget ID already exists.")
                            .build());
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, "Invalid widget input", ex);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Invalid widget input.")
                            .build());
        } catch (SQLException ex) {
            log("Widget persistence error", ex);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Unable to persist widget entry.")
                            .build());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        RequestParamContext requestContext = RequestParamContext.from(req);
        String idsParam = requestContext.first("ids", 256);

        if (idsParam == null || idsParam.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "No widget IDs provided to delete.")
                            .build());
            return;
        }

        List<Integer> ids = new ArrayList<>();
        for (String token : Arrays.stream(idsParam.split(",")).map(String::trim).filter(t -> !t.isBlank()).toList()) {
            try {
                ids.add(Integer.valueOf(token));
            } catch (NumberFormatException ex) {
                LOG.log(Level.FINE, "Invalid widget IDs parameter", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Json.createObjectBuilder()
                                .add("status", "error")
                                .add("message", "No valid widget IDs provided.")
                                .build());
                return;
            }
        }

        if (ids.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "No valid widget IDs provided.")
                            .build());
            return;
        }

        try {
            int deleted = WidgetStore.deleteBulk(ids);
            writeJson(resp, HttpServletResponse.SC_OK,
                    Json.createObjectBuilder()
                            .add("status", "ok")
                            .add("deleted", deleted)
                            .build());
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Unable to delete widget entries", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Unable to delete widget entries.")
                            .build());
        }
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder()
                            .add("status", "error")
                            .add("message", "Authentication required.")
                            .build());
            return false;
        }
        return true;
    }

    private JsonObject widgetToJson(WidgetEntry entry) {
        return Json.createObjectBuilder()
                .add("id", entry.getId())
                .add("widgetId", entry.getWidgetId() == null ? "" : entry.getWidgetId())
                .add("displayName", entry.getDisplayName() == null ? "" : entry.getDisplayName())
                .build();
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(APPLICATION_JSON);
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(body);
        }
    }

    private String sanitizeWidgetId(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (!SAFE_WIDGET_ID.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed;
    }

    private String sanitizeDisplayName(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
    }

    private static final class RequestParamContext {
        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name, int maxLen) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String value = request.getParameter(name);
            String normalized = normalize(value, maxLen);
            if (normalized != null) {
                return normalized;
            }
            return null;
        }

        private String normalize(String value, int maxLen) {
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            int effectiveMax = maxLen <= 0 ? 256 : maxLen;
            return trimmed.length() > effectiveMax ? trimmed.substring(0, effectiveMax) : trimmed;
        }
    }
}
