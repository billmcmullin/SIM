package com.sim.chatserver.config;

public class ServerConfig {

    private String serverHost;
    private int serverPort;
    private String connectionInfo;
    private String apiKey;
    private String workspaceName;

    public ServerConfig() {
    }

    public ServerConfig(String serverHost, int serverPort, String connectionInfo, String apiKey, String workspaceName) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connectionInfo = connectionInfo;
        this.apiKey = apiKey;
        this.workspaceName = workspaceName;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
