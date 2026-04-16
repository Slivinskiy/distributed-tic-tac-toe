package com.sviat.tictactoe.ui.client;

import com.sviat.tictactoe.ui.config.GameSessionProperties;
import com.sviat.tictactoe.ui.dto.SessionResponse;
import com.sviat.tictactoe.ui.exception.SessionServiceCommunicationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpGameSessionClient implements GameSessionClient {

    private final RestClient restClient;

    public HttpGameSessionClient(GameSessionProperties properties) {
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .build();
    }

    @Override
    public SessionResponse createSession() {
        try {
            return restClient.post()
                .uri("/sessions")
                .retrieve()
                .body(SessionResponse.class);
        }
        catch (RestClientResponseException exception) {
            throw new SessionServiceCommunicationException(buildErrorMessage(exception), exception);
        }
        catch (RestClientException exception) {
            throw new SessionServiceCommunicationException(
                "Could not reach game-session-service at the configured base URL.",
                exception
            );
        }
    }

    @Override
    public SessionResponse simulateSession(String sessionId) {
        try {
            return restClient.post()
                .uri("/sessions/{sessionId}/simulate", sessionId)
                .retrieve()
                .body(SessionResponse.class);
        }
        catch (RestClientResponseException exception) {
            throw new SessionServiceCommunicationException(buildErrorMessage(exception), exception);
        }
        catch (RestClientException exception) {
            throw new SessionServiceCommunicationException(
                "Could not reach game-session-service at the configured base URL.",
                exception
            );
        }
    }

    @Override
    public SessionResponse getSession(String sessionId) {
        try {
            return restClient.get()
                .uri("/sessions/{sessionId}", sessionId)
                .retrieve()
                .body(SessionResponse.class);
        }
        catch (RestClientResponseException exception) {
            throw new SessionServiceCommunicationException(buildErrorMessage(exception), exception);
        }
        catch (RestClientException exception) {
            throw new SessionServiceCommunicationException(
                "Could not reach game-session-service at the configured base URL.",
                exception
            );
        }
    }

    private String buildErrorMessage(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return exception.getMessage();
        }
        return responseBody;
    }
}
