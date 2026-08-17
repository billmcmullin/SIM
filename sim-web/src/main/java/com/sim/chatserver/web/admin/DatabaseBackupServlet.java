package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin data export backup endpoint.
 */
public class DatabaseBackupServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DatabaseBackupServlet.class.getName());

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";
    private static final DateTimeFormatter BACKUP_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)
            .withZone(ZoneOffset.UTC);

    private final DatabaseBackupService backupService = new DatabaseBackupService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req)) {
            sendErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            return;
        }

        String generatedAt = BACKUP_TS_FMT.format(Instant.now());
        String fileName = "chatserver-data-backup-" + generatedAt + ".zip";

        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
            backupService.exportBackup(resp.getOutputStream(), generatedAt);
        } catch (IOException | IllegalStateException e) {
            log.log(Level.SEVERE, "Data backup export failed", e);
            if (resp != null && !resp.isCommitted()) {
                resp.reset();
                sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Data export failed.");
            }
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? "" : String.valueOf(roleObj);
        return "ADMIN".equalsIgnoreCase(role);
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to send backup servlet error response", e);
        }
    }
}
