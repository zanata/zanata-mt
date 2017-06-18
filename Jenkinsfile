#!/usr/bin/env groovy
/**
 * Jenkinsfile for zanata-mt
 *
 * Environment variables:
 * MT_DOCKER_REGISTRY_URL - docker registry url to upload image
 * MT_OPENSHIFT_URL - openshift url for stage and prod
 * MT_GIT_SSH_URL - ssh git url
 * MT_STAGE_PROJECT_NAME - Openshift project name for stage
 * MT_PROD_PROJECT_NAME - Openshift project name for production
 *
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

@Field
def defaultNodeLabel

@Field
def branchName

@Field
def version

node {
  echo "running on node ${env.NODE_NAME}"
  /**
   * Cannot run on Openstack yet until it is setup same as KVM slaves
   * defaultNodeLabel = env.DEFAULT_NODE ?: 'master || !master'
   */
  defaultNodeLabel = env.DEFAULT_NODE ?: 'master||kvm'
  branchName = env.BRANCH_NAME
}

/* Only keep the 10 most recent builds. */
def projectProperties = [
  [
    $class: 'BuildDiscarderProperty',
    strategy: [$class: 'LogRotator', numToKeepStr: '10']
  ],
]

properties(projectProperties)

timestamps {
  node (defaultNodeLabel) {
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

        // TODO: revert to == master, this is for testing
        if (branchName != 'master') {
          def POM = readMavenPom file: 'pom.xml'
          version = POM.version.replace('-SNAPSHOT', '.' + env.BUILD_NUMBER)

          git credentialsId: 'zanata-jenkins', url: "$MT_GIT_SSH_URL"

          stage('Maven build and release') {
            sh """./mvnw -e clean verify \
                       release:prepare \
                       release:perform \
                       --batch-mode \
                       --update-snapshots \
                       -DreleaseVersion=$version \
                       -DdevelopmentVersion=$POM.version \
                       -DpushChanges=false \
                       -DstaticAnalysis \
             """
            processTestCoverage()
            sh "git push origin $POM.artifactId-$version"
          }
          stage('Stash') {
            stash name: 'generated-files',
                    includes: '**/target/**'
          }
        } else {
          stage('Build') {
            sh """./mvnw -e clean verify \
                       --batch-mode \
                       --update-snapshots \
                       -DstaticAnalysis \
             """
            processTestCoverage()
          }
        }
        // Artifacts is in docker registry, reduce workspace size
        sh "git clean -fdx"
      } catch (e) {
        echo("Caught exception: " + e)
        notify.failed()
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }

  // TODO: revert to == master, this is for testing
  if (branchName != 'master') {
    final String DOCKER_IMAGE = 'zanata-mt/server'

    stage('Deploy to STAGE') {
      def tasks = [
        "DOCUMENTATION": { mavenSite() },
        "DOCKER_BUILD_RELEASE": {
          dockerBuildAndDeploy("$DOCKER_IMAGE")
          deployToStage("$DOCKER_IMAGE")
        }
      ]
      parallel tasks
    }
    deployToProduction("$DOCKER_IMAGE")
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
              usernameVariable: 'USERNAME', passwordVariable: 'GITHUB_OAUTH2_TOKEN']]) {
      sh "./mvnw -e site -DskipTests -Dfindbugs.skip -Dgithub.global.oauth2Token=$GITHUB_OAUTH2_TOKEN"
    }
  }
}

/**
 * Build docker image and deploy to docker registry
 *
 * @param dockerImage - docker image name
 */
void dockerBuildAndDeploy(String dockerImage) {
  final String DOCKER_WORKSPACE = 'target/docker_workspace'

  node(defaultNodeLabel) {
    echo "running on node ${env.NODE_NAME}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"
    unstash 'generated-files'

    echo "Copy Dockerfile-OPENSHIFT and ROOT.war to $DOCKER_WORKSPACE/"
    sh "mkdir -p $DOCKER_WORKSPACE"
    sh "cp target/deployments/ROOT.war $DOCKER_WORKSPACE/"
    sh "cp docker/Dockerfile-OPENSHIFT $DOCKER_WORKSPACE/"

    echo "Building docker zanata-mt $version..."
    sh "docker build -f $DOCKER_WORKSPACE/Dockerfile-OPENSHIFT -t $dockerImage:$version $DOCKER_WORKSPACE"

    sh "echo Creating tag for $version..."
    sh "docker tag $dockerImage:$version $MT_DOCKER_REGISTRY_URL/$dockerImage:$version"
    sh "docker tag $dockerImage:$version $MT_DOCKER_REGISTRY_URL/$dockerImage:latest"

    echo "Docker login.."
    withCredentials([string(credentialsId: 'DOCKER-REGISTRY-TOKEN',
            variable: 'MT_REGISTRY_TOKEN')]) {
      sh "docker login -p $MT_REGISTRY_TOKEN -u unused $MT_DOCKER_REGISTRY_URL"
    }

    echo "Pushing to docker registry..."
    sh "docker push $MT_DOCKER_REGISTRY_URL/$dockerImage:$version"
    sh "docker push $MT_DOCKER_REGISTRY_URL/$dockerImage:latest"

    echo "Docker logout.."
    sh "docker logout $MT_DOCKER_REGISTRY_URL"
  }
}

/**
 * Deploy docker image to openshift staging
 * @param dockerImage
 */
void deployToStage(String dockerImage) {
  node(defaultNodeLabel) {
    echo "Login to OPENSHIFT with token"

    withCredentials([string(credentialsId: 'Jenkins-Token-open-paas-STAGE',
            variable: 'MT_OPENSHIFT_TOKEN_STAGE')]) {
      sh "oc login $MT_OPENSHIFT_URL --token $MT_OPENSHIFT_TOKEN_STAGE"
    }

    sh "oc project $MT_STAGE_PROJECT_NAME"

    echo "Update 'latest' tag to $version"
    sh "oc tag $MT_DOCKER_REGISTRY_URL/$dockerImage:$version server:latest"
    sh "oc logout"
  }
}

/**
 * Pending permission to deploy to production. 10 days before timeout
 *
 * @param dockerImage
 */
void deployToProduction(String dockerImage) {
  node (defaultNodeLabel) {
    try {
      timeout(time: 10, unit: 'DAYS') {
        def deployToProd = input(message: 'Deploy docker image to production?',
                parameters: [[$class     : 'BooleanParameterDefinition', defaultValue: false,
                              description: '', name: 'Deploy to production?']])

        if (deployToProd) {
          stage('Deploy to PRODUCTION') {
            echo "Login to OPENSHIFT with token"
            withCredentials(
                    [string(credentialsId: 'Jenkins-Token-open-paas-PROD',
                            variable: 'MT_OPENSHIFT_TOKEN_PROD')]) {
              sh "oc login $MT_OPENSHIFT_URL --token $MT_OPENSHIFT_TOKEN_PROD"
            }

            echo "Switch to use zanata-mt project"
            sh "oc project $MT_PROD_PROJECT_NAME"

            echo "Update 'latest' tag to $version"
            sh "oc tag $MT_DOCKER_REGISTRY_URL/$dockerImage:latest server:latest"
            sh "oc logout"
          }
        } else {
          echo "PROD deployment aborted."
        }
      }
    } catch (e) {
      def user = e.getCauses()[0].getUser()
      // SYSTEM means timeout.
      if ('SYSTEM' == user.toString()) {
        echo "Timeout for PROD deployment."
      } else {
        echo "PROD deployment aborted."
      }
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
}

