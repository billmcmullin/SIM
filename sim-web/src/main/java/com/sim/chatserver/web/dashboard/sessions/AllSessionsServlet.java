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

    private final transient AllSessionsService service = new AllSessionsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        service.handleGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        service.handlePost(req, resp);
    }
}
