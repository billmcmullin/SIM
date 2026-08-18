package com.sim.chatserver.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

class JsonPrimaryTextUtilTest {

    @Test
    void extractPrimaryText_returnsEmptyForNullOrBlank() {
        assertEquals("", JsonPrimaryTextUtil.extractPrimaryText(null, null, null));
        assertEquals("", JsonPrimaryTextUtil.extractPrimaryText("   ", null, null));
    }

    @Test
    void extractPrimaryText_returnsFirstNonBlankPreferredKey() {
        String body = "{\"response\":\"R\",\"textResponse\":\"T\"}";
        assertEquals("T", JsonPrimaryTextUtil.extractPrimaryText(body, null, null));
    }

    @Test
    void extractPrimaryText_returnsBodyWhenNoPreferredKey() {
        String body = "{\"other\":\"value\"}";
        assertEquals(body, JsonPrimaryTextUtil.extractPrimaryText(body, null, null));
    }

    @Test
    void extractPrimaryText_returnsBodyOnInvalidJson() {
        String body = "not-json";
        assertEquals(body, JsonPrimaryTextUtil.extractPrimaryText(body, Logger.getLogger("test"), "parse failed"));

        String arrayBody = "[1,2,3]";
        assertEquals(arrayBody, JsonPrimaryTextUtil.extractPrimaryText(arrayBody, null, null));
    }
}
