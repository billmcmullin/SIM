package com.sim.chatserver.web.profile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

import com.sim.chatserver.model.CustomerProfile;
import com.sim.chatserver.model.CustomerProfileStore;
import com.sim.chatserver.salesforce.SalesforceClient;
import com.sim.chatserver.salesforce.SalesforceCustomerMatch;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "SyncCustomerProfileSalesforceServlet", urlPatterns = {"/admin/sync-customer-profile"})
public class SyncCustomerProfileSalesforceServlet extends HttpServlet {

    private static final SalesforceClient SALESFORCE_CLIENT = new SalesforceClient();
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final Pattern FRIENDLY_NAME_PATTERN = Pattern.compile("[\\p{L}\\p{N} .,'_-]{1,128}");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, errorPayload("Authentication required."));
            return;
        }

        String sessionId = trimToNull(ServletRequestParamUtil.firstParam(req, "sessionId", 128, true, true));
        String friendlyName = trimToNull(ServletRequestParamUtil.firstParam(req, "friendlyName", 128, true, true));

        if (sessionId != null && !SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, errorPayload("Invalid sessionId format."));
            return;
        }
        if (friendlyName != null && !FRIENDLY_NAME_PATTERN.matcher(friendlyName).matches()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, errorPayload("Invalid friendlyName format."));
            return;
        }

        if (sessionId == null && friendlyName == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    errorPayload("sessionId or friendlyName is required."));
            return;
        }

        try {
            // If friendly name not provided, try cache lookup by session id first
            if (friendlyName == null) {
                CustomerProfile existing = CustomerProfileStore.loadBySessionId(sessionId);
                if (existing != null) {
                    friendlyName = trimToNull(existing.getFriendlyName());
                }
            }

            // If still no friendly name, use sessionId as fallback search token (least ideal)
            String searchName = friendlyName != null ? friendlyName : sessionId;

            SalesforceCustomerMatch match;
            try {
                match = SALESFORCE_CLIENT.findBestCustomerMatch(searchName);
            } catch (SalesforceClient.SalesforceClientException sce) {
                logFailure("Salesforce request failed while syncing customer profile", sce);
                writeJson(resp, sce.getStatusCode(), errorPayload("Salesforce request failed."));
                return;
            } catch (IllegalStateException ise) {
                logFailure("Invalid Salesforce request state while syncing customer profile", ise);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    errorPayload("Invalid Salesforce request state."));
                return;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logFailure("Salesforce lookup transport or persistence failure", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    errorPayload("Unable to query Salesforce right now."));
                return;
            } catch (IOException | java.sql.SQLException ex) {
                logFailure("Salesforce lookup transport or persistence failure", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    errorPayload("Unable to query Salesforce right now."));
                return;
            } catch (Throwable ex) {
                logFailure("Unexpected Salesforce lookup failure while syncing customer profile", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    errorPayload("Unable to query Salesforce right now."));
                return;
            }

            if (match == null) {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    errorPayload("No Salesforce record matched this customer."));
                return;
            }

            CustomerProfile profile = new CustomerProfile();
            profile.setSessionId(sessionId != null ? sessionId : "friendly:" + searchName);
            profile.setFriendlyName(match.getName() != null ? match.getName() : friendlyName);
            profile.setSalesforceContactId(match.getContactId());
            profile.setSalesforceAccountId(match.getAccountId());
            profile.setEmail(match.getEmail());
            profile.setPhone(match.getPhone());
            profile.setTitle(match.getTitle());
            profile.setDepartment(match.getDepartment());
            profile.setRawJson(match.getRawJson());
            profile.setLastSyncedAt(OffsetDateTime.now(ZoneOffset.UTC));

            CustomerProfileStore.upsert(profile);

            JsonObject profileJson = Json.createObjectBuilder()
                    .add("sessionId", nullToEmpty(profile.getSessionId()))
                    .add("friendlyName", nullToEmpty(profile.getFriendlyName()))
                    .add("salesforceContactId", nullToEmpty(profile.getSalesforceContactId()))
                    .add("salesforceAccountId", nullToEmpty(profile.getSalesforceAccountId()))
                    .add("email", nullToEmpty(profile.getEmail()))
                    .add("phone", nullToEmpty(profile.getPhone()))
                    .add("title", nullToEmpty(profile.getTitle()))
                    .add("department", nullToEmpty(profile.getDepartment()))
                    .add("lastSyncedAt", profile.getLastSyncedAt() == null ? "" : profile.getLastSyncedAt().toString())
                    .build();

            JsonObject ok = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "Customer profile synced successfully.")
                    .add("profile", profileJson)
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, ok);
        } catch (Throwable e) {
            throw new ServletException("Unable to sync customer profile from Salesforce", e);
        }
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            JsonWriter writer = Json.createWriter(resp.getWriter());
            try {
                writer.writeObject(payload);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            logFailure("Unable to write sync-customer-profile response", e);
            throw new IllegalStateException("Unable to write response", e);
        }
    }

    private JsonObject errorPayload(String message) {
        return Json.createObjectBuilder()
                .add("status", "error")
                .add("message", nullToEmpty(message))
                .build();
    }

    private void logFailure(String message, Throwable error) {
        java.util.logging.Logger.getLogger(SyncCustomerProfileSalesforceServlet.class.getName())
                .log(java.util.logging.Level.FINE, message, error);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
