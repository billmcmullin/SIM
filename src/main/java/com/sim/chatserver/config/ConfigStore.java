package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;

public final class ConfigStore {

    private static final String TABLE_NAME = "server_config";
    private static final String CREATE_TABLE_SQL
            = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + "id BIGSERIAL PRIMARY KEY, "
            + "server_host TEXT, "
            + "server_port INTEGER, "
            + "connection_info TEXT, "
            + "api_key TEXT, "
            + "workspace_name TEXT)";
    private static final String SELECT_SQL
            = "SELECT server_host, server_port, connection_info, api_key, workspace_name FROM " + TABLE_NAME
            + " ORDER BY id DESC LIMIT 1";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME;
    private static final String INSERT_SQL
            = "INSERT INTO " + TABLE_NAME
            + " (server_host, server_port, connection_info, api_key, workspace_name) VALUES (?, ?, ?, ?, ?)";

    private static volatile AppDataSourceHolder dsHolder;

    private ConfigStore() {
    }

    public static void setAppDataSourceHolder(AppDataSourceHolder holder) {
        dsHolder = holder;
    }

    public static void ensureTable() throws SQLException {
        try (Connection conn = getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(CREATE_TABLE_SQL)) {
            ps.execute();
        }
        try (Connection conn = getDataSource().getConnection()) {
            ensureWorkspaceColumn(conn);
        }
    }

    public static ServerConfig load() throws SQLException {
        ensureTable();
        try (Connection conn = getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_SQL); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                ServerConfig config = new ServerConfig();
                config.setServerHost(rs.getString("server_host"));
                config.setServerPort(rs.getInt("server_port"));
                config.setConnectionInfo(rs.getString("connection_info"));
                config.setApiKey(rs.getString("api_key"));
                config.setWorkspaceName(rs.getString("workspace_name"));
                return config;
            }
        }
        return new ServerConfig();
    }

    public static void save(ServerConfig config) throws SQLException {
        ensureTable();
        try (Connection conn = getDataSource().getConnection(); PreparedStatement deleteStmt = conn.prepareStatement(DELETE_SQL)) {
            deleteStmt.executeUpdate();
        }
        try (Connection conn = getDataSource().getConnection(); PreparedStatement insertStmt = conn.prepareStatement(INSERT_SQL)) {
            insertStmt.setString(1, config.getServerHost());
            insertStmt.setInt(2, config.getServerPort());
            insertStmt.setString(3, config.getConnectionInfo());
            insertStmt.setString(4, config.getApiKey());
            insertStmt.setString(5, config.getWorkspaceName());
            insertStmt.executeUpdate();
        }
    }

    public static void saveWorkspaceName(String workspaceName) throws SQLException {
        ServerConfig config = load();
        config.setWorkspaceName(workspaceName);
        save(config);
    }

    private static void ensureWorkspaceColumn(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, TABLE_NAME, "workspace_name")) {
            if (!columns.next()) {
                try (PreparedStatement alter = conn.prepareStatement(
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN workspace_name TEXT")) {
                    alter.execute();
                }
            }
        }
    }

    private static DataSource getDataSource() {
        AppDataSourceHolder holder = dsHolder;
        if (holder == null) {
            holder = CDI.current().select(AppDataSourceHolder.class).get();
            dsHolder = holder;
        }
        if (holder == null) {
            throw new IllegalStateException("AppDataSourceHolder is not initialized");
        }
        return holder.getDataSource();
    }
}
