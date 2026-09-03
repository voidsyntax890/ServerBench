package com.serverbench.engine.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class BenchmarkRunner {

    private final BenchmarkConfig config;
    private final String serverType;
    private final Consumer<BenchmarkMetricsSnapshot> snapshotListener;

    public BenchmarkRunner(
            BenchmarkConfig config,
            String serverType
    ) {
        this(
                config,
                serverType,
                null
        );
    }

    public BenchmarkRunner(
            BenchmarkConfig config,
            String serverType,
            Consumer<BenchmarkMetricsSnapshot> snapshotListener
    ) {
        this.config = config;
        this.serverType = serverType;
        this.snapshotListener = snapshotListener;
    }

    public BenchmarkResult run() {

        // ============================================================
        // PHASE 1: CONTROLLED WARM-UP
        // ============================================================

        runWarmup();

        // ============================================================
        // PHASE 2: METRICS
        // ============================================================

        AtomicInteger attemptedRequests =
                new AtomicInteger(0);

        AtomicInteger successfulRequests =
                new AtomicInteger(0);

        AtomicInteger failedRequests =
                new AtomicInteger(0);

        AtomicInteger connectTimeouts =
                new AtomicInteger(0);

        AtomicInteger connectionRefused =
                new AtomicInteger(0);

        AtomicInteger connectionResets =
                new AtomicInteger(0);

        AtomicInteger readTimeouts =
                new AtomicInteger(0);

        AtomicInteger noResponseFailures =
                new AtomicInteger(0);

        AtomicInteger otherIoFailures =
                new AtomicInteger(0);

        AtomicInteger diagnosticFailureCount =
                new AtomicInteger(0);

        AtomicLong totalLatencyMs =
                new AtomicLong(0);

        AtomicLong minLatencyMs =
                new AtomicLong(Long.MAX_VALUE);

        AtomicLong maxLatencyMs =
                new AtomicLong(0);

        List<Long> latencies =
                Collections.synchronizedList(
                        new ArrayList<>()
                );

        // ============================================================
        // MEASUREMENT START
        // ============================================================

        long measurementStartTime =
                System.nanoTime();

        long measurementDurationNs =
                TimeUnit.MILLISECONDS.toNanos(
                        config.getMeasurementDurationMs()
                );

        long measurementDeadline =
                measurementStartTime
                        + measurementDurationNs;

        AtomicInteger requestCounter =
                new AtomicInteger(0);

        ScheduledExecutorService snapshotExecutor =
                null;

        // ============================================================
        // LIVE SNAPSHOT REPORTER
        // ============================================================

        if (snapshotListener != null) {

            snapshotExecutor =
                    Executors.newSingleThreadScheduledExecutor();

            ScheduledExecutorService finalSnapshotExecutor =
                    snapshotExecutor;

            finalSnapshotExecutor.scheduleAtFixedRate(
                    () -> publishSnapshot(
                            measurementStartTime,
                            attemptedRequests,
                            successfulRequests,
                            failedRequests,
                            totalLatencyMs
                    ),
                    0L,
                    500L,
                    TimeUnit.MILLISECONDS
            );
        }

        // ============================================================
        // WORKER EXECUTOR
        // ============================================================

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        config.getConcurrency()
                );

        // ============================================================
        // START WORKERS
        // ============================================================

        for (int i = 0;
             i < config.getConcurrency();
             i++) {

            executor.submit(() -> {

                runWorker(
                        measurementDeadline,
                        requestCounter,
                        attemptedRequests,
                        successfulRequests,
                        failedRequests,
                        connectTimeouts,
                        connectionRefused,
                        connectionResets,
                        readTimeouts,
                        noResponseFailures,
                        otherIoFailures,
                        diagnosticFailureCount,
                        totalLatencyMs,
                        minLatencyMs,
                        maxLatencyMs,
                        latencies
                );
            });
        }

        executor.shutdown();

        // ============================================================
        // WAIT FOR WORKERS
        // ============================================================

        try {

            long awaitTimeMs;

            if (config.getExecutionMode()
                    == ExecutionMode.DURATION) {

                awaitTimeMs =
                        config.getMeasurementDurationMs()
                                + config.getRequestTimeoutMs()
                                + 5000L;

            } else {

                awaitTimeMs =
                        Math.max(
                                10000L,
                                config.getRequestTimeoutMs()
                                        + 5000L
                        );
            }

            boolean terminated =
                    executor.awaitTermination(
                            awaitTimeMs,
                            TimeUnit.MILLISECONDS
                    );

            if (!terminated) {

                executor.shutdownNow();

                executor.awaitTermination(
                        5000L,
                        TimeUnit.MILLISECONDS
                );
            }

        } catch (InterruptedException e) {

            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        // ============================================================
        // STOP LIVE SNAPSHOT REPORTER
        // ============================================================

        if (snapshotExecutor != null) {

            snapshotExecutor.shutdownNow();
        }

        // ============================================================
        // MEASUREMENT DURATION
        // ============================================================

        long measurementEndTime =
                System.nanoTime();

        long elapsedTimeMs =
                TimeUnit.NANOSECONDS.toMillis(
                        measurementEndTime
                                - measurementStartTime
                );

        long totalDurationMs;

        if (config.getExecutionMode()
                == ExecutionMode.DURATION) {

            totalDurationMs =
                    config.getMeasurementDurationMs();

        } else {

            totalDurationMs =
                    Math.max(
                            1,
                            elapsedTimeMs
                    );
        }

        // ============================================================
        // FINAL METRICS
        // ============================================================

        int attempted =
                attemptedRequests.get();

        int successful =
                successfulRequests.get();

        int failed =
                failedRequests.get();

        double averageLatencyMs =
                successful == 0
                        ? 0
                        : (double) totalLatencyMs.get()
                        / successful;

        long minimumLatency =
                successful == 0
                        ? 0
                        : minLatencyMs.get();

        long maximumLatency =
                successful == 0
                        ? 0
                        : maxLatencyMs.get();

        double throughput =
                successful == 0
                        ? 0
                        : (double) successful
                        / (totalDurationMs / 1000.0);

        double successRate =
                attempted == 0
                        ? 0
                        : (double) successful
                        / attempted
                        * 100.0;

        double errorRate =
                attempted == 0
                        ? 0
                        : (double) failed
                        / attempted
                        * 100.0;

        // ============================================================
        // FINAL SNAPSHOT
        // ============================================================

        publishSnapshot(
                measurementStartTime,
                attemptedRequests,
                successfulRequests,
                failedRequests,
                totalLatencyMs
        );

        // ============================================================
        // PERCENTILES
        // ============================================================

        List<Long> sortedLatencies;

        synchronized (latencies) {

            sortedLatencies =
                    new ArrayList<>(latencies);
        }

        Collections.sort(sortedLatencies);

        double p50 =
                calculatePercentile(
                        sortedLatencies,
                        50
                );

        double p95 =
                calculatePercentile(
                        sortedLatencies,
                        95
                );

        double p99 =
                calculatePercentile(
                        sortedLatencies,
                        99
                );

        // ============================================================
        // FINAL RESULT
        // ============================================================

        return new BenchmarkResult(
                serverType,
                attempted,
                successful,
                failed,
                totalDurationMs,
                averageLatencyMs,
                throughput,
                successRate,
                errorRate,
                minimumLatency,
                maximumLatency,
                p50,
                p95,
                p99,
                connectTimeouts.get(),
                connectionRefused.get(),
                connectionResets.get(),
                readTimeouts.get(),
                noResponseFailures.get(),
                otherIoFailures.get()
        );
    }

    // ================================================================
    // LIVE METRICS SNAPSHOT
    // ================================================================

    private void publishSnapshot(
            long measurementStartTime,
            AtomicInteger attemptedRequests,
            AtomicInteger successfulRequests,
            AtomicInteger failedRequests,
            AtomicLong totalLatencyMs
    ) {

        if (snapshotListener == null) {
            return;
        }

        long elapsedTimeMs =
                TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime()
                                - measurementStartTime
                );

        int attempted =
                attemptedRequests.get();

        int successful =
                successfulRequests.get();

        int failed =
                failedRequests.get();

        double throughput =
                elapsedTimeMs <= 0
                        ? 0.0
                        : successful
                        / (elapsedTimeMs / 1000.0);

        double averageLatency =
                successful == 0
                        ? 0.0
                        : (double) totalLatencyMs.get()
                        / successful;

        BenchmarkMetricsSnapshot snapshot =
                new BenchmarkMetricsSnapshot(
                        serverType,
                        attempted,
                        successful,
                        failed,
                        throughput,
                        averageLatency,
                        elapsedTimeMs
                );

        try {

            snapshotListener.accept(
                    snapshot
            );

        } catch (RuntimeException ignored) {

            /*
             * Live reporting must never affect
             * benchmark execution.
             */
        }
    }

    // ================================================================
    // WORKER
    // ================================================================

    private void runWorker(
            long measurementDeadline,
            AtomicInteger requestCounter,
            AtomicInteger attemptedRequests,
            AtomicInteger successfulRequests,
            AtomicInteger failedRequests,
            AtomicInteger connectTimeouts,
            AtomicInteger connectionRefused,
            AtomicInteger connectionResets,
            AtomicInteger readTimeouts,
            AtomicInteger noResponseFailures,
            AtomicInteger otherIoFailures,
            AtomicInteger diagnosticFailureCount,
            AtomicLong totalLatencyMs,
            AtomicLong minLatencyMs,
            AtomicLong maxLatencyMs,
            List<Long> latencies
    ) {

        try (
                Socket socket = new Socket()
        ) {

            // --------------------------------------------------------
            // ESTABLISH ONE CONNECTION FOR THIS WORKER
            // --------------------------------------------------------

            try {

                socket.connect(
                        new InetSocketAddress(
                                config.getHost(),
                                config.getPort()
                        ),
                        config.getRequestTimeoutMs()
                );

                socket.setSoTimeout(
                        config.getRequestTimeoutMs()
                );

            } catch (SocketTimeoutException e) {

                connectTimeouts.incrementAndGet();

                printDiagnosticFailure(
                        diagnosticFailureCount,
                        e
                );

                return;

            } catch (ConnectException e) {

                connectionRefused.incrementAndGet();

                printDiagnosticFailure(
                        diagnosticFailureCount,
                        e
                );

                return;

            } catch (SocketException e) {

                connectionResets.incrementAndGet();

                printDiagnosticFailure(
                        diagnosticFailureCount,
                        e
                );

                return;

            } catch (IOException e) {

                otherIoFailures.incrementAndGet();

                printDiagnosticFailure(
                        diagnosticFailureCount,
                        e
                );

                return;
            }

            try (
                    PrintWriter writer =
                            new PrintWriter(
                                    socket.getOutputStream(),
                                    true
                            );

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            socket.getInputStream()
                                    )
                            )
            ) {

                while (true) {

                    // ------------------------------------------------
                    // DURATION MODE
                    // ------------------------------------------------

                    if (config.getExecutionMode()
                            == ExecutionMode.DURATION) {

                        if (System.nanoTime()
                                >= measurementDeadline) {

                            break;
                        }
                    }

                    // ------------------------------------------------
                    // REQUEST MODE
                    // ------------------------------------------------

                    int requestNumber =
                            requestCounter
                                    .getAndIncrement();

                    if (config.getExecutionMode()
                            == ExecutionMode.REQUESTS) {

                        if (requestNumber
                                >= config.getTotalRequests()) {

                            break;
                        }
                    }

                    attemptedRequests
                            .incrementAndGet();

                    long requestStartTime =
                            System.nanoTime();

                    try {

                        writer.println(
                                "Hello from ServerBench Benchmark"
                        );

                        String response =
                                reader.readLine();

                        long requestEndTime =
                                System.nanoTime();

                        long latencyMs =
                                TimeUnit.NANOSECONDS.toMillis(
                                        requestEndTime
                                                - requestStartTime
                                );

                        if (response != null) {

                            successfulRequests
                                    .incrementAndGet();

                            totalLatencyMs
                                    .addAndGet(
                                            latencyMs
                                    );

                            minLatencyMs
                                    .accumulateAndGet(
                                            latencyMs,
                                            Math::min
                                    );

                            maxLatencyMs
                                    .accumulateAndGet(
                                            latencyMs,
                                            Math::max
                                    );

                            latencies.add(
                                    latencyMs
                            );

                        } else {

                            noResponseFailures
                                    .incrementAndGet();

                            failedRequests
                                    .incrementAndGet();

                            printDiagnosticFailure(
                                    diagnosticFailureCount,
                                    null
                            );

                            break;
                        }

                    } catch (SocketTimeoutException e) {

                        readTimeouts.incrementAndGet();

                        failedRequests
                                .incrementAndGet();

                        printDiagnosticFailure(
                                diagnosticFailureCount,
                                e
                        );

                        break;

                    } catch (SocketException e) {

                        connectionResets.incrementAndGet();

                        failedRequests
                                .incrementAndGet();

                        printDiagnosticFailure(
                                diagnosticFailureCount,
                                e
                        );

                        break;

                    } catch (IOException e) {

                        otherIoFailures.incrementAndGet();

                        failedRequests
                                .incrementAndGet();

                        printDiagnosticFailure(
                                diagnosticFailureCount,
                                e
                        );

                        break;
                    }
                }
            }

        } catch (IOException ignored) {

            /*
             * Socket cleanup errors after the worker has finished
             * are not counted as benchmark request failures.
             */
        }
    }

    // ================================================================
    // CONTROLLED WARM-UP
    // ================================================================

    private void runWarmup() {

        long warmupDurationMs =
                config.getWarmupDurationMs();

        if (warmupDurationMs <= 0) {

            System.out.println(
                    "Warm-up disabled."
            );

            return;
        }

        int warmupRequestLimit =
                Math.min(
                        Math.max(
                                config.getTotalRequests(),
                                config.getConcurrency() * 5
                        ),
                        config.getConcurrency() * 10
                );

        System.out.println(
                "Starting warm-up for "
                        + warmupDurationMs
                        + " ms..."
        );

        System.out.println(
                "Warm-up request limit: "
                        + warmupRequestLimit
        );

        AtomicInteger warmupRequests =
                new AtomicInteger(0);

        long warmupDeadline =
                System.nanoTime()
                        + TimeUnit.MILLISECONDS.toNanos(
                                warmupDurationMs
                        );

        ExecutorService warmupExecutor =
                Executors.newFixedThreadPool(
                        config.getConcurrency()
                );

        for (int i = 0;
             i < config.getConcurrency();
             i++) {

            warmupExecutor.submit(() -> {

                while (true) {

                    if (System.nanoTime()
                            >= warmupDeadline) {

                        break;
                    }

                    int requestNumber =
                            warmupRequests
                                    .getAndIncrement();

                    if (requestNumber
                            >= warmupRequestLimit) {

                        break;
                    }

                    performWarmupRequest();
                }
            });
        }

        warmupExecutor.shutdown();

        try {

            warmupExecutor.awaitTermination(
                    warmupDurationMs
                            + config.getRequestTimeoutMs()
                            + 1000L,
                    TimeUnit.MILLISECONDS
            );

        } catch (InterruptedException e) {

            warmupExecutor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        System.out.println(
                "Warm-up completed. Requests sent: "
                        + Math.min(
                                warmupRequests.get(),
                                warmupRequestLimit
                        )
        );
    }

    // ================================================================
    // WARM-UP REQUEST
    // ================================================================

    private void performWarmupRequest() {

        try (
                Socket socket = new Socket()
        ) {

            socket.connect(
                    new InetSocketAddress(
                            config.getHost(),
                            config.getPort()
                    ),
                    config.getRequestTimeoutMs()
            );

            socket.setSoTimeout(
                    config.getRequestTimeoutMs()
            );

            try (
                    PrintWriter writer =
                            new PrintWriter(
                                    socket.getOutputStream(),
                                    true
                            );

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            socket.getInputStream()
                                    )
                            )
            ) {

                writer.println(
                        "Hello from ServerBench Warmup"
                );

                reader.readLine();
            }

        } catch (IOException ignored) {

            // Warm-up failures do not affect benchmark metrics.
        }
    }

    // ================================================================
    // DIAGNOSTIC OUTPUT
    // ================================================================

    private void printDiagnosticFailure(
            AtomicInteger diagnosticFailureCount,
            IOException exception
    ) {

        int diagnosticNumber =
                diagnosticFailureCount
                        .incrementAndGet();

        if (diagnosticNumber > 10) {
            return;
        }

        System.err.println();

        System.err.println(
                "[Benchmark Diagnostic] Failure #"
                        + diagnosticNumber
        );

        if (exception == null) {

            System.err.println(
                    "Failure Type: No Response"
            );

        } else {

            System.err.println(
                    "Exception Type: "
                            + exception.getClass().getName()
            );

            System.err.println(
                    "Message: "
                            + exception.getMessage()
            );
        }

        System.err.println();
    }

    // ================================================================
    // PERCENTILE CALCULATION
    // ================================================================

    private double calculatePercentile(
            List<Long> sortedLatencies,
            double percentile
    ) {

        if (sortedLatencies.isEmpty()) {
            return 0;
        }

        double rank =
                (percentile / 100.0)
                        * (sortedLatencies.size() - 1);

        int lowerIndex =
                (int) Math.floor(rank);

        int upperIndex =
                (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {

            return sortedLatencies
                    .get(lowerIndex);
        }

        double lowerValue =
                sortedLatencies
                        .get(lowerIndex);

        double upperValue =
                sortedLatencies
                        .get(upperIndex);

        double fraction =
                rank - lowerIndex;

        return lowerValue
                + fraction
                * (upperValue - lowerValue);
    }
}