package br.com.memdeck.deck.presentation;

import java.util.UUID;

import br.com.memdeck.deck.application.CreateDeckCommand;
import br.com.memdeck.deck.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDeckRequest(
		@NotBlank @Size(max = 100) String name,
		@Size(max = 500) String description,
		@NotNull Visibility visibility) {

	CreateDeckCommand toCommand(UUID ownerId) {
		return new CreateDeckCommand(ownerId, name, description, visibility);
	}
}
