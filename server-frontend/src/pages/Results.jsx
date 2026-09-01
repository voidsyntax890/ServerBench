import {
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    getExperimentResults,
    getExperiment,
} from "../services/experimentApi";

import "./Results.css";

const architectureNames = {
    SINGLE_THREADED: "Single Threaded",
    MULTI_THREADED: "Multi Threaded",
    THREAD_POOL: "Thread Pool",
    VIRTUAL_THREAD: "Virtual Thread",
};

function StatusBadge({ status }) {
    const safeStatus =
        status || "UNKNOWN";

    return (
        <span
            className={`results-status results-status-${safeStatus.toLowerCase()}`}
        >
            <span className="results-status-dot" />
            {safeStatus}
        </span>
    );
}

function formatNumber(value) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "—";
    }

    return number.toLocaleString(
        undefined,
        {
            maximumFractionDigits: 2,
        }
    );
}

function formatDecimal(
    value,
    digits = 4
) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "—";
    }

    return number.toFixed(digits);
}

function formatLatency(value) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "—";
    }

    return `${number.toFixed(4)} ms`;
}

function formatDateTime(value) {
    if (!value) {
        return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString(
        undefined,
        {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        }
    );
}

function MetricBox({
    label,
    value,
    helper,
}) {
    return (
        <div className="result-metric-box">
            <span>{label}</span>

            <strong>{value}</strong>

            {helper && (
                <small>{helper}</small>
            )}
        </div>
    );
}

function Results({
    experimentId,
    onBack,
    onComparison,
}) {
    const [result, setResult] =
        useState(null);

    const [experiment, setExperiment] =
        useState(null);

    const [isLoading, setIsLoading] =
        useState(true);

    const [loadError, setLoadError] =
        useState("");

    useEffect(() => {
        let mounted = true;

        async function loadResults() {
            if (!experimentId) {
                if (mounted) {
                    setResult(null);
                    setExperiment(null);
                    setLoadError(
                        "No experiment was selected."
                    );
                    setIsLoading(false);
                }

                return;
            }

            setIsLoading(true);
            setLoadError("");

            try {
                const [
                    resultsResponse,
                    experimentResponse,
                ] = await Promise.all([
                    getExperimentResults(
                        experimentId
                    ),
                    getExperiment(
                        experimentId
                    ),
                ]);

                if (mounted) {
                    setResult(
                        resultsResponse
                    );
                    setExperiment(
                        experimentResponse
                    );
                }
            } catch (error) {
                if (mounted) {
                    setResult(null);
                    setExperiment(null);

                    setLoadError(
                        error.message ||
                            "Unable to load experiment results."
                    );
                }
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        }

        loadResults();

        return () => {
            mounted = false;
        };
    }, [experimentId]);

    const runs = useMemo(() => {
        if (!Array.isArray(result?.runs)) {
            return [];
        }

        return result.runs;
    }, [result]);

    const overallMetrics =
        useMemo(() => {
            let successfulRequests = 0;
            let failedRequests = 0;
            let totalDuration = 0;

            for (const run of runs) {
                const benchmark =
                    run.benchmarkResult;

                if (!benchmark) {
                    continue;
                }

                successfulRequests +=
                    Number(
                        benchmark.successfulRequests ||
                            0
                    );

                failedRequests +=
                    Number(
                        benchmark.failedRequests ||
                            0
                    );

                totalDuration +=
                    Number(
                        benchmark.totalDurationMs ||
                            0
                    );
            }

            const totalRequests =
                successfulRequests +
                failedRequests;

            const successRate =
                totalRequests === 0
                    ? 0
                    : (
                          (successfulRequests /
                              totalRequests) *
                          100
                      );

            return {
                successfulRequests,
                failedRequests,
                totalRequests,
                successRate,
                totalDuration,
            };
        }, [runs]);

    const highestThroughput =
        useMemo(() => {
            if (runs.length === 0) {
                return 0;
            }

            return Math.max(
                ...runs.map(
                    (run) =>
                        Number(
                            run.benchmarkResult
                                ?.throughputRequestsPerSecond ||
                                0
                        )
                )
            );
        }, [runs]);

    if (isLoading) {
        return (
            <div className="results-page">

                <div className="results-state">

                    <strong>
                        Loading results...
                    </strong>

                    <span>
                        Retrieving benchmark measurements from ServerBench.
                    </span>

                </div>

            </div>
        );
    }

    if (loadError) {
        return (
            <div className="results-page">

                <section className="results-state results-state-error">

                    <strong>
                        Unable to load results.
                    </strong>

                    <span>
                        {loadError}
                    </span>

                    <button
                        className="results-back-button"
                        type="button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Experiment
                    </button>

                </section>

            </div>
        );
    }

    if (!result) {
        return (
            <div className="results-page">

                <section className="results-state results-state-error">

                    <strong>
                        Results are not available.
                    </strong>

                    <span>
                        This experiment does not have persisted benchmark results yet.
                    </span>

                    <button
                        className="results-back-button"
                        type="button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Experiment
                    </button>

                </section>

            </div>
        );
    }

    const experimentName =
        result.experimentName ||
        experiment?.name ||
        "Experiment";

    const experimentStatus =
        experiment?.status ||
        "COMPLETED";

    const totalRuns =
        result.totalRuns ?? runs.length;

    const successfulRuns =
        result.successfulRuns ?? 0;

    const failedRuns =
        result.failedRuns ?? 0;

    const completionRate =
        totalRuns === 0
            ? 0
            : (
                  (successfulRuns /
                      totalRuns) *
                  100
              );

    return (
        <div className="results-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <section className="results-header">

                <div>

                    <button
                        className="results-back-button"
                        type="button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Experiment
                    </button>

                    <div className="results-breadcrumb">
                        Experiments / Results
                    </div>

                    <div className="results-title-row">

                        <div>

                            <h1>
                                {experimentName}
                            </h1>

                            <p>
                                Detailed benchmark measurements
                                from the completed experiment.
                            </p>

                        </div>

                        <StatusBadge
                            status={
                                experimentStatus
                            }
                        />

                    </div>

                </div>

                <button
                    className="results-comparison-button"
                    type="button"
                    onClick={onComparison}
                >
                    Compare Architectures
                    <span>→</span>
                </button>

            </section>

            {/* ==================================================
                EXECUTION SUMMARY
            ================================================== */}

            <section className="results-summary-card">

                <div className="results-summary-main">

                    <div>

                        <span className="summary-eyebrow">
                            Execution Summary
                        </span>

                        <h2>

                            {
                                successfulRuns
                            }

                            <span>
                                {" "}
                                of{" "}
                                {totalRuns}
                            </span>

                        </h2>

                        <p>
                            benchmark runs completed successfully
                        </p>

                    </div>

                    <div className="summary-progress-area">

                        <div className="summary-progress-label">

                            <span>
                                Completion
                            </span>

                            <strong>
                                {completionRate.toFixed(
                                    0
                                )}
                                %
                            </strong>

                        </div>

                        <div className="summary-progress-track">

                            <div
                                className="summary-progress-fill"
                                style={{
                                    width: `${completionRate}%`,
                                }}
                            />

                        </div>

                    </div>

                </div>

                <div className="summary-divider" />

                <div className="summary-stat-row">

                    <div>

                        <span>
                            Total Requests
                        </span>

                        <strong>
                            {formatNumber(
                                overallMetrics.totalRequests
                            )}
                        </strong>

                    </div>

                    <div>

                        <span>
                            Successful Requests
                        </span>

                        <strong>
                            {formatNumber(
                                overallMetrics.successfulRequests
                            )}
                        </strong>

                    </div>

                    <div>

                        <span>
                            Failed Requests
                        </span>

                        <strong
                            className={
                                overallMetrics.failedRequests >
                                0
                                    ? "warning-value"
                                    : ""
                            }
                        >
                            {formatNumber(
                                overallMetrics.failedRequests
                            )}
                        </strong>

                    </div>

                    <div>

                        <span>
                            Overall Success Rate
                        </span>

                        <strong>
                            {formatDecimal(
                                overallMetrics.successRate,
                                2
                            )}
                            %
                        </strong>

                    </div>

                </div>

            </section>

            {/* ==================================================
                RUN RESULTS
            ================================================== */}

            {runs.length === 0 ? (
                <section className="result-run-card">

                    <div className="results-state">

                        <strong>
                            No benchmark runs available.
                        </strong>

                        <span>
                            The experiment has no persisted run results.
                        </span>

                    </div>

                </section>
            ) : (
                runs.map((run) => {

                    const benchmark =
                        run.benchmarkResult;

                    const throughput =
                        Number(
                            benchmark
                                ?.throughputRequestsPerSecond ||
                                0
                        );

                    const barWidth =
                        highestThroughput === 0
                            ? 0
                            : (throughput /
                                  highestThroughput) *
                              100;

                    const totalRequests =
                        Number(
                            benchmark?.totalRequests ||
                                0
                        );

                    const successfulRequests =
                        Number(
                            benchmark?.successfulRequests ||
                                0
                        );

                    const failedRequests =
                        Number(
                            benchmark?.failedRequests ||
                                0
                        );

                    const successRate =
                        Number(
                            benchmark?.successRate ||
                                0
                        );

                    const errorRate =
                        Number(
                            benchmark?.errorRate ||
                                0
                        );

                    return (
                        <section
                            className="result-run-card"
                            key={`${run.architecture}-${run.repetitionNumber}`}
                        >

                            {/* ==================================================
                                RUN HEADER
                            ================================================== */}

                            <div className="run-header">

                                <div className="run-title-group">

                                    <div className="run-architecture-icon">

                                        {run.architecture ===
                                            "SINGLE_THREADED" &&
                                            "1"}

                                        {run.architecture ===
                                            "MULTI_THREADED" &&
                                            "M"}

                                        {run.architecture ===
                                            "THREAD_POOL" &&
                                            "P"}

                                        {run.architecture ===
                                            "VIRTUAL_THREAD" &&
                                            "V"}

                                    </div>

                                    <div>

                                        <h2>
                                            {
                                                architectureNames[
                                                    run.architecture
                                                ] ||
                                                run.architecture
                                            }
                                        </h2>

                                        <p>
                                            Repetition{" "}
                                            {
                                                run.repetitionNumber
                                            }
                                        </p>

                                    </div>

                                </div>

                                <StatusBadge
                                    status={
                                        run.status
                                    }
                                />

                            </div>

                            {/* ==================================================
                                PRIMARY METRICS
                            ================================================== */}

                            <div className="primary-metrics-grid">

                                <MetricBox
                                    label="Throughput"
                                    value={`${formatNumber(
                                        throughput
                                    )} req/s`}
                                    helper="Requests per second"
                                />

                                <MetricBox
                                    label="Average Latency"
                                    value={formatLatency(
                                        benchmark?.averageLatencyMs
                                    )}
                                    helper="Mean request latency"
                                />

                                <MetricBox
                                    label="Success Rate"
                                    value={`${formatDecimal(
                                        successRate,
                                        2
                                    )}%`}
                                    helper={`${formatNumber(
                                        successfulRequests
                                    )} successful`}
                                />

                                <MetricBox
                                    label="Total Requests"
                                    value={formatNumber(
                                        totalRequests
                                    )}
                                    helper={`${formatNumber(
                                        failedRequests
                                    )} failed`}
                                />

                            </div>

                            {/* ==================================================
                                THROUGHPUT
                            ================================================== */}

                            <div className="throughput-section">

                                <div className="metric-section-header">

                                    <div>

                                        <h3>
                                            Throughput
                                        </h3>

                                        <p>
                                            Relative throughput for
                                            this experiment.
                                        </p>

                                    </div>

                                    <strong>
                                        {formatNumber(
                                            throughput
                                        )}{" "}
                                        req/s
                                    </strong>

                                </div>

                                <div className="throughput-track">

                                    <div
                                        className="throughput-fill"
                                        style={{
                                            width: `${barWidth}%`,
                                        }}
                                    />

                                </div>

                            </div>

                            {/* ==================================================
                                LATENCY METRICS
                            ================================================== */}

                            <div className="latency-section">

                                <div className="metric-section-header">

                                    <div>

                                        <h3>
                                            Latency Distribution
                                        </h3>

                                        <p>
                                            Percentile latency measurements.
                                        </p>

                                    </div>

                                </div>

                                <div className="latency-grid">

                                    <div>

                                        <span>
                                            Minimum
                                        </span>

                                        <strong>
                                            {formatLatency(
                                                benchmark?.minimumLatencyMs
                                            )}
                                        </strong>

                                    </div>

                                    <div>

                                        <span>
                                            P50
                                        </span>

                                        <strong>
                                            {formatLatency(
                                                benchmark?.p50LatencyMs
                                            )}
                                        </strong>

                                    </div>

                                    <div>

                                        <span>
                                            P95
                                        </span>

                                        <strong>
                                            {formatLatency(
                                                benchmark?.p95LatencyMs
                                            )}
                                        </strong>

                                    </div>

                                    <div>

                                        <span>
                                            P99
                                        </span>

                                        <strong>
                                            {formatLatency(
                                                benchmark?.p99LatencyMs
                                            )}
                                        </strong>

                                    </div>

                                </div>

                            </div>

                            {/* ==================================================
                                FAILURE BREAKDOWN
                            ================================================== */}

                            {failedRequests > 0 && (
                                <div className="failure-section">

                                    <div className="metric-section-header">

                                        <div>

                                            <h3>
                                                Failure Breakdown
                                            </h3>

                                            <p>
                                                Categorized request failures
                                                for this run.
                                            </p>

                                        </div>

                                    </div>

                                    <div className="failure-grid">

                                        <div>

                                            <span>
                                                Connect Timeouts
                                            </span>

                                            <strong>
                                                {
                                                    benchmark
                                                        ?.connectTimeouts ??
                                                    0
                                                }
                                            </strong>

                                        </div>

                                        <div>

                                            <span>
                                                Connection Refused
                                            </span>

                                            <strong>
                                                {
                                                    benchmark
                                                        ?.connectionRefused ??
                                                    0
                                                }
                                            </strong>

                                        </div>

                                        <div>

                                            <span>
                                                Connection Resets
                                            </span>

                                            <strong>
                                                {
                                                    benchmark
                                                        ?.connectionResets ??
                                                    0
                                                }
                                            </strong>

                                        </div>

                                        <div>

                                            <span>
                                                Read Timeouts
                                            </span>

                                            <strong className="failure-value">
                                                {
                                                    benchmark
                                                        ?.readTimeouts ??
                                                    0
                                                }
                                            </strong>

                                        </div>

                                        <div>

                                            <span>
                                                No Response
                                            </span>

                                            <strong>
                                                {
                                                    benchmark
                                                        ?.noResponseFailures ??
                                                    0
                                                }
                                            </strong>

                                        </div>

                                        <div>

                                            <span>
                                                Other I/O Failures
                                            </span>

                                            <strong>
                                                {
                                                    benchmark
                                                        ?.otherIoFailures ??
                                                    0
                                                }
                                            </strong>

                                        </div>

                                    </div>

                                </div>
                            )}

                            {/* ==================================================
                                RUN TIMING
                            ================================================== */}

                            <div className="latency-section">

                                <div className="metric-section-header">

                                    <div>

                                        <h3>
                                            Run Timing
                                        </h3>

                                        <p>
                                            Timing captured by the benchmark engine.
                                        </p>

                                    </div>

                                </div>

                                <div className="latency-grid">

                                    <div>

                                        <span>
                                            Started
                                        </span>

                                        <strong>
                                            {formatDateTime(
                                                run.startedAt
                                            )}
                                        </strong>

                                    </div>

                                    <div>

                                        <span>
                                            Finished
                                        </span>

                                        <strong>
                                            {formatDateTime(
                                                run.finishedAt
                                            )}
                                        </strong>

                                    </div>

                                    <div>

                                        <span>
                                            Duration
                                        </span>

                                        <strong>
                                            {formatNumber(
                                                benchmark?.totalDurationMs
                                            )}{" "}
                                            ms
                                        </strong>

                                    </div>

                                    <div>

                                        <span>
                                            Error Rate
                                        </span>

                                        <strong>
                                            {formatDecimal(
                                                errorRate,
                                                2
                                            )}
                                            %
                                        </strong>

                                    </div>

                                </div>

                            </div>

                            {run.status ===
                                "FAILED" &&
                                run.errorMessage && (
                                    <div className="failure-section">

                                        <div className="metric-section-header">

                                            <div>

                                                <h3>
                                                    Run Error
                                                </h3>

                                                <p>
                                                    The benchmark engine reported the following failure.
                                                </p>

                                            </div>

                                        </div>

                                        <div className="failure-grid">

                                            <div>
                                                <span>
                                                    Error
                                                </span>

                                                <strong className="failure-value">
                                                    {
                                                        run.errorMessage
                                                    }
                                                </strong>
                                            </div>

                                        </div>

                                    </div>
                                )}

                        </section>
                    );
                })
            )}

            {/* ==================================================
                FOOTER ACTION
            ================================================== */}

            <section className="results-bottom-action">

                <div>

                    <h2>
                        Ready to compare?
                    </h2>

                    <p>
                        Review the measured architecture
                        performance side by side.
                    </p>

                </div>

                <button
                    className="results-comparison-button"
                    type="button"
                    onClick={onComparison}
                >
                    Open Comparison
                    <span>→</span>
                </button>

            </section>

        </div>
    );
}

export default Results;