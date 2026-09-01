import {
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    getExperiments,
    startExperiment,
    getExperiment,
} from "../services/experimentApi";

import "./ExperimentDetails.css";

const architectureNames = {
    SINGLE_THREADED: "Single Threaded",
    MULTI_THREADED: "Multi Threaded",
    THREAD_POOL: "Thread Pool",
    VIRTUAL_THREAD: "Virtual Thread",
};

const TERMINAL_STATUSES = [
    "COMPLETED",
    "FAILED",
    "CANCELLED",
];

function StatusBadge({ status }) {
    const safeStatus = status || "UNKNOWN";

    return (
        <span
            className={`detail-status status-${safeStatus.toLowerCase()}`}
        >
            <span className="detail-status-dot" />
            {safeStatus}
        </span>
    );
}

function LifecycleStep({
    number,
    title,
    state,
}) {
    return (
        <div
            className={`lifecycle-step ${state}`}
        >
            <div className="lifecycle-number">
                {state === "completed"
                    ? "✓"
                    : number}
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

function getLifecycleState(
    status,
    step
) {
    const order = {
        CREATED: 1,
        RUNNING: 2,
        COMPLETED: 3,
        FAILED: 3,
        CANCELLED: 3,
    };

    const current =
        order[status] || 1;

    if (current > step) {
        return "completed";
    }

    if (current === step) {
        return "active";
    }

    return "pending";
}

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

function ExperimentDetails({
    experimentId,
    onBack,
    onViewResults,
}) {
    const [experiments, setExperiments] =
        useState([]);

    const [experimentStatus, setExperimentStatus] =
        useState(null);

    const [isLoading, setIsLoading] =
        useState(true);

    const [isStarting, setIsStarting] =
        useState(false);

    const [loadError, setLoadError] =
        useState("");

    const [startError, setStartError] =
        useState("");

    /*
     * ------------------------------------------------------------
     * Load experiment configuration
     * ------------------------------------------------------------
     */

    useEffect(() => {
        let mounted = true;

        async function loadExperiments() {
            setIsLoading(true);
            setLoadError("");

            try {
                const response =
                    await getExperiments();

                const list =
                    Array.isArray(response)
                        ? response
                        : response?.experiments || [];

                if (mounted) {
                    setExperiments(list);
                }
            } catch (error) {
                if (mounted) {
                    setLoadError(
                        error.message ||
                            "Unable to load experiment data."
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

    /*
     * ------------------------------------------------------------
     * Selected experiment configuration
     * ------------------------------------------------------------
     */

    const experiment = useMemo(() => {
        return experiments.find(
            (item) =>
                item.id === experimentId
        );
    }, [
        experiments,
        experimentId,
    ]);

    /*
     * ------------------------------------------------------------
     * Keep status synchronized with backend
     * ------------------------------------------------------------
     */

    useEffect(() => {
        if (!experimentId) {
            return undefined;
        }

        let mounted = true;
        let intervalId = null;

        async function loadStatus() {
            try {
                const response =
                    await getExperiment(
                        experimentId
                    );

                if (mounted) {
                    setExperimentStatus(
                        response
                    );
                }

                const status =
                    response?.status;

                if (
                    TERMINAL_STATUSES.includes(
                        status
                    ) ||
                    status === "CREATED"
                ) {
                    if (intervalId) {
                        clearInterval(
                            intervalId
                        );
                        intervalId = null;
                    }
                }
            } catch {
                /*
                 * Do not replace the complete
                 * experiment page with a polling
                 * error. The next polling cycle
                 * can retry.
                 */
            }
        }

        loadStatus();

        /*
         * Poll while the experiment is running.
         * We also poll after starting below.
         */
        intervalId = setInterval(
            async () => {
                try {
                    const response =
                        await getExperiment(
                            experimentId
                        );

                    if (mounted) {
                        setExperimentStatus(
                            response
                        );
                    }

                    const status =
                        response?.status;

                    if (
                        TERMINAL_STATUSES.includes(
                            status
                        ) ||
                        status === "CREATED"
                    ) {
                        clearInterval(
                            intervalId
                        );
                        intervalId = null;
                    }
                } catch {
                    // Retry on next cycle.
                }
            },
            1500
        );

        return () => {
            mounted = false;

            if (intervalId) {
                clearInterval(
                    intervalId
                );
            }
        };
    }, [experimentId]);

    /*
     * ------------------------------------------------------------
     * Start experiment
     * ------------------------------------------------------------
     */

    const handleStartExperiment =
        async () => {
            if (!experimentId) {
                return;
            }

            setIsStarting(true);
            setStartError("");

            try {
                const response =
                    await startExperiment(
                        experimentId
                    );

                setExperimentStatus(
                    response
                );

                /*
                 * Immediately refresh the
                 * authoritative status from
                 * the backend.
                 */
                const latest =
                    await getExperiment(
                        experimentId
                    );

                setExperimentStatus(
                    latest
                );
            } catch (error) {
                setStartError(
                    error.message ||
                        "Unable to start experiment."
                );
            } finally {
                setIsStarting(false);
            }
        };

    /*
     * ------------------------------------------------------------
     * Loading state
     * ------------------------------------------------------------
     */

    if (isLoading) {
        return (
            <div className="details-page">

                <div className="details-state">

                    <strong>
                        Loading experiment...
                    </strong>

                    <span>
                        Retrieving experiment data from ServerBench.
                    </span>

                </div>

            </div>
        );
    }

    /*
     * ------------------------------------------------------------
     * Error state
     * ------------------------------------------------------------
     */

    if (loadError) {
        return (
            <div className="details-page">

                <section className="details-card details-state details-state-error">

                    <strong>
                        Unable to load experiment.
                    </strong>

                    <span>
                        {loadError}
                    </span>

                    <button
                        className="secondary-detail-button"
                        type="button"
                        onClick={onBack}
                    >
                        Back to Experiments
                    </button>

                </section>

            </div>
        );
    }

    if (!experiment) {
        return (
            <div className="details-page">

                <section className="details-card details-state details-state-error">

                    <strong>
                        Experiment not found.
                    </strong>

                    <span>
                        The selected experiment is no longer available.
                    </span>

                    <button
                        className="secondary-detail-button"
                        type="button"
                        onClick={onBack}
                    >
                        Back to Experiments
                    </button>

                </section>

            </div>
        );
    }

    /*
     * ------------------------------------------------------------
     * Use the live backend status whenever
     * it is available.
     * ------------------------------------------------------------
     */

    const status =
        experimentStatus?.status ||
        experiment.status ||
        "UNKNOWN";

    const architectureCount =
        Array.isArray(
            experiment.architectures
        )
            ? experiment.architectures.length
            : 0;

    const environment =
        experiment.environmentMetadata;

    const totalRuns =
        experimentStatus?.totalRuns ??
        null;

    const successfulRuns =
        experimentStatus?.successfulRuns ??
        null;

    const failedRuns =
        experimentStatus?.failedRuns ??
        null;

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

                                <h1>
                                    {experiment.name}
                                </h1>

                                <StatusBadge
                                    status={status}
                                />

                            </div>

                            <p>
                                {experiment.description ||
                                    "No description provided."}
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

                    {status === "CREATED" && (
                        <button
                            className="primary-detail-button"
                            type="button"
                            disabled={isStarting}
                            onClick={
                                handleStartExperiment
                            }
                        >
                            {isStarting
                                ? "Starting..."
                                : "Start Experiment"}

                            {!isStarting && (
                                <span>
                                    →
                                </span>
                            )}
                        </button>
                    )}

                    {status === "RUNNING" && (
                        <button
                            className="primary-detail-button"
                            type="button"
                            disabled
                        >
                            Running...
                        </button>
                    )}

                    {status === "COMPLETED" && (
                        <button
                            className="primary-detail-button"
                            type="button"
                            onClick={
                                onViewResults
                            }
                        >
                            View Results
                            <span>→</span>
                        </button>
                    )}

                </div>

            </section>

            {/* ==================================================
                START ERROR
            ================================================== */}

            {startError && (
                <section className="details-card details-state-error start-error-card">

                    <strong>
                        Experiment could not be started.
                    </strong>

                    <span>
                        {startError}
                    </span>

                </section>
            )}

            {/* ==================================================
                LIFECYCLE
            ================================================== */}

            <section className="details-card">

                <div className="details-card-header">

                    <div>

                        <h2>
                            Experiment Status
                        </h2>

                        <p>
                            Current lifecycle state of this experiment.
                        </p>

                    </div>

                </div>

                <div className="lifecycle">

                    <LifecycleStep
                        number="1"
                        title="Created"
                        state={getLifecycleState(
                            status,
                            1
                        )}
                    />

                    <div
                        className={`lifecycle-connector ${
                            status === "RUNNING" ||
                            status === "COMPLETED" ||
                            status === "FAILED" ||
                            status === "CANCELLED"
                                ? "completed"
                                : ""
                        }`}
                    />

                    <LifecycleStep
                        number="2"
                        title="Running"
                        state={getLifecycleState(
                            status,
                            2
                        )}
                    />

                    <div
                        className={`lifecycle-connector ${
                            status === "COMPLETED" ||
                            status === "FAILED" ||
                            status === "CANCELLED"
                                ? "completed"
                                : ""
                        }`}
                    />

                    <LifecycleStep
                        number="3"
                        title={
                            status === "FAILED"
                                ? "Failed"
                                : status === "CANCELLED"
                                  ? "Cancelled"
                                  : "Completed"
                        }
                        state={
                            status === "COMPLETED" ||
                            status === "FAILED" ||
                            status === "CANCELLED"
                                ? "completed"
                                : "pending"
                        }
                    />

                </div>

            </section>

            {/* ==================================================
                CONFIGURATION + EXECUTION
            ================================================== */}

            <section className="details-summary-grid">

                <article className="details-card">

                    <div className="details-card-header">

                        <div>

                            <h2>
                                Configuration
                            </h2>

                            <p>
                                Benchmark configuration used for this experiment.
                            </p>

                        </div>

                    </div>

                    <div className="configuration-list">

                        <div className="configuration-row">
                            <span>
                                Execution Mode
                            </span>

                            <strong>
                                {
                                    experiment.executionMode
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Concurrency
                            </span>

                            <strong>
                                {
                                    experiment.concurrency
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Measurement Duration
                            </span>

                            <strong>
                                {experiment.measurementDurationMs !=
                                null
                                    ? `${experiment.measurementDurationMs} ms`
                                    : "—"}
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Total Requests
                            </span>

                            <strong>
                                {experiment.totalRequests !=
                                null
                                    ? experiment.totalRequests
                                    : "—"}
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Warm-up Duration
                            </span>

                            <strong>
                                {
                                    experiment.warmupDurationMs
                                }{" "}
                                ms
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Request Timeout
                            </span>

                            <strong>
                                {
                                    experiment.requestTimeoutMs
                                }{" "}
                                ms
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Repetitions
                            </span>

                            <strong>
                                {
                                    experiment.repetitions
                                }
                            </strong>
                        </div>

                        {experiment.threadPoolSize !=
                            null && (
                            <div className="configuration-row">

                                <span>
                                    Thread Pool Size
                                </span>

                                <strong>
                                    {
                                        experiment.threadPoolSize
                                    }
                                </strong>

                            </div>
                        )}

                    </div>

                </article>

                <article className="details-card">

                    <div className="details-card-header">

                        <div>

                            <h2>
                                Experiment Summary
                            </h2>

                            <p>
                                Current execution information from ServerBench.
                            </p>

                        </div>

                    </div>

                    <div className="execution-stat-grid">

                        <div className="execution-stat">

                            <span>
                                Status
                            </span>

                            <strong>
                                {status}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Architectures
                            </span>

                            <strong>
                                {architectureCount}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Total Runs
                            </span>

                            <strong>
                                {totalRuns ??
                                    "—"}
                            </strong>

                        </div>

                        <div className="execution-stat primary">

                            <span>
                                Successful Runs
                            </span>

                            <strong>
                                {successfulRuns ??
                                    "—"}
                            </strong>

                        </div>

                        {failedRuns !== null && (
                            <div className="execution-stat">

                                <span>
                                    Failed Runs
                                </span>

                                <strong>
                                    {
                                        failedRuns
                                    }
                                </strong>

                            </div>
                        )}

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

                            <h2>
                                Benchmark Target
                            </h2>

                            <p>
                                Server endpoint used by this experiment.
                            </p>

                        </div>

                    </div>

                    <div className="target-box">

                        <div className="target-value">

                            {experiment.host}

                            <span>
                                :
                            </span>

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

                            <h2>
                                Architectures
                            </h2>

                            <p>
                                Architectures included in this experiment.
                            </p>

                        </div>

                        <span className="architecture-count">
                            {architectureCount}{" "}
                            selected
                        </span>

                    </div>

                    <div className="details-architecture-list">

                        {Array.isArray(
                            experiment.architectures
                        ) &&
                            experiment.architectures.map(
                                (architecture) => (
                                    <div
                                        key={
                                            architecture
                                        }
                                        className="details-architecture"
                                    >

                                        <span className="architecture-check">
                                            ✓
                                        </span>

                                        <span>
                                            {
                                                architectureNames[
                                                    architecture
                                                ] ||
                                                architecture
                                            }
                                        </span>

                                    </div>
                                )
                            )}

                    </div>

                </article>

            </section>

            {/* ==================================================
                ENVIRONMENT
            ================================================== */}

            {environment && (
                <section className="details-card">

                    <div className="details-card-header">

                        <div>

                            <h2>
                                Environment
                            </h2>

                            <p>
                                Environment metadata captured for this experiment.
                            </p>

                        </div>

                    </div>

                    <div className="configuration-list">

                        <div className="configuration-row">
                            <span>
                                Operating System
                            </span>

                            <strong>
                                {
                                    environment.operatingSystem
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Java Version
                            </span>

                            <strong>
                                {
                                    environment.javaVersion
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Java Runtime
                            </span>

                            <strong>
                                {
                                    environment.javaRuntime
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Processor
                            </span>

                            <strong>
                                {
                                    environment.processor
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Available Processors
                            </span>

                            <strong>
                                {
                                    environment.availableProcessors
                                }
                            </strong>
                        </div>

                        <div className="configuration-row">
                            <span>
                                Maximum Memory
                            </span>

                            <strong>
                                {
                                    environment.maxMemoryMb
                                }{" "}
                                MB
                            </strong>
                        </div>

                    </div>

                </section>
            )}

            {/* ==================================================
                EXPERIMENT INFORMATION
            ================================================== */}

            <section className="details-card">

                <div className="details-card-header">

                    <div>

                        <h2>
                            Experiment Information
                        </h2>

                        <p>
                            Persistent information stored for this experiment.
                        </p>

                    </div>

                </div>

                <div className="run-information-grid">

                    <div className="run-info-item">

                        <span>
                            Created
                        </span>

                        <strong>
                            {formatDateTime(
                                experiment.createdAt
                            )}
                        </strong>

                    </div>

                    <div className="run-info-item">

                        <span>
                            Experiment ID
                        </span>

                        <strong>
                            {experiment.id}
                        </strong>

                    </div>

                    <div className="run-info-item">

                        <span>
                            Execution Mode
                        </span>

                        <strong>
                            {
                                experiment.executionMode
                            }
                        </strong>

                    </div>

                    <div className="run-info-item">

                        <span>
                            Status
                        </span>

                        <strong>
                            {status}
                        </strong>

                    </div>

                </div>

            </section>

        </div>
    );
}

export default ExperimentDetails;