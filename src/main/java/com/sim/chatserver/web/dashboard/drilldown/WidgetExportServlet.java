package com.sim.chatserver.web.dashboard.drilldown;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionIdFormatter;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetExportServlet", urlPatterns = {"/dashboard/widgets/drilldown/export"})
public class WidgetExportServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetExportServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    private static final String DEFAULT_FORMAT = "csv";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        JsonObject payload;
        try (JsonReader jr = Json.createReader(req.getInputStream())) {
            payload = jr.readObject();
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body.");
            return;
        }

        String selectionId = payload.getString("selectionId", "").trim();
        if (selectionId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "selectionId required.");
            return;
        }

        List<String> selectedChatIds = new ArrayList<>();
        if (payload.containsKey("selectedChatIds")) {
            try {
                payload.getJsonArray("selectedChatIds")
                        .forEach(v -> selectedChatIds.add(v.toString().replace("\"", "").trim()));
            } catch (Exception ignored) {
            }
        }

        String format = payload.getString("format", DEFAULT_FORMAT).trim().toLowerCase(Locale.ROOT);
        if (format.isEmpty()) {
            format = DEFAULT_FORMAT;
        }
        if (!format.equals("csv") && !format.equals("json") && !format.equals("text") && !format.equals("pdf")) {
            format = DEFAULT_FORMAT;
        }

        // Optional synthesized analysis markdown (for evidence-style PDF)
        String reportMarkdown = payload.getString("reportMarkdown", "").trim();

        HttpSession session = req.getSession(false);
        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Selection not found.");
            return;
        }

        try {
            List<TermChatSnapshot> exportRows = resolveExportRows(selection, selectedChatIds);

            String extension = switch (format) {
                case "json" ->
                    "json";
                case "text" ->
                    "txt";
                case "pdf" ->
                    "pdf";
                default ->
                    "csv";
            };

            String filename = "chats-export-" + Instant.now().toString().replace(":", "-") + "." + extension;
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            String disposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded;

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setHeader("Content-Disposition", disposition);

            switch (format) {
                case "csv" ->
                    writeCsv(resp, exportRows);
                case "json" ->
                    writeJson(resp, exportRows);
                case "text" ->
                    writeText(resp, exportRows);
                case "pdf" ->
                    writePdf(resp, exportRows, selectionId, reportMarkdown);
                default ->
                    writeCsv(resp, exportRows);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Export failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Export failed: " + e.getMessage());
        }
    }

    private List<TermChatSnapshot> resolveExportRows(WidgetReviewStartServlet.Selection selection, List<String> selectedChatIds) throws SQLException {
        List<TermChatSnapshot> exportRows = new ArrayList<>();

        if (selection.hasSnapshots()) {
            if (selectedChatIds.isEmpty()) {
                exportRows.addAll(selection.snapshots);
            } else {
                for (TermChatSnapshot s : selection.snapshots) {
                    if (s.getChatId() != null && selectedChatIds.contains(s.getChatId())) {
                        exportRows.add(s);
                    }
                }
            }
            return exportRows;
        }

        if (selectedChatIds.isEmpty()) {
            return exportRows;
        }

        String widgetId = selection.widgetId;
        String tableName = sanitizeWidgetTableName(widgetId);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                return exportRows;
            }

            String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                    + quoteIdentifier(tableName)
                    + " WHERE widget_chat_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String chatId : selectedChatIds) {
                    ps.setString(1, chatId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String cid = rs.getString("widget_chat_id");
                            Timestamp created = rs.getTimestamp("created_at");
                            String prompt = rs.getString("prompt");
                            String responseText = rs.getString("response_text");
                            String sessionId = rs.getString("session_id");
                            exportRows.add(new TermChatSnapshot(
                                    sessionId == null ? "" : sessionId,
                                    widgetId,
                                    cid == null ? "" : cid,
                                    prompt,
                                    responseText,
                                    created,
                                    sessionId
                            ));
                        }
                    }
                }
            }
        }

        return exportRows;
    }

    private void writeCsv(HttpServletResponse resp, List<TermChatSnapshot> exportRows) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        try (OutputStream out = resp.getOutputStream()) {
            out.write("sessionId,sessionIdDisplay,createdAt,prompt,response\n".getBytes(StandardCharsets.UTF_8));
            for (TermChatSnapshot row : exportRows) {
                String sessionId = safe(row.getSessionId());
                String sessionDisplay = SessionIdFormatter.formatForDisplay(sessionId);
                String createdAt = row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString();
                String prompt = safe(row.getPrompt());
                String responseText = safe(row.getResponse());
                out.write(csvLine(new String[]{sessionId, sessionDisplay, createdAt, prompt, responseText}).getBytes(StandardCharsets.UTF_8));
                out.write('\n');
            }
            out.flush();
        }
    }

    private void writeJson(HttpServletResponse resp, List<TermChatSnapshot> exportRows) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        try (OutputStream out = resp.getOutputStream()) {
            JsonArrayBuilder ab = Json.createArrayBuilder();
            for (TermChatSnapshot row : exportRows) {
                String sid = safe(row.getSessionId());
                ab.add(Json.createObjectBuilder()
                        .add("sessionId", sid)
                        .add("sessionIdDisplay", SessionIdFormatter.formatForDisplay(sid))
                        .add("createdAt", row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString())
                        .add("prompt", safe(row.getPrompt()))
                        .add("response", safe(row.getResponse())));
            }
            Json.createWriter(out).writeArray(ab.build());
            out.flush();
        }
    }

    private void writeText(HttpServletResponse resp, List<TermChatSnapshot> exportRows) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        try (OutputStream out = resp.getOutputStream()) {
            for (TermChatSnapshot row : exportRows) {
                String sessionId = safe(row.getSessionId());
                String sessionDisplay = SessionIdFormatter.formatForDisplay(sessionId);
                String createdAt = row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString();
                out.write(("Session: " + sessionId + " (" + sessionDisplay + ")\n").getBytes(StandardCharsets.UTF_8));
                out.write(("Created At: " + createdAt + "\n").getBytes(StandardCharsets.UTF_8));
                out.write(("Prompt:\n" + safe(row.getPrompt()) + "\n\n").getBytes(StandardCharsets.UTF_8));
                out.write(("Response:\n" + safe(row.getResponse()) + "\n").getBytes(StandardCharsets.UTF_8));
                out.write(("----------------------------------------\n").getBytes(StandardCharsets.UTF_8));
            }
            out.flush();
        }
    }

    private void writePdf(HttpServletResponse resp, List<TermChatSnapshot> rows, String selectionId, String reportMarkdown) throws IOException {
        String html = (reportMarkdown != null && !reportMarkdown.isBlank())
                ? buildEvidenceReportHtmlFromMarkdown(reportMarkdown, rows, selectionId)
                : buildFallbackRowsHtml(rows, selectionId);

        try {
            byte[] pdfBytes = htmlToPdf(html);
            resp.setContentType("application/pdf");
            resp.setContentLength(pdfBytes.length);
            try (OutputStream out = resp.getOutputStream()) {
                out.write(pdfBytes);
                out.flush();
            }
        } catch (Throwable t) {
            log.log(Level.WARNING, "PDF renderer unavailable/failure; falling back to HTML export.", t);
            resp.setContentType("text/html; charset=UTF-8");
            try (OutputStream out = resp.getOutputStream()) {
                out.write(html.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
        }
    }

    private byte[] htmlToPdf(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        builder.run();
        return baos.toByteArray();
    }

    // -------- Evidence-style report rendering --------
    private String buildEvidenceReportHtmlFromMarkdown(String md, List<TermChatSnapshot> rows, String selectionId) {
        String exec = section(md, "Executive Chat Analysis");
        String metrics = section(md, "Key Metrics");
        String risks = section(md, "Risks and Opportunities");
        String recs = section(md, "Recommendations");
        String coverage = section(md, "Coverage and Methodology");

        String metricsTable = markdownTableToHtml(metrics);
        String risksHtml = markdownBulletsToHtml(risks);
        String recsHtml = markdownBulletsToHtml(recs);
        String coverageHtml = markdownBulletsToHtml(coverage);

        int totalChats = rows == null ? 0 : rows.size();
        String now = Instant.now().toString();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    @page { size: A4; margin: 1in; }
                    body { font-family: Arial, Helvetica, sans-serif; color: #1f2937; font-size: 12px; line-height: 1.45; }
                    .header { border-bottom: 2px solid #111827; margin-bottom: 14px; padding-bottom: 8px; }
                    .title { margin: 0; font-size: 22px; color: #0f172a; }
                    .meta { color: #475569; font-size: 11px; margin-top: 4px; }
                    .pill-row { margin-top: 10px; }
                    .pill { display:inline-block; border:1px solid #cbd5e1; border-radius:999px; padding:2px 10px; margin-right:6px; font-size:11px; color:#334155; background:#f8fafc; }
                    h2 { font-size: 16px; color: #0f172a; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px; margin-top: 18px; }
                    h3 { font-size: 13px; color: #1e293b; margin-top: 12px; }
                    p { margin: 8px 0; white-space: pre-wrap; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 8px; }
                    th, td { border: 1px solid #e2e8f0; padding: 7px; vertical-align: top; }
                    th { background: #f8fafc; text-align: left; }
                    ul { margin-top: 6px; }
                    li { margin: 5px 0; }
                    .appendix { margin-top: 18px; }
                    .small { color: #64748b; font-size: 10px; }
                  </style>
                </head>
                <body>
                  <div class="header">
                    <h1 class="title">Chat Analysis Evidence Report</h1>
                    <div class="meta">Selection ID: %s</div>
                    <div class="meta">Generated: %s</div>
                    <div class="pill-row">
                      <span class="pill">Evidence Artifact</span>
                      <span class="pill">Total Selected Chats: %d</span>
                    </div>
                  </div>

                  <h2>Executive Chat Analysis</h2>
                  <p>%s</p>

                  <h2>Key Metrics</h2>
                  %s

                  <h2>Risks and Opportunities</h2>
                  %s

                  <h2>Recommendations</h2>
                  %s

                  <h2>Coverage and Methodology</h2>
                  %s

                  <div class="appendix">
                    <h2>Appendix (Evidence Trace)</h2>
                    <p class="small">This report was generated from synthesized analysis output and selected chat evidence available at export time.</p>
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(selectionId),
                escapeHtml(now),
                totalChats,
                escapeHtml(cleanSectionBody(exec)),
                metricsTable,
                risksHtml,
                recsHtml,
                coverageHtml
        );
    }

    private String buildFallbackRowsHtml(List<TermChatSnapshot> rows, String selectionId) {
        StringBuilder tableRows = new StringBuilder();
        int limit = Math.min(rows == null ? 0 : rows.size(), 40);
        for (int i = 0; i < limit; i++) {
            TermChatSnapshot r = rows.get(i);
            String createdAt = r.getCreatedAt() == null ? "" : r.getCreatedAt().toInstant().toString();
            tableRows.append("<tr>")
                    .append("<td>").append(escapeHtml(safe(r.getChatId()))).append("</td>")
                    .append("<td>").append(escapeHtml(createdAt)).append("</td>")
                    .append("<td>").append(escapeHtml(trimForCell(safe(r.getPrompt()), 180))).append("</td>")
                    .append("<td>").append(escapeHtml(trimForCell(safe(r.getResponse()), 180))).append("</td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    @page { size: A4; margin: 1in; }
                    body { font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #1f2937; }
                    h1 { margin-bottom: 4px; }
                    .meta { color: #64748b; font-size: 11px; margin-bottom: 10px; }
                    table { width:100%%; border-collapse: collapse; }
                    th, td { border:1px solid #e2e8f0; padding:6px; vertical-align:top; }
                    th { background:#f8fafc; text-align:left; }
                  </style>
                </head>
                <body>
                  <h1>Chat Export Report</h1>
                  <div class="meta">Selection: %s</div>
                  <table>
                    <thead>
                      <tr><th>Chat ID</th><th>Created</th><th>Prompt</th><th>Response</th></tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                </body>
                </html>
                """.formatted(escapeHtml(selectionId), tableRows.toString());
    }

    private String section(String md, String heading) {
        if (md == null || md.isBlank()) {
            return "";
        }
        String marker = "## " + heading;
        int start = md.indexOf(marker);
        if (start < 0) {
            return "";
        }
        int next = md.indexOf("\n## ", start + marker.length());
        return (next < 0 ? md.substring(start) : md.substring(start, next)).trim();
    }

    private String cleanSectionBody(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceFirst("^##\\s+[^\\n]+\\s*", "").trim();
    }

    private String markdownBulletsToHtml(String sectionMd) {
        String body = cleanSectionBody(sectionMd);
        if (body.isBlank()) {
            return "<p>None</p>";
        }

        String[] lines = body.split("\\r?\\n");
        StringBuilder out = new StringBuilder();
        boolean ulOpen = false;

        for (String line : lines) {
            String t = line.trim();
            if (t.startsWith("### ")) {
                if (ulOpen) {
                    out.append("</ul>");
                    ulOpen = false;
                }
                out.append("<h3>").append(escapeHtml(t.substring(4).trim())).append("</h3>");
            } else if (t.startsWith("- ")) {
                if (!ulOpen) {
                    out.append("<ul>");
                    ulOpen = true;
                }
                out.append("<li>").append(escapeHtml(t.substring(2).trim())).append("</li>");
            } else if (!t.isBlank()) {
                if (ulOpen) {
                    out.append("</ul>");
                    ulOpen = false;
                }
                out.append("<p>").append(escapeHtml(t)).append("</p>");
            }
        }
        if (ulOpen) {
            out.append("</ul>");
        }
        return out.toString();
    }

    private String markdownTableToHtml(String sectionMd) {
        String body = cleanSectionBody(sectionMd);
        String[] lines = body.split("\\r?\\n");
        List<String> tableLines = new ArrayList<>();
        for (String l : lines) {
            if (l.contains("|")) {
                tableLines.add(l.trim());
            }
        }
        if (tableLines.size() < 2) {
            return "<p>No metrics table found.</p>";
        }

        // row0 = header, row1 = separator, rest = data
        List<String> header = splitPipeRow(tableLines.get(0));
        StringBuilder html = new StringBuilder("<table><thead><tr>");
        for (String h : header) {
            html.append("<th>").append(escapeHtml(h)).append("</th>");
        }
        html.append("</tr></thead><tbody>");

        for (int i = 2; i < tableLines.size(); i++) {
            List<String> cells = splitPipeRow(tableLines.get(i));
            if (cells.isEmpty()) {
                continue;
            }
            html.append("<tr>");
            for (String c : cells) {
                html.append("<td>").append(escapeHtml(c)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private List<String> splitPipeRow(String row) {
        String r = row == null ? "" : row.trim();
        if (r.startsWith("|")) {
            r = r.substring(1);
        }
        if (r.endsWith("|")) {
            r = r.substring(0, r.length() - 1);
        }

        String[] parts = r.split("\\|");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isBlank()) {
                out.add(t);
            }
        }
        return out;
    }

    // ---------- helpers ----------
    private String trimForCell(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static String csvLine(String[] cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(csvEscape(cols[i]));
        }
        return sb.toString();
    }

    private static String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        boolean needsQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        java.sql.DatabaseMetaData meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
