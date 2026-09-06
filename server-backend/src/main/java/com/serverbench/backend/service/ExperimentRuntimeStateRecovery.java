package com.serverbench.backend.service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.serverbench.backend.entity.ExperimentEntity;
import com.serverbench.backend.repository.ExperimentRepository;
import com.serverbench.backend.repository.ExperimentRuntimeStateRepository;

@Component
public class ExperimentRuntimeStateRecovery
        implements CommandLineRunner {

    private final ExperimentRepository experimentRepository;

    private final ExperimentRuntimeStateRepository
            experimentRuntimeStateRepository;

    public ExperimentRuntimeStateRecovery(
            ExperimentRepository experimentRepository,
            ExperimentRuntimeStateRepository
                    experimentRuntimeStateRepository) {

        this.experimentRepository =
                experimentRepository;

        this.experimentRuntimeStateRepository =
                experimentRuntimeStateRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        List<ExperimentEntity> runningExperiments =
                experimentRepository.findByStatus(
                        ExperimentService.ExperimentStatus.RUNNING.name()
                );

        for (ExperimentEntity experiment :
                runningExperiments) {

            experiment.setStatus(
                    ExperimentService.ExperimentStatus.FAILED.name()
            );

            experimentRepository.save(
                    experiment
            );

            experimentRuntimeStateRepository.delete(
                    experiment.getId()
            );
        }
    }
}