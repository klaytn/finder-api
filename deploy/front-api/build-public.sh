#!/usr/bin/env bash

VERSION=public-$(git rev-parse HEAD)
echo "image version=$VERSION"

sudo ./gradlew clean bootWar -x test
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com"
sudo docker build --build-arg VERSION="$VERSION" -f ./deploy/front-api/public-dockerfile -t public.ecr.aws/y1e5c4k9/finder-api:"$VERSION" .
docker push public.ecr.aws/y1e5c4k9/finder-api:"$VERSION"