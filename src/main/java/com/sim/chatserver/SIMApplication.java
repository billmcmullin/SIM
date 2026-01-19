package com.sim.chatserver;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class SIMApplication extends Application {
    // Scans for JAX-RS resources and providers automatically.
}
