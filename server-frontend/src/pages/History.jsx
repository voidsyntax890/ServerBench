import { useMemo, useState } from "react";
import "./History.css";

const experiments = [
    {
        id: "0debaf9f-6004-4a40-9bac-4dc43239a2ec",
        name: "M5 Restart Persistence Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        createdAt: "31 Aug 2026, 12:01 AM",
        duration: "12.28 sec",
    },
    {
        id: "d3920e39-95f3-405c-a412-7f9fb28219d2",
        name: "M5 Metrics Persistence Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        createdAt: "30 Aug 2026, 11:49 PM",
        duration: "12.04 sec",
    },
    {
        id: "a76e0575-818e-40d6-a6ac-2fd3f4522a98",
        name: "M5 Run Persistence Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        createdAt: "30 Aug 2026, 11:45 PM",
        duration: "12.31 sec",
    },
    {
        id: "dea0a3f5-99be-483e-af36-ef81e52ba11c",
        name: "M4 Final Four Architecture Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        createdAt: "29 Aug 2026, 06:04 PM",
        duration: "12.11 sec",
    },
    {
        id: "f1a5a8d1-2c10-4e1f-8d42-7b7d0c9f4e11",
        name: "Failure Handling Test",
        status: "FAILED",
        architectures: 4,
        repetitions: 1,
        createdAt: "29 Aug 2026, 05:40 PM",
        duration: "3.02 sec",
    },
    {
        id: "c8b32f41-6e95-47cb-b42e-6f43f7c7b191",
        name: "Repeated Start Protection Test",
        status: "COMPLETED",
        architectures: 1,
        repetitions: 1,
        createdAt: "29 Aug 2026, 05:32 PM",
        duration: "4.18 sec",
    },
];

function StatusBadge({ status }) {
    return (
        <span className={`history-status status-${status.toLowerCase()}`}>
            <span className="history-status-dot" />
            {status}
        </span>
    );
}

function History({ onBack, onOpenExperiment }) {
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");

    const filteredExperiments = useMemo(() => {
        const query = search.trim().toLowerCase();

        return experiments.filter((experiment) => {
            const matchesSearch =
                !query ||
                experiment.name.toLowerCase().includes(query);

            const matchesStatus =
                statusFilter === "ALL" ||
                experiment.status === statusFilter;

            return matchesSearch && matchesStatus;
        });
    }, [search, statusFilter]);

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

                    <h1>Experiment History</h1>

                    <p>
                        Review previously created benchmark experiments
                        and their execution status.
                    </p>
                </div>

                <div className="history-count">
                    <strong>{filteredExperiments.length}</strong>
                    <span>experiments shown</span>
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
                            setSearch(event.target.value)
                        }
                    />

                    {search && (
                        <button
                            className="clear-search"
                            type="button"
                            onClick={() => setSearch("")}
                            aria-label="Clear search"
                        >
                            ×
                        </button>
                    )}

                </div>

                <div className="history-filter-group">

                    <span>Status</span>

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
                        <option value="FAILED">
                            Failed
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
                        <h2>Experiments</h2>

                        <p>
                            Historical benchmark configurations and runs.
                        </p>
                    </div>

                    <span className="history-total">
                        {filteredExperiments.length} results
                    </span>

                </div>

                {filteredExperiments.length > 0 ? (
                    <div className="history-table-wrapper">

                        <table className="history-table">

                            <thead>

                                <tr>
                                    <th>Experiment</th>
                                    <th>Status</th>
                                    <th>Architectures</th>
                                    <th>Repetitions</th>
                                    <th>Created</th>
                                    <th>Duration</th>
                                    <th>Action</th>
                                </tr>

                            </thead>

                            <tbody>

                                {filteredExperiments.map(
                                    (experiment) => (
                                        <tr
                                            key={experiment.id}
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
                                                        experiment.architectures
                                                    }
                                                </span>

                                                <span className="history-muted">
                                                    {" "}
                                                    selected
                                                </span>
                                            </td>

                                            <td>
                                                {
                                                    experiment.repetitions
                                                }
                                            </td>

                                            <td>
                                                {
                                                    experiment.createdAt
                                                }
                                            </td>

                                            <td>
                                                {
                                                    experiment.duration
                                                }
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
                                                    <span>→</span>
                                                </button>
                                            </td>

                                        </tr>
                                    )
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
                                setStatusFilter("ALL");
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
                Experiment history will be loaded from the
                PostgreSQL-backed API during integration.
            </div>

        </div>
    );
}

export default History;