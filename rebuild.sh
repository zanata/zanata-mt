#!/usr/bin/env bash

# Author: Patrick Huang

# This script will:
# - stop running containers
# - rebuild the package
# - rebuild the docker container
# - start the docker container
# - tail the container log

SKIP_FRONTEND=false
SKIP_BUILD=false
BUILD_ONLY=false
GOOGLE_CREDENTIAL_FILE=""
GOOGLE_OPTION=""
MS_OPTION=""
DEFAULT_PROVIDER_OPTION="-DDEFAULT_TRANSLATION_PROVIDER=DEV"
TEST="-DskipTests"

while getopts ":sbfehtg:m:d:" opt; do
  case ${opt} in
    h)
      echo "Usage: $0 [-g google_credentials] [-m microsoft_key] [-d default_provider] [-efht]" >&2
      echo "-h This help" >&2
      echo "-f Skip frontend build" >&2
      echo "-s Skip all builds, just restart" >&2
      echo "-b Build only, do not run" >&2
      echo "-g Google credentials JSON file location" >&2
      echo "-m Microsoft translate API key" >&2
      echo "-d Default provider - DEV, MS, GOOGLE" >&2
      echo "-e enable DEV backend" >&2
      echo "-t Run tests" >&2
      exit 0
      ;;
    s)
      SKIP_BUILD=true
      if ${BUILD_ONLY}
      then
          echo "Cannot set -s and -b" >&2
          exit 0
      fi
      ;;
    b)
      BUILD_ONLY=true
      if ${SKIP_BUILD}
      then
          echo "Cannot set -s and -b" >&2
          exit 0
      fi
      ;;
    f)
      SKIP_FRONTEND=true
      ;;
    g)
      GOOGLE_CREDENTIAL_FILE="$OPTARG"
      ;;
    m)
      MS_OPTION="-DMT_AZURE_KEY=$OPTARG"
      ;;
    d)
      DEFAULT_PROVIDER_OPTION="-DDEFAULT_TRANSLATION_PROVIDER=$OPTARG"
      ;;
    e)
      ENABLE_DEV="-DDEV_BACKEND=true"
      ;;
    t)
      echo "Tests enabled" >&2
      TEST=""
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done

set -ex

# Use Google Default Application Credential json file path. If absent, it will
# create an empty temp file and use that path as the argument for container start.
if [ -z "$GOOGLE_CREDENTIAL_FILE" ]
then
    # create an empty file so we can get bind it in docker volume
    EMPTY_JSON=/tmp/g11n-mt-empty.json
    touch ${EMPTY_JSON}
    GOOGLE_CREDENTIAL_FILE=${EMPTY_JSON}
    echo "Google application credential file path not specified"
    echo "Using an empty file instead"
fi

GOOGLE_OPTION="-DGOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_CREDENTIAL_FILE"

./mvnw docker:stop -pl :mt-server || true

if ${SKIP_BUILD}
then
    echo "Skipping build..."
elif ${SKIP_FRONTEND}
then
    echo "Skipping frontend build..."
    ./mvnw clean install ${TEST} -pl \!frontend
else
    echo "Full build..."
    ./mvnw clean install ${TEST}
fi

if ${BUILD_ONLY}
then
    echo "Build only, exiting"
    exit 0
fi

./mvnw docker:build -pl :mt-server -DskipTests && \
  ./mvnw docker:start -pl :mt-server ${GOOGLE_OPTION} ${MS_OPTION} \
  ${ENABLE_DEV} ${DEFAULT_PROVIDER_OPTION}

docker logs --follow MT

