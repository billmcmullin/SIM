package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;

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

        RequestParamContext requestContext = RequestParamContext.from(req);
        String workspaceName = requestContext.first("workspaceName", 512);
        if (workspaceName == null) {
            workspaceName = "";
        }
        workspaceName = workspaceName.trim();

        try {
            EncryptedDbConfigStore.saveWorkspaceName(workspaceName);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\",\"workspaceName\":\"" + escapeJson(workspaceName) + "\"}");
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Unable to save workspace name", e);
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

    private static final class RequestParamContext {
        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        String first(String name, int maxLen) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String[] values = request.getParameterValues(name);
            if (values == null || values.length == 0) {
                return null;
            }
            for (String value : values) {
                String normalized = normalize(value, maxLen);
                if (normalized != null) {
                    return normalized;
                }
            }
            return null;
        }

        private String normalize(String value, int maxLen) {
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            int effectiveMax = maxLen <= 0 ? 512 : maxLen;
            return trimmed.length() > effectiveMax ? trimmed.substring(0, effectiveMax) : trimmed;
        }
    }
}
