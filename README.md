# Build and run 

## DEV mode
DEV mode is enabled by default. The service will not use MS backend in this mode but will return string with prefix 'translated'

Run:
- Build: `mvn clean package docker:build` (This will build a docker image named zanataMT)
- Run: `mvn docker:start` (This will start docker **zanataMT** and postgresql **zanataMTDB**)
- Logs: `mvn docker:logs -Ddocker.follow`
- To stop and remove: `mvn docker:stop` (This still stop and remove both containers)

## PROD mode
PROD mode can be enabled by activating maven profile `production`. This will allow service to use Microsoft Translator service with provided **ZANATA_MT_AZURE_KEY**
e.g. `mvn clean install -Pproduction`.

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

### `ZANATA_MT_AZURE_KEY` (required)
Subscription key for MS translators

### `ZANATA_MT_ORIGIN_WHITELIST` (optional)
Url for CORS access control. Http response will include `Access-Control-Allow-Origin` in the header to cross-browser access.
