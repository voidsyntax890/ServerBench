package com.serverbench.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverbench.backend.dto.response.ArchitectureResponse;
import com.serverbench.backend.service.ExperimentService;

@RestController
@RequestMapping("/api/architectures")
public class ArchitectureController {

    private final ExperimentService experimentService;

    public ArchitectureController(
            ExperimentService experimentService
    ) {
        this.experimentService =
                experimentService;
    }

    @GetMapping
    public ResponseEntity<List<ArchitectureResponse>>
    getAvailableArchitectures() {

        List<ArchitectureResponse> responses =
                experimentService
                        .getAvailableArchitectures()
                        .stream()
                        .map(
                                ArchitectureResponse::new
                        )
                        .toList();

        return ResponseEntity.ok(
                responses
        );
    }
}