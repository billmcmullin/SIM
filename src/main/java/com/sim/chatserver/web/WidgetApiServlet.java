package com.sim.chatserver.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import com.sim.chatserver.widget.WidgetStore.DuplicateWidgetIdException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetApiServlet", urlPatterns = {"/admin/widgets"})
public class WidgetApiServlet extends HttpServlet {

    private static final String APPLICATION_JSON = "application/json";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        String filter = req.getParameter("filter");
        resp.setContentType(APPLICATION_JSON);
        try {
            List<WidgetEntry> widgets = WidgetStore.list(filter);
            resp.getWriter().write("{\"status\":\"ok\",\"widgets\":["
                    + widgets.stream().map(this::widgetToJson).collect(Collectors.joining(",")) + "]}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load widget entries.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        String widgetId = req.getParameter("widgetId");
        String displayName = req.getParameter("displayName");
        String idValue = req.getParameter("id");

        resp.setContentType(APPLICATION_JSON);

        if (widgetId == null || widgetId.isBlank() || displayName == null || displayName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Widget ID and name are required.\"}");
            return;
        }

        Integer id = null;
        if (idValue != null && !idValue.isBlank()) {
            try {
                id = Integer.valueOf(idValue.trim());
            } catch (NumberFormatException ignored) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"ID must be an integer.\"}");
                return;
            }
        }

        try {
            WidgetEntry saved = WidgetStore.save(id, widgetId, displayName);
            resp.getWriter().write("{\"status\":\"ok\",\"widget\":" + widgetToJson(saved) + "}");
        } catch (DuplicateWidgetIdException ex) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
        } catch (IllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
        } catch (SQLException ex) {
            log("Widget persistence error", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to persist widget entry: "
                    + escapeJson(ex.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        String idsParam = req.getParameter("ids");
        resp.setContentType(APPLICATION_JSON);

        if (idsParam == null || idsParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No widget IDs provided to delete.\"}");
            return;
        }

        List<Integer> ids = Arrays.stream(idsParam.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No valid widget IDs provided.\"}");
            return;
        }

        try {
            int deleted = WidgetStore.deleteBulk(ids);
            resp.getWriter().write("{\"status\":\"ok\",\"deleted\":" + deleted + "}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to delete widget entries.\"}");
        }
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private String widgetToJson(WidgetEntry entry) {
        return "{"
                + "\"id\":" + entry.getId() + ","
                + "\"widgetId\":\"" + escapeJson(entry.getWidgetId()) + "\","
                + "\"displayName\":\"" + escapeJson(entry.getDisplayName()) + "\""
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
