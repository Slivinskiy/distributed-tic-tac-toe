package com.sviat.tictactoe.session.controller;

import com.sviat.tictactoe.session.dto.SessionResponse;
import com.sviat.tictactoe.session.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public SessionResponse createSession() {
        return sessionService.createSession();
    }

    @PostMapping("/{sessionId}/simulate")
    public SessionResponse simulateSession(@PathVariable String sessionId) {
        return sessionService.simulateSession(sessionId);
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable String sessionId) {
        return sessionService.getSession(sessionId);
    }
}
