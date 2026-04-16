package com.sviat.tictactoe.ui;

import com.sviat.tictactoe.ui.client.GameSessionClient;
import com.sviat.tictactoe.ui.domain.EngineGameStatus;
import com.sviat.tictactoe.ui.domain.PlayerSymbol;
import com.sviat.tictactoe.ui.domain.SessionStatus;
import com.sviat.tictactoe.ui.dto.SessionResponse;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TictactoeUiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersHomePage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Distributed Tic Tac Toe")))
            .andExpect(content().string(containsString("Start Simulation")));
    }

    @Test
    void startsSimulationThroughUiApi() throws Exception {
        mockMvc.perform(post("/api/simulations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.session.sessionId").value("session-123"))
            .andExpect(jsonPath("$.session.sessionStatus").value("X_WON"))
            .andExpect(jsonPath("$.session.moveHistory.length()").value(3))
            .andExpect(jsonPath("$.session.board[0][0]").value("X"));
    }

    @Test
    void fetchesSessionByIdThroughUiApi() throws Exception {
        mockMvc.perform(get("/api/simulations/{sessionId}", "session-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value("session-123"))
            .andExpect(jsonPath("$.sessionStatus").value("X_WON"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        GameSessionClient gameSessionClient() {
            return new FakeGameSessionClient();
        }
    }

    static class FakeGameSessionClient implements GameSessionClient {

        @Override
        public SessionResponse createSession() {
            return sessionResponse(SessionStatus.CREATED, EngineGameStatus.IN_PROGRESS, null, 0, List.of());
        }

        @Override
        public SessionResponse simulateSession(String sessionId) {
            return sessionResponse(
                SessionStatus.X_WON,
                EngineGameStatus.X_WON,
                PlayerSymbol.X,
                3,
                List.of(
                    new SessionResponse.MoveHistoryItem(1, PlayerSymbol.X, 0, 0, EngineGameStatus.IN_PROGRESS, OffsetDateTime.now()),
                    new SessionResponse.MoveHistoryItem(2, PlayerSymbol.O, 1, 1, EngineGameStatus.IN_PROGRESS, OffsetDateTime.now()),
                    new SessionResponse.MoveHistoryItem(3, PlayerSymbol.X, 0, 1, EngineGameStatus.X_WON, OffsetDateTime.now())
                )
            );
        }

        @Override
        public SessionResponse getSession(String sessionId) {
            return simulateSession(sessionId);
        }

        private SessionResponse sessionResponse(
            SessionStatus sessionStatus,
            EngineGameStatus gameStatus,
            PlayerSymbol winner,
            int moveCount,
            List<SessionResponse.MoveHistoryItem> moveHistory
        ) {
            PlayerSymbol[][] board = new PlayerSymbol[3][3];
            board[0][0] = PlayerSymbol.X;
            board[0][1] = PlayerSymbol.X;
            board[1][1] = PlayerSymbol.O;

            return new SessionResponse(
                "session-123",
                "session-123",
                sessionStatus,
                gameStatus,
                null,
                winner,
                moveCount,
                board,
                moveHistory,
                OffsetDateTime.now(),
                OffsetDateTime.now()
            );
        }
    }
}
