FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common-lib/pom.xml ./common-lib/pom.xml
COPY service-registry/pom.xml ./service-registry/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY user-service/pom.xml ./user-service/pom.xml
COPY product-service/pom.xml ./product-service/pom.xml
COPY order-service/pom.xml ./order-service/pom.xml
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY recommendation-service/pom.xml ./recommendation-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml

COPY common-lib/src ./common-lib/src
COPY payment-service/src ./payment-service/src

RUN mvn clean package -pl payment-service -am -DskipTests

FROM amazoncorretto:21-alpine3.21-jdk
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
