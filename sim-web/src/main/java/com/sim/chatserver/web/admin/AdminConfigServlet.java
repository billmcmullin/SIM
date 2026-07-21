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
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminConfigServlet extends HttpServlet {
    // parasoft-suppress SERVLET.AJDBC "This endpoint intentionally performs bounded JDBC-backed config reads and writes."
    // parasoft-suppress SERVLET.CETS "Checked exceptions are handled at servlet boundaries with safe fallback responses."
    // parasoft-suppress SERVLET.IF "CDI-managed collaborators are required and do not retain mutable request state."
    // parasoft-suppress SECURITY.ESD.SIF "Injected collaborators are framework-managed and not serialized secret payloads."

    private static final String TEMPLATE_PATH = "/WEB-INF/views/admin_config.html";
    private static final Logger log = Logger.getLogger(AdminConfigServlet.class.getName());

    @Inject
    TermsStore termsStore;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            log.info("AdminConfigServlet.init: starting initialization");
            EncryptedDbConfigStore.ensureTable();
            log.info("AdminConfigServlet.init: EncryptedDbConfigStore.ensureTable OK");

            if (termsStore == null) {
                log.severe("AdminConfigServlet.init: TermsStore injection is null");
                throw new ServletException("TermsStore injection failed (null)");
            }

            termsStore.ensureTable();
            log.info("AdminConfigServlet.init: termsStore.ensureTable OK");
            log.info("AdminConfigServlet.init: initialization completed");
        } catch (SQLException | RuntimeException e) {
            log.log(Level.SEVERE, "AdminConfigServlet init failed", e);
            throw new ServletException("Unable to initialize configuration storage", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String rid = UUID.randomUUID().toString().substring(0, 8);
        log.info(() -> "[RID " + rid + "] GET /admin start");

        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                log.info(() -> "[RID " + rid + "] Redirecting to login because no valid session/user is present.");
                req.getRequestDispatcher("/login").forward(req, resp);
                return;
            }

            String username = String.valueOf(session.getAttribute("user"));
            Object roleAttr = session.getAttribute("role");
            String role = roleAttr == null ? "UNKNOWN" : roleAttr.toString();
            log.info(() -> String.format("[RID %s] User '%s' with role '%s' requested /admin", rid, username, role));

            if (!"ADMIN".equalsIgnoreCase(role)) {
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
            } catch (SQLException | RuntimeException e) {
                log.log(Level.SEVERE, "[RID " + rid + "] Unable to load server configuration", e);
                throw new ServletException("Unable to load server configuration", e);
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
                if (termsStore == null) {
                    throw new IllegalStateException("termsStore is null in doGet");
                }
                List<TermDefinition> terms = termsStore.listAll();
                termsListJson = serializeTerms(terms);
                log.info(() -> "[RID " + rid + "] Loaded terms count=" + (terms == null ? 0 : terms.size()));
            } catch (SQLException | RuntimeException e) {
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

            String salesforceLoginUrl = config != null ? config.getSalesforceLoginUrl() : "";
            String salesforceClientId = config != null ? config.getSalesforceClientId() : "";

            String salesforceOAuthStatus = firstQueryParam(req, "salesforceOAuthStatus");
            String salesforceOAuthMessage = firstQueryParam(req, "salesforceOAuthMessage");
            if (salesforceOAuthStatus == null) {
                salesforceOAuthStatus = "";
            }
            if (salesforceOAuthMessage == null) {
                salesforceOAuthMessage = "";
            }

            String rendered = template
                    .replace("${user}", escapeHtml(username))
                    .replace("${contextPath}", req.getContextPath())
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
                    .replace("${salesforceClientSecret}", "")
                    .replace("${salesforceClientSecretStored}", Boolean.toString(salesforceClientSecretStored))
                    .replace("${salesforceRefreshToken}", "")
                    .replace("${salesforceRefreshTokenStored}", Boolean.toString(salesforceRefreshTokenStored))
                    .replace("${salesforceOAuthStatus}", escapeJs(salesforceOAuthStatus))
                    .replace("${salesforceOAuthMessage}", escapeJs(salesforceOAuthMessage))
                    .replace("${widgetListJson}", widgetListJson)
                    .replace("${termsListJson}", termsListJson);

            log.info(() -> "[RID " + rid + "] Rendered admin page size=" + rendered.length());

            resp.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                out.print(rendered);
            }

            log.info(() -> "[RID " + rid + "] GET /admin completed successfully");
        } catch (ServletException | IOException | RuntimeException e) {
            log.log(Level.SEVERE, "[RID " + rid + "] AdminConfigServlet doGet failed", e);
            throw e;
        }
    }

    private String firstQueryParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() > 512 ? normalized.substring(0, 512) : normalized;
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
                    .append(",\"name\":\"").append(escapeJson(term.getName())).append("\"")
                    .append(",\"description\":\"").append(escapeJson(term.getDescription())).append("\"")
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
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
                    .append(",\"widgetId\":\"").append(escapeJson(entry.getWidgetId())).append("\"")
                    .append(",\"displayName\":\"").append(escapeJson(entry.getDisplayName())).append("\"")
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private static String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeAttribute(String input) {
        return escapeHtml(input);
    }
}
