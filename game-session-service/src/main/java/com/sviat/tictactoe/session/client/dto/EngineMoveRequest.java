package com.sviat.tictactoe.session.client.dto;

import com.sviat.tictactoe.session.domain.PlayerSymbol;

public record EngineMoveRequest(PlayerSymbol player, int row, int column) {
}
