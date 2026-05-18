# Stage 1: Build the Java application
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code
COPY src ./src

# Compile and package the application
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM maven:3.9-eclipse-temurin-17-slim
WORKDIR /app

# Copy the pom, target, and src from the builder stage
COPY --from=builder /app/pom.xml ./pom.xml
COPY --from=builder /app/target ./target
COPY --from=builder /app/src ./src

# Default port matching the ADK documentation guide
ENV PORT=8000
EXPOSE ${PORT}

# Updated ENTRYPOINT to match your working local command configuration
ENTRYPOINT ["sh", "-c", "mvn exec:java -Dexec.mainClass='com.google.adk.web.AdkWebServer' -Dexec.args='--adk.agents.source-dir=target --server.port=${PORT}'"]