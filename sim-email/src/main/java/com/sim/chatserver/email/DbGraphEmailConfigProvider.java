package com.sim.chatserver.email;

public interface DbGraphEmailConfigProvider {

    GraphEmailConfig load();

    void save(GraphEmailConfig config, String updatedBy);
}
