#!/usr/bin/env groovy
import groovy.transform.Field
@Library('github.com/zanata/zanata-pipeline-library@v0.3.1')
import org.zanata.jenkins.Notifier

/**
 * Jenkinsfile for mt
 *
 * Environment variables:
 * MT_OPENSHIFT_DOCKERFILE: URL for Openshift Dockerfile
 *
 * MT_DOCKER_REGISTRY_URL - Managed Platform docker registry url to upload image
 * MT_DOCKER_REGISTRY_URL_OPEN - OPEN Platform docker registry url to upload image
 * MT_OPENSHIFT_URL - Managed Platform url
 * MT_OPENSHIFT_URL_OPEN - OPEN Platform url
 * MT_GIT_URL - git url
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

import org.zanata.jenkins.PullRequests
import org.zanata.jenkins.ScmGit

import static org.zanata.jenkins.Reporting.codecov

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
def version

String getLabel() {
  def labelParam = null
  try {
    labelParam = params.LABEL
  } catch (e1) {
    // workaround for https://issues.jenkins-ci.org/browse/JENKINS-38813
    echo '[WARNING] unable to access `params`'
    echo getStackTrace(e1)
    try {
      labelParam = LABEL
    } catch (e2) {
      echo '[WARNING] unable to access `LABEL`'
      echo getStackTrace(e2)
    }
  }

  if (labelParam == null) {
    echo "LABEL param is null; using default value."
  }
  def result = labelParam ?: defaultNodeLabel
  echo "Using build node label: $result"
  return result
}

timestamps {
  node {
    echo "Setup running on node ${env.NODE_NAME}"

    pipelineLibraryScmGit = new ScmGit(env, steps, 'https://github.com/zanata/zanata-pipeline-library')
    pipelineLibraryScmGit.init(PIPELINE_LIBRARY_BRANCH)
    mainScmGit = new ScmGit(env, steps, PROJ_URL)
    mainScmGit.init(env.BRANCH_NAME)
    notify = new Notifier(env, steps, currentBuild,
      pipelineLibraryScmGit, mainScmGit, (env.GITHUB_COMMIT_CONTEXT) ?: 'Jenkinsfile',
    )
    defaultNodeLabel = env.DEFAULT_NODE ?: 'master || !master'
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
    properties(projectProperties)
  }

  node(getLabel()) {
    echo "running on node ${env.NODE_NAME}"
    // See if we are affected by JENKINS-28921 (Can't access Jenkins Global Properties from Pipeline DSL)
    currentBuild.displayName = currentBuild.displayName + " {${env.NODE_NAME}}"
    // Also note that envinject plugin is
    assert env.MT_DOCKER_REGISTRY_URL
    assert env.MT_DOCKER_REGISTRY_URL_OPEN
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
        if (env.BRANCH_NAME == 'master' && params.isReleasing) {
          buildAndDeploy()
        } else {
          buildOnly()
          deployToOpenPaaS()
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

/**
 * Deploy PR build to Open PaaS project magpie for testing.
 */
private void deployToOpenPaaS() {

  if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {

    // create a new project and deploy
    stage('Deploy PR to OpenPaaS') {
      lock(resource: 'MT-DEPLOY-PR-TO-OPENPAAS', inversePrecedence: true) {
        milestone 800
        // because dockerBuildAndDeploy_OPEN will call unstash
        stash name: 'generated-files', includes: '**/target/**'

        def POM = readMavenPom file: 'pom.xml'
        branchName = env.BRANCH_NAME
        // set the version to use in this pipeline job
        version = POM.version.replace('-SNAPSHOT', '.' + branchName)
        echo "Update project version to $version"
        sh "./mvnw versions:set -DnewVersion=$version --non-recursive --batch-mode"

        final String DOCKER_IMAGE_OPEN = 'zanata-mt/server'

        dockerBuildAndPushToOpenPaaS("$DOCKER_IMAGE_OPEN")

        def ocClient = getOpenshiftClient();
        echo "Login to OPENSHIFT with token"
        withCredentials(
                [string(credentialsId: 'Jenkins-Token-open-paas-Magpie',
                        variable: 'MT_OPENSHIFT_TOKEN_MAGPIE')]) {
          sh "$ocClient login $MT_OPENSHIFT_URL_OPEN --token $MT_OPENSHIFT_TOKEN_MAGPIE --insecure-skip-tls-verify"
        }

        // TODO if we can create project on the fly then we should do it here
        echo "Deploy to project magpie"
        String project = "magpie"
        String app = "mt-server-${branchName}".toLowerCase()
        String image = "${env.MT_DOCKER_REGISTRY_URL_OPEN}/$DOCKER_IMAGE_OPEN:$version"
        def params = "--param=project=$project --param=app=$app --param=image=$image --param=azure=${env.MT_AZURE} " // --param=google='${env.MT_GOOGLE}'

        sh "$ocClient process -f ${env.WORKSPACE}/openshift/mt-template.yaml $params|$ocClient apply -f -"

        echo "Update image stream 'latest' tag to $version"
        sh "$ocClient tag $image $app:latest"
        sh "$ocClient logout"
      }
    }
  } else {
    currentBuild.result = 'FAILURE'
    error('Build failure.')
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
     *
     * Current state:
     *  OPEN Platform - PR only
     *  Managed Platform - DEV, QA, STAGE, PROD
     *
     */
    final String DOCKER_IMAGE = 'machine-translations/mt-server'

    stage('Deploy to Managed PaaS; Maven site') {
      lock(resource: 'MT-DEPLOY-TO-DEV', inversePrecedence: true) {
        milestone 600
        def tasks = [
                "DOCUMENTATION"       : { mavenSite() },
                "DOCKER_BUILD_RELEASE": {
                  dockerBuildAndPushToManagedPaaS("$DOCKER_IMAGE")
                }
        ]
        parallel tasks
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
      sh "./mvnw package site -DskipTests -Dfindbugs.skip -Dgithub.global.oauth2Token=$GITHUB_OAUTH2_TOKEN --batch-mode -pl common -am"
    }
  }
}

/**
 * Build docker image and push to docker registry for Managed Platform
 */
void dockerBuildAndPushToManagedPaaS(String dockerImage) {
  final String DOCKER_WORKSPACE = 'server/target/docker_workspace'
  node(defaultNodeLabel) {
    echo "running on node ${env.NODE_NAME}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"
    unstash 'generated-files'

    buildAndTagDockerImage(DOCKER_WORKSPACE, dockerImage, "${env.MT_DOCKER_REGISTRY_URL}")

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
 * Build docker image and push to Open Platform docker registry
 *
 * @param dockerImage - docker image name
 */
void dockerBuildAndPushToOpenPaaS(String dockerImage) {
  final String DOCKER_WORKSPACE = 'server/target/docker_workspace_open'

  echo "running on node ${env.NODE_NAME}"
  checkout scm
  // Clean the workspace
  sh "git clean -fdx"
  unstash 'generated-files'

  buildAndTagDockerImage(DOCKER_WORKSPACE, dockerImage, "${env.MT_DOCKER_REGISTRY_URL_OPEN}")

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
 * Process test results/coverage after build. Marks build as unstable if any tests fail.
 */
void processTestResults() {
  // Marks build as unstable if any tests fail.
  junit testResults: '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'

  // notify if compile+unit test successful
  notify.testResults("UNIT", currentBuild.result)
  archive "**/target/*.war"

  // parse Jacoco test coverage
  step([$class: 'JacocoPublisher'])

  if (env.BRANCH_NAME == 'master') {
    step([$class: 'MasterCoverageAction'])
  } else if (env.BRANCH_NAME.startsWith('PR-')) {
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

void buildAndTagDockerImage(workspace, dockerImage, dockerRegistryUrl) {
  echo "Copy certs and ROOT.war to $workspace/"

  sh "mkdir -p $workspace/docker/certs"
  sh "mkdir -p $workspace/target/deployments/"
  sh "cp server/target/deployments/ROOT.war $workspace/target/deployments/"
  sh "cp server/Dockerfile $workspace/"

  /**
   * Cert is downloaded in host as workaround for connection issue in container
   * TODO: move download cert task to Dockerfile when docker host is stable
   */
  def certName = 'rds-combined-ca-bundle.pem'
  sh "curl -o $workspace/docker/certs/$certName http://s3.amazonaws.com/rds-downloads/$certName"
  sh "curl -o $workspace/docker/certs/RH-IT-Root-CA.crt $env.SSL_CA_CERT"
  sh "curl -o $workspace/docker/certs/newca.crt $env.OLD_SSL_CA_CERT"
  sh "curl -o $workspace/docker/certs/RH-IT-pki-ca-chain.crt $env.SSL_INTERMEDIATE_CA_CERT"

  echo "Building docker MT $version..."
  sh "docker build --pull --no-cache -f $workspace/Dockerfile -t $dockerImage:$version $workspace"

  sh "echo Creating tag for $version..."
  sh "docker tag $dockerImage:$version $dockerRegistryUrl/$dockerImage:$version"
  sh "docker tag $dockerImage:$version $dockerRegistryUrl/$dockerImage:latest"
}
