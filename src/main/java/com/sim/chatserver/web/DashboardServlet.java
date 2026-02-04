package com.sim.chatserver.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard.html";

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();
        String adminLink = "ADMIN".equalsIgnoreCase(role)
                ? "<p><a href=\"" + req.getContextPath() + "/admin\">Go to Admin Configuration</a></p>"
                : "";

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard", e);
        }

        List<WidgetStat> widgetStats = buildWidgetStats(widgets);
        int totalChats = widgetStats.stream().mapToInt(stat -> stat.count).sum();
        String statsRows = renderWidgetStatsRows(widgetStats, req.getContextPath());

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String userName = String.valueOf(session.getAttribute("user"));
        String rendered = template
                .replace("${user}", escapeHtml(userName))
                .replace("${contextPath}", req.getContextPath())
                .replace("${role}", escapeHtml(role))
                .replace("${adminLink}", adminLink)
                .replace("${totalChats}", escapeHtml(String.valueOf(totalChats)))
                .replace("${widgetStatsRows}", statsRows);

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        if (stats.isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"empty-row\">No widget chats available.</td></tr>";
        }
        StringBuilder builder = new StringBuilder();
        for (WidgetStat stat : stats) {
            builder.append("<tr>")
                    .append("<td>").append(escapeHtml(stat.label)).append("</td>")
                    .append("<td>").append(stat.count).append("</td>")
                    .append("<td><a href=\"").append(contextPath)
                    .append("/admin/widgets/view?widgetId=").append(stat.widgetId).append("\">View chats</a></td>")
                    .append("</tr>");
        }
        return builder.toString();
    }

    private List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
        List<WidgetStat> stats = new ArrayList<>();
        if (widgets == null || widgets.isEmpty()) {
            return stats;
        }
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String displayName = widget.getDisplayName();
                displayName = displayName == null || displayName.isBlank() ? widgetId : displayName;
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }
                int count = countRows(conn, tableName);
                stats.add(new WidgetStat(widgetId, displayName, count));
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to query widget tables", e);
        }
        return stats;
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
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

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class WidgetStat {

        private final String widgetId;
        private final String label;
        private final int count;

        private WidgetStat(String widgetId, String label, int count) {
            this.widgetId = widgetId;
            this.label = label;
            this.count = count;
        }
    }
}
