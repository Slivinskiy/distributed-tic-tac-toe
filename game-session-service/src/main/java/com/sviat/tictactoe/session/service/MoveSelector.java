package com.sviat.tictactoe.session.service;

import com.sviat.tictactoe.session.domain.AvailableMove;
import com.sviat.tictactoe.session.domain.PlayerSymbol;

public interface MoveSelector {

    AvailableMove selectMove(PlayerSymbol[][] board, PlayerSymbol player);
}
