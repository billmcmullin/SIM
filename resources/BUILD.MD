# BUILD INFORMATION

## Build Worflow

Requirements: Make sure a PosgreSQL DB is available with the "chat" Table created. Locate the SQL scripts in the 'resources' directory to create these if needed. 

1. Checkout project from GitHub
2. Create Properties and perform Setup
3. Run Maven to create War File and run Jtest Monitor Goal
4. Run OWSASP Dependency Check and publish Results
5. Run jtestcli to publish Static Analysis and Unit Tests results
6. Run ESLint and the Parasoft MLP to publish results
7. Gather WAR file and prepare Jtest Coverage Agent
8. Run Wildfly Docker Container and deploy WAR file with Jtest Coverage Agent
9. Run CTP Container to connect with Jtest Coverage Agent
10. Run Maven Playwright Tests against Deployed WAR running in Container with Jtest Coverage Agent. Setup Tests will start/stop the Test Sessions, and publish coverage from CTP.
11. Once Coverage is published, stop the CTP and Wildfly containers.
12. Run Jenkin Stages to display Finding Results

## Build Commands

Use the below to build the project:

- Package war:

```bash
mvn clean verify -pl sim-core,sim-web,sim-app -Ddependency-check.skip=true -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true
```

- Run Playwright after Docker Container is deployed

```bash
mvn verify -pl sim-playwright -DbaseUrl=<SERVER INFO>/chat-server -Dheadless=true -DignoreHttpsErrors=true -DignoreHttpsErrors=true -DadminUsername=<ADMIN USER> -DadminPassword=<ADMIN PASSWORD> -DuserUsername=<USER> -DuserPassword=<PASSWORD>
```

- Run Jtest Monitor:

```bash
mvn clean test-compile jtest:agent verify jtest:monitor -pl sim-core,sim-web,sim-app -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true -DignoreHttpsErrors=true -DnvdApiKey=<API KEY>
```

- Run Jtest:

```bash
mvn clean test-compile jtest:agent verify jtest:jtest -pl sim-core,sim-web,sim-app -Djtest.config="<TEST CONFIG>" -Ddependency-check.skip=true -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true -DignoreHttpsErrors=true
```

-Run OWASP

```bash
mvn clean verify -pl sim-core,sim-web,sim-app -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true -DnvdApiKey=<API KEY>
```

- Purge OWASP:

```bash
mvn org.owasp:dependency-check-maven:purge
```
