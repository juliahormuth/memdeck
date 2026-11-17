package br.com.memdeck.flashcard.presentation;

import java.util.UUID;

import br.com.memdeck.flashcard.application.CreateFlashcardCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFlashcardRequest(
		@NotBlank @Size(max = 500) String front,
		@NotBlank @Size(max = 10000) String back) {

	CreateFlashcardCommand toCommand(UUID requesterId, UUID deckId) {
		return new CreateFlashcardCommand(requesterId, deckId, front, back);
	}
}
