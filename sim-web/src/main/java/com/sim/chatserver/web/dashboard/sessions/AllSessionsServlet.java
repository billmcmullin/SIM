package com.sim.chatserver.web.dashboard.sessions;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
import java.io.IOException;
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
