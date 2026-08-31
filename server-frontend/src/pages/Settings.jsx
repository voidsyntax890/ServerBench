import "./Settings.css";

function Settings({ onBack }) {
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
                    <h1>Settings</h1>

                    <p>
                        View the current ServerBench application
                        configuration.
                    </p>
                </div>
            </div>

            {/* ==================================================
                BACKEND CONNECTION
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>
                        <h2>Backend Connection</h2>

                        <p>
                            Current application connectivity information.
                        </p>
                    </div>

                    <span className="settings-status">
                        Local
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
                            http://localhost:8080
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
                        <h2>Appearance</h2>

                        <p>
                            Current interface preferences.
                        </p>
                    </div>

                </div>

                <div className="settings-list">

                    <div className="settings-row">

                        <div>
                            <span className="settings-label">
                                Theme
                            </span>

                            <small>
                                Interface appearance
                            </small>
                        </div>

                        <strong>
                            Light
                        </strong>

                    </div>

                </div>

            </section>

            {/* ==================================================
                APPLICATION
            ================================================== */}

            <section className="settings-card">

                <div className="settings-card-header">

                    <div>
                        <h2>Application</h2>

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

            <div className="settings-note">
                Advanced settings will be added only when the
                corresponding backend functionality exists.
            </div>

        </div>
    );
}

export default Settings;