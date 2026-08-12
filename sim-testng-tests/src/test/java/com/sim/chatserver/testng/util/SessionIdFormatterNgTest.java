package com.sim.chatserver.testng.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.sim.chatserver.util.SessionIdFormatter;

public class SessionIdFormatterNgTest {

    @Test
    public void formatForDisplay_ReturnsEmpty_ForNull() {
        assertEquals(SessionIdFormatter.formatForDisplay(null), "");
    }

    @Test
    public void formatForDisplay_ReturnsEmpty_ForBlank() {
        assertEquals(SessionIdFormatter.formatForDisplay("   \t  "), "");
    }

    @Test
    public void formatForDisplay_ReturnsAsIs_WhenLengthAtMost16() {
        assertEquals(SessionIdFormatter.formatForDisplay("1234567890abcdef"), "1234567890abcdef");
        assertEquals(SessionIdFormatter.formatForDisplay("short-id"), "short-id");
    }

    @Test
    public void formatForDisplay_CollapsesDashedSessionId() {
        String sessionId = "12345678-1234-1234-1234-1234567890ab";
        String formatted = SessionIdFormatter.formatForDisplay(sessionId);

        assertCollapsed(formatted, "12345678", "90ab");
    }

    @Test
    public void formatForDisplay_CollapsesVeryLongSessionIdWithoutDashes() {
        String sessionId = "abcdefghijklmnopqrstuvwxyz12345";
        String formatted = SessionIdFormatter.formatForDisplay(sessionId);

        assertCollapsed(formatted, "abcdefgh", "2345");
    }

    @Test
    public void formatForDisplay_UsesMediumLengthStrategy_When17To24AndNoDash() {
        String sessionId = "abcdefghijklmnopq";
        String formatted = SessionIdFormatter.formatForDisplay(sessionId);

        assertCollapsed(formatted, "abcdefghij", "opq");
    }

    @Test
    public void formatForDisplay_TrimsInputBeforeFormatting() {
        String sessionId = "  abcdefghijklmnopq  ";
        String formatted = SessionIdFormatter.formatForDisplay(sessionId);

        assertCollapsed(formatted, "abcdefghij", "opq");
    }

    private void assertCollapsed(String formatted, String prefix, String suffix) {
        assertTrue(formatted.startsWith(prefix), "prefix should be preserved");
        assertTrue(formatted.endsWith(suffix), "suffix should be preserved");
        assertTrue(formatted.length() > prefix.length() + suffix.length(), "collapsed value should include a separator");
    }
}
