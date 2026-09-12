package com.serverbench.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.serverbench.engine.benchmark.BenchmarkResult;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.analysis.BottleneckFinding;

/**
 * Generates a human-readable explanation for an already-detected
 * ServerBench performance finding.
 *
 * The rule-based finding remains the source of truth.
 * AI is used only to explain the finding using the supplied
 * finding evidence and the broader experiment result context.
 */
@Service
public class PerformanceExplanationService {

    private static final String SYSTEM_PROMPT = """
            You are the ServerBench Performance Advisor.

            Your job is to explain an already-detected ServerBench
            performance finding using only the benchmark data supplied
            by ServerBench.

            The rule-based finding is authoritative.

            Strict rules:
            - Never invent benchmark measurements.
            - Never change or reinterpret the finding severity.
            - Never invent CPU, memory, thread, queue, throughput, latency,
              or infrastructure data.
            - Never claim that an architecture is universally best.
            - Never present a possible cause as a confirmed cause.
            - Clearly distinguish measured evidence from interpretation.
            - Use the supplied experiment runs when comparing architectures
              or repeated measurements.
            - Prefer concrete measured values over generic advice.
            - Recommend investigation or validation steps rather than
              unsupported fixes.
            - Do not repeat the finding verbatim without adding useful
              interpretation.
            - Keep the response technically useful and reasonably concise.

            Return exactly these sections and no others:

            Why this matters:
            Evidence:
            Interpretation:
            What to investigate next:

            Each section should contain one or two concise paragraphs.
            Use only information supported by the supplied ServerBench data.
            """;

    private final ChatClient chatClient;

    public PerformanceExplanationService(
            ChatClient.Builder chatClientBuilder
    ) {
        this.chatClient =
                chatClientBuilder
                        .defaultSystem(SYSTEM_PROMPT)
                        .build();
    }

    public String explain(
            BottleneckFinding finding,
            ExperimentResult experimentResult
    ) {

        if (finding == null) {
            throw new IllegalArgumentException(
                    "Bottleneck finding cannot be null."
            );
        }

        if (experimentResult == null) {
            throw new IllegalArgumentException(
                    "Experiment result cannot be null."
            );
        }

        String prompt =
                buildPrompt(
                        finding,
                        experimentResult
                );

        String explanation =
                chatClient
                        .prompt()
                        .user(prompt)
                        .call()
                        .content();

        if (explanation == null
                || explanation.isBlank()) {

            throw new IllegalStateException(
                    "AI advisor returned an empty explanation."
            );
        }

        return explanation.trim();
    }

    static String buildPrompt(
            BottleneckFinding finding,
            ExperimentResult experimentResult
    ) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append("""
                Explain the following ServerBench performance finding.

                SELECTED FINDING
                ================

                Category:
                %s

                Severity:
                %s

                Architecture:
                %s

                Finding:
                %s

                Description:
                %s

                Measured evidence:
                %s

                Existing recommendation:
                %s

                EXPERIMENT RUN CONTEXT
                ======================

                The selected finding must be interpreted in the context
                of the complete experiment results below.

                """.formatted(
                finding.getCategory().name(),
                finding.getSeverity().name(),
                finding.getArchitecture(),
                finding.getTitle(),
                finding.getDescription(),
                finding.getEvidence(),
                finding.getRecommendation()
        ));

        List<ExperimentRunResult> runs =
                experimentResult.getRunResults();

        int runNumber = 0;

        for (ExperimentRunResult run : runs) {

            if (run == null) {
                continue;
            }

            runNumber++;

            prompt.append(
                    formatRunContext(
                            runNumber,
                            run
                    )
            );
        }

        prompt.append("""
                
                ANALYSIS REQUIREMENTS
                =====================

                1. Explain why the selected finding matters for this
                   particular benchmark.

                2. Cite the measured values that support the finding.
                   When useful, include p50, p95, p99, throughput,
                   success rate, error rate, or request counts.

                3. Use the other successful runs only when they provide
                   meaningful context for the selected finding.

                4. When repeated runs exist, distinguish a repeated
                   pattern from a single-run observation.

                5. Do not claim causation when the benchmark only shows
                   correlation or an observable pattern.

                6. End with concrete validation or investigation steps
                   that follow from the measured evidence.

                7. Do not recommend changing ServerBench code merely
                   because a performance result is unexpected.
                """);

        return prompt.toString();
    }

    private static String formatRunContext(
            int runNumber,
            ExperimentRunResult run
    ) {

        StringBuilder context =
                new StringBuilder();

        context.append(
                "Run "
                        + runNumber
                        + ":\n"
        );

        context.append(
                "architecture="
                        + run.getArchitecture().name()
                        + "\n"
        );

        context.append(
                "repetition="
                        + run.getRepetitionNumber()
                        + "\n"
        );

        context.append(
                "status="
                        + run.getStatus().name()
                        + "\n"
        );

        if (run.getErrorMessage() != null
                && !run.getErrorMessage().isBlank()) {

            context.append(
                    "errorMessage="
                            + run.getErrorMessage()
                            + "\n"
            );
        }

        BenchmarkResult result =
                run.getBenchmarkResult();

        if (result == null) {

            context.append(
                    "benchmarkResult=unavailable\n\n"
            );

            return context.toString();
        }

        context.append(
                "totalRequests="
                        + result.getTotalRequests()
                        + "\n"
        );

        context.append(
                "successfulRequests="
                        + result.getSuccessfulRequests()
                        + "\n"
        );

        context.append(
                "failedRequests="
                        + result.getFailedRequests()
                        + "\n"
        );

        context.append(
                "successRate="
                        + format(result.getSuccessRate())
                        + "%\n"
        );

        context.append(
                "errorRate="
                        + format(result.getErrorRate())
                        + "%\n"
        );

        context.append(
                "totalDurationMs="
                        + result.getTotalDurationMs()
                        + "\n"
        );

        context.append(
                "throughputRequestsPerSecond="
                        + format(
                                result.getThroughputRequestsPerSecond()
                        )
                        + "\n"
        );

        context.append(
                "averageLatencyMs="
                        + format(
                                result.getAverageLatencyMs()
                        )
                        + "\n"
        );

        context.append(
                "minimumLatencyMs="
                        + result.getMinimumLatencyMs()
                        + "\n"
        );

        context.append(
                "maximumLatencyMs="
                        + result.getMaximumLatencyMs()
                        + "\n"
        );

        context.append(
                "p50LatencyMs="
                        + format(
                                result.getP50LatencyMs()
                        )
                        + "\n"
        );

        context.append(
                "p95LatencyMs="
                        + format(
                                result.getP95LatencyMs()
                        )
                        + "\n"
        );

        context.append(
                "p99LatencyMs="
                        + format(
                                result.getP99LatencyMs()
                        )
                        + "\n"
        );

        context.append(
                "connectTimeouts="
                        + result.getConnectTimeouts()
                        + "\n"
        );

        context.append(
                "connectionRefused="
                        + result.getConnectionRefused()
                        + "\n"
        );

        context.append(
                "connectionResets="
                        + result.getConnectionResets()
                        + "\n"
        );

        context.append(
                "readTimeouts="
                        + result.getReadTimeouts()
                        + "\n"
        );

        context.append(
                "noResponseFailures="
                        + result.getNoResponseFailures()
                        + "\n"
        );

        context.append(
                "otherIoFailures="
                        + result.getOtherIoFailures()
                        + "\n\n"
        );

        return context.toString();
    }

    private static String format(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.4f",
                value
        );
    }
}