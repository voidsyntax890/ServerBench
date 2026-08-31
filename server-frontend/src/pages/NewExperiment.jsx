import { useMemo, useState } from "react";

import { createExperiment } from "../services/experimentApi";

import "./NewExperiment.css";

const architectures = [
    {
        id: "SINGLE_THREADED",
        name: "Single Threaded",
        description:
            "One execution path handles requests sequentially.",
    },
    {
        id: "MULTI_THREADED",
        name: "Multi Threaded",
        description:
            "Uses a platform thread for each accepted request.",
    },
    {
        id: "THREAD_POOL",
        name: "Thread Pool",
        description:
            "Uses a bounded reusable pool of platform threads.",
    },
    {
        id: "VIRTUAL_THREAD",
        name: "Virtual Thread",
        description:
            "Uses a virtual thread for each accepted request.",
    },
];

function NewExperiment({
    onBack,
    onCreated,
}) {
    const [form, setForm] = useState({
        name: "",
        description: "",
        host: "localhost",
        port: "8010",

        executionMode: "DURATION",

        totalRequests: "",

        measurementDurationMs: "3000",

        concurrency: "5",

        warmupDurationMs: "1000",

        requestTimeoutMs: "2000",

        repetitions: "1",

        threadPoolSize: "10",

        architectures: [],
    });

    const [touched, setTouched] = useState({});

    const [isCreating, setIsCreating] =
        useState(false);

    const [createError, setCreateError] =
        useState("");

    const [createdExperiment, setCreatedExperiment] =
        useState(null);

    const updateField = (
        field,
        value
    ) => {
        setForm((current) => ({
            ...current,
            [field]: value,
        }));

        setCreateError("");
        setCreatedExperiment(null);
    };

    const toggleArchitecture = (
        architecture
    ) => {
        setForm((current) => {
            const selected =
                current.architectures.includes(
                    architecture
                );

            return {
                ...current,

                architectures: selected
                    ? current.architectures.filter(
                          (item) =>
                              item !==
                              architecture
                      )
                    : [
                          ...current.architectures,
                          architecture,
                      ],
            };
        });

        setCreateError("");
        setCreatedExperiment(null);
    };

    const fieldErrors = useMemo(() => {
        const errors = {};

        if (!form.name.trim()) {
            errors.name =
                "Experiment name is required.";
        }

        if (!form.host.trim()) {
            errors.host =
                "Host is required.";
        }

        const port = Number(
            form.port
        );

        if (
            !Number.isInteger(port) ||
            port < 1 ||
            port > 65535
        ) {
            errors.port =
                "Port must be between 1 and 65535.";
        }

        const concurrency = Number(
            form.concurrency
        );

        if (
            !Number.isInteger(
                concurrency
            ) ||
            concurrency <= 0
        ) {
            errors.concurrency =
                "Concurrency must be greater than 0.";
        }

        const warmup =
            Number(
                form.warmupDurationMs
            );

        if (
            !Number.isInteger(warmup) ||
            warmup < 0
        ) {
            errors.warmupDurationMs =
                "Warm-up duration cannot be negative.";
        }

        const timeout =
            Number(
                form.requestTimeoutMs
            );

        if (
            !Number.isInteger(timeout) ||
            timeout <= 0
        ) {
            errors.requestTimeoutMs =
                "Request timeout must be greater than 0.";
        }

        const repetitions =
            Number(form.repetitions);

        if (
            !Number.isInteger(
                repetitions
            ) ||
            repetitions <= 0
        ) {
            errors.repetitions =
                "Repetitions must be greater than 0.";
        }

        if (
            form.executionMode ===
            "REQUESTS"
        ) {
            const totalRequests =
                Number(
                    form.totalRequests
                );

            if (
                !Number.isInteger(
                    totalRequests
                ) ||
                totalRequests <= 0
            ) {
                errors.totalRequests =
                    "Total requests must be greater than 0.";
            }
        }

        if (
            form.executionMode ===
            "DURATION"
        ) {
            const duration =
                Number(
                    form.measurementDurationMs
                );

            if (
                !Number.isInteger(
                    duration
                ) ||
                duration <= 0
            ) {
                errors.measurementDurationMs =
                    "Measurement duration must be greater than 0.";
            }
        }

        if (
            form.architectures.length ===
            0
        ) {
            errors.architectures =
                "Select at least one architecture.";
        }

        if (
            form.architectures.includes(
                "THREAD_POOL"
            )
        ) {
            const threadPoolSize =
                Number(
                    form.threadPoolSize
                );

            if (
                !Number.isInteger(
                    threadPoolSize
                ) ||
                threadPoolSize <= 0
            ) {
                errors.threadPoolSize =
                    "Thread pool size must be greater than 0.";
            }
        }

        return errors;
    }, [form]);

    const isValid =
        Object.keys(
            fieldErrors
        ).length === 0;

    const showError = (
        field
    ) =>
        touched[field]
            ? fieldErrors[field]
            : null;

    const markTouched = (
        field
    ) => {
        setTouched(
            (current) => ({
                ...current,
                [field]: true,
            })
        );
    };

    const markAllTouched = () => {
        setTouched({
            name: true,
            host: true,
            port: true,
            concurrency: true,
            warmupDurationMs: true,
            requestTimeoutMs: true,
            repetitions: true,
            totalRequests: true,
            measurementDurationMs: true,
            architectures: true,
            threadPoolSize: true,
        });
    };

    const buildPayload = () => {
        const payload = {
            name: form.name.trim(),

            description:
                form.description.trim(),

            host: form.host.trim(),

            port: Number(
                form.port
            ),

            executionMode:
                form.executionMode,

            totalRequests:
                form.executionMode ===
                "REQUESTS"
                    ? Number(
                          form.totalRequests
                      )
                    : null,

            measurementDurationMs:
                form.executionMode ===
                "DURATION"
                    ? Number(
                          form.measurementDurationMs
                      )
                    : null,

            concurrency:
                Number(
                    form.concurrency
                ),

            warmupDurationMs:
                Number(
                    form.warmupDurationMs
                ),

            requestTimeoutMs:
                Number(
                    form.requestTimeoutMs
                ),

            repetitions:
                Number(
                    form.repetitions
                ),

            architectures:
                form.architectures,

            threadPoolSize:
                form.architectures.includes(
                    "THREAD_POOL"
                )
                    ? Number(
                          form.threadPoolSize
                      )
                    : null,
        };

        return payload;
    };

    const handleSubmit = async (
        event
    ) => {
        event.preventDefault();

        markAllTouched();

        setCreateError("");
        setCreatedExperiment(
            null
        );

        if (!isValid) {
            return;
        }

        setIsCreating(true);

        try {
            const payload =
                buildPayload();

            const response =
                await createExperiment(
                    payload
                );

            setCreatedExperiment(
                response
            );

            /*
             * Give the user a visible
             * success state first.
             *
             * The created experiment ID
             * is real and comes from
             * Spring Boot.
             */
        } catch (error) {
            setCreateError(
                error.message ||
                    "Unable to create experiment."
            );
        } finally {
            setIsCreating(false);
        }
    };

    const selectedArchitectureNames =
        architectures
            .filter(
                (architecture) =>
                    form.architectures.includes(
                        architecture.id
                    )
            )
            .map(
                (architecture) =>
                    architecture.name
            );

    return (
        <div className="experiment-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <div className="experiment-header">

                <div className="experiment-header-left">

                    <button
                        className="back-button"
                        type="button"
                        onClick={
                            onBack
                        }
                        disabled={
                            isCreating
                        }
                    >
                        <span>
                            ←
                        </span>

                        Back to Dashboard
                    </button>

                    <div>

                        <div className="breadcrumb">
                            Experiments /
                            New Experiment
                        </div>

                        <h1>
                            New Experiment
                        </h1>

                        <p>
                            Configure a benchmark
                            experiment before
                            execution.
                        </p>

                    </div>

                </div>

            </div>

            {/* ==================================================
                FORM
            ================================================== */}

            <form
                className="experiment-layout"
                onSubmit={
                    handleSubmit
                }
            >

                <div className="experiment-form">

                    {/* ==================================================
                        EXPERIMENT DETAILS
                    ================================================== */}

                    <section className="form-section">

                        <div className="section-heading">

                            <div>

                                <h2>
                                    Experiment Details
                                </h2>

                                <p>
                                    Identify and
                                    describe this
                                    benchmark.
                                </p>

                            </div>

                        </div>

                        <div className="form-grid">

                            <div className="form-field full-width">

                                <label htmlFor="name">
                                    Experiment Name
                                    <span>
                                        *
                                    </span>
                                </label>

                                <input
                                    id="name"
                                    type="text"
                                    maxLength="100"
                                    value={
                                        form.name
                                    }
                                    placeholder="e.g. Four Architecture Comparison"
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "name",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                    onBlur={() =>
                                        markTouched(
                                            "name"
                                        )
                                    }
                                />

                                {showError(
                                    "name"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "name"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                            <div className="form-field full-width">

                                <label htmlFor="description">
                                    Description
                                </label>

                                <textarea
                                    id="description"
                                    rows="4"
                                    maxLength="500"
                                    value={
                                        form.description
                                    }
                                    placeholder="Describe what you are benchmarking and why."
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "description",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                />

                                <span className="field-hint">
                                    Optional context
                                    for this experiment.
                                </span>

                            </div>

                        </div>

                    </section>

                    {/* ==================================================
                        TARGET
                    ================================================== */}

                    <section className="form-section">

                        <div className="section-heading">

                            <div>

                                <h2>
                                    Benchmark Target
                                </h2>

                                <p>
                                    Define where the
                                    benchmark server
                                    will run.
                                </p>

                            </div>

                        </div>

                        <div className="form-grid">

                            <div className="form-field">

                                <label htmlFor="host">
                                    Host
                                    <span>
                                        *
                                    </span>
                                </label>

                                <input
                                    id="host"
                                    type="text"
                                    value={
                                        form.host
                                    }
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "host",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                    onBlur={() =>
                                        markTouched(
                                            "host"
                                        )
                                    }
                                />

                                {showError(
                                    "host"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "host"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                            <div className="form-field">

                                <label htmlFor="port">
                                    Port
                                    <span>
                                        *
                                    </span>
                                </label>

                                <input
                                    id="port"
                                    type="number"
                                    min="1"
                                    max="65535"
                                    value={
                                        form.port
                                    }
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "port",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                    onBlur={() =>
                                        markTouched(
                                            "port"
                                        )
                                    }
                                />

                                {showError(
                                    "port"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "port"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                        </div>

                    </section>

                    {/* ==================================================
                        EXECUTION
                    ================================================== */}

                    <section className="form-section">

                        <div className="section-heading">

                            <div>

                                <h2>
                                    Execution Configuration
                                </h2>

                                <p>
                                    Define how the
                                    benchmark should
                                    execute.
                                </p>

                            </div>

                        </div>

                        <div className="form-grid">

                            <div className="form-field">

                                <label htmlFor="executionMode">
                                    Execution Mode
                                </label>

                                <select
                                    id="executionMode"
                                    value={
                                        form.executionMode
                                    }
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "executionMode",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                >
                                    <option value="DURATION">
                                        Duration
                                    </option>

                                    <option value="REQUESTS">
                                        Requests
                                    </option>
                                </select>

                            </div>

                            {form.executionMode ===
                            "DURATION" ? (
                                <div className="form-field">

                                    <label htmlFor="measurementDurationMs">
                                        Measurement Duration
                                        <span>
                                            *
                                        </span>
                                    </label>

                                    <div className="input-with-unit">

                                        <input
                                            id="measurementDurationMs"
                                            type="number"
                                            min="1"
                                            value={
                                                form.measurementDurationMs
                                            }
                                            disabled={
                                                isCreating
                                            }
                                            onChange={(
                                                event
                                            ) =>
                                                updateField(
                                                    "measurementDurationMs",
                                                    event
                                                        .target
                                                        .value
                                                )
                                            }
                                            onBlur={() =>
                                                markTouched(
                                                    "measurementDurationMs"
                                                )
                                            }
                                        />

                                        <span>
                                            ms
                                        </span>

                                    </div>

                                    {showError(
                                        "measurementDurationMs"
                                    ) && (
                                        <span className="field-error">
                                            {
                                                showError(
                                                    "measurementDurationMs"
                                                )
                                            }
                                        </span>
                                    )}

                                </div>
                            ) : (
                                <div className="form-field">

                                    <label htmlFor="totalRequests">
                                        Total Requests
                                        <span>
                                            *
                                        </span>
                                    </label>

                                    <input
                                        id="totalRequests"
                                        type="number"
                                        min="1"
                                        value={
                                            form.totalRequests
                                        }
                                        placeholder="10000"
                                        disabled={
                                            isCreating
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            updateField(
                                                "totalRequests",
                                                event
                                                    .target
                                                    .value
                                            )
                                        }
                                        onBlur={() =>
                                            markTouched(
                                                "totalRequests"
                                            )
                                        }
                                    />

                                    {showError(
                                        "totalRequests"
                                    ) && (
                                        <span className="field-error">
                                            {
                                                showError(
                                                    "totalRequests"
                                                )
                                            }
                                        </span>
                                    )}

                                </div>
                            )}

                            <div className="form-field">

                                <label htmlFor="concurrency">
                                    Concurrency
                                    <span>
                                        *
                                    </span>
                                </label>

                                <input
                                    id="concurrency"
                                    type="number"
                                    min="1"
                                    value={
                                        form.concurrency
                                    }
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "concurrency",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                    onBlur={() =>
                                        markTouched(
                                            "concurrency"
                                        )
                                    }
                                />

                                {showError(
                                    "concurrency"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "concurrency"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                            <div className="form-field">

                                <label htmlFor="warmupDurationMs">
                                    Warm-up Duration
                                    <span>
                                        *
                                    </span>
                                </label>

                                <div className="input-with-unit">

                                    <input
                                        id="warmupDurationMs"
                                        type="number"
                                        min="0"
                                        value={
                                            form.warmupDurationMs
                                        }
                                        disabled={
                                            isCreating
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            updateField(
                                                "warmupDurationMs",
                                                event
                                                    .target
                                                    .value
                                            )
                                        }
                                        onBlur={() =>
                                            markTouched(
                                                "warmupDurationMs"
                                            )
                                        }
                                    />

                                    <span>
                                        ms
                                    </span>

                                </div>

                                {showError(
                                    "warmupDurationMs"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "warmupDurationMs"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                            <div className="form-field">

                                <label htmlFor="requestTimeoutMs">
                                    Request Timeout
                                    <span>
                                        *
                                    </span>
                                </label>

                                <div className="input-with-unit">

                                    <input
                                        id="requestTimeoutMs"
                                        type="number"
                                        min="1"
                                        value={
                                            form.requestTimeoutMs
                                        }
                                        disabled={
                                            isCreating
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            updateField(
                                                "requestTimeoutMs",
                                                event
                                                    .target
                                                    .value
                                            )
                                        }
                                        onBlur={() =>
                                            markTouched(
                                                "requestTimeoutMs"
                                            )
                                        }
                                    />

                                    <span>
                                        ms
                                    </span>

                                </div>

                                {showError(
                                    "requestTimeoutMs"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "requestTimeoutMs"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                            <div className="form-field">

                                <label htmlFor="repetitions">
                                    Repetitions
                                    <span>
                                        *
                                    </span>
                                </label>

                                <input
                                    id="repetitions"
                                    type="number"
                                    min="1"
                                    value={
                                        form.repetitions
                                    }
                                    disabled={
                                        isCreating
                                    }
                                    onChange={(
                                        event
                                    ) =>
                                        updateField(
                                            "repetitions",
                                            event
                                                .target
                                                .value
                                        )
                                    }
                                    onBlur={() =>
                                        markTouched(
                                            "repetitions"
                                        )
                                    }
                                />

                                {showError(
                                    "repetitions"
                                ) && (
                                    <span className="field-error">
                                        {
                                            showError(
                                                "repetitions"
                                            )
                                        }
                                    </span>
                                )}

                            </div>

                        </div>

                    </section>

                    {/* ==================================================
                        ARCHITECTURES
                    ================================================== */}

                    <section className="form-section">

                        <div className="section-heading">

                            <div>

                                <h2>
                                    Server Architectures
                                </h2>

                                <p>
                                    Select one or more
                                    architectures to
                                    benchmark.
                                </p>

                            </div>

                            <span className="selection-count">
                                {
                                    form.architectures
                                        .length
                                }{" "}
                                selected
                            </span>

                        </div>

                        <div className="architecture-grid">

                            {architectures.map(
                                (
                                    architecture
                                ) => {

                                    const selected =
                                        form.architectures.includes(
                                            architecture.id
                                        );

                                    return (
                                        <button
                                            key={
                                                architecture.id
                                            }
                                            type="button"
                                            className={`architecture-card ${
                                                selected
                                                    ? "selected"
                                                    : ""
                                            }`}
                                            disabled={
                                                isCreating
                                            }
                                            onClick={() =>
                                                toggleArchitecture(
                                                    architecture.id
                                                )
                                            }
                                        >

                                            <span
                                                className={`architecture-check ${
                                                    selected
                                                        ? "checked"
                                                        : ""
                                                }`}
                                            >
                                                {selected
                                                    ? "✓"
                                                    : ""}
                                            </span>

                                            <span className="architecture-content">

                                                <strong>
                                                    {
                                                        architecture.name
                                                    }
                                                </strong>

                                                <small>
                                                    {
                                                        architecture.description
                                                    }
                                                </small>

                                            </span>

                                        </button>
                                    );
                                }
                            )}

                        </div>

                        {showError(
                            "architectures"
                        ) && (
                            <span className="field-error">
                                {
                                    showError(
                                        "architectures"
                                    )
                                }
                            </span>
                        )}

                        {form.architectures.includes(
                            "THREAD_POOL"
                        ) && (
                            <div className="thread-pool-field">

                                <div className="form-field">

                                    <label htmlFor="threadPoolSize">
                                        Thread Pool Size
                                        <span>
                                            *
                                        </span>
                                    </label>

                                    <input
                                        id="threadPoolSize"
                                        type="number"
                                        min="1"
                                        value={
                                            form.threadPoolSize
                                        }
                                        disabled={
                                            isCreating
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            updateField(
                                                "threadPoolSize",
                                                event
                                                    .target
                                                    .value
                                            )
                                        }
                                        onBlur={() =>
                                            markTouched(
                                                "threadPoolSize"
                                            )
                                        }
                                    />

                                    <span className="field-hint">
                                        Used only for
                                        the Thread Pool
                                        architecture.
                                    </span>

                                    {showError(
                                        "threadPoolSize"
                                    ) && (
                                        <span className="field-error">
                                            {
                                                showError(
                                                    "threadPoolSize"
                                                )
                                            }
                                        </span>
                                    )}

                                </div>

                            </div>
                        )}

                    </section>

                    {/* ==================================================
                        ERROR
                    ================================================== */}

                    {createError && (
                        <div className="create-error">
                            <strong>
                                Experiment could not be created.
                            </strong>

                            <span>
                                {createError}
                            </span>
                        </div>
                    )}

                    {/* ==================================================
                        SUCCESS
                    ================================================== */}

                    {createdExperiment && (
                        <div className="success-message">

                            <strong>
                                Experiment created successfully.
                            </strong>

                            <span>
                                ID:{" "}
                                {
                                    createdExperiment.id
                                }
                            </span>

                            <span>
                                Status:{" "}
                                {
                                    createdExperiment.status
                                }
                            </span>

                            {onCreated && (
                                <button
                                    type="button"
                                    className="success-open-button"
                                    onClick={() =>
                                        onCreated(
                                            createdExperiment
                                        )
                                    }
                                >
                                    Open Experiment
                                    <span>
                                        →
                                    </span>
                                </button>
                            )}

                        </div>
                    )}

                    {/* ==================================================
                        ACTIONS
                    ================================================== */}

                    <div className="form-actions">

                        <button
                            type="button"
                            className="secondary-button"
                            onClick={
                                onBack
                            }
                            disabled={
                                isCreating
                            }
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            className="primary-button"
                            disabled={
                                !isValid ||
                                isCreating
                            }
                        >
                            {isCreating ? (
                                <>
                                    Creating...
                                </>
                            ) : (
                                <>
                                    Create Experiment
                                </>
                            )}
                        </button>

                    </div>

                </div>

                {/* ==================================================
                    SUMMARY
                ================================================== */}

                <aside className="configuration-summary">

                    <div className="summary-card">

                        <div className="summary-header">
                            Configuration Summary
                        </div>

                        <div className="summary-section">

                            <span className="summary-label">
                                Execution
                            </span>

                            <div className="summary-row">
                                <span>
                                    Mode
                                </span>

                                <strong>
                                    {
                                        form.executionMode ===
                                        "DURATION"
                                            ? "Duration"
                                            : "Requests"
                                    }
                                </strong>
                            </div>

                            {form.executionMode ===
                            "DURATION" ? (
                                <div className="summary-row">
                                    <span>
                                        Duration
                                    </span>

                                    <strong>
                                        {
                                            form.measurementDurationMs
                                        }{" "}
                                        ms
                                    </strong>
                                </div>
                            ) : (
                                <div className="summary-row">
                                    <span>
                                        Requests
                                    </span>

                                    <strong>
                                        {
                                            form.totalRequests ||
                                            "—"
                                        }
                                    </strong>
                                </div>
                            )}

                            <div className="summary-row">
                                <span>
                                    Concurrency
                                </span>

                                <strong>
                                    {
                                        form.concurrency
                                    }
                                </strong>
                            </div>

                            <div className="summary-row">
                                <span>
                                    Repetitions
                                </span>

                                <strong>
                                    {
                                        form.repetitions
                                    }
                                </strong>
                            </div>

                        </div>

                        <div className="summary-divider" />

                        <div className="summary-section">

                            <span className="summary-label">
                                Target
                            </span>

                            <div className="summary-row">
                                <span>
                                    Host
                                </span>

                                <strong>
                                    {
                                        form.host
                                    }
                                </strong>
                            </div>

                            <div className="summary-row">
                                <span>
                                    Port
                                </span>

                                <strong>
                                    {
                                        form.port
                                    }
                                </strong>
                            </div>

                        </div>

                        <div className="summary-divider" />

                        <div className="summary-section">

                            <span className="summary-label">
                                Architectures
                            </span>

                            {
                                selectedArchitectureNames.length ===
                                0 ? (
                                    <div className="summary-empty">
                                        No architectures
                                        selected.
                                    </div>
                                ) : (
                                    <div className="selected-list">

                                        {selectedArchitectureNames.map(
                                            (
                                                name
                                            ) => (
                                                <div
                                                    className="selected-item"
                                                    key={
                                                        name
                                                    }
                                                >
                                                    <span>
                                                        ✓
                                                    </span>

                                                    {
                                                        name
                                                    }
                                                </div>
                                            )
                                        )}

                                    </div>
                                )
                            }

                        </div>

                        {form.architectures.includes(
                            "THREAD_POOL"
                        ) && (
                            <>
                                <div className="summary-divider" />

                                <div className="summary-row">
                                    <span>
                                        Thread Pool
                                    </span>

                                    <strong>
                                        {
                                            form.threadPoolSize
                                        }
                                    </strong>
                                </div>
                            </>
                        )}

                    </div>

                </aside>

            </form>

        </div>
    );
}

export default NewExperiment;