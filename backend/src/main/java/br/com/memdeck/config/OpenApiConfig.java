package br.com.memdeck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
class OpenApiConfig {

	@Bean
	OpenAPI memdeckOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Memdeck API")
				.version("0.0.1")
				.description("REST API of Memdeck, a flashcard platform with spaced repetition."));
	}
}
