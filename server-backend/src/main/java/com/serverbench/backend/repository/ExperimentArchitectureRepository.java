package com.serverbench.backend.repository;

import com.serverbench.backend.entity.ExperimentArchitectureEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperimentArchitectureRepository
        extends JpaRepository<ExperimentArchitectureEntity, Long> {

    List<ExperimentArchitectureEntity>
    findByExperimentId(String experimentId);
}