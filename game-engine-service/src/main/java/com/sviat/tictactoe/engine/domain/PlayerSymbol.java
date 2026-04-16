package com.sviat.tictactoe.engine.domain;

public enum PlayerSymbol {
    X,
    O;

    public PlayerSymbol opponent() {
        return this == X ? O : X;
    }

    public GameStatus winningStatus() {
        return this == X ? GameStatus.X_WON : GameStatus.O_WON;
    }
}
