# sim-playwright

## Purpose

This module runs UI integration tests with Playwright and publishes both test results and application coverage to Parasoft through CTP, with the Coverage Agent deployed on the Server Under Test.

This module is intentionally different from sim-testng-tests, which demonstrates a local jtest-agent-only workflow.

## Coverage and Result Flow

1. JUnit 5 test methods run through Maven Failsafe.

1. Playwright sends requests to the running SIM application.

1. The Coverage Agent on the Server Under Test reads the Baggage header on incoming requests.

1. CTP correlates test events and coverage data, then publishes to DTP.

## Why Both Parasoft Libraries Are Included

This module uses two Parasoft integration libraries, and both are required for full behavior.

1. coverage-integration-junit5

- Purpose: publishes JUnit 5 test lifecycle events so Parasoft can recognize test results correctly.
- Why needed: without JUnit lifecycle integration, tests may execute but result correlation can be incomplete.
- Related setting: junit.jupiter.extensions.autodetection.enabled=true.

1. coverage-integration-playwright

- Purpose: configures Playwright context options so coverage correlation headers are propagated with browser traffic.
- Why needed: without Playwright header propagation, the Coverage Agent cannot reliably associate app traffic with the running test context.
- Related call in code: PlaywrightCoverageIntegration.updateBrowserContextOptions(...).

## Required Configuration

Configuration file:

- src/test/resources/coverage-integration.properties

Required properties:

- parasoft.coverage.integration.ctp.url
- parasoft.coverage.integration.ctp.envId
- parasoft.coverage.integration.dtp.sessionTag
- parasoft.coverage.integration.ctp.auth.username
- parasoft.coverage.integration.ctp.auth.password

Optional but commonly used:

- parasoft.coverage.integration.parallel.test.enabled
- parasoft.coverage.integration.ctp.userId

## How Test Results Are Recognized

For this project, recognition depends on JUnit 5 plus Failsafe configuration.

1. Test classes must match the include pattern used by Failsafe.

- Class name ends with IT.java.

1. Test methods must use JUnit 5 annotations.

- org.junit.jupiter.api.Test.

1. Tests run in Failsafe integration-test and verify goals.

- This is configured in pom.xml.

1. JUnit extension auto-detection must remain enabled.

- junit.jupiter.extensions.autodetection.enabled=true.

## Baggage Header Configuration

The module supports two ways to set the Baggage header used for coverage correlation.

### Default behavior

- BaseUiIT calls PlaywrightCoverageIntegration.updateBrowserContextOptions(...).
- If no manual override is supplied, Parasoft integration uses its default propagation behavior.

### Manual override behavior

Manual override is useful for troubleshooting or when you need a specific correlation value.

Option A, Maven system property:

- -Dparasoft.coverage.baggageHeader=key=value

Option B, environment variable:

- PARASOFT_COVERAGE_BAGGAGE_HEADER=key=value

Current implementation checks these in order:

1. parasoft.coverage.baggageHeader system property.

1. PARASOFT_COVERAGE_BAGGAGE_HEADER environment variable.

If either is non-blank, that value is applied as the Baggage request header for Playwright traffic.

### Using a value from local browser settings

You can also copy a known-good Baggage value from your local browser context and pass it as an override.

Common approaches:

1. Open browser developer tools on a working session and copy the Baggage header from a request to the SIM app.

1. If your environment stores correlation context in browser-managed data, extract the effective header value used in outgoing requests and reuse it.

Then pass that copied value with one of the override options above.

## Example Commands

Run Playwright integration tests:

- mvn -pl sim-playwright -Dplaywright.skipITs=false failsafe:integration-test failsafe:verify

Run with manual Baggage override:

- mvn -pl sim-playwright -Dplaywright.skipITs=false failsafe:integration-test failsafe:verify -Dparasoft.coverage.baggageHeader=key=value

Run with alternate runtime options:

- mvn -pl sim-playwright -Dplaywright.skipITs=false failsafe:integration-test failsafe:verify -DbaseUrl=<http://localhost:8080/chat-server> -Dheadless=true -DignoreHttpsErrors=true

## Troubleshooting

1. Tests run but no coverage appears.

- Verify Coverage Agent is deployed and active on the Server Under Test.
- Verify CTP URL, environment ID, and credentials in coverage-integration.properties.
- Verify requests from tests actually reach the instrumented application.

1. Coverage appears but tests are not correlated as expected.

- Confirm coverage-integration-junit5 is present.
- Confirm junit.jupiter.extensions.autodetection.enabled=true is still being passed to test runtime.
- Confirm tests are JUnit 5 and included by Failsafe pattern.

1. Header correlation issues.

- Try manual Baggage override with a known-good value captured from browser network traffic.
- Ensure the override string is not blank and not truncated by shell parsing.

## Notes

- This module is the CTP plus deployed Coverage Agent path and should remain that way.
- The sim-testng-tests module demonstrates a separate local workflow without CTP and without a deployed Coverage Agent.
