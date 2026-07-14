package com.sim.chatserver.web.login;

import java.io.IOException;
import java.util.regex.Pattern;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"", "/login"})
public class LoginServlet extends HttpServlet {

    @Inject
    UserService userService;

    private static final String VIEW = "/WEB-INF/views/login.html";
    private static final Pattern SAFE_USERNAME = Pattern.compile("^[A-Za-z0-9._@-]{1,128}$");

    @Override
    public void init() throws ServletException {
        super.init();

        // Fallback for non-CDI runtime (plain Jetty in UI tests)
        if (userService == null) {
            ServletContext ctx = getServletContext();

            AppDataSourceHolder dsHolder = (AppDataSourceHolder) ctx.getAttribute("appDataSourceHolder");
            if (dsHolder == null) {
                dsHolder = new AppDataSourceHolder();
                dsHolder.init(); // uses DB_* env vars; fails fast if missing
                ctx.setAttribute("appDataSourceHolder", dsHolder);
            }

            UserService manual = new UserService();
            manual.setDsHolder(dsHolder);
            this.userService = manual;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userService.ensureAdminExists(); // creates admin/admin if absent

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            forwardSafe(req, resp, "/dashboard");
            return;
        }

        forwardSafe(req, resp, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = sanitizeUsername(firstParam(req, "username"));
        String password = sanitizePassword(firstParam(req, "password"));
        if (username == null || password == null) {
            req.setAttribute("loginError", "missing");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            forwardSafe(req, resp, VIEW);
            return;
        }

        UserAccount authenticatedUser = userService.authenticateAndGetUser(username, password);
        if (authenticatedUser == null || authenticatedUser.getUsername() == null || authenticatedUser.getUsername().isBlank()) {
            req.setAttribute("loginError", "invalid");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            forwardSafe(req, resp, VIEW);
            return;
        }

        String sessionUser = sanitizeUsername(authenticatedUser.getUsername());
        if (sessionUser == null) {
            req.setAttribute("loginError", "invalid");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            forwardSafe(req, resp, VIEW);
            return;
        }

        HttpSession existing = req.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", sessionUser);
        session.setMaxInactiveInterval(30 * 60);
        forwardSafe(req, resp, "/dashboard");
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String[] values = req.getParameterValues(name);
        if (values == null || values.length == 0) {
            return null;
        }
        String value = values[0];
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
    }

    private void forwardSafe(HttpServletRequest req, HttpServletResponse resp, String target)
            throws ServletException, IOException {
        if (target == null || target.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!VIEW.equals(target) && !"/dashboard".equals(target)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        req.getRequestDispatcher(target).forward(req, resp);
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
