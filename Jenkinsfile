@Library('jenkins-shared-libraries')

def REPO_NAME  = 'strongbox/strongbox'
def SERVER_ID  = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://dev.carlspring.org/nexus/content/repositories/carlspring-oss-snapshots/'

pipeline {
    agent {
        docker {
            args '-v /mnt/ramdisk/3:/home/jenkins --cap-add SYS_ADMIN'
            image 'hub.carlspring.org/jenkins/opensuse-slave:latest'
        }
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
        skipDefaultCheckout()
    }
    stages {
        stage('Setup workspace')
        {
            steps {
                script {
                    env.HDDWS=env.WORKSPACE
                    env.RAMWS="/home/jenkins/workspace/"+ sh(returnStdout: true, script: 'basename "${HDDWS}"').trim()
                    env.RAMMOUNT=env.WORKSPACE+"/ram"

                    cleanWs deleteDirs: true
                    checkout scm

                    echo "Preparing workspace..."
                    sh "mkdir -p '$RAMWS'"
                    sh "cp -R `ls -A '$HDDWS' | grep -v ram` '$RAMWS'"
                    sh "mkdir -p '$RAMMOUNT'"
                    sh "sudo mount --bind  '$RAMWS' '$RAMMOUNT'"
                }
            }
        }
        stage('Building...')
        {
            steps {
                withMaven(maven: 'maven-3.3.9', mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    sh "cd '$RAMMOUNT' && mvn -U clean install -Dintegration.tests -Dprepare.revision -Dmaven.test.failure.ignore=true"
                }
            }
        }
        stage('Code Analysis') {
            steps {
                withMaven(maven: 'maven-3.3.9', mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    script {
                        if(BRANCH_NAME == 'master') {
                            withSonarQubeEnv('sonar') {
                                // requires SonarQube Scanner for Maven 3.2+
                                sh "cd '$RAMMOUNT' && mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar " +
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
                                    sh "cd '$RAMMOUNT' && mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar " +
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
                    withMaven(maven: 'maven-3.3.9', mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                    {
                        sh "cd '$RAMMOUNT' && mvn deploy" +
                           " -DskipTests" +
                           " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                if(BRANCH_NAME == 'master') {
                    build job: "strongbox/strongbox-os-builds", wait: false, parameters: [[$class: 'StringParameterValue', name: 'REVISION', value: '*/master']]
                }
            }
        }
        changed {
            script {
                if(BRANCH_NAME == 'master') {
                    def skype = new org.carlspring.jenkins.notification.skype.Skype()
                    skype.sendNotification("admins;devs");
                }
            }
        }
        always {
            script {
                // unmount and copy back to hdd
                sh "sudo umount --force $RAMMOUNT"
                sh "cp -R '$RAMWS/.' '$RAMMOUNT'"
            }

            // remove unnecessary directories.
            sh "(cd '$HDDWS' && find . -maxdepth 1 ! -name 'ram' ! -name '.' ! -name '..' -exec rm -rf '{}' \\;)"

            // clean up ram
            sh "rm -rf '$RAMWS'"
        }
    }
}
