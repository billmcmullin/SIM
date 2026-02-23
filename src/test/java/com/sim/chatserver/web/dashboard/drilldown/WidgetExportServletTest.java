package com.sim.chatserver.web.dashboard.drilldown;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.WidgetReviewStartServlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Replacement/fixed WidgetExportServletTest with robust ServletInputStream and
 * response writer/outputStream stubbing to avoid Invalid JSON and NPEs.
 */
public class WidgetExportServletTest {

    private WidgetExportServlet servlet;
    private AppDataSourceHolder dsHolderMock;

    @BeforeEach
    public void setup() {
        servlet = new WidgetExportServlet();
        dsHolderMock = mock(AppDataSourceHolder.class);
        servlet.dsHolder = dsHolderMock;
    }

    @AfterEach
    public void tearDown() {
        // noop
    }

    // Robust ServletInputStream wrapper backed by bytes
    private static ServletInputStream servletInputStreamFrom(byte[] bytes) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // not used in tests
            }
        };
    }

    // Delegating ServletOutputStream wrapper
    private static class DelegatingServletOutputStream extends ServletOutputStream {

        private final OutputStream delegate;

        DelegatingServletOutputStream(OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            /* no-op */ }
    }

    // Helper to create Selection instance (uses reflection to call package-private ctor)
    private WidgetReviewStartServlet.Selection createSelection(String widgetId, List<String> chatIds, List<TermChatSnapshot> snapshots) throws Exception {
        Class<?> selCls = WidgetReviewStartServlet.Selection.class;
        Class<?> stCls = WidgetReviewStartServlet.SearchTerms.class;

        var searchCtor = stCls.getDeclaredConstructor(String.class, String.class, String.class);
        searchCtor.setAccessible(true);
        Object searchTerms = searchCtor.newInstance("", "", "");

        var selCtor = selCls.getDeclaredConstructor(String.class, String.class, String.class, List.class, List.class, stCls);
        selCtor.setAccessible(true);
        return (WidgetReviewStartServlet.Selection) selCtor.newInstance(widgetId, widgetId, null, chatIds, snapshots, searchTerms);
    }

    @Test
    public void doPost_notLoggedIn_returnsUnauthorized() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        // stub writer to avoid NPE when servlet writes
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(resp).getWriter();
    }

    @Test
    public void doPost_invalidJson_sendsBadRequest() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user1");

        // supply non-JSON bytes
        when(req.getInputStream()).thenReturn(servletInputStreamFrom("not-a-json".getBytes(StandardCharsets.UTF_8)));
        when(req.getCharacterEncoding()).thenReturn(StandardCharsets.UTF_8.name());
        when(req.getContentType()).thenReturn("application/json");
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body.");
    }
}
