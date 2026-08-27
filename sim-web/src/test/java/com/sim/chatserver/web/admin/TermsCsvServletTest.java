package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

class TermsCsvServletTest {

    @Test
    void doGet_nonAdmin_returnsForbidden() {
        TermsCsvServlet servlet = new TermsCsvServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        try {
            verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    void doGet_admin_exportsCsv() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TermDefinition term = mock(TermDefinition.class);
        when(term.getName()).thenReturn("alpha");
        when(term.getDescription()).thenReturn("desc,one");
        when(term.getMatchPattern()).thenReturn("pat\"tern");
        when(term.getMatchType()).thenReturn("WILDCARD");
        when(term.isSystemFlag()).thenReturn(false);

        when(store.listAll()).thenReturn(List.of(term));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(resp.getOutputStream()).thenReturn(new CapturingServletOutputStream(out));

        withTermsStore(store, () -> servlet.doGet(req, resp));

        verify(resp).setContentType("text/csv; charset=UTF-8");
        verify(resp).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(resp).setHeader(Mockito.eq("Content-Disposition"), Mockito.contains("terms-export.csv"));

        String csv = out.toString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("name,description,match_pattern,match_type,system_flag"));
        assertTrue(csv.contains("alpha,\"desc,one\",\"pat\"\"tern\",WILDCARD,false"));
    }

    @Test
    void doGet_admin_listFailure_returnsInternalServerError() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(store.listAll()).thenThrow(new SQLException("db down"));

        withTermsStore(store, () -> servlet.doGet(req, resp));

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to export terms.");
    }

    @Test
    void doPost_nonAdmin_returnsForbidden() {
        TermsCsvServlet servlet = new TermsCsvServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        servlet.doPost(req, resp);

        try {
            verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    void doPost_admin_missingFile_returnsBadRequest() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getPart("file")).thenReturn(null);

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "No file uploaded.");
    }

    @Test
    void doPost_admin_importSuccess_forwardsToAdminTerms() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Part part = mock(Part.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        String csv = "name,description,match_pattern,match_type,system_flag\n"
                + "new-term,desc,pat,WILDCARD,false\n";

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getPart("file")).thenReturn(part);
        when(req.getRequestDispatcher("/admin/terms")).thenReturn(dispatcher);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        when(store.listAll()).thenReturn(List.of());
        when(store.createTerm("new-term", "desc", "pat", "WILDCARD")).thenReturn(mock(TermDefinition.class));

        withTermsStore(store, () -> servlet.doPost(req, resp));

        verify(store).createTerm("new-term", "desc", "pat", "WILDCARD");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_admin_importIOException_returnsInternalServerError() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Part part = mock(Part.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getPart("file")).thenReturn(part);
        when(part.getInputStream()).thenThrow(new IOException("io"));

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV import failed.");
    }

    @Test
    void processRow_existingSystemTerm_isSkipped() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);
        TermDefinition existing = mock(TermDefinition.class);

        when(existing.getName()).thenReturn("term-a");
        when(existing.isSystemFlag()).thenReturn(true);
        when(store.listAll()).thenReturn(List.of(existing));

        withTermsStore(store, () -> {
            boolean created = invokeProcessRow(servlet, new String[] { "term-a", "d", "p", "WILDCARD", "false" });
            assertFalse(created);
        });

        verify(store, never()).updateTerm(any(), any(), any(), any(), any());
        verify(store, never()).createTerm(any(), any(), any(), any());
    }

    @Test
    void processRow_existingNonSystem_updates() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);
        TermDefinition existing = mock(TermDefinition.class);

        when(existing.getName()).thenReturn("term-a");
        when(existing.getId()).thenReturn(5L);
        when(existing.isSystemFlag()).thenReturn(false);
        when(store.listAll()).thenReturn(List.of(existing));
        when(store.updateTerm(5L, "term-a", "new-desc", "new-pattern", "WILDCARD"))
                .thenReturn(mock(TermDefinition.class));

        withTermsStore(store, () -> {
            boolean created = invokeProcessRow(servlet,
                    new String[] { "term-a", "new-desc", "new-pattern", "WILDCARD", "false" });
            assertFalse(created);
        });

        verify(store).updateTerm(5L, "term-a", "new-desc", "new-pattern", "WILDCARD");
    }

    @Test
    void processRow_newTerm_createsTerm() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);

        when(store.listAll()).thenReturn(List.of());
        when(store.createTerm("new-term", "desc", "pattern", "REGEX")).thenReturn(mock(TermDefinition.class));

        withTermsStore(store, () -> {
            boolean created = invokeProcessRow(servlet,
                    new String[] { "new-term", "desc", "pattern", "REGEX", "true" });
            assertTrue(created);
        });

        verify(store).createTerm("new-term", "desc", "pattern", "REGEX");
    }

    @Test
    void processRow_blankName_throwsIllegalArgumentException() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        TermsStore store = mock(TermsStore.class);

        when(store.listAll()).thenReturn(List.of());

        withTermsStore(store, () -> {
            assertThrows(IllegalArgumentException.class,
                    () -> invokeProcessRow(servlet, new String[] { " ", "desc", "pattern", "WILDCARD", "false" }));
        });
    }

    @Test
    void parseCsvLine_andHeaderMatches_coverQuotedFields() throws Exception {
        String[] cols = invokeStaticStringArray("parseCsvLine", "\"a,b\",\"c\"\"d\",x");
        assertArrayEquals(new String[] { "a,b", "c\"d", "x" }, cols);

        boolean headerOk = invokeStaticBoolean("headerMatches",
                new String[] { "name", "description", "match_pattern", "match_type", "system_flag" });
        boolean headerBad = invokeStaticBoolean("headerMatches",
                new String[] { "name", "desc" });

        assertTrue(headerOk);
        assertFalse(headerBad);
    }

    @Test
    void forwardToAdminTerms_whenForwardFails_sendsFallbackError() throws Exception {
        TermsCsvServlet servlet = new TermsCsvServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getRequestDispatcher("/admin/terms")).thenReturn(dispatcher);
        Mockito.doThrow(new ServletException("boom")).when(dispatcher).forward(req, resp);

        invoke(servlet, "forwardToAdminTerms", new Class<?>[] { HttpServletRequest.class, HttpServletResponse.class }, req,
                resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "CSV import completed but redirect failed.");
    }

    private static boolean invokeProcessRow(TermsCsvServlet servlet, String[] cols) throws Exception {
        try {
            return (boolean) invoke(servlet, "processRow", new Class<?>[] { String[].class }, (Object) cols);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw ex;
        }
    }

    private static String[] invokeStaticStringArray(String methodName, String line) throws Exception {
        Method method = TermsCsvServlet.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String[]) method.invoke(null, line);
    }

    private static boolean invokeStaticBoolean(String methodName, String[] value) throws Exception {
        Method method = TermsCsvServlet.class.getDeclaredMethod(methodName, String[].class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, (Object) value);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static void withTermsStore(TermsStore store, ThrowingRunnable action) throws Exception {
        @SuppressWarnings("unchecked")
        Instance<TermsStore> instance = (Instance<TermsStore>) mock(Instance.class);
        when(instance.get()).thenReturn(store);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(TermsStore.class)).thenReturn(instance);

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            action.run();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class CapturingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate;

        private CapturingServletOutputStream(ByteArrayOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // No async test behavior needed.
        }
    }
}
