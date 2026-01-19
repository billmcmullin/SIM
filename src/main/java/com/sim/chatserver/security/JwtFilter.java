package com.sim.chatserver.security;

import java.io.IOException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.annotation.Priority;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Allow OPTIONS (CORS preflight)
        String method = requestContext.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) return;

        String path = requestContext.getUriInfo().getPath();
        if (path == null) path = "";
        String p = path.toLowerCase();

        // Allow unauthenticated endpoints used for bootstrap:
        // any path that contains "auth" (auth/login), or "admin/db" (db status/test/connect), or health endpoints
        if (p.contains("auth") || p.contains("admin/db") || p.equals("health") || p.startsWith("health/")) {
            return;
        }

        String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Bearer token required");
        }
        String token = auth.substring("Bearer ".length());
        try {
            Jws<Claims> claims = JwtUtil.parseToken(token);
            requestContext.setProperty("username", claims.getBody().getSubject());
            requestContext.setProperty("role", claims.getBody().get("role"));
        } catch (Exception e) {
            throw new NotAuthorizedException("Invalid token");
        }
    }
}
