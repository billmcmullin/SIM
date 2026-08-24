package com.sim.chatserver.web.dashboard.drilldown;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "WidgetTableDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/data"})
public class WidgetTableDataServlet extends HttpServlet {

    private final transient WidgetTableDataService service = new WidgetTableDataService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        service.handleGet(req, resp);
    }
}
