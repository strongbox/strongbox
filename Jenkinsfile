@Library('jenkins-shared-libraries') _

def REPO_NAME  = 'strongbox/strongbox-npm-metadata'
def SERVER_ID  = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://repo.carlspring.org/content/repositories/carlspring-oss-snapshots/'

// Notification settings for "master" and "branch/pr"
def notifyMaster = [notifyAdmins: true, recipients: [culprits(), requestor()]]
def notifyBranch = [recipients: [brokenTestsSuspects(), requestor()]]

pipeline {
    agent {
        node {
            label 'alpine:jdk8-mvn-3.5'
            customWorkspace workspace().getUniqueWorkspacePath()
        }
    }
    parameters {
        booleanParam(defaultValue: true, description: 'Send email notification?', name: 'NOTIFY_EMAIL')
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
    }
    stages {
        stage('Node')
        {
            steps {
                nodeInfo("mvn")
            }
        }
        stage('Building')
        {
            steps {
                withMavenPlus(timestamps: true, mavenLocalRepo: workspace().getM2LocalRepoPath(), mavenSettingsConfig: '67aaee2b-ca74-4ae1-8eb9-c8f16eb5e534')
                {
                    sh "mvn -U clean install -Dmaven.test.failure.ignore=true"
                }
            }
        }
        stage('Code Analysis') {
            steps {
                withMavenPlus(mavenLocalRepo: workspace().getM2LocalRepoPath(), mavenSettingsConfig: '67aaee2b-ca74-4ae1-8eb9-c8f16eb5e534', publisherStrategy: 'EXPLICIT')
                {
                    script
                    {
                        if(BRANCH_NAME == 'master')
                        {
                            withSonarQubeEnv('sonar')
                            {
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
                                withSonarQubeEnv('sonar')
                                {
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
        stage('Deploy') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                script {
                    withMavenPlus(mavenLocalRepo: workspace().getM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833', publisherStrategy: 'EXPLICIT')
                    {
                        sh "mvn deploy" +
                           " -DskipTests" +
                           " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                    }
                }
            }
        }
    }
    post {
        failure {
            script {
                if(params.NOTIFY_EMAIL) {
                    notifyFailed((BRANCH_NAME == "master") ? notifyMaster : notifyBranch)
                }
            }
        }
        unstable {
            script {
                if(params.NOTIFY_EMAIL) {
                    notifyUnstable((BRANCH_NAME == "master") ? notifyMaster : notifyBranch)
                }
            }
        }
        fixed {
            script {
                if(params.NOTIFY_EMAIL) {
                    notifyFixed((BRANCH_NAME == "master") ? notifyMaster : notifyBranch)
                }
            }
        }
        always {
            // (fallback) record test results even if withMaven should have done that already.
            junit '**/target/*-reports/*.xml'
        }
        cleanup {
            script {
                workspace().clean()
            }
        }
    }
}
