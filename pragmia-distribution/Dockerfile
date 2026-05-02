FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY src/main/java/io/pragmia distribution/
COPY src/main/resources/application.yml distribution/
RUN true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/distribution target/pragmia-distribution-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
