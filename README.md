# Solution
## General info
The solution was built as two separate microservices:
- [Package Shipping Service](package-shipping-service) - used to ship packages to certain addresses.
  - requirements: [package-shipping-service.md](requirements/package-shipping-service.md)
  - API specification: [package-shipping-service.yaml](package-shipping-service/src/main/resources/static/package-shipping-service.yaml)
- [Package Self Service Backend](package-self-service-backend) - used to submit packages for sending.
  - requirements: [start-here.md](requirements/start-here.md)
  - API specification: [package-self-service.yaml](package-self-service-backend/src/main/resources/static/package-self-service-backend.yaml)

> **Note**  
> I realize that I could have used WireMock to mock the package-shipping-service, 
> but I decided it's best to have a live service available while developing `package-self-service-backend`.

## Package Shipping Service
### Technologies:
- **Java 21** 
- **Spring boot 3.2.10**
- **Maven 3.9.9**
- **PostgreSQL 17.0** instance named `postgres_package_shipping` (see [docker-compose.yaml](docker-compose.yaml))
- **Docker** - used to containerize the application (see [Dockerfile](package-shipping-service/Dockerfile))
- **Lombok** - used to reduce boilerplate code
- **JUnit 5** - @Nested classes with Given-When-Then structure for better readability

## Package Self Service Backend
### Technologies:
- **Java 21**
- **Spring boot 3.3.4**
- **Maven 3.9.9**
- **PostgreSQL 17.0** instance named `postgres_package_self_service` (see [docker-compose.yaml](docker-compose.yaml))
- **Docker** - used to containerize the application (see [Dockerfile](package-self-service-backend/Dockerfile))
- **Lombok** - used to reduce boilerplate code
- **JUnit 5** - @Nested classes with Given-When-Then structure for better readability  
...
### Resilience
- **Resilience4j Circuit Breaker** - implemented in [PackageShippingServiceClient](package-self-service-backend/src/main/java/com/zlatko/packageselfservicebackend/clients/PackageShippingServiceClient.java) 
on calls to the `package-shipping-service` API and configured in [application.properties](package-self-service-backend/src/main/resources/application.properties)
- **Resilience4j Retry** - implemented in [PackageShippingServiceClient](package-self-service-backend/src/main/java/com/zlatko/packageselfservicebackend/clients/PackageShippingServiceClient.java)
on calls to the `package-shipping-service` API and configured in [application.properties](package-self-service-backend/src/main/resources/application.properties)
- **Resilience4j Rate Limiter** - implemented in [PackageSelfServiceService](package-self-service-backend/src/main/java/com/zlatko/packageselfservicebackend/services/PackageSelfServiceService.java)
with a max limit of 100 requests per second, a 5-second wait time with a maximum of 100 consumers waiting. Configured in [application.properties](package-self-service-backend/src/main/resources/application.properties)
- **Resilience4j Time Limiter** - implemented in [PackageSelfServiceService](package-self-service-backend/src/main/java/com/zlatko/packageselfservicebackend/services/PackageSelfServiceService.java)
making sure main endpoint consumer requests time out after a minute. Configured in [application.properties](package-self-service-backend/src/main/resources/application.properties)
## Running the applications in Docker
> **⚠ Important**   
> Command : `docker-compose up -d <container name>` or `docker compose up -d <container name>` (no hyphen) depending on the docker version you are using.

### Package Shipping Service
To run the Package Shipping Service, you first need to start the PostgreSQL instance.

```shell
docker compose up -d postgres_package_shipping
```
After the PostgreSQL instance is up and running, you can start the Package Shipping Service docker container:
```shell
docker compose up -d package-shipping-service
```
### Package Self Service Backend
To run the Package Self Service Backend, you first need to start the PostgreSQL instance.

```shell
docker compose up -d postgres_package_self_service
```
After the PostgreSQL instance is up and running, you can start the Package Self Service Backend docker container:
```shell
docker compose up -d package-self-service-backend
```
## Running the applications locally
### Package Shipping Service
Before running the Package Shipping Service locally, you first need to start its PostgreSQL instance (same `docker compose` command from above).  
Then simply run the included run configuration [PackageShippingServiceApplication](.run/PackageShippingServiceApplication.run.xml) in intelliJ IDEA.

### Package Self Service Backend
Before running the Package Self Service Backend locally, you first need to start its PostgreSQL instance (same `docker compose` command from above).  
Then run the included run configuration [PackageSelfServiceBackendApplication](.run/PackageSelfServiceBackendApplication.run.xml) in intelliJ IDEA.

## CI through GitHub Actions
To trigger the CI pipeline, you can push a commit to the repository.  
This will trigger the GitHub Actions workflow to run the appropriate CI pipeline.  
Currently available pipelines are:
* [package-self-service-backend Java CI with Maven](.github/workflows/package-self-service-service-backend_maven_build_CI.yml):
  This pipeline will build the [package-self-service-backend](package-self-service-backend) project and run the tests.
* [package-shipping-service Java CI with Maven](.github/workflows/package-shipping-service_maven_build_CI.yml):
  This pipeline will build the [package-shipping-service](package-shipping-service) project and run the tests.

## Tracing and Logging
 - As soon as a request is made to the `Package Self Service Backend`, a `X-Correlation-ID` is generated and added to the request headers.
This ID is then used to trace the request through the logs of both the `Package Self Service Backend` and the `Package Shipping Service`.
 - The logging configuration is set in the respective `logback-spring.xml` files and includes the `X-Correlation-ID` and `Request-Id`.

## Things I would improve given time... So many :)
- Improve branch coverage in Unit tests
- Introduce Integration tests with a test container for PostgreSQL and a WireMock server for the `Package Self Service Backend`
- Implement a proper logging strategy by sending logs to Loki and Grafana (Started in the `logback-spring.xml` files but no time to finish)
- Implement a proper monitoring strategy by sending metrics to Prometheus and Grafana
- Maybe even consider replacing the `X-Correlation-ID` and `Request-Id` approach with TraceId and SpanId from OpenTelemetry by sending these to Jaeger, Zipkin or Tempo
- Implement a proper security strategy with OAuth2 and JWT tokens
- Replace the docker-compose files with Kubernetes manifests for the services. PostgreSQL instances are fine in Docker Compose unless provisioned directly on a cloud provider.
- Implement a proper CI/CD pipeline
- Talk changing the `Package Shipping Service API` a bit with the team responsible, to reduce the number of calls to it the current integration dictates.
- Add some diagrams to this documentation and make it more visually appealing