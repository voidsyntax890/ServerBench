const API_BASE_URL = "http://localhost:8080/api";

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
            `Request failed with HTTP ${response.status}.`;

        throw new Error(message);
    }

    return data;
}

export async function createExperiment(payload) {
    const response = await fetch(
        `${API_BASE_URL}/experiments`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json",
            },

            body: JSON.stringify(payload),
        }
    );

    return parseResponse(response);
}

export async function getExperiment(experimentId) {
    const response = await fetch(
        `${API_BASE_URL}/experiments/${experimentId}`,
        {
            method: "GET",
        }
    );

    return parseResponse(response);
}