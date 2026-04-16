package com.sviat.tictactoe.session.domain;

public enum SessionStatus {
    CREATED,
    IN_PROGRESS,
    X_WON,
    O_WON,
    DRAW;

    public static SessionStatus fromGameStatus(EngineGameStatus gameStatus) {
        return switch (gameStatus) {
            case IN_PROGRESS -> IN_PROGRESS;
            case X_WON -> X_WON;
            case O_WON -> O_WON;
            case DRAW -> DRAW;
        };
    }

    public boolean isFinished() {
        return this == X_WON || this == O_WON || this == DRAW;
    }
}
