package com.sim.chatserver.dto;

/**
 * Payload for creating a new user via admin API.
 */
public class CreateUserRequest {
    public String username;
    public String password;
    public String role;

    public CreateUserRequest() {}
}
