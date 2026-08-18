package com.sim.chatserver;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Registers the Jakarta REST (JAX-RS) application. All resources will be
 * available under /api.
 */
@ApplicationPath("/api")
public class SIMApplication extends Application {
    static final String API_ROOT_PATH = "/api";

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }
}
