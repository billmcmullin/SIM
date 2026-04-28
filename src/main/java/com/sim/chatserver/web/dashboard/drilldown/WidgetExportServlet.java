package com.sim.chatserver.web.dashboard.drilldown;

import java.awt.Color;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionIdFormatter;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetExportServlet", urlPatterns = {"/dashboard/widgets/drilldown/export"})
public class WidgetExportServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetExportServlet.class.getName());

    private static final String DEFAULT_FORMAT = "csv";
    private static final int FALLBACK_ROW_LIMIT = Integer.getInteger("export.fallbackRowLimit", 40);
    private static final int DB_ID_CHUNK_SIZE = Integer.getInteger("export.dbIdChunkSize", 500);
    private static final Color TABLE_HEADER_BG = new Color(245, 247, 250);

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long startedAt = System.nanoTime();

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

        List<String> selectedChatIds = dedupePreserveOrder(parseSelectedChatIds(payload));
        String format = normalizeFormat(payload.getString("format", DEFAULT_FORMAT));
        String reportMarkdown = payload.getString("reportMarkdown", "").trim();

        HttpSession session = req.getSession(false);
        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Selection not found.");
            return;
        }

        try {
            List<TermChatSnapshot> exportRows = resolveExportRows(selection, selectedChatIds);

            String filename = "chats-export-" + Instant.now().toString().replace(":", "-") + "." + extensionFor(format);
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
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

            long ms = (System.nanoTime() - startedAt) / 1_000_000L;
            log.info(() -> String.format(
                    "[export] format=%s selectionId=%s selectedIds=%d rows=%d durationMs=%d",
                    format, selectionId, selectedChatIds.size(), exportRows.size(), ms
            ));
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Export failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Export failed.");
        }
    }

    private List<String> parseSelectedChatIds(JsonObject payload) {
        List<String> ids = new ArrayList<>();
        if (!payload.containsKey("selectedChatIds")) {
            return ids;
        }

        try {
            JsonArray arr = payload.getJsonArray("selectedChatIds");
            if (arr == null) {
                return ids;
            }

            for (JsonValue v : arr) {
                String id = "";
                if (v.getValueType() == JsonValue.ValueType.STRING) {
                    id = ((JsonString) v).getString();
                } else if (v.getValueType() != JsonValue.ValueType.NULL) {
                    id = v.toString();
                }
                id = id == null ? "" : id.trim();
                if (!id.isEmpty()) {
                    ids.add(id);
                }
            }
        } catch (Exception ignored) {
            // tolerant parse
        }

        return ids;
    }

    private List<String> dedupePreserveOrder(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    private String normalizeFormat(String raw) {
        String format = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (format.isEmpty()) {
            return DEFAULT_FORMAT;
        }
        return switch (format) {
            case "csv", "json", "text", "pdf" ->
                format;
            default ->
                DEFAULT_FORMAT;
        };
    }

    private String extensionFor(String format) {
        return switch (format) {
            case "json" ->
                "json";
            case "text" ->
                "txt";
            case "pdf" ->
                "pdf";
            default ->
                "csv";
        };
    }

    private List<TermChatSnapshot> resolveExportRows(WidgetReviewStartServlet.Selection selection, List<String> selectedChatIds) throws SQLException {
        List<TermChatSnapshot> exportRows = new ArrayList<>();

        if (selection.hasSnapshots()) {
            if (selectedChatIds.isEmpty()) {
                exportRows.addAll(selection.snapshots);
                return exportRows;
            }

            Map<String, Integer> order = indexByOrder(selectedChatIds);
            for (TermChatSnapshot s : selection.snapshots) {
                String chatId = s.getChatId();
                if (chatId != null && order.containsKey(chatId)) {
                    exportRows.add(s);
                }
            }

            exportRows.sort(Comparator.comparingInt(x -> order.getOrDefault(safe(x.getChatId()), Integer.MAX_VALUE)));
            return exportRows;
        }

        if (selectedChatIds.isEmpty()) {
            return exportRows;
        }

        String widgetId = selection.widgetId;
        String tableName = sanitizeWidgetTableName(widgetId);
        Map<String, Integer> order = indexByOrder(selectedChatIds);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                return exportRows;
            }

            for (List<String> chunk : chunk(selectedChatIds, DB_ID_CHUNK_SIZE)) {
                if (chunk.isEmpty()) {
                    continue;
                }

                String placeholders = chunk.stream().map(x -> "?").collect(Collectors.joining(","));
                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE widget_chat_id IN (" + placeholders + ")";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int idx = 1;
                    for (String id : chunk) {
                        ps.setString(idx++, id);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
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

        exportRows.sort(Comparator.comparingInt(x -> order.getOrDefault(safe(x.getChatId()), Integer.MAX_VALUE)));
        return exportRows;
    }

    private Map<String, Integer> indexByOrder(List<String> ids) {
        Map<String, Integer> out = new LinkedHashMap<>();
        int i = 0;
        for (String id : ids) {
            out.putIfAbsent(id, i++);
        }
        return out;
    }

    private void writeCsv(HttpServletResponse resp, List<TermChatSnapshot> exportRows) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        try (OutputStream out = resp.getOutputStream()) {
            out.write("sessionId,sessionIdDisplay,createdAt,prompt,response\n".getBytes(StandardCharsets.UTF_8));
            for (TermChatSnapshot row : exportRows) {
                String sessionId = safe(row.getSessionId());
                String sessionDisplay = SessionIdFormatter.formatForDisplay(sessionId);
                String createdAt = row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString();
                out.write(csvLine(new String[]{
                    sessionId, sessionDisplay, createdAt, safe(row.getPrompt()), safe(row.getResponse())
                }).getBytes(StandardCharsets.UTF_8));
                out.write('\n');
            }
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
        }
    }

    private void writePdf(HttpServletResponse resp, List<TermChatSnapshot> rows, String selectionId, String reportMarkdown) throws IOException {
        try {
            byte[] pdfBytes = buildPdf(reportMarkdown, rows, selectionId);
            resp.setContentType("application/pdf");
            resp.setContentLength(pdfBytes.length);
            try (OutputStream out = resp.getOutputStream()) {
                out.write(pdfBytes);
            }
        } catch (Throwable t) {
            log.log(Level.WARNING, "PDF generation failed; falling back to text export.", t);
            resp.setContentType("text/plain; charset=UTF-8");
            try (OutputStream out = resp.getOutputStream()) {
                String fallback = (reportMarkdown != null && !reportMarkdown.isBlank())
                        ? reportMarkdown
                        : buildFallbackText(rows, selectionId);
                out.write(fallback.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private byte[] buildPdf(String reportMarkdown, List<TermChatSnapshot> rows, String selectionId) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph pTitle = new Paragraph("Chat Analysis Evidence Report", title);
            pTitle.setSpacingAfter(8);
            doc.add(pTitle);

            doc.add(new Paragraph("Selection ID: " + safe(selectionId), small));
            doc.add(new Paragraph("Generated: " + Instant.now(), small));
            doc.add(new Paragraph(" "));

            if (reportMarkdown != null && !reportMarkdown.isBlank()) {
                addSection(doc, h2, normal, "Executive Chat Analysis", cleanSectionBody(section(reportMarkdown, "Executive Chat Analysis")));
                addBulletSection(doc, h2, normal, "Risks and Opportunities", section(reportMarkdown, "Risks and Opportunities"));
                addBulletSection(doc, h2, normal, "Recommendations", section(reportMarkdown, "Recommendations"));
                addBulletSection(doc, h2, normal, "Coverage and Methodology", section(reportMarkdown, "Coverage and Methodology"));
                addMetricsTable(doc, h2, normal, section(reportMarkdown, "Key Metrics"));
            } else {
                addSection(doc, h2, normal, "Chat Export Report", "No synthesized report was provided. Showing selected chat rows.");
                addRowsTable(doc, rows, normal);
            }
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }

        return baos.toByteArray();
    }

    private void addSection(Document doc, Font h2, Font normal, String heading, String body) throws DocumentException {
        Paragraph head = new Paragraph(heading, h2);
        head.setSpacingBefore(6);
        head.setSpacingAfter(4);
        doc.add(head);

        Paragraph content = new Paragraph(safe(body), normal);
        content.setSpacingAfter(8);
        doc.add(content);
    }

    private void addBulletSection(Document doc, Font h2, Font normal, String heading, String sectionMd) throws DocumentException {
        Paragraph head = new Paragraph(heading, h2);
        head.setSpacingBefore(6);
        head.setSpacingAfter(4);
        doc.add(head);

        String body = cleanSectionBody(sectionMd);
        if (body.isBlank()) {
            doc.add(new Paragraph("None", normal));
            return;
        }

        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            String t = line.trim();
            if (t.startsWith("### ")) {
                Paragraph sub = new Paragraph(t.substring(4).trim(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
                sub.setSpacingBefore(4);
                sub.setSpacingAfter(2);
                doc.add(sub);
            } else if (t.startsWith("- ")) {
                Paragraph bullet = new Paragraph("• " + t.substring(2).trim(), normal);
                bullet.setIndentationLeft(14f);
                bullet.setSpacingAfter(2);
                doc.add(bullet);
            } else if (!t.isBlank()) {
                Paragraph para = new Paragraph(t, normal);
                para.setSpacingAfter(3);
                doc.add(para);
            }
        }
    }

    private void addMetricsTable(Document doc, Font h2, Font normal, String sectionMd) throws DocumentException {
        Paragraph head = new Paragraph("Key Metrics", h2);
        head.setSpacingBefore(6);
        head.setSpacingAfter(4);
        doc.add(head);

        String body = cleanSectionBody(sectionMd);
        List<String> tableLines = new ArrayList<>();
        for (String l : body.split("\\r?\\n")) {
            if (l.contains("|")) {
                String t = l.trim();
                if (!isMarkdownTableSeparatorRow(t)) {
                    tableLines.add(t);
                }
            }
        }

        if (tableLines.size() < 2) {
            doc.add(new Paragraph("No metrics table found.", normal));
            return;
        }

        List<String> header = splitPipeRow(tableLines.get(0));
        int cols = Math.max(1, header.size());
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100f);
        table.setSpacingAfter(8f);

        for (String h : header) {
            PdfPCell c = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setBackgroundColor(TABLE_HEADER_BG);
            c.setPadding(6f);
            table.addCell(c);
        }

        for (int i = 1; i < tableLines.size(); i++) {
            List<String> cells = splitPipeRow(tableLines.get(i));
            if (cells.isEmpty()) {
                continue;
            }

            for (int c = 0; c < cols; c++) {
                String val = c < cells.size() ? cells.get(c) : "";
                PdfPCell cell = new PdfPCell(new Phrase(val, normal));
                cell.setPadding(6f);
                cell.setVerticalAlignment(Element.ALIGN_TOP);
                table.addCell(cell);
            }
        }

        doc.add(table);
    }

    private boolean isMarkdownTableSeparatorRow(String line) {
        String s = line == null ? "" : line.replace("|", "").trim();
        return !s.isEmpty() && s.matches("[:\\-\\s]+");
    }

    private void addRowsTable(Document doc, List<TermChatSnapshot> rows, Font normal) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(8f);
        table.setWidths(new float[]{1.2f, 1.4f, 3.2f, 3.2f});

        addHeaderCell(table, "Chat ID");
        addHeaderCell(table, "Created");
        addHeaderCell(table, "Prompt");
        addHeaderCell(table, "Response");

        int limit = Math.min(rows == null ? 0 : rows.size(), FALLBACK_ROW_LIMIT);
        for (int i = 0; i < limit; i++) {
            TermChatSnapshot r = rows.get(i);
            String createdAt = r.getCreatedAt() == null ? "" : r.getCreatedAt().toInstant().toString();
            table.addCell(new PdfPCell(new Phrase(safe(r.getChatId()), normal)));
            table.addCell(new PdfPCell(new Phrase(createdAt, normal)));
            table.addCell(new PdfPCell(new Phrase(trimForCell(safe(r.getPrompt()), 180), normal)));
            table.addCell(new PdfPCell(new Phrase(trimForCell(safe(r.getResponse()), 180), normal)));
        }

        doc.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        c.setBackgroundColor(TABLE_HEADER_BG);
        c.setPadding(6f);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(c);
    }

    private String buildFallbackText(List<TermChatSnapshot> rows, String selectionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chat Export Report\n");
        sb.append("Selection: ").append(safe(selectionId)).append("\n\n");

        int limit = Math.min(rows == null ? 0 : rows.size(), FALLBACK_ROW_LIMIT);
        for (int i = 0; i < limit; i++) {
            TermChatSnapshot r = rows.get(i);
            sb.append("Chat ID: ").append(safe(r.getChatId())).append("\n");
            sb.append("Created: ").append(r.getCreatedAt() == null ? "" : r.getCreatedAt().toInstant()).append("\n");
            sb.append("Prompt: ").append(trimForCell(safe(r.getPrompt()), 180)).append("\n");
            sb.append("Response: ").append(trimForCell(safe(r.getResponse()), 180)).append("\n");
            sb.append("--------------------------------------------------\n");
        }
        return sb.toString();
    }

    private String section(String md, String heading) {
        if (md == null || md.isBlank() || heading == null || heading.isBlank()) {
            return "";
        }

        String[] lines = md.split("\\r?\\n", -1);
        int startLine = -1;

        for (int i = 0; i < lines.length; i++) {
            String t = lines[i].trim();
            if (t.startsWith("## ")) {
                String h = t.substring(3).trim();
                if (h.equalsIgnoreCase(heading.trim())) {
                    startLine = i;
                    break;
                }
            }
        }
        if (startLine < 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i < lines.length; i++) {
            String t = lines[i].trim();
            if (i > startLine && t.startsWith("## ")) {
                break;
            }
            sb.append(lines[i]);
            if (i < lines.length - 1) {
                sb.append('\n');
            }
        }

        return sb.toString().trim();
    }

    private String cleanSectionBody(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceFirst("(?is)^##\\s+[^\\n]+\\s*", "").trim();
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
            out.add(p == null ? "" : p.trim());
        }
        return out;
    }

    private String trimForCell(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static List<List<String>> chunk(List<String> input, int size) {
        List<List<String>> out = new ArrayList<>();
        if (input == null || input.isEmpty() || size <= 0) {
            return out;
        }
        for (int i = 0; i < input.size(); i += size) {
            out.add(input.subList(i, Math.min(i + size, input.size())));
        }
        return out;
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
            resp.setContentType("application/json; charset=UTF-8");
            resp.setHeader("Cache-Control", "no-store");
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
}
