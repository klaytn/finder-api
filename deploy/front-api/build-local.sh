#!/usr/bin/env bash

VERSION="local1"
AWS_PROFILE=
sudo ./gradlew clean bootWar -x test
sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-baobab-api -Dspring.profiles.active="dev,prodBaobab,all,devAuthToken" -jar module-front-api/build/libs/finder-api.war
sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-cypress-api -Dspring.profiles.active="dev,prodCypress,all,devAuthToken" -jar module-front-api/build/libs/finder-api.war