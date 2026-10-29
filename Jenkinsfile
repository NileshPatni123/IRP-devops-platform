// Pipeline as code: checkout -> build/unit test -> package -> Docker image -> test deployment
// -> Selenium -> (publish image) -> deploy.  A failing test stops the pipeline before any deployment.
// Works on Linux and Windows Jenkins agents (uses sh or bat automatically).

def cmd(String c, boolean ignoreFailure = false) {
    int rc = isUnix() ? sh(script: c, returnStatus: true) : bat(script: c, returnStatus: true)
    if (rc != 0 && !ignoreFailure) {
        error("Command failed (exit ${rc}): ${c}")
    }
    return rc
}

def waitForHealth(String url) {
    boolean up = false
    for (int i = 0; i < 40 && !up; i++) {
        up = (cmd("curl -fs ${url}", true) == 0)
        if (!up) { sleep(time: 3, unit: 'SECONDS') }
    }
    if (!up) { error("Application did not become healthy at ${url}") }
}

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
    }

    triggers { pollSCM('H/2 * * * *') }   // Task 7: commit-triggered polling (also add a GitHub webhook if reachable)

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['staging', 'production'], description: 'Target environment (sets APP_ENVIRONMENT in the container)')
        string(name: 'HTTP_PORT', defaultValue: '8080', description: 'Host port for the deployed container')
        booleanParam(name: 'INJECT_DEFECT', defaultValue: false, description: 'Task 10: build a test container with the deliberate search defect')
        booleanParam(name: 'SECURITY_SCAN', defaultValue: false, description: 'Scan the image with Trivy (HIGH and CRITICAL). Reports findings; does not block deployment')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false, description: 'Task 12: push the versioned image to the registry')
        string(name: 'REGISTRY', defaultValue: 'docker.io/your-dockerhub-user', description: 'Registry namespace, e.g. docker.io/nilesh or localhost:5000')
        string(name: 'REGISTRY_CREDENTIALS', defaultValue: 'dockerhub-creds', description: 'Jenkins username/password credentials id for the registry')
    }

    environment {
        IMAGE   = 'irp-portal'
        VERSION = "1.0.${env.BUILD_NUMBER}"
        NETWORK = 'irp-net'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Unit Test') {
            steps { script { cmd('mvn -B clean verify') } }
            post {
                always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
            }
        }

        stage('Package & Archive') {
            steps { archiveArtifacts artifacts: 'target/*.war', fingerprint: true }
        }

        stage('Docker Build') {
            steps {
                script {
                    cmd("docker build -t ${IMAGE}:${VERSION} -t ${IMAGE}:latest .")
                    cmd("docker image inspect ${IMAGE}:${VERSION}")
                }
            }
        }

        stage('Security Scan (Trivy)') {
            when { expression { return params.SECURITY_SCAN } }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script { cmd("docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image --severity HIGH,CRITICAL --exit-code 1 ${IMAGE}:${VERSION}") }
                }
            }
        }

        stage('Deploy Test Environment') {
            steps {
                script {
                    cmd("docker network create ${NETWORK}", true)
                    cmd('docker rm -f irp-test irp-selenium', true)
                    cmd("docker run -d --name irp-test --network ${NETWORK} -p 8081:8080 " +
                        "-e APP_ENVIRONMENT=test -e APP_VERSION=${VERSION} -e APP_DEMO_DEFECT=${params.INJECT_DEFECT} ${IMAGE}:${VERSION}")
                    cmd("docker run -d --name irp-selenium --network ${NETWORK} -p 4444:4444 --shm-size=2g selenium/standalone-chrome:latest")
                    waitForHealth('http://localhost:8081/actuator/health')
                    waitForHealth('http://localhost:4444/wd/hub/status')
                }
            }
        }

        stage('Selenium Tests') {
            steps {
                dir('selenium-tests') {
                    script { cmd('mvn -B test -Dbase.url=http://irp-test:8080 -Dremote.url=http://localhost:4444/wd/hub') }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'selenium-tests/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'selenium-tests/target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Publish Image') {
            when { expression { return params.PUSH_IMAGE } }
            steps {
                withCredentials([usernamePassword(credentialsId: params.REGISTRY_CREDENTIALS, usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
                    script {
                        String host = params.REGISTRY.contains('/') ? params.REGISTRY.split('/')[0] : params.REGISTRY
                        if (isUnix()) {
                            sh "echo \"\$REG_PASS\" | docker login ${host} -u \"\$REG_USER\" --password-stdin"
                        } else {
                            bat "echo %REG_PASS%| docker login ${host} -u %REG_USER% --password-stdin"
                        }
                        cmd("docker tag ${IMAGE}:${VERSION} ${params.REGISTRY}/${IMAGE}:${VERSION}")
                        cmd("docker tag ${IMAGE}:${VERSION} ${params.REGISTRY}/${IMAGE}:latest")
                        cmd("docker push ${params.REGISTRY}/${IMAGE}:${VERSION}")
                        cmd("docker push ${params.REGISTRY}/${IMAGE}:latest")
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    cmd("docker rm -f irp-portal", true)
                    cmd("docker run -d --name irp-portal --restart unless-stopped -p ${params.HTTP_PORT}:8080 " +
                        "-v irp-data:/data -e SPRING_DATASOURCE_URL=jdbc:h2:file:/data/irp " +
                        "-e APP_ENVIRONMENT=${params.ENVIRONMENT} -e APP_VERSION=${VERSION} ${IMAGE}:${VERSION}")
                    waitForHealth("http://localhost:${params.HTTP_PORT}/actuator/health")
                    echo "Deployed ${IMAGE}:${VERSION} to ${params.ENVIRONMENT} at http://localhost:${params.HTTP_PORT}/"
                }
            }
        }
    }

    post {
        always {
            script {
                cmd('docker logs irp-test', true)
                cmd('docker rm -f irp-test irp-selenium', true)
            }
        }
        success { echo "SUCCESS: commit -> test -> container (${env.IMAGE}:${env.VERSION})" }
        failure { echo 'FAILED: if a test stage failed, nothing was deployed. If Deploy failed, restore the last good version with the Ansible rollback playbook (docs/RUNBOOK.md, Task 14).' }
    }
}
