package com.sviat.tictactoe.engine;

import com.sviat.tictactoe.engine.domain.PlayerSymbol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameEngineServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsGameOnFirstMoveAndReturnsCurrentState() throws Exception {
        postMove("game-create", PlayerSymbol.X, 0, 0)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gameId").value("game-create"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.nextPlayer").value("O"))
            .andExpect(jsonPath("$.winner").value(nullValue()))
            .andExpect(jsonPath("$.moveCount").value(1))
            .andExpect(jsonPath("$.board[0][0]").value("X"))
            .andExpect(jsonPath("$.lastMove.player").value("X"))
            .andExpect(jsonPath("$.lastMove.row").value(0))
            .andExpect(jsonPath("$.lastMove.column").value(0));

        mockMvc.perform(get("/games/{gameId}", "game-create"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.board[0][0]").value("X"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void rejectsMoveWhenWrongPlayerStartsTheGame() throws Exception {
        postMove("game-wrong-starter", PlayerSymbol.O, 0, 0)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("must start with player X")));
    }

    @Test
    void rejectsMoveIntoOccupiedCell() throws Exception {
        postMove("game-occupied", PlayerSymbol.X, 1, 1).andExpect(status().isOk());

        postMove("game-occupied", PlayerSymbol.O, 1, 1)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("already occupied")));
    }

    @Test
    void reportsWinnerAndBlocksFurtherMoves() throws Exception {
        postMove("game-win", PlayerSymbol.X, 0, 0).andExpect(status().isOk());
        postMove("game-win", PlayerSymbol.O, 1, 0).andExpect(status().isOk());
        postMove("game-win", PlayerSymbol.X, 0, 1).andExpect(status().isOk());
        postMove("game-win", PlayerSymbol.O, 1, 1).andExpect(status().isOk());

        postMove("game-win", PlayerSymbol.X, 0, 2)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("X_WON"))
            .andExpect(jsonPath("$.winner").value("X"))
            .andExpect(jsonPath("$.nextPlayer").value(nullValue()));

        postMove("game-win", PlayerSymbol.O, 2, 2)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("already finished")));
    }

    @Test
    void reportsDrawWhenBoardIsFullWithoutWinner() throws Exception {
        postMove("game-draw", PlayerSymbol.X, 0, 0).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.O, 0, 1).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.X, 0, 2).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.O, 1, 1).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.X, 1, 0).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.O, 1, 2).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.X, 2, 1).andExpect(status().isOk());
        postMove("game-draw", PlayerSymbol.O, 2, 0).andExpect(status().isOk());

        postMove("game-draw", PlayerSymbol.X, 2, 2)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DRAW"))
            .andExpect(jsonPath("$.winner").value(nullValue()))
            .andExpect(jsonPath("$.nextPlayer").value(nullValue()))
            .andExpect(jsonPath("$.moveCount").value(9));
    }

    @Test
    void returnsNotFoundForUnknownGame() throws Exception {
        mockMvc.perform(get("/games/{gameId}", "missing-game"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Game 'missing-game' was not found."));
    }

    @Test
    void validatesMoveCoordinates() throws Exception {
        mockMvc.perform(
                post("/games/{gameId}/move", "game-invalid-row")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(moveJson(PlayerSymbol.X, 3, 0))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("row must be less than or equal to 2")));
    }

    private ResultActions postMove(String gameId, PlayerSymbol player, int row, int column) throws Exception {
        return mockMvc.perform(
            post("/games/{gameId}/move", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(moveJson(player, row, column))
        );
    }

    private String moveJson(PlayerSymbol player, int row, int column) {
        return """
            {
              "player": "%s",
              "row": %d,
              "column": %d
            }
            """.formatted(player, row, column);
    }

}
