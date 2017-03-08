# To test (there is no access control, deploy only in localhost for testing)

Run:
- Set all required environment variables in the list below.
- Database: `mvn docker:start` (This will create docker image **zanataMTDB**)
- Wildfly:  `mvn clean verify cargo:run`

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
