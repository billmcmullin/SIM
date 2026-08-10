package com.sim.chatserver.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
 class ServletRequestParamUtilTest {

    @Test
    void firstParamAndFirstParamFromValues_coverInputValidationAndSanitization() {
        assertNull(ServletRequestParamUtil.firstParam(null, "name", 10, false, false));
        assertNull(ServletRequestParamUtil.firstParamFromValues(null, "name", 10, false, false));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues("name")).thenReturn(new String[]{" \u0000abc\r\n "});

        assertEquals("abc", ServletRequestParamUtil.firstParam(request, "name", 10, true, true));
        assertEquals("abc", ServletRequestParamUtil.firstParamFromValues(request, "name", 10, true, true));

        when(request.getParameterValues("fallback")).thenReturn(new String[]{null, "\n next\r "});
        assertEquals("next", ServletRequestParamUtil.firstParamFromValues(request, "fallback", 10, true, true));

        when(request.getParameterValues("trunc")).thenReturn(new String[]{" 1234567890 "});
        assertEquals("1234", ServletRequestParamUtil.firstParam(request, "trunc", 4, false, false));
    }

    @Test
    void normalizeBodyTextAndContentLength_coverNullEmptyAndTruncation() {
        assertNull(ServletRequestParamUtil.normalizeBodyText(null, 20, false));
        assertNull(ServletRequestParamUtil.normalizeBodyText("\r\n", 20, true));
        assertEquals("abc", ServletRequestParamUtil.normalizeBodyText("\u0000abc\r", 20, false));
        assertEquals("abcd", ServletRequestParamUtil.normalizeBodyText("abcdef", 4, false));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentLengthLong()).thenReturn(20L);
        assertTrue(ServletRequestParamUtil.hasValidContentLength(request, 20L));
        assertFalse(ServletRequestParamUtil.hasValidContentLength(request, 19L));
        assertFalse(ServletRequestParamUtil.hasValidContentLength(null, 100L));
    }

    @Test
    void readNormalizedBodyText_coverLimitsAndFallback() throws Exception {
        assertEquals("", ServletRequestParamUtil.readNormalizedBodyText(null, 10));
        assertEquals("", ServletRequestParamUtil.readNormalizedBodyTextOrEmptyOnLimit(null, 10));

        StringReader clean = new StringReader("  line1\r\nline2  ");
        assertEquals("line1\nline2", ServletRequestParamUtil.readNormalizedBodyText(clean, 100));

        StringReader limited = new StringReader("123456789");
        assertThrows(IOException.class, () -> ServletRequestParamUtil.readNormalizedBodyText(limited, 4, 2));

        StringReader emptyOnLimit = new StringReader("123456789");
        assertEquals("", ServletRequestParamUtil.readNormalizedBodyTextOrEmptyOnLimit(emptyOnLimit, 4));
    }

    @Test
    void normalizeValueAndReadParameterValues_coverPrivateSanitizationPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues("raw")).thenReturn(new String[]{"a\u0000b\r\nc", null});

        assertEquals("a b  c", ServletRequestParamUtil.firstParamFromValues(request, "raw", 20, false, false));

        HttpServletRequest emptyRequest = mock(HttpServletRequest.class);
        when(emptyRequest.getParameterValues("raw")).thenReturn(new String[]{"\u0000\r\n"});
        assertNull(ServletRequestParamUtil.firstParamFromValues(emptyRequest, "raw", 20, true, true));

        assertEquals("value", ServletRequestParamUtil.normalizeValue("\r\n value \n", 20, false, true));
        assertNull(ServletRequestParamUtil.normalizeValue("\r\n", 20, false, true));
    }
}

