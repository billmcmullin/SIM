package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AdminTermServlet
 *
 * @see com.sim.chatserver.web.admin.AdminTermServlet
 * @author bmcmullin
 */
public class AdminTermServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

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
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete2() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

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
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete3() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete4() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete5() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete6() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete7() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        boolean deleteTermResult = true; // UTA: configured value
        when(termsStoreValue.deleteTerm(nullable(Long.class))).thenReturn(deleteTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete8() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        boolean deleteTermResult = false; // UTA: configured value
        when(termsStoreValue.deleteTerm(nullable(Long.class))).thenReturn(deleteTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete9() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        boolean deleteTermResult = true; // UTA: configured value
        when(termsStoreValue.deleteTerm(nullable(Long.class))).thenReturn(deleteTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete10() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        boolean deleteTermResult = false; // UTA: configured value
        when(termsStoreValue.deleteTerm(nullable(Long.class))).thenReturn(deleteTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete11() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete12() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.deleteTerm(nullable(Long.class))).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete13() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doDelete(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doDelete(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoDelete14() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.deleteTerm(nullable(Long.class))).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doDelete(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

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
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

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
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.listAll()).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.listAll()).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

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
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

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
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition createTermResult = null; // UTA: configured value
        when(termsStoreValue.createTerm(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(createTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition createTermResult = null; // UTA: configured value
        when(termsStoreValue.createTerm(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(createTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition createTermResult = mock(TermDefinition.class);
        Long getIdResult = 1L; // UTA: default value
        when(createTermResult.getId()).thenReturn(getIdResult);
        when(termsStoreValue.createTerm(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(createTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition createTermResult = mock(TermDefinition.class);
        Long getIdResult = 1L; // UTA: default value
        when(createTermResult.getId()).thenReturn(getIdResult);
        when(termsStoreValue.createTerm(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(createTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost9() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.createTerm(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost10() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.createTerm(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut2() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut3() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut4() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut5() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition updateTermResult = null; // UTA: configured value
        when(termsStoreValue.updateTerm(nullable(Long.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(updateTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut6() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition updateTermResult = null; // UTA: configured value
        when(termsStoreValue.updateTerm(nullable(Long.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(updateTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut7() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition updateTermResult = mock(TermDefinition.class);
        Long getIdResult = 1L; // UTA: default value
        when(updateTermResult.getId()).thenReturn(getIdResult);
        when(termsStoreValue.updateTerm(nullable(Long.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(updateTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut8() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        TermDefinition updateTermResult = mock(TermDefinition.class);
        Long getIdResult = 1L; // UTA: default value
        when(updateTermResult.getId()).thenReturn(getIdResult);
        when(termsStoreValue.updateTerm(nullable(Long.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(updateTermResult);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut9() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.updateTerm(nullable(Long.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPut(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.AdminTermServlet#doPut(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPut10() throws Throwable
    {
        // Given
        AdminTermServlet underTest = new AdminTermServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.updateTerm(nullable(Long.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenThrow(SQLException.class);
        underTest.termsStore = termsStoreValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPut(req, resp);

    }

}
