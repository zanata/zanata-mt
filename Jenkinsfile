#!/usr/bin/env groovy
 @Library('github.com/zanata/zanata-pipeline-library@master')

def dummyForLibrary = ""

try {
  pullRequests.ensureJobDescription()

  timestamps {
    node {
      ansicolor {
        stage('Checkout') {
          info.printNode()
          notify.started()
          checkout scm
        }

        stage('Install build tools') {
          info.printNode()

          // Note: if next stage happens on another node, mvnw might have to download again
          sh "./mvnw --version"
        }

        stage('Build') {
          info.printNode()
          info.printEnv()
          def testReports = '**/target/surefire-reports/TEST-*.xml'
          sh "shopt -s globstar && rm -f $testReports"
          sh """./mvnw -e clean verify \
                     --batch-mode \
                     --settings $JENKINS_HOME/.m2/settings.xml \
                     --update-snapshots \
                     -Dmaven.test.failure.ignore \
                     -DstaticAnalysis \
          """
          junit testResults: testReports, testDataPublishers: [[$class: 'StabilityTestDataPublisher']]

          //sh "curl -s https://codecov.io/bash | bash"
        }

        if (env.BRANCH_NAME == 'master') {
          stage('Release') {
            info.printNode()
            // do maven release, tag and commit to master repo
            // copy war file
            // build docker file?
            // deploy to github

            //def WAR_FILE = pwd() + '/target/deployments/ROOT.war'
            //def pom = readMavenPom()
            //def projectVersion = pom.version
            //def version = projectVersion + "-${env.BUILD_ID}"

            //def API_JSON="{'tag_name': '$version','target_commitish': 'master','name': '$version','body': 'Release of version $version','draft': false,'prerelease': true}"
            //sh "curl --data '${API_JSON}' https://api.github.com/repos/zanata/zanata-mt/releases?access_token=${env.BUILD_ID}"
          }
        }
      }
    }

    // TODO in case of failure, notify culprits via IRC and/or email
    // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
    // http://stackoverflow.com/a/39535424/14379
    // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
    notify.successful()
  }
} catch (e) {
  notify.failed()
  throw e
}
