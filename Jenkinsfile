@Library('jenkins-shared-libraries')

def REPO_NAME = 'strongbox/strongbox'

pipeline {
    agent { label 'opensuse-slave' }
    options {
        timeout(time: 2, unit: 'HOURS')
    }
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'maven-3.3.9',
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                          mavenLocalRepo: '/home/jenkins/.m2/repository')
                {
                    sh 'mvn -U clean install -Dintegration.tests -Dprepare.revision'
                }
            }
        }
        stage('Code Analysis') {
            steps {
                withMaven(maven: 'maven-3.3.9', 
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                          mavenLocalRepo: '/home/jenkins/.m2/repository')
                {
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

                                build(job: "strongbox/strongbox-os-builds", wait: false)
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
    post {
        always {
            script {
                if(BRANCH_NAME == 'master' && currentBuild.result != 'ABORTED') {
                    def skype = new org.carlspring.jenkins.notification.skype.Skype()
                    skype.sendNotification("admins;devs");
                }
            }
            deleteDir()
        }
    }
}
