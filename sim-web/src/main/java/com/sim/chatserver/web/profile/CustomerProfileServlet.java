package com.sim.chatserver.web.profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.model.CustomerIdentity;
import com.sim.chatserver.model.CustomerIdentitySessionLink;
import com.sim.chatserver.model.CustomerProfile;
import com.sim.chatserver.model.CustomerProfileStore;
import com.sim.chatserver.service.CustomerIdentityService;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CustomerProfileServlet", urlPatterns = {"/customer-profile"})
public class CustomerProfileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CustomerProfileServlet.class.getName());

    private static final String TEMPLATE_PATH = "/WEB-INF/views/customer_profile.html";
    private static final String LOGIN_PATH = "/login";
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final Pattern FRIENDLY_NAME_PATTERN = Pattern.compile("[\\p{L}\\p{N} .,'_-]{1,128}");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(LOGIN_PATH);
            return;
        }

        String sessionId = trimToNull(ServletRequestParamUtil.firstParam(req, "sessionId", 256, true, true));
        String friendlyNameParam = trimToNull(ServletRequestParamUtil.firstParam(req, "friendlyName", 256, true, true));

        if (sessionId != null && !SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid sessionId format.");
            return;
        }
        if (friendlyNameParam != null && !FRIENDLY_NAME_PATTERN.matcher(friendlyNameParam).matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid friendlyName format.");
            return;
        }

        if (sessionId == null && friendlyNameParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "sessionId or friendlyName is required.");
            return;
        }

        try {
            // Resolve a sessionId if only friendlyName was passed (best effort)
            String resolvedSessionId = sessionId;
            if (resolvedSessionId == null) {
                // Current identity service is sessionId-centric; keep old behavior for now
                resolvedSessionId = "friendly:" + friendlyNameParam;
            }

            CustomerIdentity identity = null;
            List<CustomerIdentitySessionLink> linkedSessions = List.of();

            if (sessionId != null) {
                CustomerIdentityService identityService = identityService();
                identity = identityService.resolveOrCreateBySessionId(sessionId);
                Long identityIdObject = identity == null ? null : identity.getIdentityId();
                if (identityIdObject != null) {
                    long identityId = identityIdObject.longValue();
                    linkedSessions = identityService.listLinkedSessions(identityId);
                }
            }

            CustomerProfile profile = null;
            if (sessionId != null) {
                profile = CustomerProfileStore.loadBySessionId(sessionId);
            }

            String friendlyName = firstNonBlank(
                    profile != null ? profile.getFriendlyName() : null,
                    identity != null ? identity.getCanonicalName() : null,
                    friendlyNameParam
            );

            String contextPath = ServletPathUtil.safeContextPathStrict(req.getContextPath());
            String linkedSessionsHtml = buildLinkedSessionsRows(linkedSessions, contextPath);

            String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);

            String rendered = template
                    .replace("${contextPath}", contextPath)
                    .replace("${user}", escapeHtml(String.valueOf(session.getAttribute("user"))))
                    .replace("${sessionId}", escapeHtml(nullToDash(resolvedSessionId)))
                    .replace("${rawSessionId}", escapeHtml(nullToEmpty(sessionId)))
                    .replace("${friendlyName}", escapeHtml(nullToDash(friendlyName)))
                    .replace("${email}", escapeHtml(nullToDash(profile != null ? profile.getEmail() : (identity != null ? identity.getCanonicalEmail() : null))))
                    .replace("${phone}", escapeHtml(nullToDash(profile != null ? profile.getPhone() : null)))
                    .replace("${title}", escapeHtml(nullToDash(profile != null ? profile.getTitle() : null)))
                    .replace("${department}", escapeHtml(nullToDash(profile != null ? profile.getDepartment() : null)))
                    .replace("${salesforceContactId}", escapeHtml(nullToDash(profile != null ? profile.getSalesforceContactId() : (identity != null ? identity.getSalesforceContactId() : null))))
                    .replace("${salesforceAccountId}", escapeHtml(nullToDash(profile != null ? profile.getSalesforceAccountId() : (identity != null ? identity.getSalesforceAccountId() : null))))
                    .replace("${lastSyncedAt}", escapeHtml(profile != null && profile.getLastSyncedAt() != null
                            ? formatOffsetDateTime(profile.getLastSyncedAt())
                            : (identity != null && identity.getLastSyncedAt() != null ? formatOffsetDateTime(identity.getLastSyncedAt()) : "Never")))
                    // Add this placeholder to your HTML where you want the related sessions table body/rows
                    .replace("${linkedSessionsRows}", linkedSessionsHtml);

            resp.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                out.print(rendered);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Customer profile data lookup failed", e);
            throw new ServletException("Unable to load customer profile.", e);
        }
    
        } catch (ServletException | IOException | IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    LOGGER.log(Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private String buildLinkedSessionsRows(List<CustomerIdentitySessionLink> links, String contextPath) {
        if (links == null || links.isEmpty()) {
            StringBuilder empty = new StringBuilder(96);
            openTag(empty, "tr");
            openTag(empty, "td", "colspan", "4", "class", "empty-row");
            empty.append(escapeHtml("No linked sessions found."));
            closeTag(empty, "td");
            closeTag(empty, "tr");
            return empty.toString();
        }

        return links.stream().map(link -> {
            String sid = nullToEmpty(link.getSessionId());
            String display = nullToEmpty(link.getDisplayNameSnapshot());
            String email = nullToEmpty(link.getContactEmailSnapshot());
            String updated = link.getUpdatedAt() == null ? "Ã¢â‚¬â€" : formatOffsetDateTime(link.getUpdatedAt());

            String profileHref = contextPath + "/customer-profile?sessionId=" + urlEncode(sid);

            StringBuilder row = new StringBuilder(256);
            openTag(row, "tr");

            openTag(row, "td");
            openTag(row, "a", "href", profileHref);
            row.append(escapeHtml(sid));
            closeTag(row, "a");
            closeTag(row, "td");

            openTag(row, "td");
            row.append(escapeHtml(display.isBlank() ? "Ã¢â‚¬â€" : display));
            closeTag(row, "td");

            openTag(row, "td");
            row.append(escapeHtml(email.isBlank() ? "Ã¢â‚¬â€" : email));
            closeTag(row, "td");

            openTag(row, "td");
            row.append(escapeHtml(updated));
            closeTag(row, "td");

            closeTag(row, "tr");
            return row.toString();
        }).collect(Collectors.joining());
    }

    private void openTag(StringBuilder out, String name, String... attrs) {
        out.append('<').append(name);
        for (int i = 0; i + 1 < attrs.length; i += 2) {
            out.append(' ')
                    .append(attrs[i])
                    .append("=\"")
                    .append(escapeHtml(attrs[i + 1]))
                    .append('"');
        }
        out.append('>');
    }

    private void closeTag(StringBuilder out, String name) {
        out.append('<').append('/').append(name).append('>');
    }

    private String loadTemplate(ServletContext context, String path) {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load template: " + path, e);
            throw new IllegalStateException("Unable to load template: " + path, e);
        }
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

    private String nullToDash(String value) {
        String v = trimToNull(value);
        return v == null ? "Ã¢â‚¬â€" : v;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(ch);
            }
        }
        return escaped.toString();
    }

    private String urlEncode(String input) {
        return java.net.URLEncoder.encode(input == null ? "" : input, StandardCharsets.UTF_8);
    }

    private String formatOffsetDateTime(OffsetDateTime value) {
        if (value == null) {
            return "Ã¢â‚¬â€";
        }
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value);
    }

    private CustomerIdentityService identityService() {
        return new CustomerIdentityService();
    }
}
