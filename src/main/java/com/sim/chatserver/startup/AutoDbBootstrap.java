package com.sim.chatserver.startup;

import com.sim.chatserver.dto.DbConfig;
import com.sim.chatserver.service.ConfigStore;
import com.sim.chatserver.service.DbBootstrapService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AutoDbBootstrap {
    private static final Logger log = LoggerFactory.getLogger(AutoDbBootstrap.class);

    @Inject ConfigStore store;
    @Inject DbBootstrapService bootstrapSvc;
    @Inject AppDataSourceHolder holder;

    @PostConstruct
    public void init() {
        try {
            DbConfig cfg = store.loadEncrypted();
            if (cfg == null) {
                log.info("No persisted DB config found; waiting for admin to configure DB.");
                return;
            }
            var ds = bootstrapSvc.createAndTestDataSource(cfg.jdbcUrl, cfg.username, cfg.password, cfg.driverClass);
            bootstrapSvc.runFlywayMigrations(ds);
            var emf = bootstrapSvc.buildEntityManagerFactory(ds);
            holder.setDataSource(ds);
            holder.setEmf(emf);
            log.info("Auto DB bootstrap complete");
        } catch (Exception e) {
            log.error("Auto DB bootstrap failed", e);
        }
    }
}
