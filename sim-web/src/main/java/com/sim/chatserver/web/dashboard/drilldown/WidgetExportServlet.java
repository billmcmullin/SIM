package com.sim.chatserver.web.dashboard.drilldown;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import java.util.function.Supplier;

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
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionIdFormatter;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
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
    private static final int FALLBACK_ROW_LIMIT = parseIntProperty("export.fallbackRowLimit", 40);
    private static final Color TABLE_HEADER_BG = new Color(245, 247, 250);
    private static final int MAX_JSON_PAYLOAD_BYTES = 128 * 1024;
    private final ThreadLocal<Supplier<AppDataSourceHolder>> dataSourceHolderOverride = new ThreadLocal<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        long startedAt = System.nanoTime();

        if (!isLoggedIn(req, resp)) {
            return;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body.");
            return;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body.");
            return;
        }

        String selectionId = payload.getString("selectionId", "").trim();
        if (selectionId.isEmpty()) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "selectionId required.");
            return;
        }

        List<String> selectedChatIds = dedupePreserveOrder(parseSelectedChatIds(payload));
        String format = normalizeFormat(payload.getString("format", DEFAULT_FORMAT));
        String reportMarkdown = payload.getString("reportMarkdown", "").trim();

        HttpSession session = req.getSession(false);
        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, "Selection not found.");
            return;
        }

        try {
            List<TermChatSnapshot> exportRows = resolveExportRows(selection, selectedChatIds);

            String filename = "chats-export-" + Instant.now().toString().replace(":", "-") + '.' + extensionFor(format);
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
                log.info(() -> "[export] format=" + format
                    + " selectionId=" + selectionId
                    + " selectedIds=" + selectedChatIds.size()
                    + " rows=" + exportRows.size()
                    + " durationMs=" + ms);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Export failed", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Export failed.");
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to send export error response", e);
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
        } catch (ClassCastException | IllegalStateException ex) {
            log.log(Level.FINE, "Unable to parse selectedChatIds payload", ex);
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

    private List<TermChatSnapshot> resolveExportRows(WidgetReviewStartServlet.Selection selection, List<String> selectedChatIds) {
        List<TermChatSnapshot> exportRows = new ArrayList<>();

        if (selection.hasSnapshots()) {
            if (selectedChatIds.isEmpty()) {
                exportRows.addAll(selection.snapshots);
                return exportRows;
            }

            Map<String, OrderIndex> order = indexByOrder(selectedChatIds);
            for (TermChatSnapshot s : selection.snapshots) {
                String chatId = s.getChatId();
                if (chatId != null && order.containsKey(chatId)) {
                    exportRows.add(s);
                }
            }

            exportRows.sort(Comparator.comparingInt(x -> orderValue(order, safe(x.getChatId()))));
            return exportRows;
        }

        if (selectedChatIds.isEmpty()) {
            return exportRows;
        }

        String widgetId = selection.widgetId;
        Map<String, OrderIndex> order = indexByOrder(selectedChatIds);
        exportRows.addAll(new WidgetExportQueryService(dataSourceHolder(), log)
                .loadRows(widgetId, selectedChatIds));

        exportRows.sort(Comparator.comparingInt(x -> orderValue(order, safe(x.getChatId()))));
        return exportRows;
    }

    private Map<String, OrderIndex> indexByOrder(List<String> ids) {
        Map<String, OrderIndex> out = new LinkedHashMap<>();
        int i = 0;
        for (String id : ids) {
            if (!out.containsKey(id)) {
                out.put(id, new OrderIndex(i));
                i++;
            }
        }
        return out;
    }

    private int orderValue(Map<String, OrderIndex> order, String chatId) {
        if (order == null || chatId == null) {
            return Integer.MAX_VALUE;
        }
        OrderIndex idx = order.get(chatId);
        return idx == null ? Integer.MAX_VALUE : idx.value;
    }

    private void writeCsv(HttpServletResponse resp, List<TermChatSnapshot> exportRows) {
        resp.setContentType("text/csv; charset=UTF-8");
        OutputStream out = openOutputStreamSafe(resp, "csv");
        try (out) {
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
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write CSV export", e);
        }
    }

    private void writeJson(HttpServletResponse resp, List<TermChatSnapshot> exportRows) {
        resp.setContentType("application/json; charset=UTF-8");
        OutputStream out = openOutputStreamSafe(resp, "json");
        try (out) {
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
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write JSON export", e);
        }
    }

    private void writeText(HttpServletResponse resp, List<TermChatSnapshot> exportRows) {
        resp.setContentType("text/plain; charset=UTF-8");
        OutputStream out = openOutputStreamSafe(resp, "text");
        try (out) {
            for (TermChatSnapshot row : exportRows) {
                String sessionId = safe(row.getSessionId());
                String sessionDisplay = SessionIdFormatter.formatForDisplay(sessionId);
                String createdAt = row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString();
                out.write(("Session: " + sessionId + " (" + sessionDisplay + ')').getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.write(("Created At: " + createdAt).getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.write(("Prompt:\n" + safe(row.getPrompt()) + "\n\n").getBytes(StandardCharsets.UTF_8));
                out.write(("Response:\n" + safe(row.getResponse())).getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.write(("----------------------------------------").getBytes(StandardCharsets.UTF_8));
                out.write('\n');
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write text export", e);
        }
    }

    private void writePdf(HttpServletResponse resp, List<TermChatSnapshot> rows, String selectionId, String reportMarkdown) {
        try {
            byte[] pdfBytes = buildPdf(reportMarkdown, rows, selectionId);
            resp.setContentType("application/pdf");
            resp.setContentLength(pdfBytes.length);
            try (OutputStream out = resp.getOutputStream()) {
                out.write(pdfBytes);
            }
        } catch (IllegalStateException | IOException t) {
            log.log(Level.WARNING, "PDF generation failed; falling back to text export.", t);
            String fallback = (reportMarkdown != null && !reportMarkdown.isBlank())
                    ? reportMarkdown
                    : buildFallbackText(rows, selectionId);
            writeTextPayload(resp, fallback);
        }
    }

    private void writeTextPayload(HttpServletResponse resp, String payload) {
        resp.setContentType("text/plain; charset=UTF-8");
        OutputStream out = openOutputStreamSafe(resp, "text-fallback");
        try (out) {
            out.write(safe(payload).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write fallback text export", e);
        }
    }

    private byte[] buildPdf(String reportMarkdown, List<TermChatSnapshot> rows, String selectionId) {
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
            addDocumentElement(doc, pTitle);

            addDocumentElement(doc, new Paragraph("Selection ID: " + safe(selectionId), small));
            addDocumentElement(doc, new Paragraph("Generated: " + Instant.now(), small));
            addDocumentElement(doc, new Paragraph(" "));

            if (reportMarkdown != null && !reportMarkdown.isBlank()) {
                addSection(doc, h2, normal, "Executive Chat Analysis", cleanSectionBody(section(reportMarkdown, "Executive Chat Analysis")));
                addBulletSection(doc, h2, normal, "Risks and Opportunities", section(reportMarkdown, "Risks and Opportunities"));
                addBulletSection(doc, h2, normal, "Recommendations", section(reportMarkdown, "Recommendations"));
                addBulletSection(doc, h2, normal, "Sentiment and Frustration Signals", section(reportMarkdown, "Sentiment and Frustration Signals"));
                addBulletSection(doc, h2, normal, "Coverage and Methodology", section(reportMarkdown, "Coverage and Methodology"));
                addMetricsTable(doc, h2, normal, section(reportMarkdown, "Key Metrics"));
            } else {
                addSection(doc, h2, normal, "Chat Export Report", "No synthesized report was provided. Showing selected chat rows.");
                addRowsTable(doc, rows, normal);
            }
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate PDF content", ex);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }

        return baos.toByteArray();
    }

    private void addSection(Document doc, Font h2, Font normal, String heading, String body) {
        Paragraph head = new Paragraph(heading, h2);
        head.setSpacingBefore(6);
        head.setSpacingAfter(4);
        addDocumentElement(doc, head);

        Paragraph content = new Paragraph(safe(body), normal);
        content.setSpacingAfter(8);
        addDocumentElement(doc, content);
    }

    private void addBulletSection(Document doc, Font h2, Font normal, String heading, String sectionMd) {
        Paragraph head = new Paragraph(heading, h2);
        head.setSpacingBefore(6);
        head.setSpacingAfter(4);
        addDocumentElement(doc, head);

        String body = cleanSectionBody(sectionMd);
        if (body.isBlank()) {
            addDocumentElement(doc, new Paragraph("None", normal));
            return;
        }

        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            String t = line.trim();
            if (t.startsWith("### ")) {
                Paragraph sub = new Paragraph(t.substring(4).trim(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
                sub.setSpacingBefore(4);
                sub.setSpacingAfter(2);
                addDocumentElement(doc, sub);
            } else if (t.startsWith("- ")) {
                Paragraph bullet = new Paragraph("- " + t.substring(2).trim(), normal);
                bullet.setIndentationLeft(14f);
                bullet.setSpacingAfter(2);
                addDocumentElement(doc, bullet);
            } else if (!t.isBlank()) {
                Paragraph para = new Paragraph(t, normal);
                para.setSpacingAfter(3);
                addDocumentElement(doc, para);
            }
        }
    }

    private void addMetricsTable(Document doc, Font h2, Font normal, String sectionMd) {
        Paragraph head = new Paragraph("Key Metrics", h2);
        head.setSpacingBefore(6);
        head.setSpacingAfter(4);
        addDocumentElement(doc, head);

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
            addDocumentElement(doc, new Paragraph("No metrics table found.", normal));
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

        addDocumentElement(doc, table);
    }

    private boolean isMarkdownTableSeparatorRow(String line) {
        String s = line == null ? "" : line.replace("|", "").trim();
        return !s.isEmpty() && s.matches("[:\\-\\s]+");
    }

    private void addRowsTable(Document doc, List<TermChatSnapshot> rows, Font normal) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(8f);
        try {
            table.setWidths(new float[]{1.2f, 1.4f, 3.2f, 3.2f});
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to configure PDF table widths", ex);
        }

        addHeaderCell(table, "Chat ID");
        addHeaderCell(table, "Created");
        addHeaderCell(table, "Prompt");
        addHeaderCell(table, "Response");

        int limit = Math.min(rows == null ? 0 : rows.size(), FALLBACK_ROW_LIMIT);
        for (int i = 0; i < limit; i++) {
            TermChatSnapshot r = rows.get(i);
            if (r == null) {
                continue;
            }
            String createdAt = r.getCreatedAt() == null ? "" : r.getCreatedAt().toInstant().toString();
            table.addCell(new PdfPCell(new Phrase(safe(r.getChatId()), normal)));
            table.addCell(new PdfPCell(new Phrase(createdAt, normal)));
            table.addCell(new PdfPCell(new Phrase(trimForCell(safe(r.getPrompt()), 180), normal)));
            table.addCell(new PdfPCell(new Phrase(trimForCell(safe(r.getResponse()), 180), normal)));
        }

        addDocumentElement(doc, table);
    }

    private void addDocumentElement(Document doc, Element element) {
        try {
            doc.add(element);
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to add PDF element", ex);
        }
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

        List<TermChatSnapshot> safeRows = rows == null ? List.of() : rows;

        int limit = Math.min(safeRows.size(), FALLBACK_ROW_LIMIT);
        for (int i = 0; i < limit; i++) {
            TermChatSnapshot r = safeRows.get(i);
            if (r == null) {
                continue;
            }
            sb.append("Chat ID: ").append(safe(r.getChatId())).append('\n');
            sb.append("Created: ").append(r.getCreatedAt() == null ? "" : r.getCreatedAt().toInstant()).append('\n');
            sb.append("Prompt: ").append(trimForCell(safe(r.getPrompt()), 180)).append('\n');
            sb.append("Response: ").append(trimForCell(safe(r.getResponse()), 180)).append('\n');
            sb.append("--------------------------------------------------").append('\n');
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
        if (!r.isEmpty() && r.charAt(0) == '|') {
            r = r.substring(1);
        }
        if (!r.isEmpty() && r.charAt(r.length() - 1) == '|') {
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
        return s.length() <= max ? s : s.substring(0, max) + "...";
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
        return needsQuote ? '"' + escaped + '"' : escaped;
    }

    private static int parseIntProperty(String name, int fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        String envName = name.trim().toUpperCase(Locale.ROOT).replace('.', '_');
        String raw = readEnvSetting(envName);
        if (raw.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid export servlet numeric setting", ex);
            return fallback;
        }
    }

    private static String readEnvSetting(String envName) {
        if (envName == null || envName.isBlank()) {
            return "";
        }
        String raw = new ProcessBuilder().environment().getOrDefault(envName, "");
        if (raw.isBlank()) {
            return "";
        }
        String normalized = ServletRequestParamUtil.normalizeValue(raw, 32, true, false);
        return normalized.length() > 32 ? normalized.substring(0, 32) : normalized;
    }

    private OutputStream openOutputStreamSafe(HttpServletResponse resp, String context) {
        try {
            return resp.getOutputStream();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to open " + context + " response stream", ex);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        Supplier<AppDataSourceHolder> override = dataSourceHolderOverride.get();
        if (override != null) {
            return override.get();
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setHeader("Cache-Control", "no-store");
            try {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            } catch (IOException e) {
                log.log(Level.FINE, "Unable to write auth error response", e);
                sendErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            }
            return false;
        }
        return true;
    }

    private static final class OrderIndex {

        final int value;

        private OrderIndex(int value) {
            this.value = value;
        }
    }
}
