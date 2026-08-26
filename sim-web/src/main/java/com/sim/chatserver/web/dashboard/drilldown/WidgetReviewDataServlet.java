package com.sim.chatserver.web.dashboard.drilldown;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "WidgetReviewDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/review-data"})
public class WidgetReviewDataServlet extends HttpServlet {

    private static final WidgetReviewDataService SERVICE = new WidgetReviewDataService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handleGet(req, resp);
    }
}
