package com.sim.chatserver.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * Parasoft Jtest UTA: Test class for TextBudgetUtil
 *
 * @see com.sim.chatserver.util.TextBudgetUtil
 * @author bmcmullin
 */
public class TextBudgetUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for appendWithinLimit(StringBuilder, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#appendWithinLimit(StringBuilder, String, int)
     * @author bmcmullin
     */
    @Test
    public void testAppendWithinLimit() throws Throwable
    {
        // When
        StringBuilder sb = null; // UTA: configured value
        String text = "text"; // UTA: default value
        int maxChars = 1; // UTA: default value
        boolean result = TextBudgetUtil.appendWithinLimit(sb, text, maxChars);

        // Then - assertions for result of method appendWithinLimit(StringBuilder, String, int)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for appendWithinLimit(StringBuilder, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#appendWithinLimit(StringBuilder, String, int)
     * @author bmcmullin
     */
    @Test
    public void testAppendWithinLimit2() throws Throwable
    {
        // When
        StringBuilder sb = new StringBuilder("*"); // UTA: configured value
        String text = "*"; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        boolean result = TextBudgetUtil.appendWithinLimit(sb, text, maxChars);

        // Then - assertions for result of method appendWithinLimit(StringBuilder, String, int)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for appendWithinLimit(StringBuilder, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#appendWithinLimit(StringBuilder, String, int)
     * @author bmcmullin
     */
    @Test
    public void testAppendWithinLimit3() throws Throwable
    {
        // When
        StringBuilder sb = new StringBuilder(""); // UTA: configured value
        String text = "text"; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        boolean result = TextBudgetUtil.appendWithinLimit(sb, text, maxChars);

        // Then - assertions for result of method appendWithinLimit(StringBuilder, String, int)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for approxCharsForTokens(int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#approxCharsForTokens(int)
     * @author bmcmullin
     */
    @Test
    public void testApproxCharsForTokens() throws Throwable
    {
        // When
        int tokens = 0; // UTA: configured value
        int result = TextBudgetUtil.approxCharsForTokens(tokens);

        // Then - assertions for result of method approxCharsForTokens(int)
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for approxCharsForTokens(int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#approxCharsForTokens(int)
     * @author bmcmullin
     */
    @Test
    public void testApproxCharsForTokens2() throws Throwable
    {
        // When
        int tokens = 1; // UTA: configured value
        int result = TextBudgetUtil.approxCharsForTokens(tokens);

        // Then - assertions for result of method approxCharsForTokens(int)
        assertEquals(4, result);

    }

    /**
     * Parasoft Jtest UTA: Test for compressText(String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#compressText(String, int)
     * @author bmcmullin
     */
    @Test
    public void testCompressText() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        String result = TextBudgetUtil.compressText(value, maxChars);

        // Then - assertions for result of method compressText(String, int)
        assertEquals("(empty)", result);

    }

    /**
     * Parasoft Jtest UTA: Test for compressText(String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#compressText(String, int)
     * @author bmcmullin
     */
    @Test
    public void testCompressText2() throws Throwable
    {
        // When
        String value = "value"; // UTA: configured value
        int maxChars = 1; // UTA: default value
        String result = TextBudgetUtil.compressText(value, maxChars);

        // Then - assertions for result of method compressText(String, int)
        assertEquals("…", result);

    }

    /**
     * Parasoft Jtest UTA: Test for compressText(String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#compressText(String, int)
     * @author bmcmullin
     */
    @Test
    public void testCompressText3() throws Throwable
    {
        // When
        String value = "value"; // UTA: configured value
        int maxChars = 0; // UTA: configured value
        String result = TextBudgetUtil.compressText(value, maxChars);

        // Then - assertions for result of method compressText(String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for compressText(String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#compressText(String, int)
     * @author bmcmullin
     */
    @Test
    public void testCompressText4() throws Throwable
    {
        // When
        String value = "*"; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = TextBudgetUtil.compressText(value, maxChars);

        // Then - assertions for result of method compressText(String, int)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for compressText(String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#compressText(String, int)
     * @author bmcmullin
     */
    @Test
    public void testCompressText5() throws Throwable
    {
        // When
        String value = "***"; // UTA: configured value
        int maxChars = 2; // UTA: configured value
        String result = TextBudgetUtil.compressText(value, maxChars);

        // Then - assertions for result of method compressText(String, int)
        assertEquals("*…", result);

    }

    /**
     * Parasoft Jtest UTA: Test for concatWithBudget(String, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#concatWithBudget(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testConcatWithBudget() throws Throwable
    {
        // When
        String base = "base"; // UTA: default value
        String suffix = "suffix"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = TextBudgetUtil.concatWithBudget(base, suffix, maxChars);

        // Then - assertions for result of method concatWithBudget(String, String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for concatWithBudget(String, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#concatWithBudget(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testConcatWithBudget2() throws Throwable
    {
        // When
        String base = ""; // UTA: configured value
        String suffix = ""; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = TextBudgetUtil.concatWithBudget(base, suffix, maxChars);

        // Then - assertions for result of method concatWithBudget(String, String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for concatWithBudget(String, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#concatWithBudget(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testConcatWithBudget3() throws Throwable
    {
        // When
        String base = "*"; // UTA: configured value
        String suffix = "suffix"; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = TextBudgetUtil.concatWithBudget(base, suffix, maxChars);

        // Then - assertions for result of method concatWithBudget(String, String, int)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for concatWithBudget(String, String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#concatWithBudget(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testConcatWithBudget4() throws Throwable
    {
        // When
        String base = ""; // UTA: configured value
        String suffix = "*"; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = TextBudgetUtil.concatWithBudget(base, suffix, maxChars);

        // Then - assertions for result of method concatWithBudget(String, String, int)
        assertEquals("*", result);

    }

    /**
     * Parasoft Jtest UTA: Test for estimateTokens(String)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#estimateTokens(String)
     * @author bmcmullin
     */
    @Test
    public void testEstimateTokens() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        int result = TextBudgetUtil.estimateTokens(value);

        // Then - assertions for result of method estimateTokens(String)
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for estimateTokens(String)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#estimateTokens(String)
     * @author bmcmullin
     */
    @Test
    public void testEstimateTokens2() throws Throwable
    {
        // When
        String value = "value"; // UTA: configured value
        int result = TextBudgetUtil.estimateTokens(value);

        // Then - assertions for result of method estimateTokens(String)
        assertEquals(2, result);

    }

    /**
     * Parasoft Jtest UTA: Test for trimTo(String, int)
     *
     * @see com.sim.chatserver.util.TextBudgetUtil#trimTo(String, int)
     * @author bmcmullin
     */
    @Test
    public void testTrimTo() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        String result = TextBudgetUtil.trimTo(value, maxChars);

        // Then - assertions for result of method trimTo(String, int)
        assertEquals("", result);

    }

}
