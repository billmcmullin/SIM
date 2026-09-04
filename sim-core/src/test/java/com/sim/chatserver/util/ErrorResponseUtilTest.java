package com.sim.chatserver.util;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        ErrorResponseUtil.writeError(resp, status, message, null);

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
        ErrorResponseUtil.writeError(resp, status, message, null);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError3() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, null);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String)
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
        String message = "*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, null);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError5() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "message"; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, null);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError6() throws Throwable
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
    public void testWriteError7() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError8() throws Throwable
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

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError9() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        String requestId = null; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError10() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError11() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "message"; // UTA: configured value
        String requestId = null; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError12() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "message"; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError13() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String message = "*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        String requestId = null; // UTA: configured value
        ErrorResponseUtil.writeError(resp, status, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError14() throws Throwable
    {
        // When
        HttpServletResponse resp = null; // UTA: configured value
        int status = 1; // UTA: default value
        String code = "code"; // UTA: default value
        String message = "message"; // UTA: default value
        String requestId = "requestId"; // UTA: default value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError15() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError16() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = null; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError17() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = "********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        String requestId = null; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError18() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = "********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError19() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = "message"; // UTA: configured value
        String requestId = null; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError20() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = "message"; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError21() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = null; // UTA: configured value
        String message = "*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        String requestId = null; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for writeError(HttpServletResponse, int, String, String, String)
     *
     * @see com.sim.chatserver.util.ErrorResponseUtil#writeError(HttpServletResponse, int, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testWriteError22() throws Throwable
    {
        // When
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        int status = 1; // UTA: default value
        String code = "code"; // UTA: configured value
        String message = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        invokeWriteError(resp, status, code, message, requestId);

    }

    private static void invokeWriteError(
            HttpServletResponse resp,
            int status,
            String code,
            String message,
            String requestId
    ) throws Throwable {
        Method method = ErrorResponseUtil.class.getDeclaredMethod(
                "writeError",
                HttpServletResponse.class,
                int.class,
                String.class,
                String.class,
                String.class
        );
        method.setAccessible(true);
        try {
            method.invoke(null, resp, status, code, message, requestId);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            }
            throw ex;
        }
    }

    @Test
    public void testWriteErrorPrivateOverload_delegatesWithDefaultCode() throws Throwable {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(writer);

        Method method = ErrorResponseUtil.class.getDeclaredMethod(
                "writeError",
                HttpServletResponse.class,
                int.class,
                String.class
        );
        method.setAccessible(true);

        method.invoke(null, resp, 400, "bad input");

        verify(resp).setStatus(400);
        verify(resp).setContentType("application/json; charset=UTF-8");
    }

    // Merged from ErrorResponseUtilBranchTest
    @Test
        void trimTo_returnsEmptyWhenValueNullOrMaxNonPositive() throws Exception {
            Method trimTo = ErrorResponseUtil.class.getDeclaredMethod("trimTo", String.class, int.class);
            trimTo.setAccessible(true);
    
            assertEquals("", trimTo.invoke(null, null, 10));
            assertEquals("", trimTo.invoke(null, "abc", 0));
        }
}

