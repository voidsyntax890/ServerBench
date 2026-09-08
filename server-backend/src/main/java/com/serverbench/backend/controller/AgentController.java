package com.serverbench.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverbench.backend.dto.response.AgentRecordResponse;
import com.serverbench.backend.service.AgentRegistryService;
import com.serverbench.distributed.contracts.AgentHeartbeatEvent;
import com.serverbench.distributed.contracts.AgentRegistrationEvent;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "http://localhost:5173")
public class AgentController {

    private final AgentRegistryService agentRegistryService;

    public AgentController(
            AgentRegistryService agentRegistryService
    ) {
        this.agentRegistryService = agentRegistryService;
    }

    // ================================================================
    // AGENT REGISTRATION
    // ================================================================
    @PostMapping("/register")
    public ResponseEntity<AgentRecordResponse> register(
            @RequestBody AgentRegistrationEvent event
    ) {
        AgentRecordResponse response =
                agentRegistryService.register(event);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ================================================================
    // AGENT HEARTBEAT
    // ================================================================
    @PostMapping("/heartbeat")
    public ResponseEntity<AgentRecordResponse> heartbeat(
            @RequestBody AgentHeartbeatEvent event
    ) {
        return ResponseEntity.ok(
                agentRegistryService.heartbeat(event)
        );
    }

    // ================================================================
    // GET ALL AGENTS
    // ================================================================
    @GetMapping
    public ResponseEntity<List<AgentRecordResponse>> getAgents() {
        return ResponseEntity.ok(
                agentRegistryService.getAgents()
        );
    }

    // ================================================================
    // GET ONE AGENT
    // ================================================================
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentRecordResponse> getAgent(
            @PathVariable("agentId") String agentId
    ) {
        return ResponseEntity.ok(
                agentRegistryService.getAgent(agentId)
        );
    }
}
