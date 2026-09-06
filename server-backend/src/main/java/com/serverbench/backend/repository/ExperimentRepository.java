package com.serverbench.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serverbench.backend.entity.ExperimentEntity;

public interface ExperimentRepository
        extends JpaRepository<ExperimentEntity, String> {

    List<ExperimentEntity> findByStatus(String status);
}