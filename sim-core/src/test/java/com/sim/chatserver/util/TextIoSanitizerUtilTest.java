package com.sim.chatserver.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

class TextIoSanitizerUtilTest {

    @Test
    void canonicalize_handlesNullAndNfkcNormalization() {
        assertEquals("", TextIoSanitizerUtil.canonicalize(null));
        assertEquals("", TextIoSanitizerUtil.canonicalize(""));
        assertEquals("ABC123", TextIoSanitizerUtil.canonicalize("ＡＢＣ１２３"));
    }

    @Test
    void validateCanonicalized_removesUnsafeControlCharsAndTruncates() {
        String input = " \u0000A\rB\u0007\n\t C ";
        String sanitized = TextIoSanitizerUtil.validateCanonicalized(input, 0);
        assertEquals("AB\n\t C", sanitized);

        assertEquals("AB", TextIoSanitizerUtil.validateCanonicalized(input, 2));
        assertEquals("", TextIoSanitizerUtil.validateCanonicalized(null, 10));
    }

    @Test
    void readAtMostChars_respectsLimitAndNullGuards() throws IOException {
        assertEquals("", TextIoSanitizerUtil.readAtMostChars(null, 5));
        assertEquals("", TextIoSanitizerUtil.readAtMostChars(new StringReader("abc"), 0));

        StringReader reader = new StringReader("abcdefghij");
        assertEquals("abcde", TextIoSanitizerUtil.readAtMostChars(reader, 5));
    }

    @Test
    void stripControlCharacters_preservesAllowedWhitespaceControls() {
        String value = "A\u0000B\nC\rD\tE\u0007";
        assertEquals("AB\nC\rD\tE", TextIoSanitizerUtil.stripControlCharacters(value));
        assertEquals("", TextIoSanitizerUtil.stripControlCharacters(null));
    }

    @Test
    void safeFileToken_appliesFallbackAndSanitization() {
        assertEquals("server", TextIoSanitizerUtil.safeFileToken(null, null));
        assertEquals("fallback", TextIoSanitizerUtil.safeFileToken("", "fallback"));
        assertEquals("abc_123", TextIoSanitizerUtil.safeFileToken("abc 123", "x"));
        assertEquals("___", TextIoSanitizerUtil.safeFileToken("***", "fallback"));
    }
}
