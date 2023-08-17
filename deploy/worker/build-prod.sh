#!/usr/bin/env bash

VERSION=prod-$(git rev-parse HEAD)
GIT_BRANCH=$(git branch --show-current)

echo "image version=$VERSION"
echo "git branch=$GIT_BRANCH"

if [[ $GIT_BRANCH != main* ]];
then
	echo "current branch is not 'main' but $GIT_BRANCH."
	exit 1
fi

sudo ./gradlew clean bootWar -x test
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com"
sudo docker build --build-arg VERSION="$VERSION" -f ./deploy/worker/dockerfile -t $(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com/finder-worker:"$VERSION" .
docker push $(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com/finder-worker:"$VERSION"
