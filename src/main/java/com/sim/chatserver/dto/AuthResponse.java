package com.sim.chatserver.dto;

public class AuthResponse {
    public String token;
    public String username;
    public String role;
    public AuthResponse(String t, String u, String r) { token = t; username = u; role = r; }
}
