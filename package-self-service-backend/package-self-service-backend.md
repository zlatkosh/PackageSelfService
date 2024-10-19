# Getting Started

## Docker Compose support

This project contains a Docker Compose file [docker-compose.yaml](docker-compose.yaml).
In this file, the following services have been defined:

* postgres: [`postgres:17.0`](https://hub.docker.com/_/postgres)

## CI through GitHub Actions
To trigger the CI pipeline, you can push a commit to the repository.  
This will trigger the GitHub Actions workflow to run the appropriate CI pipeline.  
Currently available pipelines are:
* [package-self-service-backend Java CI with Maven](../.github/workflows/package-self-service-service-backend_maven_build_CI.yml): 
This pipeline will build the [package-self-service-backend](../package-self-service-backend) project and run the tests.
* [package-shipping-service Java CI with Maven](../.github/workflows/package-shipping-service_maven_build_CI.yml):
This pipeline will build the [package-shipping-service](../package-shipping-service) project and run the tests.

## Testcontainers support

This project
uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.3.4/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`postgres:latest`](https://hub.docker.com/_/postgres)
