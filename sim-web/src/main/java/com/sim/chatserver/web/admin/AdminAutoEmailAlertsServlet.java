package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.admin.AutoEmailAlertConfigStore.AutoEmailAlertConfig;
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
import java.io.Reader;
import java.io.StringReader;
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
    private static final Object INIT_LOCK = new Object();
    private static final String STORE_ATTR = AdminAutoEmailAlertsServlet.class.getName() + ".store";
    private static final String SCHEDULER_ATTR = AdminAutoEmailAlertsServlet.class.getName() + ".scheduler";

    @Override
    public void init() throws ServletException {
        super.init();
        synchronized (INIT_LOCK) {
            if (lookupConfigStore() != null && lookupScheduler() != null) {
                return;
            }
            try {
                initializeInfrastructure();
            } catch (IllegalStateException e) {
                throw new ServletException("Unable to initialize automatic email alerts.", e);
            }
        }
    }

    @Override
    public void destroy() {
        synchronized (INIT_LOCK) {
            AutoEmailAlertScheduler configuredScheduler = lookupScheduler();
            if (configuredScheduler != null) {
                configuredScheduler.stop();
            }
            getServletContext().removeAttribute(SCHEDULER_ATTR);
            getServletContext().removeAttribute(STORE_ATTR);
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            AutoEmailAlertConfig cfg = configStore().load();
            writeJson(resp, HttpServletResponse.SC_OK, toResponseJson(cfg));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to load automatic email alert config.", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load automatic email alert config.");
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Automatic email alert config store is not initialized.", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Automatic email alert configuration is not initialized.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }

        if (!isValidJsonRequest(req)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        JsonObject payload;
        try {
            String requestBody = readRequestBody(req);
            if (requestBody == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
                return;
            }
            try (JsonReader reader = Json.createReader(new StringReader(requestBody))) {
                payload = reader.readObject();
            }
        } catch (JsonException | ClassCastException e) {
            log.log(Level.FINE, "Invalid automatic alert payload.", e);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        try {
            AutoEmailAlertConfig incoming = fromPayload(payload);
            if (payload.getBoolean("sendTestEmail", false)) {
                AutoEmailAlertScheduler.TestEmailResult result = scheduler().sendHealthTestEmail(incoming);
                int status = result.sent() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST;
                JsonObject body = Json.createObjectBuilder()
                        .add("status", result.sent() ? "ok" : "error")
                        .add("message", safe(result.message()))
                        .build();
                writeJson(resp, status, body);
                return;
            }

            String updatedBy = currentUser(req);
            AutoEmailAlertConfig saved = configStore().saveConfig(incoming, updatedBy);
            writeJson(resp, HttpServletResponse.SC_OK, toResponseJson(saved));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to save automatic email alert config.", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save automatic email alert config.");
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Automatic email alert config store is not initialized.", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Automatic email alert configuration is not initialized.");
        }
    }

    private void initializeInfrastructure() {
        try {
            AppDataSourceHolder dsHolder = dataSourceHolder();
            WidgetAvailabilityChecker availabilityChecker = availabilityChecker();
            TermsStore termsStore = termsStore();
            DbEmailConfigProvider dbEmailConfigProvider = dbEmailConfigProvider();

            AutoEmailAlertConfigStore initializedStore = new AutoEmailAlertConfigStore(dsHolder.getDataSource());
            initializedStore.ensureTable();
            initializedStore.ensureDefaultRow();

            AutoEmailAlertScheduler initializedScheduler = new AutoEmailAlertScheduler(
                    initializedStore,
                    dsHolder.getDataSource(),
                    availabilityChecker,
                    termsStore,
                    dbEmailConfigProvider
            );
            initializedScheduler.start();

            getServletContext().setAttribute(STORE_ATTR, initializedStore);
            getServletContext().setAttribute(SCHEDULER_ATTR, initializedScheduler);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.SEVERE, "Failed to initialize automatic email alert infrastructure.", e);
            throw new IllegalStateException("Unable to initialize automatic email alerts.", e);
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
        cfg.setHealthRunbookUrl(stringVal(payload, "healthRunbookUrl"));
        cfg.setHealthRunbookAttachmentPath(stringVal(payload, "healthRunbookAttachmentPath"));

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
                .add("healthRunbookUrl", safe(cfg.getHealthRunbookUrl()))
                .add("healthRunbookAttachmentPath", safe(cfg.getHealthRunbookAttachmentPath()))

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
                .add("healthLastRestartAttemptAt", formatInstant(cfg.getHealthLastRestartAttemptAt()))

                .add("termLastCheckedAt", formatInstant(cfg.getTermLastCheckedAt()))
                .add("termLastCount", Math.max(0L, cfg.getTermLastCount()))
                .add("termLastAlertAt", formatInstant(cfg.getTermLastAlertAt()))

                .add("updatedBy", safe(cfg.getUpdatedBy()))
                .add("updatedAt", formatInstant(cfg.getUpdatedAt()));

        return json.build();
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
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
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private String readRequestBody(HttpServletRequest req) {
        if (req == null || !isValidJsonRequest(req)) {
            return null;
        }

        try {
            Reader requestReader = req.getReader();
            if (requestReader != null) {
                try (Reader reader = requestReader) {
                    String body = ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES, 4096);
                    return validateTaintedText(body);
                }
            }
        } catch (IOException | IllegalStateException e) {
            log.log(Level.FINE, "Unable to read automatic alert payload from request reader.", e);
        }

        return null;
    }

    private String validateTaintedText(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String normalizedInput = ServletRequestParamUtil.normalizeBodyText(value, MAX_JSON_PAYLOAD_BYTES, false);
        if (normalizedInput.isEmpty()) {
            return "";
        }
        StringBuilder safe = new StringBuilder(normalizedInput.length());
        for (int i = 0; i < normalizedInput.length(); i++) {
            char ch = normalizedInput.charAt(i);
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        String normalized = safe.toString();
        return normalized.length() > MAX_JSON_PAYLOAD_BYTES
                ? normalized.substring(0, MAX_JSON_PAYLOAD_BYTES)
                : normalized;
    }

    private AutoEmailAlertConfigStore configStore() {
        AutoEmailAlertConfigStore configuredStore = lookupConfigStore();
        if (configuredStore == null) {
            throw new IllegalStateException("Automatic email alert config store is not initialized.");
        }
        return configuredStore;
    }

    private AutoEmailAlertScheduler scheduler() {
        AutoEmailAlertScheduler configuredScheduler = lookupScheduler();
        if (configuredScheduler == null) {
            throw new IllegalStateException("Automatic email alert scheduler is not initialized.");
        }
        return configuredScheduler;
    }

    private AutoEmailAlertConfigStore lookupConfigStore() {
        Object value = getServletContext().getAttribute(STORE_ATTR);
        if (value instanceof AutoEmailAlertConfigStore configuredStore) {
            return configuredStore;
        }
        return null;
    }

    private AutoEmailAlertScheduler lookupScheduler() {
        Object value = getServletContext().getAttribute(SCHEDULER_ATTR);
        if (value instanceof AutoEmailAlertScheduler configuredScheduler) {
            return configuredScheduler;
        }
        return null;
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private WidgetAvailabilityChecker availabilityChecker() {
        return CDI.current().select(WidgetAvailabilityChecker.class).get();
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

    private DbEmailConfigProvider dbEmailConfigProvider() {
        return CDI.current().select(DbEmailConfigProvider.class).get();
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
            if (raw != null && raw.length() >= 2 && raw.charAt(0) == '"' && raw.charAt(raw.length() - 1) == '"') {
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
        return Math.toIntExact(seconds);
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

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, safe(message));
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write automatic-email error response", e);
            sendErrorSafe(resp, status, safe(message));
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write automatic-email JSON response", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, safe(message));
        } catch (IOException ioe) {
            log.log(Level.FINE, "Fallback sendError failed", ioe);
        }
    }
}
