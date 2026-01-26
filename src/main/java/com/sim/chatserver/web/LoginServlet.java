package com.sim.chatserver.web;

import java.io.IOException;

import com.sim.chatserver.service.UserService;

import jakarta.inject.Inject;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userService.ensureAdminExists(); // make sure admin/admin exists
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=missing");
            return;
        }

        if (!userService.authenticate(username, password)) {
            resp.sendRedirect(req.getContextPath() + "/login?error=invalid");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", username);
        session.setMaxInactiveInterval(30 * 60);
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
