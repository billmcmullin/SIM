package com.sim.chatserver.security;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
/**
 * Parasoft Jtest UTA: Test class for SecurityHeadersFilter
 *
 * @see com.sim.chatserver.security.SecurityHeadersFilter
 * @author bmcmullin
 */
public class SecurityHeadersFilterTest
{

    /**
     * Parasoft Jtest UTA: Test for doFilter(ServletRequest, ServletResponse, FilterChain)
     *
     * @see com.sim.chatserver.security.SecurityHeadersFilter#doFilter(ServletRequest, ServletResponse, FilterChain)
     * @author bmcmullin
     */
    @Test
    public void testDoFilter() throws Throwable
    {
        // Given
        SecurityHeadersFilter underTest = new SecurityHeadersFilter();

        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/chat-server/dashboard");
        when(request.getContextPath()).thenReturn("/chat-server");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        underTest.doFilter(request, response, chain);

        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        verify(response).setHeader("Pragma", "no-cache");

    }

    /**
     * Parasoft Jtest UTA: Test for doFilter(ServletRequest, ServletResponse, FilterChain)
     *
     * @see com.sim.chatserver.security.SecurityHeadersFilter#doFilter(ServletRequest, ServletResponse, FilterChain)
     * @author bmcmullin
     */
    @Test
    public void testDoFilter2() throws Throwable
    {
        // Given
        SecurityHeadersFilter underTest = new SecurityHeadersFilter();

        // When
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        underTest.doFilter(request, response, chain);

    }

    /**
     * Parasoft Jtest UTA: Test for static asset cache behavior.
     */
    @Test
    public void testDoFilter_StaticAssetCaching() throws Throwable
    {
        // Given
        SecurityHeadersFilter underTest = new SecurityHeadersFilter();

        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/chat-server/assets/css/app.css");
        when(request.getContextPath()).thenReturn("/chat-server");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        underTest.doFilter(request, response, chain);

        verify(response).setHeader("Cache-Control", "public, max-age=604800, immutable");
        verify(response).setDateHeader(eq("Expires"), anyLong());
        verify(response, never()).setHeader("Pragma", "no-cache");
    }
}
