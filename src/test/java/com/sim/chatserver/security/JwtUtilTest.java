package com.sim.chatserver.security;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
/**
 * Parasoft Jtest UTA: Test class for JwtUtil
 *
 * @see com.sim.chatserver.security.JwtUtil
 * @author bmcmullin
 */
public class JwtUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for generateToken(String, String)
     *
     * @see com.sim.chatserver.security.JwtUtil#generateToken(String, String)
     * @author bmcmullin
     */
    @Test
    public void testGenerateToken() throws Throwable
    {
        // When
        String subject = "subject"; // UTA: default value
        String role = null; // UTA: configured value
        String result = JwtUtil.generateToken(subject, role);

    }

    /**
     * Parasoft Jtest UTA: Test for generateToken(String, String)
     *
     * @see com.sim.chatserver.security.JwtUtil#generateToken(String, String)
     * @author bmcmullin
     */
    @Test
    public void testGenerateToken2() throws Throwable
    {
        // When
        String subject = "subject"; // UTA: default value
        String role = "role"; // UTA: default value
        String result = JwtUtil.generateToken(subject, role);

    }

}
