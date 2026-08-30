package com.serverbench.backend.repository;

import com.serverbench.backend.entity.ExperimentEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperimentRepository
        extends JpaRepository<ExperimentEntity, String> {
}