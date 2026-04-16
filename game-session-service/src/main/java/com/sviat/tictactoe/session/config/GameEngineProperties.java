package com.sviat.tictactoe.session.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "game-engine")
public record GameEngineProperties(@NotBlank String baseUrl) {
}
