# ServerBench Architecture

## 1. Purpose

ServerBench is a distributed server performance and load-testing platform designed to configure, execute, measure, persist, visualize, and analyze controlled benchmark workloads.

The architecture separates benchmark management from benchmark execution and supporting infrastructure so that the system can support both local and distributed execution while keeping persistence and observability concerns independent.

> **Phase 1 status:** The core ServerBench platform is complete.  
> **Phase 2:** Security and deployment strengthening are being developed separately.

---

## 2. High-Level Architecture

```text
                         ┌─────────────────────────┐
                         │       React Frontend    │
                         │   Configuration / UI    │
                         └────────────┬────────────┘
                                      │ HTTP / SSE
                                      ▼
                         ┌─────────────────────────┐
                         │    Spring Boot Backend  │
                         │ REST API / Orchestration│
                         └────────────┬────────────┘
                                      │
                 ┌────────────────────┼────────────────────┐
                 │                    │                    │
                 ▼                    ▼                    ▼
        ┌────────────────┐   ┌────────────────┐   ┌────────────────┐
        │ Configuration  │   │ Experiment /   │   │ Performance /  │
        │ Validation     │   │ Run Lifecycle  │   │ AI Analysis    │
        └────────────────┘   └───────┬────────┘   └────────────────┘
                                      │
                         ┌────────────┴────────────┐
                         │                         │
                         ▼                         ▼
                ┌────────────────┐        ┌────────────────┐
                │     Redis      │        │     Kafka      │
                │ Active Runtime │        │ Distributed    │
                │     State      │        │    Messaging   │
                └────────────────┘        └───────┬────────┘
                                                  │
                                      ┌───────────┼───────────┐
                                      │           │           │
                                      ▼           ▼           ▼
                                ┌──────────┐ ┌──────────┐ ┌──────────┐
                                │ Agent 1  │ │ Agent 2  │ │ Agent N  │
                                │ Executor │ │ Executor │ │ Executor │
                                └────┬─────┘ └────┬─────┘ └────┬─────┘
                                     │             │             │
                                     └─────────────┼─────────────┘
                                                   ▼
                                         ┌───────────────────┐
                                         │   ServerBench     │
                                         │  Benchmark Engine │
                                         └─────────┬─────────┘
                                                   │
                                                   ▼
                                            Target Server(s)

       ┌─────────────────────────────────────────────────────────────┐
       │                     Observability                           │
       │                                                             │
       │ Prometheus ──► Grafana                                      │
       │ OpenTelemetry ──► Jaeger                                    │
       └─────────────────────────────────────────────────────────────┘

                         ┌───────────────────────┐
                         │      PostgreSQL       │
                         │ Persistent Experiment │
                         │ and Benchmark Data    │
                         └───────────────────────┘
```

---

## 3. Major Components

### 3.1 Frontend

The frontend is implemented with React and Vite.

Its responsibilities include:

- experiment configuration
- experiment lifecycle interaction
- benchmark status presentation
- result visualization
- performance analysis presentation
- user-facing navigation and application views

The frontend communicates with the backend rather than directly controlling benchmark engines, databases, Kafka, or Redis.

---

### 3.2 Backend

The backend is implemented with Spring Boot.

Its responsibilities include:

- exposing application APIs
- validating benchmark configuration
- creating and managing experiments
- controlling the experiment lifecycle
- coordinating execution
- persisting experiment and benchmark information
- maintaining active runtime state
- providing performance analysis
- exposing live execution information to the frontend

The backend acts as the central control-plane component.

---

### 3.3 Benchmark Engine

The benchmark engine is a separate module responsible for workload execution.

The engine contains the core abstractions and execution implementations used by ServerBench.

The separation allows execution strategies to evolve independently from the backend orchestration layer.

One important implementation explored by the project is Java virtual-thread based concurrency, alongside traditional thread-oriented execution.

---

### 3.4 Distributed Agents

Agents are dedicated execution components used when benchmark work is distributed.

An agent is responsible for executing assigned benchmark work rather than acting as the central application controller.

The distributed architecture therefore separates:

```text
Control Plane
    Backend
       │
       ▼
  Work Assignment
       │
       ▼
Execution Plane
    Agents
       │
       ▼
Benchmark Engine
```

---

### 3.5 Kafka

Kafka provides asynchronous messaging for distributed execution.

It is used to decouple:

- benchmark orchestration
- work delivery
- agent-side execution
- distributed execution events/results

This avoids requiring the backend to directly manage every agent execution through a synchronous request/response interaction.

---

### 3.6 Redis

Redis is used for active runtime state.

This is intentionally separated from durable historical storage.

Conceptually:

```text
Redis
└── Active execution / runtime state

PostgreSQL
└── Persistent experiment / benchmark history
```

Redis is therefore suited to information that the backend needs to access quickly while an experiment is running.

---

### 3.7 PostgreSQL

PostgreSQL provides durable persistence for application and benchmark data.

The relational model stores structured experiment and execution information so that benchmark history remains available beyond an individual runtime session.

---

### 3.8 Observability Stack

ServerBench includes a dedicated observability path:

```text
Metrics
   │
   ▼
Prometheus
   │
   ▼
Grafana

Tracing
   │
   ▼
OpenTelemetry
   │
   ▼
Jaeger
```

The purpose is to make both application behavior and distributed execution easier to inspect.

---

### 3.9 Spring AI

Spring AI is used as the AI-assisted performance analysis layer.

The AI layer is placed after benchmark measurements and analysis data rather than inside the core measurement path.

This preserves an important architectural principle:

```text
Measured Data
     │
     ▼
Deterministic Analysis
     │
     ▼
AI-Assisted Interpretation
```

The benchmark measurement itself remains the source of truth.

---

## 4. Control Plane and Execution Plane

One of the central architectural decisions is the separation of responsibilities.

### Control Plane

The backend controls:

- experiment configuration
- validation
- lifecycle
- orchestration
- persistence
- runtime state
- analysis
- frontend communication

### Execution Plane

The benchmark engine and agents handle:

- workload generation
- concurrency
- target interaction
- execution-specific measurements

This separation makes it easier to distribute workload generation without moving application management responsibilities into every worker.

---

## 5. Data Flow

A simplified local flow is:

```text
User
  │
  ▼
Frontend
  │
  ▼
Backend
  │
  ├── Validate configuration
  │
  ├── Create experiment
  │
  ├── Maintain runtime state
  │
  └── Execute benchmark
          │
          ▼
      Benchmark Engine
          │
          ▼
       Target Server
          │
          ▼
       Measurements
          │
          ├──────────────► Redis / runtime state
          │
          └──────────────► PostgreSQL / persistence
          │
          ▼
       Result Analysis
          │
          ├──────────────► Frontend
          │
          └──────────────► Spring AI
```

The distributed flow adds Kafka and agents between orchestration and execution.

---

## 6. Database and Runtime-State Separation

ServerBench deliberately treats durable state and short-lived runtime state differently.

### PostgreSQL

Used for information that should remain available as historical application data.

### Redis

Used for information that is required during active execution and lifecycle management.

This prevents the benchmark control loop from depending exclusively on relational persistence for rapidly changing runtime information.

---

## 7. Live Updates

The frontend can receive live experiment information from the backend through server-sent events (SSE).

The conceptual path is:

```text
Benchmark Execution
       │
       ▼
Backend Runtime State
       │
       ▼
SSE Stream
       │
       ▼
React Frontend
```

This allows the UI to represent active execution without requiring constant full-page refreshes.

---

## 8. Deployment Packaging

Docker Compose packages the principal application and infrastructure services into a reproducible local environment.

The Compose environment brings together components such as:

- backend
- frontend
- PostgreSQL
- Redis
- Kafka
- benchmark agent
- Prometheus
- Grafana
- Jaeger

The goal of Phase 1 is a reproducible development/demo environment. Cloud deployment is intentionally outside the current core-project scope.

---

## 9. Architectural Principles

ServerBench follows these principles:

### Separation of Concerns

Each major subsystem has a distinct responsibility.

### Modular Execution

Benchmark implementations are kept separate from API orchestration.

### Distributed-by-Design Execution

Agent execution is separated from central application management.

### Durable vs Runtime State

PostgreSQL handles persistent data while Redis supports active runtime state.

### Observable Infrastructure

Metrics and traces are available for inspecting the system itself.

### AI as an Analysis Layer

AI assists interpretation without becoming the source of benchmark truth.

### Controlled Complexity

Infrastructure components are included because they solve a concrete system requirement.

---

## 10. Phase 1 Boundary

This document describes the **completed Phase 1 architecture**.

Security and deployment hardening are intentionally treated as a separate advancement phase. Those changes should strengthen the architecture without unnecessarily redesigning the completed core platform.
