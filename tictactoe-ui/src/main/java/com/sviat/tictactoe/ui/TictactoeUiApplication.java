package com.sviat.tictactoe.ui;

import com.sviat.tictactoe.ui.config.GameSessionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GameSessionProperties.class)
public class TictactoeUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TictactoeUiApplication.class, args);
	}

}
