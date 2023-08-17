#!/usr/bin/env bash

VERSION=public-$(git rev-parse HEAD)
echo "image version=$VERSION"

./gradlew clean bootWar -x test

aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com"
docker build --build-arg VERSION="$VERSION" -f ./deploy/worker/public-dockerfile -t public.ecr.aws/y1e5c4k9/finder-worker:"$VERSION" .
docker push public.ecr.aws/y1e5c4k9/finder-worker:"$VERSION"