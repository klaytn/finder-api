DOCKER_REGISTRY=asia-northeast3-docker.pkg.dev
REPO_NAME="finder-prod"
GIT_BRANCH=$(git branch --show-current)
PROJECT_ID=klaytn-finder

function main() {
    PS3="Select target: "
    select target in "front-api" "open-api" "compiler-api" "worker"
    do
        case $target in
            "front-api")
                WAR_NAME="finder-api"
                break
                ;;
            "open-api")
                WAR_NAME="finder-oapi"
                break
                ;;
            "compiler-api")
                WAR_NAME="finder-compiler-api"
                break
                ;;
            "worker")
                WAR_NAME="finder-worker"
                break
                ;;
            *)
                echo "Invalid target"
                exit 1
                break
                ;;
        esac
    done
    PS3="Select phase: "
    select phase in "public" "prod" "stag" "local"
    do
        case $phase in
            "public")
                break
                ;;
            "prod")
                break
                ;;
            "stag")
                break
                ;;
            "local")
                break
                ;;
            *)
                echo "Invalid phase"
                exit 1
                break
                ;;
        esac
    done
    VERSION=$phase-$(git rev-parse HEAD)
    echo "-----------------------------"
    echo "Target: $target"
    echo "Phase: $phase"
    echo "War: $WAR_NAME"
    echo "Image: $VERSION"
    echo "Location: $DOCKER_REGISTRY/$PROJECT_ID/$REPO_NAME/$WAR_NAME:$VERSION"
    echo "-----------------------------"

    while true; do
        read -p "Build? y/n " yn
        case $yn in
            [Yy]* ) sudo ./gradlew clean bootWar -x test; break;;
            [Nn]* ) break;;
            * ) echo "Please answer [y or n].";;
        esac
    done
    MODULE_NAME=module-$target
    if [[ $phase == "local" ]]
    then
        source ./conf.sh
        sudo -E java $JAVA_OPTS -Dsun.net.inetaddr.ttl=0 -DAPP_LOGS=./logs/prod-$target -Dspring.profiles.active="prod,prodCypress,devAuthToken" -jar $MODULE_NAME/build/libs/$WAR_NAME.war
        exit 0
    else
        gcloud auth configure-docker $DOCKER_REGISTRY
        sudo docker build --build-arg MODULE_NAME="$MODULE_NAME" --build-arg WAR_NAME="$WAR_NAME" -f ./deploy/Dockerfile -t $DOCKER_REGISTRY/$PROJECT_ID/$REPO_NAME/$WAR_NAME:$VERSION .
        docker push $DOCKER_REGISTRY/$PROJECT_ID/$REPO_NAME/$WAR_NAME:$VERSION
    fi

}



pkill -f '.*GradleDaemon.*'
main
pkill -f '.*GradleDaemon.*'