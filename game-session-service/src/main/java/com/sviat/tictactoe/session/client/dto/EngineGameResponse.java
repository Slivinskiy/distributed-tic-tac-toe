package com.sviat.tictactoe.session.client.dto;

import com.sviat.tictactoe.session.domain.EngineGameStatus;
import com.sviat.tictactoe.session.domain.PlayerSymbol;

public record EngineGameResponse(
    String gameId,
    EngineGameStatus status,
    PlayerSymbol nextPlayer,
    PlayerSymbol winner,
    int moveCount,
    PlayerSymbol[][] board,
    MoveView lastMove
) {
    public record MoveView(PlayerSymbol player, int row, int column) {
    }
}
