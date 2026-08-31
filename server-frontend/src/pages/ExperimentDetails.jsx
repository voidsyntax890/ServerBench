import "./ExperimentDetails.css";

const experiment = {
    id: "0debaf9f-6004-4a40-9bac-4dc43239a2ec",
    name: "M5 Restart Persistence Test",
    description:
        "Verifying persistent experiment, run, and metric storage.",
    status: "COMPLETED",

    executionMode: "DURATION",
    host: "localhost",
    port: 8010,

    concurrency: 5,
    measurementDurationMs: 3000,
    warmupDurationMs: 1000,
    requestTimeoutMs: 2000,
    repetitions: 1,

    architectures: [
        "SINGLE_THREADED",
        "MULTI_THREADED",
        "THREAD_POOL",
        "VIRTUAL_THREAD",
    ],

    totalRuns: 4,
    successfulRuns: 4,
    failedRuns: 0,

    createdAt: "31 Aug 2026, 12:00:45 AM",
    startedAt: "31 Aug 2026, 12:01:12 AM",
    finishedAt: "31 Aug 2026, 12:01:24 AM",
    executionDuration: "12.28 sec",
};

const architectureNames = {
    SINGLE_THREADED: "Single Threaded",
    MULTI_THREADED: "Multi Threaded",
    THREAD_POOL: "Thread Pool",
    VIRTUAL_THREAD: "Virtual Thread",
};

function StatusBadge({ status }) {
    return (
        <span className={`detail-status status-${status.toLowerCase()}`}>
            <span className="detail-status-dot" />
            {status}
        </span>
    );
}

function LifecycleStep({ number, title, state }) {
    return (
        <div className={`lifecycle-step ${state}`}>
            <div className="lifecycle-number">
                {state === "completed" ? "✓" : number}
            </div>

            <div className="lifecycle-content">
                <strong>{title}</strong>

                <span>
                    {state === "completed"
                        ? "Completed"
                        : state === "active"
                          ? "In progress"
                          : "Pending"}
                </span>
            </div>
        </div>
    );
}

function ExperimentDetails({ onBack, onViewResults }) {
    const completionRate =
        experiment.totalRuns === 0
            ? 0
            : Math.round(
                  (experiment.successfulRuns /
                      experiment.totalRuns) *
                      100
              );

    return (
        <div className="details-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <section className="details-header">

                <div className="details-header-main">

                    <button
                        className="details-back-button"
                        type="button"
                        onClick={onBack}
                    >
                        <span>←</span>
                        Back to Experiments
                    </button>

                    <div className="details-title-row">

                        <div>
                            <div className="details-breadcrumb">
                                Experiments / Experiment Details
                            </div>

                            <div className="details-title-line">

                                <h1>{experiment.name}</h1>

                                <StatusBadge
                                    status={experiment.status}
                                />

                            </div>

                            <p>
                                {experiment.description}
                            </p>
                        </div>

                    </div>

                </div>

                <div className="details-header-actions">

                    <button
                        className="secondary-detail-button"
                        type="button"
                        onClick={onBack}
                    >
                        Back
                    </button>

                    {experiment.status === "COMPLETED" && (
                        <button
                            className="primary-detail-button"
                            type="button"
                            onClick={onViewResults}
                        >
                            View Results
                            <span>→</span>
                        </button>
                    )}

                </div>

            </section>

            {/* ==================================================
                LIFECYCLE
            ================================================== */}

            <section className="details-card">

                <div className="details-card-header">
                    <div>
                        <h2>Experiment Status</h2>

                        <p>
                            Current lifecycle state of this experiment.
                        </p>
                    </div>
                </div>

                <div className="lifecycle">

                    <LifecycleStep
                        number="1"
                        title="Created"
                        state="completed"
                    />

                    <div className="lifecycle-connector completed" />

                    <LifecycleStep
                        number="2"
                        title="Running"
                        state="completed"
                    />

                    <div className="lifecycle-connector completed" />

                    <LifecycleStep
                        number="3"
                        title="Completed"
                        state="completed"
                    />

                </div>

            </section>

            {/* ==================================================
                SUMMARY
            ================================================== */}

            <section className="details-summary-grid">

                <article className="details-card">

                    <div className="details-card-header">
                        <div>
                            <h2>Configuration</h2>

                            <p>
                                Benchmark configuration used for this run.
                            </p>
                        </div>
                    </div>

                    <div className="configuration-list">

                        <div className="configuration-row">
                            <span>Execution Mode</span>
                            <strong>
                                {experiment.executionMode}
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>Concurrency</span>
                            <strong>
                                {experiment.concurrency}
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>Measurement Duration</span>
                            <strong>
                                {experiment.measurementDurationMs} ms
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>Warm-up Duration</span>
                            <strong>
                                {experiment.warmupDurationMs} ms
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>Request Timeout</span>
                            <strong>
                                {experiment.requestTimeoutMs} ms
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>Repetitions</span>
                            <strong>
                                {experiment.repetitions}
                            </strong>
                        </div>

                    </div>

                </article>

                <article className="details-card">

                    <div className="details-card-header">
                        <div>
                            <h2>Execution Summary</h2>

                            <p>
                                Result of the benchmark execution.
                            </p>
                        </div>
                    </div>

                    <div className="execution-stat-grid">

                        <div className="execution-stat">
                            <span>Total Runs</span>
                            <strong>
                                {experiment.totalRuns}
                            </strong>
                        </div>

                        <div className="execution-stat success">
                            <span>Successful</span>
                            <strong>
                                {experiment.successfulRuns}
                            </strong>
                        </div>

                        <div className="execution-stat danger">
                            <span>Failed</span>
                            <strong>
                                {experiment.failedRuns}
                            </strong>
                        </div>

                        <div className="execution-stat primary">
                            <span>Completion</span>
                            <strong>
                                {completionRate}%
                            </strong>
                        </div>

                    </div>

                </article>

            </section>

            {/* ==================================================
                TARGET + ARCHITECTURES
            ================================================== */}

            <section className="details-summary-grid">

                <article className="details-card">

                    <div className="details-card-header">
                        <div>
                            <h2>Benchmark Target</h2>

                            <p>
                                Server endpoint used by this experiment.
                            </p>
                        </div>
                    </div>

                    <div className="target-box">

                        <div className="target-value">
                            {experiment.host}
                            <span>:</span>
                            {experiment.port}
                        </div>

                        <div className="target-label">
                            Benchmark server
                        </div>

                    </div>

                </article>

                <article className="details-card">

                    <div className="details-card-header">
                        <div>
                            <h2>Architectures</h2>

                            <p>
                                Architectures included in this experiment.
                            </p>
                        </div>

                        <span className="architecture-count">
                            {experiment.architectures.length} selected
                        </span>
                    </div>

                    <div className="details-architecture-list">

                        {experiment.architectures.map(
                            (architecture) => (
                                <div
                                    key={architecture}
                                    className="details-architecture"
                                >
                                    <span className="architecture-check">
                                        ✓
                                    </span>

                                    <span>
                                        {
                                            architectureNames[
                                                architecture
                                            ]
                                        }
                                    </span>
                                </div>
                            )
                        )}

                    </div>

                </article>

            </section>

            {/* ==================================================
                RUN INFORMATION
            ================================================== */}

            <section className="details-card">

                <div className="details-card-header">
                    <div>
                        <h2>Run Information</h2>

                        <p>
                            Execution timing for this experiment.
                        </p>
                    </div>
                </div>

                <div className="run-information-grid">

                    <div className="run-info-item">

                        <span>Created</span>

                        <strong>
                            {experiment.createdAt}
                        </strong>

                    </div>

                    <div className="run-info-item">

                        <span>Started</span>

                        <strong>
                            {experiment.startedAt}
                        </strong>

                    </div>

                    <div className="run-info-item">

                        <span>Finished</span>

                        <strong>
                            {experiment.finishedAt}
                        </strong>

                    </div>

                    <div className="run-info-item">

                        <span>Execution Duration</span>

                        <strong>
                            {experiment.executionDuration}
                        </strong>

                    </div>

                </div>

            </section>

        </div>
    );
}

export default ExperimentDetails;