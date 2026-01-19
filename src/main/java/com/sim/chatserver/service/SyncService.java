package com.sim.chatserver.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton SyncService: integrate your HTTP client and normalization logic here.
 * The runSync method should:
 *  - read admin-configured base URL and API key (persisted)
 *  - fetch embeds and chats
 *  - normalize and upsert into DB (via JPA EntityManager)
 *  - compute term matches and persist
 */
@ApplicationScoped
public class SyncService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    public void runSync(String scope) {
        log.info("Sync not implemented yet. scope={}", scope);
        // TODO: implement fetching from remote API, normalization, upsert, and term matching.
    }
}
