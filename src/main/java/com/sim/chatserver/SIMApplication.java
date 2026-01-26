package com.sim.chatserver;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Registers the Jakarta REST (JAX-RS) application. All resources will be
 * available under /api.
 */
@ApplicationPath("/api")
public class SIMApplication extends Application {
    // empty – relies on classpath scanning of @Path resources
}
