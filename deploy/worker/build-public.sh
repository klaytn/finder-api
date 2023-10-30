#!/usr/bin/env bash

VERSION=public-$(git rev-parse HEAD)
echo "image version=$VERSION"

./gradlew clean bootWar -x test

aws ecr get-login-password --region ap-northeast-2 --profile krosslab_prod | docker login --username AWS --password-stdin "$(aws sts get-caller-identity --query Account --output text --profile krosslab_prod).dkr.ecr.ap-northeast-2.amazonaws.com"
sudo docker build --build-arg VERSION="$VERSION" -f ./deploy/worker/public-dockerfile -t public.ecr.aws/klaytn-finder/finder-worker:"$VERSION" .
docker push public.ecr.aws/klaytn-finder/finder-worker:"$VERSION"