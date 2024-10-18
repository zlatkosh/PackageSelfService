# Package Shipping Service

The **Package Shipping Service** is used to ship packages to a certain address.  
It has the following features:

- Tracks status of packages.
- Order the shipping of a Package.
- Display Shipping Details.

___

## API Specification
The API specification can be found in the [package-shipping-service.yaml](src/main/resources/static/package-shipping-service.yaml) file.

___
## Technical Details
I built this as a separate application since anyway __PackageSelfServiceBackEnd__ needed to call its API
and I'd rather have a live service available instead of only rely on a WireMock endpoint for this part.  
This is a __Spring Boot 3.2.10__ and __Java 21__.  
It creates Java 21's new virtual threads instead of the traditional heavyweight OS threads.

___
## Running the application
> **⚠ Important**  
> Before running the application please init the postgres docker container from [docker-compose.yaml](docker-compose.yaml).  
> Command : `docker-compose up -d postgres` or `docker compose up -d postgres` (no hyphen) depending on the docker version you are using.
