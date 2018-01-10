#!/usr/bin/env bash

set -ex

# Author: Patrick Huang

# This script will:
# - stop running containers
# - rebuild the package
# - rebuild the docker container
# - start the docker container
# - tail the container log
# When it starts the container, it will try to pass in command line arguments.
# The first argument is considered the path to a
# Google Default Application Credential json file path. If absent, it will
# create an empty temp file and use that path as the argument for container start.
GOOGLE_CREDENTIAL_FILE=$1
if [ -z "$GOOGLE_CREDENTIAL_FILE" ]
then
    # create an empty file so we can get bind it in docker volume
    EMPTY_JSON=/tmp/g11n-mt-empty.json
    touch ${EMPTY_JSON}
    GOOGLE_CREDENTIAL_FILE=${EMPTY_JSON}
    echo "You did not specify Google application credential file path as first argument"
    echo "Using an empty file instead"
fi

mvn docker:stop || true
mvn clean package -DskipTests
mvn docker:build -DskipTests
mvn docker:start \
 -DGOOGLE_APPLICATION_CREDENTIALS=${GOOGLE_CREDENTIAL_FILE} \
 -DMT_AZURE_KEY=$2

docker logs --follow zanataMT

