package com.sim.chatserver.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Simple JWT utility. Uses system property or env var CHAT_JWT_SECRET. For
 * production, provide a secure secret of sufficient length.
 */
public class JwtUtil {

    private static final Logger log = Logger.getLogger(JwtUtil.class.getName());

    private static final String SECRET = System.getProperty("chat.jwt.secret",
            System.getenv().getOrDefault("CHAT_JWT_SECRET", "replace-this-with-a-secure-secret-of-32+chars"));

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_SEC = 7 * 24 * 3600L;

    public static String generateToken(String subject, String role) {
        Instant now = Instant.now();
        Claims claims = Jwts.claims().setSubject(subject).setIssuedAt(Date.from(now));
        if (role != null) {
            claims.put("role", role);
        }
        Date exp = Date.from(now.plusSeconds(EXPIRATION_SEC));
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(exp)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token);
    }
}
