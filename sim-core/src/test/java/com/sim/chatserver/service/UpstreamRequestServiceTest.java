package com.sim.chatserver.service;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.UpstreamRequestService.UpstreamConnectivityException;
import com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse;

import jakarta.json.JsonArray;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * Parasoft Jtest UTA: Test class for UpstreamRequestService
 *
 * @see com.sim.chatserver.service.UpstreamRequestService
 * @author bmcmullin
 */
public class UpstreamRequestServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = null; // UTA: configured value
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge2() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        int statusCodeResult = 413; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge3() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        int statusCodeResult = 414; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge4() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = null; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult);

        int statusCodeResult = 400; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge5() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = null; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult);

        int statusCodeResult = 422; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge6() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = null; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult);

        int statusCodeResult = 500; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge7() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "TOO LARGE"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult);

        int statusCodeResult = 400; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge8() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "TOO LARGE"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult);

        int statusCodeResult = 422; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge9() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "TOO LARGE"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult);

        int statusCodeResult = 500; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge10() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = "TOKEN"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 400; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge11() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = "TOKEN"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 422; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge12() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = "TOKEN"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 500; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge13() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = "bodyResult2"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 400; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge14() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = "bodyResult2"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 422; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge15() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = "bodyResult2"; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 500; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge16() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = ""; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 400; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge17() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = ""; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 422; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(UpstreamResponse)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#isLikelyContextTooLarge(UpstreamResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge18() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        UpstreamResponse resp = mock(UpstreamResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        String bodyResult2 = ""; // UTA: configured value
        when(resp.body()).thenReturn(bodyResult, bodyResult2);

        int statusCodeResult = 500; // UTA: configured value
        when(resp.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(resp);

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamUrl = null; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat2() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamUrl = "upstreamUrl"; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat3() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamUrl = null; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = "requestId"; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat4() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamUrl = "/CHAT"; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat5() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamUrl = "/STREAM-CHAT"; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat6() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamUrl = "upstreamUrl"; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = null; // UTA: configured value
        String mode = null; // UTA: configured value
        String sessionId = null; // UTA: configured value
        boolean reset = false; // UTA: default value
        JsonArray attachments = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        assertThrows(IOException.class, () -> {
            invokeSendChat(underTest, upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, String,
     *      boolean, JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat7() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamBaseOrEndpoint = null; // UTA: configured value
        String workspace = "workspace"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, String,
     *      boolean, JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat8() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamBaseOrEndpoint = "upstreamBaseOrEndpoint"; // UTA: configured value
        String workspace = "workspace"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, String,
     *      boolean, JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat9() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamBaseOrEndpoint = null; // UTA: configured value
        String workspace = "workspace"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = "requestId"; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, String,
     *      boolean, JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat10() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamBaseOrEndpoint = "/CHAT"; // UTA: configured value
        String workspace = "workspace"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, String,
     *      boolean, JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat11() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamBaseOrEndpoint = "/STREAM-CHAT"; // UTA: configured value
        String workspace = "workspace"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            invokeSendChat(underTest, upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService#sendChat(String, String, String, String, String, String,
     *      boolean, JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat12() throws Throwable
    {
        // Given
        UpstreamRequestService underTest = new UpstreamRequestService();

        // When
        String upstreamBaseOrEndpoint = "upstreamBaseOrEndpoint"; // UTA: configured value
        String workspace = "workspace"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = null; // UTA: configured value
        String mode = null; // UTA: configured value
        String sessionId = null; // UTA: configured value
        boolean reset = false; // UTA: default value
        JsonArray attachments = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        assertThrows(IOException.class, () -> {
            invokeSendChat(underTest, upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }


    /**
     * Consolidated from UpstreamRequestService_UpstreamConnectivityExceptionTest.java to keep one test class per production source file.
     */
    /**
     * Parasoft Jtest UTA: Test for code()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamConnectivityException#code()
     * @author bmcmullin
     */
    @Test
    public void testCode() throws Throwable
    {
        // Given
        String code = "code"; // UTA: default value
        String message = "message"; // UTA: default value
        Throwable cause = mock(Throwable.class);
        UpstreamConnectivityException underTest = newUpstreamConnectivityException(code, message, cause);

        // When
        String result = invokeUpstreamConnectivityCode(underTest);

        // Then - assertions for result of method code()
        assertEquals("code", result);

    }


    /**
     * Consolidated from UpstreamRequestService_UpstreamResponseTest.java to keep one test class per production source file.
     */
    /**
     * Parasoft Jtest UTA: Test for body()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#body()
     * @author bmcmullin
     */
    @Test
    public void testBody() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        String result = underTest.body();

        // Then - assertions for result of method body()
        assertEquals("body", result);

    }

    /**
     * Parasoft Jtest UTA: Test for contentType()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#contentType()
     * @author bmcmullin
     */
    @Test
    public void testContentType() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        String result = underTest.contentType();

        // Then - assertions for result of method contentType()
        assertEquals("contentType", result);

    }

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        Object arg0 = new Object(); // UTA: default value
        boolean result = underTest.equals(arg0);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for hashCode()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#hashCode()
     * @author bmcmullin
     */
    @Test
    public void testHashCode() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        int result = underTest.hashCode();

        // Then - assertions for result of method hashCode()
        // assertEquals(1, result);// UTA: Expected value may be unstable

    }

    /**
     * Parasoft Jtest UTA: Test for statusCode()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#statusCode()
     * @author bmcmullin
     */
    @Test
    public void testStatusCode() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        int result = underTest.statusCode();

        // Then - assertions for result of method statusCode()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("UpstreamResponse[statusCode=1, contentType=contentType, body=body]", result);

    }

    private static UpstreamConnectivityException newUpstreamConnectivityException(
            String code,
            String message,
            Throwable cause
    ) {
        try {
            Constructor<UpstreamConnectivityException> ctor = UpstreamConnectivityException.class
                    .getDeclaredConstructor(String.class, String.class, Throwable.class);
            ctor.setAccessible(true);
            return ctor.newInstance(code, message, cause);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to instantiate UpstreamConnectivityException for test", ex);
        }
    }

    private static String invokeUpstreamConnectivityCode(UpstreamConnectivityException ex) throws Exception {
        Method method = UpstreamConnectivityException.class.getDeclaredMethod("code");
        method.setAccessible(true);
        return (String) method.invoke(ex);
    }

    private static UpstreamResponse invokeSendChat(
            UpstreamRequestService underTest,
            String upstreamBaseOrEndpoint,
            String workspace,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments,
            String requestId
    ) throws Throwable {
        Method method = UpstreamRequestService.class.getDeclaredMethod(
                "sendChat",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                boolean.class,
                JsonArray.class,
                String.class
        );
        method.setAccessible(true);
        try {
            return (UpstreamResponse) method.invoke(
                    underTest,
                    upstreamBaseOrEndpoint,
                    workspace,
                    apiKey,
                    message,
                    mode,
                    sessionId,
                    reset,
                    attachments,
                    requestId
            );
        } catch (InvocationTargetException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            }
            throw ex;
        }
    }
}



