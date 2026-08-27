package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class AdminTermServletTest {

    @Test
    void doGet_unauthorized_returns401() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doGet(req, resp));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, capture.errorStatus.get());
    }

    @Test
    void doGet_nonAdmin_returns403() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user1");
        when(session.getAttribute("role")).thenReturn("VIEWER");

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doGet(req, resp));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, capture.errorStatus.get());
    }

    @Test
    void doGet_admin_success_returnsTerms() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        TermDefinition term = mock(TermDefinition.class);
        when(term.getId()).thenReturn(10L);
        when(term.getName()).thenReturn("alpha");
        when(term.getDescription()).thenReturn("desc");
        when(term.getMatchPattern()).thenReturn("pat");
        when(term.getMatchType()).thenReturn("WILDCARD");
        when(term.isSystemFlag()).thenReturn(false);

        when(store.listAll()).thenReturn(List.of(term));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doGet(req, resp));

        assertEquals(HttpServletResponse.SC_OK, capture.jsonStatus.get());
        JsonObject payload = capture.payload.get();
        assertEquals("ok", payload.getString("status"));
        assertTrue(payload.getJsonArray("terms").size() == 1);
    }

    @Test
    void doGet_admin_storeFailure_returns500() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(store.listAll()).thenThrow(new SQLException("db down"));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doGet(req, resp));

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, capture.errorStatus.get());
    }

    @Test
    void doPost_invalidLength_returns400() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getContentLengthLong()).thenReturn(999999L);

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doPost(req, resp));

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, capture.errorStatus.get());
    }

    @Test
    void doPost_success_returnsCreatedTerm() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        String body = "{\"name\":\"alpha\",\"description\":\"desc\",\"matchPattern\":\"pat\",\"matchType\":\"WILDCARD\"}";
        TermDefinition created = mock(TermDefinition.class);
        when(created.getId()).thenReturn(7L);
        when(created.getName()).thenReturn("alpha");
        when(created.getDescription()).thenReturn("desc");
        when(created.getMatchPattern()).thenReturn("pat");
        when(created.getMatchType()).thenReturn("WILDCARD");
        when(created.isSystemFlag()).thenReturn(false);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getContentLengthLong()).thenReturn((long) body.length());
        when(req.getReader()).thenReturn(new java.io.BufferedReader(new StringReader(body)));
        when(store.createTerm("alpha", "desc", "pat", "WILDCARD")).thenReturn(created);

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doPost(req, resp));

        assertEquals(HttpServletResponse.SC_OK, capture.jsonStatus.get());
        assertEquals("ok", capture.payload.get().getString("status"));
    }

    @Test
    void doPost_createConflict_returns409() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        String body = "{\"name\":\"alpha\",\"description\":\"desc\"}";

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getContentLengthLong()).thenReturn((long) body.length());
        when(req.getReader()).thenReturn(new java.io.BufferedReader(new StringReader(body)));
        when(store.createTerm(any(), any(), any(), any())).thenThrow(new SQLException("duplicate"));

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doPost(req, resp));

        assertEquals(HttpServletResponse.SC_CONFLICT, capture.errorStatus.get());
    }

    @Test
    void doPut_invalidId_returns400() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        String body = "{\"id\":999999999999999999999999,\"name\":\"alpha\",\"description\":\"desc\"}";

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getContentLengthLong()).thenReturn((long) body.length());
        when(req.getReader()).thenReturn(new java.io.BufferedReader(new StringReader(body)));

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doPut(req, resp));

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, capture.errorStatus.get());
    }

    @Test
    void doPut_updateReturnsNull_returns403() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        String body = "{\"id\":5,\"name\":\"alpha\",\"description\":\"desc\",\"matchPattern\":\"pat\",\"matchType\":\"WILDCARD\"}";

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getContentLengthLong()).thenReturn((long) body.length());
        when(req.getReader()).thenReturn(new java.io.BufferedReader(new StringReader(body)));
        when(store.updateTerm(5L, "alpha", "desc", "pat", "WILDCARD")).thenReturn(null);

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doPut(req, resp));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, capture.errorStatus.get());
    }

    @Test
    void doDelete_success_returnsOk() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("id")).thenReturn(new String[] { "15" });
        when(store.deleteTerm(15L)).thenReturn(true);

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doDelete(req, resp));

        assertEquals(HttpServletResponse.SC_OK, capture.jsonStatus.get());
    }

    @Test
    void doDelete_invalidId_returns400() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("id")).thenReturn(new String[] { "x" });

        JsonCapture capture = new JsonCapture();
        withTermsStoreAndJsonCapture(store, capture, () -> servlet.doDelete(req, resp));

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, capture.errorStatus.get());
    }

    @Test
    void privateHelpers_coverReadBodyAndFallback() throws Exception {
        AdminTermServlet servlet = new AdminTermServlet();

        String clean = (String) invoke(servlet, "validateCanonicalizedBodyText", new Class<?>[] { String.class }, "ab\u0001cd\n");
        assertTrue(clean.contains("ab"));

        String body = (String) invoke(servlet, "readRequestBody", new Class<?>[] { HttpServletRequest.class }, new Object[] { null });
        assertEquals("", body);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);
        invoke(servlet, "sendFallbackServerError", new Class<?>[] { HttpServletResponse.class }, resp);
        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static void withTermsStoreAndJsonCapture(TermsStore store, JsonCapture capture, ThrowingRunnable action)
            throws Exception {
        @SuppressWarnings("unchecked")
        Instance<TermsStore> instance = (Instance<TermsStore>) mock(Instance.class);
        when(instance.get()).thenReturn(store);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(TermsStore.class)).thenReturn(instance);

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
                MockedStatic<ServletJsonResponseUtil> jsonStatic = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);

            jsonStatic.when(() -> ServletJsonResponseUtil.writeError(any(), anyInt(), any()))
                    .thenAnswer(invocation -> {
                        capture.errorStatus.set(invocation.getArgument(1));
                        capture.errorMessage.set(invocation.getArgument(2));
                        return null;
                    });
            jsonStatic.when(() -> ServletJsonResponseUtil.writeJson(any(), anyInt(), any(JsonObject.class)))
                    .thenAnswer(invocation -> {
                        capture.jsonStatus.set(invocation.getArgument(1));
                        capture.payload.set(invocation.getArgument(2));
                        return null;
                    });

            action.run();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class JsonCapture {
        private final AtomicInteger errorStatus = new AtomicInteger(-1);
        private final AtomicReference<String> errorMessage = new AtomicReference<>();
        private final AtomicInteger jsonStatus = new AtomicInteger(-1);
        private final AtomicReference<JsonObject> payload = new AtomicReference<>();
    }
}
