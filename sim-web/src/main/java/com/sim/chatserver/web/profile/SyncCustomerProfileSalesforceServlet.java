package com.sim.chatserver.web.profile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

import com.sim.chatserver.model.CustomerProfile;
import com.sim.chatserver.model.CustomerProfileStore;
import com.sim.chatserver.salesforce.SalesforceClient;
import com.sim.chatserver.salesforce.SalesforceCustomerMatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "SyncCustomerProfileSalesforceServlet", urlPatterns = {"/admin/sync-customer-profile"})
public class SyncCustomerProfileSalesforceServlet extends HttpServlet {

    private final SalesforceClient salesforceClient = new SalesforceClient();
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final Pattern FRIENDLY_NAME_PATTERN = Pattern.compile("[\\p{L}\\p{N} .,'_-]{1,128}");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return;
        }

        String sessionId = trimToNull(req.getParameter("sessionId"));
        String friendlyName = trimToNull(req.getParameter("friendlyName"));

        if (sessionId != null && !SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                "{\"status\":\"error\",\"message\":\"Invalid sessionId format.\"}");
            return;
        }
        if (friendlyName != null && !FRIENDLY_NAME_PATTERN.matcher(friendlyName).matches()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                "{\"status\":\"error\",\"message\":\"Invalid friendlyName format.\"}");
            return;
        }

        if (sessionId == null && friendlyName == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"error\",\"message\":\"sessionId or friendlyName is required.\"}");
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
                match = salesforceClient.findBestCustomerMatch(searchName);
            } catch (SalesforceClient.SalesforceClientException sce) {
                writeJson(resp, sce.getStatusCode(),
                        "{\"status\":\"error\",\"message\":\"Salesforce request failed.\"}");
                return;
            } catch (IllegalStateException ise) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"status\":\"error\",\"message\":\"Invalid Salesforce request state.\"}");
                return;
            }

            if (match == null) {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                        "{\"status\":\"error\",\"message\":\"No Salesforce record matched this customer.\"}");
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

            writeJson(resp, HttpServletResponse.SC_OK,
                    "{"
                    + "\"status\":\"ok\","
                    + "\"message\":\"Customer profile synced successfully.\","
                    + "\"profile\":{"
                    + "\"sessionId\":\"" + escapeJson(nullToEmpty(profile.getSessionId())) + "\","
                    + "\"friendlyName\":\"" + escapeJson(nullToEmpty(profile.getFriendlyName())) + "\","
                    + "\"salesforceContactId\":\"" + escapeJson(nullToEmpty(profile.getSalesforceContactId())) + "\","
                    + "\"salesforceAccountId\":\"" + escapeJson(nullToEmpty(profile.getSalesforceAccountId())) + "\","
                    + "\"email\":\"" + escapeJson(nullToEmpty(profile.getEmail())) + "\","
                    + "\"phone\":\"" + escapeJson(nullToEmpty(profile.getPhone())) + "\","
                    + "\"title\":\"" + escapeJson(nullToEmpty(profile.getTitle())) + "\","
                    + "\"department\":\"" + escapeJson(nullToEmpty(profile.getDepartment())) + "\","
                    + "\"lastSyncedAt\":\"" + escapeJson(profile.getLastSyncedAt().toString()) + "\""
                    + "}"
                    + "}");
        } catch (Exception e) {
            throw new ServletException("Unable to sync customer profile from Salesforce", e);
        }
    }

    private void writeJson(HttpServletResponse resp, int status, String payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(payload);
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

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
