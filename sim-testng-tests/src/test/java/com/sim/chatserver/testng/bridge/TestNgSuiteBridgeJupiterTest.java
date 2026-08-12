package com.sim.chatserver.testng.bridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import com.sim.chatserver.testng.util.HashUtilNgTest;
import com.sim.chatserver.testng.util.SessionIdFormatterNgTest;
import com.sim.chatserver.testng.util.TextBudgetUtilNgTest;

class TestNgSuiteBridgeJupiterTest {

    @Test
    void testNgSuitePassesThroughJupiterBridge() {
        TestNG testng = new TestNG();
        testng.setUseDefaultListeners(false);
        testng.setTestClasses(new Class<?>[] {
            TextBudgetUtilNgTest.class,
            HashUtilNgTest.class,
            SessionIdFormatterNgTest.class
        });

        TestListenerAdapter results = new TestListenerAdapter();
        // Keep a lightweight bridge: TestNG executes inside JUnit 5, and jtest:agent
        // collects coverage from executed production code without CTP listeners.
        testng.addListener(results);

        testng.run();

        int executed = results.getPassedTests().size()
                + results.getFailedTests().size()
                + results.getSkippedTests().size();

        assertTrue(executed > 0, "Bridge did not execute any TestNG tests");
        assertFalse(results.getFailedTests().size() > 0,
                "TestNG failures: " + summarize(results.getFailedTests()));
    }

    private String summarize(Iterable<ITestResult> failures) {
        String message = "";
        for (ITestResult failure : failures) {
            String item = failure.getTestClass().getName() + "#" + failure.getMethod().getMethodName();
            if (failure.getThrowable() != null) {
                item = item + " -> " + failure.getThrowable().toString();
            }
            message = message.isEmpty() ? item : message + "; " + item;
        }
        return message;
    }
}
