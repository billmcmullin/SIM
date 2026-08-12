# sim-testng-tests

## Purpose

This module is a demonstration setup that shows how to run TestNG tests and still have Parasoft Jtest Unit Tests recognize pass/fail results and collect coverage, without:

- deploying the Parasoft Coverage Agent on a server
- integrating through CTP

This is intentionally different from the Playwright test modules.

## Why This Module Uses a JUnit 5 Bridge

Parasoft Jtest Unit Tests most reliably recognizes unit test outcomes from JUnit-based execution in this local Maven flow.

To keep TestNG test code while still providing JUnit-recognizable results, this module uses a bridge class:

- JUnit 5 discovers and runs one bridge test class.
- The bridge starts TestNG programmatically.
- TestNG executes all TestNG test classes.
- jtest:agent collects runtime coverage from executed production code.

Because of this architecture, Maven/JUnit reports one JUnit test case (the bridge), while TestNG internally runs many test methods.

## Scope and Constraints

This module is for demonstration and local validation of:

- TestNG execution through a JUnit 5 bridge
- Jtest Unit Tests pass/fail recognition
- coverage capture via jtest:agent instrumentation

This module does not use:

- CTP listener configuration
- coverage-integration.properties
- deployed server-side coverage agent

## Important Difference From Playwright

Playwright tests in this repository are configured to use CTP and a deployed Coverage Agent, and that setup is valid for that test type.

Do not copy this module's strategy into Playwright modules unless you explicitly want a local, agent-only unit-test-style workflow.

## Current Test Discovery Behavior

In pom.xml, Surefire includes only bridge test classes matching *JupiterTest.java.

Result:

- JUnit/Jtest view: one executed test case (the bridge test method)
- TestNG view: all TestNG methods executed by the bridge

This is expected and is not a test loss.

## Typical Commands

Run this module tests:

mvn -pl sim-testng-tests test

Run Jtest Unit Tests with runtime coverage instrumentation:

mvn clean test-compile jtest:agent test jtest:jtest -pl sim-core,sim-testng-tests -am "-Djtest.home={JTEST_HOME}}" "-Djtest.config=builtin://Unit Tests" "-Dtest=TestNgSuiteBridgeJupiterTest" "-Dsurefire.failIfNoSpecifiedTests=false"

## Troubleshooting Notes

If you see only one executed JUnit test case, this is expected for the bridge architecture.

If TestNG method counts are needed, check the TestNG suite summary in the test output.

If you remove the bridge include pattern or migrate tests to native JUnit 5, behavior in Jtest Unit Tests reporting will change.

## Maintenance Guidance

Keep this module aligned to its stated goal: demonstrate TestNG plus Jtest Unit Tests compatibility without CTP/deployed coverage agent dependencies.

If future requirements change (for example, requiring per-test-case accounting directly in JUnit reports), consider converting the tests to native JUnit 5.
