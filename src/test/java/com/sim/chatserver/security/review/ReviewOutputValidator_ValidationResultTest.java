package com.sim.chatserver.security.review;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for ValidationResult
 *
 * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult
 * @author bmcmullin
 */
public class ReviewOutputValidator_ValidationResultTest
{

    /**
     * Parasoft Jtest UTA: Test for getErrors()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getErrors()
     * @author bmcmullin
     */
    @Test
    public void testGetErrors() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        List<String> result = underTest.getErrors();

        // Then - assertions for result of method getErrors()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getExpectedChatIds()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getExpectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetExpectedChatIds() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        List<String> result = underTest.getExpectedChatIds();

        // Then - assertions for result of method getExpectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFoundChatIds()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getFoundChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetFoundChatIds() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        List<String> result = underTest.getFoundChatIds();

        // Then - assertions for result of method getFoundChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLength()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getLength()
     * @author bmcmullin
     */
    @Test
    public void testGetLength() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        int result = underTest.getLength();

        // Then - assertions for result of method getLength()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMissingChatIds()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getMissingChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetMissingChatIds() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        List<String> result = underTest.getMissingChatIds();

        // Then - assertions for result of method getMissingChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUnexpectedChatIds()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getUnexpectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetUnexpectedChatIds() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        List<String> result = underTest.getUnexpectedChatIds();

        // Then - assertions for result of method getUnexpectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWarnings()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#getWarnings()
     * @author bmcmullin
     */
    @Test
    public void testGetWarnings() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        List<String> result = underTest.getWarnings();

        // Then - assertions for result of method getWarnings()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isValid()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#isValid()
     * @author bmcmullin
     */
    @Test
    public void testIsValid() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        boolean result = underTest.isValid();

        // Then - assertions for result of method isValid()
        assertFalse(result);

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        boolean valid = false; // UTA: default value
        List<String> errors = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        errors.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        int length = 1; // UTA: default value
        List<String> expectedChatIds = null; // UTA: configured value
        List<String> foundChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<String> unexpectedChatIds = null; // UTA: configured value
        ValidationResult underTest = new ValidationResult(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, unexpectedChatIds);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("ValidationResult{valid=false, errors=[item], warnings=[], length=1, expectedChatIds=[], foundChatIds=[], missingChatIds=[], unexpectedChatIds=[]}", result);

        // Then - assertions for this instance of ReviewOutputValidator.ValidationResult
        assertAll(() -> {
            assertFalse(underTest.isValid());
        }, () -> {
            assertNotNull(underTest.getErrors());
            assertEquals(1, underTest.getErrors().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        }, () -> {
            assertEquals(1, underTest.getLength());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUnexpectedChatIds());
            assertEquals(0, underTest.getUnexpectedChatIds().size());
        });

    }
}
