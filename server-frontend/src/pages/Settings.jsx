import {
    useEffect,
    useState,
} from "react";

import "./Settings.css";

const SETTINGS_KEY =
    "serverbench-settings";

const DEFAULT_SETTINGS = {
    theme: "light",
    autoRefresh: true,
    refreshInterval: 5,
    recentExperimentCount: 5,
    confirmBeforeStart: true,
};

function loadSettings() {
    try {
        const saved =
            localStorage.getItem(
                SETTINGS_KEY
            );

        if (!saved) {
            return DEFAULT_SETTINGS;
        }

        return {
            ...DEFAULT_SETTINGS,
            ...JSON.parse(saved),
        };
    } catch {
        return DEFAULT_SETTINGS;
    }
}

function applyTheme(theme) {
    const root =
        document.documentElement;

    if (theme === "system") {
        root.removeAttribute(
            "data-theme"
        );

        root.style.colorScheme =
            "light dark";

        return;
    }

    root.setAttribute(
        "data-theme",
        theme
    );

    root.style.colorScheme =
        theme;
}

function Settings({ onBack }) {
    const [settings, setSettings] =
        useState(loadSettings);

    const [saved, setSaved] =
        useState(false);

    const [
        backendStatus,
        setBackendStatus,
    ] = useState("Checking...");

    useEffect(() => {
        applyTheme(settings.theme);

        localStorage.setItem(
            SETTINGS_KEY,
            JSON.stringify(settings)
        );
    }, [settings]);

    useEffect(() => {
        async function checkBackend() {
            try {
                const response =
                    await fetch(
                        "/api/experiments",
                        {
                            method: "GET",
                        }
                    );

                if (response.ok) {
                    setBackendStatus(
                        "Connected"
                    );
                } else {
                    setBackendStatus(
                        "Unavailable"
                    );
                }
            } catch {
                setBackendStatus(
                    "Unavailable"
                );
            }
        }

        checkBackend();
    }, []);

    const updateSetting = (
        key,
        value
    ) => {
        setSettings(
            (current) => ({
                ...current,
                [key]: value,
            })
        );

        setSaved(false);
    };

    const saveSettings = () => {
        localStorage.setItem(
            SETTINGS_KEY,
            JSON.stringify(settings)
        );

        applyTheme(
            settings.theme
        );

        setSaved(true);

        window.setTimeout(
            () => setSaved(false),
            2000
        );
    };

    const resetSettings = () => {
        const confirmed =
            window.confirm(
                "Reset all ServerBench preferences to their default values?"
            );

        if (!confirmed) {
            return;
        }

        setSettings(
            DEFAULT_SETTINGS
        );

        localStorage.setItem(
            SETTINGS_KEY,
            JSON.stringify(
                DEFAULT_SETTINGS
            )
        );

        applyTheme(
            DEFAULT_SETTINGS.theme
        );

        setSaved(true);

        window.setTimeout(
            () => setSaved(false),
            2000
        );
    };

    return (
        <div className="settings-page">

            <button
                className="settings-back"
                type="button"
                onClick={onBack}
            >
                <span>←</span>
                Back to Dashboard
            </button>

            <div className="settings-breadcrumb">
                ServerBench / Settings
            </div>

            <div className="settings-header">

                <div>

                    <h1>
                        Settings
                    </h1>

                    <p>
                        Configure your ServerBench
                        application preferences.
                    </p>

                </div>

                <button
                    className="settings-save-button"
                    type="button"
                    onClick={
                        saveSettings
                    }
                >
                    {saved
                        ? "Saved ✓"
                        : "Save Settings"}
                </button>

            </div>

            {/* ==================================================
                BACKEND CONNECTION
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>

                        <h2>
                            Backend Connection
                        </h2>

                        <p>
                            Current ServerBench application
                            connectivity.
                        </p>

                    </div>

                    <span
                        className={`settings-status ${
                            backendStatus ===
                            "Connected"
                                ? "settings-status-connected"
                                : ""
                        }`}
                    >
                        <span className="settings-status-dot" />
                        {backendStatus}
                    </span>

                </div>

                <div className="settings-list">

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Backend URL
                            </span>

                            <small>
                                Spring Boot REST API
                            </small>

                        </div>

                        <strong>
                            /api
                        </strong>

                    </div>

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Database
                            </span>

                            <small>
                                Persistent benchmark storage
                            </small>

                        </div>

                        <strong>
                            PostgreSQL
                        </strong>

                    </div>

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Frontend
                            </span>

                            <small>
                                Client application framework
                            </small>

                        </div>

                        <strong>
                            React + Vite
                        </strong>

                    </div>

                </div>

            </section>

            {/* ==================================================
                APPEARANCE
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>

                        <h2>
                            Appearance
                        </h2>

                        <p>
                            Customize how ServerBench
                            looks on your screen.
                        </p>

                    </div>

                </div>

                <div className="settings-option-group">

                    <span className="settings-label">
                        Theme
                    </span>

                    <div className="settings-segmented">

                        <button
                            type="button"
                            className={
                                settings.theme ===
                                "light"
                                    ? "active"
                                    : ""
                            }
                            onClick={() =>
                                updateSetting(
                                    "theme",
                                    "light"
                                )
                            }
                        >
                            ☀ Light
                        </button>

                        <button
                            type="button"
                            className={
                                settings.theme ===
                                "dark"
                                    ? "active"
                                    : ""
                            }
                            onClick={() =>
                                updateSetting(
                                    "theme",
                                    "dark"
                                )
                            }
                        >
                            ☾ Dark
                        </button>

                        <button
                            type="button"
                            className={
                                settings.theme ===
                                "system"
                                    ? "active"
                                    : ""
                            }
                            onClick={() =>
                                updateSetting(
                                    "theme",
                                    "system"
                                )
                            }
                        >
                            ◉ System
                        </button>

                    </div>

                    <small className="settings-helper">
                        System follows your operating system
                        appearance preference.
                    </small>

                </div>

            </section>

            {/* ==================================================
                DASHBOARD
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>

                        <h2>
                            Dashboard
                        </h2>

                        <p>
                            Configure dashboard refresh and display preferences.
                        </p>

                    </div>

                </div>

                <div className="settings-list">

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Auto-refresh
                            </span>

                            <small>
                                Automatically refresh live
                                experiment information.
                            </small>

                        </div>

                        <label className="settings-switch">

                            <input
                                type="checkbox"
                                checked={
                                    settings.autoRefresh
                                }
                                onChange={(
                                    event
                                ) =>
                                    updateSetting(
                                        "autoRefresh",
                                        event
                                            .target
                                            .checked
                                    )
                                }
                            />

                            <span />

                        </label>

                    </div>

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Refresh interval
                            </span>

                            <small>
                                Interval used by live dashboard
                                components.
                            </small>

                        </div>

                        <select
                            className="settings-select"
                            value={
                                settings.refreshInterval
                            }
                            disabled={
                                !settings.autoRefresh
                            }
                            onChange={(
                                event
                            ) =>
                                updateSetting(
                                    "refreshInterval",
                                    Number(
                                        event
                                            .target
                                            .value
                                    )
                                )
                            }
                        >
                            <option value={3}>
                                3 seconds
                            </option>

                            <option value={5}>
                                5 seconds
                            </option>

                            <option value={10}>
                                10 seconds
                            </option>

                            <option value={15}>
                                15 seconds
                            </option>
                        </select>

                    </div>

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Recent experiments
                            </span>

                            <small>
                                Number displayed on the dashboard.
                            </small>

                        </div>

                        <select
                            className="settings-select"
                            value={
                                settings.recentExperimentCount
                            }
                            onChange={(
                                event
                            ) =>
                                updateSetting(
                                    "recentExperimentCount",
                                    Number(
                                        event
                                            .target
                                            .value
                                    )
                                )
                            }
                        >
                            <option value={3}>
                                3
                            </option>

                            <option value={5}>
                                5
                            </option>

                            <option value={10}>
                                10
                            </option>

                        </select>

                    </div>

                </div>

            </section>

            {/* ==================================================
                BENCHMARK
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>

                        <h2>
                            Benchmark Behaviour
                        </h2>

                        <p>
                            Safety preferences for experiment execution.
                        </p>

                    </div>

                </div>

                <div className="settings-list">

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Confirm before starting
                            </span>

                            <small>
                                Ask for confirmation before launching
                                a benchmark experiment.
                            </small>

                        </div>

                        <label className="settings-switch">

                            <input
                                type="checkbox"
                                checked={
                                    settings.confirmBeforeStart
                                }
                                onChange={(
                                    event
                                ) =>
                                    updateSetting(
                                        "confirmBeforeStart",
                                        event
                                            .target
                                            .checked
                                    )
                                }
                            />

                            <span />

                        </label>

                    </div>

                </div>

            </section>

            {/* ==================================================
                APPLICATION
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>

                        <h2>
                            Application
                        </h2>

                        <p>
                            ServerBench application information.
                        </p>

                    </div>

                </div>

                <div className="settings-list">

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Application
                            </span>

                            <small>
                                Benchmarking platform
                            </small>

                        </div>

                        <strong>
                            ServerBench
                        </strong>

                    </div>

                    <div className="settings-row">

                        <div>

                            <span className="settings-label">
                                Version
                            </span>

                            <small>
                                Current frontend version
                            </small>

                        </div>

                        <strong>
                            1.0.0
                        </strong>

                    </div>

                </div>

            </section>

            {/* ==================================================
                DATA
            ================================================== */}

            <section className="settings-card settings-danger-card">

                <div className="settings-card-header">

                    <div>

                        <h2>
                            Preferences
                        </h2>

                        <p>
                            Reset locally stored ServerBench
                            preferences.
                        </p>

                    </div>

                </div>

                <button
                    className="settings-reset-button"
                    type="button"
                    onClick={
                        resetSettings
                    }
                >
                    Reset Preferences
                </button>

            </section>

            <div className="settings-note">
                Preferences are stored locally in your browser.
                Benchmark configuration and results remain managed
                by the ServerBench backend.
            </div>

        </div>
    );
}

export default Settings;