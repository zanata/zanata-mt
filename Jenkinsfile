#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')

/* Only keep the 10 most recent builds. */
def projectProperties = [
  [
    $class: 'BuildDiscarderProperty',
    strategy: [$class: 'LogRotator', numToKeepStr: '10']
  ],
]

properties(projectProperties)
def surefireTestReports='target/surefire-reports/TEST-*.xml'

timestamps {
  node {
    ansicolor {
      pullRequests.ensureJobDescription()
      try {
        stage('Checkout') {
          info.printNode()
          info.printEnv()
          notify.started()
          checkout scm
          // Clean the workspace
          sh "git clean -fdx"
          sh "./mvnw --version"
        }

        stage('Build') {
          sh """./mvnw -e clean verify \
                       --batch-mode \
                       --update-snapshots \
                       -Dmaven.test.failure.ignore \
                       -DstaticAnalysis \
             """
          junit testResults: "**/${surefireTestReports}"
          notify.testResults("UNIT")
          archive "**/target/*.war"
          if (currentBuild.result == null){
            notify.successful()
          }else{
            notify.failed()
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

