FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/BlogService-0.0.1-SNAPSHOT.jar blogservice.jar
ENTRYPOINT ["java","-jar","/blogservice.jar"]
EXPOSE 8082