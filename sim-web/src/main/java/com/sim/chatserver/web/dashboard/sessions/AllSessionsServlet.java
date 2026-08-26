package com.sim.chatserver.web.dashboard.sessions;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AllSessionsServlet", urlPatterns = {
    "/dashboard/sessions/data",
    "/dashboard/sessions/chats",
    "/dashboard/sessions/select"
})
public class AllSessionsServlet extends HttpServlet {

    private static final AllSessionsService SERVICE = new AllSessionsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handleGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handlePost(req, resp);
    }
}
