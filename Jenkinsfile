#!/usr/bin/env groovy
/**
 * Jenkinsfile for zanata-mt
 *
 * Master (documentation will be build on separate job):
 * checkout -> maven build and release -> process test coverage
 *  -> maven site
 *  -> docker image build -> deploy docker registry -> deploy to stage (openshift)
 *
 * PR:
 * checkout -> maven build -> process test coverage
 */

@Library('zanata-pipeline-library@master')

import org.zanata.jenkins.Notifier
import org.zanata.jenkins.PullRequests

import groovy.transform.Field

PullRequests.ensureJobDescription(env, manager, steps)

@Field
def notify
// initialiser must be run separately (bindings not available during compilation phase)
notify = new Notifier(env, steps)

/* Only keep the 10 most recent builds. */
def projectProperties = [
  [
    $class: 'BuildDiscarderProperty',
    strategy: [$class: 'LogRotator', numToKeepStr: '10']
  ],
]

properties(projectProperties)

timestamps {
  node {
    echo "running on node ${env.NODE_NAME}"
    ansicolor {
      try {
        stage('Checkout') {
          notify.started()
          checkout scm
          // Clean the workspace
          sh "git clean -fdx"
          sh "./mvnw --version"
        }

        if (env.BRANCH_NAME == 'master') {
          def POM=readMavenPom file: 'pom.xml'
          final String VERSION=POM.version.replace('-SNAPSHOT', '.' + env.BUILD_NUMBER)
          final String GIT_URL='git@github.com:zanata/zanata-mt.git'

          final String DOCKER_IMAGE = 'zanata-mt/server'
          final String DOCKERFILE_GIT_REPO = "$CEE_GITLAB_URL/zanata/zanata-dockerfiles.git"

          git credentialsId: 'zanata-jenkins', url: "$GIT_URL"

          stage ('Maven build and release') {
            sh """./mvnw -e clean verify \
                       release:prepare \
                       release:perform \
                       --batch-mode \
                       --update-snapshots \
                       -DreleaseVersion=$VERSION \
                       -DdevelopmentVersion=$POM.version \
                       -DpushChanges=false \
                       -DstaticAnalysis \
             """
            processTestCoverage()
            sh "git push origin $POM.artifactId-$VERSION"
          }
          stage('Deploy to STAGE') {
            def tasks = [
                    "DOCUMENTATION": { mavenSite() },
                    "DOCKER_BUILD_RELEASE": {
                      dockerBuildAndDeploy("$VERSION", "$DOCKERFILE_GIT_REPO",
                              "${DOCKER_IMAGE}");
                      deployToStage("${VERSION}", "${DOCKER_IMAGE}")
                    },
            ]
            parallel tasks
          }
          // Artifacts is in docker registry, reduce workspace size
          sh "git clean -fdx"

          deployToProduction("${DOCKER_IMAGE}")
        } else {
          stage('Build') {
            sh """./mvnw -e clean verify \
                       --batch-mode \
                       --update-snapshots \
                       -DstaticAnalysis \
             """
            processTestCoverage()
          }
          // Reduce workspace size
          sh "git clean -fdx"
        }
      } catch (e) {
        echo("Caught exception: " + e)
        notify.failed()
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }
}

// Run maven site to build documentation
void mavenSite() {
  sh "./mvnw -e site"
}

/**
 * Build docker image and deploy to docker registry
 *
 * @param version - image version
 * @param dockerGitRepo - git repo to for dockerfile
 * @param dockerImage - docker image name
 */
void dockerBuildAndDeploy(String version, String dockerGitRepo, String dockerImage) {
  def workspace = "server/target/docker_workspace"

  echo "Getting dockerfile from $dockerGitRepo:master and copy to $workspace/zanata-mt/deployments/"
  sh "git clone --depth 1 --single-branch $dockerGitRepo -b master $workspace"
  sh "mkdir -p $workspace/zanata-mt/deployments"
  sh "cp server/target/deployments/ROOT.war $workspace/zanata-mt/deployments/"

  echo "Building docker zanata-mt $version..."
  sh "docker build -f $workspace/zanata-mt/Dockerfile-OPENSHIFT -t $dockerImage:$version $workspace/zanata-mt"

  sh "echo Creating tag for $version..."
  sh "docker tag $dockerImage:$version $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:$version"
  sh "docker tag $dockerImage:$version $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:latest"

  echo "Docker login.."
  withCredentials([string(credentialsId: 'DOCKER-REGISTRY-TOKEN', variable: 'DOCKER_REGISTRY_TOKEN')]) {
    sh "docker login -p $DOCKER_REGISTRY_TOKEN -u unused $RH_ENGINEERING_DOCKER_REGISTRY_URL"
  }

  echo "Pushing to docker registry..."
  sh "docker push $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:$version"
  sh "docker push $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:latest"

  echo "Docker logout.."
  sh "docker logout $RH_ENGINEERING_DOCKER_REGISTRY_URL"
}

/**
 * Deploy docker image to openshift staging
 * @param version
 * @param dockerImage
 */
void deployToStage(String version, String dockerImage) {
  echo "Login to OPENSHIFT with token"

  withCredentials([string(credentialsId: 'Jenkins-Token-open-paas-STAGE', variable: 'OPEN_PAAS_TOKEN_STAGE')]) {
    sh "oc login $OPEN_PAAS_URL --token $OPEN_PAAS_TOKEN_STAGE"
  }

  sh "oc project zanata-mt-stage"

  echo "Update 'latest' tag to $version"
  sh "oc tag $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:$version server:latest"
  sh "oc logout"
}

/**
 * Pending permission to deploy to production. 10 days before timeout
 *
 * @param dockerImage
 */
void deployToProduction(String dockerImage) {
  def deployToProd = false
  def isTimeout = false
  try {
    timeout(time: 10, unit: 'DAYS') {
      deployToProd = input(message: 'Deploy docker image to production?',
              parameters: [[$class: 'BooleanParameterDefinition', defaultValue: false,
                            description: '', name: 'Deploy to production?']])
    }
  } catch (e) {
    deployToProd = false
    def user = e.getCauses()[0].getUser()
    // SYSTEM means timeout.
    if('SYSTEM' == user.toString()) {
      isTimeout = true
    }
  }
  if (deployToProd) {
    stage ('Deploy to PRODUCTION') {
      echo "Login to OPENSHIFT with token"
      withCredentials([string(credentialsId: 'Jenkins-Token-open-paas-PROD', variable: 'OPEN_PAAS_TOKEN_PROD')]) {
        sh "oc login $OPEN_PAAS_URL --token $OPEN_PAAS_TOKEN_PROD"
      }

      echo "Switch to use zanata-mt project"
      sh "oc project zanata-mt-prod"

      echo "Update 'latest' tag to $VERSION"
      sh "oc tag $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:latest server:latest"

      echo "Logout from open-paas"
      sh "oc logout"
    }
  } else {
    if (isTimeout) {
      echo "Timeout for PROD deployment."
    } else {
      echo "PROD deployment aborted."
    }
  }
}


/**
 * Process test coverage after build
 */
void processTestCoverage() {
  def surefireTestReports = 'target/surefire-reports/TEST-*.xml'
  junit testResults: "**/${surefireTestReports}"

  // send test coverage data to codecov.io
  try {
    withCredentials(
            [[$class: 'StringBinding',
              credentialsId: 'codecov_zanata-mt',
              variable: 'CODECOV_TOKEN']]) {
      // NB the codecov script uses CODECOV_TOKEN
      sh "curl -s https://codecov.io/bash | bash -s - -K"
    }
  } catch (InterruptedException e) {
    throw e
  } catch (hudson.AbortException e) {
    throw e
  } catch (e) {
    echo "[WARNING] Ignoring codecov error: $e"
  }
  // notify if compile+unit test successful
  notify.testResults("UNIT", currentBuild.result)
  archive "**/target/*.jar, **/target/*.war"

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
}

