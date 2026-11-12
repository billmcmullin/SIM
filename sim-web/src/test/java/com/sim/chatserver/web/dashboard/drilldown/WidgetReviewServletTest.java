package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewServlet
 * @author bmcmullin
 */
public class WidgetReviewServletTest
{
    @Test
    public void displayContextIsEscapedAsText() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        var selection = mock(com.sim.chatserver.web.dashboard.widgets.Selection.class);
        String id = "00000000-0000-0000-0000-000000000004";
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("phase4-user");
        when(request.getParameterValues("selectionId")).thenReturn(new String[] { id });
        when(request.getServletContext()).thenReturn(context);
        when(request.getContextPath()).thenReturn("/chat-server");
        when(selection.getDisplayContext()).thenReturn("Topic <img src=x onerror=alert(1)> & day");
        when(selection.hasSnapshots()).thenReturn(true);
        when(context.getResourceAsStream(nullable(String.class))).thenReturn(new java.io.ByteArrayInputStream(
            "<p id='reviewOrigin'>${displayContext}</p><span>${displaySubjectType}</span><script>const subjectType='${subjectType}';</script>".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        var output = new java.io.StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(output));
        try (var start = org.mockito.Mockito.mockStatic(com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.class)) {
            start.when(() -> com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.fetchSelection(session, id)).thenReturn(selection);
            new WidgetReviewServlet().doGet(request, response);
        }
        org.junit.jupiter.api.Assertions.assertEquals(
                "<p id='reviewOrigin'>Topic &lt;img src=x onerror=alert(1)&gt; &amp; day</p><span>Selection</span><script>const subjectType='Term';</script>", output.toString());
    }


    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        WidgetReviewServlet underTest = new WidgetReviewServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        WidgetReviewServlet underTest = new WidgetReviewServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        WidgetReviewServlet underTest = new WidgetReviewServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        WidgetReviewServlet underTest = new WidgetReviewServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getParameterResult = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        WidgetReviewServlet underTest = new WidgetReviewServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getParameterResult = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

}
