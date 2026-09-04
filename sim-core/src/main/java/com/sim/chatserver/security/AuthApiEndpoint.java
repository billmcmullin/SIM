package com.sim.chatserver.security;

import java.util.Map;

import jakarta.ws.rs.core.Response;

public interface AuthApiEndpoint {

    Response loginApi(Map<String, String> payload);
}
