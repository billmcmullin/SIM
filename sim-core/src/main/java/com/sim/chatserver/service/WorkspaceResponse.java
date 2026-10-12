package com.sim.chatserver.service;

public final class WorkspaceResponse {

    private static final String EMPTY = "";

    private final int statusCode;
    private final String body;
    private final String contentType;

    WorkspaceResponse(int statusCode, String body, String contentType) {
        this.statusCode = statusCode;
        this.body = body == null ? EMPTY : body;
        this.contentType = contentType == null ? EMPTY : contentType;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public String contentType() {
        return contentType;
    }

    boolean isError() {
        return statusCode >= 400;
    }
}
