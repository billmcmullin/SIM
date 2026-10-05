package com.sim.chatserver.web.admin;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ForumDatabaseExplorerServlet extends HttpServlet {

    private static final ForumDatabaseExplorerService SERVICE = new ForumDatabaseExplorerService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handlePost(req, resp);
    }
}