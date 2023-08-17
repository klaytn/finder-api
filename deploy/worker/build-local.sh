#!/usr/bin/env bash

VERSION="local1"
AWS_PROFILE=
# Need to Run papi first and update application-client.yml papi url
sudo ./gradlew clean bootWar -x test

sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-worker -Dspring.profiles.active="prod" -jar module-worker/build/libs/finder-worker.war