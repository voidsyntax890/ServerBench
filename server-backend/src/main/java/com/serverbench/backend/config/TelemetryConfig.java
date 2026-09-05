package com.serverbench.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

@Configuration
public class TelemetryConfig {

    private static final String SERVICE_NAME =
            "serverbench-backend";

    @Bean
    public OpenTelemetry openTelemetry() {

        Resource resource =
                Resource.getDefault().merge(
                        Resource.create(
                                Attributes.builder()
                                        .put(
                                                "service.name",
                                                SERVICE_NAME
                                        )
                                        .build()
                        )
                );

        LoggingSpanExporter loggingExporter =
                new LoggingSpanExporter();

        OtlpGrpcSpanExporter otlpExporter =
                OtlpGrpcSpanExporter.builder()
                        .setEndpoint(
                                "http://localhost:4317"
                        )
                        .build();

        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .setResource(resource)
                        .addSpanProcessor(
                                BatchSpanProcessor.builder(
                                        loggingExporter
                                ).build()
                        )
                        .addSpanProcessor(
                                BatchSpanProcessor.builder(
                                        otlpExporter
                                ).build()
                        )
                        .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(
                        tracerProvider
                )
                .buildAndRegisterGlobal();
    }

    @Bean
    public Tracer serverBenchTracer(
            OpenTelemetry openTelemetry
    ) {

        return openTelemetry
                .getTracer(
                        "com.serverbench"
                );
    }
}