FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/ConfigService-0.0.1-SNAPSHOT.jar configservice.jar
ENTRYPOINT ["java","-jar","/configservice.jar"]
EXPOSE 8083