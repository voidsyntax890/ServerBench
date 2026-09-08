package com.serverbench.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverbench.backend.service.DistributedBenchmarkOrchestrator;
import com.serverbench.distributed.contracts.BenchmarkJob;
import com.serverbench.engine.benchmark.ServerArchitecture;

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
    public ResponseEntity<BenchmarkJob> startDistributed(
            @PathVariable("experimentId") String experimentId,
            @RequestParam("architecture") ServerArchitecture architecture
    ) {
        BenchmarkJob job =
                orchestrator.start(
                        experimentId,
                        architecture
                );

        return ResponseEntity.ok(job);
    }
}