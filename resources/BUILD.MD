# BUILD INFORMATION

## Build Information

Requirements: Make sure a PosgreSQL DB is available with the "chat" Table created. Locate the SQL scripts in the  [resources/posgres](resources/posgres) directory to create these if needed. Docker is supported with this WAR file. 

`NOTE: Make sure to have configured for Parasoft Jtest if you are using it. The instructions below assumes you have it configured for a Parasoft License already.`

Current [Jenkin Pipeline flow](resources/SIM_Jenkins_Pipeline.groovy) execution summary:

1. **Checkout** – Pull the `release` branch from GitHub.  
2. **Create Jtest properties** – Generate Parasoft config files for standard analysis and OWASP/A6 publishing.  
3. **Run Maven build/test flow** – Build and run tests for core modules with Parasoft agent/monitor instrumentation.  
4. **Run Parasoft CLI analyses** – Execute Static/Unit analysis and OWASP Top 10 analysis, then publish reports.  
5. **Run Maven OWASP A6 scan** – Execute Dependency-Check using NVD API/cache and publish A6 results to Parasoft.  
6. **Run ESLint** – Lint JavaScript assets and produce JSON findings.  
7. **Prepare + run integration tests** – Prepare shared monitor/agent config, restart Docker services, and run Playwright integration tests.  
8. **Publish consolidated results** – Publish unit/integration test reports, coverage, ESLint issues, dependency-check results, and Parasoft findings.  
9. **Post actions** – Run success/failure notifications based on final build outcome.

## WAR File location
After building the war file, it is located here:
```bash
<PATH TO PROJECT>\SIM_JAVA\sim-app\target\chat-server.war
```

Place in the Standalone directory in the Wildfly server to deploy. 

## Build Commands

Use the below to build the project with or without Parasoft Jtest:

- Package war:

```bash
mvn clean verify -pl sim-core,sim-web,sim-app -Ddependency-check.skip=true -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true
```

- Run Playwright after Docker Container is deployed

```bash
mvn verify -pl sim-playwright -DbaseUrl=<SERVER INFO>/chat-server -Dheadless=true -DignoreHttpsErrors=true -DignoreHttpsErrors=true -DadminUsername=<ADMIN USER> -DadminPassword=<ADMIN PASSWORD> -DuserUsername=<USER> -DuserPassword=<PASSWORD>
```

- Run Jtest Monitor for Application Coverage setup:

```bash
mvn clean test-compile jtest:agent verify jtest:monitor -pl sim-core,sim-web,sim-app -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true -DignoreHttpsErrors=true
```

- Run Jtest Analysis:

```bash
mvn clean test-compile jtest:agent verify jtest:jtest -pl sim-core,sim-web,sim-app -Djtest.config="<TEST CONFIG>" -Ddependency-check.skip=true -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true -DignoreHttpsErrors=true
```

- Run OWASP

```bash
mvn org.owasp:dependency-check-maven:12.2.2:aggregate -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true -DnvdApiKey=<API KEY> -DdataDirectory=<DC_DB_DIRECTORY>
```

- Purge OWASP:

```bash
mvn org.owasp:dependency-check-maven:purge
```

## Check for updated Dependencies

- Quick check (no changes made)

```bash
mvn versions:display-dependency-updates
```

- Also check plugin updates

```bash
mvn versions:display-plugin-updates
```

- Check parent/BOM updates

```bash
mvn versions:display-parent-updates
mvn versions:display-property-updates
```

- For a specific module

```bash
mvn -pl sim-playwright versions:display-dependency-updates
```
