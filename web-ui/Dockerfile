# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -q -pl web-ui -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/web-ui/target/*.jar /app/app.jar
EXPOSE 8092
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app/app.jar"]
