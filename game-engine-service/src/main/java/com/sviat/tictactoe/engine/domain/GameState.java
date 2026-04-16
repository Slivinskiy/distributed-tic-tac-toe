package com.sviat.tictactoe.engine.domain;

import com.sviat.tictactoe.engine.exception.GameRuleViolationException;

public final class GameState {

    public static final int BOARD_SIZE = 3;

    private final String gameId;
    private final PlayerSymbol[][] board;
    private GameStatus status;
    private PlayerSymbol nextPlayer;
    private PlayerSymbol winner;
    private int moveCount;
    private Move lastMove;

    public GameState(String gameId) {
        this.gameId = gameId;
        this.board = new PlayerSymbol[BOARD_SIZE][BOARD_SIZE];
        this.status = GameStatus.IN_PROGRESS;
        this.nextPlayer = PlayerSymbol.X;
    }

    public String getGameId() {
        return gameId;
    }

    public GameStatus getStatus() {
        return status;
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

    public Move getLastMove() {
        return lastMove;
    }

    public PlayerSymbol[][] copyBoard() {
        PlayerSymbol[][] copy = new PlayerSymbol[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, BOARD_SIZE);
        }
        return copy;
    }

    public void applyMove(Move move) {
        validateMove(move);

        board[move.row()][move.column()] = move.player();
        moveCount++;
        lastMove = move;

        if (hasWinningLine(move.player())) {
            winner = move.player();
            status = move.player().winningStatus();
            nextPlayer = null;
            return;
        }

        if (moveCount == BOARD_SIZE * BOARD_SIZE) {
            status = GameStatus.DRAW;
            nextPlayer = null;
            return;
        }

        nextPlayer = move.player().opponent();
    }

    private void validateMove(Move move) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new GameRuleViolationException(
                "Game '%s' is already finished with status %s.".formatted(gameId, status)
            );
        }

        if (move.player() != nextPlayer) {
            if (moveCount == 0) {
                throw new GameRuleViolationException(
                    "Game '%s' must start with player %s.".formatted(gameId, nextPlayer)
                );
            }

            throw new GameRuleViolationException(
                "It is player %s's turn for game '%s'.".formatted(nextPlayer, gameId)
            );
        }

        if (board[move.row()][move.column()] != null) {
            throw new GameRuleViolationException(
                "Cell [%d,%d] is already occupied.".formatted(move.row(), move.column())
            );
        }
    }

    private boolean hasWinningLine(PlayerSymbol player) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (board[row][0] == player && board[row][1] == player && board[row][2] == player) {
                return true;
            }
        }

        for (int column = 0; column < BOARD_SIZE; column++) {
            if (board[0][column] == player && board[1][column] == player && board[2][column] == player) {
                return true;
            }
        }

        return (board[0][0] == player && board[1][1] == player && board[2][2] == player)
            || (board[0][2] == player && board[1][1] == player && board[2][0] == player);
    }
}
