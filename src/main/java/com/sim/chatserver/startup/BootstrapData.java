package com.sim.chatserver.startup;

import com.sim.chatserver.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps an initial admin user if none exists.
 */
@ApplicationScoped
public class BootstrapData {
    private static final Logger log = LoggerFactory.getLogger(BootstrapData.class);

    @Inject
    UserService userService;

    @PostConstruct
    public void init() {
        try {
            userService.ensureAdminExists();
        } catch (Exception e) {
            log.error("Failed to bootstrap admin user", e);
        }
    }
}
