# Klaytn Finder Backend (API, Worker)

This repository is for the backend of the Klaytn Finder. It contains the API and the worker.
For the overall architecture of the finder, please refer to the [Main Repo](https://github.com/klaytn/finder/blob/main/README.md).

For the Helm charts, please refer to the [Helm Repo](https://github.com/klaytn/finder-helm-chart/blob/main/README.md)

## Setup

- Install OpenJDK (`brew install openjdk`)
- Install Kotlin (`brew install kotlin`)
- Install Gradle (`brew install gradle`)

Zookeeper are required for the worker.

## Local Development

- Front API: `./deploy/front-api/build-local.sh`
- Private API: `./deploy/private-api/build-local.sh`
- Open API: `./deploy/open-api/build-local.sh`
- Compiler API: `./deploy/compiler-api/build-local.sh`
- Worker: `./deploy/worker/build-local.sh`

## Deployment

### Staging

- Front API: `./deploy/front-api/build-stag.sh`
- Private API: `./deploy/private-api/build-stag.sh`
- Open API: `./deploy/open-api/build-stag.sh`
- Compiler API: `./deploy/compiler-api/build-stag.sh`
- Worker: `./deploy/worker/build-stag.sh`

### Production

- Front API: `./deploy/front-api/build-prod.sh`
- Private API: `./deploy/private-api/build-prod.sh`
- Open API: `./deploy/open-api/build-prod.sh`
- Compiler API: `./deploy/compiler-api/build-prod.sh`
- Worker: `./deploy/worker/build-prod.sh`
