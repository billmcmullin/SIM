package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class AdminConfigServletTest {

    @Test
    void init_success_callsEnsureTableAndTermsStore() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);

        try (MockedStatic<EncryptedDbConfigStore> configStatic = Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            servlet.init();
            configStatic.verify(EncryptedDbConfigStore::ensureTable);
            verify(termsStore).ensureTable();
        }
    }

    @Test
    void init_failure_throwsServletException() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);

        try (MockedStatic<EncryptedDbConfigStore> configStatic = Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            configStatic.when(EncryptedDbConfigStore::ensureTable).thenThrow(new SQLException("db"));

            try {
                servlet.init();
            } catch (ServletException ex) {
                assertTrue(ex.getMessage().contains("Unable to initialize configuration storage"));
                return;
            }
        }

        throw new AssertionError("Expected ServletException");
    }

    @Test
    void doGet_noSession_forwardsToLogin() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher login = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(login);

        servlet.doGet(req, resp);

        verify(login).forward(req, resp);
    }

    @Test
    void doGet_nonAdmin_forwardsToDashboard() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dashboard = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(session.getAttribute("role")).thenReturn("ANALYST");
        when(req.getRequestDispatcher("/dashboard")).thenReturn(dashboard);

        servlet.doGet(req, resp);

        verify(dashboard).forward(req, resp);
    }

    @Test
    void doGet_admin_success_rendersTemplate() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext ctx = mock(ServletContext.class);
        StringWriter body = new StringWriter();

        String template = "${user}|${contextPath}|${serverHost}|${serverPort}|${connectionInfo}|${workspaceName}|"
                + "${apiKeyStored}|${apiKeyForJs}|${salesforceInstanceUrl}|${salesforceApiKeyStored}|"
                + "${salesforceApiKeyForJs}|${salesforceLoginUrl}|${salesforceClientId}|${salesforceUsername}|"
                + "${salesforceClientSecretStored}|${salesforceRefreshTokenStored}|${salesforcePasswordStored}|"
                + "${salesforceApiTokenStored}|${awsRegion}|${awsInstanceId}|${awsAccessKeyIdStored}|"
                + "${awsSecretAccessKeyStored}|${salesforceOAuthStatus}|${salesforceOAuthMessage}|"
                + "${widgetListJson}|${termsListJson}";

        ServerConfig cfg = new ServerConfig("host-a", 5432, "conn-a", "secret-key", "ws-a");
        cfg.setSalesforceInstanceUrl("https://sf.example");
        cfg.setSalesforceApiKey("sf-key");
        cfg.setSalesforceLoginUrl("https://login.salesforce.com");
        cfg.setSalesforceClientId("client-1");
        cfg.setSalesforceClientSecret("secret-1");
        cfg.setSalesforceRefreshToken("refresh-1");
        cfg.setSalesforceUsername("user-1");
        cfg.setSalesforcePassword("pw-1");
        cfg.setSalesforceApiToken("tok-1");
        cfg.setAwsRegion("us-west-2");
        cfg.setAwsInstanceId("i-abc");
        cfg.setAwsAccessKeyId("AKIA");
        cfg.setAwsSecretAccessKey("SECRET");

        TermDefinition term = mock(TermDefinition.class);
        when(term.getId()).thenReturn(99L);
        when(term.getName()).thenReturn("term1");
        when(term.getDescription()).thenReturn("desc1");

        when(termsStore.listAll()).thenReturn(List.of(term));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin-user");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getContextPath()).thenReturn("/chat-server");
        when(req.getServletContext()).thenReturn(ctx);
        when(req.getParameterValues("salesforceOAuthStatus")).thenReturn(new String[] { "ok" });
        when(req.getParameterValues("salesforceOAuthMessage")).thenReturn(new String[] { "linked" });
        when(ctx.getResourceAsStream("/WEB-INF/views/admin_config.html"))
                .thenReturn(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        try (MockedStatic<EncryptedDbConfigStore> configStatic = Mockito.mockStatic(EncryptedDbConfigStore.class);
                MockedStatic<WidgetStore> widgetStatic = Mockito.mockStatic(WidgetStore.class)) {
            configStatic.when(EncryptedDbConfigStore::load).thenReturn(cfg);
            widgetStatic.when(() -> WidgetStore.list(null))
                    .thenReturn(List.of(com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "w1", "Widget One", Instant.now())));

            servlet.doGet(req, resp);
        }

        String rendered = body.toString();
        assertTrue(rendered.contains("admin-user"));
        assertTrue(rendered.contains("/chat-server"));
        assertTrue(rendered.contains("host-a"));
        assertTrue(rendered.contains("\"widgetId\":\"w1\""));
        assertTrue(rendered.contains("\"name\":\"term1\""));
        verify(resp).setContentType("text/html;charset=UTF-8");
    }

    @Test
    void doGet_admin_configLoadFailure_sendsServerError() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext ctx = mock(ServletContext.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin-user");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getServletContext()).thenReturn(ctx);
        when(req.getContextPath()).thenReturn("/chat-server");
        when(ctx.getResourceAsStream("/WEB-INF/views/admin_config.html"))
                .thenReturn(new ByteArrayInputStream("ok".getBytes(StandardCharsets.UTF_8)));
        when(resp.isCommitted()).thenReturn(false);

        try (MockedStatic<EncryptedDbConfigStore> configStatic = Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            configStatic.when(EncryptedDbConfigStore::load).thenThrow(new SQLException("db down"));
            servlet.doGet(req, resp);
        }

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void doGet_outerIllegalStateCatch_sendsServerError() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void doGet_whenCommitted_doesNotSendFallbackError() {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(true);

        servlet.doGet(req, resp);

        try {
            verify(resp, never()).sendError(anyInt(), any());
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    void privateHelpers_coverEscapingAndSerialization() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);

        assertEquals("", invokeStaticString("escapeJson", null));
        assertEquals("a\\\"b\\nc", invokeStaticString("escapeJson", "a\"b\nc"));
        assertEquals("a\\'b\\\"c", invokeStaticString("escapeJs", "a'b\"c"));
        assertEquals("x&amp;y&lt;z&gt;&quot;&#39;", invokeInstanceString(servlet, "escapeHtml", "x&y<z>\"'"));
        assertEquals("", invokeInstanceString(servlet, "escapeAttribute", null));

        String emptyWidgets = invokeStaticListString("serializeWidgets", List.of());
        assertEquals("[]", emptyWidgets);

        String widgets = invokeStaticListString("serializeWidgets",
                List.of(com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(7, "wid-1", "Display One", Instant.now())));
        assertTrue(widgets.contains("\"id\":7"));
        assertTrue(widgets.contains("\"widgetId\":\"wid-1\""));

        TermDefinition term = mock(TermDefinition.class);
        when(term.getId()).thenReturn(11L);
        when(term.getName()).thenReturn("alpha");
        when(term.getDescription()).thenReturn("beta");

        String terms = invokeStaticListString("serializeTerms", List.of(term));
        assertTrue(terms.contains("\"id\":11"));
        assertTrue(terms.contains("\"name\":\"alpha\""));

        String emptyTerms = invokeStaticListString("serializeTerms", List.of());
        assertEquals("[]", emptyTerms);
    }

    @Test
    void loadTemplate_missingResource_throwsIllegalState() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        ServletContext context = mock(ServletContext.class);

        when(context.getResourceAsStream("/missing")).thenReturn(null);

        try {
            invoke(servlet, "loadTemplate", new Class<?>[] { ServletContext.class, String.class }, context, "/missing");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof IllegalStateException illegalState) {
                assertTrue(illegalState.getMessage().contains("Template not found"));
                return;
            }
            throw ex;
        }

        throw new AssertionError("Expected IllegalStateException");
    }

    @Test
    void loadTemplate_ioFailure_throwsIllegalState() throws Exception {
        TermsStore termsStore = mock(TermsStore.class);
        TestableAdminConfigServlet servlet = new TestableAdminConfigServlet(termsStore);
        ServletContext context = mock(ServletContext.class);

        when(context.getResourceAsStream("/broken")).thenReturn(new InputStreamThatFailsOnRead());

        try {
            invoke(servlet, "loadTemplate", new Class<?>[] { ServletContext.class, String.class }, context, "/broken");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof IllegalStateException illegalState) {
                assertTrue(illegalState.getMessage().contains("Unable to load template"));
                return;
            }
            throw ex;
        }

        throw new AssertionError("Expected IllegalStateException");
    }

    private static String invokeStaticString(String methodName, String input) throws Exception {
        Method method = AdminConfigServlet.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, input);
    }

    @SuppressWarnings("unchecked")
    private static String invokeStaticListString(String methodName, List<?> input) throws Exception {
        Method method = AdminConfigServlet.class.getDeclaredMethod(methodName, List.class);
        method.setAccessible(true);
        return (String) method.invoke(null, input);
    }

    private static String invokeInstanceString(AdminConfigServlet servlet, String methodName, String input) throws Exception {
        Method method = AdminConfigServlet.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String) method.invoke(servlet, input);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getSuperclass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static final class TestableAdminConfigServlet extends AdminConfigServlet {
        private final TermsStore termsStore;

        private TestableAdminConfigServlet(TermsStore termsStore) {
            this.termsStore = termsStore;
        }

        protected TermsStore termsStore() {
            return termsStore;
        }
    }

    private static final class InputStreamThatFailsOnRead extends java.io.InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("io");
        }
    }
}
