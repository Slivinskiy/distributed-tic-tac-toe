package com.sviat.tictactoe.session.client;

import com.sviat.tictactoe.session.client.dto.EngineGameResponse;
import com.sviat.tictactoe.session.client.dto.EngineMoveRequest;
import com.sviat.tictactoe.session.config.GameEngineProperties;
import com.sviat.tictactoe.session.exception.EngineCommunicationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpGameEngineClient implements GameEngineClient {

    private final RestClient restClient;

    public HttpGameEngineClient(GameEngineProperties properties) {
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .build();
    }

    @Override
    public EngineGameResponse makeMove(String gameId, EngineMoveRequest moveRequest) {
        try {
            return restClient.post()
                .uri("/games/{gameId}/move", gameId)
                .body(moveRequest)
                .retrieve()
                .body(EngineGameResponse.class);
        }
        catch (RestClientResponseException exception) {
            String responseBody = exception.getResponseBodyAsString();
            String message = responseBody == null || responseBody.isBlank()
                ? exception.getMessage()
                : responseBody;

            throw new EngineCommunicationException(
                "Game engine rejected the request for game '%s': %s".formatted(gameId, message),
                exception
            );
        }
        catch (RestClientException exception) {
            throw new EngineCommunicationException(
                "Could not reach game engine at the configured base URL.",
                exception
            );
        }
    }
}
