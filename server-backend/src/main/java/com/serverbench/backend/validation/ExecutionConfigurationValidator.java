package com.serverbench.backend.validation;

import java.util.List;

import com.serverbench.backend.dto.request.ExperimentRequest;
import com.serverbench.engine.benchmark.ExecutionMode;
import com.serverbench.engine.benchmark.ServerArchitecture;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExecutionConfigurationValidator
        implements ConstraintValidator<
                ValidExecutionConfiguration,
                ExperimentRequest
                > {

    @Override
    public boolean isValid(
            ExperimentRequest request,
            ConstraintValidatorContext context
    ) {

        /*
         * Field-level validation such as @NotNull,
         * @Positive, @Size, etc. is handled separately
         * by Jakarta Validation.
         *
         * This validator handles rules where one field
         * depends on another field.
         */
        if (request == null) {
            return true;
        }

        ExecutionMode executionMode =
                request.getExecutionMode();

        /*
         * executionMode itself is validated by @NotNull.
         * If it is missing, allow the field-level validator
         * to report that error rather than producing another
         * conditional error here.
         */
        if (executionMode == null) {
            return true;
        }

        boolean valid = true;

        context.disableDefaultConstraintViolation();

        // ============================================================
        // REQUESTS / DURATION VALIDATION
        // ============================================================

        if (executionMode == ExecutionMode.REQUESTS) {

            Integer totalRequests =
                    request.getTotalRequests();

            if (totalRequests == null
                    || totalRequests <= 0) {

                context
                        .buildConstraintViolationWithTemplate(
                                "Total requests must be greater than 0 "
                                        + "when execution mode is REQUESTS."
                        )
                        .addPropertyNode("totalRequests")
                        .addConstraintViolation();

                valid = false;
            }
        }

        if (executionMode == ExecutionMode.DURATION) {

            Long measurementDurationMs =
                    request.getMeasurementDurationMs();

            if (measurementDurationMs == null
                    || measurementDurationMs <= 0) {

                context
                        .buildConstraintViolationWithTemplate(
                                "Measurement duration must be greater than 0 "
                                        + "when execution mode is DURATION."
                        )
                        .addPropertyNode(
                                "measurementDurationMs"
                        )
                        .addConstraintViolation();

                valid = false;
            }
        }

        // ============================================================
        // THREAD POOL VALIDATION
        // ============================================================

        List<ServerArchitecture> architectures =
                request.getArchitectures();

        /*
         * @NotEmpty on architectures handles the case where
         * the list is missing or empty.
         *
         * If it is null/empty here, there is no need to perform
         * the architecture-specific rule.
         */
        if (architectures == null
                || architectures.isEmpty()) {

            return valid;
        }

        boolean threadPoolSelected =
                architectures.contains(
                        ServerArchitecture.THREAD_POOL
                );

        if (threadPoolSelected) {

            Integer threadPoolSize =
                    request.getThreadPoolSize();

            if (threadPoolSize == null
                    || threadPoolSize <= 0) {

                context
                        .buildConstraintViolationWithTemplate(
                                "Thread pool size must be greater than 0 "
                                        + "when THREAD_POOL architecture "
                                        + "is selected."
                        )
                        .addPropertyNode(
                                "threadPoolSize"
                        )
                        .addConstraintViolation();

                valid = false;
            }
        }

        return valid;
    }
}