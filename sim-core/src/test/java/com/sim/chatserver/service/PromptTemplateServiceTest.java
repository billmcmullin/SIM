package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;
/**
 * Parasoft Jtest UTA: Test class for PromptTemplateService
 *
 * @see com.sim.chatserver.service.PromptTemplateService
 * @author bmcmullin
 */
public class PromptTemplateServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for addReportRubricIfMissing(String)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#addReportRubricIfMissing(String)
     * @author bmcmullin
     */
    @Test
    public void testAddReportRubricIfMissing() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        String result = underTest.addReportRubricIfMissing(message);

    }

    /**
     * Parasoft Jtest UTA: Test for addReportRubricIfMissing(String)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#addReportRubricIfMissing(String)
     * @author bmcmullin
     */
    @Test
    public void testAddReportRubricIfMissing2() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        String result = underTest.addReportRubricIfMissing(message);

    }

    /**
     * Parasoft Jtest UTA: Test for addReportRubricIfMissing(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#addReportRubricIfMissing(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testAddReportRubricIfMissing3() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        boolean compactRubric = false; // UTA: configured value
        String result = underTest.addReportRubricIfMissing(message, compactRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for addReportRubricIfMissing(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#addReportRubricIfMissing(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testAddReportRubricIfMissing4() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        boolean compactRubric = true; // UTA: configured value
        String result = underTest.addReportRubricIfMissing(message, compactRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for addReportRubricIfMissing(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#addReportRubricIfMissing(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testAddReportRubricIfMissing5() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        boolean compactRubric = false; // UTA: configured value
        String result = underTest.addReportRubricIfMissing(message, compactRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for addReportRubricIfMissing(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#addReportRubricIfMissing(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testAddReportRubricIfMissing6() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        boolean compactRubric = true; // UTA: configured value
        String result = underTest.addReportRubricIfMissing(message, compactRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = false; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt2() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        boolean enforceRubric = false; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt3() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = true; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt4() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = false; // UTA: configured value
        boolean compactRubric = false; // UTA: default value
        boolean enforceMarkdownOnly = true; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt5() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = false; // UTA: configured value
        boolean compactRubric = false; // UTA: default value
        boolean enforceMarkdownOnly = false; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt6() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        boolean enforceRubric = false; // UTA: configured value
        boolean compactRubric = false; // UTA: default value
        boolean enforceMarkdownOnly = true; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt7() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = true; // UTA: configured value
        boolean compactRubric = false; // UTA: configured value
        boolean enforceMarkdownOnly = true; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt8() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = true; // UTA: configured value
        boolean compactRubric = false; // UTA: configured value
        boolean enforceMarkdownOnly = false; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt9() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = true; // UTA: configured value
        boolean compactRubric = true; // UTA: configured value
        boolean enforceMarkdownOnly = true; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt10() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = null; // UTA: configured value
        boolean enforceRubric = true; // UTA: configured value
        boolean compactRubric = true; // UTA: configured value
        boolean enforceMarkdownOnly = false; // UTA: configured value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for buildControlledPrompt(String, boolean, boolean, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#buildControlledPrompt(String, boolean, boolean, boolean)
     * @author bmcmullin
     */
    @Test
    public void testBuildControlledPrompt11() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        boolean enforceRubric = false; // UTA: default value
        boolean compactRubric = false; // UTA: default value
        boolean enforceMarkdownOnly = false; // UTA: default value
        String result = underTest.buildControlledPrompt(userMessage, enforceRubric, compactRubric, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for looksStructuredAlready(String)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#looksStructuredAlready(String)
     * @author bmcmullin
     */
    @Test
    public void testLooksStructuredAlready() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        boolean result = underTest.looksStructuredAlready(message);

    }

    /**
     * Parasoft Jtest UTA: Test for looksStructuredAlready(String)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#looksStructuredAlready(String)
     * @author bmcmullin
     */
    @Test
    public void testLooksStructuredAlready2() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        boolean result = underTest.looksStructuredAlready(message);

    }

    /**
     * Parasoft Jtest UTA: Test for withPromptInjectionGuardrails(String)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#withPromptInjectionGuardrails(String)
     * @author bmcmullin
     */
    @Test
    public void testWithPromptInjectionGuardrails() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        String result = underTest.withPromptInjectionGuardrails(message);

    }

    /**
     * Parasoft Jtest UTA: Test for withPromptInjectionGuardrails(String)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#withPromptInjectionGuardrails(String)
     * @author bmcmullin
     */
    @Test
    public void testWithPromptInjectionGuardrails2() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        String result = underTest.withPromptInjectionGuardrails(message);

    }

    /**
     * Parasoft Jtest UTA: Test for withPromptInjectionGuardrails(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#withPromptInjectionGuardrails(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testWithPromptInjectionGuardrails3() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        boolean enforceMarkdownOnly = true; // UTA: configured value
        String result = underTest.withPromptInjectionGuardrails(message, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for withPromptInjectionGuardrails(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#withPromptInjectionGuardrails(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testWithPromptInjectionGuardrails4() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = null; // UTA: configured value
        boolean enforceMarkdownOnly = false; // UTA: configured value
        String result = underTest.withPromptInjectionGuardrails(message, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for withPromptInjectionGuardrails(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#withPromptInjectionGuardrails(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testWithPromptInjectionGuardrails5() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        boolean enforceMarkdownOnly = true; // UTA: configured value
        String result = underTest.withPromptInjectionGuardrails(message, enforceMarkdownOnly);

    }

    /**
     * Parasoft Jtest UTA: Test for withPromptInjectionGuardrails(String, boolean)
     *
     * @see com.sim.chatserver.service.PromptTemplateService#withPromptInjectionGuardrails(String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testWithPromptInjectionGuardrails6() throws Throwable
    {
        // Given
        PromptTemplateService underTest = new PromptTemplateService();

        // When
        String message = "message"; // UTA: default value
        boolean enforceMarkdownOnly = false; // UTA: configured value
        String result = underTest.withPromptInjectionGuardrails(message, enforceMarkdownOnly);

    }
}
