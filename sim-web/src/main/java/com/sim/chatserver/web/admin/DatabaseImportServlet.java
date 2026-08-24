package com.sim.chatserver.web.admin;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
public class DatabaseImportServlet extends HttpServlet {

    private final transient DatabaseImportService service = new DatabaseImportService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        service.handlePost(req, resp);
    }
}
