package br.com.memdeck.deck.presentation;

import java.util.UUID;

import br.com.memdeck.deck.application.UpdateDeckCommand;
import br.com.memdeck.deck.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDeckRequest(
		@NotBlank @Size(max = 100) String name,
		@Size(max = 500) String description,
		@NotNull Visibility visibility) {

	UpdateDeckCommand toCommand(UUID requesterId, UUID deckId) {
		return new UpdateDeckCommand(requesterId, deckId, name, description, visibility);
	}
}
