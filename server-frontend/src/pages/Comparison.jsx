import {
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    getExperimentComparison,
} from "../services/experimentApi";

import {
    Bar,
    BarChart,
    CartesianGrid,
    Legend,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";

import "./Comparison.css";

const architectureNames = {
    SINGLE_THREADED: "Single Threaded",
    MULTI_THREADED: "Multi Threaded",
    THREAD_POOL: "Thread Pool",
    VIRTUAL_THREAD: "Virtual Thread",
};

const architectureShortNames = {
    SINGLE_THREADED: "Single",
    MULTI_THREADED: "Multi",
    THREAD_POOL: "Pool",
    VIRTUAL_THREAD: "Virtual",
};

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

function formatPercentage(value) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "—";
    }

    return `${number.toFixed(2)}%`;
}

function formatLatency(value) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "—";
    }

    return `${number.toFixed(4)} ms`;
}

function ComparisonMetric({
    label,
    value,
    winner,
    helper,
}) {
    return (
        <div
            className={`comparison-metric ${
                winner ? "winner" : ""
            }`}
        >
            <span className="comparison-metric-label">
                {label}
            </span>

            <strong>
                {value}
            </strong>

            {winner && (
                <span className="winner-label">
                    Best
                </span>
            )}

            {helper && (
                <small>
                    {helper}
                </small>
            )}
        </div>
    );
}

function Comparison({
    selectedExperimentId,
    onBack,
}) {
    const [comparison, setComparison] =
        useState(null);

    const [isLoading, setIsLoading] =
        useState(true);

    const [loadError, setLoadError] =
        useState("");

    useEffect(() => {
        let mounted = true;

        async function loadComparison() {
            if (!selectedExperimentId) {
                if (mounted) {
                    setComparison(null);

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
                const response =
                    await getExperimentComparison(
                        selectedExperimentId
                    );

                if (mounted) {
                    setComparison(response);
                }
            } catch (error) {
                if (mounted) {
                    setComparison(null);

                    setLoadError(
                        error.message ||
                            "Unable to load comparison data."
                    );
                }
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        }

        loadComparison();

        return () => {
            mounted = false;
        };
    }, [selectedExperimentId]);

    const comparisons =
        useMemo(() => {
            if (
                !Array.isArray(
                    comparison?.comparisons
                )
            ) {
                return [];
            }

            return comparison.comparisons;
        }, [comparison]);

    const highestThroughput =
        useMemo(() => {
            if (comparisons.length === 0) {
                return null;
            }

            return Math.max(
                ...comparisons.map(
                    (item) =>
                        Number(
                            item.averageThroughput
                        )
                )
            );
        }, [comparisons]);

    const highestSuccessRate =
        useMemo(() => {
            if (comparisons.length === 0) {
                return null;
            }

            return Math.max(
                ...comparisons.map(
                    (item) =>
                        Number(
                            item.averageSuccessRate
                        )
                )
            );
        }, [comparisons]);

    const lowestAverageLatency =
        useMemo(() => {
            if (comparisons.length === 0) {
                return null;
            }

            return Math.min(
                ...comparisons.map(
                    (item) =>
                        Number(
                            item.averageLatency
                        )
                )
            );
        }, [comparisons]);

    const lowestP95 =
        useMemo(() => {
            if (comparisons.length === 0) {
                return null;
            }

            return Math.min(
                ...comparisons.map(
                    (item) =>
                        Number(
                            item.averageP95Latency
                        )
                )
            );
        }, [comparisons]);

    const lowestP99 =
        useMemo(() => {
            if (comparisons.length === 0) {
                return null;
            }

            return Math.min(
                ...comparisons.map(
                    (item) =>
                        Number(
                            item.averageP99Latency
                        )
                )
            );
        }, [comparisons]);

    const throughputChartData =
        useMemo(() => {
            return comparisons.map(
                (item) => ({
                    architecture:
                        architectureShortNames[
                            item.architecture
                        ] ||
                        item.architecture,

                    throughput:
                        Number(
                            item.averageThroughput ||
                                0
                        ),
                })
            );
        }, [comparisons]);

    const reliabilityChartData =
        useMemo(() => {
            return comparisons.map(
                (item) => ({
                    architecture:
                        architectureShortNames[
                            item.architecture
                        ] ||
                        item.architecture,

                    successRate:
                        Number(
                            item.averageSuccessRate ||
                                0
                        ),

                    errorRate:
                        Number(
                            item.averageErrorRate ||
                                0
                        ),
                })
            );
        }, [comparisons]);

    if (isLoading) {
        return (
            <div className="comparison-page">

                <section className="comparison-card comparison-state">

                    <strong>
                        Loading comparison...
                    </strong>

                    <span>
                        Retrieving architecture comparison from ServerBench.
                    </span>

                </section>

            </div>
        );
    }

    if (loadError) {
        return (
            <div className="comparison-page">

                <section className="comparison-card comparison-state comparison-state-error">

                    <strong>
                        Unable to load comparison.
                    </strong>

                    <span>
                        {loadError}
                    </span>

                    <button
                        type="button"
                        className="comparison-back-button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Results
                    </button>

                </section>

            </div>
        );
    }

    if (!comparison) {
        return (
            <div className="comparison-page">

                <section className="comparison-card comparison-state comparison-state-error">

                    <strong>
                        Comparison is not available.
                    </strong>

                    <span>
                        No comparison data was returned for this experiment.
                    </span>

                    <button
                        type="button"
                        className="comparison-back-button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Results
                    </button>

                </section>

            </div>
        );
    }

    const throughputWinner =
        comparison.highestThroughputArchitecture;

    const successRateWinner =
        comparison.highestSuccessRateArchitecture;

    const averageLatencyWinner =
        comparison.lowestAverageLatencyArchitecture;

    const p95Winner =
        comparison.lowestP95LatencyArchitecture;

    const p99Winner =
        comparison.lowestP99LatencyArchitecture;

    return (
        <div className="comparison-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <section className="comparison-header">

                <div>

                    <button
                        type="button"
                        className="comparison-back-button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Results
                    </button>

                    <div className="comparison-breadcrumb">
                        Experiments / Comparison
                    </div>

                    <h1>
                        Architecture Comparison
                    </h1>

                    <p>
                        Compare benchmark performance across
                        the server architectures used in this
                        experiment.
                    </p>

                </div>

                <div className="comparison-experiment-badge">

                    <span>
                        Experiment
                    </span>

                    <strong>
                        {comparison.experimentName}
                    </strong>

                </div>

            </section>

            {/* ==================================================
                KEY FINDINGS
            ================================================== */}

            <section className="comparison-card">

                <div className="comparison-card-header">

                    <div>

                        <h2>
                            Metric Leaders
                        </h2>

                        <p>
                            Best-performing architecture for
                            each measured metric.
                        </p>

                    </div>

                </div>

                <div className="leaders-grid">

                    {comparisons.map(
                        (item) => {

                            const isHighestThroughput =
                                item.architecture ===
                                throughputWinner;

                            const isHighestSuccessRate =
                                item.architecture ===
                                successRateWinner;

                            const isLowestAverageLatency =
                                item.architecture ===
                                averageLatencyWinner;

                            const isLowestP95 =
                                item.architecture ===
                                p95Winner;

                            const isLowestP99 =
                                item.architecture ===
                                p99Winner;

                            if (
                                !isHighestThroughput &&
                                !isHighestSuccessRate &&
                                !isLowestAverageLatency &&
                                !isLowestP95 &&
                                !isLowestP99
                            ) {
                                return null;
                            }

                            return (
                                <div
                                    className="leader-summary"
                                    key={
                                        item.architecture
                                    }
                                >

                                    <div className="leader-name">
                                        {
                                            architectureNames[
                                                item.architecture
                                            ] ||
                                            item.architecture
                                        }
                                    </div>

                                    <div className="leader-items">

                                        {isHighestThroughput && (
                                            <span>
                                                Highest throughput
                                            </span>
                                        )}

                                        {isHighestSuccessRate && (
                                            <span>
                                                Highest success rate
                                            </span>
                                        )}

                                        {isLowestAverageLatency && (
                                            <span>
                                                Lowest average latency
                                            </span>
                                        )}

                                        {isLowestP95 && (
                                            <span>
                                                Lowest P95 latency
                                            </span>
                                        )}

                                        {isLowestP99 && (
                                            <span>
                                                Lowest P99 latency
                                            </span>
                                        )}

                                    </div>

                                </div>
                            );
                        }
                    )}

                </div>

            </section>

            {/* ==================================================
                COMPARISON TABLE
            ================================================== */}

            <section className="comparison-card">

                <div className="comparison-card-header">

                    <div>

                        <h2>
                            Performance Comparison
                        </h2>

                        <p>
                            Metric-by-metric comparison of the
                            selected architectures.
                        </p>

                    </div>

                </div>

                <div className="comparison-table-wrapper">

                    <table className="comparison-table">

                        <thead>

                            <tr>
                                <th>
                                    Architecture
                                </th>

                                <th>
                                    Throughput
                                </th>

                                <th>
                                    Avg Latency
                                </th>

                                <th>
                                    P95 Latency
                                </th>

                                <th>
                                    P99 Latency
                                </th>

                                <th>
                                    Success Rate
                                </th>

                                <th>
                                    Error Rate
                                </th>

                                <th>
                                    Runs
                                </th>
                            </tr>

                        </thead>

                        <tbody>

                            {comparisons.map(
                                (item) => (

                                    <tr
                                        key={
                                            item.architecture
                                        }
                                    >

                                        <td>
                                            <strong>
                                                {
                                                    architectureNames[
                                                        item.architecture
                                                    ] ||
                                                    item.architecture
                                                }
                                            </strong>
                                        </td>

                                        <td
                                            className={
                                                item.architecture ===
                                                throughputWinner
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {
                                                formatNumber(
                                                    item.averageThroughput
                                                )
                                            }{" "}
                                            req/s
                                        </td>

                                        <td
                                            className={
                                                item.architecture ===
                                                averageLatencyWinner
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {
                                                formatLatency(
                                                    item.averageLatency
                                                )
                                            }
                                        </td>

                                        <td
                                            className={
                                                item.architecture ===
                                                p95Winner
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {
                                                formatLatency(
                                                    item.averageP95Latency
                                                )
                                            }
                                        </td>

                                        <td
                                            className={
                                                item.architecture ===
                                                p99Winner
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {
                                                formatLatency(
                                                    item.averageP99Latency
                                                )
                                            }
                                        </td>

                                        <td
                                            className={
                                                item.architecture ===
                                                successRateWinner
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {
                                                formatPercentage(
                                                    item.averageSuccessRate
                                                )
                                            }
                                        </td>

                                        <td>
                                            {
                                                formatPercentage(
                                                    item.averageErrorRate
                                                )
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.totalRuns
                                            }
                                        </td>

                                    </tr>

                                )
                            )}

                        </tbody>

                    </table>

                </div>

            </section>

            {/* ==================================================
                THROUGHPUT
            ================================================== */}

            <section className="comparison-card">

                <div className="comparison-card-header">

                    <div>

                        <h2>
                            Throughput Comparison
                        </h2>

                        <p>
                            Average requests processed per
                            second by each architecture.
                        </p>

                    </div>

                    <span className="comparison-unit">
                        requests / sec
                    </span>

                </div>

                <div className="comparison-chart">

                    <ResponsiveContainer
                        width="100%"
                        height={360}
                    >

                        <BarChart
                            data={
                                throughputChartData
                            }
                            margin={{
                                top: 20,
                                right: 20,
                                left: 10,
                                bottom: 20,
                            }}
                        >

                            <CartesianGrid
                                stroke="#eef2f7"
                                vertical={false}
                            />

                            <XAxis
                                dataKey="architecture"
                                tick={{
                                    fill: "#64748b",
                                    fontSize: 11,
                                }}
                                axisLine={{
                                    stroke: "#dbe2ea",
                                }}
                                tickLine={false}
                            />

                            <YAxis
                                tick={{
                                    fill: "#64748b",
                                    fontSize: 11,
                                }}
                                axisLine={false}
                                tickLine={false}
                            />

                            <Tooltip
                                formatter={(value) => [
                                    `${formatNumber(
                                        value
                                    )} req/s`,
                                    "Throughput",
                                ]}
                            />

                            <Bar
                                dataKey="throughput"
                                fill="#2563eb"
                                radius={[
                                    5,
                                    5,
                                    0,
                                    0,
                                ]}
                                barSize={65}
                            />

                        </BarChart>

                    </ResponsiveContainer>

                </div>

            </section>

            {/* ==================================================
                RELIABILITY
            ================================================== */}

            <section className="comparison-card">

                <div className="comparison-card-header">

                    <div>

                        <h2>
                            Reliability Comparison
                        </h2>

                        <p>
                            Measured success and error rates for
                            each architecture in this experiment.
                        </p>

                    </div>

                    <span className="comparison-unit">
                        percentage
                    </span>

                </div>

                <div className="comparison-chart">

                    <ResponsiveContainer
                        width="100%"
                        height={360}
                    >

                        <BarChart
                            data={reliabilityChartData}
                            margin={{
                                top: 35,
                                right: 20,
                                left: 10,
                                bottom: 20,
                            }}
                        >

                            <CartesianGrid
                                stroke="#eef2f7"
                                vertical={false}
                            />

                            <XAxis
                                dataKey="architecture"
                                tick={{
                                    fill: "#64748b",
                                    fontSize: 11,
                                }}
                                axisLine={{
                                    stroke: "#dbe2ea",
                                }}
                                tickLine={false}
                            />

                            <YAxis
                                domain={[
                                    0,
                                    (dataMax) =>
                                        Math.max(
                                            1,
                                            Math.ceil(
                                                Number(dataMax) / 10
                                            ) * 10
                                        ),
                                ]}
                                tick={{
                                    fill: "#64748b",
                                    fontSize: 11,
                                }}
                                axisLine={false}
                                tickLine={false}
                            />

                            <Tooltip
                                formatter={(value, name) => [
                                    `${Number(value).toFixed(2)}%`,
                                    name,
                                ]}
                            />

                            <Legend />

                            <Bar
                                dataKey="successRate"
                                name="Success Rate"
                                fill="#16a34a"
                                radius={[
                                    4,
                                    4,
                                    0,
                                    0,
                                ]}
                                barSize={32}
                                label={{
                                    position: "top",
                                    formatter: (value) =>
                                        `${Number(value).toFixed(2)}%`,
                                    fontSize: 10,
                                }}
                            />

                            <Bar
                                dataKey="errorRate"
                                name="Error Rate"
                                fill="#dc2626"
                                radius={[
                                    4,
                                    4,
                                    0,
                                    0,
                                ]}
                                barSize={32}
                                label={{
                                    position: "top",
                                    formatter: (value) =>
                                        Number(value) === 0
                                            ? "0%"
                                            : `${Number(value).toFixed(2)}%`,
                                    fontSize: 10,
                                }}
                            />

                        </BarChart>

                    </ResponsiveContainer>

                </div>

                {/* ==================================================
                    RELIABILITY DATA TABLE
                ================================================== */}

                <div className="reliability-data-table">

                    <div className="reliability-table-header">
                        <span>
                            Architecture
                        </span>

                        <span>
                            Success Rate
                        </span>

                        <span>
                            Error Rate
                        </span>

                        <span>
                            Successful Runs
                        </span>

                        <span>
                            Failed Runs
                        </span>
                    </div>

                    {comparisons.map((item) => (

                        <div
                            className="reliability-table-row"
                            key={item.architecture}
                        >

                            <strong>
                                {
                                    architectureNames[
                                        item.architecture
                                    ] || item.architecture
                                }
                            </strong>

                            <span>
                                {
                                    formatPercentage(
                                        item.averageSuccessRate
                                    )
                                }
                            </span>

                            <span
                                className={
                                    Number(
                                        item.averageErrorRate
                                    ) > 0
                                        ? "reliability-error"
                                        : ""
                                }
                            >
                                {
                                    formatPercentage(
                                        item.averageErrorRate
                                    )
                                }
                            </span>

                            <span>
                                {item.successfulRuns}
                            </span>

                            <span>
                                {item.failedRuns}
                            </span>

                        </div>

                    ))}

                </div>

            </section>

            {/* ==================================================
                METRIC SUMMARY
            ================================================== */}

            <section className="comparison-card">

                <div className="comparison-card-header">

                    <div>

                        <h2>
                            Metric Summary
                        </h2>

                        <p>
                            A concise view of the strongest
                            measured characteristics.
                        </p>

                    </div>

                </div>

                <div className="summary-metrics-grid">

                    <ComparisonMetric
                        label="Highest Throughput"
                        value={
                            highestThroughput === null
                                ? "—"
                                : `${formatNumber(
                                      highestThroughput
                                  )} req/s`
                        }
                        winner={
                            Boolean(
                                throughputWinner
                            )
                        }
                        helper={
                            throughputWinner
                                ? architectureNames[
                                      throughputWinner
                                  ] ||
                                  throughputWinner
                                : null
                        }
                    />

                    <ComparisonMetric
                        label="Highest Success Rate"
                        value={
                            highestSuccessRate === null
                                ? "—"
                                : formatPercentage(
                                      highestSuccessRate
                                  )
                        }
                        winner={
                            Boolean(
                                successRateWinner
                            )
                        }
                        helper={
                            successRateWinner
                                ? architectureNames[
                                      successRateWinner
                                  ] ||
                                  successRateWinner
                                : null
                        }
                    />

                    <ComparisonMetric
                        label="Lowest Average Latency"
                        value={
                            lowestAverageLatency === null
                                ? "—"
                                : formatLatency(
                                      lowestAverageLatency
                                  )
                        }
                        winner={
                            Boolean(
                                averageLatencyWinner
                            )
                        }
                        helper={
                            averageLatencyWinner
                                ? architectureNames[
                                      averageLatencyWinner
                                  ] ||
                                  averageLatencyWinner
                                : null
                        }
                    />

                    <ComparisonMetric
                        label="Lowest P95 Latency"
                        value={
                            lowestP95 === null
                                ? "—"
                                : formatLatency(
                                      lowestP95
                                  )
                        }
                        winner={
                            Boolean(
                                p95Winner
                            )
                        }
                        helper={
                            p95Winner
                                ? architectureNames[
                                      p95Winner
                                  ] ||
                                  p95Winner
                                : null
                        }
                    />

                    <ComparisonMetric
                        label="Lowest P99 Latency"
                        value={
                            lowestP99 === null
                                ? "—"
                                : formatLatency(
                                      lowestP99
                                  )
                        }
                        winner={
                            Boolean(
                                p99Winner
                            )
                        }
                        helper={
                            p99Winner
                                ? architectureNames[
                                      p99Winner
                                  ] ||
                                  p99Winner
                                : null
                        }
                    />

                </div>

            </section>

        </div>
    );
}

export default Comparison;