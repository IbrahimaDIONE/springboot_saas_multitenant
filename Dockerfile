FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B verify

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl && addgroup -S app && adduser -S app -G app \
    && mkdir -p /data/memoires /data/ouvrages && chown -R app:app /data
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER app
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=90s --retries=12 \
  CMD curl -fsS http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
