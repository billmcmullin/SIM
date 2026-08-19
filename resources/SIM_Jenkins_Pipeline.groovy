pipeline {
    agent {
        label 'Agent'
    }

    environment {
        // URL to checkout project from GIT
        GIT_URL                = 'https://github.com/billmcmullin/SIM.git'
        // Branch to checkout project from GIT
        BRANCH                 = 'release'
        // Parasoft Session Tag for running this build
        SESSION_TAG            = 'Jenkins Jtest'
        // Parasoft Test Configuration to run this build
        TEST_CONFIG            = 'jtest.dtp://StaticAndUnit'
        // Parasoft Security Compliance Test Configruation to run 2025 OWASP
        OWASP_2025_TEST_CONFIG = 'jtest.dtp://OWASP Top 10-2025 [Parasoft 2026.1]'
        // Parasoft Security Compliance Test Configuration for CWE
        CWE_TEST_CONFIG        = 'jtest.dtp://CWE Top 25 + On the Cusp 2025 [Parasoft 2026.1]'
        // Publish results to Parasoft DTP
        PUBLISH                = 'true'
        // Shared output location
        SHARED_DIR             = '/home/jenkins/shared/SIM_Java'
        // Persisted OWASP Dependency-Check data cache (critical for CI stability)
        DC_DATA_DIR            = '/home/jenkins/shared/dependency-check-data'
        // Integration Tests
        DOCKER_COMPOSE_FILE    = 'Wildfly-Jtest-docker-compose.yml'
        PLAYWRIGHT_BASE_URL    = 'http://chatserver:8080/chat-server'

        //CTP Information for Coverage
        CTP_WEBSOCKET           = 'wss://ctp:8080/em/coverage/websocket'
        CTP_QUEUE               = '/user/queue/environments/4/components/2/coverage'
        TEST_USER               = 'jonnytest'
    }

    stages {
        stage('Checkout') {
            steps {
                git url: "${GIT_URL}", branch: "${BRANCH}"
            }
        }

        stage('Create Jtest Properties file') {
            steps {
                script {
                    sh '''
                        echo "dtp.project=SIM Java" > jtest_${JOB_NAME}.properties
                        echo "parasoft.eula.accepted=true" >> jtest_${JOB_NAME}.properties
                        echo "build.id=${BUILD_TAG}" >> jtest_${JOB_NAME}.properties
                        echo "session.tag=${SESSION_TAG}" >> jtest_${JOB_NAME}.properties
                        echo "report.coverage.images=${JOB_NAME}-ALL;${JOB_NAME}-UT" >> jtest_${JOB_NAME}.properties
                        echo "scope.scontrol=true" >> jtest_${JOB_NAME}.properties
                        echo "scontrol.rep1.type=git" >> jtest_${JOB_NAME}.properties
                        echo "scontrol.rep1.git.url=${GIT_URL}" >> jtest_${JOB_NAME}.properties
                        echo "scontrol.rep1.git.branch=${BRANCH}" >> jtest_${JOB_NAME}.properties
                        echo "scontrol.rep1.git.workspace=${WORKSPACE}" >> jtest_${JOB_NAME}.properties
                        echo "scontrol.git.exec=/usr/bin/git" >> jtest_${JOB_NAME}.properties
                        echo "report.scontrol=full" >> jtest_${JOB_NAME}.properties
                        echo "license.release.on.exit=true" >> jtest_${JOB_NAME}.properties

                        echo "dtp.project=SIM Java" > jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "dtp.url=https://dtp:8443" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "dtp.user=ratchet" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "dtp.password=aCvxBC05GFbAjcw1TR0ZlA==" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "parasoft.eula.accepted=true" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "build.id=${BUILD_TAG}" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "session.tag=${SESSION_TAG}" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "scope.scontrol=true" >> jtest_${JOB_NAME}.properties
                        echo "scontrol.rep1.type=git" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "scontrol.rep1.git.url=${GIT_URL}" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "scontrol.rep1.git.branch=${BRANCH}" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "scontrol.rep1.git.workspace=${WORKSPACE}" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "scontrol.git.exec=/usr/bin/git" >> jtest_${JOB_NAME}_3RDCHECK.properties
                        echo "report.scontrol=full" >> jtest_${JOB_NAME}_3RDCHECK.properties
                    '''
                }
            }
        }

        stage('Run mvn') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([string(credentialsId: 'NIST_API_KEY', variable: 'NVD_API_KEY')]) {
                        sh '''
                            ./mvnw clean test-compile jtest:agent verify jtest:monitor \
                                -Djtest.settings="${WORKSPACE}/jtest_${JOB_NAME}.properties" \
                                -Djtest.publish="${PUBLISH}" \
                                -Dproperty.report.coverage.images="${JOB_NAME}-ALL;${JOB_NAME}-UT;${JOB_NAME}-FT;${JOB_NAME}-MT;${JOB_NAME}-Play" \
                                -Dmaven.test.failure.ignore=true \
                                -Dmaven.test.error.ignore=true \
                                -DautoUpdate=false \
                                -s /home/jenkins/agent/conf/settings.xml
                        '''
                    }
                }
            }
        }

        stage('Run Jtestcli') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh """
                        \$JTEST_HOME/jtestcli -data ${WORKSPACE}/target/jtest/monitor/jtest.data.json \
                            -config "${TEST_CONFIG}" \
                            -settings jtest_${JOB_NAME}.properties \
                            -publish \
                            -report "${WORKSPACE}/report/team"

                        \$JTEST_HOME/jtestcli \
                            -data ${WORKSPACE}/target/jtest/monitor/jtest.data.json \
                            -config "${OWASP_2025_TEST_CONFIG}" \
                            -settings jtest_${JOB_NAME}.properties \
                            -publish \
                            -report "${WORKSPACE}/report/OWASP2025" 

                        \$JTEST_HOME/jtestcli \
                            -data ${WORKSPACE}/target/jtest/monitor/jtest.data.json \
                            -config "${CWE_TEST_CONFIG}" \
                            -settings jtest_${JOB_NAME}.properties \
                            -publish \
                            -report "${WORKSPACE}/report/CWE" \
                    """
                }
            }
        }

        stage('Prepare and Run Integration Tests') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set -e
                        DEST_DIR="${SHARED_DIR}"
                        MONITOR_DIR="${WORKSPACE}/target/jtest/monitor"
                        MONITOR_ZIP="${MONITOR_DIR}/monitor.zip"
                        AGENT_FILE="${SHARED_DIR}/monitor/agent.properties"

                        mkdir -p "${DEST_DIR}"

                        if [ -f "${MONITOR_ZIP}" ]; then
                            echo "Found monitor.zip: ${MONITOR_ZIP}"
                            unzip -o "${MONITOR_ZIP}" -d "${DEST_DIR}"
                        else
                            echo "WARNING: monitor.zip not found at ${MONITOR_ZIP}"
                        fi

                        if [ ! -f "${AGENT_FILE}" ]; then
                            echo "WARNING: ${AGENT_FILE} not found. Skipping update."
                            exit 0
                        fi

                        if grep -q '^jtest\\.agent\\.enableMultiuserCoverage=' "${AGENT_FILE}"; then
                            sed -i 's/^jtest\\.agent\\.enableMultiuserCoverage=.*/jtest.agent.enableMultiuserCoverage=true/' "${AGENT_FILE}"
                        else
                            echo 'jtest.agent.enableMultiuserCoverage=true' >> "${AGENT_FILE}"
                        fi

                        if grep -q '^jtest\\.agent\\.autoStart=' "${AGENT_FILE}"; then
                            sed -i 's/^jtest\\.agent\\.autoStart=.*/jtest.agent.autoStart=false/' "${AGENT_FILE}"
                        else
                            echo 'jtest.agent.autoStart=false' >> "${AGENT_FILE}"
                        fi

                        if grep -q '^jtest\\.agent\\.jbossCompatibilityMode=' "${AGENT_FILE}"; then
                            sed -i 's/^jtest\\.agent\\.jbossCompatibilityMode=.*/jtest.agent.jbossCompatibilityMode=true/' "${AGENT_FILE}"
                        else
                            echo 'jtest.agent.jbossCompatibilityMode=true' >> "${AGENT_FILE}"
                        fi

                        if grep -q '^jtest\\.agent\\.restServerEnabled=' "${AGENT_FILE}"; then
                            sed -i 's/^jtest\\.agent\\.restServerEnabled=.*/jtest.agent.restServerEnabled=true/' "${AGENT_FILE}"
                        else
                            echo 'jtest.agent.restServerEnabled=true' >> "${AGENT_FILE}"
                        fi
                        
                        if grep -q 'ctp\\.websocket\\.url=' "${AGENT_FILE}"; then
                            sed -i "s/^ctp\\.websocket\\.url=.*/ctp.websocket.url=${CTP_WEBSOCKET}/" "${AGENT_FILE}"
                        else
                            echo "ctp.websocket.url=${CTP_WEBSOCKET}" >> "${AGENT_FILE}"
                        fi

                        if grep -q 'ctp\\.subscription\\.url=' "${AGENT_FILE}"; then
                            sed -i 's/^ctp\\.subscription\\.url=.*/ctp.subscription.url=${CTP_QUEUE}/' "${AGENT_FILE}"
                        else
                            echo "ctp.subscription.queue=${CTP_QUEUE}" >> "${AGENT_FILE}"
                        fi
                        
                        if grep -q 'dtp\\.buildID=' "${AGENT_FILE}"; then
                            sed -i "s/^dtp\\.buildID=.*/dtp.buildID=${BUILD_TAG}/" "${AGENT_FILE}"
                        else
                            echo "dtp.buildID=${BUILD_TAG}" >> "${AGENT_FILE}"
                        fi
                        
                        if grep -q 'dtp\\.project=' "${AGENT_FILE}"; then
                            sed -i 's/^dtp\\.project=.*/dtp.project=SIM Java/' "${AGENT_FILE}"
                        else
                            echo "dtp.project=SIM Java" >> "${AGENT_FILE}"
                        fi
                        
                        if grep -q 'dtp\\.coverageImages=' "${AGENT_FILE}"; then
                            sed -i "s/^dtp\\.coverageImages=.*/dtp.coverageImages=${JOB_NAME}-ALL;${JOB_NAME}-Play;${JOB_NAME}-FT/" "${AGENT_FILE}"
                        else
                            echo "dtp.coverageImages=${JOB_NAME}-ALL;${JOB_NAME}-Play;${JOB_NAME}-FT" >> "${AGENT_FILE}"
                        fi

                        echo "Updated ${AGENT_FILE}:"
                        grep -E '^jtest\\.agent\\.(enableMultiuserCoverage|autoStart|jbossCompatibilityMode|restServerEnabled)=' "${AGENT_FILE}" || true
                        grep -E '^ctp\\.(subscription|websocket|)\\.(queue|url)=' "${AGENT_FILE}" || true
                        grep -E '^dtp\\.(coverageImages|project)\\.(queue|url)=' "${AGENT_FILE}" || true

                        docker compose -f "${WORKSPACE}/resources/${DOCKER_COMPOSE_FILE}" restart
                        docker compose -f "${WORKSPACE}/resources/${DOCKER_COMPOSE_FILE}" ps

                        ./mvnw verify -pl sim-playwright \
                            -DbaseUrl="${PLAYWRIGHT_BASE_URL}" \
                            -Dheadless=true \
                            -DignoreHttpsErrors=true \
                            -DadminUsername=admin \
                            -DadminPassword=admin \
                            -DuserUsername=jonnytest \
                            -DuserPassword=test1234 \
                            -Dexec.args="install --with-deps chromium" \
                            -Dmaven.test.failure.ignore=true \
                            -Dmaven.test.error.ignore=true \
                            -Dexec.mainClass=com.microsoft.playwright.CLI \
                            -Dparasoft.coverage.baggageHeader="test-operator-id=${TEST_USER}" \
                            -Dplaywright.skipITs=false
                    '''
                }
            }
        }

        stage('Run ESLint') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set +e
                        mkdir -p report/eslint

                        if [ ! -f package.json ]; then
                            npm init -y >/dev/null 2>&1
                        fi

                        npm install --no-audit --package-lock --no-fund --save-dev eslint@10.4.0 eslint-formatter-checkstyle
                        npm ls @eslint/plugin-kit || true

                        # JSON report for Jenkins Warnings NG
                        npx eslint "sim-app/src/main/webapp/assets/js/**/*.js" \
                            -f json \
                            -o report/eslint/eslint-report.json
                        ESLINT_EXIT=$?

                        echo "ESLint exit code: ${ESLINT_EXIT}"

                        # Checkstyle XML report for Parasoft MLP input
                        npx eslint "sim-app/src/main/webapp/assets/js/**/*.js" \
                            -f checkstyle \
                            -o report/eslint/eslint-report.xml || true

                        echo "Running Parasoft MLP"
                        ${MLP}/multilanguage-pack.sh -tool "eslint" \
                            -source.dir "${WORKSPACE}/sim-app" \
                            -results.file "${WORKSPACE}/report/eslint/eslint-report.xml" \
                            -settings "jtest_${JOB_NAME}_3RDCHECK.properties"

                        exit 0
                    '''
                }
            }
        }

        stage('Run Maven OWASP for A6') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([string(credentialsId: 'NIST_API_KEY', variable: 'NVD_API_KEY')]) {
                        sh '''
                            set -e
                            mkdir -p "$DC_DATA_DIR"

                            ./mvnw org.owasp:dependency-check-maven:12.2.2:aggregate \
                                -DnvdApiKey="$NVD_API_KEY" \
                                -DdataDirectory="$DC_DATA_DIR" \
                                -DskipTests=true \
                                -DnodeAnalyzerEnabled=false \
                                -DnodeAuditAnalyzerEnabled=false \
                                -DretireJsAnalyzerEnabled=fals \
                                -DassemblyAnalyzerEnabled=false \
                                -DnugetconfAnalyzerEnabled=false \
                                -DnuspecAnalyzerEnabled=false \
                                -DmsbuildAnalyzerEnabled=fals \
                                -fn

                            $DEPENDENCY_CHECK/dependencycheck.sh \
                                -results.file "${WORKSPACE}/target/dependency-check-report.xml" \
                                -settings "jtest_${JOB_NAME}_3RDCHECK.properties"
                        '''
                    }
                }
            }
        }

        stage('Publish Results') {
            steps {
                script {
                    xunit(
                        checksName: '',
                        thresholds: [
                            failed(
                                failureNewThreshold: '200',
                                failureThreshold: '200',
                                unstableNewThreshold: '500',
                                unstableThreshold: '500'
                            )
                        ],
                        tools: [
                            [
                                $class: 'ParasoftType',
                                deleteOutputFiles: true,
                                failIfNotNew: true,
                                pattern: '**/report/team/report.xml',
                                skipNoTestFiles: false,
                                stopProcessingIfError: true
                            ]
                        ]
                    )

                    recordParasoftCoverage(
                        coverageQualityGates: [[criticality: 'ERROR', integerThreshold: 1, threshold: 1.0, type: 'PROJECT']],
                        pattern: '**/report/team/coverage.xml'
                    )

                    if (fileExists("${env.WORKSPACE}/report/eslint/eslint-report.json")) {
                        recordIssues(
                            enabledForFailure: true,
                            tools: [esLint(pattern: 'report/eslint/eslint-report.json')],
                            qualityGates: [[threshold: 999999, type: 'TOTAL', unstable: true]]
                        )
                    }

                    if (fileExists("${env.WORKSPACE}/target/dependency-check-report.xml")) {
                        dependencyCheckPublisher(
                            pattern: '**/dependency-check-report.xml',
                            failedTotalHigh: 999999,
                            failedTotalMedium: 999999,
                            failedTotalLow: 999999,
                            unstableTotalHigh: 999999,
                            unstableTotalMedium: 999999,
                            unstableTotalLow: 999999
                        )
                    }

                    recordIssues(
                        tools: [
                            parasoftFindings(
                                pattern: '**/report/**/report.xml',
                                localSettingsPath: "${WORKSPACE}/jtest_${JOB_NAME}.properties"
                            )
                        ],
                        qualityGates: []
                    )
                }
            }
        }
    }

    post {
        always {
            // Personal testing mode: force SUCCESS even if publishers mark UNSTABLE
            script {
                currentBuild.result = 'SUCCESS'
            }
        }
        success {
            echo 'success.'
            chuckNorris()
        }
        failure {
            echo 'failed.'
            chuckNorris()
        }
        unstable {
            echo 'unstable.'
            chuckNorris()
        }
    }
}
