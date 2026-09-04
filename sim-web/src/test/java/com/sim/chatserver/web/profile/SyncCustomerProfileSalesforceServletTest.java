package com.sim.chatserver.web.profile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.model.CustomerProfile;
import com.sim.chatserver.model.CustomerProfileStore;
import com.sim.chatserver.salesforce.SalesforceClient;
import com.sim.chatserver.salesforce.SalesforceCustomerMatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for SyncCustomerProfileSalesforceServlet
 *
 * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet
 * @author bmcmullin
 */
public class SyncCustomerProfileSalesforceServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

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
     * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

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
     * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

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
     * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

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
     * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

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
     * @see com.sim.chatserver.web.profile.SyncCustomerProfileSalesforceServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        assertDoesNotThrow(() -> {
            underTest.doPost(req, resp);
        });

    }

    @Test
    public void testDoPost_success_savesProfileAndReturnsOk() throws Throwable
    {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        SalesforceClient mockedClient = mock(SalesforceClient.class);
        SalesforceCustomerMatch match = mock(SalesforceCustomerMatch.class);
        when(match.fullName()).thenReturn("Alice Example");
        when(match.contactId()).thenReturn("003-contact");
        when(match.accountId()).thenReturn("001-account");
        when(match.emailValue()).thenReturn("alice@example.com");
        when(match.phoneValue()).thenReturn("+1-555-0100");
        when(match.titleValue()).thenReturn("Manager");
        when(match.departmentValue()).thenReturn("Support");
        when(match.rawJsonValue()).thenReturn("{\"raw\":true}");
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenReturn(match);

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn(mock(Object.class));
            when(req.getSession(anyBoolean())).thenReturn(session);
            when(req.getParameterValues("sessionId")).thenReturn(null);
            when(req.getParameterValues("friendlyName")).thenReturn(new String[] { "Alice" });

            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.isCommitted()).thenReturn(false);
            StringWriter body = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(body));

            AtomicReference<CustomerProfile> saved = new AtomicReference<>();
            try (MockedStatic<CustomerProfileStore> storeMock = mockStatic(CustomerProfileStore.class)) {
                storeMock.when(() -> CustomerProfileStore.saveProfile(any(CustomerProfile.class)))
                        .thenAnswer(inv -> {
                            saved.set(inv.getArgument(0));
                            return null;
                        });

                underTest.doPost(req, resp);

                verify(resp).setStatus(HttpServletResponse.SC_OK);
                CustomerProfile profile = saved.get();
                assertNotNull(profile);
                assertEquals("Alice Example", profile.getFriendlyName());
                assertEquals("003-contact", profile.getSalesforceContactId());
                assertEquals("001-account", profile.getSalesforceAccountId());
                assertEquals("alice@example.com", profile.getEmail());
                assertEquals("+1-555-0100", profile.getPhone());
                assertEquals("Manager", profile.getTitle());
                assertEquals("Support", profile.getDepartment());
            }
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_salesforceClientException_usesStatusCode() throws Throwable
    {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        SalesforceClient mockedClient = mock(SalesforceClient.class);
        SalesforceClient.SalesforceClientException failure = newSalesforceClientException(502, "Bad gateway");
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenThrow(failure);

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn(mock(Object.class));
            when(req.getSession(anyBoolean())).thenReturn(session);
            when(req.getParameterValues("sessionId")).thenReturn(null);
            when(req.getParameterValues("friendlyName")).thenReturn(new String[] { "Alice" });

            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.isCommitted()).thenReturn(false);
            when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

            underTest.doPost(req, resp);

            verify(resp).setStatus(502);
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_invalidSessionIdFormat_returnsBadRequest() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        HttpServletRequest req = authenticatedRequest("bad value with spaces", null);
        HttpServletResponse resp = jsonResponse();

        underTest.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testDoPost_invalidFriendlyNameFormat_returnsBadRequest() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        HttpServletRequest req = authenticatedRequest(null, "bad@name");
        HttpServletResponse resp = jsonResponse();

        underTest.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testDoPost_friendlyNameLoadedFromStoreAndUsedForLookup() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        SalesforceClient mockedClient = mock(SalesforceClient.class);
        SalesforceCustomerMatch match = mock(SalesforceCustomerMatch.class);
        when(match.fullName()).thenReturn("Resolved Name");
        when(match.contactId()).thenReturn("003-contact");
        when(match.accountId()).thenReturn("001-account");
        when(match.emailValue()).thenReturn("resolved@example.com");
        when(match.phoneValue()).thenReturn("+1-555-0101");
        when(match.titleValue()).thenReturn("Engineer");
        when(match.departmentValue()).thenReturn("R&D");
        when(match.rawJsonValue()).thenReturn("{}");
        when(mockedClient.lookupBestCustomerMatch("Cached Name")).thenReturn(match);

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest("session-123", null);
            HttpServletResponse resp = jsonResponse();

            CustomerProfile existing = mock(CustomerProfile.class);
            when(existing.getFriendlyName()).thenReturn("Cached Name");

            try (MockedStatic<CustomerProfileStore> storeMock = mockStatic(CustomerProfileStore.class)) {
                storeMock.when(() -> CustomerProfileStore.loadBySessionId("session-123")).thenReturn(existing);

                underTest.doPost(req, resp);

                verify(mockedClient).lookupBestCustomerMatch("Cached Name");
                verify(resp).setStatus(HttpServletResponse.SC_OK);
            }
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_lookupReturnsNull_returnsNotFound() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        SalesforceClient mockedClient = mock(SalesforceClient.class);
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenReturn(null);

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest(null, "Alice");
            HttpServletResponse resp = jsonResponse();

            underTest.doPost(req, resp);

            verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_lookupIllegalState_returnsBadRequest() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        SalesforceClient mockedClient = mock(SalesforceClient.class);
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenThrow(new IllegalStateException("bad request state"));

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest(null, "Alice");
            HttpServletResponse resp = jsonResponse();

            underTest.doPost(req, resp);

            verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_lookupInterrupted_returnsBadGatewayAndSetsInterruptFlag() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        SalesforceClient mockedClient = mock(SalesforceClient.class);
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenThrow(new InterruptedException("interrupted"));

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest(null, "Alice");
            HttpServletResponse resp = jsonResponse();

            underTest.doPost(req, resp);

            verify(resp).setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_lookupIOException_returnsBadGateway() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        SalesforceClient mockedClient = mock(SalesforceClient.class);
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenThrow(new IOException("io"));

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest(null, "Alice");
            HttpServletResponse resp = jsonResponse();

            underTest.doPost(req, resp);

            verify(resp).setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_lookupIllegalArgument_returnsBadGateway() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        SalesforceClient mockedClient = mock(SalesforceClient.class);
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenThrow(new IllegalArgumentException("bad input"));

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest(null, "Alice");
            HttpServletResponse resp = jsonResponse();

            underTest.doPost(req, resp);

            verify(resp).setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_saveProfileSQLException_returnsInternalServerError() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();

        SalesforceClient mockedClient = mock(SalesforceClient.class);
        SalesforceCustomerMatch match = mock(SalesforceCustomerMatch.class);
        when(match.fullName()).thenReturn("Alice Example");
        when(match.contactId()).thenReturn("003-contact");
        when(match.accountId()).thenReturn("001-account");
        when(match.emailValue()).thenReturn("alice@example.com");
        when(match.phoneValue()).thenReturn("+1-555-0100");
        when(match.titleValue()).thenReturn("Manager");
        when(match.departmentValue()).thenReturn("Support");
        when(match.rawJsonValue()).thenReturn("{\"raw\":true}");
        when(mockedClient.lookupBestCustomerMatch("Alice")).thenReturn(match);

        SalesforceClient originalClient = replaceSalesforceClient(mockedClient);
        try {
            HttpServletRequest req = authenticatedRequest(null, "Alice");
            HttpServletResponse resp = jsonResponse();

            try (MockedStatic<CustomerProfileStore> storeMock = mockStatic(CustomerProfileStore.class)) {
                storeMock.when(() -> CustomerProfileStore.saveProfile(any(CustomerProfile.class)))
                        .thenThrow(new java.sql.SQLException("db write failure"));

                underTest.doPost(req, resp);

                verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
            }
        } finally {
            replaceSalesforceClient(originalClient);
        }
    }

    @Test
    public void testDoPost_writeJsonFailureFallsBackToSendError() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(anyBoolean())).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);
        when(resp.getWriter()).thenThrow(new IOException("writer unavailable"));

        underTest.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    public void testDoPost_sendErrorIOException_isHandled() throws Throwable {
        SyncCustomerProfileSalesforceServlet underTest = new SyncCustomerProfileSalesforceServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(anyBoolean())).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);
        when(resp.getWriter()).thenThrow(new IOException("writer unavailable"));
        org.mockito.Mockito.doThrow(new IOException("sendError failure"))
                .when(resp)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");

        assertDoesNotThrow(() -> underTest.doPost(req, resp));
        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    private HttpServletRequest authenticatedRequest(String sessionId, String friendlyName) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(mock(Object.class));
        when(req.getSession(anyBoolean())).thenReturn(session);
        when(req.getParameterValues("sessionId"))
                .thenReturn(sessionId == null ? null : new String[] { sessionId });
        when(req.getParameterValues("friendlyName"))
                .thenReturn(friendlyName == null ? null : new String[] { friendlyName });
        return req;
    }

    private HttpServletResponse jsonResponse() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        return resp;
    }

    private static SalesforceClient.SalesforceClientException newSalesforceClientException(int statusCode, String message)
            throws Exception {
        Constructor<SalesforceClient.SalesforceClientException> ctor = SalesforceClient.SalesforceClientException.class
                .getDeclaredConstructor(int.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(statusCode, message);
    }

    private static SalesforceClient replaceSalesforceClient(SalesforceClient replacement) throws Exception {
        Field field = SyncCustomerProfileSalesforceServlet.class.getDeclaredField("SALESFORCE_CLIENT");
        field.setAccessible(true);
        SalesforceClient original = (SalesforceClient) field.get(null);

        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Object base = unsafe.staticFieldBase(field);
        long offset = unsafe.staticFieldOffset(field);
        unsafe.putObject(base, offset, replacement);
        return original;
    }
}
