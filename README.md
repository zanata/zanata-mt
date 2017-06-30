[![GitHub release](https://img.shields.io/github/release/zanata/zanata-mt.svg?maxAge=3600)](https://github.com/zanata/zanata-mt/releases)
[![Codecov](https://img.shields.io/codecov/c/github/zanata/zanata-mt.svg?maxAge=3600)](https://codecov.io/gh/zanata/zanata-mt)
[![api](https://img.shields.io/badge/docs-API-brightgreen.svg?maxAge=3600)](http://zanata.org/zanata-mt/apidocs/)
[![license](https://img.shields.io/github/license/zanata/zanata-mt.svg?maxAge=3600)](https://github.com/zanata/zanata-mt/blob/master/LICENSE)

# Build and run 

Run:
- Package: `mvn clean package` (build and package war file)
- Go to `server` directory
- Docker build: run `mvn docker:build -DskipTests` (build docker image named zanataMT)
- Start docker: `mvn docker:start` (This will start docker **zanataMT** and postgresql **zanataMTDB**)
- Logs: `mvn docker:logs -Ddocker.follow`
- To stop and remove: `mvn docker:stop` (This still stop and remove both containers)

## DEV mode

DEV mode is enabled when **ZANATA_MT_AZURE_KEY** is not present in environment variable. The service will not use MS backend in this mode but will return wrapped string: 'translated[𠾴 string 𠾴]'.

`ZANATA_MT_API_ID` = `devID`<br/>
`ZANATA_MT_API_KEY` = `devKEY`
                  
----

# Documentations

## API

See [here](http://zanata.org/zanata-mt/apidocs) for more information.


## Environment Variables

List of Environment variables that are used in the application.

### `ZANATA_MT_API_ID` (required)
ID for /api request

### `ZANATA_MT_API_KEY` (required)
Api key for /api request

### `ZANATA_MT_AZURE_KEY` (required, otherwise DEV mode will be enabled)
Subscription key for MS translators

### `ZANATA_MT_ORIGIN_WHITELIST` (optional)
Url for CORS access control. Http response will include `Access-Control-Allow-Origin` in the header to cross-browser access.
