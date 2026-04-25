# Use a Maven base image to build the Java application
FROM maven:3.8-openjdk-17-slim AS builder
WORKDIR /app
COPY pom.xml .

#RUN mvn dependency:go-offline -B

RUN mvn clean package -DskipTests

COPY src ./src
# Use a smaller base image for the final running application

EXPOSE 8080

# The ENTRYPOINT must be a single line. This is the corrected line.
ENTRYPOINT ["mvn", "exec:java","-Dexec.mainClass=com.google.adk.web.AdkWebServer","-Dexec.classpathScope=compile","-Dexec.args=--server.port=${PORT} --adk.agents.source-dir=src/main/java"]
