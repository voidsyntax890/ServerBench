# ============================================================
# ServerBench Agent
# Multi-stage Maven build + Java 21 runtime
# ============================================================

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy the complete Maven reactor structure.
COPY pom.xml .

COPY server-engine/pom.xml server-engine/pom.xml
COPY server-distributed-contracts/pom.xml server-distributed-contracts/pom.xml
COPY server-backend/pom.xml server-backend/pom.xml
COPY server-agent/pom.xml server-agent/pom.xml

# Copy source required by the agent and its reactor dependencies.
COPY server-engine/src server-engine/src
COPY server-distributed-contracts/src server-distributed-contracts/src
COPY server-agent/src server-agent/src

# Build the agent together with its required reactor modules.
RUN mvn -pl server-agent -am clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/server-agent/target/server-agent-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]