package com.serverbench.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serverbench.backend.entity.BenchmarkMetricsEntity;

public interface BenchmarkMetricsRepository
        extends JpaRepository<BenchmarkMetricsEntity, Long> {

    Optional<BenchmarkMetricsEntity> findByRun_Id(
            String runId
    );
}