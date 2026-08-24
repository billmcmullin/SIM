package com.sim.chatserver.web.dashboard.drilldown;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "WidgetReviewDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/review-data"})
public class WidgetReviewDataServlet extends HttpServlet {

    private final transient WidgetReviewDataService service = new WidgetReviewDataService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        service.handleGet(req, resp);
    }
}
