package com.sim.chatserver.dto;

public class DbConfig {
    public String jdbcUrl;
    public String username;
    public String password;
    public String driverClass; // optional, e.g. org.postgresql.Driver

    public DbConfig() {}
}
