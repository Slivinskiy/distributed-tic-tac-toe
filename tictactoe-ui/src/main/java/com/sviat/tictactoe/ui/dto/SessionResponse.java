package com.sviat.tictactoe.ui.dto;

import com.sviat.tictactoe.ui.domain.EngineGameStatus;
import com.sviat.tictactoe.ui.domain.PlayerSymbol;
import com.sviat.tictactoe.ui.domain.SessionStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record SessionResponse(
    String sessionId,
    String gameId,
    SessionStatus sessionStatus,
    EngineGameStatus gameStatus,
    PlayerSymbol nextPlayer,
    PlayerSymbol winner,
    int moveCount,
    PlayerSymbol[][] board,
    List<MoveHistoryItem> moveHistory,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public record MoveHistoryItem(
        int moveNumber,
        PlayerSymbol player,
        int row,
        int column,
        EngineGameStatus resultingGameStatus,
        OffsetDateTime performedAt
    ) {
    }
}
