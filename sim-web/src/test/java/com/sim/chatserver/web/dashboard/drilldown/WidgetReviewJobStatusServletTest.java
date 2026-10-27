package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.ReviewJobService;
import com.sim.chatserver.model.review.ReviewJobStatus;
import org.mockito.MockedStatic;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewJobStatusServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet
 * @author bmcmullin
 */
public class WidgetReviewJobStatusServletTest
{
    @Test
    void pollingAnotherUsersJob_returnsNotFound() throws Exception {
        ReviewJobService jobs = mock(ReviewJobService.class);
        HttpServletRequest request = ownedRequest("bob");
        HttpServletResponse response = responseWriter(new StringWriter());
        try (MockedStatic<WidgetReviewJobStatusServlet> shared = mockStatic(WidgetReviewJobStatusServlet.class, CALLS_REAL_METHODS)) {
            shared.when(WidgetReviewJobStatusServlet::jobService).thenReturn(jobs);
            new WidgetReviewJobStatusServlet().doGet(request, response);
        }
        verify(jobs).getStatusForUser("owned-job", "bob");
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(jobs, never()).getStatus(anyString());
    }

    @Test
    void pollingOwnedJob_returnsProgress() throws Exception {
        ReviewJobService jobs = mock(ReviewJobService.class);
        when(jobs.getStatusForUser("owned-job", "alice")).thenReturn(ReviewJobStatus.builder().jobId("owned-job").build());
        HttpServletResponse response = responseWriter(new StringWriter());
        try (MockedStatic<WidgetReviewJobStatusServlet> shared = mockStatic(WidgetReviewJobStatusServlet.class, CALLS_REAL_METHODS)) {
            shared.when(WidgetReviewJobStatusServlet::jobService).thenReturn(jobs);
            new WidgetReviewJobStatusServlet().doGet(ownedRequest("alice"), response);
        }
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void cancellingAnotherUsersJob_doesNotCancelIt() throws Exception {
        ReviewJobService jobs = mock(ReviewJobService.class);
        HttpServletResponse response = responseWriter(new StringWriter());
        try (MockedStatic<WidgetReviewJobStatusServlet> shared = mockStatic(WidgetReviewJobStatusServlet.class, CALLS_REAL_METHODS)) {
            shared.when(WidgetReviewJobStatusServlet::jobService).thenReturn(jobs);
            new WidgetReviewJobStatusServlet().doDelete(ownedRequest("bob"), response);
        }
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(jobs, never()).cancelForUser(anyString(), anyString());
        verify(jobs, never()).cancel(anyString());
    }

    @Test
    void cancellingOwnedJob_returnsUpdatedStatus() throws Exception {
        ReviewJobService jobs = mock(ReviewJobService.class);
        ReviewJobStatus queued = ReviewJobStatus.builder().jobId("owned-job").build();
        ReviewJobStatus cancelled = ReviewJobStatus.builder().jobId("owned-job").phase(ReviewJobStatus.Phase.CANCELLED)
                .done(true).finishedAtEpochMs(System.currentTimeMillis()).build();
        when(jobs.getStatusForUser("owned-job", "alice")).thenReturn(queued, cancelled);
        when(jobs.cancelForUser("owned-job", "alice")).thenReturn(true);
        StringWriter body = new StringWriter();
        HttpServletResponse response = responseWriter(body);
        try (MockedStatic<WidgetReviewJobStatusServlet> shared = mockStatic(WidgetReviewJobStatusServlet.class, CALLS_REAL_METHODS)) {
            shared.when(WidgetReviewJobStatusServlet::jobService).thenReturn(jobs);
            new WidgetReviewJobStatusServlet().doDelete(ownedRequest("alice"), response);
        }
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(body.toString().contains("CANCELLED"));
        verify(jobs, never()).cancel(anyString());
    }

    private HttpServletRequest ownedRequest(String username) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(username);
        when(request.getParameterValues("jobId")).thenReturn(new String[]{"owned-job"});
        return request;
    }

    private HttpServletResponse responseWriter(StringWriter body) throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        return response;
    }


    /**
     * Parasoft Jtest UTA: Test for destroy()
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#destroy()
     * @author bmcmullin
     */
    @Test
    public void testDestroy() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        underTest.destroy();

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doDelete(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doDelete(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete2() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doDelete(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete3() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doDelete(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete4() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for init()
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#init()
     * @author bmcmullin
     */
    @Test
    public void testInit() throws Throwable
    {
        // Given
        WidgetReviewJobStatusServlet underTest = new WidgetReviewJobStatusServlet();

        // When
        underTest.init();

    }

    /**
     * Parasoft Jtest UTA: Test for jobService()
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewJobStatusServlet#jobService()
     * @author bmcmullin
     */
    @Test
    public void testJobService() throws Throwable
    {
        // When
        ReviewJobService result = WidgetReviewJobStatusServlet.jobService();

    }
}
