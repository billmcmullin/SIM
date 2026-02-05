package com.sim.chatserver.web;

import java.io.IOException;
import java.util.logging.Logger;

import com.sim.chatserver.config.ConfigStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WorkspaceConfigServlet", urlPatterns = {"/admin/workspace"})
public class WorkspaceConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WorkspaceConfigServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Admin role required.\"}");
            return;
        }

        String workspaceName = req.getParameter("workspaceName");
        if (workspaceName == null) {
            workspaceName = "";
        }
        workspaceName = workspaceName.trim();

        try {
            ConfigStore.saveWorkspaceName(workspaceName);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\",\"workspaceName\":\"" + escapeJson(workspaceName) + "\"}");
        } catch (Exception e) {
            log.severe("Unable to save workspace name: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to save workspace name.\"}");
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
