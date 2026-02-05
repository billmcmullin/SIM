package com.sim.chatserver.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewServlet", urlPatterns = {"/admin/widgets/review"})
public class WidgetReviewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/widget_review.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String selectionId = req.getParameter("selectionId");
        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "selectionId required.");
            return;
        }

        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Selection not found.");
            return;
        }

        String subjectLabel = selection.displayName == null || selection.displayName.isBlank()
                ? selection.widgetId
                : selection.displayName;
        String subjectType = selection.hasSnapshots() ? "Term" : "Widget";
        String backLink = selection.getBackUrl();
        if (backLink == null || backLink.isBlank()) {
            backLink = req.getContextPath() + "/admin/widgets/view?widgetId=" + selection.widgetId;
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String userName = String.valueOf(session.getAttribute("user"));
        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();
        String rendered = template
                .replace("${user}", escapeHtml(userName))
                .replace("${role}", escapeHtml(role))
                .replace("${contextPath}", req.getContextPath())
                .replace("${widgetName}", escapeHtml(subjectLabel))
                .replace("${widgetId}", escapeHtml(selection.widgetId))
                .replace("${selectionId}", escapeHtml(selectionId))
                .replace("${subjectType}", escapeHtml(subjectType))
                .replace("${subjectLabel}", escapeHtml(subjectLabel))
                .replace("${backLink}", escapeHtml(backLink));

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
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
}
