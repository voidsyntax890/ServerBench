import { useEffect, useMemo, useState } from "react";

import { getExperiments } from "../services/experimentApi";

import "./History.css";

function StatusBadge({ status }) {
    const safeStatus = status || "UNKNOWN";

    return (
        <span
            className={`history-status status-${safeStatus.toLowerCase()}`}
        >
            <span className="history-status-dot" />
            {safeStatus}
        </span>
    );
}

function formatCreatedAt(value) {
    if (!value) {
        return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString(undefined, {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function History({
    onBack,
    onOpenExperiment,
}) {
    const [experiments, setExperiments] =
        useState([]);

    const [search, setSearch] =
        useState("");

    const [statusFilter, setStatusFilter] =
        useState("ALL");

    const [isLoading, setIsLoading] =
        useState(true);

    const [loadError, setLoadError] =
        useState("");

    useEffect(() => {
        let mounted = true;

        async function loadExperiments() {
            setIsLoading(true);
            setLoadError("");

            try {
                const response =
                    await getExperiments();

                const data =
                    Array.isArray(response)
                        ? response
                        : response?.experiments || [];

                if (mounted) {
                    setExperiments(data);
                }
            } catch (error) {
                if (mounted) {
                    setLoadError(
                        error.message ||
                            "Unable to load experiment history."
                    );
                }
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        }

        loadExperiments();

        return () => {
            mounted = false;
        };
    }, []);

    const filteredExperiments =
        useMemo(() => {
            const query =
                search.trim().toLowerCase();

            return experiments.filter(
                (experiment) => {
                    const matchesSearch =
                        !query ||
                        String(
                            experiment.name || ""
                        )
                            .toLowerCase()
                            .includes(query) ||
                        String(
                            experiment.id || ""
                        )
                            .toLowerCase()
                            .includes(query);

                    const matchesStatus =
                        statusFilter === "ALL" ||
                        experiment.status ===
                            statusFilter;

                    return (
                        matchesSearch &&
                        matchesStatus
                    );
                }
            );
        }, [
            experiments,
            search,
            statusFilter,
        ]);

    return (
        <div className="history-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <section className="history-header">

                <div>

                    <button
                        className="history-back-button"
                        type="button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Dashboard
                    </button>

                    <div className="history-breadcrumb">
                        ServerBench / History
                    </div>

                    <h1>
                        Experiment History
                    </h1>

                    <p>
                        Review previously created benchmark experiments
                        and their execution status.
                    </p>

                </div>

                <div className="history-count">
                    <strong>
                        {filteredExperiments.length}
                    </strong>

                    <span>
                        experiments shown
                    </span>
                </div>

            </section>

            {/* ==================================================
                FILTERS
            ================================================== */}

            <section className="history-card history-filters">

                <div className="history-search">

                    <span className="history-search-icon">
                        ⌕
                    </span>

                    <input
                        type="search"
                        placeholder="Search experiments..."
                        value={search}
                        onChange={(event) =>
                            setSearch(
                                event.target.value
                            )
                        }
                    />

                    {search && (
                        <button
                            className="clear-search"
                            type="button"
                            onClick={() =>
                                setSearch("")
                            }
                            aria-label="Clear search"
                        >
                            ×
                        </button>
                    )}

                </div>

                <div className="history-filter-group">

                    <span>
                        Status
                    </span>

                    <select
                        value={statusFilter}
                        onChange={(event) =>
                            setStatusFilter(
                                event.target.value
                            )
                        }
                    >

                        <option value="ALL">
                            All statuses
                        </option>

                        <option value="COMPLETED">
                            Completed
                        </option>

                        <option value="RUNNING">
                            Running
                        </option>

                        <option value="CREATED">
                            Created
                        </option>

                        <option value="FAILED">
                            Failed
                        </option>

                        <option value="CANCELLED">
                            Cancelled
                        </option>

                    </select>

                </div>

            </section>

            {/* ==================================================
                HISTORY TABLE
            ================================================== */}

            <section className="history-card">

                <div className="history-card-header">

                    <div>

                        <h2>
                            Experiments
                        </h2>

                        <p>
                            Historical benchmark configurations and runs.
                        </p>

                    </div>

                    <span className="history-total">
                        {filteredExperiments.length}{" "}
                        {filteredExperiments.length ===
                        1
                            ? "result"
                            : "results"}
                    </span>

                </div>

                {isLoading ? (
                    <div className="history-empty">

                        <h3>
                            Loading experiment history...
                        </h3>

                        <p>
                            Retrieving persisted experiments from ServerBench.
                        </p>

                    </div>
                ) : loadError ? (
                    <div className="history-empty">

                        <h3>
                            Unable to load history
                        </h3>

                        <p>
                            {loadError}
                        </p>

                        <button
                            type="button"
                            onClick={() => {
                                setLoadError("");
                                setIsLoading(true);
                            }}
                        >
                            Retry
                        </button>

                    </div>
                ) : filteredExperiments.length >
                  0 ? (
                    <div className="history-table-wrapper">

                        <table className="history-table">

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
                                        Created
                                    </th>

                                    <th>
                                        Duration
                                    </th>

                                    <th>
                                        Action
                                    </th>

                                </tr>

                            </thead>

                            <tbody>

                                {filteredExperiments.map(
                                    (experiment) => {

                                        const architectureCount =
                                            Array.isArray(
                                                experiment.architectures
                                            )
                                                ? experiment
                                                      .architectures
                                                      .length
                                                : 0;

                                        return (
                                            <tr
                                                key={
                                                    experiment.id
                                                }
                                                className="history-row"
                                            >

                                                <td>

                                                    <div className="history-experiment">

                                                        <strong>
                                                            {
                                                                experiment.name
                                                            }
                                                        </strong>

                                                        <span>
                                                            {
                                                                experiment.id
                                                            }
                                                        </span>

                                                    </div>

                                                </td>

                                                <td>

                                                    <StatusBadge
                                                        status={
                                                            experiment.status
                                                        }
                                                    />

                                                </td>

                                                <td>

                                                    <span className="history-number">
                                                        {
                                                            architectureCount
                                                        }
                                                    </span>

                                                    <span className="history-muted">
                                                        {" "}
                                                        selected
                                                    </span>

                                                </td>

                                                <td>
                                                    {
                                                        experiment.repetitions ??
                                                        "—"
                                                    }
                                                </td>

                                                <td>
                                                    {formatCreatedAt(
                                                        experiment.createdAt
                                                    )}
                                                </td>

                                                <td>
                                                    —
                                                </td>

                                                <td>

                                                    <button
                                                        className="history-view-button"
                                                        type="button"
                                                        onClick={() =>
                                                            onOpenExperiment(
                                                                experiment.id
                                                            )
                                                        }
                                                    >
                                                        View
                                                        <span>
                                                            →
                                                        </span>
                                                    </button>

                                                </td>

                                            </tr>
                                        );
                                    }
                                )}

                            </tbody>

                        </table>

                    </div>
                ) : (
                    <div className="history-empty">

                        <div className="history-empty-icon">
                            ⌕
                        </div>

                        <h3>
                            No experiments found
                        </h3>

                        <p>
                            Try a different search term or
                            status filter.
                        </p>

                        <button
                            type="button"
                            onClick={() => {
                                setSearch("");
                                setStatusFilter(
                                    "ALL"
                                );
                            }}
                        >
                            Clear filters
                        </button>

                    </div>
                )}

            </section>

            {/* ==================================================
                FOOTNOTE
            ================================================== */}

            <div className="history-note">
                Experiment history is loaded from the
                PostgreSQL-backed ServerBench API.
            </div>

        </div>
    );
}

export default History;