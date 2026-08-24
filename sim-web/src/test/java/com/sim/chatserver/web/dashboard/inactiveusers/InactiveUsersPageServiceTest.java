package com.sim.chatserver.web.dashboard.inactiveusers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class InactiveUsersPageServiceTest {

    @Test
    void handleGet_whenUnauthenticated_forwardsToLogin() throws Exception {
        InactiveUsersPageService service = new InactiveUsersPageService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        service.handleGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void privateHelpers_coverParseAndSessionIdSanitization() throws Exception {
        InactiveUsersPageService service = new InactiveUsersPageService();

        Method parseInt = InactiveUsersPageService.class.getDeclaredMethod("parseInt", String.class, int.class);
        parseInt.setAccessible(true);
        assertEquals(7, parseInt.invoke(service, "7", 3));
        assertEquals(3, parseInt.invoke(service, "bad", 3));

        Method sanitizeSessionId = InactiveUsersPageService.class.getDeclaredMethod("sanitizeSessionId", String.class);
        sanitizeSessionId.setAccessible(true);
        assertEquals("session_1", sanitizeSessionId.invoke(service, " session_1 "));
        assertNull(sanitizeSessionId.invoke(service, "bad id"));
        assertNull(sanitizeSessionId.invoke(service, new Object[]{null}));
    }
}
