
AWS_PROFILE=

# Check --clean arguement
if [ "$1" == "--clean" ]; then
    ./gradlew clean bootWar -x test
else 
    ./gradlew :module-open-api:bootWar
fi

sudo ./gradlew clean bootWar -x test
sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-baobab-api -Dspring.profiles.active="dev,prodBaobab,all,devAuthToken" -jar module-open-api/build/libs/finder-oapi.war
# sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-cypress-api -Dspring.profiles.active="dev,prodCypress,all,devAuthToken" -jar module-open-api/build/libs/finder-oapi.war
