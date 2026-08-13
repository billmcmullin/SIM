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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req == null || resp == null) {
            return;
        }

        resolveUserService().ensureAdminExists(); // creates admin/admin if absent

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            req.getRequestDispatcher("/dashboard").forward(req, resp);
            return;
        }

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req == null || resp == null) {
            return;
        }

        String username = sanitizeUsername(ServletRequestParamUtil.firstParam(req, "username", 256, true, true));
        String password = sanitizePassword(ServletRequestParamUtil.firstParam(req, "password", 256, true, true));
        if (username == null || password == null) {
            req.setAttribute("loginError", "missing");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        UserAccount authenticatedUser = resolveUserService().authenticateAndGetUser(username, password);
        if (authenticatedUser == null || authenticatedUser.getUsername() == null || authenticatedUser.getUsername().isBlank()) {
            req.setAttribute("loginError", "invalid");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        String sessionUser = sanitizeUsername(authenticatedUser.getUsername());
        if (sessionUser == null) {
            req.setAttribute("loginError", "invalid");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        HttpSession existing = req.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", sessionUser);
        session.setMaxInactiveInterval(30 * 60);
        String contextPath = req.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }
        resp.sendRedirect(contextPath + "/dashboard");
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
