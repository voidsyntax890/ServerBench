package com.serverbench.engine.benchmark.analysis;

import java.util.Objects;

public final class BottleneckFinding {

    private final BottleneckCategory category;
    private final BottleneckSeverity severity;

    private final String architecture;
    private final String title;
    private final String description;
    private final String evidence;
    private final String recommendation;

    public BottleneckFinding(
            BottleneckCategory category,
            BottleneckSeverity severity,
            String architecture,
            String title,
            String description,
            String evidence,
            String recommendation
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Finding category cannot be null."
            );
        }

        if (severity == null) {
            throw new IllegalArgumentException(
                    "Finding severity cannot be null."
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Finding title cannot be blank."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Finding description cannot be blank."
            );
        }

        if (evidence == null || evidence.isBlank()) {
            throw new IllegalArgumentException(
                    "Finding evidence cannot be blank."
            );
        }

        if (recommendation == null || recommendation.isBlank()) {
            throw new IllegalArgumentException(
                    "Finding recommendation cannot be blank."
            );
        }

        this.category = category;
        this.severity = severity;
        this.architecture =
                architecture == null
                        ? ""
                        : architecture.trim();

        this.title = title.trim();
        this.description = description.trim();
        this.evidence = evidence.trim();
        this.recommendation =
                recommendation.trim();
    }

    public BottleneckCategory getCategory() {
        return category;
    }

    public BottleneckSeverity getSeverity() {
        return severity;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getRecommendation() {
        return recommendation;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof BottleneckFinding other)) {
            return false;
        }

        return category == other.category
                && severity == other.severity
                && Objects.equals(
                        architecture,
                        other.architecture
                )
                && Objects.equals(title, other.title)
                && Objects.equals(
                        description,
                        other.description
                )
                && Objects.equals(
                        evidence,
                        other.evidence
                )
                && Objects.equals(
                        recommendation,
                        other.recommendation
                );
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                category,
                severity,
                architecture,
                title,
                description,
                evidence,
                recommendation
        );
    }

    @Override
    public String toString() {

        return "BottleneckFinding{" +
                "category=" + category +
                ", severity=" + severity +
                ", architecture='" +
                architecture + '\'' +
                ", title='" +
                title + '\'' +
                ", description='" +
                description + '\'' +
                ", evidence='" +
                evidence + '\'' +
                ", recommendation='" +
                recommendation + '\'' +
                '}';
    }
}