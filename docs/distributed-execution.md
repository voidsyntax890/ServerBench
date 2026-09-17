# ServerBench Distributed Execution

## 1. Purpose

ServerBench supports distributed benchmark execution through dedicated benchmark agents.

The purpose of this architecture is to separate the system's central control responsibilities from the workload-execution responsibilities.

> **Phase 1 distributed execution foundation is complete.**  
> Security and deployment hardening for distributed operation are part of Phase 2.

---

## 2. Why Distributed Execution?

A single benchmark process can become the limiting factor when workload generation is increased.

Distributed execution allows workload generation to be performed by multiple independent agents.

Conceptually:

```text
                  Central Backend
                        │
                        ▼
                      Kafka
               ┌────────┼────────┐
               ▼        ▼        ▼
             Agent 1  Agent 2  Agent N
               │        │        │
               └────────┼────────┘
                        ▼
               Benchmark Execution
                        │
                        ▼
                  Target Server
```

The architecture therefore allows execution capacity to be separated from the central management application.

---

## 3. Components

### Backend

The backend acts as the orchestration and management layer.

It is responsible for:

- experiment lifecycle
- validation
- execution coordination
- persistent experiment state
- distributed execution coordination
- communicating status to the frontend

---

### Kafka

Kafka acts as the messaging layer between orchestration and agent execution.

The key architectural benefit is decoupling.

Instead of requiring the backend to hold direct synchronous execution calls to every agent, work can be communicated through the messaging layer.

---

### Agent

An agent is an execution worker.

Its responsibilities include:

- receiving assigned benchmark work
- preparing execution
- invoking the benchmark engine
- producing execution-related output/state
- participating in the distributed execution lifecycle

An agent should not be treated as a second central backend.

---

### Benchmark Engine

The agent delegates actual workload generation to the benchmark engine.

Therefore:

```text
Agent
  │
  ▼
Benchmark Engine
  │
  ▼
Target
```

The agent provides the distributed execution boundary while the engine performs the benchmark work.

---

## 4. Execution Flow

A conceptual distributed execution flow is:

```text
1. User configures experiment
          ↓
2. Frontend sends request
          ↓
3. Backend validates configuration
          ↓
4. Backend creates experiment/run state
          ↓
5. Backend determines distributed execution
          ↓
6. Work is published through Kafka
          ↓
7. Eligible agent receives work
          ↓
8. Agent invokes benchmark execution
          ↓
9. Benchmark engine generates workload
          ↓
10. Target responds
          ↓
11. Agent reports execution state/results
          ↓
12. Backend updates experiment/run state
          ↓
13. Results are persisted
          ↓
14. Frontend receives live/final status
```

The exact internal event/message contracts are maintained in the shared distributed-contracts module.

---

## 5. Shared Contracts

The project includes a dedicated distributed-contracts module.

The purpose is to keep the data exchanged between distributed components explicit and consistent.

This avoids duplicating the same protocol model independently in the backend and agents.

Conceptually:

```text
server-distributed-contracts
          │
      ┌───┴────┐
      ▼        ▼
  Backend     Agent
```

---

## 6. Agent Registration

Agents participate in the system through an agent registration mechanism.

The registration process gives the backend information about an execution worker before that worker is used for distributed workload execution.

This allows the orchestration layer to reason about available agents separately from end-user experiment configuration.

---

## 7. Agent Selection

Distributed execution introduces an eligibility problem:

> Which agent should execute a given piece of benchmark work?

The control plane therefore has to account for the availability and suitability of agents when assigning work.

The exact selection behavior is implemented by the current distributed execution services.

---

## 8. Distributed State

An experiment can have both overall experiment state and lower-level distributed execution state.

Conceptually:

```text
Experiment
   │
   ├── Overall lifecycle
   │
   └── Benchmark runs
          │
          ├── Run 1
          │      └── distributed execution information
          │
          ├── Run 2
          │      └── distributed execution information
          │
          └── ...
```

This separation is useful because an individual distributed job can fail or change state without necessarily representing the entire experiment lifecycle.

---

## 9. Kafka Decoupling

The messaging path can be visualized as:

```text
Backend
  │
  │ publish work
  ▼
Kafka Topic
  │
  ├───────────────┐
  ▼               ▼
Agent 1          Agent 2
  │               │
  ▼               ▼
Execute          Execute
```

The backend does not need to implement the benchmark logic itself.

Kafka therefore forms an architectural boundary:

```text
Orchestration responsibility
            │
            ▼
         Kafka
            │
            ▼
Execution responsibility
```

---

## 10. Failure Considerations

Distributed systems require failures to be treated as expected operational events.

Relevant failure cases include:

### Agent unavailable

An agent may stop responding or become unavailable.

### Message delivery issues

A message may not be processed as expected.

### Benchmark execution failure

An agent may successfully receive work but fail while executing it.

### Target failure

The benchmark target itself may be unavailable or may return failures.

### Persistence failure

The backend may encounter a problem while storing execution information.

The platform's execution lifecycle should preserve the distinction between these classes of failure rather than presenting all failures as the same event.

---

## 11. Why Not Direct HTTP Between Backend and Agents?

A direct design could look like:

```text
Backend ─────HTTP─────► Agent
```

This is simple but increases coupling between the central backend and individual workers.

The current architecture instead uses:

```text
Backend ───► Kafka ───► Agent
```

The messaging layer provides a more natural boundary for distributing asynchronous execution work.

---

## 12. Local vs Distributed Execution

ServerBench can support different execution architectures.

### Local

```text
Frontend
   ↓
Backend
   ↓
Benchmark Engine
   ↓
Target
```

### Distributed

```text
Frontend
   ↓
Backend
   ↓
Kafka
   ↓
Agent
   ↓
Benchmark Engine
   ↓
Target
```

The user-facing experiment concept can therefore remain similar while the execution topology changes underneath it.

---

## 13. Observability of Distributed Execution

Distributed execution benefits from both metrics and traces.

The project's observability stack provides:

```text
Metrics
Backend / Agents
    ↓
Prometheus
    ↓
Grafana

Tracing
Backend / Agent execution
    ↓
OpenTelemetry
    ↓
Jaeger
```

This helps distinguish application-level issues from distributed execution delays.

---

## 14. Persistence of Distributed Results

The durable benchmark record is maintained by the backend and persisted in PostgreSQL.

Runtime information can be maintained separately through Redis where appropriate.

This results in:

```text
Agent execution
      │
      ▼
Backend
 ┌────┴─────────┐
 ▼              ▼
Redis       PostgreSQL
Runtime      Durable
state        history
```

---

## 15. Phase 1 Boundary

The Phase 1 implementation establishes the distributed execution foundation.

Phase 2 will strengthen this foundation with controls that are specifically useful for deployment and secure operation, such as authenticated agent identity, credential rotation/revocation, tighter authorization, operational limits, and deployment-specific hardening.

Those enhancements should build on the completed architecture rather than unnecessarily replacing it.
