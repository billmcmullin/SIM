package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.admin.AutoEmailAlertConfigStore.AutoEmailAlertConfig;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET/POST admin endpoint for automatic email alert configuration.
 */
public class AdminAutoEmailAlertsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminAutoEmailAlertsServlet.class.getName());

    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;

    @Inject
    AppDataSourceHolder dsHolder;

    @Inject
    WidgetAvailabilityChecker availabilityChecker;

    @Inject
    TermsStore termsStore;

    @Inject
    DbEmailConfigProvider dbEmailConfigProvider;

    private transient AutoEmailAlertConfigStore store;
    private transient AutoEmailAlertScheduler scheduler;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            store = new AutoEmailAlertConfigStore(dsHolder.getDataSource());
            store.ensureTable();
            store.ensureDefaultRow();

            scheduler = new AutoEmailAlertScheduler(
                    store,
                    dsHolder.getDataSource(),
                    availabilityChecker,
                    termsStore,
                    dbEmailConfigProvider
            );
            scheduler.start();
        } catch (SQLException | RuntimeException e) {
            log.log(Level.SEVERE, "Failed to initialize automatic email alert infrastructure.", e);
            throw new ServletException("Unable to initialize automatic email alerts.", e);
        }
    }

    @Override
    public void destroy() {
        if (scheduler != null) {
            scheduler.stop();
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            AutoEmailAlertConfig cfg = store.load();
            writeJson(resp, HttpServletResponse.SC_OK, toResponseJson(cfg));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to load automatic email alert config.", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load automatic email alert config.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        if (!isValidJsonRequest(req)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        JsonObject payload;
        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (JsonException | ClassCastException e) {
            log.log(Level.FINE, "Invalid automatic alert payload.", e);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        try {
            AutoEmailAlertConfig incoming = fromPayload(payload);
            String updatedBy = currentUser(req);
            AutoEmailAlertConfig saved = store.saveConfig(incoming, updatedBy);
            writeJson(resp, HttpServletResponse.SC_OK, toResponseJson(saved));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to save automatic email alert config.", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save automatic email alert config.");
        }
    }

    private AutoEmailAlertConfig fromPayload(JsonObject payload) {
        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();

        cfg.setHealthEnabled(payload.getBoolean("healthEnabled", false));
        cfg.setHealthCheckIntervalSeconds(minutesToSeconds(intVal(payload, "healthCheckIntervalMinutes", 5)));
        cfg.setHealthOfflineDelaySeconds(minutesToSeconds(intVal(payload, "healthOfflineDelayMinutes", 5)));
        cfg.setHealthResendIntervalSeconds(minutesToSeconds(intVal(payload, "healthResendIntervalMinutes", 30)));
        cfg.setHealthRecipients(stringVal(payload, "healthRecipients"));
        cfg.setHealthSubject(stringVal(payload, "healthSubject"));
        cfg.setHealthMessage(stringVal(payload, "healthMessage"));

        cfg.setTermEnabled(payload.getBoolean("termEnabled", false));
        cfg.setTermCheckIntervalSeconds(minutesToSeconds(intVal(payload, "termCheckIntervalMinutes", 10)));
        cfg.setTermName(stringVal(payload, "termName"));
        cfg.setTermRecipients(stringVal(payload, "termRecipients"));
        cfg.setTermSubject(stringVal(payload, "termSubject"));
        cfg.setTermMessage(stringVal(payload, "termMessage"));

        return cfg;
    }

    private JsonObject toResponseJson(AutoEmailAlertConfig cfg) {
        if (cfg == null) {
            return Json.createObjectBuilder()
                    .add("status", "ok")
                    .build();
        }

        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("status", "ok")
                .add("healthEnabled", cfg.isHealthEnabled())
                .add("healthCheckIntervalMinutes", secondsToMinutes(cfg.getHealthCheckIntervalSeconds()))
                .add("healthOfflineDelayMinutes", secondsToMinutes(cfg.getHealthOfflineDelaySeconds()))
                .add("healthResendIntervalMinutes", secondsToMinutes(cfg.getHealthResendIntervalSeconds()))
                .add("healthRecipients", safe(cfg.getHealthRecipients()))
                .add("healthSubject", safe(cfg.getHealthSubject()))
                .add("healthMessage", safe(cfg.getHealthMessage()))

                .add("termEnabled", cfg.isTermEnabled())
                .add("termCheckIntervalMinutes", secondsToMinutes(cfg.getTermCheckIntervalSeconds()))
                .add("termName", safe(cfg.getTermName()))
                .add("termRecipients", safe(cfg.getTermRecipients()))
                .add("termSubject", safe(cfg.getTermSubject()))
                .add("termMessage", safe(cfg.getTermMessage()))

                .add("healthLastStatus", safe(cfg.getHealthLastStatus()))
                .add("healthLastCheckedAt", formatInstant(cfg.getHealthLastCheckedAt()))
                .add("healthOfflineSince", formatInstant(cfg.getHealthOfflineSince()))
                .add("healthLastAlertAt", formatInstant(cfg.getHealthLastAlertAt()))

                .add("termLastCheckedAt", formatInstant(cfg.getTermLastCheckedAt()))
                .add("termLastCount", Math.max(0L, cfg.getTermLastCount()))
                .add("termLastAlertAt", formatInstant(cfg.getTermLastAlertAt()))

                .add("updatedBy", safe(cfg.getUpdatedBy()))
                .add("updatedAt", formatInstant(cfg.getUpdatedAt()));

        return json.build();
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : String.valueOf(session.getAttribute("role"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    private String currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "UNKNOWN";
        }
        return String.valueOf(session.getAttribute("user"));
    }

    private boolean isValidJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
            return false;
        }
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private int intVal(JsonObject payload, String key, int fallback) {
        if (payload == null || key == null || !payload.containsKey(key) || payload.isNull(key)) {
            return fallback;
        }
        try {
            return payload.getInt(key);
        } catch (ClassCastException e) {
            log.log(Level.FINE, "Non-integer JSON value for key: {0}", key);
            try {
                return Integer.parseInt(payload.get(key).toString().replace("\"", "").trim());
            } catch (NumberFormatException ex) {
                log.log(Level.FINE, "Unable to parse integer value for key: {0}", key);
                return fallback;
            }
        }
    }

    private String stringVal(JsonObject payload, String key) {
        if (payload == null || key == null || !payload.containsKey(key) || payload.isNull(key)) {
            return null;
        }
        try {
            String value = payload.getString(key, null);
            return normalizeText(value);
        } catch (ClassCastException e) {
            log.log(Level.FINE, "Non-string JSON value for key: {0}", key);
            String raw = payload.get(key).toString();
            if (raw != null && raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
                raw = raw.substring(1, raw.length() - 1);
            }
            return normalizeText(raw);
        }
    }

    private int minutesToSeconds(int minutes) {
        int m = Math.max(0, minutes);
        long seconds = (long) m * 60L;
        if (seconds > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) seconds;
    }

    private int secondsToMinutes(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        int minutes = safeSeconds / 60;
        return Math.max(1, minutes);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('\u0000', ' ').replace("\r", "").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject body = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", safe(message))
                .build();
        writeJson(resp, status, body);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(body == null ? Json.createObjectBuilder().build() : body);
        }
    }
}
