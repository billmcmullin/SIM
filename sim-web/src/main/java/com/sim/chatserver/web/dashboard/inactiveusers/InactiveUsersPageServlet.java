package com.sim.chatserver.web.dashboard.inactiveusers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "InactiveUsersPageServlet", urlPatterns = {"/dashboard/inactive-users"})
public class InactiveUsersPageServlet extends HttpServlet {

    private static final InactiveUsersPageService SERVICE = new InactiveUsersPageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handleGet(req, resp);
    }
}
