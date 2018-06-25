# Machine translations [![GitHub release](https://img.shields.io/github/release/zanata/zanata-mt.svg?maxAge=3600)](https://github.com/zanata/zanata-mt/releases)

[![Build Status](https://travis-ci.org/zanata/zanata-mt.svg?branch=master)](https://travis-ci.org/zanata/zanata-mt)
[![Codecov](https://img.shields.io/codecov/c/github/zanata/zanata-mt.svg?maxAge=3600)](https://codecov.io/gh/zanata/zanata-mt)

[![jira](https://img.shields.io/badge/issues-Jira-yellow.svg?maxAge=3600)](https://zanata.atlassian.net/projects/ZNTAMT/issues)
[![api](https://img.shields.io/badge/docs-API-brightgreen.svg?maxAge=3600)](http://zanata.org/zanata-mt/apidocs/)
[![license](https://img.shields.io/github/license/zanata/zanata-mt.svg?maxAge=3600)](https://github.com/zanata/zanata-mt/blob/master/LICENSE)

----

## Build and run 

1. Package: `mvn clean package` (build and package frontend and server module)
2. Go to `server` directory
3. Docker build: run `mvn docker:build -DskipTests` (build docker image named MT)
4. Start docker: `mvn docker:start` (This will start docker **MT** and postgresql **MTDB**)
5. Logs: `mvn docker:logs -Ddocker.follow`
6. To stop and remove: `mvn docker:stop` (This will stop and remove both containers)

Alternatively, run `restart.sh`. It will rebuild the package and redeploy to docker containers. 

### DEV mode

DEV mode is enabled when both **MT_AZURE_KEY** and **GOOGLE_APPLICATION_CREDENTIALS** are not present in environment variable. You can also enable it by passing a non-empty **DEV_BACKEND** environment variable to the running instance. Once enabled and requested, the service will not use any paid service backend but will return wrapped string: 'translated[网 string 网]'.
                  
----

## Environment Variables

List of Environment variables that are used in the application.

### `MT_AZURE_KEY` (if this and Google key are empty, DEV mode will be enabled)
Subscription key for MS translators

### `GOOGLE_APPLICATION_CREDENTIALS` (if this and AZURE key are empty, DEV mode will be enabled)
Location to the Google service account credential json file

### `MT_ORIGIN_WHITELIST` (optional)
Url for CORS access control. Http response will include `Access-Control-Allow-Origin` in the header to cross-browser access.

### `DEV_BACKEND` (optional)
Enable dev backend for testing.

## Authentication to the REST api

Header fields that are used for authentication:

### `X-Auth-User` the account username
### `X-Auth-Token` the account secret

If the instance starts with an empty account table in the database e.g. first load, it will display
a random hash as initial password in the server log. The value of the hash will also be
written to ```~/magpie_initial_password```.

You can then authenticate to the REST api by setting `X-Auth-User` to *admin* 
and `X-Auth-Token` to the hash noted above. This credential is considered to have admin role.
This can only be used for creating admin account (currently a BETA feature). 

## To create an account (account with admin role only)
```
POST /api/account
// payload
{
	"name": "John Smith",
	"email": "jsmith@example.com",
	"accountType": "Normal",
	"roles": ["admin"],
	"credentials": [
	    {
		    "username": "devID",
	        "secret": ["d", "e", "v", "K", "E", "Y"]
	    }
    ]
	
}
```
Once an admin account is created, the initial password is no longer valid.

## To query accounts (account with admin role only)

```GET /api/account ```

Any other REST api can be accessed by an authenticated account regardless role (for now) 
