const API_BASE_URL = "/api";

async function parseResponse(response) {
    const text = await response.text();

    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok) {
        const message =
            data?.message ||
            data?.error ||
            `Request failed with HTTP ${response.status}.`;

        throw new Error(message);
    }

    return data;
}

async function request(
    url,
    options = {}
) {
    const response = await fetch(
        url,
        {
            ...options,
            headers: {
                ...(options.body
                    ? {
                          "Content-Type":
                              "application/json",
                      }
                    : {}),
                ...(options.headers || {}),
            },
        }
    );

    return parseResponse(response);
}

// ================================================================
// CREATE EXPERIMENT
// ================================================================

export async function createExperiment(
    payload
) {
    return request(
        `${API_BASE_URL}/experiments`,
        {
            method: "POST",
            body: JSON.stringify(payload),
        }
    );
}

// ================================================================
// GET ALL EXPERIMENTS
// ================================================================

export async function getExperiments() {
    return request(
        `${API_BASE_URL}/experiments`,
        {
            method: "GET",
        }
    );
}

// ================================================================
// GET ONE EXPERIMENT
// ================================================================

export async function getExperiment(
    experimentId
) {
    return request(
        `${API_BASE_URL}/experiments/${experimentId}`,
        {
            method: "GET",
        }
    );
}

// ================================================================
// START EXPERIMENT
// ================================================================

export async function startExperiment(
    experimentId
) {
    return request(
        `${API_BASE_URL}/experiments/${experimentId}/start`,
        {
            method: "POST",
        }
    );
}

// ================================================================
// GET EXPERIMENT RESULTS
// ================================================================

export async function getExperimentResults(
    experimentId
) {
    return request(
        `${API_BASE_URL}/experiments/${experimentId}/results`,
        {
            method: "GET",
        }
    );
}

// ================================================================
// GET EXPERIMENT COMPARISON
// ================================================================

export async function getExperimentComparison(
    experimentId
) {
    return request(
        `${API_BASE_URL}/experiments/${experimentId}/comparison`,
        {
            method: "GET",
        }
    );
}

// ================================================================
// GET AVAILABLE ARCHITECTURES
// ================================================================

export async function getArchitectures() {
    return request(
        `${API_BASE_URL}/architectures`,
        {
            method: "GET",
        }
    );
}

// ================================================================
// LIVE EXPERIMENT UPDATES - SSE
// ================================================================

export function subscribeToExperimentLiveUpdates(
    experimentId,
    {
        onProgress,
        onMetrics,
        onStatus,
        onError,
    } = {}
) {

    const eventSource =
        new EventSource(
            `${API_BASE_URL}/experiments/${experimentId}/live`
        );

    eventSource.addEventListener(
        "progress",
        (event) => {

            try {

                const data =
                    JSON.parse(event.data);

                if (onProgress) {
                    onProgress(data);
                }

            } catch {
                // Ignore malformed progress events.
            }
        }
    );

    eventSource.addEventListener(
        "metrics",
        (event) => {

            try {

                const data =
                    JSON.parse(event.data);

                if (onMetrics) {
                    onMetrics(data);
                }

            } catch {
                // Ignore malformed metrics events.
            }
        }
    );

    eventSource.addEventListener(
        "status",
        (event) => {

            if (onStatus) {
                onStatus(event.data);
            }
        }
    );

    eventSource.onerror = (
        error
    ) => {

        if (onError) {
            onError(error);
        }
    };

    return eventSource;
}