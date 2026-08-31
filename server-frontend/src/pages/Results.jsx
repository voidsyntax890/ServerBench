import "./Results.css";

const experiment = {
    id: "0debaf9f-6004-4a40-9bac-4dc43239a2ec",
    name: "M5 Restart Persistence Test",
    status: "COMPLETED",
    totalRuns: 4,
    successfulRuns: 4,
    failedRuns: 0,
};

const runs = [
    {
        architecture: "SINGLE_THREADED",
        totalRequests: 42089,
        successfulRequests: 42085,
        failedRequests: 4,
        throughput: 14028.33,
        averageLatency: 0.0000475,
        p50: 0,
        p95: 0,
        p99: 0,
        successRate: 99.99,
        errorRate: 0.01,
        totalDuration: 3000,
        status: "COMPLETED",
        failures: {
            connectTimeouts: 0,
            connectionRefused: 0,
            connectionResets: 0,
            readTimeouts: 4,
            noResponseFailures: 0,
            otherIoFailures: 0,
        },
    },
    {
        architecture: "MULTI_THREADED",
        totalRequests: 199008,
        successfulRequests: 199008,
        failedRequests: 0,
        throughput: 66336,
        averageLatency: 0.0004975,
        p50: 0,
        p95: 0,
        p99: 0,
        successRate: 100,
        errorRate: 0,
        totalDuration: 3000,
        status: "COMPLETED",
        failures: {
            connectTimeouts: 0,
            connectionRefused: 0,
            connectionResets: 0,
            readTimeouts: 0,
            noResponseFailures: 0,
            otherIoFailures: 0,
        },
    },
    {
        architecture: "THREAD_POOL",
        totalRequests: 189047,
        successfulRequests: 189047,
        failedRequests: 0,
        throughput: 63015.67,
        averageLatency: 0.0006136,
        p50: 0,
        p95: 0,
        p99: 0,
        successRate: 100,
        errorRate: 0,
        totalDuration: 3000,
        status: "COMPLETED",
        failures: {
            connectTimeouts: 0,
            connectionRefused: 0,
            connectionResets: 0,
            readTimeouts: 0,
            noResponseFailures: 0,
            otherIoFailures: 0,
        },
    },
    {
        architecture: "VIRTUAL_THREAD",
        totalRequests: 148643,
        successfulRequests: 148643,
        failedRequests: 0,
        throughput: 49547.67,
        averageLatency: 0.00037,
        p50: 0,
        p95: 0,
        p99: 0,
        successRate: 100,
        errorRate: 0,
        totalDuration: 3000,
        status: "COMPLETED",
        failures: {
            connectTimeouts: 0,
            connectionRefused: 0,
            connectionResets: 0,
            readTimeouts: 0,
            noResponseFailures: 0,
            otherIoFailures: 0,
        },
    },
];

const architectureNames = {
    SINGLE_THREADED: "Single Threaded",
    MULTI_THREADED: "Multi Threaded",
    THREAD_POOL: "Thread Pool",
    VIRTUAL_THREAD: "Virtual Thread",
};

function StatusBadge({ status }) {
    return (
        <span className={`results-status results-status-${status.toLowerCase()}`}>
            <span className="results-status-dot" />
            {status}
        </span>
    );
}

function formatNumber(value) {
    return Number(value).toLocaleString(undefined, {
        maximumFractionDigits: 2,
    });
}

function formatLatency(value) {
    return `${Number(value).toFixed(4)} ms`;
}

function MetricBox({ label, value, helper }) {
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

function Results({ onBack, onComparison }) {
    const highestThroughput = Math.max(
        ...runs.map((run) => run.throughput)
    );

    const overallSuccessfulRequests = runs.reduce(
        (total, run) =>
            total + run.successfulRequests,
        0
    );

    const overallFailedRequests = runs.reduce(
        (total, run) =>
            total + run.failedRequests,
        0
    );

    const overallTotalRequests =
        overallSuccessfulRequests +
        overallFailedRequests;

    const overallSuccessRate =
        overallTotalRequests === 0
            ? 0
            : (
                  (overallSuccessfulRequests /
                      overallTotalRequests) *
                  100
              ).toFixed(2);

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
                            <h1>{experiment.name}</h1>

                            <p>
                                Detailed benchmark measurements
                                from the completed experiment.
                            </p>
                        </div>

                        <StatusBadge
                            status={experiment.status}
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
                            {experiment.successfulRuns}
                            <span>
                                {" "}
                                of{" "}
                                {experiment.totalRuns}
                            </span>
                        </h2>

                        <p>
                            benchmark runs completed successfully
                        </p>
                    </div>

                    <div className="summary-progress-area">

                        <div className="summary-progress-label">
                            <span>Completion</span>
                            <strong>100%</strong>
                        </div>

                        <div className="summary-progress-track">
                            <div
                                className="summary-progress-fill"
                                style={{
                                    width: "100%",
                                }}
                            />
                        </div>

                    </div>

                </div>

                <div className="summary-divider" />

                <div className="summary-stat-row">

                    <div>
                        <span>Total Requests</span>
                        <strong>
                            {formatNumber(
                                overallTotalRequests
                            )}
                        </strong>
                    </div>

                    <div>
                        <span>Successful Requests</span>
                        <strong>
                            {formatNumber(
                                overallSuccessfulRequests
                            )}
                        </strong>
                    </div>

                    <div>
                        <span>Failed Requests</span>
                        <strong className={
                            overallFailedRequests > 0
                                ? "warning-value"
                                : ""
                        }>
                            {formatNumber(
                                overallFailedRequests
                            )}
                        </strong>
                    </div>

                    <div>
                        <span>Overall Success Rate</span>
                        <strong>
                            {overallSuccessRate}%
                        </strong>
                    </div>

                </div>

            </section>

            {/* ==================================================
                RUN RESULTS
            ================================================== */}

            {runs.map((run) => {

                const barWidth =
                    highestThroughput === 0
                        ? 0
                        : (run.throughput /
                              highestThroughput) *
                          100;

                return (
                    <section
                        className="result-run-card"
                        key={run.architecture}
                    >

                        {/* ==================================================
                            RUN HEADER
                        ================================================== */}

                        <div className="run-header">

                            <div className="run-title-group">

                                <div className="run-architecture-icon">
                                    {run.architecture ===
                                        "SINGLE_THREADED" && "1"}

                                    {run.architecture ===
                                        "MULTI_THREADED" && "M"}

                                    {run.architecture ===
                                        "THREAD_POOL" && "P"}

                                    {run.architecture ===
                                        "VIRTUAL_THREAD" && "V"}

                                </div>

                                <div>

                                    <h2>
                                        {
                                            architectureNames[
                                                run.architecture
                                            ]
                                        }
                                    </h2>

                                    <p>
                                        Repetition 1
                                    </p>

                                </div>

                            </div>

                            <StatusBadge
                                status={run.status}
                            />

                        </div>

                        {/* ==================================================
                            PRIMARY METRICS
                        ================================================== */}

                        <div className="primary-metrics-grid">

                            <MetricBox
                                label="Throughput"
                                value={`${formatNumber(
                                    run.throughput
                                )} req/s`}
                                helper="Requests per second"
                            />

                            <MetricBox
                                label="Average Latency"
                                value={formatLatency(
                                    run.averageLatency
                                )}
                                helper="Mean request latency"
                            />

                            <MetricBox
                                label="Success Rate"
                                value={`${run.successRate.toFixed(
                                    2
                                )}%`}
                                helper={`${formatNumber(
                                    run.successfulRequests
                                )} successful`}
                            />

                            <MetricBox
                                label="Total Requests"
                                value={formatNumber(
                                    run.totalRequests
                                )}
                                helper={`${formatNumber(
                                    run.failedRequests
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
                                        run.throughput
                                    )} req/s
                                </strong>

                            </div>

                            <div className="throughput-track">

                                <div
                                    className="throughput-fill"
                                    style={{
                                        width:
                                            `${barWidth}%`,
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
                                    <span>Minimum</span>
                                    <strong>
                                        {run.p50 === 0
                                            ? "0.0000"
                                            : formatLatency(
                                                  run.p50
                                              )}
                                    </strong>
                                </div>

                                <div>
                                    <span>P50</span>
                                    <strong>
                                        {formatLatency(
                                            run.p50
                                        )}
                                    </strong>
                                </div>

                                <div>
                                    <span>P95</span>
                                    <strong>
                                        {formatLatency(
                                            run.p95
                                        )}
                                    </strong>
                                </div>

                                <div>
                                    <span>P99</span>
                                    <strong>
                                        {formatLatency(
                                            run.p99
                                        )}
                                    </strong>
                                </div>

                            </div>

                        </div>

                        {/* ==================================================
                            FAILURE BREAKDOWN
                        ================================================== */}

                        {run.failedRequests > 0 && (
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
                                                run.failures
                                                    .connectTimeouts
                                            }
                                        </strong>
                                    </div>

                                    <div>
                                        <span>
                                            Connection Refused
                                        </span>

                                        <strong>
                                            {
                                                run.failures
                                                    .connectionRefused
                                            }
                                        </strong>
                                    </div>

                                    <div>
                                        <span>
                                            Connection Resets
                                        </span>

                                        <strong>
                                            {
                                                run.failures
                                                    .connectionResets
                                            }
                                        </strong>
                                    </div>

                                    <div>
                                        <span>
                                            Read Timeouts
                                        </span>

                                        <strong className="failure-value">
                                            {
                                                run.failures
                                                    .readTimeouts
                                            }
                                        </strong>
                                    </div>

                                    <div>
                                        <span>
                                            No Response
                                        </span>

                                        <strong>
                                            {
                                                run.failures
                                                    .noResponseFailures
                                            }
                                        </strong>
                                    </div>

                                    <div>
                                        <span>
                                            Other I/O Failures
                                        </span>

                                        <strong>
                                            {
                                                run.failures
                                                    .otherIoFailures
                                            }
                                        </strong>
                                    </div>

                                </div>

                            </div>
                        )}

                    </section>
                );
            })}

            {/* ==================================================
                FOOTER ACTION
            ================================================== */}

            <section className="results-bottom-action">

                <div>
                    <h2>Ready to compare?</h2>

                    <p>
                        Review all four architectures side by side
                        to understand their relative performance.
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