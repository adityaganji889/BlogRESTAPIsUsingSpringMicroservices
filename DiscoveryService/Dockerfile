FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/DiscoveryService-0.0.1-SNAPSHOT.jar discoveryservice.jar
ENTRYPOINT ["java","-jar","/discoveryservice.jar"]
EXPOSE 8761