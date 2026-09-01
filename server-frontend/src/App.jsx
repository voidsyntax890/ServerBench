import {
    useEffect,
    useMemo,
    useState,
} from "react";

import NewExperiment from "./pages/NewExperiment";
import ExperimentDetails from "./pages/ExperimentDetails";
import Results from "./pages/Results";
import Comparison from "./pages/Comparison";
import History from "./pages/History";
import Experiments from "./pages/Experiments";
import Settings from "./pages/Settings";

import {
    getExperiments,
    getExperimentComparison,
} from "./services/experimentApi";

import {
    Bar,
    BarChart,
    CartesianGrid,
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";

import "./App.css";

/*
 * ================================================================
 * STATUS BADGE
 * ================================================================
 */

function StatusBadge({ status }) {
    const safeStatus =
        status || "UNKNOWN";

    return (
        <span
            className={`status-badge status-${safeStatus.toLowerCase()}`}
        >
            <span className="status-dot" />
            {safeStatus}
        </span>
    );
}

/*
 * ================================================================
 * DATE FORMATTER
 * ================================================================
 */

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

/*
 * ================================================================
 * ARCHITECTURE HELPERS
 * ================================================================
 */

const architectureNames = {
    SINGLE_THREADED: "Single Threaded",
    MULTI_THREADED: "Multi Threaded",
    THREAD_POOL: "Thread Pool",
    VIRTUAL_THREAD: "Virtual Thread",
};

const architectureKeys = {
    SINGLE_THREADED: "single",
    MULTI_THREADED: "multi",
    THREAD_POOL: "pool",
    VIRTUAL_THREAD: "virtual",
};

/*
 * ================================================================
 * APP
 * ================================================================
 */

function App() {

    /*
     * ------------------------------------------------------------
     * Navigation
     * ------------------------------------------------------------
     */

    const [currentPage, setCurrentPage] =
        useState("dashboard");

    const [currentExperimentId, setCurrentExperimentId] =
        useState(null);

    /*
     * ------------------------------------------------------------
     * Dashboard data
     * ------------------------------------------------------------
     */

    const [experiments, setExperiments] =
        useState([]);

    const [dashboardComparisons, setDashboardComparisons] =
        useState([]);

    const [dashboardLoading, setDashboardLoading] =
        useState(true);

    const [dashboardError, setDashboardError] =
        useState("");

    /*
     * ------------------------------------------------------------
     * Load dashboard data
     * ------------------------------------------------------------
     */

    useEffect(() => {
        let mounted = true;

        async function loadDashboard() {
            setDashboardLoading(true);
            setDashboardError("");

            try {
                const response =
                    await getExperiments();

                const experimentList =
                    Array.isArray(response)
                        ? response
                        : response?.experiments || [];

                if (!mounted) {
                    return;
                }

                setExperiments(
                    experimentList
                );

                /*
                 * Only completed experiments have
                 * meaningful comparison results.
                 *
                 * We use the newest completed
                 * experiments for the dashboard.
                 */
                const completedExperiments =
                    experimentList
                        .filter(
                            (experiment) =>
                                experiment.status ===
                                "COMPLETED"
                        )
                        .sort(
                            (a, b) =>
                                new Date(
                                    b.createdAt
                                ) -
                                new Date(
                                    a.createdAt
                                )
                        );

                /*
                 * Latest completed experiment
                 * becomes the Performance Overview.
                 */
                if (
                    completedExperiments.length >
                        0 &&
                    !currentExperimentId
                ) {
                    setCurrentExperimentId(
                        completedExperiments[0].id
                    );
                }

                /*
                 * Load comparison data for up to
                 * seven recent completed experiments.
                 *
                 * This is used to create a real
                 * performance trend.
                 */
                const recentCompleted =
                    completedExperiments.slice(
                        0,
                        7
                    );

                const comparisonResults =
                    await Promise.all(
                        recentCompleted.map(
                            async (
                                experiment
                            ) => {
                                try {
                                    const comparison =
                                        await getExperimentComparison(
                                            experiment.id
                                        );

                                    return {
                                        experiment,
                                        comparison,
                                    };
                                } catch {
                                    return null;
                                }
                            }
                        )
                    );

                if (mounted) {
                    setDashboardComparisons(
                        comparisonResults.filter(
                            Boolean
                        )
                    );
                }

            } catch (error) {
                if (mounted) {
                    setDashboardError(
                        error.message ||
                            "Unable to load dashboard data."
                    );
                }
            } finally {
                if (mounted) {
                    setDashboardLoading(
                        false
                    );
                }
            }
        }

        loadDashboard();

        return () => {
            mounted = false;
        };
    }, []);

    /*
     * ------------------------------------------------------------
     * Dashboard calculations
     * ------------------------------------------------------------
     */

    const totalExperiments =
        experiments.length;

    const completedExperiments =
        experiments.filter(
            (experiment) =>
                experiment.status ===
                "COMPLETED"
        ).length;

    const runningExperiments =
        experiments.filter(
            (experiment) =>
                experiment.status ===
                "RUNNING"
        ).length;

    const failedExperiments =
        experiments.filter(
            (experiment) =>
                experiment.status ===
                "FAILED"
        ).length;

    const completionRate =
        totalExperiments === 0
            ? 0
            : (
                  (completedExperiments /
                      totalExperiments) *
                  100
              ).toFixed(0);

    const failureRate =
        totalExperiments === 0
            ? 0
            : (
                  (failedExperiments /
                      totalExperiments) *
                  100
              ).toFixed(0);

    /*
     * Newest five experiments for dashboard table.
     */
    const recentExperiments =
        useMemo(() => {
            return [...experiments]
                .sort(
                    (a, b) =>
                        new Date(
                            b.createdAt
                        ) -
                        new Date(
                            a.createdAt
                        )
                )
                .slice(0, 5)
                .map(
                    (experiment) => ({
                        ...experiment,

                        architectures:
                            Array.isArray(
                                experiment.architectures
                            )
                                ? `${experiment.architectures.length} / ${experiment.architectures.length}`
                                : "—",

                        date:
                            formatDateTime(
                                experiment.createdAt
                            ),
                    })
                );
        }, [experiments]);

    /*
     * ------------------------------------------------------------
     * Latest completed comparison
     * ------------------------------------------------------------
     */

    const latestComparison =
        useMemo(() => {
            if (
                dashboardComparisons.length ===
                0
            ) {
                return null;
            }

            return (
                dashboardComparisons[0]
                    ?.comparison || null
            );
        }, [
            dashboardComparisons,
        ]);

    /*
     * ------------------------------------------------------------
     * Performance Overview
     * ------------------------------------------------------------
     */

    const throughputData =
        useMemo(() => {
            if (
                !latestComparison ||
                !Array.isArray(
                    latestComparison.comparisons
                )
            ) {
                return [];
            }

            return latestComparison.comparisons.map(
                (item) => ({
                    architecture:
                        architectureNames[
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
        }, [
            latestComparison,
        ]);

    /*
     * ------------------------------------------------------------
     * Performance Trend
     *
     * This is based on the seven latest completed
     * experiments returned by the backend.
     * It is NOT fabricated daily data.
     * ------------------------------------------------------------
     */

    const trendData =
        useMemo(() => {
            return dashboardComparisons
                .slice()
                .reverse()
                .map(
                    ({
                        experiment,
                        comparison,
                    }) => {
                        const point = {
                            date: formatDateTime(
                                experiment.createdAt
                            ).split(",")[0],
                        };

                        if (
                            Array.isArray(
                                comparison?.comparisons
                            )
                        ) {
                            comparison.comparisons.forEach(
                                (item) => {
                                    const key =
                                        architectureKeys[
                                            item
                                                .architecture
                                        ];

                                    if (key) {
                                        point[key] =
                                            Number(
                                                item.averageThroughput ||
                                                    0
                                            );
                                    }
                                }
                            );
                        }

                        return point;
                    }
                );
        }, [
            dashboardComparisons,
        ]);

    /*
     * ------------------------------------------------------------
     * Navigation helpers
     * ------------------------------------------------------------
     */

    const openDashboard = () => {
        setCurrentPage("dashboard");
    };

    const openExperiments = () => {
        setCurrentPage("experiments");
    };

    const openHistory = () => {
        setCurrentPage("history");
    };

    const openComparison = (
        experimentId = null
    ) => {
        /*
         * If no ID was supplied from Dashboard,
         * use the newest completed experiment.
         *
         * This prevents the Comparison page from
         * opening with no experiment selected.
         */
        if (!experimentId) {
            const newestCompleted =
                [...experiments]
                    .filter(
                        (experiment) =>
                            experiment.status ===
                            "COMPLETED"
                    )
                    .sort(
                        (a, b) =>
                            new Date(
                                b.createdAt
                            ) -
                            new Date(
                                a.createdAt
                            )
                    )[0];

            if (newestCompleted) {
                experimentId =
                    newestCompleted.id;
            }
        }

        setCurrentExperimentId(
            experimentId
        );

        setCurrentPage(
            "comparison"
        );
    };

    const openSettings = () => {
        setCurrentPage("settings");
    };

    const openNewExperiment = () => {
        setCurrentPage(
            "new-experiment"
        );
    };

    const openExperimentDetails = (
        experimentId
    ) => {
        setCurrentExperimentId(
            experimentId
        );

        setCurrentPage(
            "experiment-details"
        );
    };

    const openResults = () => {
        setCurrentPage("results");
    };

    /*
     * ============================================================
     * NEW EXPERIMENT
     * ============================================================
     */

    if (
        currentPage ===
        "new-experiment"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <NewExperiment

                        onBack={
                            openDashboard
                        }

                        onCreated={(
                            experiment
                        ) => {

                            setCurrentExperimentId(
                                experiment.id
                            );

                            setCurrentPage(
                                "experiment-details"
                            );

                        }}

                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * EXPERIMENTS PAGE
     * ============================================================
     */

    if (
        currentPage ===
        "experiments"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <Experiments

                        onBack={
                            openDashboard
                        }

                        onOpenExperiment={
                            openExperimentDetails
                        }

                        onCompare={
                            openComparison
                        }

                        onNewExperiment={
                            openNewExperiment
                        }

                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * SETTINGS PAGE
     * ============================================================
     */

    if (
        currentPage ===
        "settings"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <Settings
                        onBack={
                            openDashboard
                        }
                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * EXPERIMENT DETAILS
     * ============================================================
     */

    if (
        currentPage ===
        "experiment-details"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <ExperimentDetails

                        experimentId={
                            currentExperimentId
                        }

                        onBack={() =>
                            setCurrentPage(
                                "experiments"
                            )
                        }

                        onViewResults={
                            openResults
                        }

                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * RESULTS
     * ============================================================
     */

    if (
        currentPage ===
        "results"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <Results

                        experimentId={
                            currentExperimentId
                        }

                        onBack={() =>
                            setCurrentPage(
                                "experiment-details"
                            )
                        }

                        onComparison={() =>
                            openComparison(
                                currentExperimentId
                            )
                        }

                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * COMPARISON
     * ============================================================
     */

    if (
        currentPage ===
        "comparison"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <Comparison

                        selectedExperimentId={
                            currentExperimentId
                        }

                        onSelectExperiment={(
                            experimentId
                        ) =>
                            setCurrentExperimentId(
                                experimentId
                            )
                        }

                        onBack={() =>
                            setCurrentPage(
                                "results"
                            )
                        }

                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * HISTORY
     * ============================================================
     */

    if (
        currentPage ===
        "history"
    ) {
        return (
            <div className="app-shell">

                <main className="main-content">

                    <History

                        onBack={
                            openDashboard
                        }

                        onOpenExperiment={(
                            experimentId
                        ) =>
                            openExperimentDetails(
                                experimentId
                            )
                        }

                    />

                </main>

            </div>
        );
    }

    /*
     * ============================================================
     * DASHBOARD
     * ============================================================
     */

    return (
        <div className="app-shell">

            {/* ==================================================
                SIDEBAR
            ================================================== */}

            <aside className="sidebar">

                <div className="brand">

                    <div className="brand-mark">
                        SB
                    </div>

                    <div>

                        <div className="brand-name">
                            ServerBench
                        </div>

                        <div className="brand-subtitle">
                            Benchmark. Compare. Optimize.
                        </div>

                    </div>

                </div>

                <nav className="sidebar-nav">

                    <a
                        href="#"
                        className="nav-item active"
                        onClick={(
                            event
                        ) => {
                            event.preventDefault();
                            openDashboard();
                        }}
                    >
                        <span className="nav-icon">
                            ⌂
                        </span>

                        Dashboard
                    </a>

                    <a
                        href="#"
                        className="nav-item"
                        onClick={(
                            event
                        ) => {
                            event.preventDefault();
                            openExperiments();
                        }}
                    >
                        <span className="nav-icon">
                            ▣
                        </span>

                        Experiments
                    </a>

                    <a
                        href="#"
                        className="nav-item"
                        onClick={(
                            event
                        ) => {
                            event.preventDefault();
                            openHistory();
                        }}
                    >
                        <span className="nav-icon">
                            ◷
                        </span>

                        History
                    </a>

                    <a
                        href="#"
                        className="nav-item"
                        onClick={(
                            event
                        ) => {
                            event.preventDefault();
                            openComparison();
                        }}
                    >
                        <span className="nav-icon">
                            ◫
                        </span>

                        Comparison
                    </a>

                    <a
                        href="#"
                        className="nav-item"
                        onClick={(
                            event
                        ) => {
                            event.preventDefault();
                            openSettings();
                        }}
                    >
                        <span className="nav-icon">
                            ⚙
                        </span>

                        Settings
                    </a>

                </nav>

                <div className="sidebar-footer">

                    <div className="sidebar-info">

                        <div className="sidebar-info-title">
                            About ServerBench
                        </div>

                        <p>
                            Benchmark server architectures,
                            compare performance, and analyze
                            results.
                        </p>

                        <span className="sidebar-version">
                            Version 1.0.0
                        </span>

                    </div>

                    <div className="profile-card">

                        <div className="profile-avatar">
                            AB
                        </div>

                        <div className="profile-details">

                            <strong>
                                Anirban
                            </strong>

                            <span>
                                Developer
                            </span>

                        </div>

                    </div>

                </div>

            </aside>

            {/* ==================================================
                MAIN CONTENT
            ================================================== */}

            <main className="main-content">

                <header className="topbar">

                    <button
                        className="mobile-menu-button"
                        type="button"
                    >
                        ☰
                    </button>

                    <div className="topbar-actions">

                        <button
                            className="icon-button"
                            type="button"
                            aria-label="Notifications"
                        >
                            ♢
                        </button>

                        <div className="topbar-profile">

                            <div className="topbar-avatar">
                                AB
                            </div>

                            <div className="topbar-user">

                                <strong>
                                    Anirban
                                </strong>

                                <span>
                                    Developer
                                </span>

                            </div>

                        </div>

                    </div>

                </header>

                <div className="page-content">

                    {/* ==================================================
                        PAGE HEADER
                    ================================================== */}

                    <section className="page-header">

                        <div>

                            <h1>
                                Dashboard
                            </h1>

                            <p>
                                Overview of your benchmarking
                                activity and performance.
                            </p>

                        </div>

                        <button
                            className="primary-button"
                            type="button"
                            onClick={
                                openNewExperiment
                            }
                        >
                            <span>
                                +
                            </span>

                            New Experiment
                        </button>

                    </section>

                    {/* ==================================================
                        DASHBOARD ERROR
                    ================================================== */}

                    {dashboardError && (
                        <section className="dashboard-section">

                            <article className="card">

                                <div
                                    style={{
                                        padding:
                                            "20px",
                                        color:
                                            "#dc2626",
                                    }}
                                >
                                    {dashboardError}
                                </div>

                            </article>

                        </section>
                    )}

                    {/* ==================================================
                        KPI CARDS
                    ================================================== */}

                    <section className="metrics-grid">

                        <article className="metric-card">

                            <div className="metric-card-top">

                                <div className="metric-icon metric-blue">
                                    ◈
                                </div>

                                <span className="metric-label">
                                    Total Experiments
                                </span>

                            </div>

                            <div className="metric-value">
                                {dashboardLoading
                                    ? "—"
                                    : totalExperiments}
                            </div>

                            <div className="metric-footer">
                                All time
                            </div>

                        </article>

                        <article className="metric-card">

                            <div className="metric-card-top">

                                <div className="metric-icon metric-green">
                                    ✓
                                </div>

                                <span className="metric-label">
                                    Completed
                                </span>

                            </div>

                            <div className="metric-value">
                                {dashboardLoading
                                    ? "—"
                                    : completedExperiments}
                            </div>

                            <div className="metric-footer">
                                {dashboardLoading
                                    ? "Loading..."
                                    : `${completionRate}% completion rate`}
                            </div>

                        </article>

                        <article className="metric-card">

                            <div className="metric-card-top">

                                <div className="metric-icon metric-orange">
                                    ◷
                                </div>

                                <span className="metric-label">
                                    Running
                                </span>

                            </div>

                            <div className="metric-value">
                                {dashboardLoading
                                    ? "—"
                                    : runningExperiments}
                            </div>

                            <div className="metric-footer">
                                Currently in progress
                            </div>

                        </article>

                        <article className="metric-card">

                            <div className="metric-card-top">

                                <div className="metric-icon metric-red">
                                    ×
                                </div>

                                <span className="metric-label">
                                    Failed
                                </span>

                            </div>

                            <div className="metric-value">
                                {dashboardLoading
                                    ? "—"
                                    : failedExperiments}
                            </div>

                            <div className="metric-footer">
                                {dashboardLoading
                                    ? "Loading..."
                                    : `${failureRate}% failure rate`}
                            </div>

                        </article>

                    </section>

                    {/* ==================================================
                        RECENT EXPERIMENTS
                    ================================================== */}

                    <section className="dashboard-section">

                        <article className="card">

                            <div className="card-header">

                                <div>

                                    <h2>
                                        Recent Experiments
                                    </h2>

                                    <p>
                                        Latest benchmark activity
                                    </p>

                                </div>

                                <button
                                    className="text-button"
                                    type="button"
                                    onClick={
                                        openExperiments
                                    }
                                >
                                    View all
                                </button>

                            </div>

                            <div className="table-wrapper">

                                <table className="experiments-table">

                                    <thead>

                                        <tr>

                                            <th>
                                                Experiment
                                            </th>

                                            <th>
                                                Status
                                            </th>

                                            <th>
                                                Architectures
                                            </th>

                                            <th>
                                                Repetitions
                                            </th>

                                            <th>
                                                Date
                                            </th>

                                            <th>
                                                Action
                                            </th>

                                        </tr>

                                    </thead>

                                    <tbody>

                                        {dashboardLoading ? (
                                            <tr>

                                                <td
                                                    colSpan="6"
                                                    style={{
                                                        textAlign:
                                                            "center",
                                                        padding:
                                                            "30px",
                                                    }}
                                                >
                                                    Loading experiments...
                                                </td>

                                            </tr>
                                        ) : recentExperiments.length ===
                                          0 ? (
                                            <tr>

                                                <td
                                                    colSpan="6"
                                                    style={{
                                                        textAlign:
                                                            "center",
                                                        padding:
                                                            "30px",
                                                    }}
                                                >
                                                    No experiments found.
                                                </td>

                                            </tr>
                                        ) : (
                                            recentExperiments.map(
                                                (
                                                    experiment
                                                ) => (
                                                    <tr
                                                        key={
                                                            experiment.id
                                                        }
                                                    >

                                                        <td>

                                                            <span className="experiment-name">
                                                                {
                                                                    experiment.name
                                                                }
                                                            </span>

                                                        </td>

                                                        <td>

                                                            <StatusBadge
                                                                status={
                                                                    experiment.status
                                                                }
                                                            />

                                                        </td>

                                                        <td>
                                                            {
                                                                experiment.architectures
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                experiment.repetitions
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                experiment.date
                                                            }
                                                        </td>

                                                        <td>

                                                            <button
                                                                className="view-button"
                                                                type="button"
                                                                onClick={() =>
                                                                    openExperimentDetails(
                                                                        experiment.id
                                                                    )
                                                                }
                                                            >
                                                                View
                                                            </button>

                                                        </td>

                                                    </tr>
                                                )
                                            )
                                        )}

                                    </tbody>

                                </table>

                            </div>

                        </article>

                    </section>

                    {/* ==================================================
                        PERFORMANCE OVERVIEW
                    ================================================== */}

                    <section className="dashboard-section">

                        <article className="card">

                            <div className="card-header">

                                <div>

                                    <h2>
                                        Performance Overview
                                    </h2>

                                    <p>
                                        Throughput from the latest
                                        completed benchmark
                                    </p>

                                </div>

                                <span className="chart-unit">
                                    requests / sec
                                </span>

                            </div>

                            <div className="chart-container bar-chart-container">

                                {dashboardLoading ? (
                                    <div
                                        style={{
                                            height:
                                                "360px",
                                            display:
                                                "flex",
                                            alignItems:
                                                "center",
                                            justifyContent:
                                                "center",
                                            color:
                                                "#94a3b8",
                                            fontSize:
                                                "13px",
                                        }}
                                    >
                                        Loading performance data...
                                    </div>
                                ) : throughputData.length ===
                                  0 ? (
                                    <div
                                        style={{
                                            height:
                                                "360px",
                                            display:
                                                "flex",
                                            alignItems:
                                                "center",
                                            justifyContent:
                                                "center",
                                            color:
                                                "#94a3b8",
                                            fontSize:
                                                "13px",
                                        }}
                                    >
                                        No completed benchmark results available.
                                    </div>
                                ) : (
                                    <ResponsiveContainer
                                        width="100%"
                                        height={360}
                                    >

                                        <BarChart
                                            data={
                                                throughputData
                                            }

                                            margin={{
                                                top: 20,
                                                right: 24,
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
                                                formatter={(
                                                    value
                                                ) => [
                                                    `${Number(
                                                        value
                                                    ).toLocaleString()} req/s`,
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
                                                barSize={56}
                                            />

                                        </BarChart>

                                    </ResponsiveContainer>
                                )}

                            </div>

                        </article>

                    </section>

                    {/* ==================================================
                        PERFORMANCE TREND
                    ================================================== */}

                    <section className="dashboard-section">

                        <article className="card">

                            <div className="card-header">

                                <div>

                                    <h2>
                                        Performance Trend
                                    </h2>

                                    <p>
                                        Throughput across recent
                                        completed experiments
                                    </p>

                                </div>

                                <span className="chart-unit">
                                    requests / sec
                                </span>

                            </div>

                            <div className="chart-container">

                                {dashboardLoading ? (
                                    <div
                                        style={{
                                            height:
                                                "380px",
                                            display:
                                                "flex",
                                            alignItems:
                                                "center",
                                            justifyContent:
                                                "center",
                                            color:
                                                "#94a3b8",
                                            fontSize:
                                                "13px",
                                        }}
                                    >
                                        Loading trend data...
                                    </div>
                                ) : trendData.length ===
                                  0 ? (
                                    <div
                                        style={{
                                            height:
                                                "380px",
                                            display:
                                                "flex",
                                            alignItems:
                                                "center",
                                            justifyContent:
                                                "center",
                                            color:
                                                "#94a3b8",
                                            fontSize:
                                                "13px",
                                        }}
                                    >
                                        Not enough completed experiments for a trend.
                                    </div>
                                ) : (
                                    <ResponsiveContainer
                                        width="100%"
                                        height={380}
                                    >

                                        <LineChart
                                            data={
                                                trendData
                                            }

                                            margin={{
                                                top: 20,
                                                right: 24,
                                                left: 10,
                                                bottom: 10,
                                            }}
                                        >

                                            <CartesianGrid
                                                stroke="#eef2f7"
                                                vertical={false}
                                            />

                                            <XAxis
                                                dataKey="date"
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
                                                formatter={(
                                                    value,
                                                    name
                                                ) => [
                                                    `${Number(
                                                        value
                                                    ).toLocaleString()} req/s`,
                                                    name,
                                                ]}
                                            />

                                            <Line
                                                type="monotone"
                                                dataKey="single"
                                                name="Single Threaded"
                                                stroke="#2563eb"
                                                strokeWidth={2.5}
                                                dot={{
                                                    r: 3,
                                                }}
                                                activeDot={{
                                                    r: 5,
                                                }}
                                            />

                                            <Line
                                                type="monotone"
                                                dataKey="multi"
                                                name="Multi Threaded"
                                                stroke="#16a34a"
                                                strokeWidth={2.5}
                                                dot={{
                                                    r: 3,
                                                }}
                                                activeDot={{
                                                    r: 5,
                                                }}
                                            />

                                            <Line
                                                type="monotone"
                                                dataKey="pool"
                                                name="Thread Pool"
                                                stroke="#f59e0b"
                                                strokeWidth={2.5}
                                                dot={{
                                                    r: 3,
                                                }}
                                                activeDot={{
                                                    r: 5,
                                                }}
                                            />

                                            <Line
                                                type="monotone"
                                                dataKey="virtual"
                                                name="Virtual Thread"
                                                stroke="#8b5cf6"
                                                strokeWidth={2.5}
                                                dot={{
                                                    r: 3,
                                                }}
                                                activeDot={{
                                                    r: 5,
                                                }}
                                            />

                                        </LineChart>

                                    </ResponsiveContainer>
                                )}

                            </div>

                        </article>

                    </section>

                    {/* ==================================================
                        QUICK ACTIONS
                    ================================================== */}

                    <section className="dashboard-section">

                        <article className="card">

                            <div className="card-header">

                                <div>

                                    <h2>
                                        Quick Actions
                                    </h2>

                                    <p>
                                        Common benchmarking tasks
                                    </p>

                                </div>

                            </div>

                            <div className="quick-actions">

                                <button
                                    className="quick-action"
                                    type="button"
                                    onClick={
                                        openNewExperiment
                                    }
                                >

                                    <span className="quick-action-icon">
                                        +
                                    </span>

                                    <span>

                                        <strong>
                                            Create New Experiment
                                        </strong>

                                        <small>
                                            Configure a new benchmark
                                        </small>

                                    </span>

                                    <span className="quick-action-arrow">
                                        →
                                    </span>

                                </button>

                                <button
                                    className="quick-action"
                                    type="button"
                                    onClick={
                                        openExperiments
                                    }
                                >

                                    <span className="quick-action-icon">
                                        ▤
                                    </span>

                                    <span>

                                        <strong>
                                            View All Experiments
                                        </strong>

                                        <small>
                                            Browse experiments
                                        </small>

                                    </span>

                                    <span className="quick-action-arrow">
                                        →
                                    </span>

                                </button>

                                <button
                                    className="quick-action"
                                    type="button"
                                    onClick={() =>
                                        openComparison()
                                    }
                                >

                                    <span className="quick-action-icon">
                                        ◫
                                    </span>

                                    <span>

                                        <strong>
                                            Compare Results
                                        </strong>

                                        <small>
                                            Compare the latest completed experiment
                                        </small>

                                    </span>

                                    <span className="quick-action-arrow">
                                        →
                                    </span>

                                </button>

                                <button
                                    className="quick-action"
                                    type="button"
                                    onClick={
                                        openHistory
                                    }
                                >

                                    <span className="quick-action-icon">
                                        ◷
                                    </span>

                                    <span>

                                        <strong>
                                            View History
                                        </strong>

                                        <small>
                                            Review previous benchmarks
                                        </small>

                                    </span>

                                    <span className="quick-action-arrow">
                                        →
                                    </span>

                                </button>

                            </div>

                        </article>

                    </section>

                </div>

            </main>

        </div>
    );
}

export default App;