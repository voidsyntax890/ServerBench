import { useState } from "react";

import "./Landing.css";

const architectures = [
    {
        id: "single",
        number: "01",
        title: "Single Threaded",
        short:
            "One execution path handles requests sequentially.",
        detail:
            "This architecture provides a simple baseline: requests move through a single execution path, making it useful for understanding the cost and behaviour of sequential request handling.",
        focus: "Sequential execution",
    },
    {
        id: "multi",
        number: "02",
        title: "Multi Threaded",
        short:
            "Multiple threads process work concurrently.",
        detail:
            "Multiple platform threads can process requests at the same time, allowing ServerBench to study the effect of conventional concurrent request handling under the same workload.",
        focus: "Concurrent request handling",
    },
    {
        id: "pool",
        number: "03",
        title: "Thread Pool",
        short:
            "Reusable worker threads keep concurrency bounded.",
        detail:
            "A thread pool reuses a controlled number of worker threads. This makes resource usage more predictable while still allowing concurrent request processing.",
        focus: "Bounded concurrency",
    },
    {
        id: "virtual",
        number: "04",
        title: "Virtual Thread",
        short:
            "Lightweight threads provide another concurrency model.",
        detail:
            "Virtual threads provide lightweight units of execution that are designed for large numbers of concurrent tasks, giving ServerBench another model to evaluate under the same benchmark conditions.",
        focus: "Lightweight concurrency",
    },
];

const benchmarkSteps = [
    ["01", "Configure", "Define the target, workload and execution settings."],
    ["02", "Select", "Choose the architectures that will participate."],
    ["03", "Warm up", "Allow the environment to settle before measurement."],
    ["04", "Execute", "Run the configured workload against each architecture."],
    ["05", "Measure", "Collect throughput, latency and reliability signals."],
    ["06", "Compare", "Evaluate the architectures on the same experiment."],
    ["07", "Interpret", "Surface meaningful performance and reliability findings."],
    ["08", "Explain", "Use BenchPulse for evidence-backed interpretation."],
];

function Landing({
    transitionState = "idle",
    onEnterDashboard,
}) {
    const [openArchitecture, setOpenArchitecture] =
        useState(null);

    const isLeaving =
        transitionState === "dashboard-exit";

    const handleStart =
        () => {
            if (isLeaving || !onEnterDashboard) {
                return;
            }

            onEnterDashboard();
        };

    const scrollTo =
        (id) => {
            document
                .getElementById(id)
                ?.scrollIntoView({
                    behavior: "smooth",
                    block: "start",
                });
        };

    const toggleArchitecture =
        (id) => {
            setOpenArchitecture(
                (current) =>
                    current === id
                        ? null
                        : id
            );
        };

    return (
        <main
            className={`landing-page ${
                isLeaving
                    ? "landing-page-leaving"
                    : ""
            }`}
        >
            <header className="landing-nav">
                <button
                    type="button"
                    className="landing-brand"
                    onClick={() =>
                        window.scrollTo({
                            top: 0,
                            behavior: "smooth",
                        })
                    }
                >
                    <span className="landing-brand-mark">
                        SB
                    </span>

                    <span className="landing-brand-copy">
                        <strong>ServerBench</strong>
                        <small>
                            Benchmark. Compare. Optimize.
                        </small>
                    </span>
                </button>

                <nav className="landing-nav-links">
                    <button
                        type="button"
                        onClick={() =>
                            scrollTo("architectures")
                        }
                    >
                        Architectures
                    </button>

                    <button
                        type="button"
                        onClick={() =>
                            scrollTo("how-it-works")
                        }
                    >
                        How it works
                    </button>

                    <button
                        type="button"
                        onClick={() =>
                            scrollTo("benchpulse")
                        }
                    >
                        BenchPulse
                    </button>
                </nav>

                <button
                    type="button"
                    className="landing-nav-cta"
                    onClick={handleStart}
                >
                    Open Dashboard
                    <span>→</span>
                </button>
            </header>

            <section className="landing-hero">
                <div className="landing-grid-overlay" />
                <div className="landing-orb landing-orb-one" />
                <div className="landing-orb landing-orb-two" />

                <div className="landing-scanline" />

                <div className="landing-container landing-hero-inner">
                    <div className="landing-hero-copy">
                        <div className="landing-eyebrow">
                            <span className="landing-eyebrow-pulse" />
                            Performance benchmarking platform
                        </div>

                        <h1>
                            See how server
                            <span>architectures</span>
                            behave under load.
                        </h1>

                        <p>
                            ServerBench turns server architecture
                            experiments into measurable, comparable
                            results — so performance decisions are
                            backed by evidence rather than guesswork.
                        </p>

                        <div className="landing-hero-actions">
                            <button
                                type="button"
                                className="landing-primary-button"
                                onClick={handleStart}
                            >
                                Start Benchmarking
                                <span>→</span>
                            </button>

                            <button
                                type="button"
                                className="landing-secondary-button"
                                onClick={() =>
                                    scrollTo("what-is-serverbench")
                                }
                            >
                                Learn About ServerBench
                            </button>
                        </div>

                        <div className="landing-proof-strip">
                            <span>
                                <i />
                                Controlled experiments
                            </span>

                            <span>
                                <i />
                                Measured performance
                            </span>

                            <span>
                                <i />
                                Evidence-based analysis
                            </span>
                        </div>
                    </div>

                    <div className="landing-hero-visual">
                        <div className="landing-visual-frame">
                            <div className="landing-visual-bar">
                                <div className="landing-window-dots">
                                    <span />
                                    <span />
                                    <span />
                                </div>

                                <span>
                                    SERVERBENCH / EXPERIMENT
                                </span>

                                <span className="landing-live-pill">
                                    <i />
                                    LIVE SIGNAL
                                </span>
                            </div>

                            <div className="landing-visual-body">
                                <div className="landing-visual-heading">
                                    <span>
                                        Benchmark workspace
                                    </span>
                                    <strong>
                                        Compare behaviour, not assumptions.
                                    </strong>
                                </div>

                                <div className="landing-visual-stats">
                                    <div>
                                        <small>MODE</small>
                                        <strong>
                                            Controlled
                                        </strong>
                                        <span>
                                            Same workload
                                        </span>
                                    </div>

                                    <div>
                                        <small>MODELS</small>
                                        <strong>4</strong>
                                        <span>
                                            Architectures
                                        </span>
                                    </div>

                                    <div>
                                        <small>ANALYSIS</small>
                                        <strong>
                                            BenchPulse
                                        </strong>
                                        <span>
                                            Evidence first
                                        </span>
                                    </div>
                                </div>

                                <div className="landing-signal-panel">
                                    <div className="landing-signal-grid" />
                                    <div className="landing-signal-label">
                                        <span>
                                            PERFORMANCE SIGNAL
                                        </span>
                                        <span>
                                            comparative view
                                        </span>
                                    </div>

                                    <div className="landing-signal-graph">
                                        <svg
                                            viewBox="0 0 520 160"
                                            preserveAspectRatio="none"
                                            aria-hidden="true"
                                        >
                                            <path
                                                className="signal-path signal-path-a"
                                                d="M0 126 C52 121 62 88 112 98 S172 134 216 83 S270 30 322 60 S380 119 426 72 S475 44 520 18"
                                            />
                                            <path
                                                className="signal-path signal-path-b"
                                                d="M0 142 C66 134 76 118 126 124 S182 102 226 112 S291 71 336 92 S392 108 441 58 S486 67 520 50"
                                            />
                                            <path
                                                className="signal-path signal-path-c"
                                                d="M0 104 C56 102 84 111 121 84 S182 58 230 70 S291 124 340 116 S405 79 449 96 S487 102 520 84"
                                            />
                                        </svg>

                                        <div className="landing-signal-scan" />
                                        <div className="landing-signal-node node-one" />
                                        <div className="landing-signal-node node-two" />
                                        <div className="landing-signal-node node-three" />
                                    </div>
                                </div>

                                <div className="landing-visual-footer">
                                    <span>Benchmark</span>
                                    <span>Compare</span>
                                    <span>Analyze</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section
                id="what-is-serverbench"
                className="landing-section landing-section-light"
            >
                <div className="landing-container">
                    <div className="landing-section-heading">
                        <div className="landing-section-kicker">
                            What ServerBench does
                        </div>

                        <h2>
                            A clearer way to study server performance.
                        </h2>

                        <p>
                            ServerBench gives you one place to configure
                            experiments, execute benchmark workloads,
                            compare server architectures and understand
                            what the measurements are telling you.
                        </p>
                    </div>

                    <div className="landing-capability-grid">
                        {[
                            [
                                "01",
                                "Benchmark",
                                "Run controlled workloads against selected server architectures and capture the resulting measurements.",
                            ],
                            [
                                "02",
                                "Compare",
                                "Place throughput, latency and reliability characteristics side by side within the same experimental context.",
                            ],
                            [
                                "03",
                                "Understand",
                                "Turn benchmark findings into clearer engineering conclusions with evidence-backed analysis.",
                            ],
                        ].map(
                            (item) => (
                                <article
                                    className="landing-capability-card"
                                    key={item[0]}
                                >
                                    <span>
                                        {item[0]}
                                    </span>

                                    <h3>
                                        {item[1]}
                                    </h3>

                                    <p>
                                        {item[2]}
                                    </p>

                                    <div className="landing-card-corner" />
                                </article>
                            )
                        )}
                    </div>
                </div>
            </section>

            <section className="landing-section landing-section-dark">
                <div className="landing-container">
                    <div className="landing-split">
                        <div className="landing-dark-heading">
                            <div className="landing-section-kicker landing-kicker-dark">
                                Why controlled comparison
                            </div>

                            <h2>
                                The number matters.
                                The context matters more.
                            </h2>

                            <p>
                                A useful benchmark keeps the experimental
                                conditions understandable so differences in
                                execution behaviour can be interpreted without
                                mixing unrelated variables.
                            </p>
                        </div>

                        <div className="landing-principles">
                            {[
                                [
                                    "01",
                                    "Same experiment",
                                    "Architectures are evaluated within the same configured benchmark context.",
                                ],
                                [
                                    "02",
                                    "Same measurements",
                                    "Common performance and reliability characteristics create a shared basis for comparison.",
                                ],
                                [
                                    "03",
                                    "Evidence before explanation",
                                    "Measured results remain the foundation; interpretation comes after the evidence.",
                                ],
                            ].map(
                                (item) => (
                                    <div
                                        className="landing-principle"
                                        key={item[0]}
                                    >
                                        <span>
                                            {item[0]}
                                        </span>

                                        <div>
                                            <strong>
                                                {item[1]}
                                            </strong>

                                            <p>
                                                {item[2]}
                                            </p>
                                        </div>
                                    </div>
                                )
                            )}
                        </div>
                    </div>
                </div>
            </section>

            <section
                id="architectures"
                className="landing-section landing-section-light"
            >
                <div className="landing-container">
                    <div className="landing-section-heading">
                        <div className="landing-section-kicker">
                            Explore the architectures
                        </div>

                        <h2>
                            Four execution models.
                            One comparison framework.
                        </h2>

                        <p>
                            Select an architecture to see the role it
                            plays in the benchmark. The cards are intentionally
                            interactive so the page explains rather than simply lists.
                        </p>
                    </div>

                    <div className="landing-architecture-grid">
                        {architectures.map(
                            (architecture) => {
                                const isOpen =
                                    openArchitecture ===
                                    architecture.id;

                                return (
                                    <article
                                        className={`landing-architecture-card ${
                                            isOpen
                                                ? "landing-architecture-card-open"
                                                : ""
                                        }`}
                                        key={architecture.id}
                                    >
                                        <button
                                            type="button"
                                            className="landing-architecture-trigger"
                                            onClick={() =>
                                                toggleArchitecture(
                                                    architecture.id
                                                )
                                            }
                                            aria-expanded={isOpen}
                                        >
                                            <span className="landing-architecture-number">
                                                {
                                                    architecture.number
                                                }
                                            </span>

                                            <span className="landing-architecture-arrow">
                                                {isOpen
                                                    ? "↑"
                                                    : "↗"}
                                            </span>

                                            <h3>
                                                {
                                                    architecture.title
                                                }
                                            </h3>

                                            <p>
                                                {
                                                    architecture.short
                                                }
                                            </p>

                                            <span className="landing-architecture-hint">
                                                {isOpen
                                                    ? "Hide details"
                                                    : "Explore architecture"}
                                            </span>
                                        </button>

                                        <div
                                            className={`landing-architecture-detail ${
                                                isOpen
                                                    ? "landing-architecture-detail-open"
                                                    : ""
                                            }`}
                                        >
                                            <p>
                                                {
                                                    architecture.detail
                                                }
                                            </p>

                                            <div className="landing-focus-chip">
                                                Focus:
                                                <strong>
                                                    {
                                                        architecture.focus
                                                    }
                                                </strong>
                                            </div>
                                        </div>
                                    </article>
                                );
                            }
                        )}
                    </div>
                </div>
            </section>

            <section
                id="how-it-works"
                className="landing-section landing-section-soft"
            >
                <div className="landing-container">
                    <div className="landing-section-heading">
                        <div className="landing-section-kicker">
                            How benchmarking works
                        </div>

                        <h2>
                            A structured path from setup to insight.
                        </h2>

                        <p>
                            Every stage exists to keep the experiment
                            understandable and the resulting comparison useful.
                        </p>
                    </div>

                    <div className="landing-process-grid">
                        {benchmarkSteps.map(
                            (step) => (
                                <article
                                    className="landing-process-step"
                                    key={step[0]}
                                >
                                    <span>
                                        {step[0]}
                                    </span>

                                    <div>
                                        <h3>
                                            {step[1]}
                                        </h3>

                                        <p>
                                            {step[2]}
                                        </p>
                                    </div>
                                </article>
                            )
                        )}
                    </div>
                </div>
            </section>

            <section className="landing-section landing-section-light">
                <div className="landing-container">
                    <div className="landing-evidence-panel">
                        <div>
                            <div className="landing-section-kicker">
                                Measured evidence
                            </div>

                            <h2>
                                Read the run through the metrics that matter.
                            </h2>

                            <p>
                                ServerBench keeps the key benchmark
                                characteristics visible so the comparison
                                remains anchored to actual execution behaviour.
                            </p>
                        </div>

                        <div className="landing-evidence-grid">
                            {[
                                [
                                    "Throughput",
                                    "Requests processed per second.",
                                ],
                                [
                                    "Latency",
                                    "Average and percentile response behaviour.",
                                ],
                                [
                                    "Reliability",
                                    "Successful and failed execution outcomes.",
                                ],
                                [
                                    "History",
                                    "Review completed experiments over time.",
                                ],
                            ].map(
                                (item) => (
                                    <div
                                        key={item[0]}
                                    >
                                        <span>
                                            {item[0]}
                                        </span>
                                        <p>
                                            {item[1]}
                                        </p>
                                    </div>
                                )
                            )}
                        </div>
                    </div>
                </div>
            </section>

            <section
                id="benchpulse"
                className="landing-section landing-section-dark landing-benchpulse-section"
            >
                <div className="landing-container">
                    <div className="landing-benchpulse-layout">
                        <div className="landing-benchpulse-copy">
                            <div className="landing-section-kicker landing-kicker-dark">
                                BenchPulse Intelligence
                            </div>

                            <h2>
                                Let the evidence
                                <span>lead the explanation.</span>
                            </h2>

                            <p>
                                BenchPulse adds an AI-assisted interpretation
                                layer to ServerBench. It works from benchmark
                                findings and measured characteristics to help
                                explain what may be influencing observed behaviour.
                            </p>

                            <div className="landing-ai-tags">
                                <span>Evidence</span>
                                <span>Findings</span>
                                <span>Explanation</span>
                            </div>
                        </div>

                        <div className="landing-ai-console">
                            <div className="landing-ai-console-top">
                                <div>
                                    <span className="landing-ai-orb">
                                        <i />
                                        <i />
                                        <i />
                                    </span>

                                    <strong>
                                        BenchPulse
                                    </strong>
                                </div>

                                <span className="landing-ai-live">
                                    <i />
                                    ANALYSIS READY
                                </span>
                            </div>

                            <div className="landing-ai-network">
                                <span className="ai-ring ai-ring-one" />
                                <span className="ai-ring ai-ring-two" />
                                <span className="ai-ring ai-ring-three" />

                                <span className="ai-core">
                                    <span />
                                </span>

                                <span className="ai-node ai-node-one" />
                                <span className="ai-node ai-node-two" />
                                <span className="ai-node ai-node-three" />
                                <span className="ai-node ai-node-four" />

                                <span className="ai-beam ai-beam-one" />
                                <span className="ai-beam ai-beam-two" />
                                <span className="ai-beam ai-beam-three" />
                                <span className="ai-beam ai-beam-four" />
                            </div>

                            <div className="landing-ai-insights">
                                <div>
                                    <span>
                                        FINDING
                                    </span>
                                    <strong>
                                        Performance signal detected
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        EVIDENCE
                                    </span>
                                    <strong>
                                        Grounded in measured benchmark data
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        EXPLANATION
                                    </span>
                                    <strong>
                                        Interpret the observed behaviour
                                    </strong>
                                </div>
                            </div>

                            <div className="landing-ai-console-footer">
                                <span>evidence</span>
                                <span>reasoning</span>
                                <span>insight</span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section className="landing-section landing-section-light">
                <div className="landing-container">
                    <div className="landing-section-heading">
                        <div className="landing-section-kicker">
                            Why ServerBench
                        </div>

                        <h2>
                            Built for engineers who want to know why.
                        </h2>
                    </div>

                    <div className="landing-why-grid">
                        {[
                            [
                                "Controlled",
                                "Keep the experiment clearly defined.",
                            ],
                            [
                                "Comparable",
                                "Evaluate architectures within one benchmark context.",
                            ],
                            [
                                "Observable",
                                "Follow execution, measurements and history.",
                            ],
                            [
                                "Explainable",
                                "Use BenchPulse to interpret measured findings.",
                            ],
                        ].map(
                            (item) => (
                                <article key={item[0]}>
                                    <span>
                                        {item[0]}
                                    </span>
                                    <p>
                                        {item[1]}
                                    </p>
                                </article>
                            )
                        )}
                    </div>
                </div>
            </section>

            <section className="landing-final-cta">
                <div className="landing-container">
                    <div className="landing-final-panel">
                        <div>
                            <div className="landing-section-kicker">
                                Ready to benchmark?
                            </div>

                            <h2>
                                Turn server behaviour into
                                measurable evidence.
                            </h2>

                            <p>
                                Configure an experiment, compare
                                architectures and understand what the
                                results are telling you.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="landing-primary-button"
                            onClick={handleStart}
                        >
                            Enter ServerBench
                            <span>→</span>
                        </button>
                    </div>
                </div>
            </section>

            <footer className="landing-footer">
                <div className="landing-container">
                    <div>
                        <strong>ServerBench</strong>
                        <span>
                            Benchmark. Compare. Optimize.
                        </span>
                    </div>

                    <span>
                        Performance benchmarking workspace
                    </span>
                </div>
            </footer>
        </main>
    );
}

export default Landing;
