package com.sim.chatserver.config;

/**
 * Simple POJO used when an admin switches to an external database.
 */
public class DbConfig {

    private String host;
    private String port;
    private String dbName;
    private String jdbcUrl;
    private String username;
    private String password;
    private int maxPoolSize = 10;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public String getHost() {
        return host;
    }

    final void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    final void setPort(String port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    final void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    final void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    final void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    final void setPassword(String password) {
        this.password = password;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    final void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
