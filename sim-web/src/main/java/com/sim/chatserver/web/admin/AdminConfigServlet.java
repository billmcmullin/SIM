package com.sim.chatserver.web.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
public class AdminConfigServlet extends HttpServlet {
    private static final String TEMPLATE_PATH = "/WEB-INF/views/admin_config.html";
    private static final String SESSION_USER_ATTR = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String SESSION_ROLE_ATTR = com.sim.chatserver.util.CommonLiteralConstants.ROLE;
    private static final String LOGIN_PATH = com.sim.chatserver.util.CommonLiteralConstants.PATH_LOGIN;
    private static final String ADMIN_ROLE = CommonLiteralConstants.ROLE_ADMIN;
    private static final String CONTEXT_PLACEHOLDER = CommonLiteralConstants.TEMPLATE_CONTEXT_PATH;
    private static final String USER_PLACEHOLDER = CommonLiteralConstants.TEMPLATE_USER;
    private static final String HTML_UTF8_CONTENT_TYPE = CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8;
    private static final String REQUEST_FAILED_MESSAGE = CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED;
    private static final String FALLBACK_SEND_ERROR_LOG = CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD;
    private static final String LOAD_CONFIG_ERROR = CommonLiteralConstants.MSG_LOAD_SERVER_CONFIGURATION;
    private static final String UNHANDLED_DO_GET_LOG = CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET;
    private static final Logger log = Logger.getLogger(AdminConfigServlet.class.getCanonicalName());

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            log.info("AdminConfigServlet.init: starting initialization");
            EncryptedDbConfigStore.ensureTable();
            log.info("AdminConfigServlet.init: EncryptedDbConfigStore.ensureTable OK");

            termsStore().ensureTable();
            log.info("AdminConfigServlet.init: termsStore.ensureTable OK");
            log.info("AdminConfigServlet.init: initialization completed");
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.SEVERE, "AdminConfigServlet init failed", e);
            throw new ServletException("Unable to initialize configuration storage", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        final String rid = UUID.randomUUID().toString().substring(0, 8);
        log.info(() -> "[RID " + rid + "] GET /admin start");

        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute(SESSION_USER_ATTR) == null) {
                log.info(() -> "[RID " + rid + "] Redirecting to login because no valid session/user is present.");
                req.getRequestDispatcher(LOGIN_PATH).forward(req, resp);
                return;
            }

            String username = String.valueOf(session.getAttribute(SESSION_USER_ATTR));
            Object roleAttr = session.getAttribute(SESSION_ROLE_ATTR);
            String role = roleAttr == null ? CommonLiteralConstants.VALUE_UNKNOWN_UPPER : roleAttr.toString();
            log.info(() -> String.format("[RID %s] User '%s' with role '%s' requested /admin", rid, username, role));

            if (!ADMIN_ROLE.equalsIgnoreCase(role)) {
                log.warning(() -> String.format("[RID %s] User '%s' with role '%s' denied access to /admin; redirecting to dashboard.", rid, username, role));
                req.getRequestDispatcher("/dashboard").forward(req, resp);
                return;
            }

            log.info(() -> "[RID " + rid + "] Loading template: " + TEMPLATE_PATH);
            String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
            log.info(() -> "[RID " + rid + "] Template loaded, size=" + template.length());

            ServerConfig config;
            try {
                config = EncryptedDbConfigStore.load();
                log.info(() -> "[RID " + rid + "] EncryptedDbConfigStore.load OK");
            } catch (SQLException | IllegalStateException e) {
                log.log(Level.SEVERE, "[RID " + rid + "] " + LOAD_CONFIG_ERROR, e);
                throw new ServletException(LOAD_CONFIG_ERROR, e);
            }

            String widgetListJson = "[]";
            try {
                List<WidgetEntry> widgets = WidgetStore.list(null);
                widgetListJson = serializeWidgets(widgets);
                log.info(() -> "[RID " + rid + "] Loaded widgets count=" + (widgets == null ? 0 : widgets.size()));
            } catch (SQLException e) {
                log.log(Level.WARNING, "[RID " + rid + "] Unable to load widget entries", e);
            }

            String termsListJson = "[]";
            try {
                List<TermDefinition> terms = termsStore().listAll();
                termsListJson = serializeTerms(terms);
                log.info(() -> "[RID " + rid + "] Loaded terms count=" + (terms == null ? 0 : terms.size()));
            } catch (SQLException | IllegalStateException e) {
                log.log(Level.WARNING, "[RID " + rid + "] Unable to load term definitions", e);
            }

            boolean apiKeyStored = config != null && config.getApiKey() != null && !config.getApiKey().isBlank();
            String apiKeyForJs = escapeJs(apiKeyStored && config != null ? config.getApiKey() : "");
            String workspaceName = config != null ? config.getWorkspaceName() : "";

            boolean salesforceApiKeyStored = config != null
                    && config.getSalesforceApiKey() != null
                    && !config.getSalesforceApiKey().isBlank();
                String salesforceApiKeyForJs = escapeJs(salesforceApiKeyStored && config != null ? config.getSalesforceApiKey() : "");
            String salesforceInstanceUrl = config != null ? config.getSalesforceInstanceUrl() : "";

            boolean salesforceClientSecretStored = config != null
                    && config.getSalesforceClientSecret() != null
                    && !config.getSalesforceClientSecret().isBlank();

            boolean salesforceRefreshTokenStored = config != null
                    && config.getSalesforceRefreshToken() != null
                    && !config.getSalesforceRefreshToken().isBlank();

                boolean salesforcePasswordStored = config != null
                    && config.getSalesforcePassword() != null
                    && !config.getSalesforcePassword().isBlank();

                boolean salesforceApiTokenStored = config != null
                    && config.getSalesforceApiToken() != null
                    && !config.getSalesforceApiToken().isBlank();

            String salesforceLoginUrl = config != null ? config.getSalesforceLoginUrl() : "";
            String salesforceClientId = config != null ? config.getSalesforceClientId() : "";
                String salesforceUsername = config != null ? config.getSalesforceUsername() : "";

                String awsRegion = config != null ? config.getAwsRegion() : "";
                String awsInstanceId = config != null ? config.getAwsInstanceId() : "";
                boolean awsAccessKeyIdStored = config != null
                    && config.getAwsAccessKeyId() != null
                    && !config.getAwsAccessKeyId().isBlank();
                boolean awsSecretAccessKeyStored = config != null
                    && config.getAwsSecretAccessKey() != null
                    && !config.getAwsSecretAccessKey().isBlank();

            String salesforceOAuthStatus = ServletRequestParamUtil.firstParam(req, "salesforceOAuthStatus", 512, true, true);
            String salesforceOAuthMessage = ServletRequestParamUtil.firstParam(req, "salesforceOAuthMessage", 512, true, true);
            if (salesforceOAuthStatus == null) {
                salesforceOAuthStatus = "";
            }
            if (salesforceOAuthMessage == null) {
                salesforceOAuthMessage = "";
            }

            String rendered = template
                    .replace(USER_PLACEHOLDER, escapeHtml(username))
                    .replace(CONTEXT_PLACEHOLDER, req.getContextPath())
                    .replace("${serverHost}", escapeAttribute(config != null ? config.getServerHost() : ""))
                    .replace("${serverPort}", config != null ? String.valueOf(config.getServerPort()) : "")
                    .replace("${connectionInfo}", escapeAttribute(config != null ? config.getConnectionInfo() : ""))
                    .replace("${workspaceName}", escapeHtml(workspaceName))
                    .replace("${apiKey}", "")
                    .replace("${apiKeyStored}", Boolean.toString(apiKeyStored))
                    .replace("${apiKeyForJs}", apiKeyForJs)
                    .replace("${salesforceInstanceUrl}", escapeAttribute(salesforceInstanceUrl))
                    .replace("${salesforceApiKey}", "")
                    .replace("${salesforceApiKeyStored}", Boolean.toString(salesforceApiKeyStored))
                    .replace("${salesforceApiKeyForJs}", salesforceApiKeyForJs)
                    .replace("${salesforceLoginUrl}", escapeAttribute(salesforceLoginUrl))
                    .replace("${salesforceClientId}", escapeAttribute(salesforceClientId))
                    .replace("${salesforceUsername}", escapeAttribute(salesforceUsername))
                    .replace("${salesforceClientSecret}", "")
                    .replace("${salesforceClientSecretStored}", Boolean.toString(salesforceClientSecretStored))
                    .replace("${salesforceRefreshToken}", "")
                    .replace("${salesforceRefreshTokenStored}", Boolean.toString(salesforceRefreshTokenStored))
                    .replace("${salesforcePassword}", "")
                    .replace("${salesforcePasswordStored}", Boolean.toString(salesforcePasswordStored))
                    .replace("${salesforceApiToken}", "")
                    .replace("${salesforceApiTokenStored}", Boolean.toString(salesforceApiTokenStored))
                    .replace("${awsRegion}", escapeAttribute(awsRegion))
                    .replace("${awsInstanceId}", escapeAttribute(awsInstanceId))
                    .replace("${awsAccessKeyId}", "")
                    .replace("${awsSecretAccessKey}", "")
                    .replace("${awsAccessKeyIdStored}", Boolean.toString(awsAccessKeyIdStored))
                    .replace("${awsSecretAccessKeyStored}", Boolean.toString(awsSecretAccessKeyStored))
                    .replace("${salesforceOAuthStatus}", escapeJs(salesforceOAuthStatus))
                    .replace("${salesforceOAuthMessage}", escapeJs(salesforceOAuthMessage))
                    .replace("${widgetListJson}", widgetListJson)
                    .replace("${termsListJson}", termsListJson);

            log.info(() -> "[RID " + rid + "] Rendered admin page size=" + rendered.length());

            resp.setContentType(HTML_UTF8_CONTENT_TYPE);
            try (PrintWriter out = resp.getWriter()) {
                out.print(rendered);
            }

            log.info(() -> "[RID " + rid + "] GET /admin completed successfully");
        } catch (ServletException | IOException e) {
            log.log(Level.SEVERE, "[RID " + rid + "] AdminConfigServlet doGet failed", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, REQUEST_FAILED_MESSAGE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, FALLBACK_SEND_ERROR_LOG, ioe);
                }
            }
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, UNHANDLED_DO_GET_LOG, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, REQUEST_FAILED_MESSAGE);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, FALLBACK_SEND_ERROR_LOG, ioe);
                }
            }
        }
    }

    private static String serializeTerms(List<TermDefinition> terms) {
        if (terms == null || terms.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (TermDefinition term : terms) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append("{\"id\":").append(term.getId())
                    .append(",\"name\":\"").append(escapeJson(term.getName())).append('"')
                    .append(",\"description\":\"").append(escapeJson(term.getDescription())).append('"')
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
                return builder.toString();
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to load template: " + path, e);
            throw new IllegalStateException("Unable to load template: " + path, e);
        }
    }

    private static String serializeWidgets(List<WidgetEntry> widgets) {
        if (widgets == null || widgets.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (WidgetEntry entry : widgets) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append("{\"id\":").append(entry.getId())
                    .append(",\"widgetId\":\"").append(escapeJson(entry.getWidgetId())).append('"')
                    .append(",\"displayName\":\"").append(escapeJson(entry.getDisplayName())).append('"')
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO)
                .replace("\"", com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_QUOTE_TO)
                .replace(System.lineSeparator(), com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO)
                .replace(System.lineSeparator(), "");
    }

    private static String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO)
                .replace("'", com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_SINGLE_QUOTE)
                .replace("\"", com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_QUOTE_TO)
                .replace(System.lineSeparator(), com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO)
                .replace(System.lineSeparator(), "");
    }

    private String escapeHtml(String input) {
        return DashboardTemplateRenderer.escapeHtml(input);
    }

    private String escapeAttribute(String input) {
        return escapeHtml(input);
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
