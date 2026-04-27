package com.sim.chatserver.config;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
/**
 * Parasoft Jtest UTA: Test class for Database
 *
 * @see com.sim.chatserver.config.Database
 * @author bmcmullin
 */
public class DatabaseTest
{

    /**
     * Parasoft Jtest UTA: Test for getConnection()
     *
     * @see com.sim.chatserver.config.Database#getConnection()
     * @author bmcmullin
     */
    @Test
    public void testGetConnection() throws Throwable
    {
        // When
        Connection result = Database.getConnection();

    }
}
