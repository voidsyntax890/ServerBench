package com.serverbench.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.serverbench.distributed.contracts.AgentCapability;
import com.serverbench.distributed.contracts.AgentHeartbeatEvent;
import com.serverbench.distributed.contracts.AgentRegistrationEvent;
import com.serverbench.distributed.contracts.AgentStatus;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Component
public class AgentRegistrationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AgentRegistrationService.class
            );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final String controllerUrl;
    private final String configuredAgentId;
    private final String agentVersion;
    private final int maxConcurrency;

    private final AtomicBoolean registered =
            new AtomicBoolean(false);

    private final AtomicReference<String> activeJobId =
            new AtomicReference<>(null);

    private final String agentId;

    public AgentRegistrationService(
            ObjectMapper objectMapper,
            @Value(
                    "${serverbench.agent.controller-url:http://localhost:8080}"
            )
            String controllerUrl,
            @Value("${serverbench.agent.id:}")
            String configuredAgentId,
            @Value("${serverbench.agent.version:1.0.0}")
            String agentVersion,
            @Value("${serverbench.agent.max-concurrency:1000}")
            int maxConcurrency
    ) {

        if (maxConcurrency <= 0) {
            throw new IllegalArgumentException(
                    "Agent max concurrency must be greater than 0."
            );
        }

        this.objectMapper = objectMapper;
        this.httpClient =
                HttpClient.newHttpClient();

        this.controllerUrl =
                stripTrailingSlash(controllerUrl);

        this.configuredAgentId =
                configuredAgentId;

        this.agentVersion =
                agentVersion;

        this.maxConcurrency =
                maxConcurrency;

        this.agentId =
                resolveAgentId(configuredAgentId);
    }

    // ================================================================
    // STARTUP REGISTRATION
    // ================================================================

    @PostConstruct
    public void registerOnStartup() {
        register();
    }

    // ================================================================
    // HEARTBEAT / REGISTRATION MAINTENANCE
    // ================================================================

    @Scheduled(
            fixedDelayString =
                    "${serverbench.agent.heartbeat-interval-ms:5000}"
    )
    public void maintainRegistration() {

        if (!registered.get()) {
            register();
            return;
        }

        sendHeartbeat();
    }

    // ================================================================
    // AGENT REGISTRATION
    // ================================================================

    private void register() {

        AgentCapability capability =
                new AgentCapability(
                        List.of(
                                "SINGLE_THREADED",
                                "MULTI_THREADED",
                                "THREAD_POOL",
                                "VIRTUAL_THREAD"
                        ),
                        Runtime.getRuntime()
                                .availableProcessors(),
                        Runtime.getRuntime()
                                .maxMemory()
                                / (1024L * 1024L),
                        maxConcurrency
                );

        AgentRegistrationEvent event =
                new AgentRegistrationEvent(
                        UUID.randomUUID().toString(),
                        agentId,
                        agentVersion,
                        resolveHostName(),
                        getCurrentStatus(),
                        capability,
                        Instant.now()
                );

        try {

            HttpResponse<String> response =
                    post(
                            "/api/agents/register",
                            event
                    );

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                registered.set(true);

                log.info(
                        "ServerBench agent registered successfully: agentId={}, status={}, activeJobId={}",
                        agentId,
                        getCurrentStatus(),
                        getActiveJobId()
                );

            } else {

                registered.set(false);

                log.warn(
                        "Agent registration rejected: status={}, body={}",
                        response.statusCode(),
                        response.body()
                );
            }

        } catch (Exception exception) {

            registered.set(false);

            log.warn(
                    "Unable to register ServerBench agent {}: {}",
                    agentId,
                    exception.getMessage()
            );
        }
    }

    // ================================================================
    // AGENT HEARTBEAT
    // ================================================================

    private void sendHeartbeat() {

        AgentHeartbeatEvent event =
                new AgentHeartbeatEvent(
                        UUID.randomUUID().toString(),
                        agentId,
                        getCurrentStatus(),
                        getActiveJobId(),
                        Instant.now()
                );

        try {

            HttpResponse<String> response =
                    post(
                            "/api/agents/heartbeat",
                            event
                    );

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                return;
            }

            if (response.statusCode() == 400
                    || response.statusCode() == 404) {

                registered.set(false);
            }

            log.warn(
                    "Agent heartbeat rejected: status={}, body={}",
                    response.statusCode(),
                    response.body()
            );

        } catch (Exception exception) {

            log.warn(
                    "Agent heartbeat failed for {}: {}",
                    agentId,
                    exception.getMessage()
            );
        }
    }

    // ================================================================
    // JOB RESERVATION
    // ================================================================

    public boolean tryAcquireJob(
            String jobId
    ) {

        if (jobId == null
                || jobId.isBlank()) {

            return false;
        }

        if (!registered.get()) {

            log.warn(
                    "Rejecting job {} because agent {} is not registered.",
                    jobId,
                    agentId
            );

            return false;
        }

        boolean acquired =
                activeJobId.compareAndSet(
                        null,
                        jobId.trim()
                );

        if (!acquired) {

            log.warn(
                    "Rejecting job {} because agent {} is already executing job {}.",
                    jobId,
                    agentId,
                    activeJobId.get()
            );
        }

        return acquired;
    }

    public void releaseJob(
            String jobId
    ) {

        if (jobId == null
                || jobId.isBlank()) {

            return;
        }

        boolean released =
                activeJobId.compareAndSet(
                        jobId.trim(),
                        null
                );

        if (released) {

            log.info(
                    "Agent {} released job {} and is READY.",
                    agentId,
                    jobId
            );
        }
    }

    // ================================================================
    // AGENT STATE
    // ================================================================

    public AgentStatus getCurrentStatus() {

        if (activeJobId.get() != null) {
            return AgentStatus.BUSY;
        }

        if (registered.get()) {
            return AgentStatus.READY;
        }

        return AgentStatus.STARTING;
    }

    public String getActiveJobId() {
        return activeJobId.get();
    }

    public String getAgentId() {
        return agentId;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public boolean isRegistered() {
        return registered.get();
    }

    // ================================================================
    // HTTP POST
    // ================================================================

    private HttpResponse<String> post(
            String path,
            Object payload
    ) throws IOException, InterruptedException {

        String body =
                objectMapper.writeValueAsString(
                        payload
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        controllerUrl + path
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(body)
                        )
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // ================================================================
    // AGENT ID
    // ================================================================

    private String resolveAgentId(
            String configuredId
    ) {

        if (configuredId != null
                && !configuredId.isBlank()) {

            return configuredId.trim();
        }

        return "agent-"
                + UUID.randomUUID()
                        .toString()
                        .substring(
                                0,
                                8
                        );
    }

    // ================================================================
    // HOSTNAME
    // ================================================================

    private String resolveHostName() {

        try {

            return InetAddress
                    .getLocalHost()
                    .getHostName();

        } catch (Exception exception) {

            return "unknown-host";
        }
    }

    // ================================================================
    // URL NORMALIZATION
    // ================================================================

    private String stripTrailingSlash(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "Agent controller URL cannot be empty."
            );
        }

        String result =
                value.trim();

        while (result.endsWith("/")) {

            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        return result;
    }
}