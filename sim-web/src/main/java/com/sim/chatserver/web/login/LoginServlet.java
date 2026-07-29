package com.sim.chatserver.web.login;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

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
    private static final Object USER_SERVICE_LOCK = new Object();
    private static volatile UserService configuredUserService;

    UserService userService;

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

        String username = sanitizeUsername(firstParam(req, "username"));
        String password = sanitizePassword(firstParam(req, "password"));
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

    private UserService resolveUserService() {
        if (userService != null) {
            return userService;
        }

        UserService service = configuredUserService;
        if (service != null) {
            return service;
        }

        synchronized (USER_SERVICE_LOCK) {
            service = configuredUserService;
            if (service != null) {
                return service;
            }

            try {
                service = CDI.current().select(UserService.class).get();
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "CDI UserService lookup failed", ex);
                throw new IllegalStateException(
                        "UserService is unavailable. WildFly-managed datasource/JPA model requires CDI wiring.",
                        ex);
            }

            configuredUserService = service;
            return service;
        }
    }

    private String firstParam(HttpServletRequest req, String name) {
        return RequestParamContext.from(req).first(name);
    }

    private static final class RequestParamContext {

        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String value = request.getParameter(name);
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
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
