package com.sim.chatserver.web.admin;

import com.sim.chatserver.service.widget.WidgetHealthConfigStore;
import com.sim.chatserver.service.widget.WidgetHealthConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonException;
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
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /admin/widget-health-config POST /admin/widget-health-config
 */
public class WidgetHealthConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetHealthConfigServlet.class.getCanonicalName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final String STORE_ATTRIBUTE_KEY = WidgetHealthConfigStore.class.getCanonicalName();

    @Override
    public void init() throws ServletException {
        super.init();
        WidgetHealthConfigStore initializedStore = ensureStoreInitialized();
        if (initializedStore == null) {
            throw new ServletException("Failed to initialize widget health config store");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            WidgetHealthConfigStore currentStore = ensureStoreInitialized();

            WidgetHealthConfig cfg = currentStore.load();
            if (cfg == null) {
                currentStore.ensureDefaultRow();
                cfg = currentStore.load();
            }

            writeJson(resp, HttpServletResponse.SC_OK, toJson(cfg));
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load widget health config", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorJson("Unable to load widget health config."));
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            WidgetHealthConfigStore currentStore = ensureStoreInitialized();

            if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, errorJson(com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD));
                return;
            }

            String body = readRequestBody(req);
            if (body == null) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, errorJson(com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD));
                return;
            }
            JsonObject in = parseJson(body);

            WidgetHealthConfig cfg = new WidgetHealthConfig();
            cfg.setHealthcheckUrl(stringOrNull(in, "healthcheckUrl"));
            cfg.setHealthcheckEnabled(boolOrDefault(in, "healthcheckEnabled", true));
            cfg.setCheckIntervalSeconds(minutesToSeconds(intOrDefault(in, "checkIntervalMinutes", 5), 5));
            cfg.setMethod(stringOrNull(in, "method"));
            cfg.setTimeoutMs(intOrDefault(in, "timeoutMs", 8000));
            cfg.setExpectJsonField(stringOrNull(in, "expectJsonField"));
            cfg.setExpectJsonValue(stringOrNull(in, "expectJsonValue"));
            cfg.setWidgetId(stringOrNull(in, com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID));

            // New optional request-shaping fields for synthetic probe
            cfg.setRequestOrigin(stringOrNull(in, "requestOrigin"));
            cfg.setRequestReferer(stringOrNull(in, "requestReferer"));
            cfg.setRequestUserAgent(stringOrNull(in, "requestUserAgent"));
            cfg.setRequestCookie(stringOrNull(in, "requestCookie"));

            // Optional API key header configuration
            cfg.setApiKeyHeaderName(stringOrNull(in, "apiKeyHeaderName"));
            cfg.setApiKeyValue(stringOrNull(in, "apiKeyValue"));

            WidgetHealthConfig existing = currentStore.load();

            if (cfg.getApiKeyHeaderName() == null && existing != null && existing.getApiKeyHeaderName() != null) {
                cfg.setApiKeyHeaderName(existing.getApiKeyHeaderName());
            }

            if (cfg.getApiKeyValue() == null) {
                if (existing != null) {
                    cfg.setApiKeyValue(existing.getApiKeyValue());
                }
            }

            if (cfg.getRequestCookie() == null) {
                if (existing != null) {
                    cfg.setRequestCookie(existing.getRequestCookie());
                }
            }

            HttpSession session = req.getSession(false);
            String updatedBy = session != null && session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) != null
                    ? String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER))
                    : com.sim.chatserver.util.CommonLiteralConstants.VALUE_UNKNOWN_LOWER;
            cfg.setUpdatedBy(updatedBy);
            cfg.setUpdatedAt(Instant.now());

            WidgetHealthConfig saved = currentStore.save(cfg);
            writeJson(resp, HttpServletResponse.SC_OK, toJson(saved));

        } catch (SQLException | IllegalArgumentException | IllegalStateException | JsonException e) {
            log.log(Level.WARNING, "Unable to save widget health config", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, errorJson("Unable to save widget health config."));
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
        }
    }

    private WidgetHealthConfigStore ensureStoreInitialized() {
        jakarta.servlet.ServletContext context = getServletContext();
        WidgetHealthConfigStore local = (WidgetHealthConfigStore) context.getAttribute(STORE_ATTRIBUTE_KEY);
        if (local != null) {
            return local;
        }

        synchronized (context) {
            local = (WidgetHealthConfigStore) context.getAttribute(STORE_ATTRIBUTE_KEY);
            if (local != null) {
                return local;
            }

            return createStore(context);
        }
    }

    private WidgetHealthConfigStore createStore(jakarta.servlet.ServletContext context) {
        try {
            WidgetHealthConfigStore created = new WidgetHealthConfigStore(dataSourceHolder().getDataSource());
            created.ensureTable();
            created.ensureDefaultRow();
            context.setAttribute(STORE_ATTRIBUTE_KEY, created);
            return created;
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to initialize WidgetHealthConfigStore", e);
            throw new IllegalStateException("Failed to initialize widget health config store", e);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, errorJson(com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED));
            return false;
        }
        Object role = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE);
        if (role == null || !com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN.equalsIgnoreCase(String.valueOf(role))) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, errorJson(com.sim.chatserver.util.CommonLiteralConstants.MSG_ADMIN_ROLE_REQUIRED));
            return false;
        }
        return true;
    }

    private JsonObject toJson(WidgetHealthConfig cfg) {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("id", cfg == null ? 1 : cfg.getId())
                .add("healthcheckUrl", safe(cfg == null ? null : cfg.getHealthcheckUrl()))
            .add("healthcheckEnabled", cfg == null || cfg.isHealthcheckEnabled())
            .add("checkIntervalMinutes", secondsToMinutes(cfg == null ? 300 : cfg.getCheckIntervalSeconds()))
                .add("method", safe(cfg == null ? null : cfg.getMethod()))
                .add("timeoutMs", cfg == null ? 8000 : Math.max(1, cfg.getTimeoutMs()))
                .add("expectJsonField", safe(cfg == null ? null : cfg.getExpectJsonField()))
                .add("expectJsonValue", safe(cfg == null ? null : cfg.getExpectJsonValue()))
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, safe(cfg == null ? null : cfg.getWidgetId()))
                // New optional request-shaping fields
                .add("requestOrigin", safe(cfg == null ? null : cfg.getRequestOrigin()))
                .add("requestReferer", safe(cfg == null ? null : cfg.getRequestReferer()))
                .add("requestUserAgent", safe(cfg == null ? null : cfg.getRequestUserAgent()))
                .add("requestCookieStored", cfg != null && cfg.getRequestCookie() != null)
                .add("apiKeyHeaderName", safe(cfg == null ? null : cfg.getApiKeyHeaderName()))
                .add("apiKeyStored", cfg != null && cfg.getApiKeyValue() != null)
                .add("updatedBy", safe(cfg == null ? null : cfg.getUpdatedBy()))
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_UPDATED_AT, cfg == null || cfg.getUpdatedAt() == null ? "" : cfg.getUpdatedAt().toString());

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
        } catch (ClassCastException e) {
            log.log(Level.FINE, "Unable to read JSON string key {0} directly; falling back to raw value", key);
            val = obj.get(key).toString();
            if (val != null && val.length() >= 2 && val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {
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
        } catch (ClassCastException e) {
            log.log(Level.FINE, "Unable to read JSON int key {0} directly; trying parse fallback", key);
            try {
                return Integer.parseInt(obj.get(key).toString().replace("\"", "").trim());
            } catch (NumberFormatException ex) {
                log.log(Level.FINE, "Unable to parse JSON int key {0}; using fallback", key);
                return fallback;
            }
        }
    }

    private boolean boolOrDefault(JsonObject obj, String key, boolean fallback) {
        if (obj == null || key == null || !obj.containsKey(key) || obj.isNull(key)) {
            return fallback;
        }
        try {
            return obj.getBoolean(key);
        } catch (ClassCastException e) {
            log.log(Level.FINE, "Unable to read JSON boolean key {0} directly; trying parse fallback", key);
            String raw = String.valueOf(obj.get(key)).replace("\"", "").trim();
            if (com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE.equalsIgnoreCase(raw) || "1".equals(raw) || com.sim.chatserver.util.CommonLiteralConstants.VALUE_YES.equalsIgnoreCase(raw)
                    || "on".equalsIgnoreCase(raw) || "y".equalsIgnoreCase(raw)) {
                return true;
            }
            if (com.sim.chatserver.util.CommonLiteralConstants.VALUE_FALSE.equalsIgnoreCase(raw) || "0".equals(raw) || "no".equalsIgnoreCase(raw)
                    || "off".equalsIgnoreCase(raw) || "n".equalsIgnoreCase(raw)) {
                return false;
            }
            return fallback;
        }
    }

    private int minutesToSeconds(int minutes, int fallbackMinutes) {
        int safeMinutes = minutes <= 0 ? fallbackMinutes : minutes;
        long seconds = (long) safeMinutes * 60L;
        if (seconds > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(seconds);
    }

    private int secondsToMinutes(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        int minutes = safeSeconds / 60;
        return Math.max(1, minutes);
    }

    private JsonObject errorJson(String message) {
        return Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message)
                .build();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String readRequestBody(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        try {
            BufferedReader reader = req.getReader();
            try {
                return ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES);
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to read widget health config request body", e);
            return null;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write widget health config JSON response", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}

