package com.sim.chatserver.web.admin;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
import java.io.IOException;
public class ForumDatabaseExplorerServlet extends HttpServlet {

    private static final ForumDatabaseExplorerService SERVICE = new ForumDatabaseExplorerService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        SERVICE.handlePost(req, resp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}