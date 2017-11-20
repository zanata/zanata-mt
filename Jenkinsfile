#!/usr/bin/env groovy
/**
 * Jenkinsfile for zanata-mt
 *
 * Environment variables:
 * MT_OPENSHIFT_DOCKERFILE: URL for Openshift Dockerfile
 *
 * MT_DOCKER_REGISTRY_URL - Managed Platform docker registry url to upload image
 * MT_DOCKER_REGISTRY_URL_OPEN - OPEN Platform docker registry url to upload image
 * MT_OPENSHIFT_URL - Managed Platform url
 * MT_OPENSHIFT_URL_OPEN - OPEN Platform url
 * MT_GIT_URL - git url
 * MT_DEV_PROJECT_NAME - Managed Platform project name for DEV
 * MT_STAGE_PROJECT_NAME_OPEN - OPEN Platform project name for stage
 * MT_PROD_PROJECT_NAME_OPEN - OPEN Platform project name for production
 *
 *
 * Master (documentation will be build on separate job):
 * checkout -> maven build and release -> process test coverage
 *  -> maven site
 *  -> docker image build -> deploy docker registry -> deploy to dev (openshift)
 *
 * PR:
 * checkout -> maven build -> process test coverage
 */

@Field
public static final String PROJ_URL = 'https://github.com/zanata/zanata-mt'

@Field
public static final String PIPELINE_LIBRARY_BRANCH = 'v0.3.1'

@Library('github.com/zanata/zanata-pipeline-library@v0.3.1')
import org.zanata.jenkins.Notifier
import org.zanata.jenkins.PullRequests
import org.zanata.jenkins.ScmGit
import static org.zanata.jenkins.Reporting.codecov
import static org.zanata.jenkins.StackTraces.getStackTrace

import groovy.transform.Field

milestone 0
PullRequests.ensureJobDescription(env, manager, steps)

@Field
def pipelineLibraryScmGit

@Field
def mainScmGit

@Field
def notify
// initialiser must be run separately (bindings not available during compilation phase)

@Field
def defaultNodeLabel

@Field
def branchName

@Field
def version

node {
  echo "running on node ${env.NODE_NAME}"
  pipelineLibraryScmGit = new ScmGit(env, steps, 'https://github.com/zanata/zanata-pipeline-library')
  pipelineLibraryScmGit.init(PIPELINE_LIBRARY_BRANCH)
  mainScmGit = new ScmGit(env, steps, PROJ_URL)
  mainScmGit.init(env.BRANCH_NAME)
  notify = new Notifier(env, steps, currentBuild,
      pipelineLibraryScmGit, mainScmGit, 'Jenkinsfile',
  )
  defaultNodeLabel = env.DEFAULT_NODE ?: 'master || !master'
  // eg github-zanata-org/zanata-platform/update-Jenkinsfile
  jobName = env.JOB_NAME
  def projectProperties = [
    [$class: 'BuildDiscarderProperty',
      strategy:
        [$class: 'LogRotator',
          numToKeepStr: '10',        // keep records for at most X builds
        ]
    ],
    [$class: 'GithubProjectProperty',
      projectUrlStr: PROJ_URL
    ],
    [$class: 'ParametersDefinitionProperty',
      parameterDefinitions: [
        [$class: 'LabelParameterDefinition',
          // Specify the default node in Jenkins env var DEFAULT_NODE
          // (eg kvm), or leave blank to build on any node.
          defaultValue: defaultNodeLabel,
          description: 'Jenkins node label to use for build',
          name: 'LABEL'
        ],
        [$class: 'BooleanParameterDefinition',
          defaultValue: true,
          description: 'Make release',
          name: 'isReleasing'
        ],
      ]
    ]
  ]
  branchName = env.BRANCH_NAME
  properties(projectProperties)
}

timestamps {
  node (defaultNodeLabel) {
    echo "running on node ${env.NODE_NAME}"
    // See if we are affected by JENKINS-28921 (Can't access Jenkins Global Properties from Pipeline DSL)
    // Also note that envinject plugin is
    assert env.MT_DOCKER_REGISTRY_URL
    assert env.MT_DOCKER_REGISTRY_URL_OPEN
    pipelineLibraryScmGit = new ScmGit(env, steps, 'https://github.com/zanata/zanata-pipeline-library')
    pipelineLibraryScmGit.init(PIPELINE_LIBRARY_BRANCH)
    mainScmGit = new ScmGit(env, steps, PROJ_URL)
    mainScmGit.init(env.BRANCH_NAME)
    notify = new Notifier(env, steps, currentBuild,
        pipelineLibraryScmGit, mainScmGit, 'Jenkinsfile',
    )
    ansicolor {
      try {
        stage('Checkout') {
          notify.started()
          checkout scm
          // Clean the workspace
          sh "git clean -fdx"
          sh "./mvnw --version"
        }

        notify.startBuilding()
        if (branchName == 'master' && params.isReleasing) {
          buildAndDeploy()
        } else {
          buildOnly()
        }
        // Artifacts are in docker registry, so reduce workspace size
        sh "git clean -fdx"
        notify.finish()
      } catch (e) {
        echo("Caught exception: " + e)
        notify.failed()
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }
}

private void buildAndDeploy() {
  def POM = readMavenPom file: 'pom.xml'
  version = POM.version.replace('-SNAPSHOT', '.' + env.BUILD_NUMBER)
  echo "Update project version to $version"
  sh "./mvnw versions:set -DnewVersion=$version --non-recursive --batch-mode"

  buildOnly()
  if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
    stage('Stash') {
      stash name: 'generated-files', includes: '**/target/**'
    }
    stage('Tag and upload') {
      String tag = "MT-$version"
      sh "shopt -s globstar && git commit **/pom.xml pom.xml -m 'MT release: $tag' && git tag $tag"

      withCredentials(
              [[$class          : 'UsernamePasswordMultiBinding', credentialsId: 'zanata-jenkins',
                usernameVariable: 'GIT_USERNAME', passwordVariable: 'GITHUB_OAUTH2_TOKEN']]) {
        sh "git push https://$GIT_USERNAME:$GITHUB_OAUTH2_TOKEN@$MT_GIT_URL $tag"

        String apiJson = "{\"tag_name\": \"$tag\",\"target_commitish\": \"master\",\"name\": \"$tag\",\"body\": \"Release of version $tag\",\"draft\": false,\"prerelease\": false}"
        echo "Create github release: $apiJson"

        def response = sh script: "curl --data '$apiJson' https://api.github.com/repos/zanata/zanata-mt/releases?access_token=$GITHUB_OAUTH2_TOKEN",
                returnStdout: true
        def releaseDetails = readJSON text: response
        def id = releaseDetails['id']

        echo "Upload artifacts to release: $tag: $id"
        sh "curl --data-binary @'./server/target/deployments/ROOT.war' -H 'Authorization: token $GITHUB_OAUTH2_TOKEN' -H 'Content-Type: application/octet-stream' https://uploads.github.com/repos/zanata/zanata-mt/releases/$id/assets?name=server.war"

      }
    }
    /**
     * TODO: Migrate all to Managed Platform only
     *
     * Current state:
     *  OPEN Platform - STAGE and PROD
     *  Managed Platform - DEV
     *
     * DEV deployment:
     *  Managed Platform - DEV
     *
     * STAGE deployment:
     *  OPEN Platform - STAGE
     *
     * PROD deployment:
     *  OPEN Platform - PROD
     */

    final String DOCKER_IMAGE_OPEN = 'zanata-mt/server'
    final String DOCKER_IMAGE = 'machine-translations/mt-server'

    stage('Deploy to DEV; Maven site') {
      lock(resource: 'MT-DEPLOY-TO-DEV', inversePrecedence: true) {
        milestone 600
        def tasks = [
                "DOCUMENTATION"       : { mavenSite() },
                "DOCKER_BUILD_RELEASE": {
                  dockerBuildAndDeploy_OPEN("$DOCKER_IMAGE_OPEN")
                  dockerBuildAndDeploy("$DOCKER_IMAGE")
                  deployToDEV("$DOCKER_IMAGE")
                }
        ]
        parallel tasks
      }
    }
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
      deployToStage_OPEN("$DOCKER_IMAGE_OPEN")
      // TODO: deploy to managed platform stage

      if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
        deployToProduction_OPEN("$DOCKER_IMAGE_OPEN")
        // TODO: deploy to managed platform production
      }
    }
  } else {
    currentBuild.result = 'FAILURE'
    error('Build failure.')
  }
}

// Marks build as unstable if any tests fail.
private void buildOnly() {
  stage('Build') {
    // install docker for integration test
//    tool name: 'docker-1.13.1', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'
    echo "Run maven build"
    sh """./mvnw -e clean verify \
                       --batch-mode \
                       --update-snapshots \
                       -DstaticAnalysis \
                       -Dmaven.test.failure.ignore \
                       -Djavax.net.debug=all \
             """
    processTestResults()
  }
}

// Run maven site to build documentation
void mavenSite() {
  node (defaultNodeLabel) {
    echo "running on node ${env.NODE_NAME}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"

    unstash 'generated-files'
    withCredentials(
            [[$class          : 'UsernamePasswordMultiBinding', credentialsId: 'zanata-jenkins',
              usernameVariable: 'GIT_USERNAME', passwordVariable: 'GITHUB_OAUTH2_TOKEN']]) {
      sh "./mvnw -e site -DskipTests -Dfindbugs.skip -Dgithub.global.oauth2Token=$GITHUB_OAUTH2_TOKEN --batch-mode -Djavax.net.debug=all"
    }
  }
}

/**
 * Build docker image and deploy to docker registry for Managed Platform
 * To replace dockerBuildAndDeploy_OPEN()
 */
void dockerBuildAndDeploy(String dockerImage) {
  final String DOCKER_WORKSPACE = 'server/target/docker_workspace'
  node(defaultNodeLabel) {
    echo "running on node ${env.NODE_NAME}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"
    unstash 'generated-files'

    echo "Copy Dockerfile, ROOT.war and zanata-mt-config.cli to $DOCKER_WORKSPACE/"
    sh "mkdir -p $DOCKER_WORKSPACE"
    sh "cp server/target/deployments/ROOT.war $DOCKER_WORKSPACE/"
    sh "cp server/docker/Dockerfile-OPENSHIFT $DOCKER_WORKSPACE/"

    /**
     * Driver and Cert is downloaded in host as workaround for connection issue in container
     * TODO: move download task to Dockerfile when docker host is stable
     */
    sh "curl -L https://repo1.maven.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar > $DOCKER_WORKSPACE/postgresql-connector.jar"
    def certName = 'rds-combined-ca-bundle.pem'
    sh "curl -o $DOCKER_WORKSPACE/$certName http://s3.amazonaws.com/rds-downloads/$certName"

    echo "Building docker MT $version..."
    sh "docker build --pull -f $DOCKER_WORKSPACE/Dockerfile-OPENSHIFT -t $dockerImage:$version $DOCKER_WORKSPACE"

    sh "echo Creating tag for $version..."
    sh "docker tag $dockerImage:$version ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:$version"
    sh "docker tag $dockerImage:$version ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:latest"

    echo "Docker login.."
    withCredentials([string(credentialsId: 'MT-DOCKER-REGISTRY-TOKEN',
            variable: 'MT_REGISTRY_TOKEN')]) {
      sh "docker login -p $MT_REGISTRY_TOKEN -e unused -u unused ${env.MT_DOCKER_REGISTRY_URL}"

      echo "Pushing to docker registry..."
      sh "docker push ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:$version"
      sh "docker push ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:latest"

      echo "Docker logout.."
      sh "docker logout ${env.MT_DOCKER_REGISTRY_URL}"

      echo "Remove local docker image $dockerImage:$version"
      sh "docker rmi $dockerImage:$version; docker rmi ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:$version; docker rmi ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:latest"
    }
  }
}

/**
 * Build docker image and deploy to docker registry for OPEN Platform
 *
 * @param dockerImage - docker image name
 */
void dockerBuildAndDeploy_OPEN(String dockerImage) {
  final String DOCKER_WORKSPACE = 'server/target/docker_workspace_open'

  node(defaultNodeLabel) {
    echo "running on node ${env.NODE_NAME}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"
    unstash 'generated-files'

    echo "Copy Dockerfile-OPENSHIFT and ROOT.war to $DOCKER_WORKSPACE/"
    sh "mkdir -p $DOCKER_WORKSPACE"
    sh "cp server/target/deployments/ROOT.war $DOCKER_WORKSPACE/"
    sh "cp server/docker/Dockerfile-OPENSHIFT $DOCKER_WORKSPACE/"

    /**
     * Cert is downloaded in host as workaround for connection issue in container
     * TODO: move download cert task to Dockerfile when docker host is stable
     */
    def certName = 'rds-combined-ca-bundle.pem'
    sh "curl -o $DOCKER_WORKSPACE/$certName http://s3.amazonaws.com/rds-downloads/$certName"

    echo "Building docker MT $version..."
    sh "docker build --pull -f $DOCKER_WORKSPACE/Dockerfile-OPENSHIFT -t $dockerImage:$version $DOCKER_WORKSPACE"

    sh "echo Creating tag for $version..."
    sh "docker tag $dockerImage:$version ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:$version"
    sh "docker tag $dockerImage:$version ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:latest"

    echo "Docker login.."
    withCredentials([string(credentialsId: 'DOCKER-REGISTRY-TOKEN',
            variable: 'MT_REGISTRY_TOKEN')]) {
      sh "docker login -p $MT_REGISTRY_TOKEN -u unused ${env.MT_DOCKER_REGISTRY_URL_OPEN}"
    }

    echo "Pushing to docker registry..."
    sh "docker push ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:$version"
    sh "docker push ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:latest"

    echo "Docker logout.."
    sh "docker logout ${env.MT_DOCKER_REGISTRY_URL_OPEN}"

    echo "Remove local docker image $dockerImage:$version"
    sh "docker rmi $dockerImage:$version; docker rmi ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:$version; docker rmi ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:latest"
  }
}

/**
 * TODO: replace docker in pipeline
 * docker.image('openshift/origin:v3.6.0-alpha.2').inside {
 * }
 */
String getOpenshiftClient() {
    def name = "openshift-origin-client-tools-v3.6.0-c4dd4cf-linux-64bit"
    def fileName = name + ".tar.gz"
    def downloadDest = "/tmp/" + fileName

    if (!fileExists("/tmp/$name")) {
      // Download client if there's not downloaded yet
      if (!fileExists("$downloadDest")) {
        sh "wget https://github.com/openshift/origin/releases/download/v3.6.0/$fileName -O $downloadDest"
      }
      sh "tar -xvzf $downloadDest -C /tmp"
    }
    return "/tmp/$name/oc"
}

/**
 * Deploy docker image to OPEN Platform Stage
 * @param dockerImage
 */
void deployToStage_OPEN(String dockerImage) {
  stage('Deploy to STAGE') {
    lock(resource: 'MT-DEPLOY-TO-STAGE', inversePrecedence: true) {
      milestone 700
      node(defaultNodeLabel) {
        def ocClient = getOpenshiftClient();

        echo "Login to OPENSHIFT with token"

        withCredentials([string(credentialsId: 'Jenkins-Token-open-paas-STAGE',
                variable: 'MT_OPENSHIFT_TOKEN_STAGE')]) {
          sh "$ocClient login $MT_OPENSHIFT_URL_OPEN --token $MT_OPENSHIFT_TOKEN_STAGE --insecure-skip-tls-verify --namespace=$MT_STAGE_PROJECT_NAME_OPEN"

          echo "Update 'latest' tag to $version"
          sh "$ocClient tag ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:$version server:latest"
          sh "$ocClient logout"
        }
      }
    }
  }
}

/**
 * Deploy docker image to Managed Platform DEV
 * @param dockerImage
 */
void deployToDEV(String dockerImage) {
  if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
    node(defaultNodeLabel) {
      def ocClient = getOpenshiftClient();

      echo "Login to OPENSHIFT with token"

      withCredentials(
        [[$class          : 'UsernamePasswordMultiBinding', credentialsId: 'Jenkins-Token-managed-paas-DEV',
          usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        sh "$ocClient login $MT_OPENSHIFT_URL --username=$USERNAME --password=$PASSWORD --insecure-skip-tls-verify --namespace=$MT_DEV_PROJECT_NAME"

        echo "Update 'latest' tag to $version"
        sh "$ocClient tag ${env.MT_DOCKER_REGISTRY_URL}/$dockerImage:$version mt-server:latest"
        sh "$ocClient logout"
      }
    }
  }
}

/**
 * Deploy to OPEN Platform PROD
 * Pending permission to deploy to production. 14 days before timeout
 *
 * @param dockerImage
 */
void deployToProduction_OPEN(String dockerImage) {
  stage('Deploy to PRODUCTION') {
    lock(resource: 'MT-DEPLOY-TO-PROD', inversePrecedence: true) {
      milestone 800
      def deployToProd = false
      timeout(time: 14, unit: 'DAYS') {
        deployToProd = input(message: 'Deploy docker image to production?',
                parameters: [[$class     : 'BooleanParameterDefinition', defaultValue: false,
                              description: '', name: 'Deploy to production?']])
      }
      milestone 900
      if (deployToProd) {
        node(defaultNodeLabel) {
          def ocClient = getOpenshiftClient();

          echo "Login to OPENSHIFT with token"
          withCredentials(
                  [string(credentialsId: 'Jenkins-Token-open-paas-PROD',
                          variable: 'MT_OPENSHIFT_TOKEN_PROD')]) {
            sh "$ocClient login $MT_OPENSHIFT_URL_OPEN --token $MT_OPENSHIFT_TOKEN_PROD --insecure-skip-tls-verify --namespace=$MT_PROD_PROJECT_NAME_OPEN"
          }

          echo "Update 'latest' tag to $version"
          sh "$ocClient tag ${env.MT_DOCKER_REGISTRY_URL_OPEN}/$dockerImage:$version server:latest"
          sh "$ocClient logout"
        }
      }
    }
  }
}

/**
 * Process test results/coverage after build. Marks build as unstable if any tests fail.
 */
void processTestResults() {
  def surefireTestReports = 'target/surefire-reports/TEST-*.xml'
  // Marks build as unstable if any tests fail.
  junit testResults: "**/${surefireTestReports}"

  // notify if compile+unit test successful
  notify.testResults("UNIT", currentBuild.result)
  archive "**/target/*.war"

  // parse Jacoco test coverage
  step([$class: 'JacocoPublisher'])

  if (branchName == 'master') {
    step([$class: 'MasterCoverageAction'])
  } else if (branchName.startsWith('PR-')) {
    step([$class: 'CompareCoverageAction'])
  }

  // ref: https://philphilphil.wordpress.com/2016/12/28/using-static-code-analysis-tools-with-jenkins-pipeline-jobs/
  step([$class: 'CheckStylePublisher',
        pattern: '**/target/checkstyle-result.xml',
        unstableTotalAll: '0'])

  step([$class: 'FindBugsPublisher',
        pattern: '**/findbugsXml.xml',
        unstableTotalAll: '0'])

  step([$class: 'WarningsPublisher',
        consoleParsers: [
                [parserName: 'Java Compiler (javac)'],
        ],
        unstableTotalAll: '0'
  ])

  // this should appear after all other static analysis steps
  step([$class: 'AnalysisPublisher'])

  // send test coverage data to codecov.io
  codecov(env, steps, mainScmGit)
}
