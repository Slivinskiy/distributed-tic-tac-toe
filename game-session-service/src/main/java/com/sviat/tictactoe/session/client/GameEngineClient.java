package com.sviat.tictactoe.session.client;

import com.sviat.tictactoe.session.client.dto.EngineGameResponse;
import com.sviat.tictactoe.session.client.dto.EngineMoveRequest;

public interface GameEngineClient {

    EngineGameResponse makeMove(String gameId, EngineMoveRequest moveRequest);
}
