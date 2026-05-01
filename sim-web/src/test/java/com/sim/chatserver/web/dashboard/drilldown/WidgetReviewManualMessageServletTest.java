package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.MapReduceConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewManualMessageServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet
 * @author bmcmullin
 */
public class WidgetReviewManualMessageServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        underTest.dsHolder = dsHolderValue;
        underTest.init();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for init()
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#init()
     * @author bmcmullin
     */
    @Test
    public void testInit() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        underTest.init();

    }
}
