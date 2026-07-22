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

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSummaryMarkdownServlet", urlPatterns = {"/dashboard/summary-markdown"})
public class DashboardSummaryMarkdownServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardSummaryMarkdownServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Object SUMMARY_STORE_LOCK = new Object();
    private static volatile DashboardDailySummaryStore SUMMARY_STORE;

    // renamed file path
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_summary_markdown.html";

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            summaryStore();
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to initialize DashboardDailySummaryStore", e);
            throw new ServletException("Failed to initialize dashboard summary markdown servlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        ZoneId zone = ZoneId.systemDefault();
        RequestContext context = RequestContext.from(req);
        LocalDate day = parseDay(context.first("day"), zone);
        int slot = parseSlotOrCurrent(context.first("slot"), zone);

        try {
            JsonObject payload = summaryStore().fetchExactOrLatest(day, slot);

            String user = String.valueOf(session.getAttribute("user"));
            String markdown = buildMarkdown(payload);
            String previewHtml = buildPreviewHtml(payload);

            JsonObject summary = payload == null ? null : payload.getJsonObject("summary");
            JsonObject meta = payload == null ? null : payload.getJsonObject("meta");

            String metaDay = readJsonString(meta, "day", day.toString());
            String metaSlot = String.valueOf(readJsonInt(meta, "slot", slot));
            String metaGeneratedAt = readJsonString(meta, "generatedAt", "—");
            String statusText = readJsonString(meta, "statusText", "idle");
            String statusClass = cssStatus(statusText);

            int entryCount = readJsonInt(summary, "entryCount", 0);
            int progressPct = readJsonInt(meta, "progressPct", 0);
            String metaUpdatedAt = readJsonString(meta, "updatedAt", "—");
            String metaMessage = readJsonString(meta, "message", "");
            String suggestedNextAction = suggestNextAction(payload);

            String template = DashboardTemplateRenderer.loadTemplateCached(req.getServletContext(), TEMPLATE_PATH);

            Map<String, String> vars = new LinkedHashMap<>();
            vars.put("contextPath", req.getContextPath());
            vars.put("user", DashboardTemplateRenderer.escapeHtml(user));

            vars.put("metaDay", DashboardTemplateRenderer.escapeHtml(metaDay));
            vars.put("metaSlot", DashboardTemplateRenderer.escapeHtml(metaSlot));
            vars.put("metaGeneratedAt", DashboardTemplateRenderer.escapeHtml(metaGeneratedAt));
            vars.put("metaUpdatedAt", DashboardTemplateRenderer.escapeHtml(metaUpdatedAt));
            vars.put("statusText", DashboardTemplateRenderer.escapeHtml(statusText));
            vars.put("statusClass", DashboardTemplateRenderer.escapeHtml(statusClass));
            vars.put("entryCount", DashboardTemplateRenderer.escapeHtml(String.valueOf(Math.max(0, entryCount))));
            vars.put("progressPct", DashboardTemplateRenderer.escapeHtml(String.valueOf(progressPct)));
            vars.put("metaMessage", DashboardTemplateRenderer.escapeHtml(metaMessage.isBlank() ? "—" : metaMessage));

            vars.put("previewHtml", previewHtml);
            vars.put("suggestedNextAction", DashboardTemplateRenderer.escapeHtml(suggestedNextAction));

            // hidden input value for copy button
            vars.put("markdownSourceAttr", escapeHtmlAttribute(markdown));

            String rendered = DashboardTemplateRenderer.renderTemplate(template, vars);

            resp.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                out.print(rendered);
            }

        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to render summary markdown page", e);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Unable to load summary markdown page.");
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
        JsonObject summary = payload == null ? null : payload.getJsonObject("summary");
        JsonObject meta = payload == null ? null : payload.getJsonObject("meta");

        String overall = readJsonString(summary, "overall", "—");
        String quality = readJsonString(summary, "quality", "—");
        String response = readJsonString(summary, "response", "—");
        String usage = readJsonString(summary, "usage", "—");
        int entryCount = readJsonInt(summary, "entryCount", 0);

        String day = readJsonString(meta, "day", "");
        int slot = readJsonInt(meta, "slot", 0);
        String generatedAt = readJsonString(meta, "generatedAt", "");
        String startedAt = readJsonString(meta, "startedAt", "");
        String updatedAt = readJsonString(meta, "updatedAt", "");
        String statusText = readJsonString(meta, "statusText", "idle");
        int progressPct = readJsonInt(meta, "progressPct", 0);
        String message = readJsonString(meta, "message", "");
        String nextAction = suggestNextAction(payload);

        StringBuilder md = new StringBuilder(1200);
        md.append("# Daily Dashboard Summary\n\n");
        md.append("- **Day:** ").append(blankDash(day)).append('\n');
        md.append("- **Slot:** ").append(slot).append('\n');
        md.append("- **Status:** ").append(blankDash(statusText)).append('\n');
        md.append("- **Progress:** ").append(progressPct).append("%\n");
        md.append("- **Entries analyzed:** ").append(Math.max(0, entryCount)).append('\n');
        md.append("- **Generated at:** ").append(blankDash(generatedAt)).append('\n');
        md.append("- **Started at:** ").append(blankDash(startedAt)).append('\n');
        md.append("- **Updated at:** ").append(blankDash(updatedAt)).append('\n');
        if (!message.isBlank()) {
            md.append("- **Message:** ").append(message).append('\n');
        }

        md.append("\n## Overall\n").append(safeBlock(overall)).append('\n');
        md.append("\n## Quality\n").append(safeBlock(quality)).append('\n');
        md.append("\n## Response\n").append(safeBlock(response)).append('\n');
        md.append("\n## Usage\n").append(safeBlock(usage)).append('\n');
        md.append("\n## Suggested Next Action\n").append(safeBlock(nextAction)).append('\n');

        return md.toString();
    }

    private String buildPreviewHtml(JsonObject payload) {
        JsonObject summary = payload == null ? null : payload.getJsonObject("summary");
        JsonObject meta = payload == null ? null : payload.getJsonObject("meta");

        String overall = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "overall", "—"));
        String quality = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "quality", "—"));
        String response = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "response", "—"));
        String usage = DashboardTemplateRenderer.escapeHtml(readJsonString(summary, "usage", "—"));

        String day = DashboardTemplateRenderer.escapeHtml(readJsonString(meta, "day", "—"));
        String slot = DashboardTemplateRenderer.escapeHtml(String.valueOf(readJsonInt(meta, "slot", 0)));
        String generatedAt = DashboardTemplateRenderer.escapeHtml(readJsonString(meta, "generatedAt", "—"));
        String statusText = DashboardTemplateRenderer.escapeHtml(readJsonString(meta, "statusText", "idle"));
        int progressPct = readJsonInt(meta, "progressPct", 0);
        int entryCount = readJsonInt(summary, "entryCount", 0);

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
        JsonObject summary = payload == null ? null : payload.getJsonObject("summary");
        JsonObject meta = payload == null ? null : payload.getJsonObject("meta");

        String status = readJsonString(meta, "statusText", "idle").toLowerCase();
        String quality = readJsonString(summary, "quality", "").toLowerCase();
        String response = readJsonString(summary, "response", "").toLowerCase();
        String usage = readJsonString(summary, "usage", "").toLowerCase();

        if ("running".equals(status) || "queued".equals(status)) {
            return "Summary is still generating. Wait for completion, then review low-performing areas and rerun checks.";
        }

        if (containsAny(quality, "low", "inconsistent", "hallucination", "incorrect", "poor")) {
            return "Review low-quality conversations first and tighten prompt instructions/guardrails for the affected widgets.";
        }

        if (containsAny(response, "slow", "latency", "timeout", "delayed")) {
            return "Investigate response latency by widget and reduce prompt/context size where possible to improve speed.";
        }

        if (containsAny(usage, "low", "drop", "decline", "underused")) {
            return "Promote underused high-value widgets and add clearer in-app guidance so users discover the right tools faster.";
        }

        return "Review Top Terms and Latest Chats to identify one repeated issue, then apply a focused prompt update and monitor tomorrow’s trend.";
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
        if ("error".equals(s)) {
            return "error";
        }
        if ("running".equals(s) || "queued".equals(s)) {
            return "running";
        }
        return "";
    }

    private String blankDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String safeBlock(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
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
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private DashboardDailySummaryStore summaryStore() {
        DashboardDailySummaryStore store = SUMMARY_STORE;
        if (store != null) {
            return store;
        }
        synchronized (SUMMARY_STORE_LOCK) {
            store = SUMMARY_STORE;
            if (store == null) {
                DashboardDailySummaryStore created = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
                created.ensureTable();
                SUMMARY_STORE = created;
                store = created;
            }
        }
        return store;
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    static final class RequestContext {

        private final HttpServletRequest request;

        private RequestContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestContext from(HttpServletRequest req) {
            return new RequestContext(req);
        }

        private String first(String name) {
            if (name == null || name.isBlank() || request == null) {
                return null;
            }
            String[] values = request.getParameterValues(name);
            if (values == null || values.length == 0 || values[0] == null) {
                return null;
            }
            String trimmed = values[0].replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed.length() > 128 ? trimmed.substring(0, 128) : trimmed;
        }
    }

    private String escapeHtmlAttribute(String s) {
        return DashboardTemplateRenderer.escapeHtml(s == null ? "" : s);
    }
}
