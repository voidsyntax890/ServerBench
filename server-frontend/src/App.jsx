import { useState } from "react";

import NewExperiment from "./pages/NewExperiment";
import ExperimentDetails from "./pages/ExperimentDetails";
import Results from "./pages/Results";
import Comparison from "./pages/Comparison";
import History from "./pages/History";
import Experiments from "./pages/Experiments";
import Settings from "./pages/Settings";

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
 * TEMPORARY UI DATA
 * ================================================================
 *
 * These values are only for the current UI-development phase.
 *
 * Later:
 *
 * React
 *    ↓
 * Spring Boot REST API
 *    ↓
 * PostgreSQL
 *
 * All of these mock values will be removed.
 */

const recentExperiments = [
    {
        id: "0debaf9f-6004-4a40-9bac-4dc43239a2ec",
        name: "M5 Restart Persistence Test",
        status: "COMPLETED",
        architectures: "4 / 4",
        repetitions: 1,
        date: "31 Aug 2026, 12:01 AM",
    },
    {
        id: "d3920e39-95f3-405c-a412-7f9fb28219d2",
        name: "M5 Metrics Persistence Test",
        status: "COMPLETED",
        architectures: "4 / 4",
        repetitions: 1,
        date: "30 Aug 2026, 11:49 PM",
    },
    {
        id: "a76e0575-818e-40d6-a6ac-2fd3f4522a98",
        name: "M5 Run Persistence Test",
        status: "COMPLETED",
        architectures: "4 / 4",
        repetitions: 1,
        date: "30 Aug 2026, 11:45 PM",
    },
    {
        id: "dea0a3f5-99be-483e-af36-ef81e52ba11c",
        name: "M4 Final Four Architecture Test",
        status: "COMPLETED",
        architectures: "4 / 4",
        repetitions: 1,
        date: "29 Aug 2026, 06:04 PM",
    },
    {
        id: "failure-test",
        name: "Failure Handling Test",
        status: "FAILED",
        architectures: "4 / 4",
        repetitions: 1,
        date: "29 Aug 2026, 05:40 PM",
    },
];

const throughputData = [
    {
        architecture: "Single Threaded",
        throughput: 14028,
    },
    {
        architecture: "Multi Threaded",
        throughput: 66336,
    },
    {
        architecture: "Thread Pool",
        throughput: 63016,
    },
    {
        architecture: "Virtual Thread",
        throughput: 49548,
    },
];

const trendData = [
    {
        date: "25 Aug",
        single: 12000,
        multi: 62000,
        pool: 54000,
        virtual: 36000,
    },
    {
        date: "26 Aug",
        single: 13000,
        multi: 70000,
        pool: 49000,
        virtual: 41000,
    },
    {
        date: "27 Aug",
        single: 13500,
        multi: 73500,
        pool: 54000,
        virtual: 39000,
    },
    {
        date: "28 Aug",
        single: 12500,
        multi: 71500,
        pool: 51000,
        virtual: 43000,
    },
    {
        date: "29 Aug",
        single: 14000,
        multi: 76000,
        pool: 55000,
        virtual: 41000,
    },
    {
        date: "30 Aug",
        single: 13800,
        multi: 74500,
        pool: 53000,
        virtual: 39000,
    },
    {
        date: "31 Aug",
        single: 14028,
        multi: 66336,
        pool: 63016,
        virtual: 49548,
    },
];

/*
 * ================================================================
 * STATUS BADGE
 * ================================================================
 */

function StatusBadge({ status }) {
    return (
        <span
            className={`status-badge status-${status.toLowerCase()}`}
        >
            <span className="status-dot" />
            {status}
        </span>
    );
}

/*
 * ================================================================
 * APP
 * ================================================================
 */

function App() {

    /*
     * Current screen.
     *
     * Later this will eventually be replaced by proper routing.
     */
    const [currentPage, setCurrentPage] =
        useState("dashboard");

    /*
     * Stores the experiment currently being viewed.
     *
     * This is the important addition for our comparison problem.
     */
    const [currentExperimentId, setCurrentExperimentId] =
        useState(null);

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
        setCurrentExperimentId(
            experimentId
        );

        setCurrentPage("comparison");
    };

    const openSettings = () => {
        setCurrentPage("settings");
    };

    const openNewExperiment = () => {
        setCurrentPage("new-experiment");
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

                        onCreated={(experiment) => {

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
                                "dashboard"
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
                        onClick={(event) => {
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
                        onClick={(event) => {
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
                        onClick={(event) => {
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
                        onClick={(event) => {
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
                        onClick={(event) => {
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
                                24
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
                                18
                            </div>

                            <div className="metric-footer">
                                75% completion rate
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
                                2
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
                                4
                            </div>

                            <div className="metric-footer">
                                17% failure rate
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

                                        {recentExperiments.map(
                                            (experiment) => (
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
                                        benchmark
                                    </p>

                                </div>

                                <span className="chart-unit">
                                    requests / sec
                                </span>

                            </div>

                            <div className="chart-container bar-chart-container">

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
                                            formatter={(value) => [
                                                `${Number(
                                                    value
                                                ).toLocaleString()} req/s`,
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
                                            barSize={56}
                                        />

                                    </BarChart>

                                </ResponsiveContainer>

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
                                        Throughput across server
                                        architectures
                                    </p>

                                </div>

                                <span className="chart-unit">
                                    requests / sec
                                </span>

                            </div>

                            <div className="chart-container">

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
                                            contentStyle={{
                                                border:
                                                    "1px solid #e5e7eb",
                                                borderRadius:
                                                    "8px",
                                                boxShadow:
                                                    "0 4px 16px rgba(15, 23, 42, 0.08)",
                                            }}
                                        />

                                        <Line
                                            type="monotone"
                                            dataKey="single"
                                            name="Single Threaded"
                                            stroke="#2563eb"
                                            strokeWidth={2.5}
                                            dot={{ r: 3 }}
                                            activeDot={{ r: 5 }}
                                        />

                                        <Line
                                            type="monotone"
                                            dataKey="multi"
                                            name="Multi Threaded"
                                            stroke="#16a34a"
                                            strokeWidth={2.5}
                                            dot={{ r: 3 }}
                                            activeDot={{ r: 5 }}
                                        />

                                        <Line
                                            type="monotone"
                                            dataKey="pool"
                                            name="Thread Pool"
                                            stroke="#f59e0b"
                                            strokeWidth={2.5}
                                            dot={{ r: 3 }}
                                            activeDot={{ r: 5 }}
                                        />

                                        <Line
                                            type="monotone"
                                            dataKey="virtual"
                                            name="Virtual Thread"
                                            stroke="#8b5cf6"
                                            strokeWidth={2.5}
                                            dot={{ r: 3 }}
                                            activeDot={{ r: 5 }}
                                        />

                                    </LineChart>

                                </ResponsiveContainer>

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
                                            Select an experiment to compare
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