pipeline {
    agent {
        label 'Agent'
    }

    environment {
        // URL to checkout project from GIT
        GIT_URL = 'https://github.com/billmcmullin/SIM.git'
        // Branch to checkout project from GIT
        BRANCH = 'release'
        // Parasoft Session Tag for running this build
        SESSION_TAG = 'Jenkins Jtest'
        // Parasoft Test Configuration to run this build
        TEST_CONFIG = 'jtest.dtp://StaticAndUnit'
        // Parasoft Security Compliance Test Configuration to run this build
        SEC_TEST_CONFIG = 'jtest.dtp://OWASP Top 10-2021 [Parasoft 2025.2]'
        // Publish results to Parasoft DTP
        PUBLISH = 'true'
        // Shared output location
        SHARED_DIR = '/home/jenkins/shared/SIM_Java'
        // Persisted OWASP Dependency-Check data cache (critical for CI stability)
        DC_DATA_DIR = '/home/jenkins/shared/dependency-check-data'
        //Integration Tests
        DOCKER_COMPOSE_FILE = 'Wildlfy-Jtest-docker-compose.yml'
        PLAYWRIGHT_BASE_URL = 'https://heavyarms/chat-server'

        // ---- Java selection ----
        // Change this to the JDK version you want to use
        JAVA_VERSION = '24.0.2'
        JAVA_HOME = "/home/jenkins/agent/jdk-${JAVA_VERSION}"
        //JAVA_HOME = "/opt/java/openjdk"
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
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
                    '''
                }
            }
        }

        stage('Run mvn') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([string(credentialsId: 'NIST_API_KEY', variable: 'NVD_API_KEY')]) {
                        sh '''
                            $MAVEN_HOME/mvn clean test-compile jtest:agent verify jtest:monitor -pl sim-core,sim-web,sim-app \
                            -Djtest.settings="jtest_${JOB_NAME}.properties" \
                            -Djtest.publish="${PUBLISH}" \
                            -Dproperty.report.coverage.images="${JOB_NAME}-ALL;${JOB_NAME}-UT;${JOB_NAME}-FT;${JOB_NAME}-MT" \
                            -Dmaven.test.failure.ignore=true \
                            -Dmaven.test.error.ignore=true \
                            -DautoUpdate=false
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
                    """
                }
            }
        }
        stage('Run Jtest OWASP') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        $JTEST_HOME/jtestcli \
                        -data ${WORKSPACE}/target/jtest/monitor/jtest.data.json \
                        -config "${SEC_TEST_CONFIG}" \
                        -settings jtest_${JOB_NAME}.properties \
                        -publish \
                        -report "${WORKSPACE}/report/OWASP" \
                        -exclude "**/test/**/*Test.java"
                    '''
                }
            }
        }

        stage('Run Maven OWASP') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([string(credentialsId: 'NIST_API_KEY', variable: 'NVD_API_KEY')]) {
                        sh '''
                            set -e
                            mkdir -p "$DC_DATA_DIR"

                            $MAVEN_HOME/mvn org.owasp:dependency-check-maven:12.2.2:aggregate \
                              -DnvdApiKey="$NVD_API_KEY" \
                              -DdataDirectory="$DC_DATA_DIR"
                        '''
                    }
                }
            }
        }

        stage('Run Publish OWASP A6') {
            when {
                expression {
                    fileExists("${env.WORKSPACE}/target/dependency-check-report.xml")
                }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set +x
                        echo "dtp.project=SIM Java" > jtest_${JOB_NAME}_OWASP.properties
                        echo "dtp.url=https://dtp:8443" >> jtest_${JOB_NAME}_OWASP.properties
                        echo "dtp.user=ratchet" >> jtest_${JOB_NAME}_OWASP.properties
                        echo "dtp.password=aCvxBC05GFbAjcw1TR0ZlA==" >> jtest_${JOB_NAME}_OWASP.properties
                        echo "parasoft.eula.accepted=true" >> jtest_${JOB_NAME}_OWASP.properties
                        echo "build.id=${BUILD_TAG}" >> jtest_${JOB_NAME}_OWASP.properties
                        echo "session.tag=${SESSION_TAG}" >> jtest_${JOB_NAME}_OWASP.properties

                        $DEPENDENCY_CHECK/dependencycheck.sh \
                        -results.file "${WORKSPACE}/target/dependency-check-report.xml" \
                        -settings "jtest_${JOB_NAME}_OWASP.properties"
                    '''
                }
            }
        }

        stage('Run ESLint') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh '''
                        set +e
                        mkdir -p report/eslint

                        if [ ! -f package.json ]; then
                          npm init -y >/dev/null 2>&1
                        fi

                        npm install --no-audit --no-fund --save-dev eslint@9.12.0

                        npx eslint "sim-app/src/main/webapp/assets/js/**/*.js" -f json \
                          -o report/eslint/eslint-report.json
                        ESLINT_EXIT=$?

                        echo "ESLint exit code: ${ESLINT_EXIT}"
                        exit 0
                    '''
                }
            }
        }

        stage('Prepare monitor and agent files for shared SIM_JAVA') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set -e
                        DEST_DIR="${SHARED_DIR}"
                        MONITOR_DIR="${WORKSPACE}/target/jtest/monitor"
                        MONITOR_ZIP="${MONITOR_DIR}/monitor.zip"

                        mkdir -p "${DEST_DIR}"

                        if [ -f "${MONITOR_ZIP}" ]; then
                            echo "Found monitor.zip: ${MONITOR_ZIP}"
                            unzip -o "${MONITOR_ZIP}" -d "${DEST_DIR}"
                        else
                            echo "WARNING: monitor.zip not found at ${MONITOR_ZIP}"
                        fi
                    '''
                }
            }
        }

        stage('Update shared agent.properties settings') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set -e
                        AGENT_FILE="${SHARED_DIR}/monitor/agent.properties"

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

                        echo "Updated ${AGENT_FILE}:"
                        grep -E '^jtest\\.agent\\.(enableMultiuserCoverage|autoStart)=' "${AGENT_FILE}" || true
                    '''
                }
            }
        }

        stage('Start app with Docker Compose') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set -e
                        docker compose -f "resources/${DOCKER_COMPOSE_FILE}" up -d --build
                        docker compose -f "resources/${DOCKER_COMPOSE_FILE}" ps
                    '''
                }
            }
        }

        stage('Run Playwright Integration Tests') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        set -e
                        $MAVEN_HOME/mvn verify -pl sim-playwright \
                        -DbaseUrl="${PLAYWRIGHT_BASE_URL}" \
                        -Dheadless=true \
                        -DignoreHttpsErrors=true \
                        -DadminUsername=admin \
                        -DadminPassword=admin \
                        -DuserUsername=jonnytest \
                        -DuserPassword=test1234
                    '''
                }
            }
        }

        stage('Publish Unit Test results') {
            steps {
                xunit checksName: '', thresholds: [failed(failureNewThreshold: '200', failureThreshold: '200', unstableNewThreshold: '500', unstableThreshold: '500')], tools: [[$class: 'ParasoftType', deleteOutputFiles: true, failIfNotNew: true, pattern: '**/report/team/report.xml', skipNoTestFiles: false, stopProcessingIfError: true]]
            }
        }

        stage('Publish ESLint Results') {
            when {
                expression { fileExists("${env.WORKSPACE}/report/eslint/eslint-report.json") }
            }
            steps {
                recordIssues(
                    enabledForFailure: true,
                    tools: [esLint(pattern: 'report/eslint/eslint-report.json')]
                )
            }
        }

        stage('Publish Coverage results') {
            steps {
                recordParasoftCoverage coverageQualityGates: [[criticality: 'ERROR', integerThreshold: 1, threshold: 1.0, type: 'PROJECT']], pattern: '**/report/team/coverage.xml'
            }
        }

        stage('Publish Dependency Check Results') {
            when {
                expression { fileExists("${env.WORKSPACE}/target/dependency-check-report.xml") }
            }
            steps {
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }

        stage('Publishing Static Analysis Results') {
            steps {
                recordIssues(
                    tools: [
                        parasoftFindings(
                            pattern: '**/report/**/report.xml',
                            localSettingsPath: "${WORKSPACE}/jtest_${JOB_NAME}.properties"
                        )
                    ]
                )
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'report/eslint/*', allowEmptyArchive: true
        }
        success {
            echo 'success.'
            chuckNorris()
        }
        failure {
            echo 'failed.'
            chuckNorris()
        }
    }
}
