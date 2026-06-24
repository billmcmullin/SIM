package com.sim.chatserver.email;

public record EmailConfig(
        String host,
        int port,
        boolean auth,
        boolean startTls,
        boolean ssl,
        String username,
        String password,
        String defaultFrom
        ) {

}
