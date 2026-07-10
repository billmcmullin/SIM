package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sim.chatserver.config.Database;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin data export backup endpoint.
 *
 * Exports all PUBLIC schema base tables as CSV files into a ZIP: -
 * tables/<table_name>.csv - manifest.json
 *
 * This includes dynamic widget tables (widget ID tables) automatically.
 *
 * Note: This is a raw data export backup, not a full pg_dump replacement.
 */
public class DatabaseBackupServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DatabaseBackupServlet.class.getName());

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";

    // Optional exclusions from export
    private static final List<String> EXCLUDED_TABLES = List.of(
            "flyway_schema_history"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            return;
        }

        String ts = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        String fileName = "chatserver-data-backup-" + ts + ".zip";

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (Connection conn = Database.getConnection(); ZipOutputStream zip = new ZipOutputStream(resp.getOutputStream(), StandardCharsets.UTF_8)) {

            List<String> exported = new ArrayList<>();
            List<String> skipped = new ArrayList<>();

            List<String> tables = listExportableTables(conn);
            for (String table : tables) {
                try {
                    exportTableAsCsv(conn, zip, table);
                    exported.add(table);
                } catch (Exception tableErr) {
                    skipped.add(table);
                    log.log(Level.WARNING, "Failed exporting table: " + table, tableErr);
                }
            }

            writeManifest(zip, exported, skipped, ts);

            zip.finish();
            log.info(() -> "Data backup export completed. Exported tables=" + exported.size() + ", skipped=" + skipped.size());

        } catch (Exception e) {
            log.log(Level.SEVERE, "Data backup export failed", e);
            if (!resp.isCommitted()) {
                resp.reset();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Data export failed.");
            }
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? "" : String.valueOf(roleObj);
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Returns all PUBLIC schema base tables, excluding internal/system tables
     * if configured.
     */
    private List<String> listExportableTables(Connection conn) throws SQLException {
        List<String> out = new ArrayList<>();

        String sql = """
                SELECT tablename
                  FROM pg_catalog.pg_tables
                 WHERE schemaname = 'public'
                 ORDER BY tablename
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String table = rs.getString(1);
                if (table == null || table.isBlank()) {
                    continue;
                }
                if (EXCLUDED_TABLES.contains(table)) {
                    continue;
                }
                out.add(table);
            }
        }

        return out;
    }

    private void exportTableAsCsv(Connection conn, ZipOutputStream zip, String tableName) throws SQLException, IOException {
        String sql = "SELECT * FROM " + quoteIdent(tableName);

        try (PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(1000);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                zip.putNextEntry(new ZipEntry("tables/" + tableName + ".csv"));
                PrintWriter w = new PrintWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8), false);

                // Header row
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        w.print(",");
                    }
                    w.print(csvEscape(md.getColumnLabel(i)));
                }
                w.print("\n");

                // Data rows
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) {
                            w.print(",");
                        }
                        w.print(csvEscape(readCellAsText(rs, md, i)));
                    }
                    w.print("\n");
                }

                w.flush();
                zip.closeEntry();
            }
        }
    }

    private void writeManifest(ZipOutputStream zip, List<String> exported, List<String> skipped, String ts) throws IOException {
        zip.putNextEntry(new ZipEntry("manifest.json"));
        PrintWriter w = new PrintWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8), false);

        w.println("{");
        w.println("  \"formatVersion\": 1,");
        w.println("  \"type\": \"raw-data-export\",");
        w.println("  \"generatedAt\": \"" + jsonEscape(ts) + "\",");
        w.println("  \"schema\": \"public\",");
        w.println("  \"exportedTableCount\": " + exported.size() + ",");
        w.println("  \"skippedTableCount\": " + skipped.size() + ",");
        w.println("  \"exportedTables\": " + toJsonArray(exported) + ",");
        w.println("  \"skippedTables\": " + toJsonArray(skipped));
        w.println("}");

        w.flush();
        zip.closeEntry();
    }

    private String readCellAsText(ResultSet rs, ResultSetMetaData md, int columnIndex) throws SQLException {
        int sqlType = md.getColumnType(columnIndex);
        if (sqlType == Types.BINARY || sqlType == Types.VARBINARY || sqlType == Types.LONGVARBINARY) {
            byte[] bytes = rs.getBytes(columnIndex);
            if (bytes == null) {
                return "";
            }
            return Base64.getEncoder().encodeToString(bytes);
        }

        String value = rs.getString(columnIndex);
        if (value == null) {
            return "";
        }

        Object raw = rs.getObject(columnIndex);
        if (raw instanceof Timestamp ts) {
            return ts.toInstant().toString();
        }
        if (raw instanceof Date d) {
            return new Timestamp(d.getTime()).toInstant().toString();
        }

        return value;
    }

    private String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String x = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + x + "\"" : x;
    }

    private String quoteIdent(String ident) {
        return "\"" + ident.replace("\"", "\"\"") + "\"";
    }

    private String toJsonArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(jsonEscape(values.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
