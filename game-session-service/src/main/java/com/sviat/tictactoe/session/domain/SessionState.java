package com.sviat.tictactoe.session.domain;

import com.sviat.tictactoe.session.client.dto.EngineGameResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class SessionState {

    public static final int BOARD_SIZE = 3;

    private final String sessionId;
    private final String gameId;
    private final OffsetDateTime createdAt;
    private final PlayerSymbol[][] board;
    private final List<SessionMoveRecord> moveHistory;
    private OffsetDateTime updatedAt;
    private SessionStatus sessionStatus;
    private EngineGameStatus gameStatus;
    private PlayerSymbol nextPlayer;
    private PlayerSymbol winner;
    private int moveCount;

    public SessionState(String sessionId, OffsetDateTime createdAt) {
        this.sessionId = sessionId;
        this.gameId = sessionId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.sessionStatus = SessionStatus.CREATED;
        this.nextPlayer = PlayerSymbol.X;
        this.board = new PlayerSymbol[BOARD_SIZE][BOARD_SIZE];
        this.moveHistory = new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getGameId() {
        return gameId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public EngineGameStatus getGameStatus() {
        return gameStatus;
    }

    public PlayerSymbol getNextPlayer() {
        return nextPlayer;
    }

    public PlayerSymbol getWinner() {
        return winner;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public List<SessionMoveRecord> getMoveHistory() {
        return List.copyOf(moveHistory);
    }

    public PlayerSymbol[][] copyBoard() {
        PlayerSymbol[][] copy = new PlayerSymbol[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, BOARD_SIZE);
        }
        return copy;
    }

    public boolean isFinished() {
        return sessionStatus.isFinished();
    }

    public void applyEngineState(AppliedMove move, EngineGameResponse gameResponse, OffsetDateTime timestamp) {
        overwriteBoard(gameResponse.board());
        this.moveCount = gameResponse.moveCount();
        this.gameStatus = gameResponse.status();
        this.sessionStatus = SessionStatus.fromGameStatus(gameResponse.status());
        this.nextPlayer = gameResponse.nextPlayer();
        this.winner = gameResponse.winner();
        this.updatedAt = timestamp;

        moveHistory.add(new SessionMoveRecord(
            moveCount,
            move.player(),
            move.row(),
            move.column(),
            gameResponse.status(),
            timestamp
        ));
    }

    private void overwriteBoard(PlayerSymbol[][] sourceBoard) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            System.arraycopy(sourceBoard[row], 0, board[row], 0, BOARD_SIZE);
        }
    }

    public record AppliedMove(PlayerSymbol player, int row, int column) {
    }
}
