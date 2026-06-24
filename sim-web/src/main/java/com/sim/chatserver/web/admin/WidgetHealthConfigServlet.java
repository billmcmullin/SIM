package com.sim.chatserver.web.admin;

import com.sim.chatserver.service.widget.WidgetHealthConfigStore;
import com.sim.chatserver.service.widget.WidgetHealthConfigStore.WidgetHealthConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /admin/widget-health-config POST /admin/widget-health-config
 */
public class WidgetHealthConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetHealthConfigServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    private transient WidgetHealthConfigStore store;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            store = new WidgetHealthConfigStore(dsHolder.getDataSource());
            store.ensureTable();
            store.ensureDefaultRow();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to initialize WidgetHealthConfigStore", e);
            throw new ServletException("Failed to initialize widget health config store", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            ensureStore();

            WidgetHealthConfig cfg = store.load();
            if (cfg == null) {
                store.ensureDefaultRow();
                cfg = store.load();
            }

            writeJson(resp, HttpServletResponse.SC_OK, toJson(cfg).toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load widget health config", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorJson("Unable to load widget health config."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            ensureStore();

            String body = req.getReader().lines().reduce("", (a, b) -> a + b);
            JsonObject in = parseJson(body);

            WidgetHealthConfig cfg = new WidgetHealthConfig();
            cfg.setHealthcheckUrl(stringOrNull(in, "healthcheckUrl"));
            cfg.setMethod(stringOrNull(in, "method"));
            cfg.setTimeoutMs(intOrDefault(in, "timeoutMs", 8000));
            cfg.setExpectJsonField(stringOrNull(in, "expectJsonField"));
            cfg.setExpectJsonValue(stringOrNull(in, "expectJsonValue"));
            cfg.setWidgetId(stringOrNull(in, "widgetId"));

            // New optional request-shaping fields for synthetic probe
            cfg.setRequestOrigin(stringOrNull(in, "requestOrigin"));
            cfg.setRequestReferer(stringOrNull(in, "requestReferer"));
            cfg.setRequestUserAgent(stringOrNull(in, "requestUserAgent"));
            cfg.setRequestCookie(stringOrNull(in, "requestCookie"));

            HttpSession session = req.getSession(false);
            String updatedBy = session != null && session.getAttribute("user") != null
                    ? String.valueOf(session.getAttribute("user"))
                    : "unknown";
            cfg.setUpdatedBy(updatedBy);
            cfg.setUpdatedAt(Instant.now());

            WidgetHealthConfig saved = store.save(cfg);
            writeJson(resp, HttpServletResponse.SC_OK, toJson(saved).toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to save widget health config", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, errorJson("Unable to save widget health config."));
        }
    }

    private void ensureStore() throws Exception {
        if (store == null) {
            store = new WidgetHealthConfigStore(dsHolder.getDataSource());
            store.ensureTable();
            store.ensureDefaultRow();
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, errorJson("Authentication required."));
            return false;
        }
        Object role = session.getAttribute("role");
        if (role == null || !"ADMIN".equalsIgnoreCase(String.valueOf(role))) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, errorJson("Admin role required."));
            return false;
        }
        return true;
    }

    private JsonObject toJson(WidgetHealthConfig cfg) {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("id", cfg == null ? 1 : cfg.getId())
                .add("healthcheckUrl", safe(cfg == null ? null : cfg.getHealthcheckUrl()))
                .add("method", safe(cfg == null ? null : cfg.getMethod()))
                .add("timeoutMs", cfg == null ? 8000 : Math.max(1, cfg.getTimeoutMs()))
                .add("expectJsonField", safe(cfg == null ? null : cfg.getExpectJsonField()))
                .add("expectJsonValue", safe(cfg == null ? null : cfg.getExpectJsonValue()))
                .add("widgetId", safe(cfg == null ? null : cfg.getWidgetId()))
                // New optional request-shaping fields
                .add("requestOrigin", safe(cfg == null ? null : cfg.getRequestOrigin()))
                .add("requestReferer", safe(cfg == null ? null : cfg.getRequestReferer()))
                .add("requestUserAgent", safe(cfg == null ? null : cfg.getRequestUserAgent()))
                .add("requestCookie", safe(cfg == null ? null : cfg.getRequestCookie()))
                .add("updatedBy", safe(cfg == null ? null : cfg.getUpdatedBy()))
                .add("updatedAt", cfg == null || cfg.getUpdatedAt() == null ? "" : cfg.getUpdatedAt().toString());

        return b.build();
    }

    private JsonObject parseJson(String body) {
        if (body == null || body.isBlank()) {
            return Json.createObjectBuilder().build();
        }
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        }
    }

    private String stringOrNull(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.containsKey(key) || obj.isNull(key)) {
            return null;
        }
        String val;
        try {
            val = obj.getString(key, null);
        } catch (Exception e) {
            val = obj.get(key).toString();
            if (val != null && val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                val = val.substring(1, val.length() - 1);
            }
        }
        if (val == null) {
            return null;
        }
        String t = val.trim();
        return t.isEmpty() ? null : t;
    }

    private int intOrDefault(JsonObject obj, String key, int fallback) {
        if (obj == null || key == null || !obj.containsKey(key) || obj.isNull(key)) {
            return fallback;
        }
        try {
            return obj.getInt(key);
        } catch (Exception e) {
            try {
                return Integer.parseInt(obj.get(key).toString().replace("\"", "").trim());
            } catch (Exception ex) {
                return fallback;
            }
        }
    }

    private String errorJson(String message) {
        return Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "Request failed." : message)
                .build()
                .toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void writeJson(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body == null ? "{}" : body);
    }
}
