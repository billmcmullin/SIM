package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for TermsCsvServlet
 *
 * @see com.sim.chatserver.web.admin.TermsCsvServlet
 * @author bmcmullin
 */
public class TermsCsvServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = null; // UTA: configured value
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Helper method to set private field termsStore
     */
    private static <T> void setPrivateField(Object object, Class<?> fieldClass, String fieldName, T value)
    {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException e) {
            throw (AssertionError) new AssertionError("No such field found").initCause(e);
        } catch (IllegalAccessException e) {
            throw (AssertionError) new AssertionError("Unable to access the specified private field").initCause(e);
        } catch (SecurityException e) {
            throw (AssertionError) new AssertionError("There was a security exception when attempting to access a private field").initCause(e);
        }
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = null; // UTA: configured value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = null; // UTA: configured value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(item.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = "getNameResult"; // UTA: default value
        String getNameResult2 = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult, getNameResult2);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = "getDescriptionResult"; // UTA: default value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = null; // UTA: configured value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(item.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = null; // UTA: configured value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = "getMatchPatternResult"; // UTA: default value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(item.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = null; // UTA: configured value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = null; // UTA: configured value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = "getMatchTypeResult"; // UTA: default value
        when(item.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = null; // UTA: configured value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = null; // UTA: configured value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(item.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet11() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = null; // UTA: configured value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = null; // UTA: configured value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(item.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        TermDefinition item2 = mock(TermDefinition.class);
        String getDescriptionResult2 = "getDescriptionResult2"; // UTA: default value
        when(item2.getDescription()).thenReturn(getDescriptionResult2);

        String getMatchPatternResult2 = "getMatchPatternResult2"; // UTA: default value
        when(item2.getMatchPattern()).thenReturn(getMatchPatternResult2);

        String getMatchTypeResult2 = "getMatchTypeResult2"; // UTA: default value
        when(item2.getMatchType()).thenReturn(getMatchTypeResult2);

        String getNameResult2 = "getNameResult2"; // UTA: default value
        when(item2.getName()).thenReturn(getNameResult2);
        listAllResult.add(item2);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet12() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet13() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getNameResult = "getNameResult"; // UTA: default value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet14() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = "getDescriptionResult"; // UTA: default value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet15() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        String getDescriptionResult = null; // UTA: configured value
        when(item.getDescription()).thenReturn(getDescriptionResult);

        String getMatchPatternResult = "getMatchPatternResult"; // UTA: default value
        when(item.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getNameResult = null; // UTA: configured value
        when(item.getName()).thenReturn(getNameResult);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStoreValue).listAll();
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet16() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        when(termsStoreValue.listAll()).thenThrow(SQLException.class);
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet17() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();
        TermsStore termsStoreValue = mock(TermsStore.class);
        setPrivateField(underTest, TermsCsvServlet.class, "termsStore", termsStoreValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        doThrow(IOException.class).when(getOutputStreamResult).write(nullable(byte[].class));
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = null; // UTA: configured value
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = null; // UTA: configured value
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        Part getPartResult = mock(Part.class);
        InputStream getInputStreamResult = mock(InputStream.class);
        when(getPartResult.getInputStream()).thenReturn(getInputStreamResult);
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = mock(Part.class);
        InputStream getInputStreamResult = mock(InputStream.class);
        when(getPartResult.getInputStream()).thenReturn(getInputStreamResult);
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TermsCsvServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        TermsCsvServlet underTest = new TermsCsvServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = mock(Part.class);
        InputStream getInputStreamResult = mock(InputStream.class);
        doThrow(IOException.class).when(getInputStreamResult).close();
        when(getPartResult.getInputStream()).thenReturn(getInputStreamResult);
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

}
