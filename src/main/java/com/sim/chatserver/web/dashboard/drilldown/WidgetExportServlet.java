package com.sim.chatserver.web.dashboard.drilldown;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.WidgetReviewStartServlet;

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

/**
 * Export selected chat rows from a review selection.
 *
 * Accepts POST with JSON body: { "selectionId": "...", "selectedChatIds":
 * ["id1","id2", ...], "format": "csv" | "json" | "text" // csv default }
 *
 * Returns an attachment with exported data.
 */
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
                payload.getJsonArray("selectedChatIds").forEach(v -> selectedChatIds.add(v.toString().replace("\"", "").trim()));
            } catch (Exception e) {
                // ignore -> will treat as empty selection
            }
        }

        String format = payload.getString("format", DEFAULT_FORMAT).trim().toLowerCase();
        if (format.isEmpty()) {
            format = DEFAULT_FORMAT;
        }
        if (!format.equals("csv") && !format.equals("json") && !format.equals("text")) {
            format = DEFAULT_FORMAT;
        }

        HttpSession session = req.getSession(false);
        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Selection not found.");
            return;
        }

        try {
            List<TermChatSnapshot> exportRows = new ArrayList<>();

            if (selection.hasSnapshots()) {
                // selection.snapshots is authoritative — filter by selectedChatIds (if provided)
                if (selectedChatIds.isEmpty()) {
                    exportRows.addAll(selection.snapshots);
                } else {
                    for (TermChatSnapshot s : selection.snapshots) {
                        if (s.getChatId() != null && selectedChatIds.contains(s.getChatId())) {
                            exportRows.add(s);
                        }
                    }
                }
            } else {
                // DB-backed widget — load rows for the selected chat ids from widget table
                if (selectedChatIds.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No chat IDs specified for export.");
                    return;
                }
                String widgetId = selection.widgetId;
                String tableName = sanitizeWidgetTableName(widgetId);
                try (Connection conn = dsHolder.getDataSource().getConnection()) {
                    if (!tableExists(conn, tableName)) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Widget table does not exist.");
                        return;
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
            }

            // Prepare response headers and stream content
            String filename = "chats-export-" + Instant.now().toString().replace(":", "-") + "." + (format.equals("csv") ? "csv" : format.equals("json") ? "json" : "txt");
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            String disposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded;

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            if (format.equals("csv")) {
                resp.setContentType("text/csv; charset=UTF-8");
            } else if (format.equals("json")) {
                resp.setContentType("application/json; charset=UTF-8");
            } else {
                resp.setContentType("text/plain; charset=UTF-8");
            }
            resp.setHeader("Content-Disposition", disposition);

            try (OutputStream out = resp.getOutputStream()) {
                if (format.equals("csv")) {
                    // header: sessionId,createdAt,prompt,response
                    String header = "sessionId,createdAt,prompt,response\n";
                    out.write(header.getBytes(StandardCharsets.UTF_8));
                    for (TermChatSnapshot row : exportRows) {
                        String sessionId = safe(row.getSessionId());
                        String createdAt = row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString();
                        String prompt = safe(row.getPrompt());
                        String responseText = safe(row.getResponse());
                        String[] cols = new String[]{sessionId, createdAt, prompt, responseText};
                        out.write(csvLine(cols).getBytes(StandardCharsets.UTF_8));
                        out.write('\n');
                    }
                } else if (format.equals("json")) {
                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    for (TermChatSnapshot row : exportRows) {
                        ab.add(Json.createObjectBuilder()
                                .add("sessionId", row.getSessionId() == null ? "" : row.getSessionId())
                                .add("createdAt", row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString())
                                .add("prompt", row.getPrompt() == null ? "" : row.getPrompt())
                                .add("response", row.getResponse() == null ? "" : row.getResponse())
                        );
                    }
                    Json.createWriter(out).writeArray(ab.build());
                } else { // text
                    for (TermChatSnapshot row : exportRows) {
                        String sessionId = safe(row.getSessionId());
                        String createdAt = row.getCreatedAt() == null ? "" : row.getCreatedAt().toInstant().toString();
                        String prompt = safe(row.getPrompt());
                        String responseText = safe(row.getResponse());
                        out.write(("Session: " + sessionId + "\n").getBytes(StandardCharsets.UTF_8));
                        out.write(("Created At: " + createdAt + "\n").getBytes(StandardCharsets.UTF_8));
                        out.write(("Prompt:\n" + prompt + "\n\n").getBytes(StandardCharsets.UTF_8));
                        out.write(("Response:\n" + responseText + "\n").getBytes(StandardCharsets.UTF_8));
                        out.write(("----------------------------------------\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
                out.flush();
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Export failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Export failed: " + e.getMessage());
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // CSV helpers (same approach as TermsCsvServlet)
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
        if (needsQuote) {
            return "\"" + escaped + "\"";
        } else {
            return escaped;
        }
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
}
