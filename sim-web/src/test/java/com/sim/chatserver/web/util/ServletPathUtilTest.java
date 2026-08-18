package com.sim.chatserver.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ServletPathUtilTest {

    @Test
    void safeContextPathStrict_validatesExpectedPatterns() {
        assertEquals("", ServletPathUtil.safeContextPathStrict(null));
        assertEquals("", ServletPathUtil.safeContextPathStrict("app"));
        assertEquals("", ServletPathUtil.safeContextPathStrict("http://example"));
        assertEquals("", ServletPathUtil.safeContextPathStrict("/bad\npath"));
        assertEquals("/chat-server", ServletPathUtil.safeContextPathStrict(" /chat-server "));
    }

    @Test
    void safeContextPathNoEmptyGuard_allowsStandardRelativeContextPathOnly() {
        assertEquals("", ServletPathUtil.safeContextPathNoEmptyGuard(""));
        assertEquals("", ServletPathUtil.safeContextPathNoEmptyGuard("example"));
        assertEquals("", ServletPathUtil.safeContextPathNoEmptyGuard("https://example"));
        assertEquals("/ctx", ServletPathUtil.safeContextPathNoEmptyGuard("/ctx"));
    }

    @Test
    void safeContextPathEnsureLeadingSlash_normalizesInput() {
        assertEquals("", ServletPathUtil.safeContextPathEnsureLeadingSlash(null));
        assertEquals("", ServletPathUtil.safeContextPathEnsureLeadingSlash("   \n\r   "));
        assertEquals("/ctx", ServletPathUtil.safeContextPathEnsureLeadingSlash("ctx"));
        assertEquals("/ctx", ServletPathUtil.safeContextPathEnsureLeadingSlash("/ctx"));
    }

    @Test
    void safeContextPathNoTrailingSlash_removesSingleTrailingSlash() {
        assertEquals("", ServletPathUtil.safeContextPathNoTrailingSlash(null));
        assertEquals("", ServletPathUtil.safeContextPathNoTrailingSlash("/"));
        assertEquals("", ServletPathUtil.safeContextPathNoTrailingSlash("   "));
        assertEquals("/ctx", ServletPathUtil.safeContextPathNoTrailingSlash("/ctx/"));
        assertEquals("/ctx", ServletPathUtil.safeContextPathNoTrailingSlash("/ctx"));
    }
}
