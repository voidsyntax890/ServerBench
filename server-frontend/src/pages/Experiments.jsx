import "./Experiments.css";

const experiments = [
    {
        id: "0debaf9f-6004-4a40-9bac-4dc43239a2ec",
        name: "M5 Restart Persistence Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        date: "31 Aug 2026, 12:01 AM",
    },
    {
        id: "d3920e39-95f3-405c-a412-7f9fb28219d2",
        name: "M5 Metrics Persistence Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        date: "30 Aug 2026, 11:49 PM",
    },
    {
        id: "a76e0575-818e-40d6-a6ac-2fd3f4522a98",
        name: "M5 Run Persistence Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        date: "30 Aug 2026, 11:45 PM",
    },
    {
        id: "dea0a3f5-99be-483e-af36-ef81e52ba11c",
        name: "M4 Final Four Architecture Test",
        status: "COMPLETED",
        architectures: 4,
        repetitions: 1,
        date: "29 Aug 2026, 06:04 PM",
    },
    {
        id: "97f65b2e-99ab-41eb-97be-f6b00c42cbaa",
        name: "M5 Persistence Test",
        status: "CREATED",
        architectures: 4,
        repetitions: 1,
        date: "30 Aug 2026, 11:39 PM",
    },
];

function StatusBadge({ status }) {
    return (
        <span
            className={`experiment-status status-${status.toLowerCase()}`}
        >
            <span className="experiment-status-dot" />
            {status}
        </span>
    );
}

function Experiments({
    onBack,
    onOpenExperiment,
    onCompare,
    onNewExperiment,
}) {
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
                        {experiments.length} experiments
                    </span>

                </div>

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
                                (experiment) => (
                                    <tr
                                        key={experiment.id}
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
                                                    experiment.status
                                                }
                                            />
                                        </td>

                                        <td>
                                            <strong className="table-number">
                                                {
                                                    experiment.architectures
                                                }
                                            </strong>

                                            <span className="table-muted">
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
                                                experiment.date
                                            }
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
                                                        experiment.status !==
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
                                )
                            )}

                        </tbody>

                    </table>

                </div>

            </section>

        </div>
    );
}

export default Experiments;