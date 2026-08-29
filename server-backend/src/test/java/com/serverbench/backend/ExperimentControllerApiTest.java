package com.serverbench.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExperimentControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // ================================================================
    // CREATE EXPERIMENT - VALID REQUEST
    // ================================================================

    @Test
    void createExperimentWithValidRequestShouldReturnCreated()
            throws Exception {

        String requestBody = """
                {
                    "name": "Automated API Test",
                    "description": "Testing experiment creation",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "DURATION",
                    "measurementDurationMs": 3000,
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL",
                        "VIRTUAL_THREAD"
                    ],
                    "threadPoolSize": 10
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id").isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Automated API Test")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CREATED")
                )
                .andExpect(
                        jsonPath("$.executionMode")
                                .value("DURATION")
                )
                .andExpect(
                        jsonPath("$.architectures[0]")
                                .value("THREAD_POOL")
                )
                .andExpect(
                        jsonPath("$.architectures[1]")
                                .value("VIRTUAL_THREAD")
                );
    }

    // ================================================================
    // VALIDATION - INVALID CONCURRENCY
    // ================================================================

    @Test
    void createExperimentWithZeroConcurrencyShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "Invalid Concurrency Test",
                    "description": "Invalid concurrency",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "DURATION",
                    "measurementDurationMs": 3000,
                    "concurrency": 0,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL"
                    ],
                    "threadPoolSize": 10
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "The experiment configuration is invalid."
                                )
                )
                .andExpect(
                        jsonPath("$.requestId")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.fieldErrors.concurrency")
                                .value(
                                        "Concurrency must be greater than 0."
                                )
                );
    }

    // ================================================================
    // VALIDATION - INVALID PORT
    // ================================================================

    @Test
    void createExperimentWithInvalidPortShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "Invalid Port Test",
                    "description": "Invalid port",
                    "host": "localhost",
                    "port": 70000,
                    "executionMode": "DURATION",
                    "measurementDurationMs": 3000,
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL"
                    ],
                    "threadPoolSize": 10
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.port")
                                .value(
                                        "Port must be between 1 and 65535."
                                )
                );
    }

    // ================================================================
    // VALIDATION - REQUESTS MODE WITHOUT TOTAL REQUESTS
    // ================================================================

    @Test
    void requestsModeWithoutTotalRequestsShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "Invalid Requests Mode Test",
                    "description": "Missing request count",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "REQUESTS",
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL"
                    ],
                    "threadPoolSize": 10
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.totalRequests")
                                .value(
                                        "Total requests must be greater than 0 "
                                                + "when execution mode is REQUESTS."
                                )
                );
    }

    // ================================================================
    // VALIDATION - DURATION MODE WITHOUT DURATION
    // ================================================================

    @Test
    void durationModeWithoutMeasurementDurationShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "Invalid Duration Mode Test",
                    "description": "Missing duration",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "DURATION",
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL"
                    ],
                    "threadPoolSize": 10
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.measurementDurationMs")
                                .value(
                                        "Measurement duration must be greater than 0 "
                                                + "when execution mode is DURATION."
                                )
                );
    }

    // ================================================================
    // VALIDATION - THREAD POOL WITHOUT THREAD POOL SIZE
    // ================================================================

    @Test
    void threadPoolWithoutPoolSizeShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "Missing Pool Size Test",
                    "description": "Missing thread pool size",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "DURATION",
                    "measurementDurationMs": 3000,
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL"
                    ]
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.threadPoolSize")
                                .value(
                                        "Thread pool size must be greater than 0 "
                                                + "when THREAD_POOL architecture "
                                                + "is selected."
                                )
                );
    }

    // ================================================================
    // VALIDATION - NO ARCHITECTURE
    // ================================================================

    @Test
    void experimentWithoutArchitectureShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "Missing Architecture Test",
                    "description": "No architecture selected",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "DURATION",
                    "measurementDurationMs": 3000,
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": []
                }
                """;

        mockMvc.perform(
                post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.architectures")
                                .value(
                                        "At least one server architecture must be selected."
                                )
                );
    }

    // ================================================================
    // ARCHITECTURE LIST
    // ================================================================

    @Test
    void getArchitecturesShouldReturnAllFourArchitectures()
            throws Exception {

        mockMvc.perform(
                get("/api/architectures")
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(4)
                )
                .andExpect(
                        jsonPath("$[0].architecture")
                                .value("SINGLE_THREADED")
                )
                .andExpect(
                        jsonPath("$[1].architecture")
                                .value("MULTI_THREADED")
                )
                .andExpect(
                        jsonPath("$[2].architecture")
                                .value("THREAD_POOL")
                )
                .andExpect(
                        jsonPath("$[3].architecture")
                                .value("VIRTUAL_THREAD")
                )
                .andExpect(
                        jsonPath(
                                "$[2].supportedSettings"
                        )
                                .isArray()
                );
    }

    // ================================================================
    // RESULTS BEFORE COMPLETION
    // ================================================================

    @Test
    void getResultsBeforeExecutionShouldReturnConflict()
            throws Exception {

        String requestBody = """
                {
                    "name": "Results Not Ready Test",
                    "description": "Results should not exist yet",
                    "host": "localhost",
                    "port": 8010,
                    "executionMode": "DURATION",
                    "measurementDurationMs": 3000,
                    "concurrency": 5,
                    "warmupDurationMs": 1000,
                    "requestTimeoutMs": 2000,
                    "repetitions": 1,
                    "architectures": [
                        "THREAD_POOL"
                    ],
                    "threadPoolSize": 10
                }
                """;

        String response =
                mockMvc.perform(
                        post("/api/experiments")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String experimentId =
                extractExperimentId(
                        response
                );

        mockMvc.perform(
                get(
                        "/api/experiments/"
                                + experimentId
                                + "/results"
                )
        )
                .andExpect(
                        status().isConflict()
                );
    }

    private String extractExperimentId(
            String response
    ) {

        String marker =
                "\"id\":\"";

        int start =
                response.indexOf(marker);

        if (start < 0) {

            throw new IllegalStateException(
                    "Experiment ID was not found in API response."
            );
        }

        start += marker.length();

        int end =
                response.indexOf(
                        "\"",
                        start
                );

        if (end < 0) {

            throw new IllegalStateException(
                    "Experiment ID was not properly formatted."
            );
        }

        return response.substring(
                start,
                end
        );
    }
}