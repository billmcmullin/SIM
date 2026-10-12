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

import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "WorkspaceConfigServlet", urlPatterns = {"/admin/workspace"})
public class WorkspaceConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WorkspaceConfigServlet.class.getCanonicalName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return;
        }
        String role = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE) == null ? "" : session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE).toString();
        if (!com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, com.sim.chatserver.util.CommonLiteralConstants.MSG_ADMIN_ROLE_REQUIRED);
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
                            .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                            .add("workspaceName", workspaceName)
                            .build());
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Unable to save workspace name", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save workspace name.");
        }
    
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
