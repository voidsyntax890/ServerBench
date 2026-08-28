package com.serverbench.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(
        validatedBy = ExecutionConfigurationValidator.class
)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExecutionConfiguration {

    String message() default
            "Invalid configuration for the selected execution mode.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}