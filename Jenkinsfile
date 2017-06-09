#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@0.1')

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
      pullRequests.ensureJobDescription()
      try {
        stage('Checkout') {
          notify.started()
          checkout scm
          // Clean the workspace
          sh "git clean -fdx"
          sh "./mvnw --version"
        }

        if (env.BRANCH_NAME != 'master') {
          def POM=readMavenPom file: 'pom.xml'
          final String VERSION=POM.version.replace('-SNAPSHOT', '.' + env.BUILD_NUMBER)
          final String GIT_URL='git@github.com:zanata/zanata-mt.git'

          final String DOCKER_WORKSPACE = 'docker_workspace'
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
          stage ('Docker build and deploy') {
            echo "Getting dockerfile from $DOCKERFILE_GIT_REPO:master and copy to target/$DOCKER_WORKSPACE/zanata-mt/deployments/"
            sh "git clone --depth 1 --single-branch $DOCKERFILE_GIT_REPO -b master target/$DOCKER_WORKSPACE"
            sh "mkdir -p target/$DOCKER_WORKSPACE/zanata-mt/deployments"
            sh "cp target/deployments/ROOT.war target/$DOCKER_WORKSPACE/zanata-mt/deployments/"

            echo "Building docker zanata-mt $VERSION..."
            sh "docker build -f target/$DOCKER_WORKSPACE/zanata-mt/Dockerfile-OPENSHIFT -t $DOCKER_IMAGE:$VERSION target/$DOCKER_WORKSPACE/zanata-mt"

            sh "echo Creating tag for $VERSION..."
            sh "docker tag $DOCKER_IMAGE:$VERSION $RH_ENGINEERING_DOCKER_REGISTRY_URL/$DOCKER_IMAGE:$VERSION"
            sh "docker tag $DOCKER_IMAGE:$VERSION $RH_ENGINEERING_DOCKER_REGISTRY_URL/$DOCKER_IMAGE:latest"

            echo "Docker login.."
            withCredentials([string(credentialsId: 'DOCKER-REGISTRY-TOKEN', variable: 'DOCKER_REGISTRY_TOKEN')]) {
              sh "docker login -p $DOCKER_REGISTRY_TOKEN -u unused $RH_ENGINEERING_DOCKER_REGISTRY_URL"
            }

            echo "Pushing to docker registry..."
            sh "docker push $RH_ENGINEERING_DOCKER_REGISTRY_URL/$DOCKER_IMAGE:$VERSION"
            sh "docker push $RH_ENGINEERING_DOCKER_REGISTRY_URL/$DOCKER_IMAGE:latest"

            echo "Docker logout.."
            sh "docker logout $RH_ENGINEERING_DOCKER_REGISTRY_URL"
          }
          stage ("Release") {
            def tasks = [
                "SITE" : { mavenSite() },
                "RELEASE_OPEN_SHIFT": { releaseToStage("${VERSION}", "${DOCKER_IMAGE}")},
            ]
            parallel tasks
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
      } catch (e) {
        notify.failed()
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }
}

void mavenSite() {
  sh "./mvnw -e site"
}

void releaseToStage(String version, String dockerImage) {
  echo "Login to OPENSHIFT with token"

  withCredentials([string(credentialsId: 'Jenkins-Token-open-paas-STAGE', variable: 'OPEN_PAAS_TOKEN_STAGE')]) {
    sh "oc login $OPEN_PAAS_URL --token $OPEN_PAAS_TOKEN_STAGE"
  }

  sh "oc project zanata-mt-stage"

  echo "Update 'latest' tag to $version"
  sh "oc tag $RH_ENGINEERING_DOCKER_REGISTRY_URL/$dockerImage:$version server:latest"
  sh "oc logout"
}

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

  notify.testResults("UNIT")
  archive "**/target/*.war"
  if (currentBuild.result == null) {
    notify.successful()
  } else {
    notify.failed()
  }
}

