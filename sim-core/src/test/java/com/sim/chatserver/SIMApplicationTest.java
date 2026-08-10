package com.sim.chatserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.ApplicationPath;

class SIMApplicationTest {

    @Test
    void apiRootPath_matchesApplicationPathAnnotation() {
        ApplicationPath annotation = SIMApplication.class.getAnnotation(ApplicationPath.class);

        assertNotNull(annotation);
        assertEquals("/api", annotation.value());
        assertEquals("/api", SIMApplication.API_ROOT_PATH);
    }


    // Merged from SIMApplicationCoverageTest
    
    
        @Test
        void constructor_createsInstance() {
            SIMApplication app = new SIMApplication();
            assertNotNull(app);
        }
}
