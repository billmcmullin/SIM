package com.sim.chatserver.security;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for JwtFilter
 *
 * @see com.sim.chatserver.security.JwtFilter
 * @author bmcmullin
 */
public class JwtFilterTest
{

    /**
     * Parasoft Jtest UTA: Test for filter(ContainerRequestContext)
     *
     * @see com.sim.chatserver.security.JwtFilter#filter(ContainerRequestContext)
     * @author bmcmullin
     */
    @Test
    public void testFilter() throws Throwable
    {
        // Given
        JwtFilter underTest = new JwtFilter();

        // When
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String getMethodResult = "options"; // UTA: configured value
        when(requestContext.getMethod()).thenReturn(getMethodResult);
        underTest.filter(requestContext);

    }

    /**
     * Parasoft Jtest UTA: Test for filter(ContainerRequestContext)
     *
     * @see com.sim.chatserver.security.JwtFilter#filter(ContainerRequestContext)
     * @author bmcmullin
     */
    @Test
    public void testFilter2() throws Throwable
    {
        // Given
        JwtFilter underTest = new JwtFilter();

        // When
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String getMethodResult = ""; // UTA: configured value
        when(requestContext.getMethod()).thenReturn(getMethodResult);

        UriInfo getUriInfoResult = mock(UriInfo.class);
        String getPathResult = "AUTH"; // UTA: configured value
        when(getUriInfoResult.getPath()).thenReturn(getPathResult);
        when(requestContext.getUriInfo()).thenReturn(getUriInfoResult);
        underTest.filter(requestContext);

    }

    /**
     * Parasoft Jtest UTA: Test for filter(ContainerRequestContext)
     *
     * @see com.sim.chatserver.security.JwtFilter#filter(ContainerRequestContext)
     * @author bmcmullin
     */
    @Test
    public void testFilter3() throws Throwable
    {
        // Given
        JwtFilter underTest = new JwtFilter();

        // When
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String getHeaderStringResult = null; // UTA: configured value
        when(requestContext.getHeaderString(nullable(String.class))).thenReturn(getHeaderStringResult);

        String getMethodResult = ""; // UTA: configured value
        when(requestContext.getMethod()).thenReturn(getMethodResult);

        UriInfo getUriInfoResult = mock(UriInfo.class);
        String getPathResult = ""; // UTA: configured value
        when(getUriInfoResult.getPath()).thenReturn(getPathResult);
        when(requestContext.getUriInfo()).thenReturn(getUriInfoResult);
        assertThrows(NotAuthorizedException.class, () -> {
            underTest.filter(requestContext);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for filter(ContainerRequestContext)
     *
     * @see com.sim.chatserver.security.JwtFilter#filter(ContainerRequestContext)
     * @author bmcmullin
     */
    @Test
    public void testFilter4() throws Throwable
    {
        // Given
        JwtFilter underTest = new JwtFilter();

        // When
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String getHeaderStringResult = ""; // UTA: configured value
        when(requestContext.getHeaderString(nullable(String.class))).thenReturn(getHeaderStringResult);

        String getMethodResult = ""; // UTA: configured value
        when(requestContext.getMethod()).thenReturn(getMethodResult);

        UriInfo getUriInfoResult = mock(UriInfo.class);
        String getPathResult = ""; // UTA: configured value
        when(getUriInfoResult.getPath()).thenReturn(getPathResult);
        when(requestContext.getUriInfo()).thenReturn(getUriInfoResult);
        assertThrows(NotAuthorizedException.class, () -> {
            underTest.filter(requestContext);
        });

    }
}
