package com.sviat.tictactoe.ui.service;

import com.sviat.tictactoe.ui.client.GameSessionClient;
import com.sviat.tictactoe.ui.dto.SimulationResultResponse;
import com.sviat.tictactoe.ui.dto.SessionResponse;
import org.springframework.stereotype.Service;

@Service
public class SimulationFacade {

    private final GameSessionClient gameSessionClient;

    public SimulationFacade(GameSessionClient gameSessionClient) {
        this.gameSessionClient = gameSessionClient;
    }

    public SimulationResultResponse startSimulation() {
        SessionResponse createdSession = gameSessionClient.createSession();
        SessionResponse simulatedSession = gameSessionClient.simulateSession(createdSession.sessionId());
        return new SimulationResultResponse(simulatedSession);
    }

    public SessionResponse getSession(String sessionId) {
        return gameSessionClient.getSession(sessionId);
    }
}
