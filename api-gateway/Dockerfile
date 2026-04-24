# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -q -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/api-gateway/target/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app/app.jar"]
