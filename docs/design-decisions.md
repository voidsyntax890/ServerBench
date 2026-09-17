# ServerBench Design Decisions

## 1. Purpose

This document records important engineering decisions made while developing ServerBench.

The objective is not to list every implementation detail, but to explain **why the major technologies and architectural boundaries exist**.

> **Phase 1 is the completed core platform. Phase 2 security and deployment work is intentionally separate.**

---

## 2. Why Java?

Java is used for the backend, benchmark engine, and distributed execution components.

The choice supports the project's focus on:

- concurrency
- server-side application development
- object-oriented design
- mature networking libraries
- JVM observability
- modern concurrency through virtual threads

Java also allows the benchmark engine and backend ecosystem to share a common language and runtime family.

---

## 3. Why Spring Boot?

Spring Boot provides the application foundation for the backend.

It simplifies the implementation of:

- REST APIs
- dependency injection
- application configuration
- data access integration
- lifecycle management
- observability integration
- service-layer architecture

The backend is therefore organized around familiar controller, service, repository, entity, validation, and integration boundaries.

---

## 4. Why Keep the Benchmark Engine Separate?

The benchmark engine is separated from the web/backend layer.

This prevents the API layer from becoming tightly coupled to specific workload-execution implementations.

Conceptually:

```text
Backend
  │
  │ Experiment / execution request
  ▼
Benchmark Engine
  │
  ├── Execution strategy A
  ├── Execution strategy B
  └── Additional strategies
```

This is important because ServerBench is itself intended to study execution behavior.

---

## 5. Why Virtual Threads?

Virtual threads are included because high-concurrency workload generation is a core subject of the project.

Virtual threads provide a modern Java concurrency model that can be evaluated against traditional thread-oriented execution.

The project therefore treats virtual threads as an experimental execution architecture rather than as a marketing feature.

A meaningful comparison requires keeping the workload conditions consistent while changing the execution strategy.

---

## 6. Why Kafka?

Kafka is used for distributed benchmark messaging.

A direct synchronous design could couple the backend to every benchmark agent:

```text
Backend ─── synchronous request ───► Agent
```

The distributed design instead introduces messaging:

```text
Backend
   │
   ▼
 Kafka
   │
   ├──► Agent 1
   ├──► Agent 2
   └──► Agent N
```

This provides a clearer separation between orchestration and distributed execution.

Kafka is particularly useful when benchmark execution work needs to be distributed independently of the central API process.

---

## 7. Why Redis?

Redis is used for active runtime state.

The design separates:

```text
Fast-changing execution state
        ↓
      Redis

Durable historical application data
        ↓
    PostgreSQL
```

This keeps the active execution path conceptually separate from long-lived historical persistence.

---

## 8. Why PostgreSQL?

PostgreSQL is used for durable structured data.

Benchmarking produces information that should remain queryable after an execution has completed.

A relational database is appropriate for:

- experiments
- benchmark runs
- execution metadata
- historical result relationships
- structured application state

The application uses JPA/Spring Data to interact with this persistence layer.

---

## 9. Why React?

React is used for the frontend because ServerBench requires an interactive interface rather than a static page.

The frontend needs to:

- configure experiments
- observe experiment state
- display benchmark results
- visualize data
- present performance analysis

Vite provides the frontend development/build tooling.

---

## 10. Why Recharts?

Charts are important because benchmark output is inherently measurement-oriented.

A chart can communicate trends in:

- latency
- throughput
- repetition results
- comparative performance

Recharts provides a practical React-native approach to rendering those visualizations.

---

## 11. Why Server-Sent Events?

Benchmark execution can take long enough that the user interface should not have to wait for a single blocking HTTP response.

SSE provides a simple server-to-client live update mechanism:

```text
Backend
   │
   │ event stream
   ▼
Frontend
```

This is appropriate for presenting active experiment progress and status without introducing a more complex real-time transport layer.

---

## 12. Why Prometheus?

Prometheus is used for metrics-oriented monitoring.

Metrics answer questions such as:

- How is the backend behaving?
- What is the observed runtime/resource behavior?
- Are application components generating expected telemetry?

Prometheus is therefore part of the observability rather than the benchmark business-data persistence layer.

---

## 13. Why OpenTelemetry?

Distributed execution makes tracing useful because one logical operation can cross multiple application boundaries.

OpenTelemetry provides a standard instrumentation and trace model.

The conceptual path is:

```text
Backend / Agent execution
          │
          ▼
   OpenTelemetry
          │
          ▼
        Jaeger
```

This helps inspect distributed execution flow rather than relying only on logs.

---

## 14. Why Grafana?

Prometheus stores metrics, while Grafana provides an interface for exploring and visualizing them.

The separation allows:

```text
Metric Collection → Prometheus
Metric Visualization → Grafana
```

This keeps monitoring concerns independent from the main application UI.

---

## 15. Why Jaeger?

Jaeger is used to inspect distributed traces generated through the observability stack.

It is especially useful when the question is:

> Where did time go across the execution path?

Metrics can show that a problem exists; traces can help show where in the request/execution path it occurred.

---

## 16. Why Spring AI?

The AI layer is intended to help interpret benchmark findings.

A deterministic benchmark should still produce measured results without AI.

Therefore the architecture deliberately places AI after the measurement and analysis stages:

```text
Benchmark
   ↓
Measured Results
   ↓
Analysis
   ↓
Spring AI
```

This keeps AI from becoming a hidden source of benchmark measurements.

---

## 17. Why Docker Compose?

ServerBench depends on several infrastructure services.

A manual installation of every dependency would make the project harder to reproduce.

Docker Compose provides a repeatable local environment containing the principal services used by the platform.

This is especially useful for demonstrations, development, and integration testing.

---

## 18. Why GitHub Actions?

GitHub Actions provides automated verification whenever code changes are pushed or proposed through pull requests.

The CI workflow acts as a quality guardrail by validating the project rather than restricting future architecture decisions.

The project can therefore continue evolving while using CI to catch accidental regressions.

---

## 19. Why Not Automatically Build Docker Images on Every Commit?

Docker image creation is treated as a deliberate checkpoint rather than a mandatory operation for every small source-code change.

The reasoning is practical:

- source-code correctness should be validated continuously
- Docker packaging is more meaningful at stable checkpoints
- unnecessary image builds increase CI work
- manual checkpoint builds remain easy to perform before releases

---

## 20. Why Keep Cloud Deployment Separate?

The core project can be developed and demonstrated locally without immediately introducing cloud-specific complexity.

The intended progression is:

```text
Completed Core Platform
          ↓
Security / Deployment Strengthening
          ↓
Deployment-Ready Configuration
          ↓
Future Cloud Deployment
```

This keeps Phase 1 focused and avoids introducing cloud services before they are actually required.

---

## 21. Why Separate Phase 1 and Phase 2?

The project reached a stable core architecture before the security/deployment enhancement work began.

This creates a useful engineering boundary:

```text
Phase 1
Core platform
Benchmarking
Distributed execution
Observability
Persistence
Frontend
AI analysis
Docker
CI

Phase 2
Security hardening
Deployment hardening
Final validation
```

This allows the completed platform to remain the stable baseline while security and deployment changes are evaluated independently.

---

## 22. Engineering Principle

A recurring principle throughout ServerBench is:

> **Add a technology when it solves a real architectural problem, not merely because the technology is popular.**

This is particularly important for distributed systems, observability, security, and deployment infrastructure.

---

## 23. Current Boundary

These decisions describe the completed Phase 1 implementation and its architectural reasoning.

Security controls and deployment-specific hardening are being evaluated separately in Phase 2 according to their concrete usefulness to ServerBench.
