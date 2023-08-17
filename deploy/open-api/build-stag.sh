#!/usr/bin/env bash

VERSION=stag-$(git rev-parse HEAD)
GIT_BRANCH=$(git branch --show-current)

echo "image version=$VERSION"
echo " git   branch=$GIT_BRANCH" 

if [[ $GIT_BRANCH != stag* ]]; 
then
	echo "current branch is not 'stag' but $GIT_BRANCH."
	exit 1
fi

./gradlew clean bootWar -x test
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com"
docker build --build-arg VERSION="$VERSION" -f ./deploy/open-api/dockerfile -t $(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com/finder-oapi:"$VERSION" .
docker push $(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com/finder-oapi:"$VERSION"
