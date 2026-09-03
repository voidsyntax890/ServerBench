import {
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    getExperiments,
    startExperiment,
    getExperiment,
    subscribeToExperimentLiveUpdates,
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

/*
 * ------------------------------------------------------------
 * Live metric formatting helpers
 * ------------------------------------------------------------
 */

function formatMetricNumber(value) {
    if (value == null || Number.isNaN(Number(value))) {
        return "—";
    }

    return Number(value).toLocaleString();
}

function formatThroughput(value) {
    if (value == null || Number.isNaN(Number(value))) {
        return "—";
    }

    return `${Number(value).toFixed(2)} req/s`;
}

function formatLatency(value) {
    if (value == null || Number.isNaN(Number(value))) {
        return "—";
    }

    return `${Number(value).toFixed(4)} ms`;
}

function formatElapsedTime(value) {
    if (value == null || Number.isNaN(Number(value))) {
        return "—";
    }

    const milliseconds = Number(value);

    if (milliseconds < 1000) {
        return `${milliseconds} ms`;
    }

    return `${(milliseconds / 1000).toFixed(2)} s`;
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

        /*
         * Load the authoritative state once immediately.
         *
         * SSE then becomes responsible for live changes.
         */
        async function loadInitialStatus() {
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
            } catch {
                /*
                 * The existing page already has its
                 * normal error/loading behavior.
                 */
            }
        }

        loadInitialStatus();

        /*
         * Open one persistent SSE connection.
         */
        const eventSource =
            subscribeToExperimentLiveUpdates(
                experimentId,
                {
                    onProgress: (progress) => {

                        if (!mounted) {
                            return;
                        }

                        setExperimentStatus(
                            (current) => ({
                                ...current,
                                status:
                                    "RUNNING",

                                currentArchitecture:
                                    progress.currentArchitecture,

                                currentRepetition:
                                    progress.currentRepetition,

                                completedRuns:
                                    progress.completedRuns,

                                totalRuns:
                                    progress.totalRuns,

                                progressPercentage:
                                    progress.progressPercentage,
                            })
                        );
                    },

                    onMetrics: (metrics) => {

                        if (!mounted) {
                            return;
                        }

                        setExperimentStatus(
                            (current) => ({
                                ...current,

                                currentAttemptedRequests:
                                    metrics.attemptedRequests,

                                currentSuccessfulRequests:
                                    metrics.successfulRequests,

                                currentFailedRequests:
                                    metrics.failedRequests,

                                currentThroughputRequestsPerSecond:
                                    metrics.throughputRequestsPerSecond,

                                currentAverageLatencyMs:
                                    metrics.averageLatencyMs,

                                currentElapsedTimeMs:
                                    metrics.elapsedTimeMs,
                            })
                        );
                    },

                    onStatus: async (
                        nextStatus
                    ) => {

                        if (!mounted) {
                            return;
                        }

                        /*
                         * Get the authoritative final state
                         * whenever lifecycle status changes.
                         */
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

                        } catch {
                            /*
                             * SSE remains open and can reconnect.
                             */
                        }

                        if (
                            nextStatus ===
                                "COMPLETED" ||
                            nextStatus ===
                                "FAILED" ||
                            nextStatus ===
                                "CANCELLED"
                        ) {

                            eventSource.close();
                        }
                    },
                }
            );

        return () => {
            mounted = false;
            eventSource.close();
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

    const completedRuns =
        experimentStatus?.completedRuns ??
        0;

    const progressPercentage =
        experimentStatus?.progressPercentage ??
        0;

    const currentArchitecture =
        experimentStatus?.currentArchitecture ??
        null;

    const currentRepetition =
        experimentStatus?.currentRepetition ??
        0;

    /*
     * ------------------------------------------------------------
     * Live benchmark metrics
     * ------------------------------------------------------------
     */

    const currentAttemptedRequests =
        experimentStatus?.currentAttemptedRequests ??
        null;

    const currentSuccessfulRequests =
        experimentStatus?.currentSuccessfulRequests ??
        null;

    const currentFailedRequests =
        experimentStatus?.currentFailedRequests ??
        null;

    const currentThroughputRequestsPerSecond =
        experimentStatus?.currentThroughputRequestsPerSecond ??
        null;

    const currentAverageLatencyMs =
        experimentStatus?.currentAverageLatencyMs ??
        null;

    const currentElapsedTimeMs =
        experimentStatus?.currentElapsedTimeMs ??
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
                LIVE EXECUTION PROGRESS
            ================================================== */}

            {status === "RUNNING" && (
                <section className="details-card live-progress-card">

                    <div className="details-card-header">

                        <div>

                            <h2>
                                Live Execution Progress
                            </h2>

                            <p>
                                Current benchmark execution status and metrics.
                            </p>

                        </div>

                        <span className="live-indicator">
                            <span className="live-indicator-dot" />
                            LIVE
                        </span>

                    </div>

                    <div className="live-progress-summary">

                        <div className="live-progress-main">

                            <span>
                                Progress
                            </span>

                            <strong>
                                {progressPercentage.toFixed(
                                    0
                                )}
                                %
                            </strong>

                        </div>

                        <div className="live-progress-track">

                            <div
                                className="live-progress-fill"
                                style={{
                                    width: `${Math.min(
                                        100,
                                        Math.max(
                                            0,
                                            progressPercentage
                                        )
                                    )}%`,
                                }}
                            />

                        </div>

                        <div className="live-progress-runs">

                            <strong>
                                {completedRuns}
                            </strong>

                            <span>
                                {" "}
                                of{" "}
                                {totalRuns ?? "—"} runs completed
                            </span>

                        </div>

                    </div>

                    {/* ------------------------------------------------
                        Current execution information
                    ------------------------------------------------- */}

                    <div className="live-progress-details">

                        <div className="execution-stat">

                            <span>
                                Current Architecture
                            </span>

                            <strong>
                                {currentArchitecture
                                    ? architectureNames[
                                          currentArchitecture
                                      ] ||
                                      currentArchitecture
                                    : "—"}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Repetition
                            </span>

                            <strong>
                                {currentRepetition > 0
                                    ? currentRepetition
                                    : "—"}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Completed Runs
                            </span>

                            <strong>
                                {completedRuns}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Remaining Runs
                            </span>

                            <strong>
                                {totalRuns == null
                                    ? "—"
                                    : Math.max(
                                          0,
                                          totalRuns -
                                              completedRuns
                                      )}
                            </strong>

                        </div>

                    </div>

                    {/* ------------------------------------------------
                        Live benchmark metrics
                    ------------------------------------------------- */}

                    <div className="live-progress-metrics">

                        <div className="execution-stat">

                            <span>
                                Requests
                            </span>

                            <strong>
                                {formatMetricNumber(
                                    currentAttemptedRequests
                                )}
                            </strong>

                        </div>

                        <div className="execution-stat primary">

                            <span>
                                Successful
                            </span>

                            <strong>
                                {formatMetricNumber(
                                    currentSuccessfulRequests
                                )}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Failed
                            </span>

                            <strong>
                                {formatMetricNumber(
                                    currentFailedRequests
                                )}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Throughput
                            </span>

                            <strong>
                                {formatThroughput(
                                    currentThroughputRequestsPerSecond
                                )}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Average Latency
                            </span>

                            <strong>
                                {formatLatency(
                                    currentAverageLatencyMs
                                )}
                            </strong>

                        </div>

                        <div className="execution-stat">

                            <span>
                                Elapsed Time
                            </span>

                            <strong>
                                {formatElapsedTime(
                                    currentElapsedTimeMs
                                )}
                            </strong>

                        </div>

                    </div>

                </section>
            )}

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