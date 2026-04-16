package com.sviat.tictactoe.engine.repository;

import com.sviat.tictactoe.engine.domain.GameState;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryGameRepository {

    private final ConcurrentMap<String, GameState> games = new ConcurrentHashMap<>();

    public Optional<GameState> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public GameState findOrCreate(String gameId) {
        return games.computeIfAbsent(gameId, GameState::new);
    }
}
