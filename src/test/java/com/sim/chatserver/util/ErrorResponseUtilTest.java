package com.sim.chatserver.util;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for ErrorResponseUtil
 *
 * @see com.sim.chatserver.util.ErrorResponseUtil
 * @author bmcmullin
 */
public class ErrorResponseUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError() throws Throwable
    {
        // When
        HttpServletResponse resp = null; // UTA: configured value
        int status = 1; // UTA: default value
        String message = "message"; // UTA: default value
        ErrorResponseUtil.writeError(resp, status, message);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError2() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = null; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError3() throws Throwable
    {
        // When
        HttpServletResponse resp = null; // UTA: configured value
        int status = 1; // UTA: default value
        String message = "message"; // UTA: default value
        String requestId = "requestId"; // UTA: default value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError4() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = null; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

}
