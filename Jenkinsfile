def REPO_NAME = 'strongbox/strongbox'

pipeline {
    agent { label 'opensuse-slave' }
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'maven-3.3.9',
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                          mavenLocalRepo: '/home/jenkins/.m2/repository')
                {
                    sh 'mvn -U clean install -Dspring.profiles.active=quartz-integration-tests'
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
                                sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar" +
                                   " -Dspring.profiles.active=quartz-integration-tests"

                                build(job: "strongbox/strongbox-os-builds", wait: false)
                            }
                        }
                        else {
                            if(BRANCH_NAME.startsWith("PR-"))
                            {
                                withSonarQubeEnv('sonar') {
                                    def PR_NUMBER = env.CHANGE_ID
                                    echo "Triggering sonar analysis in comment-only mode for PR: ${PR_NUMBER}."
                                    sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar " +
                                       " -Psonar-github" +
                                       " -Dsonar.github.repository=${REPO_NAME}" +
                                       " -Dsonar.github.pullRequest=${PR_NUMBER}" +
                                       " -Dspring.profiles.active=quartz-integration-tests"
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
            deleteDir()
        }
    }
}
