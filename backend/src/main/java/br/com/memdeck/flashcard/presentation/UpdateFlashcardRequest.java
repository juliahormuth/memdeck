package br.com.memdeck.flashcard.presentation;

import java.util.UUID;

import br.com.memdeck.flashcard.application.UpdateFlashcardCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFlashcardRequest(
		@NotBlank @Size(max = 500) String front,
		@NotBlank @Size(max = 10000) String back) {

	UpdateFlashcardCommand toCommand(UUID requesterId, UUID deckId, UUID flashcardId) {
		return new UpdateFlashcardCommand(requesterId, deckId, flashcardId, front, back);
	}
}
