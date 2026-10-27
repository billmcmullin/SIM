package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sim.chatserver.service.WorkflowRuntime;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "RuntimeHealthServlet", urlPatterns = {"/health/live", "/health/ready"})
public class RuntimeHealthServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(RuntimeHealthServlet.class.getCanonicalName());

    private final void readObject(ObjectInputStream input) throws IOException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        boolean ready = "/health/live".equals(request.getServletPath()) || isReady();
        try {
            ServletJsonResponseUtil.writeJson(response, ready ? 200 : 503,
                    Json.createObjectBuilder().add("status", ready ? "UP" : "DOWN").build());
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Unable to write health response", ex);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
    }

    private boolean isReady() {
        if (!WorkflowRuntime.isInitialized()) {
            return false;
        }
        try (Connection connection = openConnection()) {
            return connection != null && connection.isValid(2);
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "Database readiness validation failed", ex);
            return false;
        }
    }

    private Connection openConnection() {
        try {
            return CDI.current().select(AppDataSourceHolder.class).get().getDataSource().getConnection();
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "Unable to open readiness connection", ex);
            return null;
        } catch (IllegalStateException ex) {
            LOG.log(Level.FINE, "Readiness datasource is unavailable", ex);
            return null;
        }
    }
}