package com.serverbench.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverbench.backend.service.DistributedBenchmarkOrchestrator;
import com.serverbench.distributed.contracts.BenchmarkJob;

@RestController
@RequestMapping("/api/experiments")
public class DistributedBenchmarkController {

    private final DistributedBenchmarkOrchestrator orchestrator;

    public DistributedBenchmarkController(
            DistributedBenchmarkOrchestrator orchestrator
    ) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/{experimentId}/distributed/start")
    public ResponseEntity<List<BenchmarkJob>> startDistributed(
            @PathVariable("experimentId") String experimentId
    ) {
        List<BenchmarkJob> jobs =
                orchestrator.start(experimentId);

        return ResponseEntity.accepted()
                .body(jobs);
    }
}
