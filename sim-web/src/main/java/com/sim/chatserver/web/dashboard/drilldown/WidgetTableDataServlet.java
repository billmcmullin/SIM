package com.sim.chatserver.web.dashboard.drilldown;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
import java.io.IOException;
@WebServlet(name = "WidgetTableDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/data"})
public class WidgetTableDataServlet extends HttpServlet {

    private static final WidgetTableDataService SERVICE = new WidgetTableDataService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handleGet(req, resp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
