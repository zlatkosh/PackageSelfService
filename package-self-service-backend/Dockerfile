FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the pom.xml and download project dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy the project source code
COPY src ./src
# Package the application into a JAR file
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR file from the build stage
COPY --from=build /app/target/package-self-service-backend-1.0.0.jar app.jar

EXPOSE 8442

# Set the default command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
