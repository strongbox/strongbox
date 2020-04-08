import org.jenkinsci.plugins.workflow.libs.Library
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

@Library('jenkins-shared-libraries') _

// parameters start
// Note: params are saved as `params.INTEGRATION_TESTS_BRANCH`, but when you use the `input()` step you need to save the result in a var.
// However, you can't overwrite the `params.INTEGRATION_TESTS_BRANCH` variable, so you need to use a totally separate one.
// Also, when you run the build for the first time, you might have empty parameters.
def INTEGRATION_TESTS_BRANCH = params.getOrDefault("INTEGRATION_TESTS_BRANCH", null)
def ADDITIONAL_BUILDS = params.getOrDefault("ADDITIONAL_BUILDS", "0")
def RUN_ONLY_SMOKE = params.getOrDefault("RUN_ONLY_SMOKE", false)
def RUN_ONLY_IT = params.getOrDefault("RUN_ONLY_IT", false)
def SKIP_CONFIRM = params.getOrDefault("SKIP_CONFIRM", false)
def SKIP_TESTS = params.getOrDefault("SKIP_TESTS", false)
def M2_CLEAN = params.getOrDefault("M2_CLEAN", false)
def DEPLOY = params.getOrDefault("DEPLOY", true)
def NOTIFY_EMAIL = params.getOrDefault("NOTIFY_EMAIL", true)
// parameters end

// global build variables start
// maven
def BUILD_STAGE_M2_REPO = workspace().getM2LocalRepoPath("os-linux")
def MVN_ARGS = params.getOrDefault("MVN_ARGS", "-Dlogging.debug=false -B -U clean install -T 4 -Daether.connector.resumeDownloads=false -Dintegration.tests -Dprepare.revision --fail-at-end")
MVN_ARGS = MVN_ARGS.replaceAll("(?i)-DskipTests", "") // don't allow -DskipTests so we can control deploy stage.
def MVN_OPTS = params.getOrDefault("MVN_OPTS", '-XX:+TieredCompilation -XX:TieredStopAtLevel=1')
def SERVER_ID = 'carlspring'
def SERVER_URL = 'https://repo.carlspring.org/content/repositories/carlspring-oss-snapshots/'

// notification
def AUTHOR
def NOTIFY_MASTER = [notifyAdmins: true, recipients: [requestor()]]
def NOTIFY_BRANCH = [recipients: [brokenTestsSuspects(), requestor()]]

// github stage notification (SUCCESS = successful run; FAILURE = failed/aborted;)
def INTEGRATION_TESTS_STAGE_STATUS = null
def SMOKE_STAGE_STATUS = null
def INTEGRATION_TESTS_GITHUB_CONTEXT = "web-integration-tests"
def SMOKE_GITHUB_CONTEXT = "smoke-tests"
// global build variables end


// set parameters start
// KEEP THIS IN SYNC WITH THE CODE BELOW
BUILD_PARAMS = [
        string(defaultValue: '', description: 'Use a specific branch/pr of strongbox-web-integration-tests? (adding a value here will skip the confirmation stage)', name: 'INTEGRATION_TESTS_BRANCH', trim: true),
        string(defaultValue: '-B -U clean install -T 4 -Daether.connector.resumeDownloads=false -Dintegration.tests -Dprepare.revision --fail-at-end', description: 'Arguments passed after mvm command.', name: 'MVN_ARGS', trim: true),
        string(defaultValue: '-XX:+TieredCompilation -XX:TieredStopAtLevel=1', name: 'MVN_OPTS', trim: true),
        string(defaultValue: '0', description: 'If this field is set, it will queue additional builds (useful in parallel testing)', name: 'ADDITIONAL_BUILDS', trim: true),
        booleanParam(defaultValue: false, description: 'Run only integration tests? (will skip deploy stage due to skipped integration tests stage)', name: 'RUN_ONLY_IT'),
        booleanParam(defaultValue: false, description: 'Run only smoke tests? (will skip deploy stage due to skipped integration tests stage)', name: 'RUN_ONLY_SMOKE'),
        booleanParam(defaultValue: false, description: 'Run build stage with -DskipTests? (will also skip the deploy stage)', name: 'SKIP_TESTS'),
        booleanParam(defaultValue: false, description: 'Don\'t wait at the confirmation stage and skip all remaining stages.', name: 'SKIP_CONFIRM'),
        booleanParam(defaultValue: false, description: 'Clear .m2 cache for before build starts?', name: 'M2_CLEAN'),
        booleanParam(defaultValue: true, description: 'Deploy? (will not run if integration tests are not run or branch is not master)', name: 'DEPLOY'),
        booleanParam(defaultValue: true, description: 'Send email notification?', name: 'NOTIFY_EMAIL'),
]
properties([parameters(BUILD_PARAMS)])
// set parameters end

if (ADDITIONAL_BUILDS.isInteger() && ADDITIONAL_BUILDS.toInteger() > 0)
{
    currentBuild.description = "Build: ${ADDITIONAL_BUILDS}"

    // Jenkins cannot "just" pass the build parameters down the line.
    // DO NOT attempt to use solutions from SO which suggest to use `params.each` - it won't work as it will
    // mess the order in which the params need to be passed to the job which will create unexpected build results and
    // cast errors. Also - we can't queue more than one additional job, because "unknown reasons", which is why we do
    // it this way.
    def BUILDS_LEFT = ADDITIONAL_BUILDS.toInteger() - 1;

    if (BUILDS_LEFT >= 0)
    {
        println "Queuing additional build: ${BUILDS_LEFT} left"
        build job: env.JOB_NAME, wait: false, parameters: [
                string(name: 'INTEGRATION_TESTS_BRANCH', value: INTEGRATION_TESTS_BRANCH),
                string(name: 'MVN_ARGS', value: MVN_ARGS),
                string(name: 'MVN_OPTS', value: MVN_OPTS),
                string(name: 'ADDITIONAL_BUILDS', value: BUILDS_LEFT.toString()),
                booleanParam(name: 'RUN_ONLY_IT', value: RUN_ONLY_IT),
                booleanParam(name: 'RUN_ONLY_SMOKE', value: RUN_ONLY_SMOKE),
                booleanParam(name: 'SKIP_TESTS', value: SKIP_TESTS),
                booleanParam(name: 'SKIP_CONFIRM', value: SKIP_CONFIRM),
                booleanParam(name: 'M2_CLEAN', value: M2_CLEAN),
                booleanParam(name: 'DEPLOY', value: DEPLOY),
                booleanParam(name: 'NOTIFY_EMAIL', value: NOTIFY_EMAIL)
        ]
    }
}

pipeline {
    agent none
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: env.BRANCH_NAME == 'master' ? '' : '7', numToKeepStr: env.BRANCH_NAME == 'master' ? '1000' : '15')
        timeout(time: 2, unit: 'HOURS')
        disableResume()
        durabilityHint 'PERFORMANCE_OPTIMIZED'
        disableConcurrentBuilds()
        skipStagesAfterUnstable()
        preserveStashes(buildCount: 1)
    }
    stages {
        stage('OS') {
            parallel {
                stage('linux') {
                    agent {
                        label 'alpine-jdk8-mvn3.6'
                    }
                    options {
                        checkoutToSubdirectory 'linux'
                    }
                    steps {
                        container('maven') {
                            script {
                                setGithubStatus(INTEGRATION_TESTS_GITHUB_CONTEXT, "PENDING", "This commit is being built")
                                setGithubStatus(SMOKE_GITHUB_CONTEXT, "PENDING", "This commit is being built")

                                nodeInfo('mvn')

                                dir('linux') {

                                    if (M2_CLEAN)
                                    {
                                        sh label: "Clearing .m2 cache before proceeding",
                                           script: "rm -rf ${BUILD_STAGE_M2_REPO}/*"
                                    }

                                    AUTHOR = sh(returnStdout: true, script: "git log -1 --format='%ae' HEAD").trim()
                                    COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

                                    if(currentBuild.description) {
                                        currentBuild.description = currentBuild.description + " <br> #" + COMMIT
                                    }

                                    withMavenPlus(mavenLocalRepo: BUILD_STAGE_M2_REPO,
                                                  mavenSettingsConfig: '67aaee2b-ca74-4ae1-8eb9-c8f16eb5e534',
                                                  mavenOpts: MVN_OPTS,
                                                  options: [artifactsPublisher(disabled: true)]) {
                                        def buildCmd = "mvn ${MVN_ARGS}" + (SKIP_TESTS ? " -DskipTests" : "") + " -Pbuild-rpm"
                                        sh label: "Building the code: ${buildCmd}", script: buildCmd
                                    }

                                    withMavenPlus(mavenLocalRepo: BUILD_STAGE_M2_REPO, mavenSettingsConfig: '67aaee2b-ca74-4ae1-8eb9-c8f16eb5e534', publisherStrategy: 'EXPLICIT') {
                                        withCredentials([
                                                string(credentialsId: '5aa5789f-dd6a-48c2-a76c-10d8b16a4e53', variable: 'CODACY_API_TOKEN'),
                                                string(credentialsId: 'b3d644ac-5a8c-4a07-bf6e-6953a46ac33f', variable: 'CODACY_PROJECT_TOKEN_STRONGBOX')
                                        ]) {
                                            // we can inline the command, but this looks better in the pipeline view.
                                            sh label: "Running analysis",
                                               script: "mvn com.gavinmogan:codacy-maven-plugin:1.2.0:coverage -Pcodacy"
                                        }
                                    }
                                }
                            }
                        }
                    }
                    post {
                        success {
                            script {
                                container("maven") {
                                    dir(BUILD_STAGE_M2_REPO + "/org/carlspring/strongbox") {
                                        stash name: 'strongboxArtifacts', includes: '**/*', excludes: '**/strongbox-distribution-*'
                                    }
                                    dir("linux/strongbox-distribution/target") {
                                        stash name: 'strongboxDistribution', includes: 'strongbox-distribution-*.tar.gz'
                                    }
                                }
                            }
                        }
                        failure {
                            script {
                                container("maven") {
                                    archiveArtifacts '**/target/strongbox-vault/logs/**'
                                }
                            }
                        }
                        cleanup {
                            container('maven') {
                                script {
                                    workspace().clean()
                                }
                            }
                        }
                    }
                }
                stage('windows') {
                    when {
                        beforeAgent true
                        expression {
                            !RUN_ONLY_IT && !RUN_ONLY_SMOKE
                        }
                    }
                    agent {
                        node {
                            label 'windows'
                        }
                    }
                    options {
                        checkoutToSubdirectory 'windows'
                        throttle(categories: ['allow-concurrency-on-different-agents'])
                    }
                    tools {
                        maven 'maven-3.6'
                    }
                    steps {
                        script {
                            nodeInfo('mvn')

                            if (M2_CLEAN)
                            {
                                bat label: "Clearing .m2 cache before proceeding",
                                    script: "rm -rf /cygdrive/c/Users/Administrator/.m2/repository/*"
                            }

                            dir("windows") {
                                withMavenPlus(maven: 'maven-3.5',
                                              mavenSettingsConfig: '67aaee2b-ca74-4ae1-8eb9-c8f16eb5e534',
                                              mavenOpts: MVN_OPTS,
                                              options: [artifactsPublisher(disabled: true)]) {

                                    def buildCmd = "mvn ${MVN_ARGS}" + (SKIP_TESTS ? " -DskipTests" : "")
                                    bat label: "Building the code: ${buildCmd}", script: buildCmd
                                }
                            }
                        }
                    }
                    post {
                        failure {
                            archiveArtifacts '**/target/strongbox-vault/logs/**'
                        }
                        cleanup {
                            script {
                                cleanWs deleteDirs: true
                            }
                        }
                    }
                }
            }
        }
        stage('Confirm') {
            when {
                expression {
                    !SKIP_CONFIRM && !RUN_ONLY_SMOKE && !INTEGRATION_TESTS_BRANCH
                }
            }
            agent none
            steps {
                script {
                    try
                    {
                        // don't allow skipping for master branch.
                        if (BRANCH_NAME.equals('master'))
                        {
                            INTEGRATION_TESTS_BRANCH = "master"
                        }
                        else
                        {
                            // This notified only if the author of the change is a core member.
                            notifyCoreMember(AUTHOR)

                            timeout(time: 5, unit: "MINUTES") {
                                INTEGRATION_TESTS_BRANCH = input(
                                        message: 'Run integration tests?',
                                        parameters: [
                                                string(defaultValue: 'master', description: 'Run the integration tests using a specific branch/pr?', name: 'INTEGRATION_TESTS_BRANCH', trim: true)
                                        ],
                                        submitter: 'administrators,strongbox-core,strongbox-pro,strongbox-oss'
                                )
                            }
                        }
                    }
                    catch (FlowInterruptedException e)
                    {
                        println "Skipping integration tests"
                        INTEGRATION_TESTS_BRANCH = null
                    }
                }
            }
        }
        stage('Integration tests') {
            when {
                beforeAgent true
                expression {
                    INTEGRATION_TESTS_BRANCH && !RUN_ONLY_SMOKE && !SKIP_CONFIRM
                }
            }
            steps {
                script {
                    println "Branch: ${INTEGRATION_TESTS_BRANCH}"
                    // NB: Agents with multiple containers share the same /home/jenkins which causes problems!!!
                    parallel parallelSWITStages {
                        clone = true
                        branch = INTEGRATION_TESTS_BRANCH
                        unstash = true
                        env = [
                                'STRONGBOX_DEBUG'              : true,
                                'STRONGBOX_LOG_CONSOLE_ENABLED': true
                        ]
                        modules = [
                            choco: {
                                agent = 'alpine-jdk8-mvn3.6-mono5-nuget3.4-choco0.10'
                                tools = 'mvn mono choco'
                            },
                            gradle: {
                                agent = 'alpine-jdk8-mvn3.6-gradle5.6'
                                tools = 'mvn gradle'
                            },
                            maven: {},
                            npm: {
                                agent = 'alpine-jdk8-mvn3.6-node12'
                                tools = 'mvn npm node yarn'
                            },
                            nuget: {
                                agent = 'alpine-jdk8-mvn3.6-mono5-nuget3.4'
                                tools = 'mvn mono'
                            },
                            sbt: {
                                agent = 'alpine-jdk8-mvn3.6-sbt1.3'
                                tools = 'mvn'
                            },
                            pypi: {
                                agent = 'alpine-jdk8-mvn3.6-pip19.3'
                                tools = 'mvn python pip'
                            },
                            raw: {
                                container = 'maven'
                                tools = 'mvn'
                            }
                        ]
                    }
                }
            }
            post {
                success {
                    script {
                        INTEGRATION_TESTS_STAGE_STATUS = 'SUCCESS'
                    }
                }
                unsuccessful {
                    script {
                        INTEGRATION_TESTS_STAGE_STATUS = 'FAILURE'
                    }
                }
            }
        }
        stage('Smoke tests') {
            when {
                beforeAgent true
                expression {
                    !SKIP_CONFIRM && (RUN_ONLY_SMOKE || INTEGRATION_TESTS_BRANCH)
                }
            }
            // smoke test
            // 1. documentation opens
            // 2. can configure
            // 3. can login
            // 4. can resolve artifacts from maven, npm, nuget.
            parallel {
                stage("spring-boot:run") {
                    agent {
                        label 'alpine-jdk8-mvn3.6'
                    }
                    options {
                        timeout(time: 15, unit: 'MINUTES')
                        checkoutToSubdirectory 'spring-boot'
                    }
                    environment {
                        STRONGBOX_ORIENTDB_STUDIO_ENABLED = true
                        STRONGBOX_VAULT = "spring-boot/strongbox-web-core/target/strongbox-vault"
                    }
                    stages {
                        stage('Preparing') {
                            steps {
                                container('maven') {
                                    script {
                                        final def mvnLocalRepo = workspace().getM2LocalRepoPath("smoke-spring-boot")
                                        final def strongboxGroupPath = "${mvnLocalRepo}/org/carlspring/strongbox"

                                        sh label: "Creating .m2 path to unstash artifacts",
                                           script: "mkdir -p ${strongboxGroupPath}"

                                        dir(strongboxGroupPath) {
                                            unstash name: 'strongboxArtifacts'
                                        }

                                        dir("spring-boot/strongbox-web-core") {
                                            withMavenPlus(mavenLocalRepo: mvnLocalRepo,
                                                          mavenSettingsConfig: '67aaee2b-ca74-4ae1-8eb9-c8f16eb5e534',
                                                          mavenOpts: '-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn',
                                                          publisherStrategy: 'EXPLICIT') {

                                                // log file should be placed in the root.
                                                sh label: "Starting using spring-boot:run",
                                                   script: "nohup mvn spring-boot:run --log-file=smoke-spring-boot.log  &"

                                                waitForResponse(url: "localhost:48080", attempts: 30)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        stage('Testing') {
                            steps {
                                script {
                                    smokeIt('spring-boot')
                                }
                            }
                        }
                    }
                    post {
                        unsuccessful {
                            dir('') {
                                archiveArtifacts allowEmptyArchive: true, artifacts: "spring-boot/strongbox-web-core/spring-boot.log"
                                archiveArtifacts allowEmptyArchive: true, artifacts: "spring-boot/strongbox-web-core/curl-test-result.log"
                                archiveArtifacts allowEmptyArchive: true, artifacts: "spring-boot/strongbox-web-core/target/strongbox-vault/logs/**"
                            }
                        }
                        cleanup {
                            script {
                                workspace().clean()
                            }
                        }
                    }
                }
                stage("strongbox-distribution") {
                    agent {
                        label 'alpine-jdk8-mvn3.6'
                    }
                    options {
                        timeout(time: 15, unit: 'MINUTES')
                        skipDefaultCheckout true
                    }
                    environment {
                        STRONGBOX_ORIENTDB_STUDIO_ENABLED = true
                        STRONGBOX_VAULT = "$WORKSPACE/strongbox-distribution/strongbox-vault"
                    }
                    stages {
                        stage('Preparing') {
                            steps {
                                script {
                                    container('maven') {
                                        final def mvnLocalRepo = workspace().getM2LocalRepoPath("smoke-strongbox-distribution")
                                        final def strongboxGroupPath = "${mvnLocalRepo}/org/carlspring/strongbox"

                                        sh label: "Creating strongbox-distribution workspace",
                                           script: "mkdir strongbox-distribution"

                                        sh label: "Creating .m2 path to unstash artifacts",
                                           script: "mkdir -p ${strongboxGroupPath}"

                                        dir(strongboxGroupPath) {
                                            unstash name: 'strongboxArtifacts'
                                        }

                                        dir("strongbox-distribution") {
                                            unstash name: 'strongboxDistribution'

                                            sh label: "Unzipping distribution...",
                                               script: "tar xzf strongbox-distribution-*.tar.gz --strip-components=1"

                                            sh label: "Starting using strongbox-distribution",
                                               script: "cd strongbox-* && ./bin/strongbox start"

                                            waitForResponse(url: "localhost:48080", attempts: 30)
                                        }
                                    }
                                }
                            }
                        }
                        stage('Testing') {
                            steps {
                                script {
                                    smokeIt('strongbox-distribution')
                                }
                            }
                        }
                    }
                    post {
                        unsuccessful {
                            dir('') {
                                archiveArtifacts allowEmptyArchive: true, artifacts: "strongbox-distribution/curl-test-result.log"
                                archiveArtifacts allowEmptyArchive: true, artifacts: "strongbox-distribution/strongbox-vault/logs/**"
                            }
                        }
                        cleanup {
                            script {
                                workspace().clean()
                            }
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        SMOKE_STAGE_STATUS = 'SUCCESS'
                    }
                }
                unsuccessful {
                    script {
                        SMOKE_STAGE_STATUS = 'FAILURE'
                    }
                }
            }
        }
        stage('Deploy') {
            when {
                beforeAgent true
                branch 'master'
                expression {
                    DEPLOY && INTEGRATION_TESTS_BRANCH && !SKIP_TESTS && !RUN_ONLY_SMOKE && !RUN_ONLY_IT && !SKIP_CONFIRM
                }
            }
            agent {
                label 'alpine-jdk8-mvn3.6'
            }
            steps {
                container('maven') {
                    script {
                        nodeInfo('mvn')
                        withMavenPlus(
                                mavenLocalRepo: BUILD_STAGE_M2_REPO,
                                mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                                publisherStrategy: 'EXPLICIT',
                                options: [
                                        artifactsPublisher(disabled: true),
                                        pipelineGraphPublisher(includeReleaseVersions: true, skipDownstreamTriggers: true),
                                        mavenLinkerPublisher()
                                ]
                        ) {
                            def attempt = 0
                            retry(3) {
                                if (attempt > 0)
                                {
                                    sleep time: 10, unit: 'SECONDS'
                                }

                                attempt++

                                sh label: "Deploying...",
                                   script: "mvn deploy" +
                                           " -Pdeploy-release-artifact-to-github" +
                                           " -DskipTests" +
                                           " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        unsuccessful {
            script {
                if (NOTIFY_EMAIL && currentBuild.result != 'ABORTED')
                {
                    notifyUnstable(BRANCH_NAME.equals("master") ? NOTIFY_MASTER : NOTIFY_BRANCH)
                }
            }
        }
        fixed {
            script {
                if (NOTIFY_EMAIL)
                {
                    notifyFixed(BRANCH_NAME.equals("master") ? NOTIFY_MASTER : NOTIFY_BRANCH)
                }
            }
        }
        always {
            script {
                setGithubStatus(INTEGRATION_TESTS_GITHUB_CONTEXT, INTEGRATION_TESTS_STAGE_STATUS)
                setGithubStatus(SMOKE_GITHUB_CONTEXT, SMOKE_STAGE_STATUS)
            }
        }
    }
}

def smokeIt(checkoutDir)
{
    container('maven') {
        dir(checkoutDir) {
            configFileProvider([configFile(fileId: 'strongbox-curl-test.sh', targetLocation: "./strongbox-curl-tests.sh")]) {
                sh label: "Testing strongbox via curl",
                   script: "set -o pipefail; /bin/bash ./strongbox-curl-tests.sh | tee curl-test-result.log"
            }
        }
    }
}
