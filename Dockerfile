# ---------- Stage 1: build + test ----------
# Tests run here: if any JUnit test fails, the image is not built
# and the Jenkins pipeline stops at the "Build & Test" stage.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -B package

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/pharmasafe-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
