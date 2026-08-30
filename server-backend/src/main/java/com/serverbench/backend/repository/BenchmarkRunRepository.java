package com.serverbench.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serverbench.backend.entity.BenchmarkRunEntity;

public interface BenchmarkRunRepository
        extends JpaRepository<BenchmarkRunEntity, String> {

    List<BenchmarkRunEntity> findByExperiment_Id(
            String experimentId
    );
}