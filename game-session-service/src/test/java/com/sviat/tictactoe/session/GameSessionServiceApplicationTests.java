package com.sviat.tictactoe.session;

import com.sviat.tictactoe.session.client.GameEngineClient;
import com.sviat.tictactoe.session.client.dto.EngineGameResponse;
import com.sviat.tictactoe.session.client.dto.EngineMoveRequest;
import com.sviat.tictactoe.session.domain.AvailableMove;
import com.sviat.tictactoe.session.domain.EngineGameStatus;
import com.sviat.tictactoe.session.domain.PlayerSymbol;
import com.sviat.tictactoe.session.service.MoveSelector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameSessionServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsSessionWithEmptyBoard() throws Exception {
        mockMvc.perform(post("/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").isNotEmpty())
            .andExpect(jsonPath("$.gameId").isNotEmpty())
            .andExpect(jsonPath("$.sessionStatus").value("CREATED"))
            .andExpect(jsonPath("$.gameStatus").value(nullValue()))
            .andExpect(jsonPath("$.nextPlayer").value("X"))
            .andExpect(jsonPath("$.winner").value(nullValue()))
            .andExpect(jsonPath("$.moveCount").value(0))
            .andExpect(jsonPath("$.moveHistory.length()").value(0));
    }

    @Test
    void getsSessionById() throws Exception {
        String sessionId = createSession();

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId))
            .andExpect(jsonPath("$.sessionStatus").value("CREATED"))
            .andExpect(jsonPath("$.nextPlayer").value("X"));
    }

    @Test
    void simulatesGameToCompletion() throws Exception {
        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId))
            .andExpect(jsonPath("$.sessionStatus").value("X_WON"))
            .andExpect(jsonPath("$.gameStatus").value("X_WON"))
            .andExpect(jsonPath("$.winner").value("X"))
            .andExpect(jsonPath("$.nextPlayer").value(nullValue()))
            .andExpect(jsonPath("$.moveCount").value(7))
            .andExpect(jsonPath("$.moveHistory.length()").value(7))
            .andExpect(jsonPath("$.board[2][0]").value("X"));
    }

    @Test
    void rejectsSimulatingFinishedSession() throws Exception {
        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isOk());

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("already finished")));
    }

    @Test
    void returnsNotFoundForUnknownSession() throws Exception {
        mockMvc.perform(get("/sessions/{sessionId}", "missing-session"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Session 'missing-session' was not found."));
    }

    private String createSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/sessions"))
            .andExpect(status().isOk())
            .andReturn();

        String body = result.getResponse().getContentAsString();
        return extractJsonField(body, "sessionId");
    }

    private String extractJsonField(String body, String fieldName) {
        String token = "\"%s\":\"".formatted(fieldName);
        int start = body.indexOf(token);
        int valueStart = start + token.length();
        int valueEnd = body.indexOf('"', valueStart);
        return body.substring(valueStart, valueEnd);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        GameEngineClient gameEngineClient() {
            return new InMemoryFakeEngineClient();
        }

        @Bean
        @Primary
        MoveSelector moveSelector() {
            return (board, player) -> {
                for (int row = 0; row < board.length; row++) {
                    for (int column = 0; column < board[row].length; column++) {
                        if (board[row][column] == null) {
                            return new AvailableMove(row, column);
                        }
                    }
                }
                throw new IllegalStateException("No available moves remain for simulation.");
            };
        }
    }

    static class InMemoryFakeEngineClient implements GameEngineClient {

        private final Map<String, FakeGame> games = new ConcurrentHashMap<>();

        @Override
        public EngineGameResponse makeMove(String gameId, EngineMoveRequest moveRequest) {
            FakeGame game = games.computeIfAbsent(gameId, id -> new FakeGame());
            return game.applyMove(moveRequest, gameId);
        }
    }

    static class FakeGame {

        private final PlayerSymbol[][] board = new PlayerSymbol[3][3];
        private PlayerSymbol nextPlayer = PlayerSymbol.X;
        private PlayerSymbol winner;
        private EngineGameStatus status = EngineGameStatus.IN_PROGRESS;
        private int moveCount;

        EngineGameResponse applyMove(EngineMoveRequest request, String gameId) {
            if (status != EngineGameStatus.IN_PROGRESS) {
                throw new IllegalStateException("finished");
            }
            if (request.player() != nextPlayer) {
                throw new IllegalStateException("wrong turn");
            }
            if (board[request.row()][request.column()] != null) {
                throw new IllegalStateException("occupied");
            }

            board[request.row()][request.column()] = request.player();
            moveCount++;

            if (hasWinningLine(request.player())) {
                winner = request.player();
                status = request.player() == PlayerSymbol.X ? EngineGameStatus.X_WON : EngineGameStatus.O_WON;
                nextPlayer = null;
            }
            else if (moveCount == 9) {
                status = EngineGameStatus.DRAW;
                nextPlayer = null;
            }
            else {
                nextPlayer = request.player() == PlayerSymbol.X ? PlayerSymbol.O : PlayerSymbol.X;
            }

            return new EngineGameResponse(
                gameId,
                status,
                nextPlayer,
                winner,
                moveCount,
                copyBoard(),
                new EngineGameResponse.MoveView(request.player(), request.row(), request.column())
            );
        }

        private boolean hasWinningLine(PlayerSymbol player) {
            for (int row = 0; row < 3; row++) {
                if (board[row][0] == player && board[row][1] == player && board[row][2] == player) {
                    return true;
                }
            }
            for (int column = 0; column < 3; column++) {
                if (board[0][column] == player && board[1][column] == player && board[2][column] == player) {
                    return true;
                }
            }
            return (board[0][0] == player && board[1][1] == player && board[2][2] == player)
                || (board[0][2] == player && board[1][1] == player && board[2][0] == player);
        }

        private PlayerSymbol[][] copyBoard() {
            PlayerSymbol[][] copy = new PlayerSymbol[3][3];
            for (int row = 0; row < 3; row++) {
                System.arraycopy(board[row], 0, copy[row], 0, 3);
            }
            return copy;
        }
    }
}
