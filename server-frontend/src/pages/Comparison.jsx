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

const experiment = {
    id: "0debaf9f-6004-4a40-9bac-4dc43239a2ec",
    name: "M5 Restart Persistence Test",
    status: "COMPLETED",
};

const comparisons = [
    {
        architecture: "SINGLE_THREADED",
        averageThroughput: 14028.33,
        averageLatency: 0.0000475,
        averageP95Latency: 0,
        averageP99Latency: 0,
        averageSuccessRate: 99.99,
        averageErrorRate: 0.01,
        successfulRuns: 1,
        failedRuns: 0,
        totalRuns: 1,
    },
    {
        architecture: "MULTI_THREADED",
        averageThroughput: 66336,
        averageLatency: 0.0004975,
        averageP95Latency: 0,
        averageP99Latency: 0,
        averageSuccessRate: 100,
        averageErrorRate: 0,
        successfulRuns: 1,
        failedRuns: 0,
        totalRuns: 1,
    },
    {
        architecture: "THREAD_POOL",
        averageThroughput: 63015.67,
        averageLatency: 0.0006136,
        averageP95Latency: 0,
        averageP99Latency: 0,
        averageSuccessRate: 100,
        averageErrorRate: 0,
        successfulRuns: 1,
        failedRuns: 0,
        totalRuns: 1,
    },
    {
        architecture: "VIRTUAL_THREAD",
        averageThroughput: 49547.67,
        averageLatency: 0.00037,
        averageP95Latency: 0,
        averageP99Latency: 0,
        averageSuccessRate: 100,
        averageErrorRate: 0,
        successfulRuns: 1,
        failedRuns: 0,
        totalRuns: 1,
    },
];

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
    return Number(value).toLocaleString(undefined, {
        maximumFractionDigits: 2,
    });
}

function formatPercentage(value) {
    return `${Number(value).toFixed(2)}%`;
}

function formatLatency(value) {
    return `${Number(value).toFixed(4)} ms`;
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

            <strong>{value}</strong>

            {winner && (
                <span className="winner-label">
                    Best
                </span>
            )}

            {helper && (
                <small>{helper}</small>
            )}
        </div>
    );
}

function Comparison({
    onBack,
}) {
    const highestThroughput = Math.max(
        ...comparisons.map(
            (item) => item.averageThroughput
        )
    );

    const highestSuccessRate = Math.max(
        ...comparisons.map(
            (item) => item.averageSuccessRate
        )
    );

    const lowestAverageLatency = Math.min(
        ...comparisons.map(
            (item) => item.averageLatency
        )
    );

    const lowestP95 = Math.min(
        ...comparisons.map(
            (item) => item.averageP95Latency
        )
    );

    const lowestP99 = Math.min(
        ...comparisons.map(
            (item) => item.averageP99Latency
        )
    );

    const throughputChartData =
        comparisons.map((item) => ({
            architecture:
                architectureShortNames[
                    item.architecture
                ],
            throughput:
                item.averageThroughput,
        }));

    const reliabilityChartData =
        comparisons.map((item) => ({
            architecture:
                architectureShortNames[
                    item.architecture
                ],
            successRate:
                item.averageSuccessRate,
            errorRate:
                item.averageErrorRate,
        }));

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
                        {experiment.name}
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

                    {comparisons.map((item) => {

                        const isHighestThroughput =
                            item.averageThroughput ===
                            highestThroughput;

                        const isHighestSuccessRate =
                            item.averageSuccessRate ===
                            highestSuccessRate;

                        const isLowestAverageLatency =
                            item.averageLatency ===
                            lowestAverageLatency;

                        const isLowestP95 =
                            item.averageP95Latency ===
                            lowestP95;

                        const isLowestP99 =
                            item.averageP99Latency ===
                            lowestP99;

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
                                key={item.architecture}
                            >

                                <div className="leader-name">
                                    {
                                        architectureNames[
                                            item.architecture
                                        ]
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
                    })}

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
                                                    ]
                                                }
                                            </strong>
                                        </td>

                                        <td
                                            className={
                                                item.averageThroughput ===
                                                highestThroughput
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {formatNumber(
                                                item.averageThroughput
                                            )}{" "}
                                            req/s
                                        </td>

                                        <td
                                            className={
                                                item.averageLatency ===
                                                lowestAverageLatency
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {formatLatency(
                                                item.averageLatency
                                            )}
                                        </td>

                                        <td
                                            className={
                                                item.averageP95Latency ===
                                                lowestP95
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {formatLatency(
                                                item.averageP95Latency
                                            )}
                                        </td>

                                        <td
                                            className={
                                                item.averageP99Latency ===
                                                lowestP99
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {formatLatency(
                                                item.averageP99Latency
                                            )}
                                        </td>

                                        <td
                                            className={
                                                item.averageSuccessRate ===
                                                highestSuccessRate
                                                    ? "best-cell"
                                                    : ""
                                            }
                                        >
                                            {formatPercentage(
                                                item.averageSuccessRate
                                            )}
                                        </td>

                                        <td>
                                            {formatPercentage(
                                                item.averageErrorRate
                                            )}
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
                            data={throughputChartData}
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
                                contentStyle={{
                                    border:
                                        "1px solid #e5e7eb",
                                    borderRadius:
                                        "8px",
                                    boxShadow:
                                        "0 4px 16px rgba(15, 23, 42, 0.08)",
                                }}
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
                            Success and error rates across
                            architectures.
                        </p>
                    </div>

                    <span className="comparison-unit">
                        percentage
                    </span>

                </div>

                <div className="comparison-chart">

                    <ResponsiveContainer
                        width="100%"
                        height={340}
                    >

                        <BarChart
                            data={reliabilityChartData}
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
                                domain={[
                                    0,
                                    100,
                                ]}
                                tick={{
                                    fill: "#64748b",
                                    fontSize: 11,
                                }}
                                axisLine={false}
                                tickLine={false}
                            />

                            <Tooltip
                                formatter={(value) =>
                                    `${Number(
                                        value
                                    ).toFixed(2)}%`
                                }
                                contentStyle={{
                                    border:
                                        "1px solid #e5e7eb",
                                    borderRadius:
                                        "8px",
                                    boxShadow:
                                        "0 4px 16px rgba(15, 23, 42, 0.08)",
                                }}
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
                                barSize={36}
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
                                barSize={36}
                            />

                        </BarChart>

                    </ResponsiveContainer>

                </div>

            </section>

            {/* ==================================================
                INTERPRETATION
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
                            formatNumber(
                                highestThroughput
                            ) + " req/s"
                        }
                        winner="Multi Threaded"
                        helper={
                            "Multi Threaded"
                        }
                    />

                    <ComparisonMetric
                        label="Highest Success Rate"
                        value={
                            formatPercentage(
                                highestSuccessRate
                            )
                        }
                        winner="Multi Threaded"
                        helper={
                            "Multi Threaded / Thread Pool / Virtual Thread"
                        }
                    />

                    <ComparisonMetric
                        label="Lowest Average Latency"
                        value={formatLatency(
                            lowestAverageLatency
                        )}
                        winner="Single Threaded"
                        helper={
                            "Single Threaded"
                        }
                    />

                    <ComparisonMetric
                        label="Lowest P95 Latency"
                        value={formatLatency(
                            lowestP95
                        )}
                        winner="Multiple"
                        helper={
                            "All architectures returned 0 ms"
                        }
                    />

                </div>

            </section>

        </div>
    );
}

export default Comparison;