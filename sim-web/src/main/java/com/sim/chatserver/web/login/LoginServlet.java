package com.sim.chatserver.web.login;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"", "/login"})
public class LoginServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(LoginServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/login.html";
    private static final Pattern SAFE_USERNAME = Pattern.compile("^[A-Za-z0-9._@-]{1,128}$");

    @Override
    public void init() throws ServletException {
        super.init();
        resolveUserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null || resp == null) {
            return;
        }

        resolveUserService().ensureAdminExists(); // creates admin/admin if absent

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            safeForward(req, resp, "/dashboard");
            return;
        }

        safeForward(req, resp, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null || resp == null) {
            return;
        }

        String username = sanitizeUsername(ServletRequestParamUtil.firstParam(req, "username", 256, true, true));
        String password = sanitizePassword(ServletRequestParamUtil.firstParam(req, "password", 256, true, true));
        if (username == null || password == null) {
            req.setAttribute("loginError", "missing");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            safeForward(req, resp, VIEW);
            return;
        }

        UserAccount authenticatedUser = resolveUserService().authenticateAndGetUser(username, password);
        if (authenticatedUser == null || authenticatedUser.getUsername() == null || authenticatedUser.getUsername().isBlank()) {
            req.setAttribute("loginError", "invalid");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            safeForward(req, resp, VIEW);
            return;
        }

        String sessionUser = sanitizeUsername(authenticatedUser.getUsername());
        if (sessionUser == null) {
            req.setAttribute("loginError", "invalid");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            safeForward(req, resp, VIEW);
            return;
        }

        HttpSession existing = req.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", sessionUser);
        session.setMaxInactiveInterval(30 * 60);

        String redirectTarget = buildDashboardRedirect(req);
        if (redirectTarget == null) {
            safeSendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid redirect target.");
            return;
        }
        safeRedirect(resp, redirectTarget);
    }

    private void safeForward(HttpServletRequest req, HttpServletResponse resp, String target) {
        if (req == null || resp == null || target == null || target.isBlank()) {
            return;
        }
        try {
            RequestDispatcher dispatcher = req.getRequestDispatcher(target);
            if (dispatcher == null) {
                safeSendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to dispatch request.");
                return;
            }
            dispatcher.forward(req, resp);
        } catch (IOException | ServletException ex) {
            log.log(Level.WARNING, "Login request forward failed", ex);
            safeSendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private void safeRedirect(HttpServletResponse resp, String redirectTarget) {
        if (resp == null || redirectTarget == null || redirectTarget.isBlank()) {
            return;
        }
        try {
            resp.sendRedirect(resp.encodeRedirectURL(redirectTarget));
        } catch (IOException ex) {
            log.log(Level.WARNING, "Login redirect failed", ex);
            safeSendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private void safeSendError(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send login error response", ex);
        }
    }

    private String buildDashboardRedirect(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        String contextPath = req.getContextPath();
        if (contextPath == null || contextPath.isBlank()) {
            return "/dashboard";
        }
        String trimmed = contextPath.trim();
        if (!trimmed.startsWith("/") || trimmed.contains(":") || trimmed.contains("//")) {
            return null;
        }
        return trimmed + "/dashboard";
    }

    protected UserService resolveUserService() {
        try {
            return CDI.current().select(UserService.class).get();
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "CDI UserService lookup failed", ex);
            throw new IllegalStateException(
                    "UserService is unavailable. WildFly-managed datasource/JPA model requires CDI wiring.",
                    ex);
        }
    }

    private String sanitizeUsername(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || value.length() > 128 || !SAFE_USERNAME.matcher(value).matches()) {
            return null;
        }
        return value;
    }

    private String sanitizePassword(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || value.length() > 256) {
            return null;
        }
        return value;
    }
}
