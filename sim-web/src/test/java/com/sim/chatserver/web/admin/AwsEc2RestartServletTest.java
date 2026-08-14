package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class AwsEc2RestartServletTest {

    @Test
    void doPost_returnsUnauthorized_whenNoSession() throws Exception {
        AwsEc2RestartServlet servlet = new AwsEc2RestartServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);
        mockJsonOutput(resp);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doPost_returnsForbidden_whenNotAdmin() throws Exception {
        AwsEc2RestartServlet servlet = new AwsEc2RestartServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user1");
        when(session.getAttribute("role")).thenReturn("USER");
        mockJsonOutput(resp);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void doPost_returnsBadRequest_whenFieldsMissing() throws Exception {
        AwsEc2RestartServlet servlet = new AwsEc2RestartServlet();
        HttpServletRequest req = requestWithParams(Map.of());
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        mockJsonOutput(resp);

        try (MockedStatic<EncryptedDbConfigStore> cfgMock = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            cfgMock.when(EncryptedDbConfigStore::load).thenReturn(new ServerConfig());
            servlet.doPost(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doPost_returnsOk_whenRebootSucceeds() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();

        AwsEc2RestartServlet servlet = org.mockito.Mockito.spy(new AwsEc2RestartServlet());
        org.mockito.Mockito.doAnswer(invocation -> {
            String region = invocation.getArgument(0, String.class);
            String accessKeyId = invocation.getArgument(1, String.class);
            String secretAccessKey = invocation.getArgument(2, String.class);
            String instanceId = invocation.getArgument(3, String.class);
            captured.set(region + ":" + instanceId + ":" + accessKeyId + ":" + secretAccessKey);
            return null;
        }).when(servlet).rebootEc2Instance(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());

        HttpServletRequest req = requestWithParams(Map.of(
                "awsRegion", new String[]{"us-east-1"},
                "awsInstanceId", new String[]{"i-0123456789abcdef0"},
                "awsAccessKeyId", new String[]{"AKIATEST123"},
                "awsSecretAccessKey", new String[]{"secret"},
                "restartConfirmed", new String[]{"true"}
        ));
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        mockJsonOutput(resp);

        try (MockedStatic<EncryptedDbConfigStore> cfgMock = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            cfgMock.when(EncryptedDbConfigStore::load).thenReturn(new ServerConfig());
            servlet.doPost(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        assertEquals("us-east-1:i-0123456789abcdef0:AKIATEST123:secret", captured.get());
    }

    @Test
    void doPost_returnsBadRequest_whenRestartNotConfirmed() throws Exception {
        AwsEc2RestartServlet servlet = new AwsEc2RestartServlet();

        HttpServletRequest req = requestWithParams(Map.of(
                "awsRegion", new String[]{"us-east-1"},
                "awsInstanceId", new String[]{"i-0123456789abcdef0"},
                "awsAccessKeyId", new String[]{"AKIATEST123"},
                "awsSecretAccessKey", new String[]{"secret"},
                "restartConfirmed", new String[]{"false"}
        ));
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        mockJsonOutput(resp);

        try (MockedStatic<EncryptedDbConfigStore> cfgMock = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            cfgMock.when(EncryptedDbConfigStore::load).thenReturn(new ServerConfig());
            servlet.doPost(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    private static HttpServletRequest requestWithParams(Map<String, String[]> params) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameterValues(org.mockito.ArgumentMatchers.any(String.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            return params.get(key);
        });
        return req;
    }

    private static void mockJsonOutput(HttpServletResponse resp) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // No-op
            }

            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };
        when(resp.getOutputStream()).thenReturn(servletOut);
    }
}
