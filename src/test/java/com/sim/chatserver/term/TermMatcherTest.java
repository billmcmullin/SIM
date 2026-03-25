package com.sim.chatserver.term;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for TermMatcher
 *
 * @see com.sim.chatserver.term.TermMatcher
 * @author bmcmullin
 */
public class TermMatcherTest
{

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = "getNameResult"; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern2() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern3() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = ""; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern4() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = ""; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern5() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = "getMatchPatternResult"; // UTA: default value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern6() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getNameResult = "getNameResult"; // UTA: default value
        when(term.getName()).thenReturn(getNameResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for buildStrictPattern(TermDefinition)
     *
     * @see com.sim.chatserver.term.TermMatcher#buildStrictPattern(TermDefinition)
     * @author bmcmullin
     */
    @Test
    public void testBuildStrictPattern7() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = "getMatchTypeResult"; // UTA: default value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        Pattern result = TermMatcher.buildStrictPattern(term);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String prompt = null; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches2() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String prompt = ""; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches3() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = "getNameResult"; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches4() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches5() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = ""; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = null; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches6() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = ""; // UTA: configured value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches7() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = "getMatchPatternResult"; // UTA: default value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches8() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getNameResult = "getNameResult"; // UTA: default value
        when(term.getName()).thenReturn(getNameResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

    /**
     * Parasoft Jtest UTA: Test for matches(TermDefinition, String)
     *
     * @see com.sim.chatserver.term.TermMatcher#matches(TermDefinition, String)
     * @author bmcmullin
     */
    @Test
    public void testMatches9() throws Throwable
    {
        // When
        TermDefinition term = mock(TermDefinition.class);
        String getMatchPatternResult = null; // UTA: configured value
        when(term.getMatchPattern()).thenReturn(getMatchPatternResult);

        String getMatchTypeResult = "getMatchTypeResult"; // UTA: default value
        when(term.getMatchType()).thenReturn(getMatchTypeResult);

        String getNameResult = null; // UTA: configured value
        when(term.getName()).thenReturn(getNameResult);
        String prompt = "*"; // UTA: configured value
        boolean result = TermMatcher.matches(term, prompt);

    }

}
