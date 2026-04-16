package com.sviat.tictactoe.engine.service;

import com.sviat.tictactoe.engine.domain.GameState;
import com.sviat.tictactoe.engine.domain.Move;
import com.sviat.tictactoe.engine.dto.GameResponse;
import com.sviat.tictactoe.engine.dto.MoveRequest;
import com.sviat.tictactoe.engine.exception.GameNotFoundException;
import com.sviat.tictactoe.engine.repository.InMemoryGameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final InMemoryGameRepository gameRepository;

    public GameService(InMemoryGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public GameResponse applyMove(String rawGameId, MoveRequest moveRequest) {
        String gameId = normalizeGameId(rawGameId);
        GameState gameState = gameRepository.findOrCreate(gameId);

        synchronized (gameState) {
            Move move = new Move(moveRequest.player(), moveRequest.row(), moveRequest.column());
            gameState.applyMove(move);
            return toResponse(gameState);
        }
    }

    public GameResponse getGame(String rawGameId) {
        String gameId = normalizeGameId(rawGameId);
        GameState gameState = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));

        synchronized (gameState) {
            return toResponse(gameState);
        }
    }

    private String normalizeGameId(String rawGameId) {
        String gameId = rawGameId == null ? "" : rawGameId.trim();
        if (gameId.isEmpty()) {
            throw new IllegalArgumentException("gameId must not be blank.");
        }
        return gameId;
    }

    private GameResponse toResponse(GameState gameState) {
        Move lastMove = gameState.getLastMove();
        GameResponse.MoveView lastMoveView = lastMove == null
            ? null
            : new GameResponse.MoveView(lastMove.player(), lastMove.row(), lastMove.column());

        return new GameResponse(
            gameState.getGameId(),
            gameState.getStatus(),
            gameState.getNextPlayer(),
            gameState.getWinner(),
            gameState.getMoveCount(),
            gameState.copyBoard(),
            lastMoveView
        );
    }
}
