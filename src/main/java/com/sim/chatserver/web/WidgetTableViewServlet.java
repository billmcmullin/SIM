package com.sim.chatserver.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableViewServlet", urlPatterns = {"/admin/widgets/view"})
public class WidgetTableViewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetTableViewServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/widget_table_view.html";

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String widgetId = req.getParameter("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
            return;
        }

        WidgetEntry target = null;
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry widget : widgets) {
                if (widget != null && widgetId.equals(widget.getWidgetId())) {
                    target = widget;
                    break;
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
        }

        String widgetName = target == null || target.getDisplayName() == null || target.getDisplayName().isBlank()
                ? widgetId
                : target.getDisplayName();

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String userName = String.valueOf(session.getAttribute("user"));
        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();
        String rendered = template
                .replace("${user}", escapeHtml(userName))
                .replace("${role}", escapeHtml(role))
                .replace("${contextPath}", req.getContextPath())
                .replace("${widgetId}", escapeHtml(widgetId))
                .replace("${widgetName}", escapeHtml(widgetName));

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
}
