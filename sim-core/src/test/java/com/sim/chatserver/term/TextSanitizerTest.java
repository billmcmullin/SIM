package com.sim.chatserver.term;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for TextSanitizer
 *
 * @see com.sim.chatserver.term.TextSanitizer
 * @author bmcmullin
 */
public class TextSanitizerTest
{

    /**
     * Parasoft Jtest UTA: Test for sanitizeForMatching(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForMatching(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForMatching() throws Throwable
    {
        // When
        String input = null; // UTA: configured value
        String result = TextSanitizer.sanitizeForMatching(input);

        // Then - assertions for result of method sanitizeForMatching(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForMatching(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForMatching(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForMatching2() throws Throwable
    {
        // When
        String input = ""; // UTA: configured value
        String result = TextSanitizer.sanitizeForMatching(input);

        // Then - assertions for result of method sanitizeForMatching(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForMatching(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForMatching(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForMatching3() throws Throwable
    {
        // When
        String input = "input"; // UTA: configured value
        String result = TextSanitizer.sanitizeForMatching(input);

        // Then - assertions for result of method sanitizeForMatching(String)
        assertEquals("input", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForMatching(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForMatching(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForMatching4() throws Throwable
    {
        // When
        String input = "*"; // UTA: configured value
        String result = TextSanitizer.sanitizeForMatching(input);

        // Then - assertions for result of method sanitizeForMatching(String)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForStorage(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForStorage(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForStorage() throws Throwable
    {
        // When
        String input = null; // UTA: configured value
        String result = TextSanitizer.sanitizeForStorage(input);

        // Then - assertions for result of method sanitizeForStorage(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForStorage(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForStorage(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForStorage2() throws Throwable
    {
        // When
        String input = ""; // UTA: configured value
        String result = TextSanitizer.sanitizeForStorage(input);

        // Then - assertions for result of method sanitizeForStorage(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForStorage(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForStorage(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForStorage3() throws Throwable
    {
        // When
        String input = "input"; // UTA: configured value
        String result = TextSanitizer.sanitizeForStorage(input);

        // Then - assertions for result of method sanitizeForStorage(String)
        assertEquals("input", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeForStorage(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeForStorage(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeForStorage4() throws Throwable
    {
        // When
        String input = "*"; // UTA: configured value
        String result = TextSanitizer.sanitizeForStorage(input);

        // Then - assertions for result of method sanitizeForStorage(String)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeMarkdownForDisplay(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeMarkdownForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeMarkdownForDisplay() throws Throwable
    {
        // When
        String markdown = null; // UTA: configured value
        String result = TextSanitizer.sanitizeMarkdownForDisplay(markdown);

        // Then - assertions for result of method sanitizeMarkdownForDisplay(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeMarkdownForDisplay(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeMarkdownForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeMarkdownForDisplay2() throws Throwable
    {
        // When
        String markdown = ""; // UTA: configured value
        String result = TextSanitizer.sanitizeMarkdownForDisplay(markdown);

        // Then - assertions for result of method sanitizeMarkdownForDisplay(String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeMarkdownForDisplay(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeMarkdownForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeMarkdownForDisplay3() throws Throwable
    {
        // When
        String markdown = "markdown"; // UTA: configured value
        String result = TextSanitizer.sanitizeMarkdownForDisplay(markdown);

        // Then - assertions for result of method sanitizeMarkdownForDisplay(String)
        assertEquals("markdown", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeMarkdownForDisplay(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeMarkdownForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeMarkdownForDisplay4() throws Throwable
    {
        // When
        String markdown = "*"; // UTA: configured value
        String result = TextSanitizer.sanitizeMarkdownForDisplay(markdown);

        // Then - assertions for result of method sanitizeMarkdownForDisplay(String)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeMarkdownForDisplay(String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizeMarkdownForDisplay(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeMarkdownForDisplay5() throws Throwable
    {
        // When
        String markdown = "&"; // UTA: configured value
        String result = TextSanitizer.sanitizeMarkdownForDisplay(markdown);

        // Then - assertions for result of method sanitizeMarkdownForDisplay(String)
        assertEquals("&amp;", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage() throws Throwable
    {
        // When
        String pattern = null; // UTA: configured value
        String matchType = "matchType"; // UTA: default value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage2() throws Throwable
    {
        // When
        String pattern = ""; // UTA: configured value
        String matchType = "matchType"; // UTA: default value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage3() throws Throwable
    {
        // When
        String pattern = "pattern"; // UTA: configured value
        String matchType = "regex"; // UTA: configured value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("pattern", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage4() throws Throwable
    {
        // When
        String pattern = "pattern"; // UTA: configured value
        String matchType = ""; // UTA: configured value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("pattern", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage5() throws Throwable
    {
        // When
        String pattern = "*"; // UTA: configured value
        String matchType = "regex"; // UTA: configured value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage6() throws Throwable
    {
        // When
        String pattern = "*"; // UTA: configured value
        String matchType = "matchType"; // UTA: default value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizePatternForStorage(String, String)
     *
     * @see com.sim.chatserver.term.TextSanitizer#sanitizePatternForStorage(String, String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizePatternForStorage7() throws Throwable
    {
        // When
        String pattern = "pattern"; // UTA: configured value
        String matchType = "matchType"; // UTA: default value
        String result = TextSanitizer.sanitizePatternForStorage(pattern, matchType);

        // Then - assertions for result of method sanitizePatternForStorage(String, String)
        assertEquals("pattern", result);

    }
}
