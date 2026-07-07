FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/UserService-0.0.1-SNAPSHOT.jar userservice.jar
ENTRYPOINT ["java","-jar","/userservice.jar"]
EXPOSE 8081