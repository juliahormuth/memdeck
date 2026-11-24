FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S memdeck && adduser -S -G memdeck memdeck
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER memdeck
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 CMD nc -z localhost 8080 || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
