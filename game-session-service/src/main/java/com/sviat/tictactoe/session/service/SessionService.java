package com.sviat.tictactoe.session.service;

import com.sviat.tictactoe.session.client.GameEngineClient;
import com.sviat.tictactoe.session.client.dto.EngineGameResponse;
import com.sviat.tictactoe.session.client.dto.EngineMoveRequest;
import com.sviat.tictactoe.session.domain.AvailableMove;
import com.sviat.tictactoe.session.domain.SessionState;
import com.sviat.tictactoe.session.dto.SessionResponse;
import com.sviat.tictactoe.session.exception.SessionNotFoundException;
import com.sviat.tictactoe.session.exception.SessionRuleViolationException;
import com.sviat.tictactoe.session.repository.InMemorySessionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final InMemorySessionRepository sessionRepository;
    private final GameEngineClient gameEngineClient;
    private final MoveSelector moveSelector;
    private final Clock clock;

    public SessionService(
        InMemorySessionRepository sessionRepository,
        GameEngineClient gameEngineClient,
        MoveSelector moveSelector,
        Clock clock
    ) {
        this.sessionRepository = sessionRepository;
        this.gameEngineClient = gameEngineClient;
        this.moveSelector = moveSelector;
        this.clock = clock;
    }

    public SessionResponse createSession() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        SessionState sessionState = new SessionState(UUID.randomUUID().toString(), now);
        sessionRepository.save(sessionState);
        return toResponse(sessionState);
    }

    public SessionResponse getSession(String rawSessionId) {
        SessionState sessionState = getRequiredSession(rawSessionId);
        synchronized (sessionState) {
            return toResponse(sessionState);
        }
    }

    public SessionResponse simulateSession(String rawSessionId) {
        SessionState sessionState = getRequiredSession(rawSessionId);

        synchronized (sessionState) {
            if (sessionState.isFinished()) {
                throw new SessionRuleViolationException(
                    "Session '%s' is already finished with status %s."
                        .formatted(sessionState.getSessionId(), sessionState.getSessionStatus())
                );
            }

            while (!sessionState.isFinished()) {
                AvailableMove selectedMove = moveSelector.selectMove(
                    sessionState.copyBoard(),
                    sessionState.getNextPlayer()
                );

                EngineGameResponse gameResponse = gameEngineClient.makeMove(
                    sessionState.getGameId(),
                    new EngineMoveRequest(sessionState.getNextPlayer(), selectedMove.row(), selectedMove.column())
                );

                sessionState.applyEngineState(
                    new SessionState.AppliedMove(
                        gameResponse.lastMove().player(),
                        selectedMove.row(),
                        selectedMove.column()
                    ),
                    gameResponse,
                    OffsetDateTime.now(clock)
                );
            }

            return toResponse(sessionState);
        }
    }

    private SessionState getRequiredSession(String rawSessionId) {
        String sessionId = normalizeSessionId(rawSessionId);
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    private String normalizeSessionId(String rawSessionId) {
        String sessionId = rawSessionId == null ? "" : rawSessionId.trim();
        if (sessionId.isEmpty()) {
            throw new IllegalArgumentException("sessionId must not be blank.");
        }
        return sessionId;
    }

    private SessionResponse toResponse(SessionState sessionState) {
        List<SessionResponse.MoveHistoryItem> moveHistory = sessionState.getMoveHistory()
            .stream()
            .map(SessionResponse.MoveHistoryItem::from)
            .toList();

        return new SessionResponse(
            sessionState.getSessionId(),
            sessionState.getGameId(),
            sessionState.getSessionStatus(),
            sessionState.getGameStatus(),
            sessionState.getNextPlayer(),
            sessionState.getWinner(),
            sessionState.getMoveCount(),
            sessionState.copyBoard(),
            moveHistory,
            sessionState.getCreatedAt(),
            sessionState.getUpdatedAt()
        );
    }
}
