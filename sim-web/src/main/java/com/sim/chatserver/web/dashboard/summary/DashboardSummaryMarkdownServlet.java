package com.sim.chatserver.web.dashboard.summary;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardSummaryMarkdownServlet", urlPatterns = {"/dashboard/summary-markdown"})
public class DashboardSummaryMarkdownServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardSummaryMarkdownServlet.class.getCanonicalName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String SUMMARY_STORE_KEY = DashboardDailySummaryStore.class.getCanonicalName();
    private static final String LS = System.lineSeparator();

    // renamed file path
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_summary_markdown.html";

    @Override
    public void init() throws ServletException {
        super.init();
        DashboardDailySummaryStore initializedStore = summaryStore();
        if (initializedStore == null) {
            log.log(Level.SEVERE, "Unable to initialize DashboardDailySummaryStore");
            throw new ServletException("Failed to initialize dashboard summary markdown servlet");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
            return;
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDate day = parseDay(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, 128, true, true), zone);
        int slot = parseSlotOrCurrent(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_SLOT, 128, true, true), zone);

        try {
            JsonObject payload = summaryStore().fetchExactOrLatest(day, slot);

            String user = String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER));
            String markdown = buildMarkdown(payload);
            String previewHtml = buildPreviewHtml(payload);

            JsonObject summary = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.LABEL_SUMMARY);
            JsonObject meta = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.FIELD_META);

            String metaDay = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, day.toString());
            String metaSlot = String.valueOf(readJsonInt(meta, com.sim.chatserver.util.CommonLiteralConstants.KEY_SLOT, slot));
            String metaGeneratedAt = readJsonString(meta, DashboardDailySummaryStore.KEY_GENERATED_AT, com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA);
            String statusText = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_STATUS_TEXT, com.sim.chatserver.util.CommonLiteralConstants.STATUS_IDLE);
            String statusClass = cssStatus(statusText);

            int entryCount = readJsonInt(summary, com.sim.chatserver.util.CommonLiteralConstants.FIELD_ENTRY_COUNT, 0);
            int progressPct = readJsonInt(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROGRESS_PCT, 0);
            String metaUpdatedAt = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_UPDATED_AT, com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA);
            String metaMessage = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "");
            String suggestedNextAction = suggestNextAction(payload);

            String template = DashboardTemplateRenderer.loadTemplateCached(req.getServletContext(), TEMPLATE_PATH);

            Map<String, String> vars = new LinkedHashMap<>();
            vars.put("contextPath", req.getContextPath());
            vars.put(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER, DashboardTemplateRenderer.escapeHtml(user));

            vars.put("metaDay", DashboardTemplateRenderer.escapeHtml(metaDay));
            vars.put("metaSlot", DashboardTemplateRenderer.escapeHtml(metaSlot));
            vars.put("metaGeneratedAt", DashboardTemplateRenderer.escapeHtml(metaGeneratedAt));
            vars.put("metaUpdatedAt", DashboardTemplateRenderer.escapeHtml(metaUpdatedAt));
            vars.put(com.sim.chatserver.util.CommonLiteralConstants.FIELD_STATUS_TEXT, DashboardTemplateRenderer.escapeHtml(statusText));
            vars.put("statusClass", DashboardTemplateRenderer.escapeHtml(statusClass));
            vars.put(com.sim.chatserver.util.CommonLiteralConstants.FIELD_ENTRY_COUNT, DashboardTemplateRenderer.escapeHtml(String.valueOf(Math.max(0, entryCount))));
            vars.put(com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROGRESS_PCT, DashboardTemplateRenderer.escapeHtml(String.valueOf(progressPct)));
            vars.put("metaMessage", DashboardTemplateRenderer.escapeHtml(metaMessage.isBlank() ? com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA : metaMessage));

            vars.put("previewHtml", previewHtml);
            vars.put("suggestedNextAction", DashboardTemplateRenderer.escapeHtml(suggestedNextAction));

            // hidden input value for copy button
            vars.put("markdownSourceAttr", DashboardTemplateRenderer.escapeHtml(markdown == null ? "" : markdown));

            String rendered = DashboardTemplateRenderer.renderTemplate(template, vars);

            resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8);
            try (PrintWriter out = resp.getWriter()) {
                out.print(rendered);
            }

        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to render summary markdown page", e);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Unable to load summary markdown page.");
        }
    
        } catch (IOException | IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
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

    private LocalDate parseDay(String raw, ZoneId zone) {
        if (raw == null || raw.isBlank()) {
            return LocalDate.now(zone);
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (java.time.format.DateTimeParseException e) {
            log.log(Level.FINE, "Invalid dashboard summary day parameter: " + sanitizeForLog(raw), e);
            return LocalDate.now(zone);
        }
    }

    private int parseSlotOrCurrent(String raw, ZoneId zone) {
        if (raw != null && !raw.isBlank()) {
            try {
                int s = Integer.parseInt(raw.trim());
                if (s >= 0 && s <= 3) {
                    return s;
                }
            } catch (NumberFormatException e) {
                log.log(Level.FINE, "Invalid dashboard summary slot parameter: " + sanitizeForLog(raw), e);
            }
        }
        int hour = java.time.LocalTime.now(zone).getHour();
        if (hour < 6) {
            return 0;
        }
        if (hour < 12) {
            return 1;
        }
        if (hour < 18) {
            return 2;
        }
        return 3;
    }

    private String buildMarkdown(JsonObject payload) {
        JsonObject summary = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.LABEL_SUMMARY);
        JsonObject meta = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.FIELD_META);

        String overall = readJsonString(summary, "overall", com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA);
        String quality = readJsonString(summary, "quality", com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA);
        String response = readJsonString(summary, com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE, com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA);
        String usage = readJsonString(summary, "usage", com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA);
        int entryCount = readJsonInt(summary, com.sim.chatserver.util.CommonLiteralConstants.FIELD_ENTRY_COUNT, 0);

        String day = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, "");
        int slot = readJsonInt(meta, com.sim.chatserver.util.CommonLiteralConstants.KEY_SLOT, 0);
        String generatedAt = readJsonString(meta, DashboardDailySummaryStore.KEY_GENERATED_AT, "");
        String startedAt = readJsonString(meta, "startedAt", "");
        String updatedAt = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_UPDATED_AT, "");
        String statusText = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_STATUS_TEXT, com.sim.chatserver.util.CommonLiteralConstants.STATUS_IDLE);
        int progressPct = readJsonInt(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROGRESS_PCT, 0);
        String message = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "");
        String nextAction = suggestNextAction(payload);

        StringBuilder md = new StringBuilder(1200);
        md.append("# Daily Dashboard Summary").append(LS).append(LS);
        md.append("- **Day:** ").append(blankDash(day)).append(System.lineSeparator());
        md.append("- **Slot:** ").append(slot).append(System.lineSeparator());
        md.append("- **Status:** ").append(blankDash(statusText)).append(System.lineSeparator());
        md.append("- **Progress:** ").append(progressPct).append('%').append(System.lineSeparator());
        md.append("- **Entries analyzed:** ").append(Math.max(0, entryCount)).append(System.lineSeparator());
        md.append("- **Generated at:** ").append(blankDash(generatedAt)).append(System.lineSeparator());
        md.append("- **Started at:** ").append(blankDash(startedAt)).append(System.lineSeparator());
        md.append("- **Updated at:** ").append(blankDash(updatedAt)).append(System.lineSeparator());
        if (!message.isBlank()) {
            md.append("- **Message:** ").append(message).append(System.lineSeparator());
        }

        md.append(LS).append("## Overall").append(LS).append(safeBlock(overall)).append(System.lineSeparator());
        md.append(LS).append("## Quality").append(LS).append(safeBlock(quality)).append(System.lineSeparator());
        md.append(LS).append("## Response").append(LS).append(safeBlock(response)).append(System.lineSeparator());
        md.append(LS).append("## Usage").append(LS).append(safeBlock(usage)).append(System.lineSeparator());
        md.append(LS).append("## Suggested Next Action").append(LS).append(safeBlock(nextAction)).append(System.lineSeparator());

        return md.toString();
    }

    private String buildPreviewHtml(JsonObject payload) {
        JsonObject summary = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.LABEL_SUMMARY);
        JsonObject meta = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.FIELD_META);

        String overall = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "overall", com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA));
        String quality = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "quality", com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA));
        String response = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE, com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA));
        String usage = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "usage", com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA));

        String day = DashboardTemplateRenderer.escapeHtml(readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA));
        String slot = DashboardTemplateRenderer.escapeHtml(String.valueOf(readJsonInt(meta, com.sim.chatserver.util.CommonLiteralConstants.KEY_SLOT, 0)));
        String generatedAt = DashboardTemplateRenderer.escapeHtml(readJsonString(meta, DashboardDailySummaryStore.KEY_GENERATED_AT, com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA));
        String statusText = DashboardTemplateRenderer.escapeHtml(readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_STATUS_TEXT, com.sim.chatserver.util.CommonLiteralConstants.STATUS_IDLE));
        int progressPct = readJsonInt(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROGRESS_PCT, 0);
        int entryCount = readJsonInt(summary, com.sim.chatserver.util.CommonLiteralConstants.FIELD_ENTRY_COUNT, 0);

        String progressText = Integer.toString(progressPct);
        String entryCountText = Integer.toString(Math.max(0, entryCount));

        return """
                <div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:12px;">
                    <div><strong>Day</strong><div>%s</div></div>
                    <div><strong>Slot</strong><div>%s</div></div>
                    <div><strong>Status</strong><div>%s</div></div>
                    <div><strong>Progress</strong><div>%s%%</div></div>
                    <div><strong>Generated</strong><div>%s</div></div>
                    <div><strong>Entries</strong><div>%s</div></div>
                </div>
                <hr style="border:none; border-top:1px solid #e5e7eb; margin:12px 0;">
                <h4 style="margin:0 0 6px 0;">Overall</h4><p style="margin:0 0 10px 0; white-space:pre-wrap;">%s</p>
                <h4 style="margin:0 0 6px 0;">Quality</h4><p style="margin:0 0 10px 0; white-space:pre-wrap;">%s</p>
                <h4 style="margin:0 0 6px 0;">Response</h4><p style="margin:0 0 10px 0; white-space:pre-wrap;">%s</p>
                <h4 style="margin:0 0 6px 0;">Usage</h4><p style="margin:0; white-space:pre-wrap;">%s</p>
                """.formatted(day, slot, statusText, progressText, generatedAt, entryCountText, overall, quality, response, usage);
    }

    private String suggestNextAction(JsonObject payload) {
        JsonObject summary = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.LABEL_SUMMARY);
        JsonObject meta = payload == null ? null : payload.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.FIELD_META);

        String status = readJsonString(meta, com.sim.chatserver.util.CommonLiteralConstants.FIELD_STATUS_TEXT, com.sim.chatserver.util.CommonLiteralConstants.STATUS_IDLE).toLowerCase();
        String quality = readJsonString(summary, "quality", "").toLowerCase();
        String response = readJsonString(summary, com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE, "").toLowerCase();
        String usage = readJsonString(summary, "usage", "").toLowerCase();

        if (com.sim.chatserver.util.CommonLiteralConstants.STATUS_RUNNING.equals(status) || "queued".equals(status)) {
            return "Summary is still generating. Wait for completion, then review low-performing areas and rerun checks.";
        }

        if (containsAny(quality, com.sim.chatserver.util.CommonLiteralConstants.VALUE_LOW, "inconsistent", "hallucination", "incorrect", "poor")) {
            return "Review low-quality conversations first and tighten prompt instructions/guardrails for the affected widgets.";
        }

        if (containsAny(response, "slow", "latency", "timeout", "delayed")) {
            return "Investigate response latency by widget and reduce prompt/context size where possible to improve speed.";
        }

        if (containsAny(usage, com.sim.chatserver.util.CommonLiteralConstants.VALUE_LOW, "drop", "decline", "underused")) {
            return "Promote underused high-value widgets and add clearer in-app guidance so users discover the right tools faster.";
        }

        return "Review Top Terms and Latest Chats to identify one repeated issue, then apply a focused prompt update and monitor tomorrow's trend.";
    }

    private boolean containsAny(String text, String... terms) {
        if (text == null || text.isBlank() || terms == null) {
            return false;
        }
        for (String t : terms) {
            if (t != null && !t.isBlank() && text.contains(t.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String cssStatus(String status) {
        String s = status == null ? "" : status.trim().toLowerCase();
        if (com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR.equals(s)) {
            return com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
        }
        if (com.sim.chatserver.util.CommonLiteralConstants.STATUS_RUNNING.equals(s) || "queued".equals(s)) {
            return com.sim.chatserver.util.CommonLiteralConstants.STATUS_RUNNING;
        }
        return "";
    }

    private String blankDash(String s) {
        return (s == null || s.isBlank()) ? com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA : s;
    }

    private String safeBlock(String s) {
        return (s == null || s.isBlank()) ? com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA : s;
    }

    private String readJsonString(JsonObject obj, String key, String fallback) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return fallback;
        }
        JsonValue v = obj.get(key);
        if (v == null || v.getValueType() == JsonValue.ValueType.NULL) {
            return fallback;
        }
        try {
            if (obj.isNull(key)) {
                return fallback;
            }
            String s = obj.getString(key, fallback);
            return (s == null || s.isBlank()) ? fallback : s;
        } catch (ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, "Unable to read JSON string for key " + key, e);
            return fallback;
        }
    }

    private int readJsonInt(JsonObject obj, String key, int fallback) {
        if (obj == null || key == null || !obj.containsKey(key) || obj.isNull(key)) {
            return fallback;
        }
        try {
            return obj.getInt(key, fallback);
        } catch (ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, "Unable to read JSON integer for key " + key, e);
            return fallback;
        }
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace(System.lineSeparator().charAt(0), '_').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), '_');
        return normalized.length() > 120 ? new String(normalized.toCharArray(), 0, 120) : normalized;
    }

    private DashboardDailySummaryStore summaryStore() {
        jakarta.servlet.ServletContext context = getServletContext();
        DashboardDailySummaryStore store = (DashboardDailySummaryStore) context.getAttribute(SUMMARY_STORE_KEY);
        if (store != null) {
            return store;
        }
        synchronized (context) {
            store = (DashboardDailySummaryStore) context.getAttribute(SUMMARY_STORE_KEY);
            if (store == null) {
                DashboardDailySummaryStore created = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
                created.ensureTable();
                context.setAttribute(SUMMARY_STORE_KEY, created);
                store = created;
            }
        }
        return store;
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
