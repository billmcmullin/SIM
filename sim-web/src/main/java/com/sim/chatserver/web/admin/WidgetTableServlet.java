package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.service.admin.CheckResult;
import com.sim.chatserver.service.admin.WidgetTableAdminQueryService;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "WidgetTableServlet", urlPatterns = {"/admin/widgets/table-check"})
public class WidgetTableServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableServlet.class.getCanonicalName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private static final int DEFAULT_PARAM_MAX_LEN = 256;
    private static final int BULK_IDS_PARAM_MAX_LEN = 8192;
    private static final WidgetTableAdminQueryService QUERY_SERVICE = new WidgetTableAdminQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
        // support either single widgetId or bulk ids (comma-separated widgetIds)
        String idsParam = firstParam(req, "ids", BULK_IDS_PARAM_MAX_LEN);
        String widgetId = firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID);

        if ((idsParam == null || idsParam.isBlank()) && (widgetId == null || widgetId.isBlank())) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Provide 'widgetId' or 'ids' query parameter.");
            return;
        }

        if (idsParam != null && !idsParam.isBlank()) {
            // bulk mode
            List<String> widgetIds = Arrays.stream(idsParam.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            handleBulkCheck(resp, widgetIds);
        } else {
            handleCheck(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
        // Support creating a table for a widget via POST widgetId=...
        handleCreate(req, resp);
    }

    private boolean authorizeAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return false;
        }
        String role = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE) == null ? "" : String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE));
        if (!com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            jsonError(resp, HttpServletResponse.SC_FORBIDDEN, com.sim.chatserver.util.CommonLiteralConstants.MSG_ADMIN_ROLE_REQUIRED);
            return false;
        }
        return true;
    }

    // Handle single widget check (existing behavior, extended to include count when exists)
    private void handleCheck(HttpServletRequest req, HttpServletResponse resp) {
        String widgetId = firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID);
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        if (!isSafeIdentifier(tableName)) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId.");
            return;
        }
        try {
            CheckResult status = QUERY_SERVICE.checkTable(tableName);
            writeSingleResponse(resp,
                    HttpServletResponse.SC_OK,
                    widgetId,
                    tableName,
                    status.exists,
                    status.count,
                    status.message,
                    false);
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to check widget table", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to inspect the database.");
        }
    }

    // Handle bulk checks: return JSON array of statuses
    private void handleBulkCheck(HttpServletResponse resp, List<String> widgetIds) {
        JsonArrayBuilder statuses = Json.createArrayBuilder();

        try {
            Map<String, String> widgetToTable = new LinkedHashMap<>();
            List<String> validTables = new ArrayList<>();

            for (String wid : widgetIds) {
                String tableName = sanitizeWidgetId(wid);
                if (!isSafeIdentifier(tableName)) {
                    JsonObjectBuilder statusBody = Json.createObjectBuilder()
                            .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, wid == null ? "" : wid)
                            .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_TABLE_NAME, tableName)
                            .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_TABLE_EXISTS, false)
                            .addNull(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT)
                            .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Invalid widget identifier.");
                    statuses.add(statusBody);
                    continue;
                }

                widgetToTable.put(wid, tableName);
                validTables.add(tableName);
            }

            Map<String, CheckResult> checks = QUERY_SERVICE.checkTables(validTables);

            for (Map.Entry<String, String> entry : widgetToTable.entrySet()) {
                String wid = entry.getKey();
                String tableName = entry.getValue();
                CheckResult result = checks.get(tableName);

                JsonObjectBuilder statusBody = Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, wid == null ? "" : wid)
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_TABLE_NAME, tableName)
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_TABLE_EXISTS, result != null && result.exists)
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "");
                Object resultCount = result == null ? null : result.count;
                if (resultCount == null) {
                    statusBody.addNull(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT);
                } else {
                    OptionalLong parsedCount = parseNullableLong(resultCount);
                    if (parsedCount.isEmpty()) {
                        statusBody.addNull(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT);
                    } else {
                        statusBody.add(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT, parsedCount.getAsLong());
                    }
                }
                statuses.add(statusBody);
            }

                JsonObjectBuilder payload = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                    .add("statuses", statuses);
                writeJson(resp, HttpServletResponse.SC_OK, payload.build());
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to check widget tables (bulk)", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to check widget table statuses.");
        }
    }

    /**
     * Create table for widget (POST). Expects parameter widgetId. If table
     * already exists, returns created=false and message. If created
     * successfully, returns created=true.
     */
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) {
        String widgetId = firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID);
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        if (!isSafeIdentifier(tableName)) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId.");
            return;
        }
        try {
            CheckResult result = QUERY_SERVICE.createTableIfMissing(tableName);
            if (!result.created) {
                writeSingleResponse(resp,
                        HttpServletResponse.SC_OK,
                        widgetId,
                        tableName,
                        true,
                        result.count,
                        "Table already exists.",
                        false);
                return;
            }

                writeSingleResponse(resp,
                    HttpServletResponse.SC_OK,
                    widgetId,
                    tableName,
                    true,
                    result.count,
                    "Table created successfully.",
                    true);
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to create widget table", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create the table.");
        }
    }

    private String sanitizeWidgetId(String widgetId) {
        String normalized = widgetId.trim().replaceAll(com.sim.chatserver.util.CommonLiteralConstants.REGEX_NON_ALNUM_UNDERSCORE, "_");
        if (normalized.isEmpty()) {
            normalized = com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET;
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = new String(normalized.toCharArray(), 0, 60);
        }
        return normalized;
    }

    private boolean isSafeIdentifier(String identifier) {
        return identifier != null && SAFE_SQL_IDENTIFIER.matcher(identifier).matches();
    }

    private String firstParam(HttpServletRequest req, String name) {
        return firstParam(req, name, DEFAULT_PARAM_MAX_LEN);
    }

    private String firstParam(HttpServletRequest req, String name, int maxLen) {
        String value = ServletRequestParamUtil.firstParam(
                req,
                name,
                maxLen > 0 ? maxLen : DEFAULT_PARAM_MAX_LEN,
                true,
                false);
        return value == null ? "" : value;
    }

    private void jsonError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_ERROR_RESPONSE, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, jakarta.json.JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_RESPONSE, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writeSingleResponse(HttpServletResponse resp,
            int status,
            String widgetId,
            String tableName,
            boolean exists,
            Object count,
            String message,
            boolean created) {
        JsonObjectBuilder body = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, widgetId == null ? "" : widgetId)
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_TABLE_NAME, tableName == null ? "" : tableName)
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_TABLE_EXISTS, exists)
                .add("created", created)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, message == null ? "" : message);
        if (count == null) {
            body.addNull(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT);
        } else {
            OptionalLong parsedCount = parseNullableLong(count);
            if (parsedCount.isEmpty()) {
                body.addNull(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT);
            } else {
                body.add(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT, parsedCount.getAsLong());
            }
        }
        writeJson(resp, status, body.build());
    }

    private OptionalLong parseNullableLong(Object value) {
        if (value == null) {
            return OptionalLong.empty();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(text));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid count value", ex);
            return OptionalLong.empty();
        }
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
