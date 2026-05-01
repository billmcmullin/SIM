package com.sim.chatserver.security.review;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.review.ReviewOutputValidator.ValidationResult;
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

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReport(String, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReport(String, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReport4() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReport(report, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReport(String, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReport(String, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReport5() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        int maxChars = 0; // UTA: configured value
        ValidationResult result = underTest.validateFinalReport(report, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportHierarchical(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportHierarchical(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportHierarchical() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportHierarchical(report, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportHierarchical(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportHierarchical(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportHierarchical2() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = ""; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportHierarchical(report, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportHierarchical(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportHierarchical(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportHierarchical3() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportHierarchical(report, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportHierarchical(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportHierarchical(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportHierarchical4() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = "report"; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportHierarchical(report, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportHierarchical(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportHierarchical(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportHierarchical5() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportHierarchical(report, expectedChatIds, maxChars);

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
        String report = ""; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportStrict(report, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportStrict3() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportStrict(report, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportStrict4() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for validateFinalReportStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateFinalReportStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateFinalReportStrict5() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String report = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateFinalReportStrict(report, expectedChatIds, maxChars);

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

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutput(String, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutput(String, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutput4() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutput(output, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutput(String, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutput(String, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutput5() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        int maxChars = 0; // UTA: configured value
        ValidationResult result = underTest.validateMapOutput(output, maxChars);

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
        String output = null; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict3() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict4() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict5() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict6() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict7() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict8() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict9() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = "output"; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for validateMapOutputStrict(String, List, int)
     *
     * @see com.sim.chatserver.security.review.ReviewOutputValidator#validateMapOutputStrict(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testValidateMapOutputStrict10() throws Throwable
    {
        // Given
        ReviewOutputValidator underTest = new ReviewOutputValidator();

        // When
        String output = null; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        int maxChars = 1; // UTA: default value
        ValidationResult result = underTest.validateMapOutputStrict(output, expectedChatIds, maxChars);

    }
}
