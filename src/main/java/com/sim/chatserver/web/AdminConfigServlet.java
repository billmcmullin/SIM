// src/main/java/com/sim/chatserver/web/AdminConfigServlet.java
package com.sim.chatserver.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import com.sim.chatserver.config.ConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminConfigServlet", urlPatterns = {"/admin"})
public class AdminConfigServlet extends HttpServlet {

    private static final String TEMPLATE_PATH = "/WEB-INF/views/admin_config.html";

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ConfigStore.ensureTable();
        } catch (Exception e) {
            throw new ServletException("Unable to initialize configuration storage", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        ServerConfig config;
        try {
            config = ConfigStore.load();
        } catch (Exception e) {
            throw new ServletException("Unable to load server configuration", e);
        }

        String widgetListJson = "[]";
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            widgetListJson = serializeWidgets(widgets);
        } catch (SQLException e) {
            log("Unable to load widget entries", e);
        }

        boolean apiKeyStored = config != null && config.getApiKey() != null && !config.getApiKey().isBlank();
        String apiKeyForJs = escapeJs(apiKeyStored ? config.getApiKey() : "");

        String rendered = template
                .replace("${user}", escapeHtml(String.valueOf(session.getAttribute("user"))))
                .replace("${contextPath}", req.getContextPath())
                .replace("${serverHost}", escapeAttribute(config != null ? config.getServerHost() : ""))
                .replace("${serverPort}", config != null ? String.valueOf(config.getServerPort()) : "")
                .replace("${connectionInfo}", escapeAttribute(config != null ? config.getConnectionInfo() : ""))
                .replace("${apiKey}", "")
                .replace("${apiKeyStored}", Boolean.toString(apiKeyStored))
                .replace("${apiKeyForJs}", apiKeyForJs)
                .replace("${widgetListJson}", widgetListJson);

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) throws IOException {
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

    private static String serializeWidgets(List<WidgetEntry> widgets) {
        if (widgets == null || widgets.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (WidgetEntry entry : widgets) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("{\"id\":").append(entry.getId())
                    .append(",\"widgetId\":\"").append(escapeJson(entry.getWidgetId())).append("\"")
                    .append(",\"displayName\":\"").append(escapeJson(entry.getDisplayName())).append("\"")
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private static String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
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

    private String escapeAttribute(String input) {
        return escapeHtml(input);
    }
}
