package com.sim.chatserver.util;

import org.junit.jupiter.api.Test;
/**
 * Parasoft Jtest UTA: Test class for SessionIdFormatter
 *
 * @see com.sim.chatserver.util.SessionIdFormatter
 * @author bmcmullin
 */
public class SessionIdFormatterTest
{

    /**
     * Parasoft Jtest UTA: Test for formatForDisplay(String)
     *
     * @see com.sim.chatserver.util.SessionIdFormatter#formatForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testFormatForDisplay() throws Throwable
    {
        // When
        String sessionId = null; // UTA: configured value
        String result = SessionIdFormatter.formatForDisplay(sessionId);

    }

    /**
     * Parasoft Jtest UTA: Test for formatForDisplay(String)
     *
     * @see com.sim.chatserver.util.SessionIdFormatter#formatForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testFormatForDisplay2() throws Throwable
    {
        // When
        String sessionId = ""; // UTA: configured value
        String result = SessionIdFormatter.formatForDisplay(sessionId);

    }

    /**
     * Parasoft Jtest UTA: Test for formatForDisplay(String)
     *
     * @see com.sim.chatserver.util.SessionIdFormatter#formatForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testFormatForDisplay3() throws Throwable
    {
        // When
        String sessionId = "****************"; // UTA: configured value
        String result = SessionIdFormatter.formatForDisplay(sessionId);

    }

    /**
     * Parasoft Jtest UTA: Test for formatForDisplay(String)
     *
     * @see com.sim.chatserver.util.SessionIdFormatter#formatForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testFormatForDisplay4() throws Throwable
    {
        // When
        String sessionId = "-****************"; // UTA: configured value
        String result = SessionIdFormatter.formatForDisplay(sessionId);

    }

    /**
     * Parasoft Jtest UTA: Test for formatForDisplay(String)
     *
     * @see com.sim.chatserver.util.SessionIdFormatter#formatForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testFormatForDisplay5() throws Throwable
    {
        // When
        String sessionId = "*************************"; // UTA: configured value
        String result = SessionIdFormatter.formatForDisplay(sessionId);

    }

    /**
     * Parasoft Jtest UTA: Test for formatForDisplay(String)
     *
     * @see com.sim.chatserver.util.SessionIdFormatter#formatForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testFormatForDisplay6() throws Throwable
    {
        // When
        String sessionId = "*****************"; // UTA: configured value
        String result = SessionIdFormatter.formatForDisplay(sessionId);

    }
}
