package com.sim.chatserver.security;

import java.lang.reflect.Method;
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
 *
 * This implementation is defensive: it builds tokens with the JwtBuilder API
 * (avoiding assumptions about Jwts.claims() return types) and parses tokens via
 * reflection so it works with multiple JJWT versions (parserBuilder() or older
 * parser()).
 */
public class JwtUtil {

    private static final Logger log = Logger.getLogger(JwtUtil.class.getName());

    private static final String SECRET = System.getProperty("chat.jwt.secret",
            System.getenv().getOrDefault("CHAT_JWT_SECRET", "replace-this-with-a-secure-secret-of-32+chars"));

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_SEC = 7 * 24 * 3600L;

    public static String generateToken(String subject, String role) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date exp = Date.from(now.plusSeconds(EXPIRATION_SEC));

        // Use the builder API directly (avoid Claims builder return-type differences)
        var builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    /**
     * Parse a token into Jws<Claims>. This method attempts to use the newer
     * parserBuilder() API via reflection if present; otherwise it falls back to
     * the older parser() API. Any reflection exceptions are wrapped in a
     * JwtException.
     *
     * @param token JWT compact string
     * @return parsed Jws<Claims>
     * @throws JwtException on parse/validation error
     */
    public static Jws<Claims> parseToken(String token) throws JwtException {
        try {
            // Try newer API: Jwts.parserBuilder().setSigningKey(Key).build().parseClaimsJws(token)
            Method parserBuilderMethod = Jwts.class.getMethod("parserBuilder");
            Object parserBuilder = parserBuilderMethod.invoke(null);

            // setSigningKey(Key)
            Method setSigningKeyMethod = parserBuilder.getClass().getMethod("setSigningKey", Key.class);
            Object pbWithKey = setSigningKeyMethod.invoke(parserBuilder, SIGNING_KEY);

            // build()
            Method buildMethod = pbWithKey.getClass().getMethod("build");
            Object parser = buildMethod.invoke(pbWithKey);

            // parseClaimsJws(String)
            Method parseClaimsJwsMethod = parser.getClass().getMethod("parseClaimsJws", String.class);
            @SuppressWarnings("unchecked")
            Jws<Claims> jws = (Jws<Claims>) parseClaimsJwsMethod.invoke(parser, token);
            return jws;
        } catch (NoSuchMethodException e) {
            // parserBuilder() not present -> try older API via reflection: Jwts.parser().setSigningKey(...).parseClaimsJws(...)
            try {
                Method parserMethod = Jwts.class.getMethod("parser");
                Object parser = parserMethod.invoke(null);

                // Try setSigningKey(Key) first
                try {
                    Method setSigningKeyMethod = parser.getClass().getMethod("setSigningKey", Key.class);
                    setSigningKeyMethod.invoke(parser, SIGNING_KEY);
                } catch (NoSuchMethodException ex) {
                    // Fallback: some older versions expect a byte[] key
                    Method setSigningKeyMethod2 = parser.getClass().getMethod("setSigningKey", byte[].class);
                    setSigningKeyMethod2.invoke(parser, (Object) SECRET.getBytes(StandardCharsets.UTF_8));
                }

                Method parseClaimsJwsMethod = parser.getClass().getMethod("parseClaimsJws", String.class);
                @SuppressWarnings("unchecked")
                Jws<Claims> jws = (Jws<Claims>) parseClaimsJwsMethod.invoke(parser, token);
                return jws;
            } catch (Exception ex) {
                throw new JwtException("Failed to parse JWT using fallback parser()", ex);
            }
        } catch (JwtException je) {
            // If JJWT throws JwtException, rethrow
            throw je;
        } catch (Exception ex) {
            throw new JwtException("Failed to parse JWT", ex);
        }
    }
}
