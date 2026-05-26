FROM maven:3.9-eclipse-temurin-21-alpine as build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests=true


FROM amazoncorretto:21-alpine3.23-jdk as run
WORKDIR /run
COPY --from=build /app/target/*.jar /run/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/run/app.jar"]