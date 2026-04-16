package com.sviat.tictactoe.engine.dto;

import com.sviat.tictactoe.engine.domain.GameStatus;
import com.sviat.tictactoe.engine.domain.PlayerSymbol;

public record GameResponse(
    String gameId,
    GameStatus status,
    PlayerSymbol nextPlayer,
    PlayerSymbol winner,
    int moveCount,
    PlayerSymbol[][] board,
    MoveView lastMove
) {
    public record MoveView(PlayerSymbol player, int row, int column) {
    }
}
