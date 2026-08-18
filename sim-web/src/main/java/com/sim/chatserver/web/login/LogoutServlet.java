package com.sim.chatserver.web.login;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            var s = req.getSession(false);
            if (s != null) {
                s.invalidate();
            }
            safeForwardToLogin(req, resp);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private void safeForwardToLogin(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.getRequestDispatcher("/login").forward(req, resp);
        } catch (IOException | ServletException ex) {
            log.log(Level.WARNING, "Logout forward failed", ex);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }
}
