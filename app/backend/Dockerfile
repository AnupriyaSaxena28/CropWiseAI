# ---- Build stage: compile the Spring Boot jar with Maven + JDK 17 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Run stage: slim JRE image ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render provides $PORT; Spring reads it via server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
