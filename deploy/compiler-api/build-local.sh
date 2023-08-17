#!/usr/bin/env bash

VERSION="local1"
AWS_PROFILE=
sudo ./gradlew clean bootWar -x test
sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-compiler-api -Dspring.profiles.active="prod,prodBaobab,all,devAuthToken" -jar module-compiler-api/build/libs/finder-compiler-api.war
# sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-compiler-api -Dspring.profiles.active="prod,prodCypress,all,devAuthToken" -jar module-compiler-api/build/libs/finder-compiler-api.war
