package com.sviat.tictactoe.ui.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "game-session")
public record GameSessionProperties(@NotBlank String baseUrl) {
}
