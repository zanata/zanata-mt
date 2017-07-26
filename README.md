# Machine translations [![GitHub release](https://img.shields.io/github/release/zanata/zanata-mt.svg?maxAge=3600)](https://github.com/zanata/zanata-mt/releases)

[![Build Status](https://travis-ci.org/zanata/zanata-mt.svg?branch=master)](https://travis-ci.org/zanata/zanata-mt)
[![Codecov](https://img.shields.io/codecov/c/github/zanata/zanata-mt.svg?maxAge=3600)](https://codecov.io/gh/zanata/zanata-mt)

[![jira](https://img.shields.io/badge/issues-Jira-yellow.svg?maxAge=3600)](https://zanata.atlassian.net/projects/ZNTAMT/issues)
[![api](https://img.shields.io/badge/docs-API-brightgreen.svg?maxAge=3600)](http://zanata.org/zanata-mt/apidocs/)
[![license](https://img.shields.io/github/license/zanata/zanata-mt.svg?maxAge=3600)](https://github.com/zanata/zanata-mt/blob/master/LICENSE)

----

## Build and run 

1. Package: `mvn clean package` (build and package war file)
2. Go to `server` directory
3. Docker build: run `mvn docker:build -DskipTests` (build docker image named zanataMT)
4. Start docker: `mvn docker:start` (This will start docker **zanataMT** and postgresql **zanataMTDB**)
5. Logs: `mvn docker:logs -Ddocker.follow`
6. To stop and remove: `mvn docker:stop` (This will stop and remove both containers)

### DEV mode

DEV mode is enabled when **ZANATA_MT_AZURE_KEY** or **GOOGLE_APPLICATION_CREDENTIALS** is not present in environment variable. The service will not use any paid service backend in this mode but will return wrapped string: 'translated[𠾴 string 𠾴]'.

`ZANATA_MT_API_ID` = `devID`<br/>
`ZANATA_MT_API_KEY` = `devKEY`
                  
----

## Environment Variables

List of Environment variables that are used in the application.

### `ZANATA_MT_API_ID` (required)
ID for /api request

### `ZANATA_MT_API_KEY` (required)
Api key for /api request

### `ZANATA_MT_AZURE_KEY` (if this and Google key are empty, DEV mode will be enabled)
Subscription key for MS translators

### `GOOGLE_APPLICATION_CREDENTIALS` (if this and AZURE key are empty, DEV mode will be enabled)
Location to the Google service account credential json file

### `ZANATA_MT_ORIGIN_WHITELIST` (optional)
Url for CORS access control. Http response will include `Access-Control-Allow-Origin` in the header to cross-browser access.
