package com.sim.chatserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
 class ApiAuthResolverTest {

    @Test
    void normalizeApiKeyToken_handlesAuthorizationAndBearerPrefixes() {
        assertNull(ApiAuthResolver.normalizeApiKeyToken(null));
        assertNull(ApiAuthResolver.normalizeApiKeyToken("   "));
        assertEquals("token-1", ApiAuthResolver.normalizeApiKeyToken("Authorization: Bearer token-1"));
        assertEquals("token-2", ApiAuthResolver.normalizeApiKeyToken("Bearer token-2"));
        assertEquals("token-3", ApiAuthResolver.normalizeApiKeyToken(" token-3 "));
    }

    @Test
    void stripAuthorizationPrefix_handlesPrefixAndBlank() {
        assertNull(ApiAuthResolver.stripAuthorizationPrefix(null));
        assertNull(ApiAuthResolver.stripAuthorizationPrefix("   "));
        assertEquals("Bearer abc", ApiAuthResolver.stripAuthorizationPrefix("Authorization: Bearer abc"));
        assertEquals("plain", ApiAuthResolver.stripAuthorizationPrefix("plain"));
    }

    @Test
    void resolveForServerConfigOutbound_usesExplicitRequestToken() {
        ApiAuthResolver.ResolvedApiAuth auth =
                ApiAuthResolver.resolveForServerConfigOutbound("Authorization: Bearer req-token");

        assertEquals("req-token", auth.token());
        assertEquals("Authorization", auth.preferredHeaderName());
        assertEquals("REQUEST", auth.source());
        assertTrue(auth.hasToken());
    }

    @Test
    void resolveForServerConfigOutbound_returnsEmptyWithoutExplicitToken() {
        ApiAuthResolver.ResolvedApiAuth auth = ApiAuthResolver.resolveForServerConfigOutbound("   ");

        assertEquals("NONE", auth.source());
        assertEquals("Authorization", auth.preferredHeaderName());
        assertFalse(auth.hasToken());
    }

    @Test
    void resolveForOutbound_prefersExplicitRequestToken() {
        ApiAuthResolver.ResolvedApiAuth auth =
                ApiAuthResolver.resolveForOutbound("Authorization: Bearer req-token");

        assertEquals("req-token", auth.token());
        assertEquals("REQUEST", auth.source());
        assertEquals("Authorization", auth.preferredHeaderName());
        assertTrue(auth.hasToken());
    }

    @Test
    void resolveForOutbound_returnsEmptyWhenNoSourcesAvailable() {
        ApiAuthResolver.ResolvedApiAuth auth = ApiAuthResolver.resolveForOutbound(null);

        assertEquals("NONE", auth.source());
        assertEquals("Authorization", auth.preferredHeaderName());
        assertFalse(auth.hasToken());
    }
}

