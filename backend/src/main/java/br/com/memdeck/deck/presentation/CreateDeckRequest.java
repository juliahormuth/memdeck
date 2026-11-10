package br.com.memdeck.deck.presentation;

import br.com.memdeck.deck.application.CreateDeckCommand;
import br.com.memdeck.deck.domain.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDeckRequest(
		@Schema(description = "Deck name", example = "Human anatomy") @NotBlank @Size(max = 100) String name,
		@Schema(description = "Optional description", example = "Bones and muscles") @Size(max = 500) String description,
		@Schema(description = "Who can find and review the deck") @NotNull Visibility visibility) {

	CreateDeckCommand toCommand() {
		return new CreateDeckCommand(name, description, visibility);
	}
}
