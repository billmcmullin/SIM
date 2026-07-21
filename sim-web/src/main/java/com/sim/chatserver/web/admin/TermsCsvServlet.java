package com.sim.chatserver.web.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * CSV import/export for terms. Export (GET) returns CSV, Import (POST) accepts
 * CSV upload.
 *
 * CSV format header: name,description,match_pattern,match_type,system_flag
 *
 * Only ADMIN users may call these endpoints (simple session role check to match
 * existing patterns).
 */
@WebServlet(name = "TermsCsvServlet", urlPatterns = {"/admin/terms/export", "/admin/terms/import"})
@MultipartConfig(fileSizeThreshold = 1024 * 50, // 50KB
        maxFileSize = 1024 * 1024 * 5, // 5MB
        maxRequestSize = 1024 * 1024 * 10) // 10MB
public class TermsCsvServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TermsCsvServlet.class.getName());

    @Inject
    private TermsStore termsStore;

    private static final String[] CSV_HEADER = new String[]{"name", "description", "match_pattern", "match_type", "system_flag"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // export
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
            return;
        }

        try {
            List<TermDefinition> terms = termsStore.listAll();

            // Build CSV into a UTF-8 byte stream so client fetch receives correct bytes.
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("text/csv; charset=UTF-8");

            // Provide both filename and filename* to support non-ASCII filenames in some clients
            String filename = "terms-export.csv";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);

            try (OutputStream out = resp.getOutputStream()) {
                // header
                String headerLine = csvLine(CSV_HEADER) + "\n";
                out.write(headerLine.getBytes(StandardCharsets.UTF_8));

                for (TermDefinition t : terms) {
                    String[] cols = new String[]{
                        t.getName() == null ? "" : t.getName(),
                        t.getDescription() == null ? "" : t.getDescription(),
                        t.getMatchPattern() == null ? "" : t.getMatchPattern(),
                        t.getMatchType() == null ? "" : t.getMatchType(),
                        String.valueOf(t.isSystemFlag())
                    };
                    String line = csvLine(cols) + "\n";
                    out.write(line.getBytes(StandardCharsets.UTF_8));
                }
                out.flush();
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to export terms", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to export terms: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // import
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
            return;
        }

        Part filePart = req.getPart("file");
        if (filePart == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file uploaded.");
            return;
        }

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream in = filePart.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String line;
            boolean sawHeader = false;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                // first non-empty line is header - attempt to validate
                if (!sawHeader) {
                    sawHeader = true;
                    String[] headerCols = parseCsvLine(line);
                    if (!headerMatches(headerCols)) {
                        // If header does not match, treat the first line as data (header optional).
                        String[] dataCols = headerCols;
                        try {
                            boolean createdNew = processRow(dataCols);
                            if (createdNew) {
                                created++;
                            } else {
                                updated++;
                            }
                        } catch (IllegalArgumentException | SQLException e) {
                            log.log(Level.FINE, "Skipping invalid CSV data line", e);
                            errors.add("line " + lineNum + ": " + e.getMessage());
                        }
                    }
                    continue;
                }

                String[] cols = parseCsvLine(line);
                try {
                    boolean createdNew = processRow(cols);
                    if (createdNew) {
                        created++;
                    } else {
                        updated++;
                    }
                } catch (IllegalArgumentException | SQLException e) {
                    log.log(Level.FINE, "Skipping invalid CSV data line", e);
                    errors.add("line " + lineNum + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            log.log(Level.WARNING, "CSV import failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV import failed: " + e.getMessage());
            return;
        }

        // Redirect back to admin terms page with stats (URL-encode)
        StringBuilder msg = new StringBuilder();
        msg.append("imported=").append(created).append("&updated=").append(updated);
        if (!errors.isEmpty()) {
            msg.append("&errors=").append(URLEncoder.encode(String.join("; ", errors), StandardCharsets.UTF_8));
        }
        resp.sendRedirect(safeRedirectTarget(req, msg.toString()));
    }

    private String safeRedirectTarget(HttpServletRequest req, String query) {
        String contextPath = req == null ? "" : req.getContextPath();
        String safeContext = (contextPath == null || contextPath.isBlank()) ? "" : contextPath.trim();
        if (!safeContext.isEmpty() && (!safeContext.startsWith("/") || safeContext.contains("://") || safeContext.contains("\r") || safeContext.contains("\n"))) {
            safeContext = "";
        }
        return safeContext + "/admin/terms?" + (query == null ? "" : query);
    }

    /**
     * Return true = created new term, false = updated existing or skipped
     * (system term).
     *
     * Behavior (per request): - Parse CSV columns:
     * name,description,match_pattern,match_type,system_flag - If a term with
     * same name exists: - If it's a system term (existing.isSystemFlag() ==
     * true) then DO NOT modify it (skip). - Otherwise (non-system) overwrite
     * the DB record's name/description/matchPattern/matchType using the CSV
     * columns by calling termsStore.updateTerm(...). - If no existing term
     * found, create a new term using CSV columns (calls createTerm).
     *
     * Note: changing the system_flag on an existing term is not performed here
     * because TermsStore does not expose a setter for that in the current API.
     * If you want the CSV to be able to flip system-ness, add an API in
     * TermsStore (e.g. setSystemFlag(id, boolean)) and call it here.
     */
    private boolean processRow(String[] cols) throws SQLException {
        // Ensure at least column count length
        String name = cols.length > 0 ? cols[0].trim() : "";
        String description = cols.length > 1 ? cols[1].trim() : "";
        String matchPattern = cols.length > 2 ? cols[2].trim() : "";
        String matchType = cols.length > 3 ? cols[3].trim() : "";
        String systemFlagStr = cols.length > 4 ? cols[4].trim().toLowerCase(Locale.ROOT) : "false";
        boolean csvSystemFlag = "true".equals(systemFlagStr) || "1".equals(systemFlagStr);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }

        // find existing by name (listAll and match by name, case-insensitive)
        TermDefinition existing = null;
        List<TermDefinition> all = termsStore.listAll();
        for (TermDefinition td : all) {
            if (td.getName() != null && td.getName().equalsIgnoreCase(name)) {
                existing = td;
                break;
            }
        }

        if (existing != null) {
            // If the existing term is a system term, do not overwrite it.
            if (existing.isSystemFlag()) {
                // Skip modifying system terms.
                return false;
            }

            // Overwrite non-system entries using CSV columns (name, description, pattern, type)
            TermDefinition updated = termsStore.updateTerm(existing.getId(), name, description, matchPattern, matchType);
            if (updated == null) {
                // If updateTerm returns null (unexpected for non-system), treat as failure.
                throw new SQLException("Failed to update term with id " + existing.getId());
            }

            // Note: we do not toggle existing system-ness here even if CSV's system_flag is true.
            // If you need to set system-flag on existing rows, add a TermsStore API to do so and call it here.
            return false; // existing updated
        } else {
            // No existing term found — create new term using CSV columns.
            TermDefinition created = termsStore.createTerm(name, description, matchPattern, matchType);

            // Note: TermsStore.createTerm in the current API doesn't accept a systemFlag parameter.
            // If you need to create a term marked as system (csvSystemFlag == true), then
            // extend TermsStore with an API (e.g., createTerm(name, desc, pattern, type, isSystem))
            // and call it instead. For now we create a normal (non-system) term.
            return created != null;
        }
    }

    // Simple CSV writer: quote each column as needed and join with commas
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

    // Minimal CSV parser supporting quoted fields ("" -> ")
    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            return new String[0];
        }
        int len = line.length();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // lookahead for doubled quote
                    if (i + 1 < len && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static boolean headerMatches(String[] headerCols) {
        if (headerCols == null || headerCols.length < CSV_HEADER.length) {
            return false;
        }
        for (int i = 0; i < CSV_HEADER.length; i++) {
            if (!CSV_HEADER[i].equalsIgnoreCase(headerCols[i].trim())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAdmin(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        Object role = req.getSession(false) == null ? null : req.getSession(false).getAttribute("role");
        return role != null && "ADMIN".equalsIgnoreCase(String.valueOf(role));
    }
}
