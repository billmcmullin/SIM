package com.sim.chatserver.config;

public class ServerConfig {

    private String serverHost;
    private int serverPort;
    private String connectionInfo;
    private String apiKey;
    private String workspaceName;

    // Salesforce fields (loaded from DB config store)
    private String salesforceInstanceUrl;
    private String salesforceApiKey;

    // Salesforce OAuth refresh fields
    private String salesforceLoginUrl;
    private String salesforceClientId;
    private String salesforceClientSecret;
    private String salesforceRefreshToken;

    // Salesforce username + password + API token login fields
    private String salesforceUsername;
    private String salesforcePassword;
    private String salesforceApiToken;

    // AWS EC2 fields
    private String awsRegion;
    private String awsInstanceId;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

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

    final void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    final void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getConnectionInfo() {
        return connectionInfo;
    }

    final void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getApiKey() {
        return apiKey;
    }

    final void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    final void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getSalesforceInstanceUrl() {
        return salesforceInstanceUrl;
    }

    public void setSalesforceInstanceUrl(String salesforceInstanceUrl) {
        this.salesforceInstanceUrl = salesforceInstanceUrl;
    }

    public String getSalesforceApiKey() {
        return salesforceApiKey;
    }

    public void setSalesforceApiKey(String salesforceApiKey) {
        this.salesforceApiKey = salesforceApiKey;
    }

    public String getSalesforceLoginUrl() {
        return salesforceLoginUrl;
    }

    public void setSalesforceLoginUrl(String salesforceLoginUrl) {
        this.salesforceLoginUrl = salesforceLoginUrl;
    }

    public String getSalesforceClientId() {
        return salesforceClientId;
    }

    public void setSalesforceClientId(String salesforceClientId) {
        this.salesforceClientId = salesforceClientId;
    }

    public String getSalesforceClientSecret() {
        return salesforceClientSecret;
    }

    public void setSalesforceClientSecret(String salesforceClientSecret) {
        this.salesforceClientSecret = salesforceClientSecret;
    }

    public String getSalesforceRefreshToken() {
        return salesforceRefreshToken;
    }

    public void setSalesforceRefreshToken(String salesforceRefreshToken) {
        this.salesforceRefreshToken = salesforceRefreshToken;
    }

    public String getSalesforceUsername() {
        return salesforceUsername;
    }

    public void setSalesforceUsername(String salesforceUsername) {
        this.salesforceUsername = salesforceUsername;
    }

    public String getSalesforcePassword() {
        return salesforcePassword;
    }

    public void setSalesforcePassword(String salesforcePassword) {
        this.salesforcePassword = salesforcePassword;
    }

    public String getSalesforceApiToken() {
        return salesforceApiToken;
    }

    public void setSalesforceApiToken(String salesforceApiToken) {
        this.salesforceApiToken = salesforceApiToken;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public String getAwsInstanceId() {
        return awsInstanceId;
    }

    public void setAwsInstanceId(String awsInstanceId) {
        this.awsInstanceId = awsInstanceId;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }
}
