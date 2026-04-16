package com.sviat.tictactoe.ui.client;

import com.sviat.tictactoe.ui.dto.SessionResponse;

public interface GameSessionClient {

    SessionResponse createSession();

    SessionResponse simulateSession(String sessionId);

    SessionResponse getSession(String sessionId);
}
