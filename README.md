# Build and run 

Run:
- Package: `mvn clean package` (build and package war file)
- Docker build: Go to `server` directory, run `mvn docker:build -DskipTests` (build docker image named zanataMT)
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

### `ZANATA_MT_AZURE_KEY` (if this and Google key is empty, DEV mode will be enabled)
Subscription key for MS translators

### `GOOGLE_API_KEY` (if this and AZURE key is empty, DEV mode will be enabled)
API key for Google translators

### `ZANATA_MT_ORIGIN_WHITELIST` (optional)
Url for CORS access control. Http response will include `Access-Control-Allow-Origin` in the header to cross-browser access.
