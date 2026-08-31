package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        String result = invokeAddReportRubricIfMissing(underTest, message, compactRubric);

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
        String result = invokeAddReportRubricIfMissing(underTest, message, compactRubric);

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
        String result = invokeAddReportRubricIfMissing(underTest, message, compactRubric);

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
        String result = invokeAddReportRubricIfMissing(underTest, message, compactRubric);

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
        boolean result = invokeLooksStructuredAlready(underTest, message);

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
        boolean result = invokeLooksStructuredAlready(underTest, message);

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
        String result = invokeWithPromptInjectionGuardrails(underTest, message, enforceMarkdownOnly);

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
        String result = invokeWithPromptInjectionGuardrails(underTest, message, enforceMarkdownOnly);

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
        String result = invokeWithPromptInjectionGuardrails(underTest, message, enforceMarkdownOnly);

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
        String result = invokeWithPromptInjectionGuardrails(underTest, message, enforceMarkdownOnly);

    }


    // Merged from PromptTemplateServiceBranchTest
    @Test
        void addReportRubricIfMissing_returnsInputWhenAlreadyStructured() {
            PromptTemplateService service = new PromptTemplateService();
            String structured = "## Executive Summary\nAlready structured";
    
            String result = service.addReportRubricIfMissing(structured);
    
            assertEquals(structured, result);
        }
    
        @Test
        void addReportRubricIfMissing_overloadReturnsInputWhenAlreadyStructured() throws Throwable {
            PromptTemplateService service = new PromptTemplateService();
            String structured = "## Executive Summary\nAlready structured";
    
            String result = invokeAddReportRubricIfMissing(service, structured, true);
    
            assertEquals(structured, result);
        }
    
        @Test
        void readObject_throwsNotSerializableException() throws Exception {
            PromptTemplateService service = new PromptTemplateService();
            Method readObject = PromptTemplateService.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
            readObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> readObject.invoke(service, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(PromptTemplateService.class.getName(), cause.getMessage());
        }
    
        @Test
        void writeObject_throwsNotSerializableException() throws Exception {
            PromptTemplateService service = new PromptTemplateService();
            Method writeObject = PromptTemplateService.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
            writeObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> writeObject.invoke(service, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(PromptTemplateService.class.getName(), cause.getMessage());
        }
    private static String invokeAddReportRubricIfMissing(PromptTemplateService service, String message, boolean compactRubric) throws Throwable {
        Method m = PromptTemplateService.class.getDeclaredMethod("addReportRubricIfMissing", String.class, boolean.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(service, message, Boolean.valueOf(compactRubric));
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private static boolean invokeLooksStructuredAlready(PromptTemplateService service, String message) throws Throwable {
        Method m = PromptTemplateService.class.getDeclaredMethod("looksStructuredAlready", String.class);
        m.setAccessible(true);
        try {
            return ((Boolean) m.invoke(service, message)).booleanValue();
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private static String invokeWithPromptInjectionGuardrails(PromptTemplateService service, String message, boolean enforceMarkdownOnly) throws Throwable {
        Method m = PromptTemplateService.class.getDeclaredMethod("withPromptInjectionGuardrails", String.class, boolean.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(service, message, Boolean.valueOf(enforceMarkdownOnly));
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}