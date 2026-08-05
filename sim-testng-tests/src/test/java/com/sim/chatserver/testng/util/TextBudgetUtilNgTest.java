package com.sim.chatserver.testng.util;

import com.sim.chatserver.util.TextBudgetUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TextBudgetUtilNgTest {

    @Test
    public void appendWithinLimit_AppendsWholeText_WhenEnoughRoom() {
        StringBuilder sb = new StringBuilder("ab");

        boolean appendedFullText = TextBudgetUtil.appendWithinLimit(sb, "cd", 4);

        assertTrue(appendedFullText);
        assertEquals(sb.toString(), "abcd");
    }

    @Test
    public void appendWithinLimit_Truncates_WhenInputExceedsBudget() {
        StringBuilder sb = new StringBuilder("ab");

        boolean appendedFullText = TextBudgetUtil.appendWithinLimit(sb, "cdef", 4);

        assertFalse(appendedFullText);
        assertEquals(sb.toString(), "abcd");
    }

    @DataProvider(name = "trimCases")
    public Object[][] trimCases() {
        return new Object[][] {
            {null, 5, ""},
            {"", 3, ""},
            {"abc", 0, ""},
            {"abc", 5, "abc"},
            {"abcdef", 3, "abc"}
        };
    }

    @Test(dataProvider = "trimCases")
    public void trimTo_ReturnsExpectedValues(String value, int maxChars, String expected) {
        assertEquals(TextBudgetUtil.trimTo(value, maxChars), expected);
    }

    @DataProvider(name = "compressCases")
    public Object[][] compressCases() {
        return new Object[][] {
            {null, 10, "(empty)"},
            {"   ", 10, "(empty)"},
            {"alpha   beta", 20, "alpha beta"},
            {"alpha beta gamma", 1, "\u2026"},
            {"alpha beta gamma", 7, "alpha \u2026"}
        };
    }

    @Test(dataProvider = "compressCases")
    public void compressText_ReturnsExpectedValues(String value, int maxChars, String expected) {
        assertEquals(TextBudgetUtil.compressText(value, maxChars), expected);
    }

    @DataProvider(name = "tokenCases")
    public Object[][] tokenCases() {
        return new Object[][] {
            {null, 0},
            {"", 0},
            {"abcd", 1},
            {"abcde", 2}
        };
    }

    @Test(dataProvider = "tokenCases")
    public void estimateTokens_ReturnsExpectedValues(String value, int expected) {
        assertEquals(TextBudgetUtil.estimateTokens(value), expected);
    }
}
