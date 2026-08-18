package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return;
        }

        String workspaceName = ServletRequestParamUtil.firstParam(req, "workspaceName", 512, true, true);
        if (workspaceName == null) {
            workspaceName = "";
        }
        workspaceName = workspaceName.trim();

        try {
            EncryptedDbConfigStore.saveWorkspaceName(workspaceName);
            ServletJsonResponseUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Json.createObjectBuilder()
                            .add("status", "ok")
                            .add("workspaceName", workspaceName)
                            .build());
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Unable to save workspace name", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save workspace name.");
        }
    
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

}
