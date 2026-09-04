package com.serverbench.backend.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.serverbench.engine.benchmark.BenchmarkMetricsSnapshot;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class ServerBenchMetrics {

    private final MeterRegistry meterRegistry;

    /*
     * Number of experiments currently executing.
     *
     * This is a global operational metric and intentionally has
     * no experiment-specific label.
     */
    private final AtomicInteger activeExperiments =
            new AtomicInteger(0);

    /*
     * Latest benchmark snapshot for each architecture/execution-mode
     * combination.
     *
     * We deliberately do NOT use experimentId as a Prometheus label.
     * Experiment IDs are high-cardinality values and should not be
     * used as metric labels.
     */
    private final ConcurrentMap<String, MetricState> metricStates =
            new ConcurrentHashMap<>();

    public ServerBenchMetrics(
            MeterRegistry meterRegistry
    ) {
        this.meterRegistry = meterRegistry;

        Gauge.builder(
                "serverbench.active.experiments",
                activeExperiments,
                AtomicInteger::get
        )
        .description(
                "Number of ServerBench experiments currently executing."
        )
        .register(meterRegistry);
    }

    /*
     * ---------------------------------------------------------------
     * EXPERIMENT LIFECYCLE
     * ---------------------------------------------------------------
     */

    public void experimentStarted() {

        activeExperiments.incrementAndGet();
    }

    public void experimentFinished() {

        activeExperiments.updateAndGet(
                current -> Math.max(0, current - 1)
        );
    }

    /*
     * ---------------------------------------------------------------
     * BENCHMARK SNAPSHOT
     * ---------------------------------------------------------------
     */

    public void updateBenchmarkMetrics(
            BenchmarkMetricsSnapshot snapshot,
            String executionMode
    ) {

        if (snapshot == null) {
            return;
        }

        String architecture =
                normalizeLabel(
                        snapshot.getServerType()
                );

        String normalizedExecutionMode =
                normalizeLabel(
                        executionMode
                );

        String stateKey =
                architecture
                        + "|"
                        + normalizedExecutionMode;

        MetricState state =
                metricStates.computeIfAbsent(
                        stateKey,
                        key -> registerMetricState(
                                architecture,
                                normalizedExecutionMode
                        )
                );

        state.update(snapshot);
    }

    /*
     * ---------------------------------------------------------------
     * REGISTER METRIC STATE
     * ---------------------------------------------------------------
     */

    private MetricState registerMetricState(
            String architecture,
            String executionMode
    ) {

        MetricState state =
                new MetricState();

        /*
         * Requests attempted
         */
        Gauge.builder(
                "serverbench.benchmark.requests.attempted",
                state,
                MetricState::getAttemptedRequests
        )
        .description(
                "Latest number of attempted benchmark requests."
        )
        .tag("architecture", architecture)
        .tag("execution_mode", executionMode)
        .register(meterRegistry);

        /*
         * Requests successful
         */
        Gauge.builder(
                "serverbench.benchmark.requests.successful",
                state,
                MetricState::getSuccessfulRequests
        )
        .description(
                "Latest number of successful benchmark requests."
        )
        .tag("architecture", architecture)
        .tag("execution_mode", executionMode)
        .register(meterRegistry);

        /*
         * Requests failed
         */
        Gauge.builder(
                "serverbench.benchmark.requests.failed",
                state,
                MetricState::getFailedRequests
        )
        .description(
                "Latest number of failed benchmark requests."
        )
        .tag("architecture", architecture)
        .tag("execution_mode", executionMode)
        .register(meterRegistry);

        /*
         * Throughput
         */
        Gauge.builder(
                "serverbench.benchmark.throughput.requests.per.second",
                state,
                MetricState::getThroughputRequestsPerSecond
        )
        .description(
                "Latest benchmark throughput in requests per second."
        )
        .tag("architecture", architecture)
        .tag("execution_mode", executionMode)
        .register(meterRegistry);

        /*
         * Average latency
         */
        Gauge.builder(
                "serverbench.benchmark.average.latency.ms",
                state,
                MetricState::getAverageLatencyMs
        )
        .description(
                "Latest average benchmark latency in milliseconds."
        )
        .tag("architecture", architecture)
        .tag("execution_mode", executionMode)
        .register(meterRegistry);

        /*
         * Elapsed benchmark time
         */
        Gauge.builder(
                "serverbench.benchmark.elapsed.time.ms",
                state,
                MetricState::getElapsedTimeMs
        )
        .description(
                "Latest elapsed benchmark time in milliseconds."
        )
        .tag("architecture", architecture)
        .tag("execution_mode", executionMode)
        .register(meterRegistry);

        return state;
    }

    private String normalizeLabel(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }

        return value.trim().toUpperCase();
    }

    /*
     * ---------------------------------------------------------------
     * MUTABLE METRIC STATE
     * ---------------------------------------------------------------
     */

    private static final class MetricState {

        private volatile double attemptedRequests;
        private volatile double successfulRequests;
        private volatile double failedRequests;
        private volatile double throughputRequestsPerSecond;
        private volatile double averageLatencyMs;
        private volatile double elapsedTimeMs;

        private void update(
                BenchmarkMetricsSnapshot snapshot
        ) {

            this.attemptedRequests =
                    snapshot.getAttemptedRequests();

            this.successfulRequests =
                    snapshot.getSuccessfulRequests();

            this.failedRequests =
                    snapshot.getFailedRequests();

            this.throughputRequestsPerSecond =
                    snapshot.getThroughputRequestsPerSecond();

            this.averageLatencyMs =
                    snapshot.getAverageLatencyMs();

            this.elapsedTimeMs =
                    snapshot.getElapsedTimeMs();
        }

        private double getAttemptedRequests() {
            return attemptedRequests;
        }

        private double getSuccessfulRequests() {
            return successfulRequests;
        }

        private double getFailedRequests() {
            return failedRequests;
        }

        private double getThroughputRequestsPerSecond() {
            return throughputRequestsPerSecond;
        }

        private double getAverageLatencyMs() {
            return averageLatencyMs;
        }

        private double getElapsedTimeMs() {
            return elapsedTimeMs;
        }
    }
}