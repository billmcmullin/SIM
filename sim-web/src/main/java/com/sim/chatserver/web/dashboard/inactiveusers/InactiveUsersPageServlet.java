package com.sim.chatserver.web.dashboard.inactiveusers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "InactiveUsersPageServlet", urlPatterns = {"/dashboard/inactive-users"})
public class InactiveUsersPageServlet extends HttpServlet {

    private final transient InactiveUsersPageService service = new InactiveUsersPageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        service.handleGet(req, resp);
    }
}
