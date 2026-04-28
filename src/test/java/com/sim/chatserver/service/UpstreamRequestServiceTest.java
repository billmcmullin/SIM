package com.sim.chatserver.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.UpstreamRequestService.UpstreamConnectivityException;
import com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse;

import jakarta.json.JsonArray;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

        // Then - assertions for result of method isLikelyContextTooLarge(UpstreamRequestService.UpstreamResponse)
        assertFalse(result);

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

        // Then - assertions for result of method isLikelyContextTooLarge(UpstreamRequestService.UpstreamResponse)
        assertTrue(result);

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

        // Then - assertions for result of method isLikelyContextTooLarge(UpstreamRequestService.UpstreamResponse)
        assertFalse(result);

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

        // Then - assertions for result of method isLikelyContextTooLarge(UpstreamRequestService.UpstreamResponse)
        assertFalse(result);

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
            underTest.sendChat(upstreamUrl, apiKey, message, mode, sessionId, reset, attachments, requestId);
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
            underTest.sendChat(upstreamUrl, apiKey, message, mode, sessionId, reset, attachments, requestId);
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
        String upstreamUrl = "/CHAT"; // UTA: configured value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        String requestId = null; // UTA: configured value
        assertThrows(UpstreamConnectivityException.class, () -> {
            underTest.sendChat(upstreamUrl, apiKey, message, mode, sessionId, reset, attachments, requestId);
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
    public void testSendChat4() throws Throwable
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
            underTest.sendChat(upstreamBaseOrEndpoint, workspace, apiKey, message, mode, sessionId, reset, attachments, requestId);
        });

    }

}
