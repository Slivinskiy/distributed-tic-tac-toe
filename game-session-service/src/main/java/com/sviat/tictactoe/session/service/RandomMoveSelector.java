package com.sviat.tictactoe.session.service;

import com.sviat.tictactoe.session.domain.AvailableMove;
import com.sviat.tictactoe.session.domain.PlayerSymbol;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomMoveSelector implements MoveSelector {

    @Override
    public AvailableMove selectMove(PlayerSymbol[][] board, PlayerSymbol player) {
        List<AvailableMove> availableMoves = new ArrayList<>();

        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (board[row][column] == null) {
                    availableMoves.add(new AvailableMove(row, column));
                }
            }
        }

        if (availableMoves.isEmpty()) {
            throw new IllegalStateException("No available moves remain for simulation.");
        }

        int index = ThreadLocalRandom.current().nextInt(availableMoves.size());
        return availableMoves.get(index);
    }
}
