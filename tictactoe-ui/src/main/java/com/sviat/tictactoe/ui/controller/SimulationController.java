package com.sviat.tictactoe.ui.controller;

import com.sviat.tictactoe.ui.dto.SimulationResultResponse;
import com.sviat.tictactoe.ui.dto.SessionResponse;
import com.sviat.tictactoe.ui.service.SimulationFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationFacade simulationFacade;

    public SimulationController(SimulationFacade simulationFacade) {
        this.simulationFacade = simulationFacade;
    }

    @PostMapping
    public SimulationResultResponse startSimulation() {
        return simulationFacade.startSimulation();
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable String sessionId) {
        return simulationFacade.getSession(sessionId);
    }
}
