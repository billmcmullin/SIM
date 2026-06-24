package com.sim.chatserver.email;

public record ResolvedEmailConfig(
        EmailConfig config,
        EmailConfigSource source,
        boolean valid,
        String message
        ) {

}
