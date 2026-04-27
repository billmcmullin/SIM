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
 * Parasoft Jtest UTA: Test class for ReviewOutputValidator
 *
 * @see com.sim.chatserver.security.review.ReviewOutputValidator
 * @author bmcmullin
 */
public class ReviewOutputValidatorTest
{

    /**
     * Parasoft Jtest UTA: Test for extractChatIds(String)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#extractChatIds(String)
     * @author bmcmullin
     */
    @Test
    public void testExtractChatIds() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: configured value
        List<String> result = underTest.extractChatIds(output);

        // Then - assertions for result of method extractChatIds(String)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for extractChatIds(String)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#extractChatIds(String)
     * @author bmcmullin
     */
    @Test
    public void testExtractChatIds2() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        List<String> result = underTest.extractChatIds(output);

        // Then - assertions for result of method extractChatIds(String)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReport(String)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReport(String)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReport() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = "report"; // UTA: configured value
        ValidationResult result = underTest.validateFinalReport(report);

        // Then - assertions for result of method validateFinalReport(String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(8, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(0, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReport(String)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReport(String)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReport2() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        ValidationResult result = underTest.validateFinalReport(report);

        // Then - assertions for result of method validateFinalReport(String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(1, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(0, result.getWarnings().size());
        }, () -> {
            assertEquals(0, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReport(String, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReport(String, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReport3() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = "report"; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReport(report, maxChars);

        // Then - assertions for result of method validateFinalReport(String, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(9, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(1, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportStrict() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportStrict(report, expectedChatIds, maxChars);

        // Then - assertions for result of method validateFinalReportStrict(String, List, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(1, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(1, result.getWarnings().size());
        }, () -> {
            assertEquals(0, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportStrict2() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = "report"; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportStrict(report, expectedChatIds, maxChars);

        // Then - assertions for result of method validateFinalReportStrict(String, List, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(10, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(2, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(1, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(1, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutput(String)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutput(String)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutput() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: configured value
        ValidationResult result = underTest.validateMapOutput(output);

        // Then - assertions for result of method validateMapOutput(String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(5, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(1, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutput(String)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutput(String)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutput2() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        ValidationResult result = underTest.validateMapOutput(output);

        // Then - assertions for result of method validateMapOutput(String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(1, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(0, result.getWarnings().size());
        }, () -> {
            assertEquals(0, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutput(String, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutput(String, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutput3() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutput(output, maxChars);

        // Then - assertions for result of method validateMapOutput(String, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(6, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(2, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds);

        // Then - assertions for result of method validateMapOutputStrict(String, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(5, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(2, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict2() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds);

        // Then - assertions for result of method validateMapOutputStrict(String, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isValid());
        }, () -> {
            assertNotNull(result.getErrors());
            assertEquals(7, result.getErrors().size());
        }, () -> {
            assertNotNull(result.getWarnings());
            assertEquals(1, result.getWarnings().size());
        }, () -> {
            assertEquals(6, result.getLength());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(1, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(1, result.getMissingChatIds().size());
        }, () -> {
            assertNotNull(result.getUnexpectedChatIds());
            assertEquals(0, result.getUnexpectedChatIds().size());
        });

    }

}
