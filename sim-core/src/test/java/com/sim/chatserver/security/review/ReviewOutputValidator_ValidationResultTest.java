package com.sim.chatserver.security.review;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult;
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

    }
}
