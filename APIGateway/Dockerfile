FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/APIGateway-0.0.1-SNAPSHOT.jar apigatewayservice.jar
ENTRYPOINT ["java","-jar","/apigatewayservice.jar"]
EXPOSE 9090