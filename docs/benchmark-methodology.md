# ServerBench Benchmark Methodology

## 1. Purpose

ServerBench is intended to perform controlled server performance and load-testing experiments.

A benchmark is only useful when its configuration and execution conditions are clearly defined. This document describes the methodology used by the completed Phase 1 platform to structure experiments and interpret their results.

> **Important:** Benchmark results are environment-dependent. CPU, memory, JVM configuration, network conditions, target-server behavior, background processes, and other factors can affect measurements.

---

## 2. Experiment Model

A ServerBench experiment defines the conditions under which a target is benchmarked.

Core parameters include:

| Parameter | Purpose |
|---|---|
| Target host | Hostname/address of the benchmark target |
| Target port | Port exposed by the target |
| Execution mode | Selected benchmark execution strategy |
| Total requests | Fixed number of requests when request-based execution is selected |
| Measurement duration | Time window when duration-based execution is selected |
| Concurrency | Number of concurrent workload operations |
| Warm-up duration | Time allowed for the system to reach a representative operating state before measurement |
| Request timeout | Maximum waiting time for an individual request |
| Repetitions | Number of benchmark repetitions |
| Thread-pool size | Execution-pool configuration where applicable |

The backend validates the experiment configuration before execution.

---

## 3. Benchmark Lifecycle

A normal benchmark follows this sequence:

```text
1. Define target
       ↓
2. Configure workload
       ↓
3. Validate configuration
       ↓
4. Select execution architecture
       ↓
5. Create experiment
       ↓
6. Start execution
       ↓
7. Warm up
       ↓
8. Measure workload
       ↓
9. Complete repetition
       ↓
10. Persist results
       ↓
11. Repeat when configured
       ↓
12. Analyze and visualize
```

---

## 4. Warm-Up

Warm-up is used to reduce the influence of startup effects before the measurement interval.

Startup effects can include:

- JVM initialization
- class loading
- JIT compilation
- connection establishment
- cache population
- initial resource allocation

A warm-up period should therefore be considered separately from the measured benchmark interval.

---

## 5. Measurement Period

The measurement interval contains the workload observations used to produce benchmark results.

The measurement strategy should remain consistent when comparing two experiments.

For example, when comparing two execution architectures, the following should ideally remain unchanged:

```text
Target
Request pattern
Concurrency
Timeout
Warm-up
Measurement duration/request count
Repetitions
```

Only the factor under investigation should be intentionally changed.

---

## 6. Repetitions

A single benchmark run may be influenced by temporary system conditions.

Using multiple repetitions helps reveal variation across runs.

A comparison should therefore consider:

- individual repetition results
- variation between repetitions
- overall observed behavior

Repeated measurements should not automatically be treated as statistically identical.

---

## 7. Concurrency

Concurrency controls the amount of simultaneous workload activity.

For example:

```text
Concurrency = 10
```

means the benchmark attempts to maintain a workload with approximately ten concurrent execution slots, subject to the selected engine and target behavior.

Increasing concurrency can change both throughput and latency, so concurrency should be treated as an experimental variable rather than simply a measure of benchmark quality.

---

## 8. Execution Architecture

ServerBench can compare different execution approaches.

One important Phase 1 scenario is comparing traditional thread-oriented execution with virtual-thread based execution.

The methodology for a fair comparison is:

```text
Same target
Same workload
Same request timeout
Same concurrency configuration
Same warm-up
Same repetition count
Different execution architecture
```

This isolates the client-side execution model as the primary experimental variable.

---

## 9. Metrics and Results

The benchmark result should be interpreted using the measurements collected by the platform.

Useful measurements include:

- request count
- successful operations
- failed operations
- execution duration
- throughput-related values
- latency-related values
- repetition-level results
- execution architecture
- relevant runtime/environment metadata

The repository's actual result model remains the authoritative source for the fields exposed by the application.

---

## 10. Example Experiment

A representative experiment can be documented as:

```text
Target:
<host>:<port>

Execution Architecture:
Virtual Threads

Concurrency:
100

Requests:
10,000

Warm-up:
5 seconds

Timeout:
5 seconds

Repetitions:
3
```

The exact values should be replaced by the real experiment being demonstrated.

---

## 11. Comparing Results

When comparing two benchmark configurations, avoid relying on a single metric.

For example:

```text
Configuration A
    Throughput
    Latency
    Success / failure
    Execution time

Configuration B
    Throughput
    Latency
    Success / failure
    Execution time
```

A meaningful comparison considers the complete observed behavior.

A change in throughput should be interpreted together with latency, failure rate, workload size, and execution conditions.

---

## 12. Environment Considerations

Benchmark results can be affected by:

### Hardware

- CPU model
- number of available processors
- memory capacity
- background workload

### JVM

- Java version
- JVM runtime
- heap configuration
- JIT behavior
- garbage collection activity

### Network

- network latency
- bandwidth
- connection stability
- target location
- local vs remote execution

### Target Server

- application implementation
- database load
- cache state
- server-side concurrency limits
- resource saturation

Therefore, two runs on different machines should not automatically be treated as directly equivalent.

---

## 13. Reproducibility

For a reproducible benchmark:

1. use the same target
2. keep workload parameters explicit
3. use a consistent warm-up period
4. keep repetition counts consistent
5. record environment metadata
6. avoid unnecessary background workloads
7. document any intentional configuration differences

Docker Compose is used by ServerBench to make the supporting development environment easier to reproduce.

---

## 14. Distributed Benchmark Methodology

For distributed execution, an additional variable is introduced: the execution topology.

A distributed experiment should document:

```text
Number of agents
Agent assignment
Target
Workload parameters
Execution architecture
Repetitions
Messaging/infrastructure conditions
```

The objective is to understand how workload generation behaves when execution is separated across benchmark agents.

---

## 15. AI-Assisted Analysis

AI-assisted analysis is applied after benchmark results are available.

The recommended conceptual flow is:

```text
Measured Results
      ↓
Structured Analysis
      ↓
Performance Findings
      ↓
AI-Assisted Interpretation
```

The AI layer should be treated as an interpretation and summarization mechanism, not as a replacement for measured data.

---

## 16. Reporting

A benchmark report should contain enough information to reconstruct the experiment.

At minimum:

```text
Experiment name
Target
Execution architecture
Concurrency
Workload size
Warm-up
Timeout
Repetitions
Observed results
Environment information
Analysis
```

---

## 17. Phase 1 Methodology Boundary

This methodology documents the completed core benchmarking platform.

Security controls, production network policies, deployment hardening, and cloud-specific operational practices belong to the later Phase 2 strengthening work.
