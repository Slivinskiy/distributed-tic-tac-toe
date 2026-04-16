package com.sviat.tictactoe.session.repository;

import com.sviat.tictactoe.session.domain.SessionState;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemorySessionRepository {

    private final ConcurrentMap<String, SessionState> sessions = new ConcurrentHashMap<>();

    public SessionState save(SessionState sessionState) {
        sessions.put(sessionState.getSessionId(), sessionState);
        return sessionState;
    }

    public Optional<SessionState> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
