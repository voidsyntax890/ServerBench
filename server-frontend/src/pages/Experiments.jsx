import { useEffect, useState } from "react";

import { getExperiments } from "../services/experimentApi";

import "./Experiments.css";

function StatusBadge({ status }) {
    return (
        <span
            className={`experiment-status status-${String(
                status
            ).toLowerCase()}`}
        >
            <span className="experiment-status-dot" />
            {status}
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

function Experiments({
    onBack,
    onOpenExperiment,
    onCompare,
    onNewExperiment,
}) {
    const [experiments, setExperiments] =
        useState([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [loadError, setLoadError] =
        useState("");

    useEffect(() => {
        let isMounted = true;

        const loadExperiments = async () => {
            setIsLoading(true);
            setLoadError("");

            try {
                const response =
                    await getExperiments();

                const data =
                    Array.isArray(response)
                        ? response
                        : response?.experiments || [];

                if (isMounted) {
                    setExperiments(data);
                }
            } catch (error) {
                if (isMounted) {
                    setLoadError(
                        error.message ||
                            "Unable to load experiments."
                    );
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        };

        loadExperiments();

        return () => {
            isMounted = false;
        };
    }, []);

    return (
        <div className="experiments-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <button
                className="experiments-back"
                type="button"
                onClick={onBack}
            >
                <span>←</span>
                Back to Dashboard
            </button>

            <section className="experiments-header">

                <div>
                    <div className="experiments-breadcrumb">
                        ServerBench / Experiments
                    </div>

                    <h1>Experiments</h1>

                    <p>
                        Manage benchmark experiments and review
                        their execution status.
                    </p>
                </div>

                <button
                    className="experiments-create-button"
                    type="button"
                    onClick={onNewExperiment}
                >
                    <span>+</span>
                    New Experiment
                </button>

            </section>

            {/* ==================================================
                EXPERIMENT LIST
            ================================================== */}

            <section className="experiments-card">

                <div className="experiments-card-header">

                    <div>
                        <h2>All Experiments</h2>

                        <p>
                            Previously created benchmark experiments.
                        </p>
                    </div>

                    <span className="experiments-count">
                        {experiments.length}{" "}
                        {experiments.length === 1
                            ? "experiment"
                            : "experiments"}
                    </span>

                </div>

                {isLoading && (
                    <div className="experiments-state">
                        Loading experiments...
                    </div>
                )}

                {!isLoading && loadError && (
                    <div className="experiments-state experiments-state-error">
                        <strong>
                            Unable to load experiments.
                        </strong>

                        <span>
                            {loadError}
                        </span>
                    </div>
                )}

                {!isLoading &&
                    !loadError &&
                    experiments.length === 0 && (
                        <div className="experiments-state">
                            <strong>
                                No experiments yet.
                            </strong>

                            <span>
                                Create your first benchmark experiment
                                to see it here.
                            </span>
                        </div>
                    )}

                {!isLoading &&
                    !loadError &&
                    experiments.length > 0 && (
                        <div className="experiments-table-wrapper">

                            <table className="experiments-list-table">

                                <thead>
                                    <tr>
                                        <th>Experiment</th>
                                        <th>Status</th>
                                        <th>Architectures</th>
                                        <th>Repetitions</th>
                                        <th>Created</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>

                                <tbody>

                                    {experiments.map(
                                        (experiment) => {

                                            const architectureCount =
                                                Array.isArray(
                                                    experiment.architectures
                                                )
                                                    ? experiment
                                                          .architectures
                                                          .length
                                                    : Number(
                                                          experiment.architectures ||
                                                              0
                                                      );

                                            const status =
                                                experiment.status ||
                                                "UNKNOWN";

                                            return (
                                                <tr
                                                    key={
                                                        experiment.id
                                                    }
                                                >

                                                    <td>
                                                        <div className="experiment-name-cell">

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
                                                                status
                                                            }
                                                        />
                                                    </td>

                                                    <td>
                                                        <strong className="table-number">
                                                            {
                                                                architectureCount
                                                            }
                                                        </strong>

                                                        <span className="table-muted">
                                                            {" "}
                                                            selected
                                                        </span>
                                                    </td>

                                                    <td>
                                                        {
                                                            experiment.repetitions ??
                                                            0
                                                        }
                                                    </td>

                                                    <td>
                                                        {formatCreatedAt(
                                                            experiment.createdAt
                                                        )}
                                                    </td>

                                                    <td>

                                                        <div className="experiment-actions">

                                                            <button
                                                                type="button"
                                                                onClick={() =>
                                                                    onOpenExperiment(
                                                                        experiment.id
                                                                    )
                                                                }
                                                            >
                                                                View
                                                            </button>

                                                            <button
                                                                type="button"
                                                                disabled={
                                                                    status !==
                                                                    "COMPLETED"
                                                                }
                                                                onClick={() =>
                                                                    onCompare(
                                                                        experiment.id
                                                                    )
                                                                }
                                                            >
                                                                Compare
                                                            </button>

                                                        </div>

                                                    </td>

                                                </tr>
                                            );
                                        }
                                    )}

                                </tbody>

                            </table>

                        </div>
                    )}

            </section>

        </div>
    );
}

export default Experiments;