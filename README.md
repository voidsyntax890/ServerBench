# ServerBench

**ServerBench — Distributed Server Performance & Load Testing Platform**

ServerBench is a modular server benchmarking and performance analysis platform designed to measure, compare, and analyze server behavior under controlled workloads.

The platform combines configurable benchmark execution, multiple concurrency models, distributed execution capabilities, centralized result storage, observability, and AI-assisted performance analysis into a single system.

---

## Overview

Performance testing often requires more than simply sending requests to a server. A useful benchmarking platform should make it possible to:

- define controlled benchmark workloads
- vary concurrency and execution strategies
- execute benchmarks repeatedly for comparison
- collect measurable performance results
- persist historical benchmark data
- distribute workload execution across multiple agents
- observe the benchmark infrastructure itself
- analyze results using both measured metrics and AI-assisted insights

ServerBench was built around these requirements.

The platform separates the **control plane** from the **benchmark execution plane**, allowing benchmark configuration, orchestration, execution, messaging, metrics, persistence, and analysis to remain independently structured.

---

## Project Status

### Phase 1 — Core ServerBench

**Completed**

The core ServerBench platform has been implemented as the primary project phase.

Phase 1 includes:

- benchmark configuration and execution
- multiple benchmark execution strategies
- concurrent workload generation
- virtual-thread based execution
- distributed benchmark execution foundation
- agent-based execution
- Kafka-based communication
- Redis-backed active execution state
- PostgreSQL-based persistent storage
- Prometheus-based metrics
- OpenTelemetry-based tracing
- Grafana integration
- Jaeger integration
- web-based frontend
- benchmark result visualization
- performance analysis
- AI-assisted benchmark insights
- Docker Compose based local deployment
- GitHub Actions CI validation

### Phase 2 — Security & Deployment Strengthening

**In Progress**

The current work is an advancement phase over the completed ServerBench core.

This phase focuses on adding only the security and deployment controls that provide concrete value for the platform, including authentication, authorization, ownership isolation, secure agent communication, target protection, secret handling, operational hardening, and deployment readiness.

> **Security is under process.**

---

## Key Objectives

ServerBench is designed to provide a structured environment for:

1. **Benchmark Execution**
   - Execute controlled workloads against configurable targets.
   - Support different concurrency and execution strategies.
   - Run benchmarks using request-count or duration-oriented configurations.

2. **Performance Measurement**
   - Capture benchmark execution results.
   - Persist historical results for later inspection.
   - Compare benchmark runs and execution strategies.

3. **Distributed Execution**
   - Coordinate benchmark execution across dedicated agents.
   - Use messaging infrastructure to distribute execution work.
   - Keep control-plane responsibilities separate from execution responsibilities.

4. **Observability**
   - Monitor application and benchmark metrics.
   - Trace important execution flows.
   - Visualize operational and performance information.

5. **Performance Analysis**
   - Present benchmark metrics through the frontend.
   - Generate structured performance analysis.
   - Provide AI-assisted insights using Spring AI.

6. **Deployment Readiness**
   - Provide reproducible local infrastructure.
   - Keep environment-specific configuration externalized.
   - Validate the project continuously through CI.

---

# Architecture

ServerBench is organized around a layered and distributed architecture.

```text
                    ┌──────────────────────────┐
                    │      Web Frontend        │
                    │   React + Vite + Charts  │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │      Backend / API       │
                    │ Spring Boot Application  │
                    └────────────┬─────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
       ┌─────────────┐    ┌─────────────┐    ┌──────────────┐
       │ Validation  │    │ Orchestrator│    │ AI Analysis  │
       │ & Config    │    │ / Lifecycle │    │ Spring AI    │
       └─────────────┘    └──────┬──────┘    └──────────────┘
                                 │
                  ┌──────────────┴──────────────┐
                  │                             │
                  ▼                             ▼
           ┌─────────────┐              ┌─────────────┐
           │    Redis    │              │    Kafka    │
           │ Active State│              │  Messaging  │
           └─────────────┘              └──────┬──────┘
                                               │
                              ┌────────────────┼────────────────┐
                              │                │                │
                              ▼                ▼                ▼
                       ┌────────────┐   ┌────────────┐   ┌────────────┐
                       │   Agent 1  │   │   Agent 2  │   │   Agent N  │
                       │ Benchmark  │   │ Benchmark  │   │ Benchmark  │
                       │ Execution  │   │ Execution  │   │ Execution  │
                       └─────┬──────┘   └─────┬──────┘   └─────┬──────┘
                             │                │                │
                             └────────────────┼────────────────┘
                                              │
                                              ▼
                                   ┌────────────────────┐
                                   │  Benchmark Engine  │
                                   │ Execution Models   │
                                   └────────────────────┘

        ┌─────────────────────────────────────────────────────────┐
        │                  Observability Layer                    │
        │                                                         │
        │      Prometheus │ Grafana │ OpenTelemetry │ Jaeger      │
        └─────────────────────────────────────────────────────────┘

                         ┌──────────────────────┐
                         │      PostgreSQL      │
                         │ Historical Results   │
                         │ & Application Data   │
                         └──────────────────────┘
```

---

# Architecture Principles

The project follows several core architectural principles:

### Separation of Responsibilities

The backend is responsible for management and orchestration while benchmark execution is handled by the engine and distributed agents.

### Modular Benchmark Engine

Benchmark execution strategies are isolated from the rest of the application so that concurrency models can be compared without redesigning the control layer.

### Distributed Execution

Kafka provides asynchronous communication between the orchestration layer and benchmark agents, allowing execution responsibility to be separated from the central backend.

### Active State vs Persistent State

Redis is used for active execution/runtime state while PostgreSQL stores durable application and benchmark information.

### Observability by Design

Metrics and traces are treated as part of the platform architecture rather than as an afterthought.

### AI as an Analysis Layer

AI-assisted analysis operates on benchmark findings and measured information rather than replacing the underlying benchmark measurements.

---

# Main Features

## Benchmark Configuration

ServerBench supports configurable benchmark parameters such as:

- target host
- target port
- execution mode
- total requests
- measurement duration
- concurrency
- warm-up duration
- request timeout
- repetitions
- thread-pool configuration where applicable

The backend validates benchmark configuration before execution.

---

## Multiple Execution Strategies

The benchmark engine supports different execution approaches so that the effect of concurrency models can be studied.

The project includes execution using traditional threading approaches as well as modern Java virtual-thread based execution.

This allows ServerBench to be used not only as a load generator but also as a platform for comparing server behavior under different client-side execution strategies.

---

## Distributed Benchmark Execution

ServerBench provides a distributed execution foundation using dedicated agents.

A typical distributed flow is:

```text
User
  │
  ▼
Frontend
  │
  ▼
Backend
  │
  ├── Validate experiment
  │
  ├── Create execution state
  │
  └── Dispatch distributed work
          │
          ▼
        Kafka
          │
     ┌────┴────┐
     ▼         ▼
  Agent A   Agent B
     │         │
     └────┬────┘
          ▼
   Benchmark Execution
          │
          ▼
      Results / State
          │
          ▼
       Backend
          │
          ▼
      PostgreSQL
```

This design allows benchmark workload generation to scale independently from the central control application.

---

# Observability

ServerBench includes an observability stack for understanding both application behavior and benchmark execution.

## Prometheus

Prometheus is used for metrics collection and monitoring.

Typical use cases include:

- application metrics
- runtime metrics
- benchmark-related measurements
- infrastructure visibility

## Grafana

Grafana provides dashboards for visualizing Prometheus metrics.

## OpenTelemetry

OpenTelemetry is used for distributed tracing and execution visibility across application components.

## Jaeger

Jaeger provides a trace visualization interface for inspecting execution flows and identifying where time is being spent.

Together:

```text
ServerBench Components
        │
        ├──── Metrics ─────► Prometheus ─────► Grafana
        │
        └──── Traces ──────► OpenTelemetry ──► Jaeger
```

---

# Data & Persistence

## PostgreSQL

PostgreSQL is used for durable application and benchmark data.

Persistent information includes the application's structured benchmark and execution history.

## Redis

Redis is used for active runtime state where fast access is important during benchmark execution.

The separation can be summarized as:

```text
Redis      → Active / Runtime State
PostgreSQL → Durable / Historical State
```

---

# AI-Assisted Performance Analysis

ServerBench integrates Spring AI for AI-assisted interpretation of benchmark results.

The AI layer is intended to transform measured benchmark information into useful engineering insights, such as:

- identifying notable performance patterns
- summarizing observed behavior
- highlighting potential bottlenecks
- presenting benchmark findings in a more readable form

The measured benchmark data remains the source of truth; AI is an analysis layer over those results.

---

# Technology Stack

## Backend

- Java
- Spring Boot
- Spring Data JPA
- Spring AI
- Apache Kafka
- Redis
- PostgreSQL
- Maven

## Benchmark Engine

- Java concurrency APIs
- traditional thread-based execution
- Java virtual threads
- configurable workload execution

## Frontend

- React
- Vite
- JavaScript
- Recharts

## Observability

- Prometheus
- Grafana
- OpenTelemetry
- Jaeger

## Infrastructure

- Docker
- Docker Compose
- GitHub Actions

---

# Project Structure

```text
ServerBench/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── monitoring/
│   └── prometheus/
│       └── prometheus.yml
│
├── server-backend/
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│
├── server-agent/
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│
├── server-distributed-contracts/
│   └── src/
│
├── server-engine/
│   └── src/
│       └── main/
│           └── java/
│
├── server-frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.*
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── pom.xml
```

---

# Module Responsibilities

| Module | Responsibility |
|---|---|
| `server-backend` | REST APIs, experiment lifecycle, orchestration, persistence, analysis |
| `server-engine` | Core benchmark execution and concurrency implementations |
| `server-agent` | Distributed benchmark execution on worker agents |
| `server-distributed-contracts` | Shared contracts/models between distributed components |
| `server-frontend` | User interface, dashboards, benchmark configuration and visualization |
| `monitoring` | Monitoring configuration such as Prometheus |

---

# Getting Started

## Prerequisites

Make sure the following are installed:

- Git
- Java Development Kit
- Maven
- Node.js and npm
- Docker
- Docker Compose

---

## Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd ServerBench
```

---

## Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Update the values in `.env` according to your environment.

Important secrets such as database credentials and API keys must not be committed to Git.

---

# Run with Docker Compose

ServerBench provides a Docker Compose setup for the core application infrastructure.

Start the complete stack:

```bash
docker compose up -d
```

Check running containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f
```

Stop the stack:

```bash
docker compose down
```

---

# Local Development

The project can also be developed module by module.

## Backend

From the repository root:

```bash
mvn clean verify
```

Run the backend using your preferred IDE or Maven/Spring Boot workflow.

## Frontend

From:

```text
server-frontend/
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Create a production build:

```bash
npm run build
```

Run linting:

```bash
npm run lint
```

---

# CI/CD

ServerBench uses GitHub Actions to continuously validate the project.

The CI workflow validates the major backend and frontend build paths and runs the project's automated verification steps.

The CI pipeline is intended to act as a **quality guardrail**:

```text
Code Change
    │
    ▼
Git Push / Pull Request
    │
    ▼
GitHub Actions
    │
    ├── Backend Verification
    ├── Automated Tests
    ├── Frontend Lint
    └── Frontend Build
    │
    ▼
Validation Result
```

Docker image creation is treated as a deliberate checkpoint rather than as a mandatory image build for every code change.

---

# Benchmark Workflow

A typical ServerBench workflow is:

```text
1. Create Experiment
        │
        ▼
2. Validate Configuration
        │
        ▼
3. Select Execution Architecture
        │
        ▼
4. Start Experiment
        │
        ▼
5. Execute Workload
        │
        ├── Local Execution
        │
        └── Distributed Agent Execution
        │
        ▼
6. Collect Measurements
        │
        ▼
7. Persist Results
        │
        ▼
8. Visualize Results
        │
        ▼
9. Compare / Analyze Performance
        │
        ▼
10. Generate AI-Assisted Insights
```

---

# Example Benchmark Scenarios

ServerBench can be used to investigate questions such as:

### Concurrency Model Comparison

Compare the behavior produced by different client execution strategies under similar workload parameters.

### Concurrency Scaling

Run the same target with increasing concurrency levels and observe how throughput and latency change.

### Repeated Benchmarking

Execute multiple repetitions to observe whether measured behavior remains consistent across runs.

### Distributed Load Generation

Use multiple benchmark agents to generate workload independently from the central backend.

### Performance Investigation

Use collected measurements, dashboards, traces, and AI-assisted analysis to investigate unusual benchmark behavior.

---

# Configuration Philosophy

ServerBench separates configuration from source code where practical.

Environment-specific values such as:

- database configuration
- Redis configuration
- Kafka connection details
- application ports
- observability endpoints
- AI provider credentials

are intended to be supplied through environment configuration rather than hard-coded secrets.

The repository includes:

```text
.env.example
```

as the configuration reference without containing real credentials.

---

# Security & Deployment Hardening

Security is being added as a dedicated advancement phase after completion of the core ServerBench platform.

The objective is not to add security technologies simply for demonstration or resume value. Controls are being introduced where they address a real ServerBench threat, operational requirement, or deployment concern.

The planned strengthening areas include:

- authentication for protected management and benchmark operations
- authorization based on user roles and resource ownership
- experiment ownership isolation
- secure handling of credentials and secrets
- authenticated distributed agents
- protection of expensive benchmark operations
- target host and port security
- protection against unintended internal-network access
- controlled benchmark resource limits
- secure API and browser configuration
- auditability of security-relevant operations
- deployment-oriented infrastructure hardening

This work is being performed without unnecessarily redesigning the completed Phase 1 architecture.

---

# Development Philosophy

ServerBench is developed with the following principles:

### Correctness First

Benchmarking infrastructure must produce reliable and understandable measurements.

### Modular Design

Benchmark engine, orchestration, agents, persistence, messaging, observability, and frontend responsibilities remain separated.

### Controlled Complexity

Infrastructure components are introduced because they solve a concrete architectural problem.

### Reproducibility

Docker Compose and environment-based configuration make the development environment easier to reproduce.

### Observability

Performance tooling should itself be observable.

### Incremental Hardening

Security and deployment improvements are introduced deliberately after the stable core platform has been completed.

---

# Current Engineering Scope

### Completed

- Core benchmark engine
- Multiple execution strategies
- Virtual-thread execution
- Benchmark configuration and validation
- Experiment lifecycle
- Distributed agent execution foundation
- Kafka integration
- Redis runtime state
- PostgreSQL persistence
- Prometheus integration
- Grafana integration
- OpenTelemetry integration
- Jaeger integration
- React frontend
- Benchmark result visualization
- Performance analysis
- Spring AI integration
- Docker Compose environment
- GitHub Actions CI

### Under Development

- Security hardening
- Authentication and authorization
- Resource ownership enforcement
- Secure distributed-agent identity
- Target security controls
- Deployment hardening
- Final security and deployment validation

---

# Future Deployment Direction

The current repository is being prepared for deployment-oriented use without forcing an immediate cloud deployment.

The project will retain a clear separation between:

```text
Development
    ↓
Local Docker Environment
    ↓
Deployment-Ready Configuration
    ↓
Future Cloud Deployment
```

Cloud deployment is intentionally treated as a later stage rather than as a requirement of the current development phase.

---

# Contributing

This repository is primarily a personal engineering project, but the codebase is structured around maintainability and clear module boundaries.

Before making changes:

1. understand the module affected by the change
2. preserve existing architecture unless a change is justified
3. run the relevant tests
4. run the backend verification when backend code changes
5. run frontend lint/build when frontend code changes
6. avoid committing secrets or environment-specific credentials

---

# License

License information can be added here when the repository's licensing decision is finalized.

---

# Author

**Anirban Bhattacharyya**

Information Technology Student  
Java • Distributed Systems • Generative AI • Backend Engineering

---

## Project Status

**ServerBench Core — Completed ✅**

**Security & Deployment Strengthening — In Progress 🚧**

> **Security is under process.**
