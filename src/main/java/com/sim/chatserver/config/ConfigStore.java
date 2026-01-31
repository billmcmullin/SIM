package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class ConfigStore {

    private static final String CREATE_TABLE_SQL
            = "CREATE TABLE IF NOT EXISTS server_config ("
            + "id SERIAL PRIMARY KEY, "
            + "server_host TEXT NOT NULL, "
            + "server_port INTEGER NOT NULL, "
            + "connection_info TEXT, "
            + "api_key TEXT NOT NULL, "
            + "updated_at TIMESTAMPTZ DEFAULT now()"
            + ")";

    private ConfigStore() {
        // utility
    }

    public static synchronized void ensureTable() throws Exception {
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        }
    }

    public static ServerConfig load() throws Exception {
        String sql = "SELECT server_host, server_port, connection_info, api_key FROM server_config ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new ServerConfig(
                        rs.getString("server_host"),
                        rs.getInt("server_port"),
                        rs.getString("connection_info"),
                        rs.getString("api_key")
                );
            }
            return null;
        }
    }

    public static void save(ServerConfig config) throws Exception {
        try (Connection conn = Database.getConnection(); Statement delete = conn.createStatement()) {
            delete.executeUpdate("DELETE FROM server_config");
        }

        String insert = "INSERT INTO server_config (server_host, server_port, connection_info, api_key) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, config.getServerHost());
            ps.setInt(2, config.getServerPort());
            ps.setString(3, config.getConnectionInfo());
            ps.setString(4, config.getApiKey());
            ps.executeUpdate();
        }
    }
}
