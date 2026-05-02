FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S pragmia && adduser -S pragmia -G pragmia
USER pragmia
COPY --from=builder /build/pragmia-distribution/target/pragmia.jar app.jar
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q -O- http://localhost:8080/actuator/health | grep -q '"status":"UP"'
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
