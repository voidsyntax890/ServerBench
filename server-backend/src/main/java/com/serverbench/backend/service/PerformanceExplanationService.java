package com.serverbench.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.serverbench.engine.benchmark.analysis.BottleneckFinding;

/**
 * Generates a human-readable explanation for an already-detected
 * ServerBench performance finding.
 *
 * The rule-based finding remains the source of truth. AI is used only
 * to explain the supplied evidence and recommendation.
 */
@Service
public class PerformanceExplanationService {

    private static final String SYSTEM_PROMPT = """
            You are the ServerBench Performance Advisor.

            Your job is to explain an already-detected performance finding
            using only the evidence provided by ServerBench.

            Strict rules:
            - Never invent benchmark measurements.
            - Never change or reinterpret the finding severity.
            - Never invent CPU, memory, thread, queue, throughput, or latency data.
            - Never claim that an architecture is universally best.
            - Do not introduce facts that are not supported by the supplied finding.
            - Clearly distinguish measured evidence from possible interpretation.
            - Keep the explanation concise and technically useful.
            - Recommend investigation or validation steps rather than unsupported fixes.

            Structure the response with these sections:
            Why this matters:
            What the evidence suggests:
            What to investigate next:
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
            BottleneckFinding finding
    ) {

        if (finding == null) {
            throw new IllegalArgumentException(
                    "Bottleneck finding cannot be null."
            );
        }

        String prompt = buildPrompt(finding);

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String buildPrompt(
            BottleneckFinding finding
    ) {

        return """
                Explain the following ServerBench performance finding.

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
                """.formatted(
                finding.getCategory().name(),
                finding.getSeverity().name(),
                finding.getArchitecture(),
                finding.getTitle(),
                finding.getDescription(),
                finding.getEvidence(),
                finding.getRecommendation()
        );
    }
}