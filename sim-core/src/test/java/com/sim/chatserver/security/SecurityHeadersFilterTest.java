package com.sim.chatserver.security;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
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
        ServletRequest request = mock(ServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        underTest.doFilter(request, response, chain);

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
}
