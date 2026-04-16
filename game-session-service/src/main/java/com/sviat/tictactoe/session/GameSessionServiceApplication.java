package com.sviat.tictactoe.session;

import com.sviat.tictactoe.session.config.GameEngineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GameEngineProperties.class)
public class GameSessionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameSessionServiceApplication.class, args);
	}

}
