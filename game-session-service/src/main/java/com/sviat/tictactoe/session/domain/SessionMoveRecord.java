package com.sviat.tictactoe.session.domain;

import java.time.OffsetDateTime;

public record SessionMoveRecord(
    int moveNumber,
    PlayerSymbol player,
    int row,
    int column,
    EngineGameStatus resultingGameStatus,
    OffsetDateTime performedAt
) {
}
