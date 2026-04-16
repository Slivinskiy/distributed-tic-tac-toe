package com.sviat.tictactoe.engine.controller;

import com.sviat.tictactoe.engine.dto.GameResponse;
import com.sviat.tictactoe.engine.dto.MoveRequest;
import com.sviat.tictactoe.engine.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/{gameId}/move")
    public GameResponse makeMove(@PathVariable String gameId, @Valid @RequestBody MoveRequest moveRequest) {
        return gameService.applyMove(gameId, moveRequest);
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(@PathVariable String gameId) {
        return gameService.getGame(gameId);
    }
}
