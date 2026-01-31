package com.sim.chatserver.config;

public class ServerConfig {

    private final String serverHost;
    private final int serverPort;
    private final String connectionInfo;
    private final String apiKey;

    public ServerConfig(String serverHost, int serverPort, String connectionInfo, String apiKey) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connectionInfo = connectionInfo;
        this.apiKey = apiKey;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getConnectionInfo() {
        return connectionInfo;
    }

    public String getApiKey() {
        return apiKey;
    }
}
