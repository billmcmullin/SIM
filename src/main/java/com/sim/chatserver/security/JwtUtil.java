package com.sim.chatserver.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = System.getenv().getOrDefault("CHAT_JWT_SECRET", "change-this-secret-to-a-strong-key");
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXP_MS = 1000L * 60 * 60 * 8; // 8 hours

    public static String generateToken(String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("role", role))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXP_MS))
                .signWith(KEY)
                .compact();
    }

    public static Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
    }
}
