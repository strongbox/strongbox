@Library('jenkins-shared-libraries')

def REPO_NAME  = 'strongbox/strongbox'
def SERVER_ID  = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://dev.carlspring.org/nexus/content/repositories/carlspring-oss-snapshots/'

def workspaceUtils = new org.carlspring.jenkins.workspace.WorkspaceUtils();

pipeline {
    agent {
        node {
            label 'alpine:jdk8-mvn-3.3'
            customWorkspace workspaceUtils.generateUniqueWorkspacePath()
        }
    }
    parameters {
        booleanParam(defaultValue: true, description: 'Send email notification?', name: 'NOTIFY_EMAIL')
        booleanParam(defaultValue: true, description: 'Trigger strongbox-os-build?', name: 'TRIGGER_OS_BUILD')
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
    }
    stages {
        stage('Node')
        {
            steps {
                sh "cat /etc/node"
                sh "cat /etc/os-release"
                sh "mvn --version"
            }
        }
        stage('Building')
        {
            steps {
                withMaven(mavenLocalRepo: workspaceUtils.generateM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    withEnv(['PATH+MVN_CMD=$MVN_CMD']) {
                        timestamps {
                            sh "mvn -U clean install -Pdependency-convergence-check -Dintegration.tests -Dprepare.revision -Dmaven.test.failure.ignore=true"
                        }
                    }
                }
            }
        }
        stage('Code Analysis') {
            steps {
                withMaven(mavenLocalRepo: workspaceUtils.generateM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833', publisherStrategy: 'EXPLICIT')
                {
                    withEnv(['PATH+MVN_CMD=$MVN_CMD']) {
                        script {
                            if(BRANCH_NAME == 'master') {
                                withSonarQubeEnv('sonar') {
                                    // requires SonarQube Scanner for Maven 3.2+
                                    sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar " +
                                       "-Dintegration.tests " +
                                       "-Dprepare.revision" +
                                       "-Ddownloader.quick.query.timestamp=false " +
                                       "-Dformat=XML " +
                                       "-Dsonar.dependencyCheck.reportPath=${WORKSPACE}/dependency-check-report.xml " +
                                       "-Pdependency-check "
                                }
                            }
                            else {
                                if(BRANCH_NAME.startsWith("PR-"))
                                {
                                    withSonarQubeEnv('sonar') {
                                        def PR_NUMBER = env.CHANGE_ID
                                        echo "Triggering sonar analysis in comment-only mode for PR: ${PR_NUMBER}."
                                        sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar " +
                                           "-Dintegration.tests " +
                                           "-Dprepare.revision " +
                                           "-Dsonar.github.repository=${REPO_NAME} " +
                                           "-Dsonar.github.pullRequest=${PR_NUMBER} " +
                                           "-Dsonar.dependencyCheck.reportPath=${WORKSPACE}/dependency-check-report.xml " +
                                           "-Ddownloader.quick.query.timestamp=false " +
                                           "-Dformat=XML " +
                                           "-Pdependency-check " +
                                           "-Psonar-github"
                                    }
                                }
                                else
                                {
                                    echo "This step is skipped for branches other than master or PR-*"
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('Deploy') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                script {
                    withMaven(mavenLocalRepo: workspaceUtils.generateM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833', publisherStrategy: 'EXPLICIT')
                    {
                        withEnv(['PATH+MVN_CMD=$MVN_CMD']) {
                            sh "mvn deploy" +
                               " -DskipTests" +
                               " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                if(BRANCH_NAME == 'master' && params.TRIGGER_OS_BUILD) {
                    build job: "strongbox/strongbox-os-builds", wait: false, parameters: [[$class: 'StringParameterValue', name: 'REVISION', value: '*/master']]
                }
            }
        }
        always {
            // Email notification
            script {
                if(params.NOTIFY_EMAIL) {
                    def email = new org.carlspring.jenkins.notification.email.Email()
                    if(BRANCH_NAME == 'master') {
                        email.sendNotification()
                    } else {
                        email.sendNotification(null, false, null, [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']])
                    }
                }
            }

            // (fallback) record test results even if withMaven should have done that already.
            junit '**/target/*-reports/*.xml'

            // Cleanup workspace.
            cleanWs deleteDirs: true, externalDelete: 'rm -rf %s', notFailBuild: true
        }
    }
}

