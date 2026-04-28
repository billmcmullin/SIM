package com.sim.chatserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardTemplateRenderer
 *
 * @see com.sim.chatserver.util.DashboardTemplateRenderer
 * @author bmcmullin
 */
public class DashboardTemplateRendererTest
{

    /**
     * Parasoft Jtest UTA: Test for clearTemplateCache()
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#clearTemplateCache()
     * @author bmcmullin
     */
    @Test
    public void testClearTemplateCache() throws Throwable
    {
        // When
        DashboardTemplateRenderer.clearTemplateCache();

    }

    /**
     * Parasoft Jtest UTA: Test for escapeForJs(String)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#escapeForJs(String)
     * @author bmcmullin
     */
    @Test
    public void testEscapeForJs() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        String result = DashboardTemplateRenderer.escapeForJs(value);

        // Then - assertions for result of method escapeForJs(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for escapeForJs(String)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#escapeForJs(String)
     * @author bmcmullin
     */
    @Test
    public void testEscapeForJs2() throws Throwable
    {
        // When
        String value = "value"; // UTA: default value
        String result = DashboardTemplateRenderer.escapeForJs(value);

        // Then - assertions for result of method escapeForJs(String)
        assertEquals("value", result);

    }

    /**
     * Parasoft Jtest UTA: Test for escapeHtml(String)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#escapeHtml(String)
     * @author bmcmullin
     */
    @Test
    public void testEscapeHtml() throws Throwable
    {
        // When
        String input = null; // UTA: configured value
        String result = DashboardTemplateRenderer.escapeHtml(input);

        // Then - assertions for result of method escapeHtml(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for escapeHtml(String)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#escapeHtml(String)
     * @author bmcmullin
     */
    @Test
    public void testEscapeHtml2() throws Throwable
    {
        // When
        String input = "input"; // UTA: default value
        String result = DashboardTemplateRenderer.escapeHtml(input);

        // Then - assertions for result of method escapeHtml(String)
        assertEquals("input", result);

    }

    /**
     * Parasoft Jtest UTA: Test for loadTemplateCached(ServletContext, String)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#loadTemplateCached(ServletContext, String)
     * @author bmcmullin
     */
    @Test
    public void testLoadTemplateCached() throws Throwable
    {
        // When
        ServletContext context = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(context.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        String path = "path"; // UTA: default value
        assertThrows(IOException.class, () -> {
            DashboardTemplateRenderer.loadTemplateCached(context, path);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for loadTemplateCached(ServletContext, String)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#loadTemplateCached(ServletContext, String)
     * @author bmcmullin
     */
    @Test
    public void testLoadTemplateCached2() throws Throwable
    {
        // When
        ServletContext context = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = mock(InputStream.class);
        doThrow(IOException.class).when(getResourceAsStreamResult).close();
        when(context.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        String path = "path"; // UTA: default value
        assertThrows(IOException.class, () -> {
            DashboardTemplateRenderer.loadTemplateCached(context, path);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for renderTemplate(String, Map)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#renderTemplate(String, Map)
     * @author bmcmullin
     */
    @Test
    public void testRenderTemplate() throws Throwable
    {
        // When
        String template = "template"; // UTA: default value
        Map<String, String> values = null; // UTA: configured value
        String result = DashboardTemplateRenderer.renderTemplate(template, values);

        // Then - assertions for result of method renderTemplate(String, Map)
        assertEquals("template", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderTemplate(String, Map)
     *
     * @see com.sim.chatserver.util.DashboardTemplateRenderer#renderTemplate(String, Map)
     * @author bmcmullin
     */
    @Test
    public void testRenderTemplate2() throws Throwable
    {
        // When
        String template = "template"; // UTA: default value
        Map<String, String> values = new HashMap<String, String>(); // UTA: default value
        String key = "key"; // UTA: default value
        String value = "value"; // UTA: default value
        values.put(key, value);
        String result = DashboardTemplateRenderer.renderTemplate(template, values);

        // Then - assertions for result of method renderTemplate(String, Map)
        assertEquals("template", result);

    }

}
